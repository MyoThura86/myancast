# Phase 6 — Project Show Extensions ("မြန်မာ့ အခြေအနေအတွက် တည်ဆောက်ထားတဲ့ app")

> **အရေးကြီး** — ဒါက **plan guide** ပါ။ Phase 5 (course submit) **ပြီးမှ** စပါ။
>
> အခြေခံ — `fullscope.md` ရဲ့ nice-to-have စာရင်း၊ `phase5.md` ရဲ့ known limitation များ၊
> နဲ့ 2026-10-02 က `main` ပေါ်က code အခြေအနေ။

---

## Phase 6 — Overview

| အချက် | အသေးစိတ် |
|---|---|
| **ရည်ရွယ်ချက်** | Project show မှာ "ဒီ app က မြန်မာ အသုံးပြုသူအတွက် **ဘာကြောင့် ထူးခြားလဲ**" ကို ပြနိုင်အောင် feature ထပ်ထည့် |
| **ကြာချိန်** | Day 0 (၁.၅ နာရီ) + ၉ ရက် (~၂ ပတ်) · အချိန် နည်းရင် **"၁ ပတ် ဗားရှင်း"** (အောက်မှာ) |
| **အလုပ်ခွဲဝေပုံ** | Phase 3/4 လို **vertical slice + Day 0 contract** ပြန်သုံး |
| **အဆုံးမှာ ရရမယ့် ရလဒ်** | Tier 1 ၅ ခု + Tier 2 ၃ ခု · test အသစ်အားလုံး pass · **airplane mode demo** အလုပ်ဖြစ် |

### ⚠️ စည်းကမ်း — graded build ကို မထိရ

```
Phase 5 submit ပြီး → main မှာ tag `v1.0-submit` ထိုး → ပြီးမှ Phase 6 branch ဖွင့်
```

- Phase 5 ရဲ့ "feature အသစ် မထည့်ရ" စည်းကမ်းက **course submit အထိ** ပဲ။ Phase 6 က submit ပြီးမှ။
- `v1.0-submit` ရဲ့ APK ကို ဖုန်းထဲ သိမ်းထား — show ချိန် Phase 6 build ပျက်ရင် အဲဒါနဲ့ demo ပြန်ပြမယ်။

```powershell
git checkout main; git pull origin main
git tag v1.0-submit; git push origin v1.0-submit
```

---

## ⭐ Show မှာ ပြောမယ့် ပုံပြင် — မြန်မာ့ ပြဿနာ ၄ ခု → feature

