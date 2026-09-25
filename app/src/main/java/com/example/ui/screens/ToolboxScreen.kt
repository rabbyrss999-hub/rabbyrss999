package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telemetry.FlashlightMode
import com.example.telemetry.ToolboxManager
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.PulseViewModel

@Composable
fun ToolboxScreen(
    viewModel: PulseViewModel
) {
    val context = LocalContext.current
    val levelAngles by viewModel.levelAngles.collectAsStateWithLifecycle()
    val torchMode by viewModel.torchMode.collectAsStateWithLifecycle()
    val decibelValue by viewModel.decibelValue.collectAsStateWithLifecycle()
    val decibelPeak by viewModel.decibelPeak.collectAsStateWithLifecycle()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsStateWithLifecycle()

    var strobeFreq by remember { mutableFloatStateOf(4f) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toolboxManager.startAudioMeter()
        }
    }

    // Lifecycle for sensors: start accelerometer when Toolbox is visible, stop when hidden
    DisposableEffect(Unit) {
        viewModel.toolboxManager.startLevelSensor()
        onDispose {
            viewModel.toolboxManager.stopLevelSensor()
            viewModel.toolboxManager.stopAudioMeter()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("toolbox_screen_list"),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 28.dp
        )
    ) {
        // Section: Spirit / Bubble Level
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TealSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Level Tool",
                                    tint = TealSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Digital Bubble Level",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Dual-axis orientation & tilt gauge",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (levelAngles.isLevel) AccentGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (levelAngles.isLevel) "LEVEL (0°)" else "ACTIVE",
                                color = if (levelAngles.isLevel) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Circular Spirit Level Canvas
                    val isAligned = levelAngles.isLevel
                    val bubbleColor by animateColorAsState(
                        targetValue = if (isAligned) AccentGreen else CyanPrimary,
                        label = "bubble_color"
                    )

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF090E17))
                            .border(
                                width = 3.dp,
                                color = if (isAligned) AccentGreen else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = (size.width / 2f) - 16.dp.toPx()

                            // Outer reference ring
                            drawCircle(
                                color = Color(0x33475569),
                                radius = maxRadius * 0.66f,
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Inner bullseye ring (target zone)
                            drawCircle(
                                color = if (isAligned) AccentGreen.copy(alpha = 0.6f) else Color(0x6606B6D4),
                                radius = 24.dp.toPx(),
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Crosshair lines
                            drawLine(
                                color = Color(0x44475569),
                                start = Offset(center.x, 12.dp.toPx()),
                                end = Offset(center.x, size.height - 12.dp.toPx()),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color(0x44475569),
                                start = Offset(12.dp.toPx(), center.y),
                                end = Offset(size.width - 12.dp.toPx(), center.y),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Calculate bubble offset from roll & pitch (capped at radius)
                            val scale = maxRadius / 35f // 35 degrees = edge
                            val offsetX = (levelAngles.roll * scale).coerceIn(-maxRadius, maxRadius)
                            val offsetY = (levelAngles.pitch * scale).coerceIn(-maxRadius, maxRadius)
                            val bubbleCenter = Offset(center.x + offsetX, center.y + offsetY)

                            // Bubble glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(bubbleColor.copy(alpha = 0.8f), bubbleColor.copy(alpha = 0.1f)),
                                    center = bubbleCenter,
                                    radius = 26.dp.toPx()
                                ),
                                radius = 26.dp.toPx(),
                                center = bubbleCenter
                            )

                            // Solid bubble center
                            drawCircle(
                                color = bubbleColor,
                                radius = 16.dp.toPx(),
                                center = bubbleCenter
                            )
                        }

                        // Center readout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 110.dp)
                        ) {
                            Text(
                                text = "${levelAngles.pitch}° | ${levelAngles.roll}°",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Angle indicators readout row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PITCH (Y-AXIS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${levelAngles.pitch}°",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ROLL (X-AXIS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${levelAngles.roll}°",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calibration action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.calibrateLevel() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_calibrate_level")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Adjust,
                                contentDescription = "Calibrate",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Calibrate Zero", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.resetLevelCalibration() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_reset_level")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Reset", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Flashlight & Strobe & SOS Suite
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashlightOn,
                                    contentDescription = "Flashlight",
                                    tint = AccentAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Torch & Emergency Strobe",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Hardware LED flash controls",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (torchMode != FlashlightMode.OFF) AccentAmber else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = torchMode.name,
                                color = if (torchMode != FlashlightMode.OFF) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode selection chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = torchMode == FlashlightMode.OFF,
                            onClick = { viewModel.toolboxManager.setTorchMode(FlashlightMode.OFF) },
                            label = { Text("Off") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = torchMode == FlashlightMode.ON,
                            onClick = { viewModel.toolboxManager.setTorchMode(FlashlightMode.ON) },
                            label = { Text("Constant") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = torchMode == FlashlightMode.STROBE,
                            onClick = { viewModel.toolboxManager.setTorchMode(FlashlightMode.STROBE) },
                            label = { Text("Strobe") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = torchMode == FlashlightMode.SOS,
                            onClick = { viewModel.toolboxManager.setTorchMode(FlashlightMode.SOS) },
                            label = { Text("SOS") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (torchMode == FlashlightMode.STROBE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Strobe Frequency",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${strobeFreq.toInt()} Hz",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentAmber
                            )
                        }

                        Slider(
                            value = strobeFreq,
                            onValueChange = {
                                strobeFreq = it
                                viewModel.toolboxManager.strobeFrequencyHz = it.toInt()
                            },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentAmber,
                                activeTrackColor = AccentAmber
                            )
                        )
                    }
                }
            }
        }

        // Section: Haptics & Vibration Lab
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = "Haptics Lab",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Haptic & Vibration Lab",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Linear motor test patterns",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of 6 Haptic Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.TICK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_haptic_tick")
                        ) {
                            Text("Tick")
                        }

                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_haptic_click")
                        ) {
                            Text("Click")
                        }

                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.DOUBLE_CLICK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_haptic_double")
                        ) {
                            Text("Double")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.HEAVY) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_haptic_heavy")
                        ) {
                            Text("Heavy")
                        }

                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.HEARTBEAT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_haptic_heartbeat")
                        ) {
                            Text("Heartbeat")
                        }

                        OutlinedButton(
                            onClick = { viewModel.toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.SOS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_haptic_sos")
                        ) {
                            Text("SOS Pulse")
                        }
                    }
                }
            }
        }

        // Section: Decibel Sound Meter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Decibel Meter",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Sound & Decibel Meter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Real-time acoustic noise level",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!hasPerm) {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    viewModel.toolboxManager.toggleAudioMeter()
                                }
                            },
                            modifier = Modifier.testTag("btn_toggle_audio_meter")
                        ) {
                            Icon(
                                imageVector = if (isRecordingAudio) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Toggle Mic",
                                tint = if (isRecordingAudio) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = if (isRecordingAudio) "${decibelValue.toInt()} dB" else "-- dB",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (decibelValue > 85f) AccentRed else if (decibelValue > 65f) AccentAmber else AccentGreen
                            )
                            Text(
                                text = when {
                                    !isRecordingAudio -> "Tap mic icon to start acoustic monitor"
                                    decibelValue < 35 -> "Whisper / Quiet Library"
                                    decibelValue < 55 -> "Moderate Quiet / Office"
                                    decibelValue < 75 -> "Conversational / Street Noise"
                                    else -> "High Noise / Hearing Danger"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isRecordingAudio) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Peak: ${decibelPeak.toInt()} dB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Reset Peak",
                                    fontSize = 11.sp,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { viewModel.toolboxManager.resetAudioPeak() }
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val animatedDb by animateFloatAsState(
                        targetValue = (decibelValue / 100f).coerceIn(0f, 1f),
                        label = "animated_db"
                    )

                    LinearProgressIndicator(
                        progress = { if (isRecordingAudio) animatedDb else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (decibelValue > 85f) AccentRed else if (decibelValue > 65f) AccentAmber else AccentGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // Section: Display Pixel Inspection Tool
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenSearchDesktop,
                                contentDescription = "Pixel Test",
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Screen & Pixel Tester",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Full-screen RGB color cycling to find stuck or dead pixels",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.startPixelTest() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.testTag("btn_start_pixel_test")
                    ) {
                        Text("Start Test")
                    }
                }
            }
        }
    }
}
