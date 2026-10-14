package com.example.myancast.domain.model

/**
 * Player ရဲ့ လက်ရှိ အခြေအနေ — app တစ်ခုလုံး ဒီ class တစ်ခုတည်းကို ဖတ်တယ်။
 * PlayerController (B) က ထုတ်ပေး၊ MiniPlayer (A) နဲ့ FullPlayer (C) က ဖတ်။
 * Firestore နဲ့ map မလုပ်လို့ `is` prefix သုံးလို့ရတယ်။
 */
data class PlaybackState(
    val queue: List<Episode> = emptyList(),   // ဖွင့်မယ့် episode စာရင်း
    val currentIndex: Int = -1,               // queue ထဲက လက်ရှိ — -1 = ဘာမှ မဖွင့်ရသေး
    val podcastTitle: String = "",            // Mini player subtitle အတွက်
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,                // 0 = မသိရသေး (stream load နေဆဲ)
    val speed: Float = 1f,
    val error: String? = null,
    // ★ Phase 6 (#3) — sleep timer (additive — default ပါလို့ ရှိပြီးသား code မပျက်)
    val sleepTimerEndsAtMs: Long? = null,     // wall clock · null = မိနစ် timer မရှိ
    val sleepAtEpisodeEnd: Boolean = false
) {
    val currentEpisode: Episode? get() = queue.getOrNull(currentIndex)
    val hasEpisode: Boolean get() = currentEpisode != null
    val hasNext: Boolean get() = currentIndex in 0 until queue.lastIndex
    val hasPrevious: Boolean get() = currentIndex > 0
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}