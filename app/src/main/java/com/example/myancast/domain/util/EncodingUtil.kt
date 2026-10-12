package com.example.myancast.domain.util


// ဆုံးဖြတ်ချက် (Phase 2 → Phase 5 Day 0 မှာ အတည်ပြု — KEEP) — Zawgyi/Unicode
// ၁။ Firestore data အားလုံး Unicode ဖြစ်တယ်။
//
// ၂။ Settings မှာ Zawgyi toggle က font family ကိုပဲ ပြောင်းတယ် —
//     စာသား ကိုယ်တိုင်ကို မပြောင်းဘူး။
//
// ၃။ Unicode စာသားကို Zawgyi font နဲ့ ပြရင် စာလုံး ပုံပျက်တယ်
//     (font နဲ့ encoding က တွဲဖက် မကိုက်လို့)။
//
// ၄။ Font ပြောင်းရုံနဲ့ မလုံလောက်ဘူး — စာသားကိုပါ Rabbit converter နဲ့
//     runtime ပြောင်းရမယ် (~၁ ရက် အလုပ်)။
//
// ၅။ Course project အတွက် grading criteria မဟုတ်လို့ မလုပ်တော့ဘူး။
//
// ဒါကြောင့် (နောက်ဆုံး ဆုံးဖြတ်ချက်) —
//   • App တစ်ခုလုံး Unicode ပဲ သုံးတယ်။
//   • Settings ရဲ့ Zawgyi toggle ကို ပိတ်ထားတယ် (`enabled = false`,
//     subtitle = "Unicode စံနှုန်းကိုသာ အသုံးပြုသည်")။
//   • README ရဲ့ "Known limitation" မှာ ရေးထားတယ်။
//   • နောက်ထပ် version မှာ ထည့်မယ်ဆိုရင် — Rabbit converter ကို ဒီဖိုင်မှာ
//     `unicodeToZawgyi()` / `zawgyiToUnicode()` အဖြစ် ထည့်ပြီး Text တွေမှာ ခေါ်ရမယ်။
//
// ─────────────────────────────────────────────────────────────
