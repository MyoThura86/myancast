package com.example.myancast.domain.model

/**
 * ★ Phase 6 (Admin) — ပုံ / MP3 ဘယ်ကလာလဲ။
 * ViewModel က `android.net.Uri` ကို မသိရအောင် URI ကို String နဲ့ပဲ ကိုင်တယ်။
 */
sealed interface MediaInput {
    /** မထည့်ထား (အပိုင်း ပုံ — Podcast ပုံကို သုံး) */
    data object None : MediaInput

    /** အင်တာနက် လင့်ခ် (https://…) — တင်စရာ မလို၊ Firestore ထဲ တိုက်ရိုက် သိမ်း */
    data class Remote(val url: String) : MediaInput

    /** ဖုန်းထဲက ဖိုင် (`content://…`) — Firebase Storage ကို အရင် တင်ပြီးမှ လင့်ခ် သိမ်း */
    data class Local(val uri: String) : MediaInput
}

/** MP3 ကို စစ်ပြီး ရလာတဲ့ အချက်အလက် — ကြာချိန်နဲ့ အရွယ်ကို form ထဲ အလိုအလျောက် ဖြည့်ဖို့ */
data class AudioInfo(
    val durationSeconds: Int,
    val sizeBytes: Long,          // 0 = မသိ (server က Content-Length မပေး)
    val isMp3: Boolean,
    val displayName: String = ""  // ဖုန်းထဲက ဖိုင် နာမည် ("episode-01.mp3")
)
