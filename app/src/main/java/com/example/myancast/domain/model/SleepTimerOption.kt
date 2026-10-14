package com.example.myancast.domain.model

/** ★ Phase 6 (#3) — Sleep timer ရွေးချယ်မှု (Full Player ရဲ့ 🌙 menu) */
sealed interface SleepTimerOption {
    data object Off : SleepTimerOption
    data class Minutes(val minutes: Int) : SleepTimerOption
    /** လက်ရှိ ပိုင်း ဆုံးရင် ရပ် */
    data object EndOfEpisode : SleepTimerOption
}
