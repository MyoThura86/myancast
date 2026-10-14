package com.example.myancast.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.domain.model.PlaybackState
import com.example.myancast.domain.model.SleepTimerOption
import com.example.myancast.domain.util.formatDuration
import com.example.myancast.domain.util.sleepTimerLabel
import com.example.myancast.player.PlayerController
import com.example.myancast.player.nextSpeed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


// ─────────────────────────────────────────────
// ၁။ UI State
// ─────────────────────────────────────────────

/**
 * Full Player ရဲ့ UI state — PlaybackState ကနေ toUiState() နဲ့ map။
 * အချိန် / speed စာသားတွေကို ဒီမှာ format ပြီးသား (composable ထဲမှာ မ format ရ)။
 */
data class PlayerUiState(
    val hasEpisode: Boolean = false,
    val title: String = "",
    val podcastTitle: String = "",
    val coverUrl: String = "",
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val progress: Float = 0f,          // 0f..1f — slider
    val durationMs: Long = 0L,         // drag နေတုန်း အချိန် ပြဖို့
    val positionText: String = "0:00",
    val durationText: String = "0:00",
    val speedText: String = "1x",
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val error: String? = null,
    // ★ Phase 6 (#3)
    val sleepTimerLabel: String = "",            // "၁၂ မိနစ် ကျန်" · "" = timer မရှိ
    val sleepAtEpisodeEnd: Boolean = false
) {
    val sleepTimerActive: Boolean get() = sleepTimerLabel.isNotEmpty()
}

/**
 * Pure mapping — coroutine မလို၊ JVM ပေါ်မှာ test လို့ရ
 * @param nowMs sleep timer "ကျန်ချိန်" တွက်ဖို့ (test မှာ ပုံသေ ထား)
 */
fun PlaybackState.toUiState(nowMs: Long = System.currentTimeMillis()): PlayerUiState {
    val episode = currentEpisode ?: return PlayerUiState(speedText = formatSpeed(speed))

    // Stream က duration နောက်မှ ပေး — မသိသေးရင် Firestore ရဲ့ episode.duration (စက္ကန့်) ကို သုံး
    val totalMs = if (durationMs > 0) durationMs else episode.duration * 1000L
    val fraction = if (totalMs > 0) (positionMs.toFloat() / totalMs).coerceIn(0f, 1f) else 0f

    return PlayerUiState(
        hasEpisode = true,
        title = episode.title,
        podcastTitle = podcastTitle,
        coverUrl = episode.coverUrl,
        isPlaying = isPlaying,
        isBuffering = isBuffering,
        progress = fraction,
        durationMs = totalMs,
        positionText = formatMs(positionMs),
        durationText = formatMs(totalMs),
        speedText = formatSpeed(speed),
        hasNext = hasNext,
        hasPrevious = hasPrevious,
        error = error,
        sleepTimerLabel = sleepTimerLabel(sleepTimerEndsAtMs, sleepAtEpisodeEnd, nowMs),
        sleepAtEpisodeEnd = sleepAtEpisodeEnd
    )
}

/** 1f → "1x"၊ 1.5f → "1.5x"၊ 0.75f → "0.75x" */
fun formatSpeed(speed: Float): String {
    val text = if (speed % 1f == 0f) speed.toInt().toString()
    else speed.toString().trimEnd('0')
    return "${text}x"
}

/** Millisecond → "24:30" */
fun formatMs(ms: Long): String = formatDuration((ms / 1000).toInt())


// ─────────────────────────────────────────────
// ၂။ ViewModel
// ─────────────────────────────────────────────

class PlayerViewModel(
    private val controller: PlayerController
) : ViewModel() {

    val state: StateFlow<PlayerUiState> = controller.state
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = controller.state.value.toUiState()
        )

    fun togglePlayPause() = controller.togglePlayPause()
    fun skipBack() = controller.skipBack()
    fun skipForward() = controller.skipForward()
    fun next() = controller.next()
    fun previous() = controller.previous()

    /** Slider လွှတ်လိုက်ရင် — 0f..1f ကို ms ပြောင်းပြီး seek */
    fun seekToFraction(fraction: Float) {
        val total = controller.state.value.toUiState().durationMs
        if (total <= 0) return
        controller.seekTo((total * fraction.coerceIn(0f, 1f)).toLong())
    }

    /** ★ Phase 6 (#3) — 🌙 menu */
    fun setSleepTimer(option: SleepTimerOption) = controller.setSleepTimer(option)

    /** 0.5 → 0.75 → 1 → 1.25 → 1.5 → 2 → 0.5 */
    fun cycleSpeed() {
        val current = controller.state.value.speed
        controller.setSpeed(nextSpeed(current))
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                PlayerViewModel(app.playerController)
            }
        }
    }
}
