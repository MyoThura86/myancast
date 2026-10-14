package com.example.myancast.domain.util

import com.example.myancast.domain.model.Episode

/**
 * ★ Phase 6 (#5) — `fileSizeBytes` မရှိတဲ့ (Console က ထည့်ထားတဲ့) episode အတွက် ခန့်မှန်းဖို့ bitrate။
 * Speech podcast MP3 အများစုက 64 kbps (phase6 Day 0 ဆုံးဖြတ်ချက်) — Admin က တင်တဲ့ ဖိုင်ဆို
 * `fileSizeBytes` အတိအကျ ပါလို့ ဒီခန့်မှန်းချက် မလိုဘူး။
 */
const val ASSUMED_BITRATE_KBPS = 64

private const val BYTES_PER_MB = 1_000_000L

/** Firestore မှာ `fileSizeBytes` ရှိရင် အတိအကျ၊ မရှိရင် duration × bitrate နဲ့ ခန့်မှန်း · ၂ ခုလုံး မသိရင် 0 */
fun Episode.estimatedSizeBytes(): Long =
    if (fileSizeBytes > 0) fileSizeBytes
    else duration.coerceAtLeast(0).toLong() * ASSUMED_BITRATE_KBPS * 1000 / 8

/**
 * ★ Phase 6 (#5) — Data ချွေတာ mode ဖွင့်ထားပြီး mobile data သုံးနေချိန် ဒေါင်းမထားတဲ့ အပိုင်း ဖွင့်ရင် အရင် မေး
 */
fun shouldConfirmMobileStream(dataSaver: Boolean, metered: Boolean, downloaded: Boolean): Boolean =
    dataSaver && metered && !downloaded

/**
 * 11_500_000 → "~၁၁ MB" · 1 MB အောက် → "~၁ MB အောက်" · 0 (မသိ) → "" (UI မှာ ဖျောက်)
 * "~" ပါတာ — ခန့်မှန်းချက် ဖြစ်နိုင်လို့ (အတိအကျ ကတိ မပေးဘူး)။
 */
fun formatMegabytes(bytes: Long): String {
    if (bytes <= 0) return ""
    if (bytes < BYTES_PER_MB) return "~၁ MB အောက်"
    val mb = (bytes + BYTES_PER_MB / 2) / BYTES_PER_MB   // အနီးဆုံး MB
    return "~${mb.toMyanmarDigits()} MB"
}
