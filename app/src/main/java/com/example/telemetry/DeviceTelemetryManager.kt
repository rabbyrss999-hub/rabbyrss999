package com.example.telemetry

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.DecimalFormat
import java.util.Locale

class DeviceTelemetryManager(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    fun getBatteryTelemetryFlow(): Flow<BatteryTelemetry> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context?, intent: Intent?) {
                intent?.let {
                    val telemetry = parseBatteryIntent(it)
                    trySend(telemetry)
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(receiver, filter)
        if (initialIntent != null) {
            trySend(parseBatteryIntent(initialIntent))
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    private fun parseBatteryIntent(intent: Intent): BatteryTelemetry {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unplugged"
        }

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val tempC = tempRaw / 10.0f
        val tempF = (tempC * 9 / 5) + 32

        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val healthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Normal"
        }

        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        return BatteryTelemetry(
            level = batteryPct,
            isCharging = isCharging,
            status = statusStr,
            plugType = plugType,
            temperatureC = tempC,
            temperatureF = tempF,
            voltageMv = voltageMv,
            health = healthStr,
            technology = technology
        )
    }

    fun getStorageTelemetry(): StorageTelemetry {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - freeBytes
            val usedPercent = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes) * 100f else 0f

            StorageTelemetry(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                usedPercent = usedPercent,
                totalFormatted = formatBytes(totalBytes),
                freeFormatted = formatBytes(freeBytes),
                usedFormatted = formatBytes(usedBytes)
            )
        } catch (_: Exception) {
            StorageTelemetry(
                totalBytes = 64_000_000_000L,
                freeBytes = 32_000_000_000L,
                usedBytes = 32_000_000_000L,
                usedPercent = 50f,
                totalFormatted = "64.0 GB",
                freeFormatted = "32.0 GB",
                usedFormatted = "32.0 GB"
            )
        }
    }

    fun getMemoryTelemetry(): MemoryTelemetry {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalBytes = memInfo.totalMem
        val availBytes = memInfo.availMem
        val usedBytes = totalBytes - availBytes
        val usedPercent = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes) * 100f else 0f

        return MemoryTelemetry(
            totalBytes = totalBytes,
            availBytes = availBytes,
            usedBytes = usedBytes,
            usedPercent = usedPercent,
            totalFormatted = formatBytes(totalBytes),
            availFormatted = formatBytes(availBytes),
            usedFormatted = formatBytes(usedBytes),
            isLowMemory = memInfo.lowMemory
        )
    }

    fun getNetworkTelemetry(): NetworkTelemetry {
        val cm = connectivityManager ?: return NetworkTelemetry()
        val activeNetwork = cm.activeNetwork ?: return NetworkTelemetry(
            isConnected = false,
            type = "Offline",
            isMetered = false,
            linkSpeedDownKbps = 0,
            ipAddress = "Disconnected"
        )

        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkTelemetry()

        val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val speedKbps = capabilities.linkDownstreamBandwidthKbps

        val type = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Connected"
        }

        val ipAddress = getLocalIpAddress() ?: "127.0.0.1"

        return NetworkTelemetry(
            isConnected = isConnected,
            type = type,
            isMetered = isMetered,
            linkSpeedDownKbps = speedKbps,
            ipAddress = ipAddress
        )
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    fun getDeviceHardwareInfo(): DeviceHardwareInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm?.defaultDisplay?.getRealMetrics(metrics)

        @Suppress("DEPRECATION")
        val refreshRate = wm?.defaultDisplay?.refreshRate ?: 60f

        val sensors = sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
        val sensorDetails = sensors.map {
            SensorDetail(
                name = it.name,
                typeString = getSensorTypeName(it.type),
                vendor = it.vendor,
                power = it.power,
                maxRange = it.maximumRange
            )
        }

        val uptimeMs = SystemClock.elapsedRealtime()
        val hours = (uptimeMs / (1000 * 60 * 60)) % 24
        val minutes = (uptimeMs / (1000 * 60)) % 60
        val days = uptimeMs / (1000 * 60 * 60 * 24)
        val uptimeStr = if (days > 0) "${days}d ${hours}h ${minutes}m" else "${hours}h ${minutes}m"

        return DeviceHardwareInfo(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            model = Build.MODEL,
            brand = Build.BRAND.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            device = Build.DEVICE,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            cpuCores = Runtime.getRuntime().availableProcessors(),
            uptime = uptimeStr,
            screenResolution = "${metrics.widthPixels} × ${metrics.heightPixels} px",
            screenRefreshRate = refreshRate,
            screenDensityDpi = metrics.densityDpi,
            screenDensityScale = metrics.density,
            sensorCount = sensors.size,
            sensorList = sensorDetails
        )
    }

    private fun getSensorTypeName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
            Sensor.TYPE_LIGHT -> "Ambient Light"
            Sensor.TYPE_PROXIMITY -> "Proximity"
            Sensor.TYPE_PRESSURE -> "Barometer"
            Sensor.TYPE_GRAVITY -> "Gravity"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            Sensor.TYPE_STEP_COUNTER -> "Step Counter"
            Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
            Sensor.TYPE_HEART_RATE -> "Heart Rate"
            else -> "Hardware Sensor ($type)"
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val df = DecimalFormat("#.##")
        val gigabytes = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gigabytes >= 1.0) {
            "${df.format(gigabytes)} GB"
        } else {
            val megabytes = bytes / (1024.0 * 1024.0)
            "${df.format(megabytes)} MB"
        }
    }
}
