package com.example.myancast.data.admin

import kotlinx.coroutines.flow.Flow

enum class MediaKind { IMAGE, AUDIO }

sealed interface UploadEvent {
    /** 0f..1f */
    data class Progress(val fraction: Float) : UploadEvent
    /** တင်ပြီး — Firestore ထဲ သိမ်းရမယ့် https download URL */
    data class Done(val url: String) : UploadEvent
}

/**
 * ★ Phase 6 — ဖုန်းထဲက ပုံ / MP3 ကို cloud ပေါ် တင်သူ။
 * Flow ကို collect ရပ်ရင် (screen ပိတ်) upload ကိုပါ ရပ်တယ်။ မအောင်မြင်ရင် [AdminException]။
 */
interface MediaUploader {
    fun upload(localUri: String, kind: MediaKind, storagePath: String): Flow<UploadEvent>

    /** ဒီ app က တင်ထားတဲ့ ဖိုင်ဆိုမှ ဖျက် (အင်တာနက် လင့်ခ်ဆို ဘာမှ မလုပ်) — မအောင်မြင်လည်း မပစ် */
    suspend fun deleteIfOwned(url: String)
}

const val STORAGE_PODCASTS = "podcasts"
const val STORAGE_EPISODES = "episodes"

/**
 * "podcasts/<id>/cover_1760000000000.jpg" · "episodes/<id>/audio_….mp3"
 * နာမည်ထဲ အချိန် ထည့်တယ် — ပုံ ပြောင်းရင် URL အသစ် ဖြစ်လို့ ဖုန်းတွေက cache ထဲက ပုံဟောင်း မပြတော့ဘူး။
 */
fun mediaStoragePath(folder: String, ownerId: String, kind: MediaKind, nowMillis: Long): String =
    when (kind) {
        MediaKind.IMAGE -> "$folder/$ownerId/cover_$nowMillis.jpg"
        MediaKind.AUDIO -> "$folder/$ownerId/audio_$nowMillis.mp3"
    }
