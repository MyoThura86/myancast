package com.example.myancast.player

import com.example.myancast.domain.model.PlaybackState
import com.example.myancast.domain.model.SleepTimerOption
import kotlinx.coroutines.flow.StateFlow

/**
 * Player ကို ထိန်းတဲ့ API — ViewModel တွေက ဒါကိုပဲ သိတယ် (Media3 ကို မသိဘူး)။
 * App တစ်ခုလုံးမှာ instance တစ်ခုတည်း (MyanCastApp.playerController)။
 */
interface PlayerController {
    val state: StateFlow<PlaybackState>

    /**
     * queue.isEmpty ဆိုရင် ဘာမှ မလုပ်ဘူး။
     *
     * @param startPositionMs စဖွင့်မယ့် နေရာ (ms) — "ဆက်နားထောင်ရန်" အတွက်။
     *   `play()` ပြီးမှ `seekTo()` ခေါ်တာ **မရဘူး**: service မချိတ်ရသေးရင် seek က ပျောက်သွားတယ်။
     */
    fun play(queue: PlayerQueue, podcastTitle: String, startPositionMs: Long = 0L)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun skipBack()        // -15s
    fun skipForward()     // +30s
    fun next()
    fun previous()
    fun setSpeed(speed: Float)

    /** ★ Phase 6 (#3) — default က ဘာမှ မလုပ်ဘူး (fake တွေ ပြင်စရာ မလို) */
    fun setSleepTimer(option: SleepTimerOption) {}
}