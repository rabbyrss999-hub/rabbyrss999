package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddNoteDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.PixelTestFullscreenOverlay
import com.example.ui.screens.DeviceSpecsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.TasksNotesScreen
import com.example.ui.screens.ToolboxScreen
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.PulseViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MobilePulseApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobilePulseApp(viewModel: PulseViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val battery by viewModel.batteryTelemetry.collectAsStateWithLifecycle()
    val isPixelTestActive by viewModel.isPixelTestActive.collectAsStateWithLifecycle()
    val pixelColorIndex by viewModel.pixelColorIndex.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary)
                        )
                        Text(
                            text = "MobilePulse",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    // Battery indicator pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { viewModel.selectTab(NavigationTab.DEVICE_SPECS) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.Bolt,
                                contentDescription = "Battery",
                                tint = if (battery.isCharging) AccentGreen else CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${battery.level}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshTelemetry() },
                        modifier = Modifier.testTag("btn_top_refresh")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.OVERVIEW,
                    onClick = { viewModel.selectTab(NavigationTab.OVERVIEW) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.OVERVIEW) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Overview"
                        )
                    },
                    label = { Text("Overview") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_overview")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.TOOLBOX,
                    onClick = { viewModel.selectTab(NavigationTab.TOOLBOX) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.TOOLBOX) Icons.Filled.Build else Icons.Outlined.Build,
                            contentDescription = "Toolbox"
                        )
                    },
                    label = { Text("Toolbox") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_toolbox")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.TASKS_NOTES,
                    onClick = { viewModel.selectTab(NavigationTab.TASKS_NOTES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.TASKS_NOTES) Icons.Filled.TaskAlt else Icons.Outlined.TaskAlt,
                            contentDescription = "Tasks & Notes"
                        )
                    },
                    label = { Text("Productivity") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_tasks")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.DEVICE_SPECS,
                    onClick = { viewModel.selectTab(NavigationTab.DEVICE_SPECS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.DEVICE_SPECS) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = "Device Specs"
                        )
                    },
                    label = { Text("Specs") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_specs")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(
                targetState = currentTab,
                label = "screen_crossfade"
            ) { tab ->
                when (tab) {
                    NavigationTab.OVERVIEW -> OverviewScreen(
                        viewModel = viewModel,
                        onNavigateTab = { viewModel.selectTab(it) },
                        onOpenAddTask = { showAddTaskDialog = true }
                    )
                    NavigationTab.TOOLBOX -> ToolboxScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.TASKS_NOTES -> TasksNotesScreen(
                        viewModel = viewModel,
                        onOpenAddTask = { showAddTaskDialog = true },
                        onOpenAddNote = { showAddNoteDialog = true }
                    )
                    NavigationTab.DEVICE_SPECS -> DeviceSpecsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, description, category, priority ->
                viewModel.addTask(title, description, category, priority)
            }
        )
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { title, content, tag, colorHex ->
                viewModel.addNote(title, content, tag, colorHex)
            }
        )
    }

    // Fullscreen Screen/Pixel Test Overlay
    if (isPixelTestActive) {
        PixelTestFullscreenOverlay(
            colorIndex = pixelColorIndex,
            onNextColor = { viewModel.nextPixelColor() },
            onDismiss = { viewModel.stopPixelTest() }
        )
    }
}
