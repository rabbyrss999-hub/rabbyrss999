package com.example.telemetry

data class BatteryTelemetry(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val status: String = "Unknown",
    val plugType: String = "Battery",
    val temperatureC: Float = 0f,
    val temperatureF: Float = 0f,
    val voltageMv: Int = 0,
    val health: String = "Good",
    val technology: String = "Li-ion"
)

data class StorageTelemetry(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Float = 0f,
    val totalFormatted: String = "0 GB",
    val freeFormatted: String = "0 GB",
    val usedFormatted: String = "0 GB"
)

data class MemoryTelemetry(
    val totalBytes: Long = 0L,
    val availBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Float = 0f,
    val totalFormatted: String = "0 GB",
    val availFormatted: String = "0 GB",
    val usedFormatted: String = "0 GB",
    val isLowMemory: Boolean = false
)

data class NetworkTelemetry(
    val isConnected: Boolean = false,
    val type: String = "Offline",
    val isMetered: Boolean = false,
    val linkSpeedDownKbps: Int = 0,
    val ipAddress: String = "Unavailable"
)

data class SensorDetail(
    val name: String,
    val typeString: String,
    val vendor: String,
    val power: Float,
    val maxRange: Float
)

data class DeviceHardwareInfo(
    val manufacturer: String = "",
    val model: String = "",
    val brand: String = "",
    val device: String = "",
    val board: String = "",
    val hardware: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    val securityPatch: String = "",
    val cpuAbi: String = "",
    val cpuCores: Int = 0,
    val uptime: String = "",
    val screenResolution: String = "",
    val screenRefreshRate: Float = 60f,
    val screenDensityDpi: Int = 0,
    val screenDensityScale: Float = 1f,
    val sensorCount: Int = 0,
    val sensorList: List<SensorDetail> = emptyList()
)
