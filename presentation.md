# MyanCast Personal — Presentation Pack / တင်ပြမှု လမ်းညွှန်

> **Part 1** — Project analysis (English + မြန်မာ)
> **Part 2** — 13 slides: what goes on each slide + presenter script (EN + MM)
> **Part 3** — Demo route, Q&A prep, rehearsal checklist
>
> Presenter split follows `phase5.md` Day 4: **A** = 1–4, 11 · **B** = 5, 9, 12 · **C** = 6–8, 10 · All = 13.
> Target time: **~12 min slides + ~5 min live demo + Q&A**.
> `[brackets]` = fill in before presenting (names, date, etc.).

---

# Part 1 — Project Analysis

## 1.1 What it is / ဘာလဲ

**EN** — MyanCast Personal is an Android app for listening to Myanmar-language podcasts and reading (or listening to) Myanmar news. It was built by a team of 3 in 5 weeks as a course project, in 100% Kotlin with Jetpack Compose. Content lives in Firebase Firestore (entered by hand in the Console, read-only from the app). Personal data — subscriptions and listening history — lives on the phone in Room. Audio plays through Media3, so it keeps playing in the background with a notification and lock-screen controls.

**MM** — MyanCast Personal က မြန်မာလို podcast နားထောင်ဖို့၊ မြန်မာ သတင်း ဖတ်/နားထောင်ဖို့ Android app ပါ။ အဖွဲ့ဝင် ၃ ယောက်၊ ၅ ပတ်အတွင်း course project အဖြစ် Kotlin + Jetpack Compose နဲ့ ရေးထားတာပါ။ Content တွေကို Firebase Firestore မှာ သိမ်းထားပြီး (Console ကနေ ကိုယ်တိုင် ထည့်၊ app ကနေ ဖတ်ရုံပဲ)၊ ကိုယ်ပိုင် data — subscribe လုပ်ထားတာ၊ နားထောင်ခဲ့တဲ့ မှတ်တမ်း — ကို ဖုန်းထဲက Room database မှာ သိမ်းတယ်။ အသံကို Media3 နဲ့ ဖွင့်လို့ app ပိတ်ထားလည်း notification နဲ့ lock screen ကနေ ဆက်ထိန်းလို့ရတယ်။

## 1.2 Key numbers / အဓိက ကိန်းဂဏန်း

| | EN | MM |
|---|---|---|
| Team · time | 3 people · 5 weeks | ၃ ယောက် · ၅ ပတ် |
| Features | 15 MVP features — 14 built + 1 documented decision (Zawgyi) | MVP ၁၅ ခု — ၁၄ ခု ပြီး + Zawgyi ဆုံးဖြတ်ချက် ၁ ခု |
| Screens | 9 screens, 5 bottom tabs | Screen ၉ ခု · bottom tab ၅ ခု |
| Code size | ~5,800 lines app code · ~2,200 lines tests (incl. comments) | App code ~၅,၈၀၀ ကြောင်း · test ~၂,၂၀၀ ကြောင်း |
| Tests | **128** JVM unit tests, all passing | Unit test **၁၂၈** ခု — အားလုံး pass |
| Build | 0 Kotlin/KSP warnings · 16.9 MB release APK | Warning ၀ · Release APK 16.9 MB |
| Android | minSdk 24 (Android 7.0) · targetSdk 35 | minSdk 24 · targetSdk 35 |

## 1.3 Features / လုပ်ဆောင်ချက်

| Screen | Features (EN) | မြန်မာ |
|---|---|---|
| **Home** (ပင်မ) | Podcast list + category filter · "Continue listening" card | Podcast စာရင်း + အမျိုးအစား filter · "ဆက်နားထောင်ရန်" card |
| **Detail** | Podcast info · episode list · "Play all" · Subscribe | အသေးစိတ် · ပိုင်း စာရင်း · "▶ အားလုံး ဖွင့်" · သိမ်း |
| **Player** | Full player: cover, seek bar, −15 s / +30 s, ⏮ ⏭, speed 0.5–2x · background playback | Full player · seek · ၁၅ စက္ကန့် နောက်/၃၀ စက္ကန့် ရှေ့ · speed · background |
| **Mini player** | Bottom of every screen | Screen တိုင်း အောက်ခြေ |
| **News** (သတင်း) | Featured + list + category filter · detail with optional audio | Featured + စာရင်း · အသေးစိတ် + audio |
| **Search** (ရှာဖွေ) | By title / category | Title / အမျိုးအစား နဲ့ ရှာ |
| **Library** (စာကြည့်တိုက်) | Subscriptions + history with progress bars | သိမ်းထားတာ + မှတ်တမ်း (progress bar) |
| **Settings** (ဆက်တင်) | Dark / Light theme · Zawgyi toggle disabled (Unicode only) | Dark/Light · Zawgyi ပိတ်ထား (Unicode ပဲ) |

## 1.4 Architecture / ဖွဲ့စည်းပုံ

```
 UI       Compose Screen  ←  ViewModel (one StateFlow<UiState>)     ui/<feature>/
   │
 Domain   data classes + pure functions (no Android)                domain/
   │
 Data     Repository interfaces → Firestore (content) · Room (personal)   data/
 Player   PlayerController interface → Media3 (Service + MediaController) player/
```

