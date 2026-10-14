package com.example.myancast.data.settings

import android.content.Context
import androidx.core.content.edit
import com.example.myancast.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * ★ Phase 6 (#1) — DataStore က spec မှာ ပိတ်ထားလို့ SharedPreferences သုံး။
 * App စဖွင့်ချိန် တစ်ခါ ဖတ်ပြီး memory (StateFlow) ထဲ ထား · ပြောင်းတိုင်း `apply()` နဲ့ ရေး (main thread မပိတ်)။
 */
class SharedPrefsSettingsRepository(context: Context) : SettingsRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override fun update(transform: (AppSettings) -> AppSettings) {
        val updated = _settings.updateAndGet(transform)
        prefs.edit {
            putBoolean(KEY_DARK_MODE, updated.darkMode)
            putBoolean(KEY_DATA_SAVER, updated.dataSaver)
            putBoolean(KEY_WIFI_ONLY_DOWNLOAD, updated.wifiOnlyDownload)
        }
    }

    private fun read(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            darkMode = prefs.getBoolean(KEY_DARK_MODE, defaults.darkMode),
            dataSaver = prefs.getBoolean(KEY_DATA_SAVER, defaults.dataSaver),
            wifiOnlyDownload = prefs.getBoolean(KEY_WIFI_ONLY_DOWNLOAD, defaults.wifiOnlyDownload)
        )
    }

    private companion object {
        const val PREFS_NAME = "myancast_settings"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_DATA_SAVER = "data_saver"
        const val KEY_WIFI_ONLY_DOWNLOAD = "wifi_only_download"
    }
}
