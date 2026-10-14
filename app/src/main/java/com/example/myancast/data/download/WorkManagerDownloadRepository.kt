package com.example.myancast.data.download

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myancast.data.local.DownloadDao
import com.example.myancast.data.local.DownloadEntity
import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * ★ Phase 6 (#4) — WorkManager နဲ့ ဒေါင်း · Room မှာ status သိမ်း · `filesDir/episodes/` မှာ ဖိုင်။
 * App တစ်ခုလုံး တစ်ခုတည်း (`MyanCastApp.downloadRepository`)။
 */
class WorkManagerDownloadRepository(
    context: Context,
    private val dao: DownloadDao,
    scope: CoroutineScope
) : DownloadRepository {

    private val appContext = context.applicationContext
    private val workManager by lazy { WorkManager.getInstance(appContext) }

    private val entities: StateFlow<List<DownloadEntity>> =
        dao.observeAll().stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val statuses: StateFlow<Map<String, DownloadStatus>> = entities
        .map { list -> list.associate { it.episodeId to it.toStatus() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override fun downloadedEpisodes(): Flow<List<DownloadedEpisode>> = entities.map { list ->
        list.filter { it.status == DownloadEntity.STATUS_DONE }.map { it.toDownloaded() }
    }

    override suspend fun enqueue(episode: Episode, podcastTitle: String, wifiOnly: Boolean) {
        val existing = dao.get(episode.id)
        if (existing?.status == DownloadEntity.STATUS_DONE && File(existing.localPath).exists()) return

        dao.upsert(
            episode.toDownloadEntity(
                podcastTitle = podcastTitle,
                wifiOnly = wifiOnly,
                progress = existing?.progress ?: 0,   // .part ကျန်နေရင် ဆက်ပြ
                now = System.currentTimeMillis()
            )
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(DownloadWorker.KEY_EPISODE_ID to episode.id))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        // REPLACE — Wi-Fi only setting ပြောင်းပြီး ပြန်နှိပ်ရင် constraint အသစ်နဲ့ (`.part` ကနေ ဆက်)
        workManager.enqueueUniqueWork(workName(episode.id), ExistingWorkPolicy.REPLACE, request)
    }

    override suspend fun delete(episodeId: String) {
        workManager.cancelUniqueWork(workName(episodeId))
        dao.delete(episodeId)
        withContext(Dispatchers.IO) {
            val dir = DownloadWorker.downloadDir(appContext)
            File(dir, "$episodeId.mp3").delete()
            File(dir, "$episodeId.mp3.part").delete()
        }
    }

    override fun localPath(episodeId: String): String? =
        entities.value
            .firstOrNull { it.episodeId == episodeId && it.status == DownloadEntity.STATUS_DONE }
            ?.localPath
            ?.takeIf { it.isNotBlank() }

    private fun workName(episodeId: String) = "download-$episodeId"

    private companion object {
        const val BACKOFF_SECONDS = 30L
    }
}
