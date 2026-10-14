package com.example.myancast

import com.example.myancast.data.settings.SettingsRepository
import com.example.myancast.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** ★ Phase 6 — memory ထဲမှာပဲ ထားတဲ့ settings (ViewModel test အတွက်) */
class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val _settings = MutableStateFlow(initial)
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override fun update(transform: (AppSettings) -> AppSettings) {
        _settings.update(transform)
    }
}
