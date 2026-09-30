package com.example.ui.components

import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.WallpaperRegistry
import com.example.ui.theme.WallpaperTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperDataDialog(
    currentWallpaperId: String,
    currentWallpaperDim: Float,
    onSelectWallpaper: (String) -> Unit,
    onUpdateWallpaperDim: (Float) -> Unit,
    onExportBackup: suspend () -> String,
    onRestoreBackup: suspend (String) -> Boolean,
    onAddShortcut: (name: String, url: String, type: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Wallpaper, 1 = Data Protection & Backup

    var selectedWpId by remember { mutableStateOf(currentWallpaperId) }
    var dimValue by remember { mutableFloatStateOf(currentWallpaperDim) }

    // Restore dialog state
    var showRestoreInput by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var showAddAppDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(680.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_wallpaper_data"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Wallpaper & App Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ওয়ালপেপার ও ডাটা স্থায়ী সংরক্ষণ",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Wallpapers vs Data Protection
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = CyanPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyanPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("ওয়ালপেপার (Wallpapers)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("ডাটা সেভার (Data)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedTab == 0) {
                        // --- WALLPAPERS TAB ---
                        Text(
                            text = "SELECT PERMANENT WALLPAPER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        // Wallpaper Grid
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            WallpaperRegistry.allWallpapers.forEach { wp ->
                                val isSelected = selectedWpId == wp.id

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedWpId = wp.id
                                            onSelectWallpaper(wp.id)
                                            Toast.makeText(context, "${wp.name} wallpaper applied & saved permanently!", Toast.LENGTH_SHORT).show()
                                        }
                                        .testTag("wallpaper_item_${wp.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Gradient Preview Box
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(wp.gradientBrush)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(CyanPrimary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = wp.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSelected) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = CyanPrimary.copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVE",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = CyanPrimary,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = wp.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                selectedWpId = wp.id
                                                onSelectWallpaper(wp.id)
                                                Toast.makeText(context, "${wp.name} wallpaper saved!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = if (isSelected) {
                                                ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                                            } else {
                                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                                            },
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(if (isSelected) "Active" else "Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Brightness / Opacity Slider
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Background Opacity / Tint",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${(dimValue * 100).toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        color = CyanPrimary
                                    )
                                }
                                Slider(
                                    value = dimValue,
                                    onValueChange = {
                                        dimValue = it
                                        onUpdateWallpaperDim(it)
                                    },
                                    valueRange = 0.3f..1.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = CyanPrimary,
                                        activeTrackColor = CyanPrimary
                                    )
                                )
                            }
                        }

                        // Set as Android System Wallpaper Option
                        OutlinedButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val activeWp = WallpaperRegistry.getWallpaperById(selectedWpId)
                                        val wallpaperManager = WallpaperManager.getInstance(context)
                                        val width = 1080
                                        val height = 1920
                                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                        val canvas = Canvas(bitmap)
                                        val paint = Paint()

                                        val colorsInt = activeWp.previewColors.map { it.toArgb() }.toIntArray()
                                        val shader = android.graphics.LinearGradient(
                                            0f, 0f, 0f, height.toFloat(),
                                            colorsInt, null, android.graphics.Shader.TileMode.CLAMP
                                        )
                                        paint.shader = shader
                                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                                        wallpaperManager.setBitmap(bitmap)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "System Wallpaper set successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Applied to app! (System wallpaper requires device permission)", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_set_system_wallpaper"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set as Device System Wallpaper", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                    } else {
                        // --- DATA PROTECTION & BACKUP TAB ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AccentGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ডাটা সুরক্ষা সক্রিয় (Permanent Storage)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "আপনার সমস্ত ওয়ালপেপার, টাস্ক, নোটস এবং অ্যাপ শর্টকাট স্বয়ংক্রিয়ভাবে ডিভাইসে সেভ করা থাকে। অ্যাপ বন্ধ বা রিস্টার্ট হলেও কোনো ডাটা মুছবে না।",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Backup & Export Actions
                        Text(
                            text = "BACKUP & RESTORE DATA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val backupJson = onExportBackup()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("MobilePulse Backup", backupJson)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Full Backup copied to clipboard! Keep it safe.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_backup_json"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Backup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showRestoreInput = !showRestoreInput },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_toggle_restore_input"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore Data", fontSize = 12.sp)
                            }
                        }

                        // Restore Input Box
                        if (showRestoreInput) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Paste Backup JSON code below to restore:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    OutlinedTextField(
                                        value = restoreJsonText,
                                        onValueChange = { restoreJsonText = it },
                                        placeholder = { Text("Paste JSON here...", fontSize = 11.sp) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .testTag("input_restore_json"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (restoreJsonText.isNotBlank()) {
                                                scope.launch {
                                                    val success = onRestoreBackup(restoreJsonText.trim())
                                                    if (success) {
                                                        Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()
                                                        showRestoreInput = false
                                                        restoreJsonText = ""
                                                    } else {
                                                        Toast.makeText(context, "Invalid backup JSON format.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Apply Restore", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Quick Add App Shortcut Button
                        Text(
                            text = "CUSTOM APPS & SHORTCUTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        OutlinedButton(
                            onClick = { showAddAppDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_open_add_app_shortcut"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নতুন অ্যাপ বা ওয়েবসাইট লিঙ্ক যুক্ত করুন (+ Add Shortcut)", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto-Save: Always On",
                        fontSize = 11.sp,
                        color = AccentGreen,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                    ) {
                        Text("Done (সম্পন্ন)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Add Custom Shortcut Subdialog
    if (showAddAppDialog) {
        var appName by remember { mutableStateOf("") }
        var appUrl by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddAppDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "নতুন অ্যাপ বা লিঙ্ক যোগ করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App or Link Name (যেমন: YouTube, PFX)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = appUrl,
                        onValueChange = { appUrl = it },
                        label = { Text("URL or Package (যেমন: https://...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddAppDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (appName.isNotBlank() && appUrl.isNotBlank()) {
                                    onAddShortcut(appName.trim(), appUrl.trim(), "web")
                                    Toast.makeText(context, "$appName added and saved!", Toast.LENGTH_SHORT).show()
                                    showAddAppDialog = false
                                }
                            }
                        ) {
                            Text("Save Permanently")
                        }
                    }
                }
            }
        }
    }
}
