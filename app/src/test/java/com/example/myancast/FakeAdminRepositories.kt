package com.example.myancast

import com.example.myancast.data.admin.AdminAuthRepository
import com.example.myancast.data.admin.AdminConfig
import com.example.myancast.data.admin.AdminContentRepository
import com.example.myancast.data.admin.AdminException
import com.example.myancast.data.admin.AdminSignInResult
import com.example.myancast.data.admin.AudioInspector
import com.example.myancast.data.admin.MediaKind
import com.example.myancast.data.admin.MediaUploader
import com.example.myancast.data.admin.UploadEvent
import com.example.myancast.data.network.NetworkMonitor
import com.example.myancast.data.network.NetworkStatus
import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.MediaInput
import com.example.myancast.domain.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

// ★ Phase 6 (Admin) — ViewModel test အတွက် fake များ

class FakeAdminAuthRepository(
    signedIn: Boolean = false,
    private val password: String = "secret",
    /** null မဟုတ်ရင် စကားဝှက် မှန်လည်း ဒီ result ပဲ ပြန် (network error စမ်းဖို့) */
    var forcedResult: AdminSignInResult? = null
) : AdminAuthRepository {

    private val _isAdmin = MutableStateFlow(signedIn)
    override val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    var signInCalls = 0
        private set

    override suspend fun signIn(username: String, password: String): AdminSignInResult {
        signInCalls++
        forcedResult?.let { return it }
        val ok = AdminConfig.isAdminUsername(username) && password == this.password
        _isAdmin.value = ok
        return if (ok) AdminSignInResult.SUCCESS else AdminSignInResult.WRONG_CREDENTIALS
    }

    override fun signOut() {
        _isAdmin.value = false
    }
}

class FakeAdminContentRepository(
    private val episodes: MutableMap<String, Episode> = mutableMapOf()
) : AdminContentRepository {

    val savedPodcasts = mutableListOf<Pair<Podcast, Boolean>>()
    val savedEpisodes = mutableListOf<Pair<Episode, Boolean>>()
    val deletedPodcastIds = mutableListOf<String>()
    val deletedEpisodes = mutableListOf<Episode>()

    /** null မဟုတ်ရင် save/delete တိုင်း ဒီ error ပစ် */
    var failWith: AdminException? = null
    private var idCounter = 0

    override fun newPodcastId(): String = "newPodcast${++idCounter}"
    override fun newEpisodeId(): String = "newEpisode${++idCounter}"

    override suspend fun getEpisode(episodeId: String): Episode? = episodes[episodeId]

    override suspend fun savePodcast(podcast: Podcast, isNew: Boolean) {
        failWith?.let { throw it }
        savedPodcasts += podcast to isNew
    }

    override suspend fun deletePodcast(podcastId: String) {
        failWith?.let { throw it }
        deletedPodcastIds += podcastId
    }

    override suspend fun saveEpisode(episode: Episode, isNew: Boolean) {
        failWith?.let { throw it }
        savedEpisodes += episode to isNew
        episodes[episode.id] = episode
    }

    override suspend fun deleteEpisode(episode: Episode) {
        failWith?.let { throw it }
        deletedEpisodes += episode
        episodes.remove(episode.id)
    }
}

class FakeMediaUploader : MediaUploader {
    val uploads = mutableListOf<Triple<String, MediaKind, String>>()
    val deleted = mutableListOf<String>()
    var failWith: AdminException? = null

    override fun upload(localUri: String, kind: MediaKind, storagePath: String): Flow<UploadEvent> = flow {
        failWith?.let { throw it }
        uploads += Triple(localUri, kind, storagePath)
        emit(UploadEvent.Progress(0.5f))
        emit(UploadEvent.Done("https://firebasestorage.googleapis.com/v0/b/test/o/${storagePath.replace("/", "%2F")}"))
    }

    override suspend fun deleteIfOwned(url: String) {
        deleted += url
    }
}

class FakeAudioInspector(
    /** input → ရလဒ် · map ထဲ မပါရင် [default] */
    private val results: Map<MediaInput, AudioInfo?> = emptyMap(),
    private val default: AudioInfo? = AudioInfo(durationSeconds = 1470, sizeBytes = 11_760_000L, isMp3 = true, displayName = "ep.mp3")
) : AudioInspector {
    val inspected = mutableListOf<MediaInput>()

    override suspend fun inspect(input: MediaInput): AudioInfo? {
        inspected += input
        return if (results.containsKey(input)) results[input] else default
    }
}

class FakeNetworkMonitor(online: Boolean = true, metered: Boolean = false) : NetworkMonitor {
    private val _status = MutableStateFlow(NetworkStatus(online, metered))
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    fun set(online: Boolean, metered: Boolean = false) {
        _status.value = NetworkStatus(online, metered)
    }
}
