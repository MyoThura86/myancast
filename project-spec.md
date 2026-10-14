# MyanCast Personal — System Scope & Specification

> **မြန်မာ့ အခြေအနေအတွက် တည်ဆောက်ထားတဲ့ Podcast + သတင်း App**
>
> ဒီစာတမ်းက project တစ်ခုလုံးရဲ့ **scope · specification · feature စာရင်း · ရည်ရွယ်ချက်** ကို
> တစ်နေရာတည်းမှာ စုထားတာပါ။ Project show အတွက် တင်ပြချက် (section 1) ပါဝင်ပါတယ်။
>
> **ရင်းမြစ်** — `fullscope.md` (v1.0 spec) · `phase1–5.md` + checklist များ (ပြီးခဲ့တာ) ·
> `phase6-showcase.md` (v2.0 extension plan) · 2026-10-02 က `main` ပေါ်က code။

| အချက် | အသေးစိတ် |
|---|---|
| **စာတမ်း version** | 1.0 — 2026-10-02 |
| **App version** | v1.0 (course submission) · v2.0 (project show — စီစဉ်ဆဲ) |
| **အဖွဲ့** | ၃ ယောက် (A · B · C) |
| **Platform** | Android 7.0+ (API 24 – 35) |

### Status သင်္ကေတ (စာတမ်း တစ်ခုလုံး သုံး)

| သင်္ကေတ | အဓိပ္ပာယ် |
|---|---|
| ✅ | **v1.0 မှာ ပြီး** — code ထဲ ရှိပြီး test လုပ်ပြီး |
| 🔜 | **v2.0 မှာ လုပ်မယ်** — `phase6-showcase.md` မှာ design ပြီး၊ implementation မစရသေး |
| ⚪ | **v2.0 optional** — အချိန်ရရင် (Tier 3) |
| ⏸️ | ဆုံးဖြတ်ချက်နဲ့ ဆိုင်းငံ့/အစားထိုး |
| ☐ | Phase 5 မှာ စစ်ဆဲ/တိုင်းဆဲ — ရလဒ် ရရင် ဖြည့် |
| ❌ | Scope အပြင် — တမင် မလုပ် |

---

## မာတိကာ