**EN**
- **Three layers, MVVM.** A screen only talks to its ViewModel; a ViewModel only talks to interfaces (`PodcastRepository`, `NewsRepository`, `LibraryRepository`, `PlayerController`). No ViewModel imports `android.*` or Media3, so every ViewModel runs as a plain JVM unit test.
- **No DI framework** (spec ruled out Hilt/Koin). Two app-wide singletons live in `MyanCastApp`: `playerController` and `libraryRepository`. Each ViewModel has a small `factory()`.
- **Firestore is read-only.** Three collections: `podcasts`, `episodes`, `news`. A helper `snapshotFlow()` turns a live Firestore listener into a Kotlin `Flow` and copies the document ID into the model (Firestore doesn't do that by itself).
- **Room** keeps subscriptions and history in one `library` table.
- **Player**: `PlaybackService` (a `MediaSessionService` that owns ExoPlayer — gives notification + lock screen for free) and `Media3PlayerController` (maps player events into one `PlaybackState`, ticks position every 500 ms). `HistoryRecorder` saves progress to Room every 10 seconds, on pause and on episode change — this powers "Continue listening".
- **Pure logic is pulled out** into top-level functions (`CategoryFilter`, `NewsFilter`, `SearchFilter`, `PlayerQueue`, duration/date/Myanmar-digit formatters) so they're tested without coroutines or Android.

**MM**
- **လွှာ ၃ ခု + MVVM** — Screen က ViewModel နဲ့ပဲ ပြောတယ်။ ViewModel က interface တွေ (`PodcastRepository`, `NewsRepository`, `LibraryRepository`, `PlayerController`) နဲ့ပဲ ပြောတယ်။ ViewModel ထဲမှာ `android.*` / Media3 မရှိလို့ ဖုန်းမလိုဘဲ JVM ပေါ်မှာ test လုပ်လို့ရတယ်။
- **DI framework မသုံး** (spec က Hilt/Koin ပိတ်ထား) — `MyanCastApp` ထဲမှာ singleton ၂ ခုပဲ: `playerController`, `libraryRepository`။
- **Firestore ကို ဖတ်ရုံပဲ** — collection ၃ ခု (`podcasts`, `episodes`, `news`)။ `snapshotFlow()` helper က Firestore listener ကို `Flow` ပြောင်းပေးပြီး document ID ကိုပါ model ထဲ ထည့်ပေးတယ်။
- **Room** — subscribe နဲ့ မှတ်တမ်းကို `library` table တစ်ခုတည်းမှာ သိမ်း။
- **Player** — `PlaybackService` (ExoPlayer ကို ပိုင်တဲ့ service — notification + lock screen ပါပြီးသား) နဲ့ `Media3PlayerController` (player event တွေကို `PlaybackState` တစ်ခုတည်း ပြောင်း၊ ၅၀၀ ms တစ်ခါ position update)။ `HistoryRecorder` က ၁၀ စက္ကန့်တစ်ခါ၊ pause လုပ်ရင်၊ ပိုင်း ပြောင်းရင် Room ထဲ သိမ်းတယ် — "ဆက်နားထောင်ရန်" ဒီကနေ လာတာ။
- **Pure logic ကို ခွဲထုတ်ထား** — filter, format, queue တွေကို top-level function အဖြစ် ထားလို့ test လွယ်တယ်။

## 1.5 Tech stack / နည်းပညာ

| Area | Library (version) |
|---|---|
| Language / build | Kotlin 2.0.21 · AGP 8.7.3 · KSP · Gradle version catalog |
| UI | Jetpack Compose (BOM 2024.12.01) · Material 3 · Navigation Compose 2.8.5 · Coil 2.7 (images) |
| State | ViewModel + `StateFlow` · Coroutines 1.9 · `collectAsStateWithLifecycle` |
| Backend | Firebase Firestore (BOM 33.7.0) |
| Local DB | Room 2.6.1 |
| Audio | Media3 1.5.0 (ExoPlayer + MediaSession) |
| Testing | JUnit 4 · MockK · kotlinx-coroutines-test · Robolectric (Room in-memory) |
| Fonts | Padauk (Myanmar) · Poppins (Latin labels only) |

## 1.6 How the team worked / အဖွဲ့ အလုပ်လုပ်ပုံ

**EN**
- **Phase 1** (week 1): setup, theme, navigation, Firestore data.
- **Phase 2** (week 2): Home + Detail. We **switched from a by-layer split** (one person UI, one data, one player) **to vertical slices** — each person builds one feature end-to-end (data → model → UI). Fewer hand-offs, fewer merge conflicts, everyone understands the whole stack.
- **Phase 3** (week 3): Player. We started with a **"Day 0 contract"**: we agreed the `PlaybackState` data class and `PlayerController` interface first, plus a `FakePlayerController`. Then A (mini player), B (Media3 engine) and C (full player) worked **in parallel** — the UI people didn't wait for the engine. The contract didn't change all week.
- **Phase 4** (week 4): News, Library + Subscribe, Search + Continue listening — same contract pattern; the contract grew only additively (new fields with defaults).
- **Phase 5** (week 5): no new features. Debt triage (FIX / DROP / KEEP), bug fixes, tests 109 → 128, README, app icon, release build.

**MM**
- **Phase 1** — setup, theme, navigation, Firestore data။
- **Phase 2** — Home + Detail။ **လွှာအလိုက် ခွဲတာ**ကနေ **vertical slice** ကို ပြောင်းလိုက်တယ် — တစ်ယောက် feature တစ်ခုကို data → model → UI အစအဆုံး လုပ်။ လက်လွှဲ နည်း၊ conflict နည်း၊ တစ်ယောက်ချင်း stack တစ်ခုလုံး နားလည်။
- **Phase 3** — Player။ **Day 0 contract** — `PlaybackState` နဲ့ `PlayerController` interface + fake ကို အရင် သဘောတူပြီးမှ A (mini player)၊ B (Media3)၊ C (full player) **ပြိုင်တူ** လုပ်။ UI လုပ်တဲ့သူ engine ကို စောင့်စရာ မလို။ တစ်ပတ်လုံး contract မပြောင်းရ။
- **Phase 4** — News, Library, Search + ဆက်နားထောင်ရန် — contract ပုံစံပဲ၊ field အသစ်ကို default value နဲ့ ထပ်ထည့်ရုံ။
- **Phase 5** — feature အသစ် မထည့်။ FIX / DROP / KEEP ခွဲ၊ bug ပြင်၊ test ၁၀၉ → ၁၂၈၊ README၊ icon၊ release build။

## 1.7 Strengths / အားသာချက်

| EN | MM |
|---|---|
| Clean layering — UI never touches Firebase directly | Layer သန့် — UI က Firebase ကို တိုက်ရိုက် မထိ |
| Everything behind interfaces → fakes → fast, reliable tests | Interface + fake → test မြန်၊ ယုံကြည်ရ |
| Real background audio (notification, lock screen, speed) — not just a toy player | Background playback အစစ် — notification, lock screen, speed |
| Myanmar-first UI: Myanmar digits (၁၂၃), Myanmar dates, Padauk font, Burmese strings | မြန်မာ ဦးစားပေး — မြန်မာ ဂဏန်း၊ ရက်စွဲ၊ Padauk font |
| Consistent Loading / Error / Empty states on every screen | Screen တိုင်း Loading / Error / Empty ပါ |
| Decisions are written down (phase docs, README known limitations) | ဆုံးဖြတ်ချက်တွေ စာနဲ့ ရေးထား |

## 1.8 Weaknesses & risks (be honest) / အားနည်းချက်

| EN | MM |
|---|---|
| Zawgyi is not supported — Unicode only (needs a text converter, ~1 day) | Zawgyi မရ — Unicode ပဲ (converter လို ~၁ ရက်) |
| Dark/Light choice resets when the app restarts (no DataStore by spec) | Theme ကို app ပိတ်ရင် ပြန်ပျောက် |
| If Android kills the app, the mini player is empty (queue is in memory) — "Continue listening" still works | System က app သတ်ရင် mini player အလွတ် — "ဆက်နားထောင်ရန်" ကနေ ပြန်ဖွင့်လို့ရ |
| Content is entered by hand; no admin tool, no offline download | Data ကို လက်နဲ့ ထည့်ရ · download မရ |
| No Compose UI tests yet; device tests are manual | Compose UI test မရှိသေး · device test က လက်နဲ့ |
| Minify (R8) off in release — bigger APK, chosen to avoid stripping Firestore models | Minify ပိတ်ထား — Firestore model မပျက်အောင် |

---

# Part 2 — Slides + Presenter Scripts

> Each slide: **On slide** = what to put on the slide (keep it short — the script carries the detail).
> **Script EN / Script MM** = what the presenter says (~45–60 seconds). Say it in whichever language the audience prefers; the two versions say the same thing.

---

## Slide 1 — Title · Presenter A

**On slide**
- **MyanCast Personal**
- မြန်မာလို podcast နဲ့ သတင်း app
- A Myanmar-language podcast & news app for Android
- Team: [A name] · [B name] · [C name] — 5 weeks — [date]
- App icon (`docs/app-icon-512.png`)

**Script EN**
> Good [morning/afternoon]. We are [names], and this is **MyanCast Personal** — an Android app for listening to Myanmar podcasts and news. We built it as a team of three in five weeks, using Kotlin, Jetpack Compose, Firebase and Media3. In the next twelve minutes we'll show you what the app does, how we built it, how we split the work, and what we learned — and then we'll do a live demo.
>
> Quick note on the icon: it's the Burmese letter "မ" — the first letter of Myanmar and MyanCast — with a play button inside.

**Script MM**
> မင်္ဂလာပါ။ ကျွန်တော်/ကျွန်မတို့က [နာမည်များ] ဖြစ်ပြီး ဒီနေ့ **MyanCast Personal** ကို တင်ပြပေးမှာပါ။ မြန်မာလို podcast နဲ့ သတင်းတွေကို နားထောင်လို့ရတဲ့ Android app ပါ။ အဖွဲ့ဝင် ၃ ယောက်၊ ၅ ပတ်အတွင်း Kotlin၊ Jetpack Compose၊ Firebase နဲ့ Media3 ကို သုံးပြီး ရေးခဲ့ပါတယ်။ App က ဘာလုပ်လဲ၊ ဘယ်လို တည်ဆောက်လဲ၊ အလုပ်ကို ဘယ်လို ခွဲလဲ၊ ဘာတွေ သင်ခဲ့ရလဲ ပြောပြပြီး နောက်ဆုံးမှာ live demo ပြပါမယ်။
>
> App icon က မြန်မာ အက္ခရာ "မ" ပါ — Myanmar နဲ့ MyanCast ရဲ့ ပထမ စာလုံး — အထဲမှာ ▶ play ခလုတ် ထည့်ထားပါတယ်။

---

## Slide 2 — The Problem & Our Goal · Presenter A

**On slide**
| Problem / ပြဿနာ | Goal / ရည်ရွယ်ချက် |
|---|---|
| Most podcast apps are built for English — Burmese text and audio come second | One simple app for Myanmar podcasts **and** news |
| Listening is spread across many apps and platforms | Listen anywhere — background, lock screen, resume later |
| | Burmese everywhere: text, numbers (၁၂၃), dates |

**Script EN**
> Most podcast apps are designed for English first. Burmese text, Burmese numbers and Burmese dates are an afterthought, and Myanmar audio content is scattered across different apps.
>
> Our goal was a simple, clean app where a Myanmar listener can browse podcasts, read or listen to news, keep listening when the screen is off, and pick up exactly where they stopped. And everything the user reads — even the numbers and dates — is in Burmese.

**Script MM**
> Podcast app အများစုက English အတွက် အဓိက ဒီဇိုင်းထားတာပါ။ မြန်မာစာ၊ မြန်မာ ဂဏန်း၊ မြန်မာ ရက်စွဲ ဆိုတာ နောက်မှ စဉ်းစားတာ များပြီး မြန်မာ audio content တွေကလည်း app အမျိုးမျိုးမှာ ပြန့်ကျဲနေတယ်။
>
> ကျွန်တော်တို့ ရည်ရွယ်ချက်က ရိုးရှင်းပြီး သန့်ရှင်းတဲ့ app တစ်ခု — podcast ရှာ၊ သတင်း ဖတ်/နားထောင်၊ screen ပိတ်ထားလည်း ဆက်နားထောင်၊ ရပ်ထားတဲ့ နေရာကနေ ပြန်ဆက်လို့ရတဲ့ app။ အသုံးပြုသူ မြင်ရတဲ့ စာ အားလုံး — ဂဏန်းနဲ့ ရက်စွဲ အပါအဝင် — မြန်မာလို ဖြစ်ရမယ်။

---

## Slide 3 — Features · Presenter A

**On slide** — 5 screenshots (Home · Full Player · News · Library · Search) + short labels:
- **15 MVP features → 14 built + 1 documented decision**
- Home: podcasts + category filter + Continue listening
- Player: background play · −15 s / +30 s · speed 0.5–2x · mini + full player
- News: featured + detail (with audio)
- Library: subscriptions + history (progress bar) · Search · Dark/Light

**Script EN**
> Our spec had fifteen MVP features. Fourteen are built, and the fifteenth — the Zawgyi font toggle — we made a deliberate decision about, which I'll explain later.
>
> There are five tabs: Home, News, Search, Library and Settings. On Home you browse podcasts by category, and there's a "Continue listening" card. The player keeps playing in the background with notification and lock-screen controls, has skip back fifteen and forward thirty seconds, and speed from half to double. A mini player sits at the bottom of every screen. News can have audio, which plays through the same player. Library keeps your subscriptions and your history with progress bars.

**Script MM**
> Spec မှာ MVP feature ၁၅ ခု ပါပါတယ်။ ၁၄ ခု ပြီးပါပြီ၊ ၁၅ ခုမြောက် Zawgyi toggle ကိုတော့ ရည်ရွယ်ချက်ရှိရှိ ဆုံးဖြတ်ချက် ချထားတာပါ — နောက်မှ ရှင်းပြပါမယ်။
>
> Tab ၅ ခု ရှိတယ် — ပင်မ၊ သတင်း၊ ရှာဖွေ၊ စာကြည့်တိုက်၊ ဆက်တင်။ ပင်မ မှာ အမျိုးအစားအလိုက် podcast ရှာလို့ရပြီး "ဆက်နားထောင်ရန်" card ပါတယ်။ Player က app ပိတ်ထားလည်း notification နဲ့ lock screen ကနေ ထိန်းလို့ရတယ်၊ ၁၅ စက္ကန့် နောက်ဆုတ်၊ ၃၀ စက္ကန့် ရှေ့ကျော်၊ speed ၀.၅ ကနေ ၂ ဆ အထိ။ Mini player က screen တိုင်း အောက်ခြေမှာ ရှိတယ်။ သတင်းမှာ audio ပါရင် player တစ်ခုတည်းနဲ့ပဲ ဖွင့်တယ်။ စာကြည့်တိုက် မှာ subscribe လုပ်ထားတာနဲ့ နားထောင်ခဲ့တဲ့ မှတ်တမ်းကို progress bar နဲ့ ပြတယ်။

---

## Slide 4 — Architecture · Presenter A

**On slide** — the layer diagram from 1.4, plus three bullets:
- MVVM · UI → Domain → Data
- ViewModels only see **interfaces** → testable on the JVM
- No DI framework — 2 singletons in `MyanCastApp`

**Script EN**
> The app has three layers. At the top, Compose screens. Each screen has a ViewModel that exposes one state object — loading, error, or data. In the middle, the domain layer: plain data classes and pure functions, with no Android code. At the bottom, the data layer: repositories that read content from Firestore and save personal data to Room. The player sits beside the data layer.
>
> The key rule: a ViewModel only knows **interfaces** — never Firebase, never Media3. That's what lets us test every ViewModel without a phone. We didn't use a DI framework — the spec ruled it out, and honestly we only needed two app-wide singletons: the player controller and the library repository.

**Script MM**
> App မှာ လွှာ ၃ ခု ရှိပါတယ်။ အပေါ်ဆုံးမှာ Compose screen တွေ — screen တစ်ခုစီမှာ ViewModel တစ်ခု ရှိပြီး state object တစ်ခုတည်း — loading, error, data — ကို ထုတ်ပေးတယ်။ အလယ်မှာ domain layer — data class နဲ့ pure function တွေ၊ Android code မပါ။ အောက်ဆုံးမှာ data layer — Firestore ကနေ content ဖတ်ပြီး ကိုယ်ပိုင် data ကို Room မှာ သိမ်းတဲ့ repository တွေ။ Player က data layer ဘေးမှာ ရှိတယ်။
>
> အဓိက စည်းကမ်းက ViewModel က **interface** ကိုပဲ သိတယ် — Firebase ကိုလည်း မသိ၊ Media3 ကိုလည်း မသိ။ ဒါကြောင့် ViewModel တိုင်းကို ဖုန်းမလိုဘဲ test လုပ်လို့ရတာပါ။ DI framework မသုံးပါဘူး — spec က ပိတ်ထားသလို တကယ်လည်း singleton ၂ ခုပဲ လိုတယ် — player controller နဲ့ library repository။

---

## Slide 5 — Tech Stack & Data · Presenter B

**On slide**
- Kotlin 2.0 · Jetpack Compose + Material 3 · Navigation Compose
- **Firestore** (read-only): `podcasts` · `episodes` · `news`
- **Room**: subscriptions + history (one `library` table)
- **Media3**: ExoPlayer + MediaSession → background, notification, lock screen
- Coil (images) · Coroutines + StateFlow · Padauk font

**Script EN**
> Here's the stack. Kotlin and Jetpack Compose with Material 3 for the UI. Firestore holds our content in three collections — podcasts, episodes and news. The app only reads; we enter data in the Firebase Console. We wrapped Firestore's live listener in a Kotlin Flow, so when data changes in the Console, the app updates by itself, and the listener stops when the screen closes.
>
> Room stores the user's own data — subscriptions and history — on the phone. And for audio we used Media3: ExoPlayer runs inside a MediaSessionService, and that one choice gives us background playback, the notification and lock-screen controls.

**Script MM**
> ဒါက နည်းပညာ stack ပါ။ UI အတွက် Kotlin, Jetpack Compose နဲ့ Material 3။ Content တွေကို Firestore မှာ collection ၃ ခု — podcasts, episodes, news — နဲ့ သိမ်းတယ်။ App က ဖတ်ရုံပဲ၊ data ကို Firebase Console ကနေ ထည့်တယ်။ Firestore ရဲ့ live listener ကို Kotlin Flow အဖြစ် ပတ်ထားလို့ Console မှာ data ပြောင်းတာနဲ့ app က အလိုလို update ဖြစ်တယ်၊ screen ပိတ်ရင် listener လည်း ရပ်တယ်။
>
> Room က အသုံးပြုသူရဲ့ ကိုယ်ပိုင် data — subscribe နဲ့ မှတ်တမ်း — ကို ဖုန်းထဲမှာ သိမ်းတယ်။ အသံအတွက် Media3 — ExoPlayer ကို MediaSessionService ထဲမှာ run တယ်၊ ဒီ ရွေးချယ်မှု တစ်ခုတည်းက background playback, notification, lock screen ထိန်းချုပ်မှု အားလုံးကို ပေးတယ်။

---

## Slide 6 — ⭐ The Day 0 Contract · Presenter C

**On slide** (make this the highlight slide)
```kotlin
interface PlayerController {
    val state: StateFlow<PlaybackState>
    fun play(queue, podcastTitle, startPositionMs = 0L)
    fun togglePlayPause(); fun seekTo(ms); fun skipBack(); fun skipForward()
    fun next(); fun previous(); fun setSpeed(speed)
}
```
- Agree the interface + a **fake** on Day 0 → 3 people build in parallel
- Mini player (A) · Media3 engine (B) · Full player (C)
- Contract unchanged for the whole week

**Script EN**
> This is the decision we're proudest of. The player was the hardest part, and three people needed to work on it at once. If the UI people wait for the engine, two of us sit idle.
>
> So on Day 0 of week three, we spent an hour agreeing on one interface — PlayerController — and one data class — PlaybackState. We also wrote a FakePlayerController. Then A built the mini player and I built the full player against the fake, while B built the real Media3 engine behind the same interface. When B finished, we swapped the fake for the real one and it just worked. The contract didn't change all week, and in week four we reused the same pattern for News, Library and Search.

**Script MM**
> ဒါက ကျွန်တော်တို့ အဂုဏ်ယူရဆုံး ဆုံးဖြတ်ချက်ပါ။ Player က အခက်ဆုံး အပိုင်းဖြစ်ပြီး ၃ ယောက်လုံး တစ်ပြိုင်နက် လုပ်ဖို့ လိုတယ်။ UI လုပ်တဲ့သူတွေက engine ကို စောင့်နေရရင် ၂ ယောက် ထိုင်နေရမယ်။
>
> ဒါကြောင့် တတိယပတ် Day 0 မှာ တစ်နာရီလောက် ထိုင်ပြီး interface တစ်ခု — PlayerController — နဲ့ data class တစ်ခု — PlaybackState — ကို သဘောတူလိုက်တယ်။ FakePlayerController လည်း ရေးထားတယ်။ ပြီးတော့ A က mini player, ကျွန်တော်က full player ကို fake နဲ့ ရေး၊ B က အဲဒီ interface နောက်မှာ Media3 engine အစစ်ကို ရေး။ B ပြီးတော့ fake ကို အစစ်နဲ့ လဲလိုက်ရုံပဲ — တန်းအလုပ်လုပ်တယ်။ တစ်ပတ်လုံး contract မပြောင်းရဘူး၊ စတုတ္ထပတ်မှာ News, Library, Search အတွက်လည်း ဒီပုံစံပဲ ပြန်သုံးတယ်။

---

## Slide 7 — Vertical Slices · Presenter C

**On slide**
| Before (Week 1) — by layer | After (Week 2+) — by feature |
|---|---|
| A = UI · B = data · C = player | Each person: data → model → UI for one feature |
| Constant hand-offs, waiting | Own a feature end-to-end |
| Merge conflicts in shared files | Fewer conflicts, everyone knows the full stack |

Team: **A** Home · Library · Mini player — **B** Media3 engine · News — **C** Formatters · Full player · Search + History

**Script EN**
> Our original plan split the team by layer: one person on UI, one on data, one on the player. After week one we saw the problem — every feature needed all three people, so we kept waiting on each other and editing the same files.
>
> From week two we switched to vertical slices. Each person takes one feature and builds it top to bottom: the repository, the model, the ViewModel and the screen. A owned Home, Library and the mini player. B owned the Media3 engine and News. C owned the formatters, the full player, and Search with history. The result: fewer conflicts, and all three of us can explain every layer — which matters for today.

**Script MM**
> မူလ plan မှာ လွှာအလိုက် ခွဲထားတယ် — တစ်ယောက် UI, တစ်ယောက် data, တစ်ယောက် player။ ပထမပတ် ပြီးတော့ ပြဿနာ မြင်လာတယ် — feature တိုင်းမှာ ၃ ယောက်လုံး လိုလို့ တစ်ယောက်ကို တစ်ယောက် စောင့်ရတယ်၊ ဖိုင်တစ်ခုတည်းကို ပြင်မိကြတယ်။
>
> ဒုတိယပတ်ကစပြီး vertical slice ကို ပြောင်းလိုက်တယ်။ တစ်ယောက်က feature တစ်ခုကို အပေါ်ကနေ အောက်အထိ — repository, model, ViewModel, screen — အကုန် လုပ်တယ်။ A က ပင်မ, စာကြည့်တိုက်, mini player။ B က Media3 engine နဲ့ သတင်း။ C က formatter, full player, ရှာဖွေ + မှတ်တမ်း။ ရလဒ်က conflict နည်းသွားပြီး ၃ ယောက်လုံး layer တိုင်းကို ရှင်းပြနိုင်လာတယ်။

---

## Slide 8 — Testing & Quality · Presenter C

**On slide** — one big number:
- **128** unit tests · all passing (109 → 128 in week 5)
- **0** build warnings · **16.9 MB** release APK
- How: interfaces + fakes · pure functions · `StandardTestDispatcher` · Robolectric for Room
- Examples: category filter, Myanmar dates/digits, player queue, HistoryRecorder throttle

**Script EN**
> We have 128 unit tests, and they all pass. They run on the JVM in seconds — no phone needed.
>
> Two design choices made that possible. First, every repository and the player are interfaces, so in tests we plug in fakes. Second, we pulled pure logic — filtering, formatting durations and Myanmar dates, building the play queue — into plain functions that are easy to test. For example, we test that "five days ago" becomes "၅ ရက် အရင်က", and that HistoryRecorder saves at most once every ten seconds instead of every half-second. For the database we use Robolectric with an in-memory Room. The build has zero warnings, and the release APK is 16.9 megabytes.

**Script MM**
> Unit test ၁၂၈ ခု ရှိပြီး အားလုံး pass ပါတယ်။ ဖုန်းမလိုဘဲ JVM ပေါ်မှာ စက္ကန့်ပိုင်းအတွင်း run တယ်။
>
> ဒါ ဖြစ်နိုင်တာ ဒီဇိုင်း ဆုံးဖြတ်ချက် ၂ ခုကြောင့်ပါ။ ပထမ — repository နဲ့ player အားလုံးက interface ဖြစ်လို့ test မှာ fake ထည့်လို့ရတယ်။ ဒုတိယ — filter, အချိန်/မြန်မာ ရက်စွဲ format, play queue စတဲ့ pure logic တွေကို သာမန် function အဖြစ် ခွဲထုတ်ထားလို့ test လွယ်တယ်။ ဥပမာ "ငါးရက်အရင်" က "၅ ရက် အရင်က" ဖြစ်လား၊ HistoryRecorder က စက္ကန့်ဝက်တိုင်း မသိမ်းဘဲ ၁၀ စက္ကန့်မှ တစ်ခါ သိမ်းလား စစ်တယ်။ Database အတွက် Robolectric နဲ့ in-memory Room သုံးတယ်။ Build မှာ warning ၀ ခု၊ release APK က 16.9 MB ပါ။

---

## Slide 9 — Three Hard Problems · Presenter B

**On slide**
| Problem | Fix |
|---|---|
| **Background playback** — audio must survive leaving the app | `MediaSessionService` owns ExoPlayer; UI talks through a `MediaController` |
| **Resume lost its position** — `play()` then `seekTo()` lost the seek when the service wasn't connected yet | `play(queue, title, startPositionMs)` — position travels with the play request |
| **Burmese text showed as boxes** — Poppins has no Myanmar glyphs | Rule: Burmese text only in Padauk styles (`body`/`title`); Poppins `label*` for ASCII like "24:30", "1.5x" |

**Script EN**
> Three problems taught us the most.
>
> One: background playback. If the player lives in a screen, it dies when you leave the app. We moved ExoPlayer into a MediaSessionService, and the UI controls it through a MediaController. That's also what gives us the notification and lock screen.
>
> Two: our trickiest bug. "Continue listening" called play and then seekTo. But connecting to the service takes a moment — if it wasn't connected yet, the seek was silently lost and the episode started from zero. The fix was to pass the start position into play itself, so it travels with the request. We wrote that rule into the interface docs.
>
> Three: fonts. Our label font, Poppins, has no Myanmar glyphs, so Burmese text in labels showed empty boxes. We made a team rule: Burmese only in Padauk styles; Poppins only for things like "24:30" or "1.5x".

**Script MM**
> အများဆုံး သင်ခဲ့ရတဲ့ ပြဿနာ ၃ ခု ရှိပါတယ်။
>
> ပထမ — background playback။ Player ကို screen ထဲမှာ ထားရင် app ကနေ ထွက်တာနဲ့ ရပ်သွားတယ်။ ExoPlayer ကို MediaSessionService ထဲ ရွှေ့ပြီး UI က MediaController ကနေ ထိန်းတယ်။ Notification နဲ့ lock screen ကလည်း ဒီကနေ ရတာပါ။
>
> ဒုတိယ — အခက်ဆုံး bug။ "ဆက်နားထောင်ရန်" က play() ခေါ်ပြီးမှ seekTo() ခေါ်တယ်။ Service နဲ့ ချိတ်ဖို့ ခဏ ကြာတယ် — မချိတ်ရသေးရင် seek က ဘာမှ မပြောဘဲ ပျောက်သွားပြီး အစကနေ ပြန်ဖွင့်တယ်။ ပြင်တာက စမယ့် position ကို play() ထဲကိုပဲ တစ်ခါတည်း ထည့်ပေးလိုက်တယ်။ ဒီစည်းကမ်းကို interface မှာ ရေးထားတယ်။
>
> တတိယ — font။ Label font Poppins မှာ မြန်မာ glyph မရှိလို့ မြန်မာစာ လေးထောင့်ကွက်တွေ ပေါ်တယ်။ အဖွဲ့ စည်းကမ်း ချလိုက်တယ် — မြန်မာစာကို Padauk style မှာပဲ၊ Poppins ကို "24:30", "1.5x" လို စာမျိုးအတွက်ပဲ။

---

## Slide 10 — Known Limitations & Decisions · Presenter C

**On slide**
| Limitation | Why |
|---|---|
| **Zawgyi: Unicode only** (toggle disabled) | Firestore text is Unicode; swapping the font garbles it. Real support needs a text converter (~1 day) |
| Theme resets on app restart | No DataStore (spec decision); survives rotation |
| Mini player empty after Android kills the app | Queue lives in memory — "Continue listening" still resumes |
| Not built: download · sleep timer · favorites · queue screen · push | Nice-to-haves; week 5 = no new features |

**Script EN**
> We want to be honest about what's missing.
>
> The biggest one is Zawgyi. Our data in Firestore is Unicode. The original toggle only switched the font, and showing Unicode text in a Zawgyi font garbles it. Doing it properly means converting the text itself, which is about a day of work. Rather than ship something broken, we disabled the toggle and documented the decision.
>
> The theme choice resets when the app restarts because the spec didn't allow DataStore. If Android kills the app, the mini player is empty, but "Continue listening" on Home still resumes you. And the nice-to-haves — downloads, sleep timer, favorites — we deliberately didn't start in week five, because every new feature brings new bugs.

**Script MM**
> မရှိသေးတာတွေကို ပွင့်ပွင့်လင်းလင်း ပြောချင်ပါတယ်။
>
> အကြီးဆုံးက Zawgyi ပါ။ Firestore ထဲက data တွေက Unicode။ မူလ toggle က font ကိုပဲ ပြောင်းတာမို့ Unicode စာကို Zawgyi font နဲ့ ပြရင် စာလုံး ပျက်တယ်။ မှန်မှန်ကန်ကန် လုပ်ရင် စာသားကိုပါ converter နဲ့ ပြောင်းရမယ် — တစ်ရက်လောက် ကြာမယ်။ ပျက်နေတာကို ထုတ်မယ့်အစား toggle ကို ပိတ်ပြီး ဆုံးဖြတ်ချက်ကို စာနဲ့ ရေးထားတယ်။
>
> Theme ကို app ပိတ်ရင် ပြန်ပျောက်တယ် — spec က DataStore ခွင့်မပြုလို့။ System က app ကို သတ်ရင် mini player အလွတ်ဖြစ်တယ်၊ ဒါပေမဲ့ ပင်မ ရဲ့ "ဆက်နားထောင်ရန်" ကနေ ပြန်ဆက်လို့ရတယ်။ Download, sleep timer, favorites စတဲ့ nice-to-have တွေကို ပဉ္စမပတ်မှာ ရည်ရွယ်ချက်ရှိရှိ မစခဲ့ဘူး — feature အသစ်တိုင်း bug အသစ် ခေါ်လာလို့ပါ။

---

## Slide 11 — Live Demo · Presenter A (B drives the phone if easier)

**On slide** — "Live demo / တိုက်ရိုက် ပြသမှု" + the route as a small numbered list (see Part 3).

**Script EN**
> Now let's see it on a real phone. [Follow the 10-step demo route in Part 3, narrating each step in one sentence.]
> *Closing line after step 10:* "And that's the part we like best — it remembered exactly where I stopped, even after closing the app."

**Script MM**
> အခု ဖုန်းအစစ်ပေါ်မှာ ပြပါမယ်။ [Part 3 ရဲ့ demo လမ်းကြောင်း ၁၀ ဆင့်ကို တစ်ဆင့် တစ်ကြောင်း ပြောရင်း ပြပါ။]
> *အဆုံးသတ်:* "ဒါက ကျွန်တော်တို့ အကြိုက်ဆုံး အပိုင်းပါ — app ပိတ်ပြီးတောင် ရပ်ခဲ့တဲ့ နေရာကို အတိအကျ မှတ်ထားတယ်။"

---

## Slide 12 — What We Learned · Presenter B

**On slide** — three best decisions:
1. **Vertical slices** (week 2) — own a feature end-to-end
2. **Day 0 contract + fake** (week 3) — build in parallel
3. **Pure functions** (all along) — easy to test

Plus: *"Week 5: the best decision was adding no new features."*

**Script EN**
> Looking back over five weeks, three decisions made the biggest difference. Vertical slices, so each person owned a feature from data to screen. The Day 0 contract with a fake, so three people could build the player at the same time. And pulling logic into pure functions, which is why we have 128 fast tests.
>
> And one lesson about discipline: in week five, the most important decision was **not** adding features. We spent that week fixing bugs — for example, Light mode was unreadable in 38 places across 16 files, and a Room schema change would have crashed phones that had the old version. Finding those mattered more than a sleep timer.

**Script MM**
> ၅ ပတ်လုံး ပြန်ကြည့်ရင် အကျိုးအများဆုံး ဆုံးဖြတ်ချက် ၃ ခု ရှိပါတယ်။ Vertical slice — တစ်ယောက်က feature တစ်ခုကို data ကနေ screen အထိ ပိုင်တယ်။ Day 0 contract + fake — ၃ ယောက်လုံး player ကို တစ်ပြိုင်နက် ရေးနိုင်တယ်။ Pure function ခွဲထုတ်တာ — ဒါကြောင့် မြန်တဲ့ test ၁၂၈ ခု ရှိတာပါ။
>
> စည်းကမ်းနဲ့ ပတ်သက်တဲ့ သင်ခန်းစာ တစ်ခုလည်း ရှိတယ် — ပဉ္စမပတ်မှာ အရေးကြီးဆုံး ဆုံးဖြတ်ချက်က feature အသစ် **မထည့်တာ**ပါ။ အဲဒီ တစ်ပတ်လုံး bug ပြင်တယ် — ဥပမာ Light mode မှာ ဖိုင် ၁၆ ခု၊ နေရာ ၃၈ ခုမှာ စာ ဖတ်မရဘူး၊ Room schema ပြောင်းတာကြောင့် version အဟောင်း ရှိတဲ့ ဖုန်းတွေ crash ဖြစ်မှာ။ ဒါတွေ ရှာတွေ့တာက sleep timer ထက် ပိုအရေးကြီးတယ်။

---

## Slide 13 — Thank You · Q&A · All

**On slide**
- ကျေးဇူးတင်ပါတယ် · Thank you
- Questions? / မေးခွန်းများ
- Repo: [link]

**Script EN**
> Thank you for listening. We're happy to take questions — about the code, the architecture, or how we worked as a team.

**Script MM**
> နားထောင်ပေးတဲ့အတွက် ကျေးဇူးတင်ပါတယ်။ Code, architecture, ဒါမှမဟုတ် အဖွဲ့ အလုပ်လုပ်ပုံနဲ့ ပတ်သက်ပြီး မေးခွန်း ရှိရင် မေးနိုင်ပါတယ်။

---

# Part 3 — Demo, Q&A, Rehearsal

## 3.1 Demo route (~5 min) / Demo လမ်းကြောင်း

| # | Do | Say (EN) | ပြောရန် (MM) |
|---|---|---|---|
| 1 | **Home** — tap a category chip | "Podcasts come live from Firestore; chips filter by category." | "Podcast တွေ Firestore ကနေ တိုက်ရိုက် လာတယ်၊ chip နဲ့ filter လုပ်တယ်။" |
| 2 | **Detail** — tap "သိမ်း" | "Subscribing saves to Room on the phone." | "သိမ်းရင် ဖုန်းထဲ Room မှာ သိမ်းတယ်။" |
| 3 | Tap an episode → mini player → open Full Player | "One tap plays; the mini player follows us everywhere." | "နှိပ်တာနဲ့ ဖွင့်တယ်၊ mini player က နေရာတိုင်း လိုက်လာတယ်။" |
| 4 | **Full Player** — drag seek, +30 s, speed 1.5x | "Seek, skip, and change speed — you can hear it." | "Seek, ကျော်, speed ပြောင်း — အသံ ပြောင်းတာ ကြားရမယ်။" |
| 5 | Back to Home → press Home button → control from notification | "Audio keeps playing outside the app." | "App အပြင်မှာလည်း ဆက်ဖွင့်တယ်။" |
| 6 | **News** — open a story with audio → play | "News audio uses the same player." | "သတင်း audio လည်း player တစ်ခုတည်း။" |
| 7 | **Search** — type "နည်း" | "Search matches title and category." | "Title နဲ့ အမျိုးအစား နဲ့ ရှာတယ်။" |
| 8 | **Library** — subscriptions + history tab | "History shows how far you listened." | "မှတ်တမ်းမှာ ဘယ်အထိ နားထောင်ပြီးလဲ ပြတယ်။" |
| 9 | **Settings** — Dark → Light | "Every screen works in both themes." | "Screen တိုင်း theme ၂ မျိုးလုံး ဖတ်လို့ရတယ်။" |
| 10 | ⭐ Home → tap **ဆက်နားထောင်ရန်** card | "It resumes exactly where we stopped." | "ရပ်ခဲ့တဲ့ နေရာကနေ အတိအကျ ဆက်ဖွင့်တယ်။" |

**Before the demo / Demo မတိုင်မီ**
- Check Wi-Fi/data · open the app once beforehand (Firestore cache warms up) · volume ~50% · Bluetooth off
- Wi-Fi စစ် · app ကို တစ်ခါ ကြိုဖွင့်ထား · volume ½ · Bluetooth ပိတ်
- **Backup:** if the network fails, say: *"Firestore is a live cloud database — without internet you see our error state with a retry button, which is exactly what we designed for."* Then show the error screen and, if possible, screenshots.
- **အရန်:** အင်တာနက် ပျက်ရင် — *"Firestore က cloud database မို့ အင်တာနက် မရှိရင် ပြန်ကြိုးစားမည် ခလုတ်ပါတဲ့ error screen ပြတယ် — ဒါကိုလည်း ကြိုတင် ဒီဇိုင်းထားတာပါ"* လို့ ပြောပြီး screenshot ပြ။

## 3.2 Likely questions / မေးနိုင်တဲ့ မေးခွန်းများ

| Question | Answer (EN) | အဖြေ (MM) |
|---|---|---|
| Why this architecture? | So each layer can change and be tested alone; ViewModels never see Firebase or Media3. | Layer တစ်ခုချင်း သီးခြား ပြောင်း/test လုပ်လို့ရအောင်။ |
| Why no Hilt/Koin? | Spec ruled it out; we only need 2 singletons, so manual factories are simpler. | Spec က ပိတ်ထား၊ singleton ၂ ခုပဲ လိုလို့ factory နဲ့ လုံလောက်တယ်။ |
| How does background playback work? | ExoPlayer lives in a `MediaSessionService` (foreground service); the app talks to it via `MediaController`. | ExoPlayer က MediaSessionService ထဲမှာ၊ app က MediaController နဲ့ ထိန်းတယ်။ |
| How do you test? | Interfaces + fakes, pure functions, `StandardTestDispatcher`, Robolectric for Room — 128 tests. | Interface + fake · pure function · test ၁၂၈ ခု။ |
| How did 3 people split work? | Vertical slices from week 2; Day 0 contract for shared parts. | ဒုတိယပတ်က vertical slice · မျှသုံးတဲ့ အပိုင်းအတွက် Day 0 contract။ |
| Hardest bug? | `play()` then `seekTo()` — seek lost before the service connected. Fixed with `startPositionMs`. | Service မချိတ်ခင် seek ပျောက်တာ — `startPositionMs` နဲ့ ပြင်။ |
| Why no Zawgyi? | Data is Unicode; font swap garbles text; proper fix needs a converter (~1 day). | Data က Unicode၊ converter လို (~၁ ရက်)။ |
| How is progress saved? | `HistoryRecorder` saves to Room every 10 s, on pause, and on episode change — not every 500 ms tick. | ၁၀ စက္ကန့်တစ်ခါ၊ pause လုပ်ရင်၊ ပိုင်းပြောင်းရင် သိမ်း။ |
| Why is minify off? | R8 can strip Firestore model constructors → empty data. Keep rules are ready in `proguard-rules.pro`. | R8 က Firestore model ကို ဖြတ်နိုင်လို့ — keep rule တွေ ရေးထားပြီး။ |
| What next? | Zawgyi converter, downloads, sleep timer, queue screen, push notifications, Compose UI tests. | Zawgyi converter, download, sleep timer, queue, push, UI test။ |

## 3.3 Rehearsal checklist / လေ့ကျင့်ရန်

- [ ] Fill in `[names]`, `[date]`, `[repo link]`
- [ ] Take 5 screenshots → `docs/screenshots/` (Home · Full Player · News · Library · Search) and put them on slide 3
- [ ] Fix Firestore `duration` data for the demo podcast (at least 4 episodes) so times show correctly
- [ ] Rehearse once with a timer (target ≤ 12 min slides + 5 min demo)
- [ ] Rehearse once **with internet off** — practise the backup line
- [ ] Each person can answer at least 3 of the questions in 3.2
