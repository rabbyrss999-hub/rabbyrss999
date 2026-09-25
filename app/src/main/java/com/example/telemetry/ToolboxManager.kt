package com.example.telemetry

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class LevelAngles(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val isLevel: Boolean = false,
    val isAvailable: Boolean = true
)

enum class FlashlightMode {
    OFF, ON, STROBE, SOS
}

class ToolboxManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Flashlight state
    private var torchCameraId: String? = null
    private val _torchMode = MutableStateFlow(FlashlightMode.OFF)
    val torchMode: StateFlow<FlashlightMode> = _torchMode.asStateFlow()
    private var strobeJob: Job? = null
    var strobeFrequencyHz: Int = 4

    // Level state
    private val _levelAngles = MutableStateFlow(LevelAngles())
    val levelAngles: StateFlow<LevelAngles> = _levelAngles.asStateFlow()
    private var levelSensor: Sensor? = null
    private var levelListener: SensorEventListener? = null
    private var pitchCalibrationOffset: Float = 0f
    private var rollCalibrationOffset: Float = 0f
    private var wasLevel = false

    // Decibel meter state
    private val _decibelValue = MutableStateFlow(0f)
    val decibelValue: StateFlow<Float> = _decibelValue.asStateFlow()
    private val _decibelPeak = MutableStateFlow(0f)
    val decibelPeak: StateFlow<Float> = _decibelPeak.asStateFlow()
    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()
    private var audioRecord: AudioRecord? = null
    private var audioRecordJob: Job? = null

    init {
        findTorchCamera()
    }

    private fun findTorchCamera() {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (hasFlash) {
                    torchCameraId = id
                    return
                }
            }
        } catch (_: Exception) {}
    }

    // Flashlight methods
    fun toggleFlashlight() {
        when (_torchMode.value) {
            FlashlightMode.OFF -> setTorchMode(FlashlightMode.ON)
            else -> setTorchMode(FlashlightMode.OFF)
        }
    }

    fun setTorchMode(mode: FlashlightMode) {
        strobeJob?.cancel()
        strobeJob = null

        val camId = torchCameraId
        if (camId == null) {
            // Emulators often don't have a camera flash, so track mode gracefully
            _torchMode.value = mode
            return
        }

        try {
            when (mode) {
                FlashlightMode.OFF -> {
                    cameraManager?.setTorchMode(camId, false)
                    _torchMode.value = FlashlightMode.OFF
                }
                FlashlightMode.ON -> {
                    cameraManager?.setTorchMode(camId, true)
                    _torchMode.value = FlashlightMode.ON
                }
                FlashlightMode.STROBE -> {
                    _torchMode.value = FlashlightMode.STROBE
                    strobeJob = coroutineScope.launch(Dispatchers.IO) {
                        var state = false
                        while (isActive) {
                            state = !state
                            try {
                                cameraManager?.setTorchMode(camId, state)
                            } catch (_: Exception) {}
                            val delayMs = (1000L / (strobeFrequencyHz * 2).coerceAtLeast(1))
                            delay(delayMs)
                        }
                    }
                }
                FlashlightMode.SOS -> {
                    _torchMode.value = FlashlightMode.SOS
                    strobeJob = coroutineScope.launch(Dispatchers.IO) {
                        val dot = 150L
                        val dash = 450L
                        val interElem = 150L
                        val interChar = 450L
                        val interWord = 1200L

                        val sequence = listOf(
                            dot, interElem, dot, interElem, dot, interChar, // S
                            dash, interElem, dash, interElem, dash, interChar, // O
                            dot, interElem, dot, interElem, dot, interWord // S
                        )

                        while (isActive) {
                            var flashOn = true
                            for (duration in sequence) {
                                if (!isActive) break
                                try {
                                    cameraManager?.setTorchMode(camId, flashOn)
                                } catch (_: Exception) {}
                                flashOn = !flashOn
                                delay(duration)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            _torchMode.value = mode
        }
    }

    // Bubble Level methods
    fun startLevelSensor() {
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensor == null) {
            _levelAngles.value = LevelAngles(isAvailable = false)
            return
        }
        levelSensor = sensor

        levelListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate inclination angles
                val rawRoll = (atan2(x.toDouble(), sqrt(y * y + z * z).toDouble()) * (180.0 / Math.PI)).toFloat()
                val rawPitch = (atan2(-y.toDouble(), sqrt(x * x + z * z).toDouble()) * (180.0 / Math.PI)).toFloat()

                val calibratedPitch = rawPitch - pitchCalibrationOffset
                val calibratedRoll = rawRoll - rollCalibrationOffset

                val isLevel = kotlin.math.abs(calibratedPitch) < 0.6f && kotlin.math.abs(calibratedRoll) < 0.6f

                if (isLevel && !wasLevel) {
                    // Trigger gentle haptic snap when aligned!
                    triggerHaptic(HapticPattern.CLICK)
                }
                wasLevel = isLevel

                _levelAngles.value = LevelAngles(
                    pitch = ((calibratedPitch * 10).roundToInt()) / 10f,
                    roll = ((calibratedRoll * 10).roundToInt()) / 10f,
                    isLevel = isLevel,
                    isAvailable = true
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager?.registerListener(
            levelListener,
            levelSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stopLevelSensor() {
        levelListener?.let {
            try {
                sensorManager?.unregisterListener(it)
            } catch (_: Exception) {}
        }
        levelListener = null
    }

    fun calibrateLevel() {
        val current = _levelAngles.value
        pitchCalibrationOffset += current.pitch
        rollCalibrationOffset += current.roll
        triggerHaptic(HapticPattern.DOUBLE_CLICK)
    }

    fun resetLevelCalibration() {
        pitchCalibrationOffset = 0f
        rollCalibrationOffset = 0f
        triggerHaptic(HapticPattern.TICK)
    }

    // Haptics & Vibration Lab
    enum class HapticPattern {
        TICK, CLICK, DOUBLE_CLICK, HEAVY, HEARTBEAT, SOS
    }

    fun triggerHaptic(pattern: HapticPattern) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (pattern) {
                    HapticPattern.TICK -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                    HapticPattern.CLICK -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                    HapticPattern.DOUBLE_CLICK -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                    }
                    HapticPattern.HEAVY -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    }
                    HapticPattern.HEARTBEAT -> {
                        val timings = longArrayOf(0, 100, 120, 150)
                        val amplitudes = intArrayOf(0, 180, 0, 255)
                        vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    HapticPattern.SOS -> {
                        val timings = longArrayOf(0, 80, 80, 80, 80, 80, 180, 240, 100, 240, 100, 240, 180, 80, 80, 80, 80, 80)
                        vib.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (pattern) {
                    HapticPattern.TICK -> vib.vibrate(20)
                    HapticPattern.CLICK -> vib.vibrate(40)
                    HapticPattern.DOUBLE_CLICK -> vib.vibrate(longArrayOf(0, 30, 60, 40), -1)
                    HapticPattern.HEAVY -> vib.vibrate(80)
                    HapticPattern.HEARTBEAT -> vib.vibrate(longArrayOf(0, 100, 120, 150), -1)
                    HapticPattern.SOS -> vib.vibrate(longArrayOf(0, 80, 80, 80, 80, 80, 180, 240, 100, 240, 100, 240, 180, 80, 80, 80, 80, 80), -1)
                }
            }
        } catch (_: Exception) {}
    }

    // Audio Decibel Sound Meter
    fun toggleAudioMeter() {
        if (_isRecordingAudio.value) {
            stopAudioMeter()
        } else {
            startAudioMeter()
        }
    }

    fun startAudioMeter() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        stopAudioMeter()

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            .coerceAtLeast(2048)

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                return
            }

            record.startRecording()
            audioRecord = record
            _isRecordingAudio.value = true

            audioRecordJob = coroutineScope.launch(Dispatchers.IO) {
                val buffer = ShortArray(bufferSize)
                while (isActive && _isRecordingAudio.value) {
                    val read = record.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val amplitude = sqrt(sum / read)
                        val db = if (amplitude > 1) {
                            (20.0 * log10(amplitude)).toFloat().coerceIn(0f, 100f)
                        } else {
                            0f
                        }
                        val smoothedDb = (_decibelValue.value * 0.6f) + (db * 0.4f)
                        _decibelValue.value = ((smoothedDb * 10).roundToInt()) / 10f
                        if (smoothedDb > _decibelPeak.value) {
                            _decibelPeak.value = ((smoothedDb * 10).roundToInt()) / 10f
                        }
                    }
                    delay(80)
                }
            }
        } catch (_: Exception) {
            stopAudioMeter()
        }
    }

    fun stopAudioMeter() {
        _isRecordingAudio.value = false
        audioRecordJob?.cancel()
        audioRecordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    fun resetAudioPeak() {
        _decibelPeak.value = _decibelValue.value
    }

    fun cleanup() {
        setTorchMode(FlashlightMode.OFF)
        stopLevelSensor()
        stopAudioMeter()
    }
}
