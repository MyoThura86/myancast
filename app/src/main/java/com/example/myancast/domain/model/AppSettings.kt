package com.example.myancast.domain.model

/**
 * ★ Phase 6 (#1) — App ပိတ်ပြီး ပြန်ဖွင့်လည်း မပျောက်ရမယ့် setting များ။
 * `AppConfig` (ui/theme) က UI ဘက်မှာ ဆက်ရှိမယ် — `MainActivity` က ဒီကနေ ပြောင်းပေး
 * (data layer က `ui.theme` ကို import မလုပ်ရလို့ model ကို domain မှာ ခွဲထားတာ)။
 */
data class AppSettings(
    val darkMode: Boolean = true,
    /** Mobile data သုံးနေရင် ပုံအသစ် မဒေါင်း · ဒေါင်းမထားတဲ့ အပိုင်း ဖွင့်ခါနီး မေး (#5) */
    val dataSaver: Boolean = false,
    /** ဒေါင်းလုဒ်ကို Wi-Fi ရမှ လုပ် (#4) — Day 0 ဆုံးဖြတ်ချက် — default ON */
    val wifiOnlyDownload: Boolean = true
)
