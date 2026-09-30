package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File

class AppPreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "mobile_pulse_prefs",
        Context.MODE_PRIVATE
    )

    private val vaultFile: File = File(context.filesDir, "pulse_preferences_vault.json")

    companion object {
        const val KEY_WALLPAPER_ID = "pref_wallpaper_id"
        const val KEY_WALLPAPER_DIM = "pref_wallpaper_dim"
        const val KEY_CUSTOM_WALLPAPER_URI = "pref_custom_wallpaper_uri"
        const val KEY_LAST_BACKUP = "pref_last_backup_time"
        const val KEY_DATA_PROTECTION_ENABLED = "pref_data_protection_enabled"

        const val DEFAULT_WALLPAPER_ID = "cyber_pulse"
        const val DEFAULT_WALLPAPER_DIM = 0.85f
    }

    private val _wallpaperIdFlow = MutableStateFlow(DEFAULT_WALLPAPER_ID)
    val wallpaperIdFlow: StateFlow<String> = _wallpaperIdFlow.asStateFlow()

    private val _wallpaperDimFlow = MutableStateFlow(DEFAULT_WALLPAPER_DIM)
    val wallpaperDimFlow: StateFlow<Float> = _wallpaperDimFlow.asStateFlow()

    private val _customWallpaperUriFlow = MutableStateFlow<String?>(null)
    val customWallpaperUriFlow: StateFlow<String?> = _customWallpaperUriFlow.asStateFlow()

    private val _lastBackupTimeFlow = MutableStateFlow(System.currentTimeMillis())
    val lastBackupTimeFlow: StateFlow<Long> = _lastBackupTimeFlow.asStateFlow()

    private val _dataProtectionEnabledFlow = MutableStateFlow(true)
    val dataProtectionEnabledFlow: StateFlow<Boolean> = _dataProtectionEnabledFlow.asStateFlow()

    init {
        // 1. Initial read from SharedPreferences
        var wpId = prefs.getString(KEY_WALLPAPER_ID, null)
        var dim = prefs.getFloat(KEY_WALLPAPER_DIM, -1f)
        var customUri = prefs.getString(KEY_CUSTOM_WALLPAPER_URI, null)
        var protection = prefs.getBoolean(KEY_DATA_PROTECTION_ENABLED, true)

        // 2. If SharedPreferences was reset/cleared or default, check the filesystem vault file
        if (wpId == null && vaultFile.exists()) {
            try {
                val jsonStr = vaultFile.readText()
                val json = JSONObject(jsonStr)
                wpId = json.optString("wallpaperId", DEFAULT_WALLPAPER_ID)
                dim = json.optDouble("wallpaperDim", DEFAULT_WALLPAPER_DIM.toDouble()).toFloat()
                customUri = json.optString("customUri", "").takeIf { it != "null" && it.isNotBlank() }
                protection = json.optBoolean("dataProtection", true)

                // Restore back to SharedPreferences synchronously
                prefs.edit()
                    .putString(KEY_WALLPAPER_ID, wpId)
                    .putFloat(KEY_WALLPAPER_DIM, dim)
                    .putString(KEY_CUSTOM_WALLPAPER_URI, customUri)
                    .putBoolean(KEY_DATA_PROTECTION_ENABLED, protection)
                    .commit()
            } catch (e: Exception) {
                Log.e("AppPreferencesManager", "Failed to restore from vault file", e)
            }
        }

        val resolvedWpId = wpId ?: DEFAULT_WALLPAPER_ID
        val resolvedDim = if (dim > 0f) dim else DEFAULT_WALLPAPER_DIM

        _wallpaperIdFlow.value = resolvedWpId
        _wallpaperDimFlow.value = resolvedDim
        _customWallpaperUriFlow.value = customUri
        _lastBackupTimeFlow.value = prefs.getLong(KEY_LAST_BACKUP, System.currentTimeMillis())
        _dataProtectionEnabledFlow.value = protection

        // Write immediate backup to vault file
        saveToVaultFile(resolvedWpId, resolvedDim, customUri, protection)
    }

    private fun saveToVaultFile(wpId: String, dim: Float, customUri: String?, protection: Boolean) {
        try {
            val json = JSONObject().apply {
                put("wallpaperId", wpId)
                put("wallpaperDim", dim.toDouble())
                put("customUri", customUri ?: JSONObject.NULL)
                put("dataProtection", protection)
                put("updatedAt", System.currentTimeMillis())
            }
            vaultFile.writeText(json.toString(2))
        } catch (e: Exception) {
            Log.e("AppPreferencesManager", "Failed to save vault file", e)
        }
    }

    fun setWallpaperId(wallpaperId: String) {
        prefs.edit().putString(KEY_WALLPAPER_ID, wallpaperId).commit()
        _wallpaperIdFlow.value = wallpaperId
        saveToVaultFile(wallpaperId, _wallpaperDimFlow.value, _customWallpaperUriFlow.value, _dataProtectionEnabledFlow.value)
    }

    fun setWallpaperDim(dim: Float) {
        val clamped = dim.coerceIn(0.2f, 1.0f)
        prefs.edit().putFloat(KEY_WALLPAPER_DIM, clamped).commit()
        _wallpaperDimFlow.value = clamped
        saveToVaultFile(_wallpaperIdFlow.value, clamped, _customWallpaperUriFlow.value, _dataProtectionEnabledFlow.value)
    }

    fun setCustomWallpaperUri(uri: String?) {
        prefs.edit().putString(KEY_CUSTOM_WALLPAPER_URI, uri).commit()
        _customWallpaperUriFlow.value = uri
        saveToVaultFile(_wallpaperIdFlow.value, _wallpaperDimFlow.value, uri, _dataProtectionEnabledFlow.value)
    }

    fun setDataProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_PROTECTION_ENABLED, enabled).commit()
        _dataProtectionEnabledFlow.value = enabled
        saveToVaultFile(_wallpaperIdFlow.value, _wallpaperDimFlow.value, _customWallpaperUriFlow.value, enabled)
    }

    fun updateBackupTimestamp() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST_BACKUP, now).commit()
        _lastBackupTimeFlow.value = now
    }

    fun getWallpaperId(): String = _wallpaperIdFlow.value
    fun getWallpaperDim(): Float = _wallpaperDimFlow.value
    fun getCustomWallpaperUri(): String? = _customWallpaperUriFlow.value
    fun getLastBackupTime(): Long = _lastBackupTimeFlow.value
    fun isDataProtectionEnabled(): Boolean = _dataProtectionEnabledFlow.value
}
