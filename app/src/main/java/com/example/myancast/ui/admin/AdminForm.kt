package com.example.myancast.ui.admin

import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.model.MediaInput
import com.example.myancast.domain.model.Podcast
import com.example.myancast.domain.util.formatDurationLabel
import com.example.myancast.domain.util.formatMegabytes
import com.example.myancast.domain.util.isValidWebUrl
import com.example.myancast.domain.util.parseDurationInput
import com.example.myancast.domain.util.toMyanmarDigits
import com.example.myancast.ui.home.ALL_CATEGORY
import com.example.myancast.ui.home.buildCategories

// ★ Phase 6 (Admin) — form state + စစ်ဆေးမှု pure function များ (composable ထဲ logic မထားရ)

const val TITLE_MAX_LENGTH = 120

/** ပုံ / အသံ ဘယ်က ယူမလဲ — NONE က အပိုင်း ပုံအတွက်ပဲ ("Podcast ပုံကို သုံးမည်") */
enum class SourceMode { DEVICE, URL, NONE }

data class MediaField(
    val mode: SourceMode,
    val urlText: String = "",
    val localUri: String? = null,
    val localName: String = ""
) {
    fun toInput(): MediaInput = when (mode) {
        SourceMode.DEVICE -> localUri?.let { MediaInput.Local(it) } ?: MediaInput.None
        SourceMode.URL -> urlText.trim().takeIf { it.isNotEmpty() }
            ?.let { MediaInput.Remote(it) } ?: MediaInput.None
        SourceMode.NONE -> MediaInput.None
    }

    companion object {
        /** Firestore ထဲက URL နဲ့ form ပြန်ဆောက် — URL မရှိရင် [emptyMode] */
        fun fromSavedUrl(url: String, emptyMode: SourceMode): MediaField =
            if (url.isBlank()) MediaField(emptyMode) else MediaField(SourceMode.URL, urlText = url)
    }
}

/** သိမ်း/ဖျက် နေတုန်း UI ကို ပိတ်ပြီး progress ပြဖို့ */
sealed interface SaveStatus {
    data object Idle : SaveStatus
    /** @param label "ပုံ တင်နေပါတယ်" · @param fraction 0f..1f */
    data class Uploading(val label: String, val fraction: Float) : SaveStatus
    data object Saving : SaveStatus
    data object Deleting : SaveStatus
}

// ─────────────────────────────────────────────
// Podcast
// ─────────────────────────────────────────────

data class PodcastForm(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val cover: MediaField = MediaField(SourceMode.DEVICE)
)

data class PodcastFormErrors(
    val title: String? = null,
    val category: String? = null,
    val cover: String? = null
) {
    val hasAny: Boolean get() = title != null || category != null || cover != null
}

fun validatePodcastForm(form: PodcastForm, coverLoadFailed: Boolean): PodcastFormErrors =
    PodcastFormErrors(
        title = validateTitle(form.title),
        category = when {
            form.category.isBlank() -> "အမျိုးအစား ထည့်ပါ"
            // Home ရဲ့ "အားလုံး" filter နဲ့ ရောသွားမယ်
            form.category.trim() == ALL_CATEGORY -> "“$ALL_CATEGORY” ကို အမျိုးအစား အဖြစ် သုံးလို့ မရပါ"
            else -> null
        },
        cover = validateImage(form.cover, coverLoadFailed, required = true)
    )

// ─────────────────────────────────────────────
// Episode
// ─────────────────────────────────────────────

data class EpisodeForm(
    val title: String = "",
    val description: String = "",
    val durationText: String = "",
    val publishedAtMillis: Long = 0L,
    val audio: MediaField = MediaField(SourceMode.DEVICE),
    val cover: MediaField = MediaField(SourceMode.NONE)
)

data class EpisodeFormErrors(
    val title: String? = null,
    val audio: String? = null,
    val duration: String? = null,
    val cover: String? = null
) {
    val hasAny: Boolean get() = title != null || audio != null || duration != null || cover != null
}

/** MP3 စစ်ဆေးမှု အခြေအနေ — form ထဲက လက်ရှိ အသံ input အတွက်ပဲ မှန်တယ် (input ပြောင်းရင် Idle ပြန်ဖြစ်) */
sealed interface AudioCheck {
    data object Idle : AudioCheck
    data object Checking : AudioCheck
    data class Ok(val info: AudioInfo) : AudioCheck
    data class Failed(val message: String) : AudioCheck
}

/**
 * @param audioVerified လက်ရှိ အသံ input ကို စစ်ပြီး MP3 မှန်ကြောင်း သိပြီလား
 *   (ViewModel က save မလုပ်ခင် မစစ်ရသေးရင် အလိုအလျောက် စစ်ပေးတယ်)
 */
