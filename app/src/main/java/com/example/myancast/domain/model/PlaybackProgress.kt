package com.example.myancast.domain.model

/**
 * Episode တစ်ခုကို ဘယ်အထိ နားထောင်ထားလဲ — Room မှာ သိမ်း၊ Home နဲ့ Library မှာ ပြ။
 * Firestore နဲ့ မဆိုင်လို့ `is` prefix သုံးလို့ရတယ်။
 */
data class PlaybackProgress(
    val episodeId: String,
    val podcastId: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,          // System.currentTimeMillis()
    val episodeTitle: String = "",
    val podcastTitle: String = "",
    val coverUrl: String = ""
) {
    val fraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    /** ၉၅% ကျော် နားထောင်ပြီးရင် "ပြီးပြီ" — Continue Listening မှာ မပြတော့ဘူး */
    val isFinished: Boolean get() = fraction >= 0.95f
}