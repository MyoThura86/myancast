package com.example.myancast

import com.example.myancast.domain.model.PlaybackState
import com.example.myancast.domain.model.SleepTimerOption
import com.example.myancast.player.PlayerController
import com.example.myancast.player.PlayerQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Test အတွက် — call တွေကို record လုပ်တယ်။ A နဲ့ C ရဲ့ test တွေ သုံး။
 */
class FakePlayerController(
    initial: PlaybackState = PlaybackState()
) : PlayerController {

    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    // Recorded calls
    var playCallCount = 0
        private set
    var lastQueue: PlayerQueue? = null
        private set
    var lastPodcastTitle: String? = null
        private set
    /** play() ကို ဘယ် position ကနေ စဖွင့်ခိုင်းလဲ (ဆက်နားထောင်ရန် အတွက် — C ထည့်) */
    var lastStartPositionMs: Long? = null
        private set
    var toggleCallCount = 0
        private set
    var seekToMs: Long? = null
        private set
    var lastSpeed: Float? = null
        private set

    override fun play(queue: PlayerQueue, podcastTitle: String, startPositionMs: Long) {
        playCallCount++
        lastQueue = queue
        lastPodcastTitle = podcastTitle
        lastStartPositionMs = startPositionMs
        if (queue.isEmpty) return
        _state.update {
            it.copy(
                queue = queue.episodes,
                currentIndex = queue.startIndex,
                podcastTitle = podcastTitle,
                isPlaying = true,
                positionMs = startPositionMs
            )
        }
    }

    override fun togglePlayPause() {
        toggleCallCount++
        _state.update { it.copy(isPlaying = !it.isPlaying) }
    }

    override fun seekTo(positionMs: Long) {
        seekToMs = positionMs
        _state.update { it.copy(positionMs = positionMs) }
    }

    var skipBackCount = 0
        private set
    var skipForwardCount = 0
        private set
    var nextCount = 0
        private set
    var previousCount = 0
        private set

    override fun skipBack() { skipBackCount++ }
    override fun skipForward() { skipForwardCount++ }
    override fun next() { nextCount++ }
    override fun previous() { previousCount++ }

    override fun setSpeed(speed: Float) {
        lastSpeed = speed
        _state.update { it.copy(speed = speed) }
    }

    /** ★ Phase 6 (#3) — နောက်ဆုံး ရွေးခဲ့တဲ့ sleep timer */
    var lastSleepTimer: SleepTimerOption? = null
        private set

    override fun setSleepTimer(option: SleepTimerOption) {
        lastSleepTimer = option
        _state.update {
            when (option) {
                SleepTimerOption.Off -> it.copy(sleepTimerEndsAtMs = null, sleepAtEpisodeEnd = false)
                is SleepTimerOption.Minutes -> it.copy(sleepTimerEndsAtMs = option.minutes * 60_000L, sleepAtEpisodeEnd = false)
                SleepTimerOption.EndOfEpisode -> it.copy(sleepTimerEndsAtMs = null, sleepAtEpisodeEnd = true)
            }
        }
    }

    // ★ Test အတွက် helper — state ကို ပြောင်း
    fun setState(newState: PlaybackState) {
        _state.value = newState
    }
}