fun validateEpisodeForm(
    form: EpisodeForm,
    audioVerified: Boolean,
    coverLoadFailed: Boolean
): EpisodeFormErrors = EpisodeFormErrors(
    title = validateTitle(form.title),
    audio = validateAudio(form.audio, audioVerified),
    duration = if (parseDurationInput(form.durationText) == null) DURATION_FORMAT_ERROR else null,
    cover = validateImage(form.cover, coverLoadFailed, required = false)
)

const val DURATION_FORMAT_ERROR = "ကြာချိန်ကို 24:30 (သို့) 1:02:05 ပုံစံနဲ့ ထည့်ပါ"
const val NOT_MP3_ERROR = "MP3 ဖိုင် မဟုတ်ပါ — .mp3 ဖိုင်ကိုသာ သုံးပါ"
const val AUDIO_UNREADABLE_ERROR = "အသံဖိုင်ကို ဖွင့်လို့ မရပါ — ဖိုင် (သို့) လင့်ခ်ကို စစ်ကြည့်ပါ"

const val COVER_LOAD_FAILED_HINT = "ဒီလင့်ခ်က ပုံကို ဖွင့်လို့ မရပါ — လင့်ခ်ကို စစ်ကြည့်ပါ"

private const val URL_FORMAT_ERROR = "လင့်ခ် မှန်မှန် ထည့်ပါ (https:// နဲ့ စရပါမယ်)"

private fun validateTitle(title: String): String? = when {
    title.isBlank() -> "ခေါင်းစဉ် ထည့်ပါ"
    title.trim().length > TITLE_MAX_LENGTH ->
        "ခေါင်းစဉ်ကို စာလုံး ${TITLE_MAX_LENGTH.toMyanmarDigits()} လုံးအောက် ထားပါ"
    else -> null
}

private fun validateImage(field: MediaField, loadFailed: Boolean, required: Boolean): String? =
    when (field.mode) {
        SourceMode.NONE -> if (required) "မျက်နှာဖုံး ပုံ ထည့်ပါ" else null
        SourceMode.DEVICE -> if (field.localUri == null) "ဖုန်းထဲက ပုံ တစ်ပုံ ရွေးပါ" else null
        SourceMode.URL -> when {
            field.urlText.isBlank() -> "ပုံ လင့်ခ် ထည့်ပါ"
            !isValidWebUrl(field.urlText) -> URL_FORMAT_ERROR
            loadFailed -> COVER_LOAD_FAILED_HINT
            else -> null
        }
    }

private fun validateAudio(field: MediaField, verified: Boolean): String? = when (field.mode) {
    SourceMode.DEVICE -> when {
        field.localUri == null -> "ဖုန်းထဲက MP3 ဖိုင် ရွေးပါ"
        !verified -> "MP3 ဖိုင်ကို စစ်ဆေးလို့ မပြီးသေးပါ"
        else -> null
    }
    SourceMode.URL -> when {
        field.urlText.isBlank() -> "MP3 လင့်ခ် ထည့်ပါ"
        !isValidWebUrl(field.urlText) -> URL_FORMAT_ERROR
        !verified -> "MP3 လင့်ခ်ကို စစ်ဆေးလို့ မပြီးသေးပါ"
        else -> null
    }
    SourceMode.NONE -> "အသံဖိုင် ထည့်ပါ"
}

// ─────────────────────────────────────────────
// Display helpers
// ─────────────────────────────────────────────

/** "MP3 · ၂၄ မိနစ် · ~၁၁ MB" — မသိတဲ့ အပိုင်း ဖျောက် */
fun audioSummary(info: AudioInfo): String =
    listOf("MP3", formatDurationLabel(info.durationSeconds), formatMegabytes(info.sizeBytes))
        .filter { it.isNotBlank() }
        .joinToString(" · ")

/** Dashboard ထိပ်က — "Podcast ၅ ခု · အပိုင်း ၂၄ ခု" */
fun adminSummary(podcasts: List<Podcast>): String {
    val episodes = podcasts.sumOf { it.episodeCount.coerceAtLeast(0) }
    return "Podcast ${podcasts.size.toMyanmarDigits()} ခု · အပိုင်း ${episodes.toMyanmarDigits()} ခု"
}

/** နောက်ဆုံး ပြင်ထားတာ အပေါ်ဆုံး (Console က ထည့်ထားလို့ updatedAt မရှိတာ အောက်ဆုံး) */
fun sortPodcastsForAdmin(podcasts: List<Podcast>): List<Podcast> =
    podcasts.sortedWith(
        compareByDescending<Podcast> { it.updatedAt?.toDate()?.time ?: Long.MIN_VALUE }
            .thenBy { it.title }
    )

/** ရှိပြီးသား အမျိုးအစားများ — chip နှိပ်ပြီး ရွေးလို့ရအောင် ("အားလုံး" မပါ) */
fun categorySuggestions(podcasts: List<Podcast>): List<String> =
    buildCategories(podcasts).filter { it != ALL_CATEGORY }
