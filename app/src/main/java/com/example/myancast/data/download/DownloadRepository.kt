package com.example.myancast.data.download

import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/** ★ Phase 6 Contract (#4) — ViewModel က WorkManager/Room/File ကို မသိဘူး */
interface DownloadRepository {
    /** episodeId → status · ဒေါင်းမထားရင် map ထဲ မပါ */
    val statuses: StateFlow<Map<String, DownloadStatus>>

    fun downloadedEpisodes(): Flow<List<DownloadedEpisode>>

    suspend fun enqueue(episode: Episode, podcastTitle: String, wifiOnly: Boolean)

    /** ဒေါင်းနေတာ ရပ် (သို့) ဒေါင်းပြီးသား ဖိုင် ဖျက် */
    suspend fun delete(episodeId: String)

    /** Player အတွက် (main thread · sync) — ဒေါင်းပြီးသား ဖိုင် path · မရှိရင် null */
    fun localPath(episodeId: String): String?
}

/** Test / Preview အတွက် — ဒေါင်းလုဒ် မရှိ */
class NoDownloadRepository : DownloadRepository {
    override val statuses: StateFlow<Map<String, DownloadStatus>> =
        MutableStateFlow(emptyMap<String, DownloadStatus>()).asStateFlow()

    override fun downloadedEpisodes(): Flow<List<DownloadedEpisode>> = flowOf(emptyList())
    override suspend fun enqueue(episode: Episode, podcastTitle: String, wifiOnly: Boolean) = Unit
    override suspend fun delete(episodeId: String) = Unit
    override fun localPath(episodeId: String): String? = null
}
