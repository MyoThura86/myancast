package com.example.myancast.ui.admin

import com.example.myancast.data.admin.AdminException
import com.example.myancast.data.admin.MediaKind
import com.example.myancast.data.admin.MediaUploader
import com.example.myancast.data.admin.UploadEvent
import com.example.myancast.data.admin.mediaStoragePath

/** Admin VM ၂ ခုလုံး ပြတဲ့ — အင်တာနက် မရှိရင် upload မစခင် ပြောပြ */
const val OFFLINE_SAVE_MESSAGE = "အင်တာနက် မရှိပါ — ချိတ်ဆက်ပြီးမှ သိမ်းပါ"
const val FIX_ERRORS_MESSAGE = "အနီရောင် ပြထားတဲ့ အချက်တွေကို ပြင်ပြီးမှ သိမ်းပါ"

/** Repository/Uploader ပစ်တဲ့ [AdminException] ဆို သူ့ message (မြန်မာ) · တခြားဆို ယေဘုယျ */
fun Throwable.adminMessage(fallback: String = "သိမ်းလို့ မရပါ — ပြန်ကြိုးစားကြည့်ပါ"): String =
    (this as? AdminException)?.message ?: fallback

/**
 * Form ထဲက ပုံ/အသံ → Firestore ထဲ သိမ်းမယ့် URL။
 * ဖုန်းထဲက ဖိုင်ဆို Storage ကို တင်ပြီး download URL ပြန်ပေး (progress ကို [onProgress] နဲ့)၊
 * လင့်ခ်ဆို ဒီအတိုင်း၊ မထည့်ထားရင် ""။
 *
 * @return URL နဲ့ "ဒီ save မှာ အသစ် တင်လိုက်လား" — Firestore မှာ မသိမ်းနိုင်ရင် ပြန်ဖျက်ဖို့
 */
suspend fun MediaUploader.resolveToUrl(
    field: MediaField,
    kind: MediaKind,
    folder: String,
    ownerId: String,
    nowMillis: Long,
    progressLabel: String,
    onProgress: (SaveStatus) -> Unit
): ResolvedMedia = when (field.mode) {
    SourceMode.NONE -> ResolvedMedia("", uploaded = false)
    SourceMode.URL -> ResolvedMedia(field.urlText.trim(), uploaded = false)
    SourceMode.DEVICE -> {
        val localUri = field.localUri ?: throw AdminException("ဖိုင် မရွေးရသေးပါ")
        onProgress(SaveStatus.Uploading(progressLabel, 0f))
        var url: String? = null
        upload(localUri, kind, mediaStoragePath(folder, ownerId, kind, nowMillis)).collect { event ->
            when (event) {
                is UploadEvent.Progress -> onProgress(SaveStatus.Uploading(progressLabel, event.fraction))
                is UploadEvent.Done -> url = event.url
            }
        }
        ResolvedMedia(url ?: throw AdminException("ဖိုင် တင်လို့ မရပါ — ပြန်ကြိုးစားကြည့်ပါ"), uploaded = true)
    }
}

data class ResolvedMedia(val url: String, val uploaded: Boolean)
