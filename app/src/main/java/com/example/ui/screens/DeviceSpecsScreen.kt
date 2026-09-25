package com.example.ui.screens

import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telemetry.SensorDetail
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.PulseViewModel

@Composable
fun DeviceSpecsScreen(
    viewModel: PulseViewModel
) {
    val hardware by viewModel.hardwareInfo.collectAsStateWithLifecycle()
    val battery by viewModel.batteryTelemetry.collectAsStateWithLifecycle()
    val storage by viewModel.storageTelemetry.collectAsStateWithLifecycle()
    val memory by viewModel.memoryTelemetry.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("device_specs_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 32.dp
        )
    ) {
        // Quick System Settings Shortcuts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "SYSTEM SETTINGS SHORTCUTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ShortcutItem(
                            modifier = Modifier.weight(1f),
                            title = "Battery",
                            icon = Icons.Default.BatterySaver,
                            color = AccentGreen,
                            onClick = { viewModel.openSystemSetting(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
                        )

                        ShortcutItem(
                            modifier = Modifier.weight(1f),
                            title = "Wi-Fi",
                            icon = Icons.Default.Wifi,
                            color = CyanPrimary,
                            onClick = { viewModel.openSystemSetting(Settings.ACTION_WIFI_SETTINGS) }
                        )

                        ShortcutItem(
                            modifier = Modifier.weight(1f),
                            title = "Display",
                            icon = Icons.Default.DisplaySettings,
                            color = AccentBlue,
                            onClick = { viewModel.openSystemSetting(Settings.ACTION_DISPLAY_SETTINGS) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ShortcutItem(
                            modifier = Modifier.weight(1f),
                            title = "Storage",
                            icon = Icons.Default.SdStorage,
                            color = TealSecondary,
                            onClick = { viewModel.openSystemSetting(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }
                        )

                        ShortcutItem(
                            modifier = Modifier.weight(1f),
                            title = "Alerts",
                            icon = Icons.Default.Notifications,
                            color = AccentPurple,
                            onClick = { viewModel.openSystemSetting(Settings.ACTION_APP_NOTIFICATION_SETTINGS) }
                        )

                        ShortcutItem(
                            modifier = Modifier.weight(1f),
                            title = "Dev Options",
                            icon = Icons.Default.DeveloperMode,
                            color = AccentAmber,
                            onClick = { viewModel.openSystemSetting(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS) }
                        )
                    }
                }
            }
        }

        // Section: Processor & Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    SpecHeader(
                        title = "SoC & Hardware Architecture",
                        icon = Icons.Default.Memory,
                        accentColor = CyanPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SpecRow(label = "Processor Architecture", value = hardware.cpuAbi)
                    SpecRow(label = "Available CPU Cores", value = "${hardware.cpuCores} Cores")
                    SpecRow(label = "Board / Platform", value = hardware.board)
                    SpecRow(label = "Hardware Tag", value = hardware.hardware)
                    SpecRow(label = "Device Codename", value = hardware.device)
                }
            }
        }

        // Section: Android OS & Kernel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    SpecHeader(
                        title = "Android Operating System",
                        icon = Icons.Default.Android,
                        accentColor = AccentGreen
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SpecRow(label = "Android Version", value = "Android ${hardware.androidVersion}")
                    SpecRow(label = "API Level", value = "API ${hardware.apiLevel}")
                    SpecRow(label = "Security Patch", value = hardware.securityPatch)
                    SpecRow(label = "System Uptime", value = hardware.uptime)
                    SpecRow(label = "Manufacturer", value = hardware.manufacturer)
                    SpecRow(label = "Model", value = hardware.model)
                }
            }
        }

        // Section: Display Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    SpecHeader(
                        title = "Display & Screen Metrics",
                        icon = Icons.Default.Smartphone,
                        accentColor = AccentBlue
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SpecRow(label = "Resolution", value = hardware.screenResolution)
                    SpecRow(label = "Refresh Rate", value = "${hardware.screenRefreshRate.toInt()} Hz")
                    SpecRow(label = "Density DPI", value = "${hardware.screenDensityDpi} dpi")
                    SpecRow(label = "Density Scale Factor", value = "${hardware.screenDensityScale}x")
                }
            }
        }

        // Section: Physical Sensors Inventory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpecHeader(
                            title = "Sensor Suite Inventory",
                            icon = Icons.Default.Sensors,
                            accentColor = AccentPurple
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentPurple.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${hardware.sensorCount} Detected",
                                color = AccentPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (hardware.sensorList.isEmpty()) {
                        Text(
                            text = "No hardware sensors reported in this environment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            hardware.sensorList.take(12).forEach { sensor ->
                                SensorItemRow(sensor = sensor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecHeader(
    title: String,
    icon: ImageVector,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SensorItemRow(sensor: SensorDetail) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${sensor.typeString} • ${sensor.vendor}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${"%.2f".format(sensor.power)} mA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentAmber
            )
        }
    }
}

@Composable
fun ShortcutItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
