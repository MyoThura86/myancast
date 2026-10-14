package com.example.myancast.data.settings

import com.example.myancast.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

/** ★ Phase 6 Contract (#1) — ViewModel/UI က SharedPreferences ကို မသိရအောင် */
interface SettingsRepository {
    val settings: StateFlow<AppSettings>
    fun update(transform: (AppSettings) -> AppSettings)
}
