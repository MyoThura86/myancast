# ✅ Phase 5 — Checklist (Polish + Testing + Presentation)

> `phase5.md`၊ `fullscope.md` (Quality Checklist)၊ `phase2/3/4-checklist.md` ရဲ့ ကျန်တာတွေနဲ့ ချိန်ထိုး စစ်ပြီး။
> Code ကို 2026-10-08 မှာ စစ်ထားတယ် — branch `fix/phase5-complete` (`main` @ `692eb3e` အပေါ်)။

## အနှစ်ချုပ်

| အပိုင်း | အခြေအနေ |
|---|---|
| `clean assembleDebug` | ✅ Pass · **Kotlin/KSP warning ၀ ခု** |
| Unit test (`testDebugUnitTest`) | ✅ **128 / 128 pass** (Phase 4 မှာ 109 → **+19**) |
| `assembleRelease` | ✅ `app-release-unsigned.apk` — **16.9 MB** (minify ပိတ်) |
| MVP feature ၁၅ ခု | ✅ ၁၄ ခု + Zawgyi (#13) ⚪ KEEP ဆုံးဖြတ်ချက် |
| Code ဘက် Phase 5 အလုပ် | ✅ ပြီး (section 1–4) |
| **လူလိုတဲ့ အလုပ်** | ⬜ ကျန် (section 6) — device test · review · slide · demo |

---

## 1. 🐛 Bug ပြင်ပြီး (code review မှာ တွေ့)

| # | Bug | အဆင့် | ပြင်ပုံ |
|---|---|---|---|
| 1 | `coverURL` → `coverUrl` နာမည် ပြောင်းပြီး Room `version` မတိုး — schema hash ပြောင်းလို့ Phase 4 build ရှိတဲ့ ဖုန်းမှာ **"cannot verify the data integrity"** crash (`HistoryRecorder` မှာ catch မရှိ) | 🔴 | `AppDatabase` **version 3** (destructive fallback — ဖုန်းမှာ schema ဘယ်ဟာ ရှိနေနေ ရ) |
| 2 | **Light mode ဖတ်မရ** — UI ၁၆ ဖိုင်၊ ၃၈ နေရာမှာ `TextHi` (အဖြူနီး) ကို အလင်း background ပေါ် ရေး | 🔴 | `MaterialTheme.colorScheme.*` (`onSurface` · `onSurfaceVariant` · `outline` · `surface` · `surfaceVariant`) ပြောင်း |
| 3 | History limit ကို မှားတဲ့ query (`lastPlayed()` `LIMIT 1` → `20`) မှာ ထည့်မိ | 🟡 | `LIMIT 1` ပြန်ထား (Day 0 #8 — KEEP) |
| 4 | `android.icu` import အစား `import java.*` ထည့်မိ | 🟡 | ၂ ကြောင်းလုံး ဖျက် |
| 5 | Library ရဲ့ "ပြန်ကြိုးစားမည်" ခလုတ် ဘာမှ မလုပ် (`onRetry = { }`) — `catch` ပြီးရင် flow ဆုံးလို့ error ကနေ မထွက်နိုင် | 🟡 | `reload` trigger + `flatMapLatest` → `retry()` · test ၃ ခု |
| 6 | Settings မှာ "Phase 5 တွင် ထည့်သွင်းမည်" — KEEP ဆုံးဖြတ်ချက်နဲ့ ဆန့်ကျင် | 🟡 | "Unicode စံနှုန်းကိုသာ အသုံးပြုသည်" · disabled row ကို နှိပ်လို့ မရတော့ |
| 7 | Detail ကနေ episode ၂ ခါ မြန်မြန် နှိပ်ရင် Player ၂ ခု ထပ် | ⚪ | `launchSingleTop = true` (Phase 3 device test #15) |
| 8 | Library မှတ်တမ်း row နှိပ်လို့ မရ | ⚪ | Podcast detail ကို ဖွင့် (သတင်း audio က podcast မရှိလို့ နှိပ်လို့ မရ) |
| 9 | Home ရဲ့ 🔔 ခလုတ် ဘာမှ မလုပ် (FCM က scope အပြင်) | ⚪ | ဖယ် · Settings icon `contentDescription` မြန်မာ |
| 10 | Detail မှာ `"ပိုင်းများ (12)"` — ASCII ဂဏန်း | ⚪ | `toMyanmarDigits()` |

---

## 2. 🅰️ A — UX Polish

- [x] Finding ၆ ချက် (`phase4-checklist.md` 6.1) — PR #25 + ဒီ branch မှာ အပြီးသတ် (bug 1, 3, 4 ကြည့်)
- [x] **App icon အသစ်** — မြန်မာ အက္ခရာ **"မ"** ကို ရွှေရောင် stroke တစ်ချက်နဲ့ + ခွက်ထဲမှာ **▶**
  - Adaptive (vector) · Android 13 themed (`ic_launcher_monochrome`) · API 24–25 အတွက် PNG ၁၀ ခု · `docs/app-icon-512.png`
- [x] App နာမည် — launcher မှာ `"MyanCast"` (တိုမှ မဖြတ်) · Settings မှာ `"MyanCast Personal · ဗားရှင်း ၁.၀"`
- [x] Dark ↔ Light — screen တိုင်း theme color သုံး (bug 2)
- [x] Empty / Error audit — Library retry (bug 5)
- [x] `SectionHeader` `"See all"` → `"အားလုံး ကြည့်ရန်"` (`bodyMedium`) — Poppins carry-over list ပြီးပြီ

## 3. 🅱️ B — Release

- [x] `app/proguard-rules.pro` ဖန်တီး — minify ဖွင့်ရင် လိုမယ့် keep rule (model · Room entity · `PlaybackService`)
- [x] `isMinifyEnabled = false` ⚪ KEEP (Day 0 #5)
- [x] `assembleRelease` ✅ — 16.9 MB unsigned APK

## 4. 🅲 C — Quality + Docs

- [x] Dead code ဖျက် — `MainActivity` ရဲ့ `EPISODE_TEST` block · `FirebaseSource.kt` · `SettingsViewModel.kt` · `PlaceholderScreen.kt` (screen အားလုံး တကယ့် screen ဖြစ်ပြီ)
- [x] `README.md` — feature · architecture · Firestore schema + index · build · known limitation · အဖွဲ့
- [x] Zawgyi ဆုံးဖြတ်ချက် — `EncodingUtil.kt` + Settings + README မှာ တစ်ညီတည်း
- [x] Test အသစ် **၁၉ ခု**
  - `PlaybackStateTest` ၉ (Phase 3 contract ရဲ့ computed property)
  - `PlaybackProgressTest` ၄ (၉၅% `isFinished`)
  - `MyanmarDigitsTest` ၃
  - `LibraryViewModelTest` +၃ (error · retry · retry ပြီး tab မပျောက်)
- [ ] `SettingsScreen` / `EpisodeRow` composable test — **DROP** (Compose UI test ကို JVM မှာ run ဖို့ dependency အသစ် လို — Phase 5 မှာ မထည့်)
- [x] Repo — `.gitignore` မှာ `.kotlin/` + `.idea/deploymentTargetSelector.xml` · tracked ဖြစ်နေတာ ဖြုတ်

---

## 5. 🎯 `fullscope.md` Quality Checklist

| # | စစ်ရမယ့် အချက် | အခြေအနေ |
|---|---|---|
| 1 | App crash မဖြစ်ဘဲ run နိုင် | ⬜ device မှာ စစ်ရန် (Room crash ပြင်ပြီး) |
| 2 | Bottom nav ၅ ခု အလုပ်လုပ် | ✅ code · ⬜ device |
| 3 | Podcast list Firestore ကနေ ပေါ် | ⬜ device |
| 4 | Episode ဖွင့်နားထောင်နိုင် | ⬜ device |
| 5 | Mini player screen အားလုံးမှာ ပေါ် | ⬜ device |
| 6 | Zawgyi/Unicode — ဆုံးဖြတ်ချက် ရှင်းပြနိုင် | ✅ README + `EncodingUtil.kt` |
| 7 | News list နဲ့ detail ပေါ် | ⬜ device |
| 8 | Subscribe/unsubscribe အလုပ်လုပ် | ⬜ device |
| 9 | Search အလုပ်လုပ် | ✅ (C — Phase 4 device test) |
| 10 | Dark/Light theme toggle | ✅ code ပြင်ပြီး · ⬜ device မှာ screen တိုင်း ကြည့် |
| 11 | Unit test ၅ ခုအထက် | ✅ **၁၂၈** |
| 12 | README ရေးပြီး | ✅ (screenshot ၅ ခု ထည့်ရန် ကျန်) |

---

## 6. ⬜ ကျန်ရှိနေသေးတာ (code မဟုတ် — လူလိုတာ)

| # | အလုပ် | ဘယ်သူ | ☐ |
|---|---|---|---|
| 1 | ⚠️ **Phase 4 build ရှိတဲ့ ဖုန်းကို uninstall မလုပ်ဘဲ update** → app ဖွင့်ပြီး episode ဖွင့် → crash မဖြစ်ရ (bug 1 ပြင်ကြောင်း အတည်ပြု) | B | ☐ |
| 2 | Device test **၃၂ ခု** — Phase 3 (၁၁) + Phase 4 (၁၅) + Phase 5 Day 2 အသစ် (၆) | All | ☐ |
| 3 | Light mode ကို screen ၉ ခုလုံးမှာ မျက်စိနဲ့ စစ် (Home · Detail · Player · Mini · News · NewsDetail · Search · Library · Settings) | A | ☐ |
| 4 | Launcher မှာ icon အသစ် ကြည့် — round / square / themed (Android 13+) | A | ☐ |
| 5 | Firestore `duration` data ပြင် (demo podcast ရဲ့ episode ၄ ခု အနည်းဆုံး) | C | ☐ |
| 6 | Cross-review ၄ ခု — Phase 3: A→C, B→A · Phase 4: A→B, B→C | All | ☐ |
| 7 | စွမ်းဆောင်ရည် — cold start · scroll jank · listener leak · APK size (16.9 MB ✅ မှတ်ပြီး) | B | ☐ |
| 8 | Screenshot ၅ ခု → `docs/screenshots/` + README | C | ☐ |
| 9 | Slide ၁၀–၁၂ + demo script (`phase5.md` Day 4) | A | ☐ |
| 10 | ပြန်လေ့ကျင့် ၂ ခါ (တစ်ခါက အင်တာနက် ပိတ်ပြီး) | All | ☐ |
| 11 | ဒီ branch ကို PR → `main` · ၃ ယောက်လုံးကို ပြောင်းလဲမှု ပြော (A ရဲ့ ဖိုင် များတယ်) | — | ☐ |
| 12 | Submit (APK / repo link + slide + README) | All | ☐ |
