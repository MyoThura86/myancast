package com.example.myancast

import com.example.myancast.data.download.DownloadRepository
import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** ★ Phase 6 (#4) — WorkManager/File မလိုဘဲ ဒေါင်းလုဒ် အခြေအနေကို test ကနေ ထိန်း */
class FakeDownloadRepository(
    initialDone: List<DownloadedEpisode> = emptyList()
) : DownloadRepository {

    private val done = MutableStateFlow(initialDone)
    private val _statuses = MutableStateFlow(
        initialDone.associate { it.episode.id to DownloadStatus.Done("/f/${it.episode.id}.mp3", it.sizeBytes) as DownloadStatus }
    )
    override val statuses: StateFlow<Map<String, DownloadStatus>> = _statuses.asStateFlow()

    val enqueued = mutableListOf<Triple<Episode, String, Boolean>>()
    val deleted = mutableListOf<String>()

    override fun downloadedEpisodes(): Flow<List<DownloadedEpisode>> = done.map { it }

    override suspend fun enqueue(episode: Episode, podcastTitle: String, wifiOnly: Boolean) {
        enqueued += Triple(episode, podcastTitle, wifiOnly)
        _statuses.value = _statuses.value + (episode.id to DownloadStatus.Queued(wifiOnly))
    }

    override suspend fun delete(episodeId: String) {
        deleted += episodeId
        _statuses.value = _statuses.value - episodeId
        done.value = done.value.filter { it.episode.id != episodeId }
    }

    override fun localPath(episodeId: String): String? =
        (_statuses.value[episodeId] as? DownloadStatus.Done)?.localPath

    /** Test helper */
    fun setStatus(episodeId: String, status: DownloadStatus) {
        _statuses.value = _statuses.value + (episodeId to status)
    }
}
