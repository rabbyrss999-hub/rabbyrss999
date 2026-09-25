package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PulseDatabase
import com.example.data.model.QuickNote
import com.example.data.model.TaskItem
import com.example.data.repository.PulseRepository
import com.example.telemetry.BatteryTelemetry
import com.example.telemetry.DeviceHardwareInfo
import com.example.telemetry.DeviceTelemetryManager
import com.example.telemetry.FlashlightMode
import com.example.telemetry.LevelAngles
import com.example.telemetry.MemoryTelemetry
import com.example.telemetry.NetworkTelemetry
import com.example.telemetry.StorageTelemetry
import com.example.telemetry.ToolboxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class NavigationTab {
    OVERVIEW, TOOLBOX, TASKS_NOTES, DEVICE_SPECS
}

enum class TaskFilter {
    ALL, PENDING, COMPLETED, HIGH_PRIORITY
}

class PulseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseDatabase.getDatabase(application, viewModelScope)
    private val repository = PulseRepository(database.pulseDao())
    val telemetryManager = DeviceTelemetryManager(application)
    val toolboxManager = ToolboxManager(application, viewModelScope)

    // Current Screen Navigation
    private val _currentTab = MutableStateFlow(NavigationTab.OVERVIEW)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    // Battery Telemetry
    val batteryTelemetry: StateFlow<BatteryTelemetry> = telemetryManager.getBatteryTelemetryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryTelemetry())

    // Storage Telemetry
    private val _storageTelemetry = MutableStateFlow(telemetryManager.getStorageTelemetry())
    val storageTelemetry: StateFlow<StorageTelemetry> = _storageTelemetry.asStateFlow()

    // Memory Telemetry
    private val _memoryTelemetry = MutableStateFlow(telemetryManager.getMemoryTelemetry())
    val memoryTelemetry: StateFlow<MemoryTelemetry> = _memoryTelemetry.asStateFlow()

    // Network Telemetry
    private val _networkTelemetry = MutableStateFlow(telemetryManager.getNetworkTelemetry())
    val networkTelemetry: StateFlow<NetworkTelemetry> = _networkTelemetry.asStateFlow()

    // Hardware Specs
    private val _hardwareInfo = MutableStateFlow(telemetryManager.getDeviceHardwareInfo())
    val hardwareInfo: StateFlow<DeviceHardwareInfo> = _hardwareInfo.asStateFlow()

    // Toolbox State delegations
    val torchMode: StateFlow<FlashlightMode> = toolboxManager.torchMode
    val levelAngles: StateFlow<LevelAngles> = toolboxManager.levelAngles
    val decibelValue: StateFlow<Float> = toolboxManager.decibelValue
    val decibelPeak: StateFlow<Float> = toolboxManager.decibelPeak
    val isRecordingAudio: StateFlow<Boolean> = toolboxManager.isRecordingAudio

    // Tasks & Notes Flow
    val allTasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<QuickNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Task Filter & Search
    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Pixel Test Dialog
    private val _isPixelTestActive = MutableStateFlow(false)
    val isPixelTestActive: StateFlow<Boolean> = _isPixelTestActive.asStateFlow()

    private val _pixelColorIndex = MutableStateFlow(0)
    val pixelColorIndex: StateFlow<Int> = _pixelColorIndex.asStateFlow()

    init {
        // Periodic refresh for telemetry (storage, ram, network, uptime)
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                refreshTelemetry()
                delay(3000)
            }
        }
    }

    fun refreshTelemetry() {
        _storageTelemetry.value = telemetryManager.getStorageTelemetry()
        _memoryTelemetry.value = telemetryManager.getMemoryTelemetry()
        _networkTelemetry.value = telemetryManager.getNetworkTelemetry()
        _hardwareInfo.value = telemetryManager.getDeviceHardwareInfo()
    }

    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Task operations
    fun addTask(title: String, description: String, category: String, priority: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(
                TaskItem(
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    priority = priority,
                    isCompleted = false
                )
            )
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.TICK)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(taskId)
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
        }
    }

    // Note operations
    fun addNote(title: String, content: String, tag: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(
                QuickNote(
                    title = title.trim(),
                    content = content.trim(),
                    tag = tag,
                    colorHex = colorHex
                )
            )
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(noteId)
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
        }
    }

    // Pixel Test controls
    fun startPixelTest() {
        _pixelColorIndex.value = 0
        _isPixelTestActive.value = true
        toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
    }

    fun nextPixelColor() {
        _pixelColorIndex.value = (_pixelColorIndex.value + 1) % 6
        toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.TICK)
    }

    fun stopPixelTest() {
        _isPixelTestActive.value = false
    }

    // System Settings Launcher
    fun openSystemSetting(action: String) {
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
            // Fallback to main settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                getApplication<Application>().startActivity(fallbackIntent)
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        toolboxManager.cleanup()
    }
}
