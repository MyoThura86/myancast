package com.example.myancast

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.myancast.data.admin.AdminAuthRepository
import com.example.myancast.data.admin.AdminContentRepository
import com.example.myancast.data.admin.AndroidAudioInspector
import com.example.myancast.data.admin.AudioInspector
import com.example.myancast.data.admin.FirebaseAdminAuthRepository
import com.example.myancast.data.admin.FirebaseStorageUploader
import com.example.myancast.data.admin.FirestoreAdminContentRepository
import com.example.myancast.data.admin.MediaUploader
import com.example.myancast.data.download.DownloadRepository
import com.example.myancast.data.download.WorkManagerDownloadRepository
import com.example.myancast.data.local.AppDatabase
import com.example.myancast.data.network.AndroidNetworkMonitor
import com.example.myancast.data.network.DataSaverInterceptor
import com.example.myancast.data.network.NetworkMonitor
import com.example.myancast.data.repository.LibraryRepository
import com.example.myancast.data.repository.RoomLibraryRepository
import com.example.myancast.data.settings.SettingsRepository
import com.example.myancast.data.settings.SharedPrefsSettingsRepository
import com.example.myancast.player.HistoryRecorder
import com.example.myancast.player.Media3PlayerController
import com.example.myancast.player.PlayerController
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MyanCastApp : Application(), ImageLoaderFactory {

    /** App တစ်သက်လုံး အလုပ်လုပ်ရမယ့် အရာများ (history သိမ်း · download status) */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // ★ Phase 6 (#4) — ဒေါင်းထားတဲ့ ဖိုင် ရှိရင် player က stream မလုပ်ဘဲ အဲဒါကို ဖွင့်
    val playerController: PlayerController by lazy {
        Media3PlayerController(this, localAudioPath = { id -> downloadRepository.localPath(id) })
    }
    val database by lazy { AppDatabase.get(this) }

    // ★ Phase 4 Contract
    val libraryRepository: LibraryRepository by lazy {
        RoomLibraryRepository(database.libraryDao())
    }

    // ★ Phase 6 (#1) — app ပိတ်ပြီး ပြန်ဖွင့်လည်း မပျောက်တဲ့ setting
    val settingsRepository: SettingsRepository by lazy { SharedPrefsSettingsRepository(this) }

    // ★ Phase 6 (#2) — network (offline banner · admin က upload မလုပ်ခင် စစ်)
    val networkMonitor: NetworkMonitor by lazy { AndroidNetworkMonitor(this) }

    // ★ Phase 6 (#4) — ဒေါင်းလုဒ် (WorkManager · Wi-Fi only · resume)
    val downloadRepository: DownloadRepository by lazy {
        WorkManagerDownloadRepository(this, database.downloadDao(), appScope)
    }

    // ★ Phase 6 — Admin panel (Settings → စီမံခန့်ခွဲသူ)
    val adminAuth: AdminAuthRepository by lazy { FirebaseAdminAuthRepository() }
    val adminContent: AdminContentRepository by lazy { FirestoreAdminContentRepository() }
    val mediaUploader: MediaUploader by lazy { FirebaseStorageUploader(this) }
    val audioInspector: AudioInspector by lazy { AndroidAudioInspector(this) }

    /**
     * ★ Phase 4 (C) — playback ကို နားထောင်ပြီး history သိမ်းသူ။
     * App scope မို့ screen ပိတ်လည်း (background playback) ဆက်သိမ်းတယ်။
     */
    private val historyRecorder by lazy {
        HistoryRecorder(
            player = playerController,
            library = libraryRepository,
            scope = appScope
        )
    }

    /**
     * ★ Phase 6 (#5) — Coil ImageLoader (app တစ်ခုလုံး) — Data ချွေတာ mode + mobile data ဆို
     * ပုံအသစ် မဒေါင်း (cache ထဲက ပုံပဲ ပြ)။
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components {
            add(DataSaverInterceptor {
                settingsRepository.settings.value.dataSaver && networkMonitor.status.value.metered
            })
        }
        .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        historyRecorder.start()
    }
}
