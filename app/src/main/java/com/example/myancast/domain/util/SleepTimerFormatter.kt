package com.example.myancast.domain.util

/** ★ Phase 6 (#3) — Full Player ရဲ့ menu မှာ ပြမယ့် မိနစ် ရွေးချယ်မှုများ */
val SLEEP_TIMER_MINUTES = listOf(15, 30, 60)

/**
 * Sleep timer ရဲ့ chip စာသား။
 * "၁၂ မိနစ် ကျန်" (မိနစ်ကို အပေါ် round — ၁ မိနစ် အောက်ဆိုလည်း "၁ မိနစ် ကျန်") ·
 * "ဒီပိုင်း ဆုံးရင် ရပ်မယ်" · timer မရှိရင် ""
 */
fun sleepTimerLabel(endsAtMs: Long?, atEpisodeEnd: Boolean, nowMs: Long): String {
    if (atEpisodeEnd) return "ဒီပိုင်း ဆုံးရင် ရပ်မယ်"
    if (endsAtMs == null) return ""
    val remainingMs = (endsAtMs - nowMs).coerceAtLeast(0L)
    val minutes = ((remainingMs + 59_999L) / 60_000L).coerceAtLeast(1L)
    return "${minutes.toMyanmarDigits()} မိနစ် ကျန်"
}

/** Menu ထဲက ရွေးချယ်မှု — 15 → "၁၅ မိနစ်" · 60 → "၁ နာရီ" */
fun sleepTimerOptionLabel(minutes: Int): String = formatDurationLabel(minutes * 60)
