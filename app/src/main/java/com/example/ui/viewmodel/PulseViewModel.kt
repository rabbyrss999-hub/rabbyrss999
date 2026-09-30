package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PulseDatabase
import com.example.data.gemini.GeminiSearchService
import com.example.data.gemini.GroundingSource
import com.example.data.model.AppShortcutItem
import com.example.data.model.QuickNote
import com.example.data.model.TaskItem
import com.example.data.preferences.AppPreferencesManager
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class NavigationTab {
    OVERVIEW, TOOLBOX, TASKS_NOTES, DEVICE_SPECS
}

enum class TaskFilter {
    ALL, PENDING, COMPLETED, HIGH_PRIORITY
}

data class TechSearchState(
    val query: String = "",
    val answer: String = "",
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PulseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseDatabase.getDatabase(application, viewModelScope)
    private val repository = PulseRepository(database.pulseDao())
    val preferencesManager = AppPreferencesManager(application)
    val telemetryManager = DeviceTelemetryManager(application)
    val toolboxManager = ToolboxManager(application, viewModelScope)
    private val geminiSearchService = GeminiSearchService()

    // Persistent Wallpaper & Preferences
    val currentWallpaperId: StateFlow<String> = preferencesManager.wallpaperIdFlow
    val currentWallpaperDim: StateFlow<Float> = preferencesManager.wallpaperDimFlow
    val lastBackupTime: StateFlow<Long> = preferencesManager.lastBackupTimeFlow
    val isDataProtectionEnabled: StateFlow<Boolean> = preferencesManager.dataProtectionEnabledFlow

    private val dataVaultFile = File(application.filesDir, "pulse_permanent_data_vault.json")

    fun triggerAutoVaultSave() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backupJson = exportDataBackupJson()
                dataVaultFile.writeText(backupJson)
            } catch (_: Exception) {}
        }
    }

    fun toggleDataProtection() {
        val current = preferencesManager.isDataProtectionEnabled()
        preferencesManager.setDataProtectionEnabled(!current)
        toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
        triggerAutoVaultSave()
    }

    fun setWallpaper(wallpaperId: String) {
        preferencesManager.setWallpaperId(wallpaperId)
        toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.TICK)
        triggerAutoVaultSave()
    }

    fun setWallpaperDim(dim: Float) {
        preferencesManager.setWallpaperDim(dim)
        triggerAutoVaultSave()
    }

    // App & Link Shortcuts (Persistent in Room)
    val allShortcuts: StateFlow<List<AppShortcutItem>> = repository.allShortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addShortcut(name: String, urlOrPackage: String, iconType: String = "web", category: String = "Favorite") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertShortcut(
                AppShortcutItem(
                    name = name.trim(),
                    urlOrPackage = urlOrPackage.trim(),
                    iconType = iconType,
                    category = category,
                    isPinned = true
                )
            )
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
            triggerAutoVaultSave()
        }
    }

    fun deleteShortcut(shortcutId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShortcut(shortcutId)
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
            triggerAutoVaultSave()
        }
    }

    fun toggleShortcutPin(shortcut: AppShortcutItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShortcut(shortcut.copy(isPinned = !shortcut.isPinned))
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.TICK)
            triggerAutoVaultSave()
        }
    }

    // Full Backup & Restore System (Ensures Zero Data Loss)
    suspend fun exportDataBackupJson(): String {
        val tasks = repository.getAllTasksList()
        val notes = repository.getAllNotesList()
        val shortcuts = repository.getAllShortcutsList()
        val wallpaperId = preferencesManager.getWallpaperId()

        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("wallpaperId", wallpaperId)

        val tasksArray = JSONArray()
        for (t in tasks) {
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("category", t.category)
            obj.put("priority", t.priority)
            obj.put("isCompleted", t.isCompleted)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        val notesArray = JSONArray()
        for (n in notes) {
            val obj = JSONObject()
            obj.put("title", n.title)
            obj.put("content", n.content)
            obj.put("tag", n.tag)
            obj.put("colorHex", n.colorHex)
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        val shortcutsArray = JSONArray()
        for (s in shortcuts) {
            val obj = JSONObject()
            obj.put("name", s.name)
            obj.put("urlOrPackage", s.urlOrPackage)
            obj.put("iconType", s.iconType)
            obj.put("category", s.category)
            obj.put("isPinned", s.isPinned)
            shortcutsArray.put(obj)
        }
        root.put("shortcuts", shortcutsArray)

        preferencesManager.updateBackupTimestamp()
        return root.toString(2)
    }

    suspend fun restoreDataBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val wallpaperId = root.optString("wallpaperId")
            if (wallpaperId.isNotBlank()) {
                preferencesManager.setWallpaperId(wallpaperId)
            }

            val tasksList = mutableListOf<TaskItem>()
            val tasksArray = root.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    tasksList.add(
                        TaskItem(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "General"),
                            priority = obj.optString("priority", "Medium"),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
            }

            val notesList = mutableListOf<QuickNote>()
            val notesArray = root.optJSONArray("notes")
            if (notesArray != null) {
                for (i in 0 until notesArray.length()) {
                    val obj = notesArray.getJSONObject(i)
                    notesList.add(
                        QuickNote(
                            title = obj.getString("title"),
                            content = obj.optString("content", ""),
                            tag = obj.optString("tag", "General"),
                            colorHex = obj.optString("colorHex", "#06B6D4")
                        )
                    )
                }
            }

            val shortcutsList = mutableListOf<AppShortcutItem>()
            val shortcutsArray = root.optJSONArray("shortcuts")
            if (shortcutsArray != null) {
                for (i in 0 until shortcutsArray.length()) {
                    val obj = shortcutsArray.getJSONObject(i)
                    shortcutsList.add(
                        AppShortcutItem(
                            name = obj.getString("name"),
                            urlOrPackage = obj.getString("urlOrPackage"),
                            iconType = obj.optString("iconType", "web"),
                            category = obj.optString("category", "Favorite"),
                            isPinned = obj.optBoolean("isPinned", true)
                        )
                    )
                }
            }

            repository.restoreData(tasksList, notesList, shortcutsList, overwrite = true)
            preferencesManager.updateBackupTimestamp()
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.DOUBLE_CLICK)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Gemini Google Search Grounding State
    private val _techSearchState = MutableStateFlow(TechSearchState())
    val techSearchState: StateFlow<TechSearchState> = _techSearchState.asStateFlow()

    fun performTechSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        _techSearchState.value = _techSearchState.value.copy(
            query = trimmed,
            isLoading = true,
            error = null
        )
        viewModelScope.launch(Dispatchers.IO) {
            val result = geminiSearchService.searchWithGoogleGrounding(trimmed)
            result.fold(
                onSuccess = { res ->
                    _techSearchState.value = TechSearchState(
                        query = res.query,
                        answer = res.answer,
                        searchQueries = res.searchQueries,
                        sources = res.sources,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { err ->
                    _techSearchState.value = _techSearchState.value.copy(
                        isLoading = false,
                        error = err.message ?: "Failed to perform search"
                    )
                }
            )
        }
    }

    fun clearTechSearch() {
        _techSearchState.value = TechSearchState()
    }

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
        // Auto-check and restore from permanent disk vault on startup
        viewModelScope.launch(Dispatchers.IO) {
            delay(400) // allow DB callbacks to settle
            try {
                if (dataVaultFile.exists()) {
                    val currentTasks = repository.getAllTasksList()
                    val currentShortcuts = repository.getAllShortcutsList()
                    if (currentTasks.isEmpty() || currentShortcuts.isEmpty()) {
                        val json = dataVaultFile.readText()
                        restoreDataBackupJson(json)
                    }
                } else {
                    triggerAutoVaultSave()
                }
            } catch (_: Exception) {}
        }

        // Periodic refresh for telemetry (storage, ram, network, uptime)
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                refreshTelemetry()
                delay(3000)
            }
        }
    }

    fun savePermanentDataSnapshotNow(onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backupJson = exportDataBackupJson()
                dataVaultFile.writeText(backupJson)
                toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.DOUBLE_CLICK)
                onResult(true)
            } catch (_: Exception) {
                onResult(false)
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
            triggerAutoVaultSave()
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
            triggerAutoVaultSave()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(taskId)
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
            triggerAutoVaultSave()
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
            triggerAutoVaultSave()
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(noteId)
            toolboxManager.triggerHaptic(ToolboxManager.HapticPattern.CLICK)
            triggerAutoVaultSave()
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