1. [Project Summary (Project Show)](#1-project-summary-project-show)
2. [ရည်ရွယ်ချက်၊ ရည်မှန်းချက်နဲ့ Design မူ](#2-ရည်ရွယ်ချက်-ရည်မှန်းချက်နဲ့-design-မူ)
3. [အသုံးပြုသူများ (Personas)](#3-အသုံးပြုသူများ-personas)
4. [System Scope](#4-system-scope)
5. [Feature စာရင်း အပြည့်အစုံ](#5-feature-စာရင်း-အပြည့်အစုံ)
6. [Screen အလိုက် Functional Specification](#6-screen-အလိုက်-functional-specification)
7. [Player Specification](#7-player-specification)
8. [Myanmar Localization Specification](#8-myanmar-localization-specification)
9. [Data Specification](#9-data-specification)
10. [System Architecture](#10-system-architecture)
11. [Non-Functional Requirements](#11-non-functional-requirements)
12. [Quality & Testing](#12-quality--testing)
13. [Scope အပြင် + Known Limitations](#13-scope-အပြင်--known-limitations)
14. [Roadmap နဲ့ Development Process](#14-roadmap-နဲ့-development-process)
15. [အဖွဲ့ နဲ့ တာဝန်](#15-အဖွဲ့-နဲ့-တာဝန်)
16. [ဝေါဟာရ (Glossary)](#16-ဝေါဟာရ-glossary)
17. [Credits & Licenses](#17-credits--licenses)

---

## 1. Project Summary (Project Show)

### 1.1 Abstract (English)

> **MyanCast Personal** is a Myanmar-language podcast and audio-news app for Android, designed
> around the everyday realities of Myanmar listeners: expensive and unstable mobile data,
> frequent power cuts, low-end phones, a mixed Zawgyi/Unicode text ecosystem, and older
> listeners who mostly listen to dhamma talks.
>
> Version 1.0 delivers a complete listening experience. You can browse by category, play
> episodes in the background with lock-screen controls, use a mini player and full player
> with seek and 0.5–2× speed, read news with optional audio, search, subscribe, keep
> listening history, and resume where you stopped. All Burmese text renders correctly
> with a bundled Padauk font, and counts, dates and durations are shown in Burmese.
>
> Version 2.0, planned for the project show, adds "built for Myanmar" features:
> - downloads that resume after a dropped connection, for offline listening
> - data-size labels and a data saver mode
> - an offline banner
> - a sleep timer
> - automatic Zawgyi detection with conversion for search and notifications
> - a Myanmar lunar calendar with sabbath-day (ဥပုသ်) content
> - adjustable text size and an easy mode for elderly users
>
> The app is built with Kotlin, Jetpack Compose, MVVM, Firebase Firestore, Media3 and Room
> by a 3-person team in 5 weeks. A "Day 0 contract" process (shared interfaces plus test
> fakes) let three members build features in parallel. 109 unit tests pass.

### 1.2 အနှစ်ချုပ် (မြန်မာ)

**MyanCast Personal** က မြန်မာလို podcast နဲ့ သတင်းကို **အသံနဲ့ နားထောင်** နိုင်တဲ့ Android app ပါ။
ကမ္ဘာ့ podcast app တွေက မြန်မာ အသုံးပြုသူရဲ့ အခြေအနေကို မသိဘူး:
data ဈေးကြီးတာ၊ လိုင်းပြတ်တာ၊ မီးပျက်တာ၊ Zawgyi/Unicode ရောနေတာ၊ အသက်ကြီးသူ အများစုက တရားနာတာ။
MyanCast က ဒီ အခြေအနေတွေကို **အခြေခံ design ကတည်းက** ထည့်တွက်ထားတဲ့ app ပါ။

### 1.3 ဖြေရှင်းတဲ့ ပြဿနာ

| # | မြန်မာ့ အခြေအနေ | အသုံးပြုသူ ခံစားရတာ | MyanCast ရဲ့ အဖြေ |
|---|---|---|---|
| 1 | **Mobile data ဈေးကြီး၊ လိုင်း မငြိမ်** | Stream ရင် data ဘယ်လောက် ကုန်မလဲ မသိ · လိုင်းပြတ်ရင် ရပ် | ✅ Firestore cache · 🔜 ဒေါင်းလုဒ် (ဆက်ဒေါင်း) · 🔜 MB ပြ · 🔜 data ချွေတာ · 🔜 offline banner |
| 2 | **မီးပျက်၊ ဖုန်း low-end** | ဘက်ထရီ ချွေရ · storage နည်း · ဖုန်းဟောင်း | ✅ minSdk 24 · ✅ background playback ပေါ့ပါး · 🔜 sleep timer · 🔜 ဒေါင်းလုဒ် ဖျက်/size ပြ |
| 3 | **Zawgyi / Unicode ရောနေ** | စာလုံးပျက် · Zawgyi keyboard နဲ့ ရှာမတွေ့ | ✅ Padauk font bundle (app ထဲ မှန်) · 🔜 Zawgyi auto-detect + converter |
| 4 | **အသက်ကြီးသူ / တရားနာ** | စာလုံး သေး · ညအိပ်ခါနီး နားထောင် · ဥပုသ်နေ့ | ✅ မြန်မာစာ UI အပြည့် · 🔜 စာလုံးအရွယ် + လွယ်ကူ mode · 🔜 sleep timer · 🔜 မြန်မာ ပြက္ခဒိန် |

### 1.4 ထူးခြားချက် (Highlights)

| | ထူးခြားချက် | ဘာကြောင့် အရေးကြီးလဲ |
|---|---|---|
| ⭐ | **မြန်မာစာ အပြည့်** — UI · ဂဏန်း (၀–၉) · ရက်စွဲ ("၅ ရက် အရင်က") · ကြာချိန် ("၁ နာရီ ၂ မိနစ်") | `Locale("my")` က ဖုန်းအချို့မှာ English ပြန်ကျလို့ ကိုယ်တိုင် ရေးထား |
| ⭐ | **Background playback** — notification + lock screen + headphone ဖြုတ်ရင် ရပ် | Media3 `MediaSessionService` |
| ⭐ | **ဆက်နားထောင်ရန်** — ရပ်ခဲ့တဲ့ စက္ကန့်ကနေ ပြန်စ | App ပိတ်/ဖွင့်ပြီးလည်း Room ကနေ |
| ⭐ | **Offline-first** (v2.0) — လိုင်းမရှိလည်း ဒေါင်းထားတာ နားထောင်၊ cache data ကြည့် | မြန်မာ့ လိုင်း အခြေအနေ |
| ⭐ | **Zawgyi insight** (v2.0) — "font ပြောင်းတာ အဖြေ မဟုတ် · keyboard input နဲ့ system UI က ပြဿနာ" | အခြား app တွေ မဖြေရှင်းတဲ့ အချက် |
| ⭐ | **Day 0 Contract** — interface + fake ကို အရင်ချ → ၃ ယောက် ပြိုင်တူ | ၃ ပတ်ဆက် contract မကျိုး |

### 1.5 ကိန်းဂဏန်း

| | v1.0 (ပြီး) | v2.0 (ရည်မှန်း) |
|---|---|---|
| MVP feature (`fullscope.md`) | **၁၄ / ၁၅** ✅ (#13 Zawgyi toggle ⏸️ → v2.0 မှာ အစားထိုး) | ၁၅ / ၁၅ |
| Feature စုစုပေါင်း (section 5) | ၂၅ | ၄၃ (optional ပါ ၄၈) |
| Screen | ၉ (bottom nav ၅ + detail ၂ + player + search) | ၉ + Library tab ၁ |
| Unit test | **၁၀၉** pass | ၁၄၀ ကျော် |
| Build warning | ၀ | ၀ |
| အဖွဲ့ / ကြာချိန် | ၃ ယောက် / ၅ ပတ် | + ~၂ ပတ် |

### 1.6 Tech Stack

`Kotlin` · `Jetpack Compose (Material 3)` · `MVVM` · `Coroutines/Flow` · `Firebase Firestore` ·
`Media3 (ExoPlayer + MediaSession)` · `Room (KSP)` · `Coil` · `Navigation Compose` ·
Test: `JUnit` · `kotlinx-coroutines-test` · `MockK` · `Robolectric`
v2.0 ထပ်ထည့်: `WorkManager` · `myanmar-tools` · Rabbit converter

---

## 2. ရည်ရွယ်ချက်၊ ရည်မှန်းချက်နဲ့ Design မူ

### 2.1 Vision

> "မြန်မာ တစ်ယောက်ယောက် — ဘယ်ဖုန်း၊ ဘယ်လိုင်း၊ ဘယ်အသက်အရွယ် ဖြစ်ဖြစ် —
> မြန်မာလို အသံ content ကို **လွယ်လွယ်၊ သက်သာသာ၊ ယုံကြည်စိတ်ချစွာ** နားထောင်နိုင်ရမယ်။"

### 2.2 ရည်မှန်းချက် (Goals)

| # | Goal | တိုင်းတာပုံ | Version |
|---|---|---|---|
| G1 | Podcast + သတင်း ကို အသံနဲ့ နားထောင်နိုင် | MVP ၁၅ ခု | ✅ v1.0 |
| G2 | App ပိတ်ထားလည်း ဆက်နားထောင်နိုင် | Background/lock screen device test pass | ✅ v1.0 |
| G3 | မြန်မာစာ မှန်မှန် ပြ (ဖုန်း မရွေး) | Padauk bundle · Poppins မှာ မြန်မာစာ မရှိ | ✅ v1.0 |
| G4 | လိုင်းမရှိလည်း အသုံးဝင် | Airplane mode မှာ ဒေါင်းထားတာ ဖွင့်ရ | 🔜 v2.0 |
| G5 | Data ကုန်တာ ထိန်းနိုင် | MB ပြ · Wi-Fi only · data ချွေတာ | 🔜 v2.0 |
| G6 | Zawgyi သုံးသူလည်း သုံးနိုင် | Zawgyi query နဲ့ ရှာတွေ့ · notification မှန် | 🔜 v2.0 |
| G7 | အသက်ကြီးသူ သုံးလို့ လွယ် | စာလုံး ၁.၃x မှာ layout မပြို | 🔜 v2.0 |
| G8 | Code က test လုပ်လို့ရ၊ ၃ ယောက် ပြိုင်တူ ရေးလို့ရ | Test ၁၀၉+ · contract မကျိုး | ✅ v1.0 |

### 2.3 Non-Goals (တမင် မလုပ်တာ)

- **Content ထုတ်လုပ်ခြင်း** — app က နားထောင်ဖို့ပဲ။ Data ကို Firebase Console ကနေ admin က ထည့်တယ်။
- **Social platform** — comment · like · follow မရှိ (v2.0 မှာ share ရိုးရိုးပဲ စဉ်းစား)။
- **Account system** — login မလို · ကိုယ်ရေး data server ပေါ် မတင်။
- **Monetization** — ငွေပေးချေမှု · ကြော်ငြာ မရှိ။

### 2.4 Design မူ ၆ ခု

| # | မူ | လက်တွေ့ |
|---|---|---|
| P1 | **ရိုးရှင်းအောင် ထား** | DI framework (Hilt/Koin) · Retrofit · DataStore မသုံး · module ၁ ခုတည်း |
| P2 | **Firestore က read-only** | App က data မရေး · admin က Console ကနေ ထည့် · security rule ရိုးရှင်း |
| P3 | **Interface + fake** | Repository · PlayerController အားလုံး interface → test မှာ fake ထည့် |
| P4 | **Pure function ခွဲ** | Filter · format · mapping က top-level function → coroutine/emulator မလိုဘဲ test |
| P5 | **မြန်မာစာ ဦးစားပေး** | Burmese text = body/title style ပဲ · ဂဏန်း မြန်မာလို · code comment မြန်မာလို |
| P6 | **ကျရှုံးမှုကို ပြ** | Loading → Error → Empty → Content အစဉ်လိုက် · တိတ်တဆိတ် မပျက်ရ |

---

## 3. အသုံးပြုသူများ (Personas)

| Persona | အခြေအနေ | လိုအပ်ချက် | အဓိက feature |
|---|---|---|---|
| 👩 **မမြင့်** (၂၈) · ရုံးဝန်ထမ်း · ရန်ကုန် | Bus ပေါ်မှာ နားထောင် · mobile data ကန့်သတ် | Data သက်သာ · လိုင်းပြတ်လည်း ဆက်နားထောင် | Wi-Fi မှာ ကြိုဒေါင်း 🔜 · MB ပြ 🔜 · ဆက်နားထောင်ရန် ✅ |
| 👴 **ဦးဘ** (၆၇) · ပင်စင်စား · မန္တလေး | ညအိပ်ခါနီး တရားနာ · မျက်စိ မကောင်း | စာလုံးကြီး · ခလုတ်ကြီး · အိပ်ပျော်ရင် ရပ် | လွယ်ကူ mode 🔜 · sleep timer 🔜 · ဥပုသ်နေ့ တရားတော် 🔜 |
| 🧑‍🎓 **ကိုဇော်** (၂၀) · ကျောင်းသား | သင်ခန်းစာ podcast · speed မြန်မြန် | 1.5x · အရေးကြီးတဲ့ နေရာ မှတ် | Speed ✅ · seek ✅ · bookmark ⚪ |
| 📱 **မနု** (၃၅) · ဈေးရောင်း · နယ်မြို့ | Zawgyi ဖုန်းဟောင်း · Zawgyi keyboard | ရှာရင် တွေ့ရမယ် · notification ဖတ်လို့ရရမယ် | Zawgyi auto-detect 🔜 · သတင်း audio ✅ |

---

## 4. System Scope

### 4.1 Scope Diagram

```
                        ┌──────────────────────────────┐
   Admin (အဖွဲ့)  ───▶   │  Firebase Console            │
   data လက်နဲ့ ထည့်      │  Firestore: podcasts ·       │
                        │  episodes · news (read-only) │
                        └──────────────┬───────────────┘
                                       │ snapshot listener (+ offline cache)
                                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  MyanCast Android App                                           │
│                                                                 │
│  Home · Detail · Player · News · Search · Library · Settings    │
│                                                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────────┐  │
│  │ Room (ဖုန်းထဲ) │  │ Media3 Service │  │ SharedPreferences 🔜 │  │
│  │ subscribe ·   │  │ background ·  │  │ settings            │  │
│  │ history ·     │  │ notification  │  └─────────────────────┘  │
│  │ downloads 🔜  │  └───────────────┘  ┌─────────────────────┐  │
│  └───────────────┘                     │ WorkManager 🔜       │  │
│                                        │ episode ဒေါင်းလုဒ်    │  │
│                                        └─────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS (MP3 stream / download)
                               ▼
                     Audio hosting (audioUrl)
```

### 4.2 In Scope / Out of Scope (အတိုချုပ်)

| In Scope | Out of Scope |
|---|---|
| Podcast/သတင်း ကြည့်ခြင်း၊ နားထောင်ခြင်း | Content upload / ထုတ်လုပ်ခြင်း |
| Background playback + notification | Login / user account |
| Subscribe · history · ဆက်နားထောင်ရန် (ဖုန်းထဲ) | Cloud sync (ဖုန်းချင်း) |
| ရှာဖွေ (title/category) | Full-text search server |
| Dark/Light · မြန်မာ localization | Payment · ကြော်ငြာ |
| 🔜 ဒေါင်းလုဒ် · offline · data ချွေတာ | Comment · like · social |
| 🔜 Zawgyi detect/convert · ပြက္ခဒိန် · စာလုံးအရွယ် | Bluetooth amplifier · kiosk · broadcast |
| ⚪ Share · FCM · widget | Cloud Functions · Auto scheduler |

> Out of scope အပြည့်အစုံ + အကြောင်းပြချက် — section 13။

---

## 5. Feature စာရင်း အပြည့်အစုံ

> **ID** — `F-xx` · **ရင်းမြစ်** — `MVP#n` = `fullscope.md` MVP · `NTH#n` = nice-to-have · `P6#n` = `phase6-showcase.md`

### 5.1 ရှာဖွေ နားထောင်ခြင်း (Discovery — Home)

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-01 | Podcast စာရင်း | Firestore `podcasts` ကနေ card များ · real-time update | MVP#1 | ✅ |
| F-02 | အမျိုးအစား filter | Category chip (`"အားလုံး"` + data ထဲက category) · ရလဒ် မရှိရင် inline စာ | MVP#1 | ✅ |
| F-03 | လူကြိုက်များနေသည် | Home မှာ podcast ၅ ခု အလျားလိုက် section | Phase 2 | ✅ |
| F-04 | ဆက်နားထောင်ရန် card | နောက်ဆုံး နားထောင်ခဲ့တဲ့ (မပြီးသေးတဲ့) ပိုင်း · progress · ▶/⏸ | MVP#2 | ✅ |
| F-05 | ယနေ့ မြန်မာ ရက်စွဲ | Home header မှာ "…ခု …လ လပြည့်ကျော် …ရက်" | P6#7 | 🔜 |
| F-06 | ဥပုသ်နေ့ တရားတော် | ဥပုသ်နေ့မှာ "တရားတော်" category section ပေါ် | P6#7 | 🔜 |

### 5.2 Podcast အသေးစိတ် (Detail)

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-07 | Podcast header + ပိုင်းစာရင်း | Cover · title · category · ပိုင်း အရေအတွက် · ရက်စွဲ/ကြာချိန် · ပိုင်း မရှိရင် inline စာ | MVP#3 | ✅ |
| F-08 | ပိုင်းတစ်ခု ဖွင့် | နှိပ်ရင် အဲဒီ ပိုင်းကနေ queue နဲ့ ဖွင့် | MVP#4 | ✅ |
| F-09 | အားလုံး ဖွင့် | Podcast တစ်ခုလုံး queue | Phase 3 | ✅ |
| F-10 | သိမ်း / မသိမ်း (Subscribe) | Room မှာ သိမ်း · ခလုတ် state ပြောင်း | MVP#10 | ✅ |
| F-11 | ပိုင်းအလိုက် data size | "၂၄ မိနစ် · ~၁၁ MB" | P6#5 | 🔜 |
| F-12 | ဒေါင်းလုဒ် ခလုတ် | ⬇ / % / ✓ / ⚠ | P6#4 | 🔜 |
| F-13 | Mobile data သတိပေး | Data ချွေတာ ON + mobile data → "~၁၁ MB — ဖွင့်မှာလား" | P6#5 | 🔜 |

### 5.3 Player

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-14 | Play / Pause | Stream ဖွင့် · buffering ပြ · error ဆို ပြန်ကြိုးစား | MVP#4 | ✅ |
| F-15 | Seek | Seek bar ဆွဲ · −၁၅ စက္ကန့် / +၃၀ စက္ကန့် | MVP#5 | ✅ |
| F-16 | နောက်/ရှေ့ ပိုင်း | Queue ထဲ ပြောင်း | Phase 3 | ✅ |
| F-17 | Playback speed | 0.5x – 2.0x | MVP#15 | ✅ |
| F-18 | Mini player | Screen တိုင်း အောက်ခြေ · နှိပ်ရင် Full Player | MVP#6 | ✅ |
| F-19 | Full player | Cover · title · seek · controls · speed | MVP#7 | ✅ |
| F-20 | Background playback | App ပိတ်လည်း ဆက်ဖွင့် · notification · lock screen controls | MVP#4 | ✅ |
| F-21 | Audio focus | ဖုန်းဝင်ရင်/အခြား app ဖွင့်ရင် ရပ် · headphone ဖြုတ်ရင် ရပ် | Phase 3 | ✅ |
| F-22 | History အလိုအလျောက် သိမ်း | ၁၀ စက္ကန့် တစ်ခါ · pause/ပိုင်းပြောင်းရင် ချက်ချင်း | Phase 4 | ✅ |
| F-23 | Sleep timer | ၁၅/၃၀/၆၀ မိနစ် · "ဒီပိုင်း ဆုံးရင်" · အသံ တဖြည်းဖြည်း လျှော့ | NTH#16 · P6#3 | 🔜 |
| F-24 | Local file playback | ဒေါင်းထားရင် file ကနေ ဖွင့် (ဘယ် screen ကနေ ဖွင့်ဖွင့်) | P6#4 | 🔜 |
| F-25 | Zawgyi notification | Zawgyi ဖုန်းမှာ notification title ကို Zawgyi ပြောင်းပြ | P6#6 | 🔜 |
| F-26 | Bookmark | အချိန်မှတ် + မှတ်စု · နှိပ်ရင် အဲဒီနေရာ ပြန်သွား | P6#9 | ⚪ |

### 5.4 သတင်း (News)

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-27 | သတင်း စာရင်း | Featured card ၁ ခု + category chip + list (နောက်ဆုံး ၅၀) | MVP#8 | ✅ |
| F-28 | သတင်း အသေးစိတ် | ပုံ · ခေါင်းစဉ် · category · ရက်စွဲ · စာကိုယ် | MVP#9 | ✅ |
| F-29 | သတင်း audio | Audio ရှိမှ "▶ နားထောင်မည်" ပြ · player တူတူ သုံး | MVP#9 | ✅ |

### 5.5 ရှာဖွေ (Search)

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-30 | Podcast ရှာ | Title/category ထဲ ပါရင် တွေ့ (client-side) · state ၄ မျိုး | MVP#12 | ✅ |
| F-31 | Zawgyi query ပြောင်း | Zawgyi နဲ့ ရိုက်ရင် Unicode ပြောင်းရှာ + hint chip | P6#6 | 🔜 |

### 5.6 စာကြည့်တိုက် (Library)

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-32 | သိမ်းထားသော tab | Subscribe လုပ်ထားတဲ့ podcast များ | MVP#11 | ✅ |
| F-33 | မှတ်တမ်း tab | နားထောင်ခဲ့တဲ့ ပိုင်းများ + progress bar | MVP#11 | ✅ |
| F-34 | ဒေါင်းလုဒ် tab | ဒေါင်းထားတဲ့ ပိုင်း · စုစုပေါင်း MB · ဖျက် · offline ဖွင့် | NTH#20 · P6#4 | 🔜 |

### 5.7 ဆက်တင် (Settings)

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-35 | Dark / Light | Gold-accent theme ၂ မျိုး | MVP#14 | ✅ |
| F-36 | Zawgyi font toggle | Font ပဲ ပြောင်းလို့ Unicode data ပျက် → disable | MVP#13 | ⏸️ → F-41 |
| F-37 | Setting သိမ်း | App ပိတ်/ဖွင့်လည်း မပျောက် | P6#1 | 🔜 |
| F-38 | စာလုံးအရွယ် | ပုံမှန် / ကြီး / အကြီးဆုံး (1.0 / 1.15 / 1.3) | P6#8 | 🔜 |
| F-39 | လွယ်ကူ mode | စာလုံး အကြီးဆုံး + player ခလုတ်ကြီး | P6#8 | 🔜 |
| F-40 | Data ချွေတာ · Wi-Fi only ဒေါင်း | Mobile data မှာ cover အသစ် မဒေါင်း · stream မတိုင်မီ မေး | P6#5 · P6#4 | 🔜 |
| F-41 | ဖုန်း font (auto-detect) | Zawgyi/Unicode ကို အလိုလို သိ · ကိုယ်တိုင် ပြောင်းလို့ရ | P6#6 | 🔜 |

### 5.8 Connectivity / Offline

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-42 | Firestore offline cache | ကြည့်ဖူးတဲ့ data ကို လိုင်းမရှိလည်း ပြ (Firestore default) | — | ✅ |
| F-43 | Offline banner | "အင်တာနက် မရှိပါ — သိမ်းထားတဲ့ data ကို ပြနေပါတယ်" | P6#2 | 🔜 |
| F-44 | Offline error မှန်မှန် ပြ | Cache မရှိ + offline → "data မရှိ" မဟုတ်ဘဲ "အင်တာနက် ချိတ်ပါ" | P6#2 | 🔜 |
| F-45 | ဆက်ဒေါင်း (resume) | လိုင်းပြတ်ပြီး ပြန်ရရင် ရပ်ခဲ့တဲ့ % ကနေ ဆက် | P6#4 | 🔜 |

### 5.9 Optional (Tier 3)

| ID | Feature | ဖော်ပြချက် | ရင်းမြစ် | Status |
|---|---|---|---|---|
| F-46 | Share | Viber/Messenger ကို ပိုင်း ခေါင်းစဉ် + link | P6#10 | ⚪ |
| F-47 | ပိုင်းအသစ် notification | FCM topic · Console ကနေ ပို့ | NTH#21 · P6#11 | ⚪ |
| F-48 | Crash report | Crashlytics | P6#12 | ⚪ |
| F-49 | Home screen widget | ဆက်နားထောင်ရန် + ▶ | P6#13 | ⚪ |

### 5.10 အနှစ်ချုပ်

| Status | အရေအတွက် |
|---|---|
| ✅ v1.0 ပြီး | ၂၅ |
| 🔜 v2.0 (Tier 1 + 2) | ၁၈ |
| ⚪ v2.0 optional | ၅ |
| ⏸️ ဆိုင်းငံ့ | ၁ (F-36 → F-41 နဲ့ အစားထိုး) |
| **စုစုပေါင်း** | **၄၉** |

---

## 6. Screen အလိုက် Functional Specification

### 6.0 Screen တိုင်း လိုက်နာရမယ့် စည်းကမ်း

**State အစဉ်** — `isLoading → LoadingView` · `error → ErrorView (ပြန်ကြိုးစား)` · `data လုံးဝ မရှိ → EmptyView` · `content`

| စည်းကမ်း | အသေးစိတ် |
|---|---|
| Full-screen EmptyView | **Data လုံးဝ မရှိမှ** ပဲ · filter/child list အလွတ်က inline စာ (screen ကျန်တာ သုံးလို့ရနေ) |
| မြန်မာစာ style | `body*` / `title*` / `headline*` / `display*` ပဲ · `label*` (Poppins) က ASCII ပဲ (`"1.5x"`, `"24:30"`) |
| ဂဏန်း | မြန်မာ ဂဏန်း + မြန်မာ စကား — `"၁၂ ပိုင်း"` (`"12 episodes"` မဟုတ်) |
| Rotate | State မပျောက်ရ |
| Mini player | Tab screen တိုင်းမှာ ပေါ် (ဖွင့်ထားရင်) |
| 🔜 Offline banner | Screen တိုင်း အပေါ်ဆုံး |

### 6.1 Navigation

```
Bottom Nav (၅ ခု)
① ပင်မ (Home)         ──▶ Podcast Detail ──▶ Full Player
② သတင်း (News)        ──▶ News Detail    ──▶ Full Player (audio ရှိရင်)
③ ရှာဖွေ (Search)       ──▶ Podcast Detail
④ စာကြည့်တိုက် (Library) ──▶ Podcast Detail / Full Player
⑤ ဆက်တင် (Settings)
Mini player (အားလုံး)  ──▶ Full Player
```

- Tab ပြောင်းရင် start ကို pop + state save/restore (tab တစ်ခုချင်းရဲ့ scroll မပျောက်)
- Route အားလုံး `Screen.kt` မှာ · hard-code string မသုံး

### 6.2 Home (ပင်မ)

| အပိုင်း | အသေးစိတ် | Status |
|---|---|---|
| Greeting | "မင်္ဂလာပါ 👋" · "ဒီနေ့ ဘာနားထောင်မလဲ?" | ✅ |
| မြန်မာ ရက်စွဲ | ယနေ့ မြန်မာ ရက်စွဲ တစ်ကြောင်း | 🔜 |
| ဆက်နားထောင်ရန် | မပြီးသေးတဲ့ နောက်ဆုံး ပိုင်း · card နှိပ် → Full Player · ခလုတ် → screen မပြောင်းဘဲ ဖွင့်/ရပ် · မပြီးရင် snackbar | ✅ |
| ဥပုသ်နေ့ တရားတော် | ဥပုသ်နေ့မှပဲ ပေါ် | 🔜 |
| အမျိုးအစားများ | Chip · `"အားလုံး"` default | ✅ |
| လူကြိုက်များနေသည် | ၅ ခု အလျားလိုက် | ✅ |
| Podcast အားလုံး | Filter လုပ်ပြီး list · မတွေ့ရင် "ဒီအမျိုးအစားမှာ မရှိသေးပါ" | ✅ |

**Rule** — ဆက်နားထောင်ရန် card က `isFinished` (≥ ၉၅%) ဆို မပြ · ဖွင့်ထားပြီးသား ပိုင်းကို ပြန်နှိပ်ရင် အစက ပြန်မစ။

### 6.3 Podcast Detail

| အပိုင်း | အသေးစိတ် | Status |
|---|---|---|
| Header | Cover · title · `"{category} · {n} ပိုင်း"` · description | ✅ |
| ခလုတ် | "▶ အားလုံး ဖွင့်" · "သိမ်း / သိမ်းပြီး" | ✅ |
| ပိုင်းများ | Title · ရက်စွဲ ("၅ ရက် အရင်က") · ကြာချိန် · ဖွင့်နေတာ ⏸ icon | ✅ |
| Size + ဒေါင်းလုဒ် | "~၁၁ MB" + ⬇ | 🔜 |
| Error | Podcast မရှိ → "Podcast ရှာမတွေ့ပါ" · ပိုင်း မရှိ → header ထား + "ပိုင်း မရှိသေးပါ" | ✅ |

**Rule** — `audioUrl` အလွတ် ပိုင်းကို queue ထဲ မထည့် · နှိပ်တဲ့ ပိုင်းက ဖွင့်လို့ မရရင် queue အလွတ် (ဘာမှ မဖွင့်) + error ပြ။

### 6.4 Mini Player

- Cover · title · podcast · ▶/⏸ · progress line
- ဖွင့်ထားတာ မရှိရင် မပေါ်
- နှိပ် → Full Player

### 6.5 Full Player

| အပိုင်း | အသေးစိတ် | Status |
|---|---|---|
| Cover · title · podcast | ကြီးကြီး | ✅ |
| Seek bar | `"24:30"` / `"1:02:05"` (ASCII) · duration မသိရင် `episode.duration` သုံး | ✅ |
| Controls | ⏮ · −15s · ▶/⏸ · +30s · ⏭ | ✅ |
| Speed | 0.5x · 0.75x · 1x · 1.25x · 1.5x · 2x | ✅ |
| Error | "ဖွင့်လို့ မရပါ — အင်တာနက် ချိတ်ဆက်မှု စစ်ကြည့်ပါ" | ✅ |
| 🌙 Sleep timer | Bottom sheet · ကျန်ချိန် "၁၂ မိနစ် ကျန်" | 🔜 |
| 🔖 Bookmark | | ⚪ |
| လွယ်ကူ mode | ခလုတ် ကြီး | 🔜 |

### 6.6 News · News Detail

| အပိုင်း | အသေးစိတ် | Status |
|---|---|---|
| Featured | နောက်ဆုံး သတင်း ၁ ခု card ကြီး | ✅ |
| Category chip | Filter · empty ၂ မျိုး (data မရှိ / filter ရလဒ် မရှိ) | ✅ |
| Detail | ပုံ · ခေါင်းစဉ် · category · ရက်စွဲ · စာကိုယ် | ✅ |
| Audio | `audioUrl` ရှိမှ ခလုတ် ပြ (မရှိရင် လုံးဝ မပြ) · player တူတူ | ✅ |

### 6.7 Search

| State | ပြပုံ | Status |
|---|---|---|
| Loading | LoadingView | ✅ |
| Error | ErrorView | ✅ |
| Idle (query အလွတ်) | "ရှာလိုသည်ကို ရိုက်ပါ" | ✅ |
| မတွေ့ | "ရှာမတွေ့ပါ" | ✅ |
| ရလဒ် | Podcast list → Detail | ✅ |
| Zawgyi hint | "Zawgyi နဲ့ ရိုက်ထားလို့ Unicode ပြောင်းရှာထားပါတယ်" | 🔜 |

**Rule** — trim · စာလုံးအကြီးအသေး မခွဲ · title (သို့) category ထဲ ပါရင် တွေ့ · ✕ ခလုတ် · keyboard ရဲ့ "Search"။

### 6.8 Library

| Tab | ပါဝင်ချက် | Empty | Status |
|---|---|---|---|
| သိမ်းထားသော | Subscribe podcast များ → Detail | "သိမ်းထားတာ မရှိသေးပါ" | ✅ |
| မှတ်တမ်း | ပိုင်း + progress bar · နောက်ဆုံး နားထောင်တာ အပေါ် | "နားထောင်ထားတာ မရှိသေးပါ" | ✅ |
| ဒေါင်းလုဒ် | ပိုင်း + MB · စုစုပေါင်း MB · ဖျက် · offline ဖွင့် | "ဒေါင်းထားတာ မရှိသေးပါ" | 🔜 |

### 6.9 Settings

| Row | v1.0 | v2.0 |
|---|---|---|
| အမှောင် အပြင်အဆင် | ✅ Toggle (app ပိတ်ရင် ပျောက်) | 🔜 သိမ်းထား |
| ဇော်ဂျီ စာလုံး | ⏸️ Disable | 🔜 "ဖုန်း font: Zawgyi/Unicode (အလိုအလျောက် သိ)" နဲ့ အစားထိုး |
| စာလုံးအရွယ် | — | 🔜 ၃ ဆင့် + "မင်္ဂလာပါ" preview |
| လွယ်ကူ mode | — | 🔜 |
| Data ချွေတာ | — | 🔜 |
| Wi-Fi နဲ့ပဲ ဒေါင်း | — | 🔜 (default ON) |
| Version | "MyanCast · ဗားရှင်း ၁.၀" | ၂.၀ |

---

## 7. Player Specification

### 7.1 PlaybackState (app တစ်ခုလုံး ဒီ state တစ်ခုတည်း ဖတ်)

| Field | Type | အဓိပ္ပာယ် | Version |
|---|---|---|---|
| `queue` | `List<Episode>` | ဖွင့်မယ့် ပိုင်းစာရင်း | ✅ |
| `currentIndex` | `Int` | လက်ရှိ · `-1` = မဖွင့်ရသေး | ✅ |
| `podcastTitle` | `String` | Mini player subtitle | ✅ |
| `isPlaying` · `isBuffering` | `Boolean` | | ✅ |
| `positionMs` · `durationMs` | `Long` | ms · `durationMs = 0` = မသိရသေး | ✅ |
| `speed` | `Float` | 0.5 – 2.0 | ✅ |
| `error` | `String?` | မြန်မာ error စာ | ✅ |
| `sleepTimerEndsAtMs` | `Long?` | null = timer မရှိ | 🔜 |
| `sleepAtEpisodeEnd` | `Boolean` | | 🔜 |

တွက်ယူတဲ့ field — `currentEpisode` · `hasEpisode` · `hasNext` · `hasPrevious` · `progress (0–1)`

### 7.2 PlayerController (ViewModel တွေ သိတာ ဒီ interface ပဲ — Media3 ကို မသိ)

| Operation | အလုပ် | Version |
|---|---|---|
| `play(queue, podcastTitle, startPositionMs = 0)` | Queue အလွတ် ဆို ဘာမှ မလုပ် · start position ကို Media3 ကိုယ်တိုင် ကိုင် | ✅ |
| `togglePlayPause()` | Error ပြီးရင် ပြန် prepare · ဆုံးသွားရင် အစက | ✅ |
| `seekTo(ms)` · `skipBack()` (−15s) · `skipForward()` (+30s) | | ✅ |
| `next()` · `previous()` | | ✅ |
| `setSpeed(speed)` | | ✅ |
| `setSleepTimer(option)` | `Off` · `Minutes(n)` · `EndOfEpisode` | 🔜 |

### 7.3 Behaviour

| အခြေအနေ | Behaviour | Status |
|---|---|---|
| App background / screen ပိတ် | ဆက်ဖွင့် · notification + lock screen controls | ✅ |
| Notification နှိပ် | App ပြန်ဖွင့် | ✅ |
| ဖုန်းဝင် / အခြား app အသံ | Audio focus — ရပ် | ✅ |
| Headphone ဖြုတ် | ရပ် | ✅ |
| Recent apps ကနေ swipe | ဖွင့်နေရင် ဆက် · မဖွင့်ရင် service ရပ် | ✅ |
| Service မချိတ်ရသေးခင် `play()` | မှတ်ထားပြီး ချိတ်မိတာနဲ့ ဖွင့် (retry) | ✅ |
| Content type | `AUDIO_CONTENT_TYPE_SPEECH` | ✅ |
| Position update | ဖွင့်နေတုန်း ၅၀၀ms တစ်ခါ | ✅ |
| History သိမ်း | ၁၀ စက္ကန့် bucket · pause/ပိုင်းပြောင်း ချက်ချင်း · `durationMs = 0` ဆို မသိမ်း · ≥ ၉၅% = ပြီး | ✅ |
| Sleep timer ပြည့် | ၁၀ စက္ကန့် fade → pause → volume ပြန်ထား | 🔜 |
| "ဒီပိုင်း ဆုံးရင်" | နောက်ပိုင်း ၀:၀၀ မှာ ရပ် | 🔜 |
| ဒေါင်းထားတဲ့ ပိုင်း | Local file · file မရှိတော့ရင် stream | 🔜 |

---

## 8. Myanmar Localization Specification

### 8.1 Font

| Style | Font | သုံးပုံ |
|---|---|---|
| display · headline · title · body | **Padauk** (bundle) | မြန်မာစာ အားလုံး · ဖုန်းရဲ့ system font မမှီခို |
| label | **Poppins** | ASCII ပဲ (`"24:30"`, `"1.5x"`) — **မြန်မာ glyph မရှိ** |
| Line height | font size × ~1.8 | ဆင့်ဗျည်း၊ ချောင်းငင် မပြတ်အောင် |
| 🔜 Text scale | × 1.0 / 1.15 / 1.3 | fontSize + lineHeight နှစ်ခုလုံး |

### 8.2 Format Function များ (pure · unit test ပါ)

| Function | Input → Output | Status |
|---|---|---|
| `toMyanmarDigits()` | `12` → `"၁၂"` | ✅ |
| `formatDuration(s)` | `1470` → `"24:30"` · `3725` → `"1:02:05"` (ASCII) | ✅ |
| `formatDurationLabel(s)` | `"၂၄ မိနစ်"` · `"၁ နာရီ ၂ မိနစ်"` · `"၁ မိနစ် အောက်"` · `≤0` → `""` | ✅ |
| `formatRelativeDate(ts, now)` | `"ခုလေးတင်"` · `"၅ ရက် အရင်က"` · `"၁၅ စက်တင်ဘာ ၂၀၂၅"` | ✅ |
| `formatMegabytes(bytes)` | `"~၁၁ MB"` · `"~၁ MB အောက်"` | 🔜 |
| `sleepTimerLabel(...)` | `"၁၂ မိနစ် ကျန်"` · `"ဒီပိုင်း ဆုံးရင် ရပ်မယ်"` | 🔜 |
| `myanmarDate(ms).label()` | `"…ခု …လ လပြည့်ကျော် …ရက်"` | 🔜 |
| `normalizeToUnicode(text)` | Zawgyi → Unicode (မဟုတ်ရင် မပြောင်း) | 🔜 |

> လအမည်ကို hard-code array နဲ့ ရေးထား — `Locale("my")` က ဖုန်းအချို့မှာ English ပြန်ကျလို့။

### 8.3 Zawgyi / Unicode Strategy

| အဆင့် | ဆုံးဖြတ်ချက် | ဘာကြောင့် |
|---|---|---|
| Data | **Unicode ပဲ** (Firestore) | စံ · Google/Apple ထောက်ပံ့ |
| App ထဲ ပြ | **Padauk bundle** → ဖုန်း မရွေး မှန် | System font မမှီခို |
| v1.0 Zawgyi toggle | ⏸️ Disable | Font ပဲ ပြောင်းလို့ Unicode စာ ပျက် |
| v2.0 Search | 🔜 Query detect → Unicode ပြောင်း | Zawgyi keyboard ရိုက်သူ |
| v2.0 Notification | 🔜 Zawgyi ဖုန်း ဆို title Zawgyi ပြောင်း | Notification က system font သုံး |
| v2.0 Detect | 🔜 `"က္က"` width တိုင်း (stack/မ stack) | ပထမ ဖွင့်ချိန် တစ်ခါ · ကိုယ်တိုင် ပြောင်းလို့ရ |

### 8.4 စာသား စည်းကမ်း

- User မြင်ရတဲ့ စာ အားလုံး မြန်မာလို (ချွင်းချက် — "Podcast", "MB", ASCII အချိန်/speed)
- Error စာက **ဘာလုပ်ရမလဲ** ပါ — "ဖွင့်လို့ မရပါ — အင်တာနက် ချိတ်ဆက်မှု စစ်ကြည့်ပါ"
- `contentDescription` (screen reader) လည်း မြန်မာလို
- Category "အားလုံး" က constant `ALL_CATEGORY` တစ်ခုတည်း

---

## 9. Data Specification

### 9.1 Firestore (read-only · admin က Console ကနေ ထည့်)

```
📁 podcasts/{podcastId}
   ├─ title: String
   ├─ description: String
   ├─ coverUrl: String
   ├─ category: String           ← "နည်းပညာ" · "သတင်း" · 🔜 "တရားတော်"
   ├─ episodeCount: Number
   └─ updatedAt: Timestamp

📁 episodes/{episodeId}
   ├─ podcastId: String          ← podcasts ရဲ့ document ID
   ├─ title · description: String
   ├─ audioUrl: String           ← MP3 URL (အလွတ် = ဖွင့်လို့ မရ)
   ├─ duration: Number           ← စက္ကန့်
   ├─ publishedAt: Timestamp
   ├─ coverUrl: String           ← optional
   └─ fileSizeBytes: Number      ← 🔜 optional · မရှိရင် ခန့်မှန်း

📁 news/{newsId}
   ├─ headline · body: String
   ├─ audioUrl: String?          ← optional
   ├─ category: String
   └─ publishedAt: Timestamp
```

| စည်းကမ်း | အကြောင်းရင်း |
|---|---|
| Property တိုင်း default value | Firestore deserialization |
| Boolean မှာ `is` prefix မသုံး | Firestore mapping ပျက် |
| Document ID ကို `snapshotFlow { obj, id -> obj.copy(id = id) }` နဲ့ ထည့် | ID က field ထဲ မပါ |
| `episodes` index — `podcastId ↑` + `publishedAt ↓` | Composite query |
| Security rule — `allow read: if true` · write ပိတ် | Read-only |

### 9.2 Room (ဖုန်းထဲ · `myancast.db`)

| Table | Version | Field | Status |
|---|---|---|---|
| `library` | v2 | `id` · `type` (`"sub"`/`"history"`) · `podcastId` · `positionMs` · `durationMs` · `updatedAt` · `episodeTitle` · `podcastTitle` · cover | ✅ |
| `downloads` | v3 | `episodeId` · podcast/episode metadata · `audioUrl` · `localPath` · `sizeBytes` · `status` · `downloadedAt` | 🔜 |
| `bookmarks` | v3 | `id` · `episodeId` · `positionMs` · `note` · `createdAt` | ⚪ |

> v2 → v3 က `Migration(2, 3)` နဲ့ — subscribe/history မပျက်ရ။

### 9.3 Settings (🔜 SharedPreferences)

| Key | Default | |
|---|---|---|
| `darkMode` | `true` | |
| `textScale` | `1.0` | 1.0 / 1.15 / 1.3 |
| `easyMode` | `false` | |
| `dataSaver` | `false` | |
| `wifiOnlyDownload` | `true` | |
| `deviceFont` | `UNKNOWN` | `UNICODE` / `ZAWGYI` |

### 9.4 ဖုန်းထဲ file (🔜)

`filesDir/episodes/<episodeId>.mp3` (ဒေါင်းနေတုန်း `.mp3.part`) — internal storage · permission မလို · uninstall ရင် ပျက်

---

## 10. System Architecture

### 10.1 Layer ၃ ခု

```
┌──────────────────────────────────────────────────────────┐
│ UI  (ui/<feature>/)                                       │
│ Compose Screen ◀── StateFlow<UiState> ◀── ViewModel       │
│ (collectAsStateWithLifecycle)   (android.* import မရှိ)   │
└──────────────────────────┬───────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────┐
│ DOMAIN  (domain/)                                         │
│ model — Podcast · Episode · NewsItem · PlaybackState ·    │
│         PlaybackProgress · 🔜 AppSettings · DownloadStatus │
│ util  — pure format/convert function (unit test)          │
└──────────────────────────┬───────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────┐
│ DATA + MEDIA                                              │
│ data/repository — PodcastRepository · NewsRepository ·    │
│   LibraryRepository · 🔜 SettingsRepository ·              │
│   🔜 DownloadRepository · 🔜 NetworkMonitor   (interface)   │
│ data/firebase · data/local (Room)                         │
│ player/ — PlayerController (interface) · Media3 impl ·    │
│   PlaybackService · PlayerQueue · HistoryRecorder         │
└──────────────────────────────────────────────────────────┘
```

### 10.2 Architecture စည်းကမ်း

| စည်းကမ်း | ဘာကြောင့် |
|---|---|
| UI က Firebase/Room/Media3 ကို တိုက်ရိုက် မခေါ် | Layer ခွဲ · test |
| ViewModel မှာ `android.*` / Media3 / Room type မရှိ | JVM unit test |
| Media3 က `player/` အပြင် မထွက် · Room က `data/local/` အပြင် မထွက် | အစားထိုးလို့ရ |
| DI framework မသုံး — `MyanCastApp` မှာ singleton (`playerController`, `libraryRepository`, 🔜 `settingsRepository`, `networkMonitor`, `downloadRepository`) | Course scale မှာ လုံလောက် |
| ViewModel factory — `companion object { fun factory(...) }` · app singleton ကို `APPLICATION_KEY` နဲ့ ယူ | DI မပါဘဲ inject |
| `loadJob?.cancel()` ပြီးမှ ပြန် collect | Snapshot listener မပွား |
| `!!` မသုံး | Crash ကာကွယ် |

### 10.3 Data Flow ဥပမာ — "ဆက်နားထောင်ရန်"

```
Player ဖွင့် → PlaybackState (StateFlow)
  → HistoryRecorder (app scope, ၁၀s throttle) → LibraryRepository.saveProgress → Room
  → App ပိတ်/ဖွင့်
  → HomeViewModel ← LibraryRepository.lastPlayed() (Flow) → card ပြ
  → နှိပ် → PlayerController.play(queue, title, startPositionMs) → ရပ်ခဲ့တဲ့ နေရာက ဆက်
```

---

## 11. Non-Functional Requirements

| ID | အမျိုးအစား | Requirement | ရည်မှန်း / တိုင်းပုံ | Status |
|---|---|---|---|---|
| NFR-01 | Compatibility | Android 7.0 (API 24) – Android 15 (API 35) | Device ၂ မျိုး (API မတူ) စမ်း | ☐ |
| NFR-02 | Performance | Cold start | ၂ စက္ကန့် အောက် | ☐ |
| NFR-03 | Performance | Scroll | Frame skip ၅၀ အောက် (Logcat) | ☐ |
| NFR-04 | Reliability | Crash မရှိ | Airplane mode · nav ၂၀ ခါ မြန်မြန် · back · rotate | ☐ |
| NFR-05 | Reliability | Listener leak မရှိ | Screen ဝင်/ထွက် ၁၀ ခါ → memory မတက် | ☐ |
| NFR-06 | Offline | ကြည့်ဖူးတဲ့ data ပြ | Firestore cache | ✅ |
| NFR-07 | Offline | ဒေါင်းထားတာ လိုင်းမရှိလည်း ဖွင့် | Airplane mode demo | 🔜 |
| NFR-08 | Data usage | Stream မတိုင်မီ size သိ · mobile data မှာ မလိုတာ မဒေါင်း | MB ပြ · data ချွေတာ · Wi-Fi only | 🔜 |
| NFR-09 | Battery | Background ပေါ့ပါး · အိပ်ပျော်ရင် ရပ် | Position tick ဖွင့်နေမှ · sleep timer | ✅ / 🔜 |
| NFR-10 | Storage | ဒေါင်းထားတာ size မြင်ရ · ဖျက်လို့ရ · storage နည်းရင် မဒေါင်း | `setRequiresStorageNotLow` | 🔜 |
| NFR-11 | Localization | မြန်မာစာ မှန် (ဖုန်း မရွေး) | Padauk · Poppins မှာ မြန်မာ မရှိ | ✅ |
| NFR-12 | Accessibility | Screen reader · စာလုံးကြီး | မြန်မာ `contentDescription` ✅ · text scale 🔜 | ✅ / 🔜 |
| NFR-13 | Privacy | Login မရှိ · ကိုယ်ရေး data server မတင် · history ဖုန်းထဲပဲ | Room local | ✅ |
| NFR-14 | Security | Firestore read-only | Rule — write ပိတ် | ✅ |
| NFR-15 | Testability | Logic အားလုံး JVM test | Interface + fake + pure function | ✅ |
| NFR-16 | Maintainability | Build warning ၀ · code style တစ်မျိုး | `clean assembleDebug` | ✅ |

---

## 12. Quality & Testing

### 12.1 Test Strategy

| အလွှာ | နည်း | ဥပမာ |
|---|---|---|
| Pure function | JUnit — input/output | `DurationFormatterTest` · `DateFormatterTest` · `CategoryFilterTest` · `SearchFilterTest` · `NewsFilterTest` · `PlayerQueueTest` |
| ViewModel | Fake + `StandardTestDispatcher` + `advanceUntilIdle()` (သို့) MockK + `UnconfinedTestDispatcher` | `HomeViewModelTest` · `PodcastDetailViewModelTest` · `LibraryViewModelTest` · `NewsViewModelTest` · `SearchViewModelTest` · `PlayerViewModelTest` · `MiniPlayerViewModelTest` |
| Repository (Room) | Robolectric + in-memory Room | `RoomLibraryRepositoryTest` |
| Player logic | `FakePlayerController` | `HistoryRecorderTest` |
| Device | ဖုန်းအစစ် checklist | Background · lock screen · ဖုန်းဝင် · airplane mode · rotate |

**Test fake များ** — `FakePodcastRepository` · `FakeNewsRepository` · `FakeLibraryRepository` · `FakePlayerController`
🔜 `FakeSettingsRepository` · `FakeNetworkMonitor` · `FakeDownloadRepository`

### 12.2 ကိန်းဂဏန်း

| | v1.0 | v2.0 ရည်မှန်း |
|---|---|---|
| Unit test | ၁၀၉ pass | ၁၄၀+ |
| Device test | ၃၂ ခု (Phase 3 + 4 + 5 — ☐ Phase 5 မှာ ပြီးအောင်) | + ၁၅ ခု |
| Build warning | ၀ | ၀ |

### 12.3 Quality Checklist (`fullscope.md`)

| # | စစ်ချက် | v1.0 |
|---|---|---|
| 1 | Crash မဖြစ်ဘဲ run | ☐ (Phase 5 device test) |
| 2 | Bottom nav ၅ ခု အလုပ်လုပ် | ✅ |
| 3 | Podcast list Firestore ကနေ ပေါ် | ✅ |
| 4 | ပိုင်း ဖွင့်နားထောင်နိုင် | ✅ |
| 5 | Mini player screen အားလုံးမှာ | ✅ |
| 6 | Zawgyi/Unicode — ဆုံးဖြတ်ချက် ရှင်းပြနိုင် | ⏸️ (section 8.3) |
| 7 | News list + detail | ✅ |
| 8 | Subscribe/unsubscribe | ✅ |
| 9 | Search | ✅ |
| 10 | Dark/Light | ✅ |
| 11 | Unit test ၅ ခုအထက် | ✅ (၁၀၉) |
| 12 | README | ☐ (Phase 5) |

---

## 13. Scope အပြင် + Known Limitations

### 13.1 Scope အပြင် (တမင် မလုပ်)

| Feature | အကြောင်းရင်း |
|---|---|
| ❌ User login / account | Read-only design · privacy · course scope |
| ❌ Content upload · comment · like | Write rule + moderation လို |
| ❌ Payment · subscription · ကြော်ငြာ | Scope ပြင် |
| ❌ Cloud Functions · backend server | Firestore + Console နဲ့ လုံလောက် |
| ❌ Amplifier / Bluetooth · Kiosk · Broadcast · Auto scheduler | `fullscope.md` ကတည်းက ဖယ် |
| ❌ မြန်မာ TTS (သတင်း အသံဖတ်) | Device အများစုမှာ မြန်မာ voice မရှိ · cloud TTS = backend |
| ❌ ဖုန်းချင်း file မျှဝေ | Copyright |
| ❌ Hilt · Koin · Retrofit · DataStore | Design မူ P1 |

### 13.2 Known Limitations

| # | Limitation | အကျိုးသက်ရောက် | ဖြေရှင်းမယ့် version |
|---|---|---|---|
| L1 | Setting ကို app ပိတ်ရင် ပြန်ပျောက် (dark mode) | ဖွင့်တိုင်း ပြန်ပြောင်းရ | 🔜 v2.0 (F-37) |
| L2 | Zawgyi toggle disable | Zawgyi ဖုန်း notification/search | 🔜 v2.0 (F-31, F-25, F-41) |
| L3 | System က app ကို သတ်ပြီးရင် mini player အလွတ် | "ဆက်နားထောင်ရန်" ကနေ ပြန်ဖွင့်ရ | လက်ခံ |
| L4 | History limit မထား | Course scale မှာ ပြဿနာ မရှိ | လက်ခံ |
| L5 | Search က client-side (podcast အားလုံး ဆွဲ) | Podcast ထောင်ချီ ဆို နှေး | လက်ခံ (podcast နည်း) |
| L6 | Offline + cache မရှိ ဆို "data မရှိ" ပြမိ | မှားယွင်း message | 🔜 v2.0 (F-44) |
| L7 | Room schema ပြောင်းရင် destructive fallback | Update မှာ data ပျက်နိုင် | 🔜 v2.0 (Migration) |
| L8 | Data size ကို bitrate နဲ့ ခန့်မှန်း | "~" တိကျမှု | 🔜 `fileSizeBytes` ထည့်ရင် |

---

## 14. Roadmap နဲ့ Development Process

### 14.1 Release Plan

| Version | အချိန် | ပါဝင်ချက် | Status |
|---|---|---|---|
| **v1.0** | ၅ ပတ် (Phase 1–5) | MVP ၁၄/၁၅ · test ၁၀၉ · course submit | ✅ |
| **v2.0** | ~၂ ပတ် (Phase 6) | Tier 1 (F-37, F-43/44, F-23, F-12/24/34/45, F-11/13/40) + Tier 2 (Zawgyi, ပြက္ခဒိန်, စာလုံးအရွယ်) | 🔜 |
| v2.x | Show ပြီး | Tier 3 (share · FCM · Crashlytics · widget · bookmark) | ⚪ |
| နောင် | — | Queue screen · favorites · shake to skip · podcast တောင်းဆိုမှု form | အကြံ |

### 14.2 Phase များ

| Phase | အပတ် | ရလဒ် | ပုံစံ |
|---|---|---|---|
| 1 | 1 | Setup · theme · navigation · Firestore data | Layer အလိုက် |
| 2 | 2 | Home + Detail · pure formatter | **Vertical slice** စ |
| 3 | 3 | Player (Media3 service + mini + full) | **Day 0 contract** စ |
| 4 | 4 | News · Library · Search · ဆက်နားထောင်ရန် | Contract (additive) |
| 5 | 5 | Polish · test · README · presentation | တာဝန်အလိုက် · feature freeze |
| 6 | +2 | Project show extension (v2.0) | Contract + slice |

### 14.3 အလုပ်လုပ်ပုံ (Process)

| နည်း | ဖော်ပြချက် | ရလဒ် |
|---|---|---|
| **Vertical slice** | တစ်ယောက်ချင်း feature တစ်ခုကို data → model → UI အထိ | Layer ခွဲတုန်းက တစ်ယောက်ကို တစ်ယောက် စောင့်ရတာ ပျောက် |
| **Day 0 contract** | Interface + model + fake ကို phase အစမှာ ၃ ယောက်လုံး သဘောတူ commit | Phase 3–4 တစ်လျှောက် contract မကျိုး · ပြိုင်တူ ရေး |
| **Additive change** | Contract ပြောင်းရင် default value နဲ့ ထည့် | ကျန် ၂ ယောက် ဘာမှ မပြင်ရ |
| **Cross-review** | လှည့်ပြီး review (C→A · A→B · B→C) | ၃ ယောက်လုံး တစ်ယောက်စီ code ဖတ်ဖူး |
| **Git** | `feature/<phase>-slice-x` → PR → `main` · push မတိုင်မီ build | `.idea/` · `.kotlin/` မပါအောင် `app/src` ပဲ stage |

---

## 15. အဖွဲ့ နဲ့ တာဝန်

| Member | v1.0 (Phase 2–4 slice) | v2.0 (Phase 6 slice) |
|---|---|---|
| **A** | Home · Library + Subscribe · Mini player · theme/navigation | Setting သိမ်း · offline banner · ဒေါင်းလုဒ် UI · စာလုံးအရွယ်/လွယ်ကူ mode · share |
| **B** | Player engine (Media3 service + controller) · News | Sleep timer · ဒေါင်းလုဒ် engine (WorkManager + resume) · Zawgyi notification · FCM/Crashlytics |
| **C** | Formatter (`domain/util`) · Full player · Search · ဆက်နားထောင်ရန် + history | Data size · Zawgyi detect/convert · မြန်မာ ပြက္ခဒိန် · bookmark |

---

## 16. ဝေါဟာရ (Glossary)

| မြန်မာ | English | App ထဲ အဓိပ္ပာယ် |
|---|---|---|
| ပိုင်း | Episode | Podcast ရဲ့ အသံ ဖိုင် တစ်ခု |
| သိမ်း | Subscribe | Podcast ကို Library မှာ ထား |
| မှတ်တမ်း | History | နားထောင်ခဲ့တဲ့ ပိုင်း + progress |
| ဆက်နားထောင်ရန် | Continue Listening | ရပ်ခဲ့တဲ့ နေရာက ပြန်စ |
| ဒေါင်းလုဒ် | Download | ဖုန်းထဲ သိမ်း · offline ဖွင့် |
| ဥပုသ်နေ့ | Sabbath day | လဆန်း/လဆုတ် ၈ ရက် · လပြည့် · လကွယ် |
| လွယ်ကူ mode | Easy mode | စာလုံးကြီး · ခလုတ်ကြီး |
| Data ချွေတာ | Data saver | Mobile data မှာ မလိုတာ မဒေါင်း |
| Contract | — | ၃ ယောက် သဘောတူတဲ့ interface + model (ပြောင်းရင် အားလုံး သဘောတူရ) |
| Fake | Test double | Test မှာ အစစ်အစား သုံးတဲ့ class |
| Pure function | — | Input တူရင် output တူ · side effect မရှိ |

---

## 17. Credits & Licenses

| Component | License | သုံးပုံ |
|---|---|---|
| Padauk font (SIL) | SIL OFL 1.1 | မြန်မာစာ font |
| Poppins | SIL OFL 1.1 | Label (ASCII) |
| Zawgyi-One | — | v1.0 မှာ disable (license စစ်ပြီးမှ ဆက်ထား/ဖယ်) |
| AndroidX · Jetpack Compose · Media3 · Room · WorkManager 🔜 | Apache-2.0 | |
| Firebase SDK | Apache-2.0 (SDK) | |
| Coil | Apache-2.0 | ပုံ load |
| 🔜 Google myanmar-tools | Apache-2.0 | Zawgyi detect |
| 🔜 Rabbit converter | MIT | Zawgyi ↔ Unicode |
| 🔜 Myanmar Calendar algorithm — ဒေါက်တာ ရန်နိုင်အေး | (စစ်ပြီး ဖြည့်) | ပြက္ခဒိန် တွက်ချက် |

---

> **ဆက်စပ် စာတမ်း** — `fullscope.md` (မူလ spec) · `phase1–5.md` (ပြီးခဲ့တဲ့ အဆင့်) ·
> `phase6-showcase.md` (v2.0 implementation plan — Day 0 contract, timeline, demo script) · `CLAUDE.md` (code စည်းကမ်း)