| မြန်မာ့ အခြေအနေ | အသုံးပြုသူ ခံစားရတာ | ဖြေရှင်းမယ့် feature |
|---|---|---|
| **Mobile data ဈေးကြီး၊ လိုင်း မငြိမ်** | Episode တစ်ခု stream ရင် data ဘယ်လောက် ကုန်မလဲ မသိ · လိုင်းပြတ်ရင် ရပ်သွား | ဒေါင်းလုဒ် (#4) · data size ပြ + ချွေတာ mode (#5) · offline banner (#2) |
| **မီးပျက်၊ ဖုန်း low-end** | ဘက်ထရီ ချွေရ · storage နည်း | Sleep timer (#3) · ဒေါင်းလုဒ် ဖျက်/storage ပြ (#4) |
| **Zawgyi / Unicode ရောနေ** | Zawgyi keyboard နဲ့ ရှာရင် မတွေ့ · notification မှာ စာလုံး ပျက် | Zawgyi auto-detect + converter (#6) |
| **အသက်ကြီးသူ / တရားနာ** | စာလုံး သေး · ညအိပ်ခါနီး တရားနာ · ဥပုသ်နေ့ | စာလုံးအရွယ် + လွယ်ကူ mode (#8) · sleep timer (#3) · မြန်မာ ပြက္ခဒိန် (#7) |

> ဒါက slide **တစ်ချပ်တည်း** — ဘယ်ဘက် ပြဿနာ၊ ညာဘက် feature။ Judge တွေ ချက်ချင်း နားလည်တယ်။
> Feature တိုင်းကို "ဘယ် ပြဿနာ ဖြေရှင်းလဲ" ပြောနိုင်ရမယ် — မပြောနိုင်ရင် မထည့်နဲ့။

---

## Feature စာရင်း

### 🟢 Tier 1 — မဖြစ်မနေ (show ရဲ့ အခြေခံ)

| # | Feature | Owner | ကြာချိန် (ရက်) |
|---|---|---|---|
| 1 | **Settings သိမ်း** — app ပြန်ဖွင့်လည်း dark mode/setting မပျောက် | A | ၀.၅ |
| 2 | **Offline banner** + offline မှာ error မှန်မှန် ပြ | A | ၁ |
| 3 | **Sleep timer** — ၁၅/၃၀/၆၀ မိနစ် · "ဒီပိုင်း ဆုံးရင်" | B (+ C UI) | ၁ |
| 4 | **ဒေါင်းလုဒ်** — Wi-Fi only · လိုင်းပြတ်ရင် ဆက်ဒေါင်း · offline ဖွင့် | B (engine) + A (UI) | ၃ + ၁.၅ |
| 5 | **Data size ပြ** ("~၁၁ MB") + **data ချွေတာ mode** | C (formatter) + A (UI) | ၀.၅ + ၀.၅ |

### 🟡 Tier 2 — ထူးခြားချက် (show မှာ အမှတ်ယူမယ့် အပိုင်း)

| # | Feature | Owner | ကြာချိန် (ရက်) |
|---|---|---|---|
| 6 | **Zawgyi auto-detect + converter** — search + notification | C (+ B notification) | ၃ |
| 7 | **မြန်မာ ပြက္ခဒိန်** + ဥပုသ်နေ့ တရားတော် section | C | ၂.၅ |
| 8 | **စာလုံးအရွယ် + လွယ်ကူ mode** (အသက်ကြီးသူ) | A | ၁.၅ |

### ⚪ Tier 3 — အချိန်ရရင်

| # | Feature | Owner | ကြာချိန် (ရက်) | မှတ်ချက် |
|---|---|---|---|---|
| 9 | Bookmark — အချိန်မှတ် + မှတ်စု | C (သို့) A | ၂ | တရား/သင်ခန်းစာ နားထောင်သူ |
| 10 | Viber/Messenger share | A | ၀.၅ | `fullscope.md` မှာ out of scope — Day 0 မှာ ပြန်ဆုံးဖြတ် |
| 11 | FCM — ပိုင်းအသစ် notification | B | ၁ | Console ကနေ လက်နဲ့ ပို့ · Cloud Functions မလို |
| 12 | Crashlytics | B | ၀.၅ | "robust" ဆိုတာ ပြစရာ |
| 13 | Home screen widget (Glance) | A | ၂ | ဆက်နားထောင်ရန် card |

### ❌ မလုပ်ပါနဲ့

| Feature | ဘာကြောင့် |
|---|---|
| မြန်မာ TTS (သတင်း အသံဖတ်) | Device အများစုမှာ မြန်မာ voice မရှိ · cloud TTS က backend လို |
| Login / user upload / comment / payment | Firestore read-only design ပျက် · security rule ရေးရ |
| ဖုန်းချင်း file မျှဝေ (Bluetooth/peer) | Copyright ပြဿနာ · scope ကြီး |
| Zawgyi **font** toggle ပြန်ဖွင့် | #6 မှာ ရှင်းပြထား — ပြဿနာ အစစ်က font မဟုတ်ဘူး |

---

## Phase 6 က ဘာအပေါ်မှာ စတည်လဲ (code ထဲ စစ်ပြီး)

| အခြေအနေ | ဘာ |
|---|---|
| ✅ | `PlayerController` interface + `FakePlayerController` — additive ပြောင်းလို့ရ (Phase 4 ပုံစံ) |
| ✅ | `LibraryRepository` + Room v2 (`library` table) |
| ✅ | Pure function ပုံစံ (`domain/util/`, `SearchFilter.kt`) — feature အသစ်တွေလည်း ဒီပုံစံ |
| ✅ | Manifest မှာ `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS` ရှိပြီးသား |
| ⚠️ | `AppConfig` က `rememberSaveable` ပဲ — **app ပိတ်ရင် dark mode ပျောက်** · `SharedPreferences` code မရှိ |
| ⚠️ | `AppDatabase` က `fallbackToDestructiveMigration()` — version တိုးရင် subscribe/history **အကုန် ပျက်** |
| ⚠️ | `Media3PlayerController.toMediaItem()` က `audioUrl` ကိုပဲ သုံး — local file ကို မသိ |
| ⚠️ | `searchPodcasts()` က `contains()` ပဲ — Zawgyi နဲ့ ရိုက်ရင် မတွေ့ |
| ⚠️ | `EncodingUtil.kt` က TODO ပဲ ရှိ |
| ⚠️ | `minSdk = 24` — `java.time` (API 26) မသုံးရ · ပြက္ခဒိန်မှာ epoch millis နဲ့ တွက် |

---

## ⭐ Day 0 — Contract (၃ ယောက်လုံး၊ ၁.၅ နာရီ)

Phase 3/4 လိုပဲ — **interface + model + fake** ကို commit တစ်ခုတည်းမှာ ထည့်၊ ပြီးမှ ၃ ယောက် ပြိုင်တူ စ။
Branch: `feature/phase6-contract`

### Day 0 မှာ ဆုံးဖြတ်ရမယ့် ၆ ခု

| # | ကိစ္စ | အကြံပြု |
|---|---|---|
| 1 | Tier ဘယ်နှခု လုပ်မလဲ | Show ရက် ကြည့်ပြီး — ၂ ပတ် ရှိရင် Tier 1+2 · ၁ ပတ်ပဲ ဆို "၁ ပတ် ဗားရှင်း" |
| 2 | Share (#10) — out of scope ပြန်ဖွင့်မလား | ✅ ဖွင့် — share intent ရိုးရိုးပဲ၊ backend မလို |
| 3 | Zawgyi font toggle | ❌ ဖျက် → "ဖုန်း font" auto-detect setting နဲ့ အစားထိုး (#6) |
| 4 | Room v2 → v3 | ✅ `Migration(2, 3)` ရေး (subscribe/history မပျက်) · fallback ကို safety net အဖြစ် ဆက်ထား |
| 5 | Episode ရဲ့ file size | Firestore မှာ `fileSizeBytes` (optional) ထည့် · မရှိရင် duration × bitrate နဲ့ ခန့်မှန်း |
| 6 | ဒေါင်းလုဒ် default | Wi-Fi only = ✅ ON |

### Contract code

**① `domain/model/SleepTimerOption.kt` (အသစ်)**

```kotlin
package com.example.myancast.domain.model

/** Sleep timer ရွေးချယ်မှု — Full Player ရဲ့ 🌙 menu */
sealed interface SleepTimerOption {
    data object Off : SleepTimerOption
    data class Minutes(val minutes: Int) : SleepTimerOption
    data object EndOfEpisode : SleepTimerOption   // လက်ရှိ ပိုင်း ဆုံးရင် ရပ်
}
```

**② `PlaybackState` — additive (default ပါလို့ ရှိပြီးသား code မပျက်)**

```kotlin
    val error: String? = null,
    // ★ Phase 6
    val sleepTimerEndsAtMs: Long? = null,     // wall clock · null = timer မရှိ
    val sleepAtEpisodeEnd: Boolean = false
```

**③ `PlayerController` — additive (default body ပါလို့ fake တွေ ပြင်စရာ မလို)**

```kotlin
    /** ★ Phase 6 — default က ဘာမှ မလုပ်ဘူး */
    fun setSleepTimer(option: SleepTimerOption) {}
```

**④ `Episode` — additive (Firestore မှာ field မရှိလည်း 0)**

```kotlin
    val coverUrl: String = "",
    val fileSizeBytes: Long = 0L      // ★ Phase 6 — 0 = မသိ (ခန့်မှန်းမယ်)
```

**⑤ `domain/model/AppSettings.kt` + `data/repository/SettingsRepository.kt` (အသစ်)**

```kotlin
/** App ပိတ်ပြီး ပြန်ဖွင့်လည်း မပျောက်ရမယ့် setting များ */
data class AppSettings(
    val darkMode: Boolean = true,
    val textScale: Float = 1f,            // 1.0 / 1.15 / 1.3
    val easyMode: Boolean = false,        // အသက်ကြီးသူ mode — ခလုတ်ကြီး
    val dataSaver: Boolean = false,       // mobile data မှာ cover မဒေါင်း · stream မတိုင်မီ မေး
    val wifiOnlyDownload: Boolean = true,
    val deviceFont: DeviceFont = DeviceFont.UNKNOWN
)

enum class DeviceFont { UNKNOWN, UNICODE, ZAWGYI }

interface SettingsRepository {
    val settings: StateFlow<AppSettings>
    fun update(transform: (AppSettings) -> AppSettings)
}
```

> `AppConfig` (ui/theme) က UI ဘက်မှာ ဆက်ရှိမယ် — `MainActivity` က `AppSettings` → `AppConfig` ပြောင်းပေး။
> Data layer က `ui.theme` ကို import မလုပ်ရလို့ model ကို domain မှာ ခွဲထားတာ။

**⑥ `data/network/NetworkMonitor.kt` (အသစ်)**

```kotlin
data class NetworkStatus(
    val online: Boolean = true,
    val metered: Boolean = false     // mobile data = true · Wi-Fi = false
)

/** ViewModel က ConnectivityManager (android.*) ကို မသိရအောင် */
interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>
}
```

**⑦ `domain/model/DownloadStatus.kt` + `data/repository/DownloadRepository.kt` (အသစ်)**

```kotlin
sealed interface DownloadStatus {
    data object Queued : DownloadStatus
    data class Running(val percent: Int) : DownloadStatus
    data class Done(val localPath: String, val sizeBytes: Long) : DownloadStatus
    data object Failed : DownloadStatus
}

/** Library "ဒေါင်းလုဒ်" tab — offline မှာလည်း ဖွင့်လို့ရအောင် Episode အပြည့် သိမ်း */
data class DownloadedEpisode(
    val episode: Episode,
    val podcastTitle: String,
    val sizeBytes: Long
)

interface DownloadRepository {
    /** episodeId → status · ဒေါင်းမထားရင် map ထဲ မပါ */
    val statuses: StateFlow<Map<String, DownloadStatus>>
    fun downloadedEpisodes(): Flow<List<DownloadedEpisode>>
    suspend fun enqueue(episode: Episode, podcastTitle: String, wifiOnly: Boolean)
    suspend fun delete(episodeId: String)
}
```

**⑧ `MyanCastApp` — singleton ၃ ခု (Day 0 မှာ ယာယီ impl)**

```kotlin
    // ★ Phase 6 Contract — ယာယီ impl · owner က တကယ့် impl နဲ့ အစားထိုးမယ်
    val settingsRepository: SettingsRepository by lazy { InMemorySettingsRepository() }  // → A: SharedPrefsSettingsRepository
    val networkMonitor: NetworkMonitor by lazy { AlwaysOnlineNetworkMonitor() }         // → A: AndroidNetworkMonitor
    val downloadRepository: DownloadRepository by lazy { NoDownloadRepository() }        // → B: WorkManagerDownloadRepository
```

> Phase 3 ရဲ့ `DemoPlayerController` ပုံစံပဲ — app က Day 0 ကနေ run လို့ရနေမယ်။

**⑨ Test fakes (`app/src/test/.../myancast/`)**
- `FakeSettingsRepository` · `FakeNetworkMonitor` (status ကို test ကနေ ပြောင်းလို့ရ) · `FakeDownloadRepository`
- `FakePlayerController` — `lastSleepTimer: SleepTimerOption?` မှတ်ထား

### ✅ Day 0 Deliverable
- [ ] ဆုံးဖြတ်ချက် ၆ ခု ချပြီး
- [ ] Contract commit — `assembleDebug` + test ၁၀၉ ခု pass (ဘာမှ မပျက်)
- [ ] ၃ ယောက်လုံး `feature/phase6-contract` ကနေ branch ခွဲပြီး

---

## ⭐ Feature တစ်ခုချင်း အသေးစိတ်

### #1 Settings သိမ်း (A · ၀.၅ ရက်)

| အလုပ် | ဖိုင် |
|---|---|
| `SharedPrefsSettingsRepository` — ဖွင့်ချိန် တစ်ခါ ဖတ်ပြီး `MutableStateFlow` ထဲ ထည့် · `update()` ဆို `edit { }` နဲ့ ရေး | `data/settings/` |
| `rememberSaveable(AppConfig.Saver)` → `settingsRepository.settings.collectAsStateWithLifecycle()` | `MainActivity.kt` |
| Toggle တွေ `settingsRepository.update { it.copy(...) }` ခေါ် | `SettingsScreen.kt` |

> DataStore က spec မှာ ပိတ်ထားလို့ `SharedPreferences` သုံး။

**Test** — `SharedPrefsSettingsRepositoryTest` (Robolectric — dependency ရှိပြီးသား)
**Device** — Light ပြောင်း → recent apps ကနေ app ပိတ် → ပြန်ဖွင့် → Light ဆက်ဖြစ်နေရမယ်

---

### #2 Offline banner (A · ၁ ရက်)

| အလုပ် | ဖိုင် |
|---|---|
| `AndroidNetworkMonitor` — `registerDefaultNetworkCallback` · `NET_CAPABILITY_NOT_METERED` နဲ့ metered စစ် | `data/network/` |
| `OfflineBanner` — "အင်တာနက် မရှိပါ — သိမ်းထားတဲ့ data ကို ပြနေပါတယ်" | `ui/components/` |
| Scaffold ရဲ့ အပေါ်ဆုံးမှာ ထည့် | `MyanCastNavHost.kt` |
| Home/News ViewModel — offline + data အလွတ် ဆိုရင် EmptyView မဟုတ်ဘဲ error | `ui/home/`, `ui/news/` |

**ဘာကြောင့် ViewModel ပြင်ရလဲ** — Firestore က offline မှာ cache ကနေ data ပြန်ပေးတယ် (error မပေးဘူး)။
ပထမဆုံး install ပြီး offline ဆိုရင် cache မရှိလို့ **list အလွတ်** ရတယ် → app က "data မရှိသေးပါ" (EmptyView) ပြမိတယ် — **မှားတယ်**။

```kotlin
// ui/components/OfflineState.kt — pure, test လုပ်လို့ရ
fun offlineErrorOrNull(isEmpty: Boolean, online: Boolean): String? =
    if (isEmpty && !online) "အင်တာနက် ချိတ်ပြီး ပြန်ကြိုးစားပါ" else null
```

**Test** — `OfflineStateTest` · `HomeViewModelTest` (`FakeNetworkMonitor` offline + data အလွတ် → error)
**Device** — ① App data ရှင်း → airplane mode → ဖွင့် → error (empty မဟုတ်) ② Online တစ်ခါ ဖွင့်ပြီး → airplane mode → data + banner

---

### #3 Sleep timer (B · ၁ ရက်)

**Engine (`Media3PlayerController`)**

```kotlin
private var sleepJob: Job? = null

override fun setSleepTimer(option: SleepTimerOption) {
    sleepJob?.cancel()
    sleepJob = null
    when (option) {
        SleepTimerOption.Off ->
            _state.update { it.copy(sleepTimerEndsAtMs = null, sleepAtEpisodeEnd = false) }
        is SleepTimerOption.Minutes -> {
            val durationMs = option.minutes * 60_000L
            _state.update {
                it.copy(sleepTimerEndsAtMs = System.currentTimeMillis() + durationMs, sleepAtEpisodeEnd = false)
            }
            sleepJob = scope.launch {
                delay(durationMs)
                fadeOutAndPause()     // ၁၀ စက္ကန့် အသံ တဖြည်းဖြည်း လျှော့ → pause → volume ပြန်ထား
            }
        }
        SleepTimerOption.EndOfEpisode ->
            _state.update { it.copy(sleepTimerEndsAtMs = null, sleepAtEpisodeEnd = true) }
    }
}
```

**"ဒီပိုင်း ဆုံးရင်"** — `listener` ထဲမှာ `onMediaItemTransition(item, reason)` ထည့်:
`reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && sleepAtEpisodeEnd` ဆိုရင် `pause()` + flag ရှင်း။
နောက်ပိုင်းရဲ့ ၀:၀၀ မှာ ရပ်နေမယ် — "ဆက်နားထောင်ရန်" က အဲဒီကနေ ဆက်ဖွင့်လို့ ရတယ်။

> `MediaController` မှာ `setPauseAtEndOfMediaItems` မရှိဘူး (ExoPlayer မှာပဲ ရှိ) — ဒါကြောင့် transition event သုံးတာ။

**UI** — Full Player ရဲ့ 🌙 ခလုတ် + bottom sheet (ဖိုင်ပိုင်ရှင် **C** နဲ့ ညှိ)

```kotlin
// domain/util/SleepTimerFormatter.kt
fun sleepTimerLabel(endsAtMs: Long?, atEpisodeEnd: Boolean, nowMs: Long): String
// → "၁၂ မိနစ် ကျန်" · "ဒီပိုင်း ဆုံးရင် ရပ်မယ်" · "" (timer မရှိ)   ← မိနစ်ကို အပေါ် round
```

**Test** — `SleepTimerFormatterTest` · `PlayerViewModelTest` (option က `FakePlayerController` ဆီ ရောက်လား)
**Device** — ၁ မိနစ် (debug အတွက် ယာယီ) → screen ပိတ် → ၁ မိနစ်ကြာရင် အသံ တဖြည်းဖြည်း တိတ်ပြီး ရပ်

---

### #4 ဒေါင်းလုဒ် (B engine ၃ ရက် · A UI ၁.၅ ရက်)

**Dependency** — `libs.versions.toml` မှာ `androidx.work:work-runtime-ktx` (version ကို release page မှာ စစ်)

#### B — Engine

| အလုပ် | ဖိုင် |
|---|---|
| `DownloadEntity` (`downloads` table) — Episode ပြန်ဆောက်လို့ရအောင် field အပြည့် + `localPath`, `sizeBytes`, `status` | `data/local/` |
| `DownloadDao` | `data/local/` |
| `AppDatabase` v3 + **`Migration(2, 3)`** (`CREATE TABLE downloads ...`) | `data/local/AppDatabase.kt` |
| `DownloadWorker` (`CoroutineWorker`) | `data/download/` |
| `WorkManagerDownloadRepository` | `data/download/` |
| Player က local file ရှိရင် အဲဒါ ဖွင့် | `player/Media3PlayerController.kt`, `MyanCastApp.kt` |

**`DownloadWorker` — လိုင်းပြတ်ရင် ဆက်ဒေါင်း (resume)** ⭐

```
① filesDir/episodes/<id>.mp3.part ရှိလား စစ်
② ရှိရင် → HttpURLConnection မှာ "Range: bytes=<size>-" ထည့်
③ Server က 206 ပြန်ရင် → append · 200 ပြန်ရင် → အစက ပြန်ရေး (Range မထောက်ပံ့)
④ setProgress() — 1% တိုးမှ update (ခဏခဏ မခေါ်နဲ့)
⑤ IOException → Result.retry() (WorkManager က backoff နဲ့ ပြန်ကြိုး) · ၅ ခါ ကျော်ရင် Failed
⑥ ပြီးရင် .part → .mp3 rename · DAO မှာ Done
```

> WorkManager ရဲ့ ပုံမှန် worker က **၁၀ မိနစ်** ကျော်ရင် system က ရပ်တယ် — လိုင်းနှေးရင် ဖြစ်နိုင်တယ်။
> Resume ပါလို့ ပြန်စတဲ့အခါ ရပ်ခဲ့တဲ့ နေရာက ဆက်တယ် — ဒါက **မြန်မာ့ လိုင်းအတွက် အရေးကြီးဆုံး အပိုင်း**။

**Enqueue**

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
    .setRequiresStorageNotLow(true)
    .build()
workManager.enqueueUniqueWork("download-${episode.id}", ExistingWorkPolicy.KEEP, request)
```

**Player ချိတ်ပုံ** — `Media3PlayerController(context, localAudioPath: (String) -> String? = { null })`

```kotlin
// toMediaItem()
val uri = localAudioPath(id)
    ?.let(::File)?.takeIf { it.exists() }       // user က ဖျက်ထားရင် stream ပြန်သုံး
    ?.let(Uri::fromFile)
    ?: Uri.parse(audioUrl)
```

> ဒီလို player ထဲမှာ ဖြေရှင်းရင် Detail · ဆက်နားထောင်ရန် · Library — **ဘယ်ကနေ ဖွင့်ဖွင့်** local file ကို အလိုလို သုံးတယ်။

#### A — UI

| အလုပ် | ဖိုင် |
|---|---|
| Episode row မှာ ⬇ / % / ✓ / ⚠ ခလုတ် (ဖိုင်ပိုင်ရှင် အရင်ပြော) | `ui/components/EpisodeRow.kt` |
| Library tab ၃ — **"ဒေါင်းလုဒ်"** · စုစုပေါင်း size · ဖျက်ခလုတ် · နှိပ်ရင် ဖွင့် | `ui/library/` |
| `LibraryViewModel` မှာ `downloadRepository` ထည့် | `ui/library/LibraryViewModel.kt` |

```kotlin
// pure
fun totalDownloadBytes(items: List<DownloadedEpisode>): Long
fun downloadButtonState(status: DownloadStatus?): DownloadButtonState
```

**Test** — `LibraryViewModelTest` (`FakeDownloadRepository`) · pure function test · `WorkManagerDownloadRepository` ကို `work-testing` နဲ့ (အချိန်ရရင်)
**Device**
- [ ] Wi-Fi နဲ့ ဒေါင်း → airplane mode → Library "ဒေါင်းလုဒ်" ကနေ ဖွင့် → **ပွင့်ရမယ်**
- [ ] ဒေါင်းနေတုန်း Wi-Fi ပိတ် → ပြန်ဖွင့် → **ရပ်ခဲ့တဲ့ % ကနေ ဆက်**
- [ ] Wi-Fi only ON + mobile data ပဲ → "Wi-Fi စောင့်နေ" ပြ (မဒေါင်း)
- [ ] v2 build install → subscribe ၂ ခု → v3 update → **subscribe မပျောက်** (Migration)

---

### #5 Data size + data ချွေတာ mode (C ၀.၅ · A ၀.၅)

**C — pure formatter**

```kotlin
// domain/util/DataSizeFormatter.kt
const val ASSUMED_BITRATE_KBPS = 64      // Firestore က MP3 အချို့ကို စစ်ပြီး ပြင်ပါ

fun Episode.estimatedSizeBytes(): Long =
    if (fileSizeBytes > 0) fileSizeBytes
    else duration.toLong() * ASSUMED_BITRATE_KBPS * 1000 / 8

fun formatMegabytes(bytes: Long): String
// 11_500_000 → "~၁၁ MB" · 1 MB အောက် → "~၁ MB အောက်" · 0 → "" (ဖျောက်)
```

> မြန်မာ ဂဏန်း ပါလို့ `body*` style နဲ့ပဲ ပြ (`label*` = Poppins — glyph မရှိ)။

**A — UI**
- Episode row: `"၂၄ မိနစ် · ~၁၁ MB"`
- **Data ချွေတာ mode** ON + mobile data ဆိုရင် —
  - Cover: Coil request မှာ `networkCachePolicy(CachePolicy.DISABLED)` → cache ထဲ ရှိတာပဲ ပြ၊ အသစ် မဒေါင်း
    (`CompositionLocal` တစ်ခုနဲ့ `NavHost` မှာ ပေး — `PodcastCard`, `NewsCard`, `MiniPlayer`, Full Player အားလုံး)
  - ဒေါင်းမထားတဲ့ episode ဖွင့်ရင် dialog — "Mobile data နဲ့ ဖွင့်မှာလား (~၁၁ MB)"

**Test** — `DataSizeFormatterTest` (fileSizeBytes ရှိ/မရှိ · 0 · 1MB အောက် · နာရီကျော်)

---

### #6 Zawgyi auto-detect + converter (C ၃ ရက် · B notification)

#### ပြဿနာ အစစ်က ဘာလဲ (show မှာ ပြောရမယ့် insight ⭐)

App က **Padauk font ကို bundle** လုပ်ထားလို့ app **ထဲက** စာသား အားလုံး ဘယ်ဖုန်းမှာမဆို မှန်ပြီးသား။
Zawgyi font toggle က ဒါကြောင့် အကျိုး မရှိဘူး။ Zawgyi ဖုန်း သုံးသူ တကယ် ကြုံရတာက **၂ နေရာ**:

| နေရာ | ဖြစ်ပုံ | ဖြေရှင်းနည်း |
|---|---|---|
| **Search** | Zawgyi keyboard နဲ့ ရိုက် → Zawgyi encoding → Unicode data နဲ့ မကိုက် → **ရှာမတွေ့** | Query ကို detect → Unicode ပြောင်းပြီးမှ ရှာ |
| **Notification / lock screen** | System font (Zawgyi) နဲ့ ပြ → Unicode title **စာလုံးပျက်** | Zawgyi ဖုန်းဆိုရင် title ကို Zawgyi ပြောင်းပြီးမှ `MediaMetadata` ထဲ ထည့် |

#### Library ၂ ခု

| Library | License | သုံးပုံ |
|---|---|---|
| Google `myanmar-tools` (`ZawgyiDetector`) | Apache-2.0 | Pure Java — Maven Central (version စစ်) · domain မှာ သုံးလို့ရ |
| Rabbit converter | MIT | Java source ကို `domain/util/RabbitConverter.kt` အဖြစ် port · **license header ထား** |

> `android.icu` ရဲ့ transliterator မသုံးနဲ့ — domain purity ချိုးတယ် (Phase 4 finding နဲ့ အတူတူ)။

#### C — `EncodingUtil.kt` (TODO ကို အစားထိုး)

```kotlin
private val detector by lazy { ZawgyiDetector() }

/** User ရိုက်တာ Zawgyi ဆိုရင် Unicode ပြောင်း · Unicode/English ဆို မပြောင်း */
fun normalizeToUnicode(text: String): String =
    if (text.length >= 2 && detector.getZawgyiProbability(text) > 0.95) Rabbit.zg2uni(text)
    else text

fun unicodeToZawgyi(text: String): String = Rabbit.uni2zg(text)
```

- `searchPodcasts()` — `val q = normalizeToUnicode(query.trim())`
- `SearchUiState.convertedFromZawgyi` → hint chip **"Zawgyi နဲ့ ရိုက်ထားလို့ Unicode ပြောင်းရှာထားပါတယ်"**
- `EncodingUtil.kt` ရဲ့ ဆုံးဖြတ်ချက် comment ကို update

#### C — ဖုန်း font စစ်ခြင်း (`data/settings/SystemFontDetector.kt` — android)

```kotlin
/** Zawgyi font က "က္က" ကို stack မလုပ်ဘဲ ဘေးချင်းကပ် ပြလို့ ပိုကျယ်တယ် */
fun detectDeviceFont(): DeviceFont {
    val paint = Paint()    // system default typeface
    val stacked = paint.measureText("က္က")
    val single = paint.measureText("က")
    return if (stacked > single * 1.5f) DeviceFont.ZAWGYI else DeviceFont.UNICODE
}
```

- ပထမဆုံး ဖွင့်ချိန် (`deviceFont == UNKNOWN`) တစ်ခါ စစ် → `settingsRepository` မှာ သိမ်း
- Settings — disable ထားတဲ့ Zawgyi toggle နေရာမှာ **"ဖုန်း font: Zawgyi (အလိုအလျောက် သိ)"** + ကိုယ်တိုင် ပြောင်းလို့ရ
- ⚠️ မြန်မာ font လုံးဝ မရှိတဲ့ ဖုန်းဟောင်း (tofu box) က ZAWGYI လို့ မှားသိနိုင် — ကိုယ်တိုင် ပြောင်းလို့ရတာ ဒါကြောင့်

#### B — Notification

`toMediaItem()` မှာ `setTitle(if (isZawgyiDevice()) unicodeToZawgyi(title) else title)` (artist လည်း အတူတူ)

**Test** — `EncodingUtilTest`:
- Rabbit ရဲ့ ကိုယ်ပိုင် test file ကနေ Zawgyi↔Unicode pair ၁၀ ခု ကူး (**ကိုယ့်ဘာသာ မခန့်မှန်းနဲ့**)
- English / Unicode → မပြောင်း · စာလုံး ၁ လုံး → မပြောင်း
- `SearchFilterTest` — Zawgyi query နဲ့ Unicode podcast တွေ့ရမယ်
- Test string တွေကို `\u` escape နဲ့ ရေး (editor font ကြောင့် မျက်စိ မှားမှာ စိုးလို့)

**Device** — Zawgyi keyboard install → "တရား" ရိုက် → podcast တွေ့ + hint chip ပေါ်
(Zawgyi system font ဖုန်း ရှာရခက်ရင် notification ကို unit test + screenshot နဲ့ ပြ)

---

### #7 မြန်မာ ပြက္ခဒိန် + ဥပုသ်နေ့ (C · ၂.၅ ရက်)

**Algorithm** — ဒေါက်တာ ရန်နိုင်အေး ရဲ့ Myanmar Calendar algorithm (open source — **license စစ်ပြီး README မှာ credit ထည့်**)။
အဓိက function တွေကိုပဲ port — Julian day → မြန်မာ နှစ်/လ/ရက် · လဆန်း/လပြည့်/လဆုတ်/လကွယ် · ဥပုသ်။

```kotlin
// domain/util/MyanmarCalendar.kt — pure
data class MyanmarDate(
    val year: Int,              // မြန်မာ သက္ကရာဇ်
    val monthName: String,      // "သီတင်းကျွတ်"
    val moonPhase: MoonPhase,   // WAXING · FULL_MOON · WANING · NEW_MOON
    val fortnightDay: Int,      // ၁–၁၅
    val sabbath: Boolean        // ဥပုသ်နေ့
)

/** minSdk 24 — java.time မသုံးဘဲ epoch millis ကနေ Julian day တွက် · မြန်မာ = UTC+6:30 */
fun myanmarDate(epochMillis: Long, utcOffsetMinutes: Int = 390): MyanmarDate
fun MyanmarDate.label(): String     // format ဥပမာ — "၁၃၈၈ ခု သီတင်းကျွတ်လ လပြည့်ကျော် ၁ ရက်"
```

**UI (Home — ဖိုင်ပိုင်ရှင် A နဲ့ ညှိ)**
- Header အောက်မှာ ယနေ့ မြန်မာ ရက်စွဲ တစ်ကြောင်း
- ဥပုသ်နေ့ ဆိုရင် **"ဥပုသ်နေ့ တရားတော်"** section — category "တရားတော်" podcast များ
  (Firestore မှာ ဒီ category မရှိရင် Console မှာ ထည့်)

**Test** — `MyanmarCalendarTest` — ရက်စွဲ ၁၀ ခု (ဝါဆို/သီတင်းကျွတ်/တပေါင်း လပြည့် · ဝါထပ်နှစ် ပါအောင်)
> ⚠️ မျှော်လင့်ရလဒ်ကို **ပုံနှိပ် ပြက္ခဒိန် (သို့) reference implementation ကနေ ကူး** — ကိုယ့်ဘာသာ မတွက်နဲ့။

---

### #8 စာလုံးအရွယ် + လွယ်ကူ mode (A · ၁.၅ ရက်)

| အလုပ် | ဖိုင် |
|---|---|
| `myanCastTypography(zawgyi, textScale)` — `fontSize`, `lineHeight` နှစ်ခုလုံး × scale | `ui/theme/Type.kt` |
| Settings — "ပုံမှန် / ကြီး / အကြီးဆုံး" (1.0 / 1.15 / 1.3) + "မင်္ဂလာပါ" preview | `SettingsScreen.kt` |
| လွယ်ကူ mode — scale 1.3 + play ခလုတ် ကြီး + Mini player မြင့် | `PlayerControls.kt`, `MiniPlayer.kt` |

> **lineHeight ပါ မြှင့်ရမယ်** — မြန်မာစာ (ဆင့်ဗျည်း၊ ချောင်းငင်) က အပေါ်/အောက် ပြတ်တတ်တယ်။
> System font size နဲ့ ထပ်မြှောက်လို့ — **1.3 ထက် မကျော်နဲ့**။

**Device** — အကြီးဆုံး + ဖုန်း system font အကြီးဆုံး → Home · Detail · Player · Library စာလုံး မပြတ်၊ ခလုတ် မထပ်

---

### Tier 3 — အတိုချုပ်

| # | Feature | လုပ်ပုံ |
|---|---|---|
| 9 | Bookmark | `BookmarkEntity(id, episodeId, positionMs, note, createdAt)` (Room v3 ထဲ ပေါင်းထည့်) · Full Player 🔖 · နှိပ်ရင် `seekTo` |
| 10 | Share | `Intent.ACTION_SEND` — "{title} — MyanCast မှာ နားထောင်ပါ" · deep link မလို |
| 11 | FCM | `firebase-messaging` · topic `new_episodes` subscribe · Console → Messaging ကနေ လက်နဲ့ ပို့ |
| 12 | Crashlytics | Plugin + dependency · demo မတိုင်မီ crash ၀ ဖြစ်ကြောင်း dashboard ပြ |
| 13 | Widget | Glance · ဆက်နားထောင်ရန် card + ▶ ခလုတ် |

---

## 📅 Timeline (Day 0 + ၉ ရက်)

| Day | 🅰️ A | 🅱️ B | 🅲 C |
|---|---|---|---|
| 0 | Contract (၃ ယောက်လုံး) | ← | ← |
| 1 | #1 Settings သိမ်း · #2 `AndroidNetworkMonitor` | #3 Sleep timer engine | #5 Formatter + test · #6 Rabbit port စ |
| 2 | #2 Banner + Home/News offline error | #4 Room v3 + Migration + DAO | #6 Converter + detector + test |
| 3 | #5 Size ပြ + data ချွေတာ (Coil) | #4 `DownloadWorker` (resume) | #6 Search normalize + font detect + Settings row |
| 4 | #4 Episode row ဒေါင်းလုဒ် ခလုတ် | #4 Repository + player local file | #6 → B ကို လွှဲ · #3 UI (🌙 sheet) |
| 5 | #4 Library "ဒေါင်းလုဒ်" tab | #6 Notification Zawgyi | #7 Calendar core + test |
| 6 | #8 စာလုံးအရွယ် | #4 Device test (လိုင်းနှေး/ပြတ်) | #7 Home header + ဥပုသ် section |
| 7 | #8 လွယ်ကူ mode · #10 Share | #12 Crashlytics · #11 FCM | #9 Bookmark (သို့) buffer |
| 8 | 🐛 Device test အားလုံး (၃ ယောက်) · 🔴 bug ပဲ ပြင် | ← | ← |
| 9 | 🎬 Slide + demo + ပြန်လေ့ကျင့် ၂ ခါ · **code မထိ** | ← | ← |

**Merge စည်းကမ်း** — Day 7 ညနေ နောက်ဆုံး feature merge · Day 8 fix ပဲ · Day 9 code လုံးဝ မထိ။

### ⏱️ ၁ ပတ် ဗားရှင်း (show နီးနေရင်)

| ထည့် | ဖြုတ် |
|---|---|
| #1 · #2 · #3 · #4 (resume မပါ) · #5 · #6 (search ပဲ) | #6 notification · #7 · #8 · Tier 3 အားလုံး |

> ဒီလောက်နဲ့တင် airplane mode demo + Zawgyi search demo ပြလို့ရပြီ — show ရဲ့ အဓိက ၂ ခု။

---

## 🧪 Test အသစ် စာရင်း

| ဖိုင် | ဘာ စစ် |
|---|---|
| `SleepTimerFormatterTest` | label · round up · timer မရှိ |
| `DataSizeFormatterTest` | fileSizeBytes ရှိ/မရှိ · 0 · 1MB အောက် |
| `EncodingUtilTest` | Z↔U pair ၁၀ · English မပြောင်း · စာလုံးတို |
| `MyanmarCalendarTest` | ရက်စွဲ ၁၀ (reference ကနေ) |
| `OfflineStateTest` | offline + empty → error |
| `SharedPrefsSettingsRepositoryTest` | Robolectric — ရေးပြီး ပြန်ဖတ် |
| `SearchFilterTest` (ထပ်ဖြည့်) | Zawgyi query |
| `HomeViewModelTest` · `LibraryViewModelTest` · `PlayerViewModelTest` (ထပ်ဖြည့်) | fake အသစ်တွေနဲ့ |

> ရည်မှန်းချက် — ၁၀၉ → **၁၄၀ ကျော်**။ Slide မှာ ပြမယ့် ဂဏန်း။

---

## 📱 Device test (Day 8)

| # | စမ်းချက် | မျှော်လင့်ရလဒ် |
|---|---|---|
| 1 | Setting ပြောင်း → app ပိတ် → ပြန်ဖွင့် | Setting မပျောက် |
| 2 | First install + airplane mode | Error ပြ (empty မဟုတ်) |
| 3 | Online ဖွင့်ပြီး → airplane mode | Data + banner |
| 4 | Sleep timer ၁ မိနစ် + screen ပိတ် | Fade ပြီး ရပ် |
| 5 | "ဒီပိုင်း ဆုံးရင်" + seek ကို အဆုံးနား | နောက်ပိုင်း ၀:၀၀ မှာ ရပ် |
| 6 | ဒေါင်းပြီး → airplane mode → Library ကနေ ဖွင့် | ပွင့် |
| 7 | ဒေါင်းနေတုန်း Wi-Fi ပိတ်/ဖွင့် | % ကနေ ဆက် |
| 8 | Wi-Fi only + mobile data | မဒေါင်း · စောင့်နေ ပြ |
| 9 | ဒေါင်းထားတဲ့ file ဖျက် → ဖွင့် | Stream နဲ့ ပွင့် (crash မဖြစ်) |
| 10 | v2 → v3 update | Subscribe/history မပျောက် |
| 11 | Data ချွေတာ + mobile data | Cover အသစ် မဒေါင်း · dialog ပေါ် |
| 12 | Zawgyi keyboard နဲ့ ရှာ | တွေ့ + hint chip |
| 13 | ဥပုသ်နေ့ (ဖုန်း ရက်စွဲ ပြောင်းပြီး) | Section ပေါ် |
| 14 | စာလုံး အကြီးဆုံး + system font အကြီးဆုံး | စာလုံး မပြတ် |
| 15 | Phase 5 ရဲ့ device test ၃၂ ခု ပြန်စစ် (regression) | အရင်လို အလုပ်လုပ် |

---

## 🎬 Show Demo (~၆ မိနစ်)

```
၁။ ပြဿနာ slide (၃၀ စက္ကန့်)  → "data ဈေးကြီး · လိုင်းပြတ် · Zawgyi · အသက်ကြီးသူ"
၂။ Home                       → မြန်မာ ရက်စွဲ · (ဥပုသ်နေ့ ဆို) တရားတော် section
၃။ Detail                     → episode တိုင်း "~၁၁ MB" · ⬇ နှိပ်ပြ
၄။ Search ⭐                   → Zawgyi keyboard နဲ့ ရိုက် → တွေ့ + hint chip
၅။ Full Player                → 🌙 sleep timer ၁၅ မိနစ်
၆။ ✈️ Airplane mode ON ⭐⭐     → banner ပေါ် · Library "ဒေါင်းလုဒ်" → ဖွင့် → အသံ ထွက်
                                 "လိုင်းပြတ်လည်း ဆက်နားထောင်လို့ ရတယ်"
၇။ Settings                   → စာလုံး အကြီးဆုံး + လွယ်ကူ mode → Player ခလုတ်ကြီး
၈။ App ပိတ် → ပြန်ဖွင့်         → setting မပျောက် · ဆက်နားထောင်ရန် card
```

**ကြိုတင် ပြင်ဆင်ရန်**
- [ ] Episode ၂ ခု **ကြိုဒေါင်း**ထား (show ချိန် Wi-Fi မယုံရ)
- [ ] Zawgyi keyboard install + ဖွင့်ထား
- [ ] Quick settings မှာ airplane mode ခလုတ် ရှေ့ဆုံး ရွှေ့ထား
- [ ] `v1.0-submit` APK ကို backup အဖြစ် ဖုန်းထဲ ထား

---

## 📊 Slide ထပ်ဖြည့်

| Slide | ပါရမယ့် အချက် |
|---|---|
| ပြဿနာ → feature | အပေါ်က ဇယား ၄ ကြောင်း |
| Offline-first | Firestore cache + Room + local file → "လိုင်းမရှိလည်း အလုပ်လုပ်" diagram |
| Zawgyi insight ⭐ | "Font ပြောင်းတာ အဖြေ မဟုတ် — input နဲ့ system UI က ပြဿနာ" |
| Contract ပြန်သုံး | Phase 6 Day 0 — additive + default → test ၁၀၉ ခု တစ်ခုမှ မပျက် |
| Test | ၁၀၉ → ၁၄၀+ |
| Credit | Library/algorithm license များ |

---

## ❓ မေးခွန်း ပြင်ဆင်မှု

| မေးခွန်း | ဖြေရမယ့် အချက် |
|---|---|
| "Zawgyi font toggle ဘာကြောင့် ဖျက်လဲ" | Padauk bundle လုပ်ထားလို့ app ထဲ မှန်ပြီးသား · ပြဿနာ အစစ်က keyboard input နဲ့ notification |
| "DownloadManager ဘာကြောင့် မသုံးလဲ" | `fullscope.md` က WorkManager · Room status + Range resume ကို ကိုယ်တိုင် ထိန်းချင်လို့ · test လုပ်လို့ရ · DownloadManager လည်း ရွေးလို့ရတဲ့ နည်းပါ |
| "MB က တိကျလား" | Firestore မှာ `fileSizeBytes` ရှိရင် အတိအကျ · မရှိရင် bitrate နဲ့ ခန့်မှန်း ("~") |
| "File ကို user ဖျက်ရင်" | `File.exists()` စစ်ပြီး stream ပြန်သုံး |
| "ပြက္ခဒိန် မှန်လား" | Reference algorithm · ရက်စွဲ ၁၀ ခုနဲ့ unit test |
| "Background မှာ timer ဘယ်လို" | Player နဲ့ process တူလို့ coroutine `delay` · service အသက်ရှင်နေသမျှ အလုပ်လုပ် |

---

## Phase 6 — Troubleshooting

| ပြဿနာ | ဖြေရှင်းနည်း |
|---|---|
| ဒေါင်းလုဒ် ~၁၀ မိနစ်မှာ ရပ် | WorkManager limit — ပုံမှန်ပဲ · retry + Range resume က ဆက်ပေးမယ် |
| Server က Range မထောက်ပံ့ (200 ပြန်) | အစက ပြန်ဒေါင်း (code က handle ပြီး) |
| `Migration didn't properly handle` crash | `CREATE TABLE` SQL က Room မျှော်လင့်တာနဲ့ မကိုက် → `exportSchema = true` ဖွင့်ပြီး schema JSON ထဲက SQL ကူး |
| Network callback emulator မှာ မလာ | ဖုန်းအစစ်မှာ စမ်း |
| Zawgyi detector စာလုံးတို မှာ မှား | `length >= 2` + probability `> 0.95` ပဲ ပြောင်း |
| စာလုံး အကြီးဆုံးမှာ layout ပြို | Scale 1.3 ထက် မကျော် · `maxLines` + ellipsis |
| Data ချွေတာ ON ပေမဲ့ cover ဒေါင်းနေ | `AsyncImage` **အားလုံး** CompositionLocal ကို ဖတ်လား စစ် |
| Sleep timer ပြီးမှ အသံ မထွက်တော့ | Fade ပြီးရင် volume `1f` ပြန်ထားဖို့ မေ့နေ |

---

## Phase 6 — Git Workflow

```
main (tag v1.0-submit)
  ├── feature/phase6-contract        (၃ ယောက်)
  ├── feature/phase6-slice-a         (settings · offline · download UI · text size)
  ├── feature/phase6-slice-b         (sleep timer · download engine · notification)
  └── feature/phase6-slice-c         (data size · Zawgyi · calendar)
```

**စည်းကမ်း**
1. Push မတိုင်မီ `.\gradlew assembleDebug` + `testDebugUnitTest`
2. `git add app/src` (+ docs) ပဲ — `.idea/`, `.kotlin/` မပါအောင်
3. **Shared ဖိုင်** ပြင်ရင် ပိုင်ရှင်ကို အရင်ပြော — `EpisodeRow.kt` · `FullPlayerScreen.kt` · `HomeScreen.kt` · `SettingsScreen.kt` · `MyanCastApp.kt` · `AppDatabase.kt`
4. Contract ဖိုင်တွေ (`PlaybackState`, `PlayerController`, `SettingsRepository`, `NetworkMonitor`, `DownloadRepository`) ပြောင်းရင် ၃ ယောက်လုံး သဘောတူရမယ်

---

## 📄 README ထပ်ဖြည့်

```markdown
## Myanmar အတွက် feature များ
- ဒေါင်းလုဒ် (Wi-Fi only · လိုင်းပြတ်ရင် ဆက်) · offline နားထောင်
- Data size ပြ + data ချွေတာ mode
- Zawgyi keyboard နဲ့ ရှာလို့ရ · Zawgyi ဖုန်း notification မှန်
- မြန်မာ ပြက္ခဒိန် + ဥပုသ်နေ့ တရားတော်
- Sleep timer · စာလုံးအရွယ် · လွယ်ကူ mode

## Credit
- Rabbit converter — MIT
- Google myanmar-tools — Apache-2.0
- Myanmar Calendar algorithm — ဒေါက်တာ ရန်နိုင်အေး (license စစ်ပြီး ဖြည့်)
- Padauk font — SIL OFL
```
