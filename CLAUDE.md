# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

MyanCast Personal is a Myanmar-language podcast and news app. It is a 3-person, 5-week course project with a single `app` module (Kotlin, Jetpack Compose, MVVM). Read the relevant phase doc before building a feature, because it records the decisions the team agreed on.

Planning docs at the repo root (Phase 1–2 docs are in Burmese):
- `fullscope.md`: the full spec (features, Firestore schema, out-of-scope list). Its by-layer team split was replaced from Phase 2 on by **vertical slices** (each member does data → model → UI for one feature).
- `phase1.md`, `phase2.md`, `phase-2 Detailed.md`, `memberA/B/C.md`: Weeks 1–2 (done).
- `phase2-checklist.md`: what Phase 2 delivered, what was postponed, and the Poppins-font carry-over list.
- `phase3.md` / `phase3-checklist.md`: Player (A = Mini player, B = Media3 engine, C = Full Player).
- `phase4.md` / `phase4-checklist.md`: News, Library + Subscribe, Search + Continue Listening.
- `phase5.md` / `phase5-checklist.md`: polish, testing, presentation (course submit).
- `phase6-showcase.md`: **current phase** — post-submit extensions. Tier 1 (#1–#5) is built; Tier 2/3 are not.
- `docs/admin-panel.md`: admin panel setup (Firebase Auth user, rules, Storage/Blaze) and usage.
- `README.md`: build steps, Firestore schema, known limitations.

**Status:** All 15 MVP features are built (Zawgyi is deliberately Unicode-only). Phase 6 adds an **admin panel** (Settings → စီမံခန့်ခွဲသူ) and Tier 1 of `phase6-showcase.md`: persisted settings, offline banner, sleep timer, downloads, data size + data saver.

## Commands

Run these from the repo root. On Windows PowerShell, use `.\gradlew`.

```bash
./gradlew assembleDebug                 # build (the team rule is to run this before every push)
./gradlew installDebug                  # install on a connected device or emulator
./gradlew testDebugUnitTest             # JVM unit tests
./gradlew testDebugUnitTest --tests "com.example.myancast.ui.home.HomeViewModelTest"
./gradlew testDebugUnitTest --tests "com.example.myancast.domain.util.*"   # pure formatter tests, fast
./gradlew testDebugUnitTest --tests "*CategoryFilterTest.unknown*"          # single test by name
./gradlew connectedDebugAndroidTest     # instrumented tests (needs a device)
./gradlew lint
```

Dependencies are declared in `gradle/libs.versions.toml` (version catalog). Room uses KSP. Firebase config is in `app/google-services.json`.

## Architecture

The code is layered as UI → domain → data, all under `com.example.myancast`. There is no DI framework: the spec rules out Hilt, Koin, Retrofit and DataStore.

- **Data (`data/`)**: Repositories are the only code that talks to Firestore or Room. The UI never calls Firebase directly. `PodcastRepository` is an interface, and the Firestore implementation is `PodcastRepositoryImpl`. ViewModel factories default to `PodcastRepositoryImpl()`.
  - `data/firebase/FirestoreExt.kt` provides `Query.snapshotFlow(clazz) { obj, id -> obj.copy(id = id) }` and `toObjectWithId`. Always use these instead of `toObjects()`: Firestore does not put the document ID in the fields, so `id` would stay `""`. `snapshotFlow` wraps a snapshot listener in `callbackFlow`, and the listener is removed when collection stops.
  - Firestore is read-only for **users**; only the **admin** (Firebase Auth email/password, `AdminConfig.EMAIL`) writes, enforced by `firebase/firestore.rules` + `firebase/storage.rules`. There are 3 collections: `podcasts`, `episodes` (`podcastId` is the podcast's document ID), and `news`.
  - `data/admin/`: all admin writes go through `AdminContentRepository` (`FirestoreAdminContentRepository`: explicit field maps from `FirestoreSchema.kt`, never `toObject` serialization — `Episode.formattedDuration` would be written; batches keep `episodeCount`/`updatedAt` in sync). Media uploads go through `MediaUploader` (Firebase Storage, `podcasts/<id>/…`, `episodes/<id>/…`), MP3 checks through `AudioInspector`. Repos throw `AdminException` with a Burmese message. `FirestoreSchemaTest` fails if a written field name drifts from the `Podcast`/`Episode` model. `getEpisodes()` combines `whereEqualTo("podcastId")` with `orderBy("publishedAt")`, so it needs a Firestore composite index. A `FAILED_PRECONDITION` error in Logcat includes the link that creates it.
  - Room (`data/local/AppDatabase`, version 4) holds subscriptions and history in one `library` table (`type` = `sub`/`history`) and downloads in `downloads`. v3→v4 uses `MIGRATION_3_4` (keeps user data); `fallbackToDestructiveMigration()` stays only as a safety net. Library is exposed through `MyanCastApp.libraryRepository` (`LibraryRepository` interface, `RoomLibraryRepository` impl). **Any** entity change — even renaming a column — changes Room's schema hash, so bump `version` **and write a Migration** (copy the `CREATE TABLE` SQL from the generated `AppDatabase_Impl`; `AppDatabaseMigrationTest` shows how to test it).
  - `data/download/`: `DownloadRepository` → `WorkManagerDownloadRepository` + `DownloadWorker` (Range resume, Wi-Fi-only constraint, files in `filesDir/episodes/`). `data/settings/`: `SettingsRepository` → `SharedPrefsSettingsRepository` (DataStore is ruled out). `data/network/`: `NetworkMonitor` → `AndroidNetworkMonitor`, plus the Coil `DataSaverInterceptor` installed by `MyanCastApp.newImageLoader()`. Test-friendly no-op defaults: `AlwaysOnlineNetworkMonitor`, `NoDownloadRepository`.
  - `NewsRepository` is an interface with `FirestoreNewsRepository` as the impl.
- **Domain (`domain/`)**: `model/` contains the Firestore-mapped data classes. Every property needs a default value for Firestore deserialization, and Boolean properties must not use an `is` prefix, because that breaks Firestore mapping. `util/` contains pure, unit-tested helpers:
  - `toMyanmarDigits()` (`Int`/`Long`, 0–9 → ၀–၉). Use it for counts and labels shown in Myanmar text.
  - `formatDuration(seconds)` → `"24:30"` / `"1:02:05"` (ASCII, for progress/seek bars).
  - `formatDurationLabel(seconds)` → `"၂၄ မိနစ်"`, `"၁ နာရီ ၂ မိနစ်"`, `"၁ မိနစ် အောက်"`, or `""` for ≤ 0 (callers hide blank parts).
  - `formatRelativeDate(Timestamp?, now)` → `"ခုလေးတင်"` / `"၅ ရက် အရင်က"` / `"၁၅ စက်တင်ဘာ ၂၀၂၅"`. The logic lives in `formatRelativeMillis(epochMillis, now)` so tests don't need `Timestamp` or the real clock. Month names are a hard-coded array because `Locale("my")` falls back to English on some devices.
- **UI (`ui/<feature>/`)**: Each feature has a `XxxScreen` and a `XxxViewModel`.
  - A ViewModel exposes a single `StateFlow<XxxUiState>` data class with `isLoading` and `error`. It takes the repository through its constructor and provides a `companion object { fun factory(...) = viewModelFactory { initializer { ... } } }`. Screens get it with `viewModel(factory = XxxViewModel.factory(...))`. Detail screens pass the nav argument into `factory(id)`.
  - A ViewModel keeps a `loadJob` and cancels it before re-collecting in `refresh()`/`retry()`, so snapshot listeners don't pile up.
  - Screens use `when { isLoading -> LoadingView; error != null -> ErrorView; empty -> EmptyView; else -> content }` in that order. The shared views are in `ui/components/`.
  - A full-screen `EmptyView` is only for "no data at all". An empty *filtered* or *child* list is shown inline, so the rest of the screen stays usable. Home checks `allPodcasts.isEmpty()` for the full-screen case, and shows "ဒီအမျိုးအစားမှာ မရှိသေးပါ" inline when a category filter matches nothing. Detail keeps the podcast header and shows "ပိုင်း မရှိသေးပါ" inline.
- **Navigation**: `ui/navigation/Screen.kt` holds all route strings and argument keys. Use `Screen.PodcastDetail.create(id)` and never hard-code a route. `MyanCastNavHost` owns the Scaffold, shows the 5-tab bottom bar only on tab routes, and switches tabs with `navigateToTab` (pop to start, save/restore state).
- **Theme / Myanmar text**: `AppConfig(zawgyi, darkMode)` is built in `MainActivity` from `settingsRepository.settings` (persisted `AppSettings`) and provided through `MyanCastTheme` / `LocalAppConfig`. `SettingsScreen` takes `AppSettings` + `onSettingsChange`. `myanCastTypography(zawgyi)` switches the Myanmar font family (Padauk ↔ Zawgyi-One) for display, headline, title and body styles. The `label*` styles use Poppins, which has **no Myanmar glyphs**, so any Burmese text must use a body/title style. The Myanmar line height is about 1.8× the font size. ASCII-only text (times like `"24:30"`, speeds like `"1.5x"`) may use `label*`.
  - **Zawgyi is permanently disabled (Phase 5 Day 0: KEEP).** Firestore text is Unicode, and the toggle only swapped the font, which garbled the text. The Settings toggle is `enabled = false`, and `EncodingUtil.kt` + README record the decision (a Rabbit converter would be needed).
  - **Colors:** use `MaterialTheme.colorScheme.*` (`onSurface`, `onSurfaceVariant`, `outline`, `surface`, `surfaceVariant`) in UI code, never the raw dark-palette constants (`TextHi`, `TextLo`, `OutlineDark`, `SurfaceDark`), or Light mode becomes unreadable. Brand colors (`GoldPrimary`, and `BgDark` as content on gold) are fine.
- **Player (`player/`)**: `PlaybackService` (`MediaSessionService` owning the ExoPlayer; notification + lock screen come from the MediaSession) and `Media3PlayerController` (a `MediaController` that maps `Player` events to `PlaybackState`, plus a 500 ms position ticker). `HistoryRecorder` (app scope, started in `MyanCastApp.onCreate`) saves progress to Room every 10 s bucket, on pause, and on episode change.
  - `domain/model/PlaybackState` is one data class (queue, currentIndex, podcastTitle, isPlaying, isBuffering, positionMs, durationMs, speed, error). `currentEpisode`/`hasNext`/`hasPrevious`/`progress` are computed from it. Times are in ms; `durationMs = 0` means unknown, and the UI then falls back to `episode.duration` (seconds).
  - `player/PlayerController` is an interface with `state: StateFlow<PlaybackState>` and `play(queue, podcastTitle, startPositionMs = 0L)`, toggle, seek, skip (−15 s / +30 s), next/previous, and speed (0.5–2x). ViewModels depend only on this interface, never on `androidx.media3.*`. Media3 code stays inside `player/`.
  - There is exactly one instance app-wide: `MyanCastApp.playerController`, read in factories via `this[APPLICATION_KEY] as MyanCastApp`. It is `Media3PlayerController`, which retries the service connection on `play()`/toggle if the first connect failed. Tests use `FakePlayerController`.
  - Never call `seekTo()` right after `play()` to resume: if the service isn't connected yet the seek is lost. Pass `startPositionMs` to `play()` instead.
  - `PlayerQueue.from(episodes, startEpisodeId)` drops episodes with a blank `audioUrl`, and returns an empty queue if the tapped episode isn't playable. `play()` with an empty queue does nothing.
  - News audio plays through the same player via the pure `NewsItem.toEpisode()` mapping (`podcastId = ""`).
  - `Media3PlayerController(context, localAudioPath)` plays a downloaded file when one exists (falls back to `audioUrl` if the file is gone). Sleep timer: `PlayerController.setSleepTimer(SleepTimerOption)` (default no-op), state in `PlaybackState.sleepTimerEndsAtMs` / `sleepAtEpisodeEnd`.
  - Navigation to `Screen.Player` always uses `launchSingleTop = true`, so double taps don't stack two players.
- **Release**: `isMinifyEnabled = false` on purpose (R8 could strip Firestore model constructors). `app/proguard-rules.pro` holds the keep rules needed if minify is ever turned on.
- **App icon**: adaptive vector icon (`drawable/ic_launcher_foreground|background|monochrome.xml`) — the Burmese letter "မ" in gold with a ▶ in its bowl. Legacy PNGs in `mipmap-*` (API 24–25) are rendered from the same geometry; regenerate them if the vector changes. `docs/app-icon-512.png` is the 512 px version.

## Team conventions (from the phase docs)

- Keep all list logic (`filter`, `sortedBy`, `map`) in the ViewModel or in pure functions such as `ui/home/CategoryFilter.kt`, never inside composables.
- ViewModels must not import `android.*`, so they stay unit-testable.
- Collect state with `collectAsStateWithLifecycle()`, not `collectAsState()`.
- Do not use `!!`. In a composable, `modifier: Modifier = Modifier` is the first optional parameter.
- Put `@Preview` only on private stateless content composables wrapped in `MyanCastTheme`, not on screens that create a ViewModel.
- The category sentinel `"အားလုံး"` ("All") is the constant `ALL_CATEGORY`. Do not duplicate the literal.
- Any `Text` showing Burmese must use a `body*`/`title*`/`headline*`/`display*` style, never `label*` (Poppins). Show counts in Myanmar digits with Myanmar words, for example `"${n.toMyanmarDigits()} ပိုင်း"`, not `"$n episodes"`.
- Keep pure logic (mapping, formatting, filtering) in top-level functions, so it can be tested without coroutines. Examples: `CategoryFilter.kt`, `domain/util/`, and in Phase 3 `PlaybackState.toUiState()` and the `PlayerQueue` helpers.
- ViewModel unit tests use either `FakePodcastRepository` (in `app/src/test/.../myancast/`) with `StandardTestDispatcher` + `advanceUntilIdle()` (`HomeViewModelTest`), or MockK with `UnconfinedTestDispatcher` (`PodcastDetailViewModelTest`). Test fakes live next to `FakePodcastRepository`.
- User-facing strings and code comments are written in Burmese.
- Shared-file ownership matters, so tell the owner before editing their file. In Phase 3, `PlaybackState.kt` and `PlayerController.kt` are a frozen contract: changing them needs all 3 members.
- The docs describe `main` ← `dev` ← `feature/<name>`, but in practice the team opens PRs directly into `main`; there is no `dev` branch on the remote. Branch names: `feature/phase3-slice-a|b|c`, `feature/phase3-contract`.
- Stage `app/src` (and docs) explicitly. Don't use `git add .`. `.kotlin/` and `.idea/deploymentTargetSelector.xml` are now gitignored, but other `.idea/` files are still tracked.
- Phase 5 branches were `fix/...` only. Phase 6 work is on the `extension` branch (`phase6-showcase.md` suggests `feature/phase6-*`).
- Admin UI strings: buttons use formal verbs (`သိမ်းမည်`, `ဖျက်မည်`, `မလုပ်တော့ပါ`), messages use polite colloquial (`…ပါတယ်`, `…ပါ`), matching the rest of the app.
