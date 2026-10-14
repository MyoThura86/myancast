package com.example.myancast.data.admin

import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import com.google.firebase.Timestamp

/**
 * ★ Phase 6 — Firestore collection / field နာမည်များ (README ရဲ့ schema နဲ့ အတူတူ)။
 * App က **ဖတ်တဲ့** model (`Podcast`, `Episode`) ရဲ့ property နာမည်တွေနဲ့ တစ်လုံးမှ မလွဲရ —
 * လွဲရင် admin တင်တာ app မှာ ပေါ်မလာဘူး (`FirestoreSchemaTest` က စစ်တယ်)။
 */
object FirestoreSchema {
    const val PODCASTS = "podcasts"
    const val EPISODES = "episodes"

    object PodcastField {
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val COVER_URL = "coverUrl"
        const val CATEGORY = "category"
        const val EPISODE_COUNT = "episodeCount"   // number (integer)
        const val UPDATED_AT = "updatedAt"         // timestamp
    }

    object EpisodeField {
        const val PODCAST_ID = "podcastId"         // podcast ရဲ့ document ID
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val AUDIO_URL = "audioUrl"
        const val DURATION = "duration"            // number (integer) — **စက္ကန့်**
        const val PUBLISHED_AT = "publishedAt"     // timestamp
        const val COVER_URL = "coverUrl"           // "" = podcast ပုံကို သုံး
        const val FILE_SIZE_BYTES = "fileSizeBytes" // number (integer) — 0 = မသိ
    }
}

/**
 * Admin ပြင်လို့ရတဲ့ podcast field တွေ။ `episodeCount` နဲ့ `updatedAt` ကို repository က
 * သီးသန့် ထိန်းတယ် (episode ထည့်/ဖျက်တိုင်း အလိုအလျောက် ပြောင်း)။
 * `id` ကို field အဖြစ် မသိမ်းဘူး — document ID ပဲ (FirestoreExt ကနေ ပြန်ထည့်)။
 */
fun Podcast.toEditableFields(): Map<String, Any> = mapOf(
    FirestoreSchema.PodcastField.TITLE to title.trim(),
    FirestoreSchema.PodcastField.DESCRIPTION to description.trim(),
    FirestoreSchema.PodcastField.COVER_URL to coverUrl.trim(),
    FirestoreSchema.PodcastField.CATEGORY to category.trim()
)

/**
 * Episode → Firestore field (type အတိအကျ — duration/fileSizeBytes = integer, publishedAt = timestamp)။
 * `formattedDuration` (getter) နဲ့ `id` ကို မသိမ်းအောင် `toObject` serializer မသုံးဘဲ ကိုယ်တိုင် ရေးတယ်။
 */
fun Episode.toFirestoreFields(): Map<String, Any> = mapOf(
    FirestoreSchema.EpisodeField.PODCAST_ID to podcastId,
    FirestoreSchema.EpisodeField.TITLE to title.trim(),
    FirestoreSchema.EpisodeField.DESCRIPTION to description.trim(),
    FirestoreSchema.EpisodeField.AUDIO_URL to audioUrl.trim(),
    FirestoreSchema.EpisodeField.DURATION to duration,
    FirestoreSchema.EpisodeField.PUBLISHED_AT to (publishedAt ?: Timestamp.now()),
    FirestoreSchema.EpisodeField.COVER_URL to coverUrl.trim(),
    FirestoreSchema.EpisodeField.FILE_SIZE_BYTES to fileSizeBytes
)
