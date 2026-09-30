package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CyanPrimary

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, category: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mobile") }
    var priority by remember { mutableStateOf("Medium") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Maintenance Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details (optional)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_desc")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Mobile", "Battery", "Storage", "Hardware").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Low", "Medium", "High").forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, category, priority)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_add_task")
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, tag: String, colorHex: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Mobile") }
    var selectedColorHex by remember { mutableStateOf("#06B6D4") }

    val colorOptions = listOf("#06B6D4", "#10B981", "#8B5CF6", "#F59E0B", "#EF4444", "#3B82F6")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Quick Note",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_note_title")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_note_content")
                )

                Text(
                    text = "Color Accent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = hex }
                        ) {
                            if (selectedColorHex == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content, tag, selectedColorHex)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_add_note")
            ) {
                Text("Save Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PixelTestFullscreenOverlay(
    colorIndex: Int,
    onNextColor: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        Color.Red to "RED (#FF0000)",
        Color.Green to "GREEN (#00FF00)",
        Color.Blue to "BLUE (#0000FF)",
        Color.White to "WHITE (#FFFFFF)",
        Color.Black to "BLACK (#000000)",
        Color.Yellow to "YELLOW (#FFFF00)"
    )

    val (currentColor, colorName) = colors[colorIndex.coerceIn(0, colors.size - 1)]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentColor)
                .clickable { onNextColor() }
                .testTag("pixel_test_screen"),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.Black.copy(alpha = 0.75f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Color: $colorName • Tap screen to cycle",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Pixel Test",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PcDownloadGuideDialog(
    onDismiss: () -> Unit,
    ipAddress: String = ""
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("bn") } // "bn" or "en"
    var activeTab by remember { mutableStateOf(0) } // 0: Download, 1: Run on PC, 2: Phone Install

    val isBangla = selectedLanguage == "bn"

    val guideTextBn = """
=== পিসিতে এই অ্যাপটি ডাউনলোড ও চালানোর সম্পূর্ণ গাইড ===

১. AI Studio থেকে ডাউনলোড করার নিয়ম:
• উপরে ডানদিকের Settings / Export মেনু ক্লিক করুন।
• "Download ZIP" নির্বাচন করুন (সম্পূর্ণ প্রজেক্ট ডাউনলোড হবে)।
• অথবা "Generate APK" নির্বাচন করুন (মোবাইল ও এমুলেটরের জন্য সরাসরি ইনস্টলেশন ফাইল)।

২. পিসিতে (Computer) যেভাবে চালাবেন:
• পদ্ধতি ১ (সবচেয়ে সহজ - Emulator):
  1. আপনার পিসিতে BlueStacks, LDPlayer বা NoxPlayer ওপেন করুন।
  2. ডাউনলোড করা .apk ফাইলটি ড্র্যাগ করে এমুলেটর স্ক্রিনে ছেড়ে দিন।
  3. অ্যাপটি পিসির স্ক্রিনে সরাসরি চলতে শুরু করবে।
• পদ্ধতি ২ (Android Studio - প্রজেক্ট রান):
  1. ডাউনলোড করা ZIP ফাইলটি Unzip / Extract করুন।
  2. Android Studio চালু করে "Open..." দিয়ে ফোল্ডারটি ওপেন করুন।
  3. উপরের প্লে (Run) বাটনে চাপলেই পিসির ভার্চুয়াল ফোনে অ্যাপ চালু হবে।

৩. আসল ফোনে ইনস্টল করার নিয়ম:
• APK ফাইলটি পেনড্রাইভ, ব্লুটুথ বা গুগল ড্রাইভ দিয়ে ফোনে পাঠিয়ে ইনস্টল করে নিন।
    """.trimIndent()

    val guideTextEn = """
=== How to Download & Run MobilePulse on your PC ===

1. Download from AI Studio:
• Click on the top-right Settings / Export icon in AI Studio.
• Select "Download ZIP" to download the full project source code.
• Or select "Generate APK" to get the direct installation package.

2. How to Run on PC:
• Method 1 (Quickest - Android Emulator):
  1. Install BlueStacks, LDPlayer, or NoxPlayer on your PC.
  2. Drag and drop the downloaded .apk file into the emulator.
  3. The app will launch immediately on your computer screen.
• Method 2 (Android Studio):
  1. Unzip the downloaded project ZIP file.
  2. Open Android Studio and select "Open Project".
  3. Click the Run (Play) button to launch it in the official virtual device.

3. Install on Physical Phone:
• Transfer the APK to your phone via USB cable, Bluetooth, or Google Drive, and tap to install.
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(640.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_pc_download_guide"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header Row
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
                                imageVector = Icons.Default.Laptop,
                                contentDescription = "PC Download",
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isBangla) "পিসিতে ডাউনলোড গাইড" else "PC Download & Setup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBangla) "কম্পিউটারে চালানোর উপায়" else "How to run on PC / Mobile",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Language toggle chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBangla) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedLanguage = "bn" }
                        ) {
                            Text(
                                text = "বাংলা",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBangla) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isBangla) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedLanguage = "en" }
                        ) {
                            Text(
                                text = "EN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isBangla) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = if (isBangla) {
                        listOf("১. পিসিতে ডাউনলোড", "২. পিসিতে চালানো", "৩. ফোনে ইনস্টল")
                    } else {
                        listOf("1. Download", "2. Run on PC", "3. On Phone")
                    }

                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = activeTab == index
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = index }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content Section
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (activeTab) {
                        0 -> {
                            // Download Steps
                            GuideStepCard(
                                stepNumber = "১" ,
                                title = if (isBangla) "কম্পিউটার স্ক্রিনের উপরে ডান কোণায় দেখুন" else "Look at the Top-Right Corner",
                                description = if (isBangla)
                                    "আপনার ব্রাউজার উইন্ডোর একদম উপরের ডান কোণে (Top Right) Settings (⚙️ গিয়ার আইকন) বা তিনটি ডট (⋮) রয়েছে। সেখানে ক্লিক করুন।"
                                else
                                    "Look at the top-right corner of your browser in AI Studio. Click on the Settings (gear icon ⚙️) or the three-dots (⋮) menu.",
                                icon = Icons.Default.Download,
                                accentColor = CyanPrimary
                            )

                            GuideStepCard(
                                stepNumber = "২",
                                title = if (isBangla) "সেটিংস থেকে 'Download ZIP' বা 'Export' বাটন" else "Click 'Download ZIP' or 'Export'",
                                description = if (isBangla)
                                    "সেটিংস প্যানেলে 'Export project as ZIP' বা 'Download ZIP' বাটনটি পেয়ে যাবেন। ক্লিক করলেই আপনার পিসিতে পুরো ফাইল ডাউনলোড হয়ে যাবে।"
                                else
                                    "Inside Settings, you will see 'Export project as ZIP' or 'Download ZIP'. Click it to download the full Android project to your PC.",
                                icon = Icons.Default.Laptop,
                                accentColor = AccentGreen
                            )

                            GuideStepCard(
                                stepNumber = "৩",
                                title = if (isBangla) "সরাসরি APK তৈরি বা গিটহাব" else "Generate APK or GitHub",
                                description = if (isBangla)
                                    "সেটিংস থেকে সরাসরি APK তৈরি করতে পারেন অথবা 'Push to GitHub' করে সরাসরি কোড ক্লোন করতে পারেন।"
                                else
                                    "You can also use 'Generate APK' or 'Push to GitHub' to clone the repository directly to your machine.",
                                icon = Icons.Default.PhoneAndroid,
                                accentColor = AccentAmber
                            )
                        }
                        1 -> {
                            // Run on PC
                            GuideStepCard(
                                stepNumber = "পদ্ধতি ১",
                                title = if (isBangla) "BlueStacks / LDPlayer এমুলেটর (সবচেয়ে সহজ)" else "Android Emulator (BlueStacks / LDPlayer)",
                                description = if (isBangla)
                                    "১. আপনার পিসিতে BlueStacks বা LDPlayer ওপেন করুন।\n২. ডাউনলোড করা .apk ফাইলটি ড্র্যাগ করে এমুলেটরে ফেলুন।\n৩. চোখের পলকে অ্যাপটি কম্পিউটারের স্ক্রিনে ভার্চুয়াল ফোন হিসেবে ওপেন হবে।"
                                else
                                    "1. Launch BlueStacks, LDPlayer, or Nox on your PC.\n2. Drag & drop the downloaded .apk into the emulator.\n3. The app will immediately install and run just like on a real phone.",
                                icon = Icons.Default.PlayArrow,
                                accentColor = AccentGreen
                            )

                            GuideStepCard(
                                stepNumber = "পদ্ধতি ২",
                                title = if (isBangla) "Android Studio তে চালানো (প্রোজেক্ট রান)" else "Run in Android Studio",
                                description = if (isBangla)
                                    "১. ডাউনলোড করা ZIP ফাইলটি Unzip করুন।\n২. Android Studio ওপেন করে \"Open Project\" দিয়ে ফোল্ডারটি খুলুন।\n৩. উপরের প্লে (Run) বাটনে চাপলে পিসির অফিসিয়াল ভার্চুয়াল ফোনে চলবে।"
                                else
                                    "1. Extract the downloaded ZIP file.\n2. In Android Studio, click File > Open and select the extracted folder.\n3. Click the green 'Run' (Play) button to launch it in the Android Emulator.",
                                icon = Icons.Default.Computer,
                                accentColor = CyanPrimary
                            )
                        }
                        2 -> {
                            // On Phone
                            GuideStepCard(
                                stepNumber = "১",
                                title = if (isBangla) "ফোনে ফাইল ট্রান্সফার করুন" else "Transfer APK to Phone",
                                description = if (isBangla)
                                    "ডাউনলোড করা APK ফাইলটি USB ক্যাবল, পেনড্রাইভ, ব্লুটুথ অথবা Google Drive/WhatsApp দিয়ে আপনার স্মার্টফোনে পাঠান।"
                                else
                                    "Send the APK file to your smartphone via USB cable, Bluetooth, or Google Drive / WhatsApp.",
                                icon = Icons.Default.Share,
                                accentColor = AccentBlue
                            )

                            GuideStepCard(
                                stepNumber = "২",
                                title = if (isBangla) "ফোনে ইনস্টল ও রান করুন" else "Install & Enjoy",
                                description = if (isBangla)
                                    "ফোনের ফাইল ম্যানেজারে গিয়ে APK ফাইলে ট্যাপ করুন এবং \"Install\" চাপুন। সরাসরি সব হার্ডওয়্যার সেন্সর ফিচার কাজ করবে!"
                                else
                                    "Tap the APK on your phone's File Manager and allow install from unknown sources. All hardware telemetry & features will work natively!",
                                icon = Icons.Default.Check,
                                accentColor = AccentGreen
                            )
                        }
                    }

                    if (ipAddress.isNotBlank() && ipAddress != "Unavailable") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isBangla) "ডিভাইস আইপি: $ipAddress (পিসি ADB পেয়ারিংয়ে কার্যকর)" else "Device IP: $ipAddress (useful for wireless ADB debugging)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val textToCopy = if (isBangla) guideTextBn else guideTextEn
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("PC Guide", textToCopy)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                if (isBangla) "গাইড ক্লিপবোর্ডে কপি করা হয়েছে!" else "Guide copied to clipboard!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_pc_guide"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "গাইড কপি করুন" else "Copy Guide",
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_dismiss_pc_guide"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                    ) {
                        Text(
                            text = if (isBangla) "ঠিক আছে" else "Close",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = stepNumber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
