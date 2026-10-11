<p align="center">
  <img src="docs/app-icon-512.png" width="120" alt="MyanCast app icon" />
</p>

<h1 align="center">MyanCast Personal</h1>

<p align="center">
  မြန်မာလို podcast + သတင်း app — Kotlin · Jetpack Compose · Firebase Firestore · Media3 · Room
</p>

> App icon — မြန်မာ အက္ခရာ **"မ"** (Myanmar / MyanCast ရဲ့ ပထမ စာလုံး) ကို ရွှေရောင် stroke တစ်ချက်တည်းနဲ့ ရေးပြီး
> ခွက်ထဲမှာ **▶** (နားထောင်ရန်) ထည့်ထားတယ်။ Adaptive icon + Android 13 themed (monochrome) icon ပါတယ်။

---

## Screenshot

> `docs/screenshots/` — demo ဖုန်းမှာ ရိုက်ပြီး ထည့်ရန် (Home · Full Player · Library · News · Search)

---

## Feature

| # | Feature | Screen |
|---|---|---|
| 1 | Podcast စာရင်း + အမျိုးအစား filter | Home |
| 2 | ဆက်နားထောင်ရန် — ရပ်ထားတဲ့ နေရာကနေ ဆက်ဖွင့် | Home |
| 3 | Podcast အသေးစိတ် + episode စာရင်း + "▶ အားလုံး ဖွင့်" | Detail |
| 4 | Play / Pause — background playback (notification + lock screen) | Player |
| 5 | ၁၅ စက္ကန့် နောက် / ၃၀ စက္ကန့် ရှေ့ · seek bar | Player |
| 6 | Mini player — screen တိုင်းရဲ့ အောက်ခြေ | All |
| 7 | Full player — cover · title · seek · controls · ⏮ ⏭ | Player |
| 8 | သတင်း စာရင်း (featured + category filter) | News |
| 9 | သတင်း အသေးစိတ် + audio ရှိရင် နားထောင်လို့ရ | News |
| 10 | Subscribe / Unsubscribe (Room) | Detail |
| 11 | Library — သိမ်းထားသော + မှတ်တမ်း (progress bar) | Library |
| 12 | ရှာဖွေ — title / category | Search |
| 13 | Zawgyi / Unicode — **Unicode ပဲ** (Known limitation ကြည့်) | Settings |
| 14 | Dark / Light theme | Settings |
| 15 | Playback speed 0.5x – 2x | Player |

---

## Architecture

```
UI      (Compose Screen ← ViewModel, StateFlow)        ui/<feature>/
  │
Domain  (model + pure util — Android မသုံး)             domain/
  │
Data    (Repository → Firestore · Room)                  data/
Player  (Media3 — PlaybackService + MediaController)    player/
```

- **DI framework မသုံး** — `MyanCastApp` မှာ app-wide singleton ၂ ခု: `playerController`, `libraryRepository`
- Repository နဲ့ `PlayerController` က **interface** — test မှာ fake ထည့် (`FakePodcastRepository`, `FakeNewsRepository`, `FakeLibraryRepository`, `FakePlayerController`)
- ViewModel က `android.*` / Media3 / Room type ကို မသိဘူး — JVM ပေါ်မှာ unit test လို့ရ
- Firestore က **read-only** (data ကို Firebase Console ကနေ ထည့်) · Room မှာ subscribe + history
- Pure logic (filter, format, mapping) တွေက top-level function — `CategoryFilter`, `NewsFilter`, `SearchFilter`, `PlayerQueue`, `domain/util/`
- Team ပုံစံ — Phase 2 ကနေ **vertical slice** · Phase 3/4 မှာ **Day 0 contract** (interface + fake) ချပြီး ၃ ယောက် ပြိုင်တူ

---

## Build လုပ်ရန်

1. Firebase project ဖန်တီး → `app/google-services.json` ထည့်
2. Firestore collection ၃ ခု ဖန်တီး:

   | Collection | Field |
   |---|---|
   | `podcasts` | `title`, `description`, `coverUrl`, `category`, `episodeCount`, `updatedAt` |
   | `episodes` | `podcastId` (podcast document ID), `title`, `description`, `audioUrl`, `duration` (**စက္ကန့်**), `publishedAt`, `coverUrl` (optional) |
   | `news` | `headline`, `body`, `imageUrl`, `audioUrl` (optional), `category`, `publishedAt` |

3. Composite index — `episodes`: `podcastId` ↑ + `publishedAt` ↓
   (မထည့်ရသေးရင် Logcat ရဲ့ `FAILED_PRECONDITION` error မှာ ဖန်တီးရမယ့် link ပါတယ်)
4. Firestore rules — `allow read: if true;` (write မလို)
5. Build + install:

   ```bash
   ./gradlew installDebug        # Windows PowerShell: .\gradlew installDebug
   ./gradlew assembleRelease     # unsigned release APK (minify ပိတ်ထား — proguard-rules.pro ကြည့်)
   ```

**လိုအပ်ချက်** — Android Studio (AGP 8.7) · JDK 17 · minSdk 24 · targetSdk 35

---

## Test

```bash
./gradlew testDebugUnitTest
```

JVM unit test **၁၂၈ ခု** — ViewModel (fake repository + `StandardTestDispatcher`) · pure function · Room (Robolectric + in-memory DB) · `HistoryRecorder` throttle

---

## Known limitation

| ကိစ္စ | အကြောင်းရင်း |
|---|---|
| **Zawgyi toggle ပိတ်ထား** — Unicode ပဲ | Firestore data က Unicode · font ပြောင်းရုံနဲ့ စာလုံး ပျက်တယ် · စာသားကိုပါ ပြောင်းဖို့ Rabbit converter လိုတယ် (~၁ ရက်) — `EncodingUtil.kt` |
| Dark / Light setting က app ပိတ်ရင် ပျောက် | DataStore မသုံးဖို့ ဆုံးဖြတ်ထား (screen လှည့်တာတော့ ကျန်တယ်) |
| App ကို system က သတ်ပြီးရင် mini player အလွတ် | Queue က memory ထဲမှာပဲ — Home ရဲ့ "ဆက်နားထောင်ရန်" ကနေ ပြန်ဖွင့်လို့ရ |
| Recents ကနေ swipe ဖယ်ရင် ရပ်ထားတာ ဆို service ရပ် | ဖွင့်နေတုန်း ဆိုရင်တော့ ဆက်ဖွင့်တယ် (Phase 3 ဆုံးဖြတ်ချက်) |
| Download · sleep timer · favorites · queue screen · push notification | `fullscope.md` ရဲ့ nice-to-have — MVP အပြင် |
| Release build မှာ minify ပိတ်ထား | R8 က Firestore data class ကို ဖြတ်မိရင် data အလွတ် ဖြစ်မယ် |

---

## Project docs

| ဖိုင် | အကြောင်း |
|---|---|
| `fullscope.md` | Spec အပြည့်အစုံ — feature, Firestore schema, scope အပြင် |
| `phase1.md` … `phase5.md` | ပတ်အလိုက် plan |
| `phase2-checklist.md` … `phase5-checklist.md` | ပတ်အလိုက် ပြီး/မပြီး စစ်ဆေးချက် |

---

## အဖွဲ့

| Member | တာဝန် |
|---|---|
| **A** | Home · Library · Mini player |
| **B** | Player engine (Media3) · News |
| **C** | Formatter · Full player · Search + History |
