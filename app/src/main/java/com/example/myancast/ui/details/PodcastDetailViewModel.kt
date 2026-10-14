package com.example.myancast.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.data.download.DownloadRepository
import com.example.myancast.data.download.NoDownloadRepository
import com.example.myancast.data.network.AlwaysOnlineNetworkMonitor
import com.example.myancast.data.network.NetworkMonitor
import com.example.myancast.data.repository.LibraryRepository
import com.example.myancast.data.repository.PodcastRepository
import com.example.myancast.data.repository.PodcastRepositoryImpl
import com.example.myancast.domain.model.AppSettings
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import com.example.myancast.player.PlayerController
import com.example.myancast.domain.util.estimatedSizeBytes
import com.example.myancast.domain.util.shouldConfirmMobileStream
import com.example.myancast.player.PlayerQueue
import com.example.myancast.ui.components.DownloadButtonState
import com.example.myancast.ui.components.downloadButtonState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// ─────────────────────────────────────────────
// ၁။ UI State
// ─────────────────────────────────────────────

data class PodcastDetailUiState(
    val podcast  : Podcast?      = null,
    val episodes : List<Episode> = emptyList(),
    val isLoading: Boolean       = true,
    val error    : String?       = null,
    val canRetry : Boolean       = false,
    val playError: String?       = null,
    // ★ Phase 6 (#4, #5)
    /** Data ချွေတာ + mobile data — "ဖွင့်မှာလား" dialog */
    val pendingStream: PendingStream? = null,
    /** ဒေါင်းပြီးသား ဖိုင် ဖျက်ခါနီး မေး */
    val confirmDeleteDownload: Episode? = null,
    /** ဒေါင်းလုဒ် အကြောင်း snackbar */
    val message: String? = null
) {
    val hasContent: Boolean
        get() = podcast != null || episodes.isNotEmpty()
}


/** @param episodeId null = "အားလုံး ဖွင့်" · @param sizeBytes dialog မှာ ပြမယ့် ခန့်မှန်း data */
data class PendingStream(val episodeId: String?, val sizeBytes: Long)


// ─────────────────────────────────────────────
// ၂။ ViewModel
// ─────────────────────────────────────────────

class PodcastDetailViewModel(
    private val podcastId: String,
    private val repo: PodcastRepository,
    private val player: PlayerController,
    private val libraryRepo: LibraryRepository,
    // ★ Phase 6 — default ပါလို့ ရှိပြီးသား test တွေ မပြင်ရ
    private val downloads: DownloadRepository = NoDownloadRepository(),
    private val network: NetworkMonitor = AlwaysOnlineNetworkMonitor(),
    private val settings: StateFlow<AppSettings> = MutableStateFlow(AppSettings())
) : ViewModel() {

    // ──────── State ────────

    private val _state = MutableStateFlow(PodcastDetailUiState())
    val state: StateFlow<PodcastDetailUiState> = _state.asStateFlow()

    val nowPlayingId: StateFlow<String?> = player.state
        .map { if (it.isPlaying) it.currentEpisode?.id else null }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ★ အသစ် — isSubscribed (Flow)
    val isSubscribed: StateFlow<Boolean> = libraryRepo.subscribedIds()
        .map { podcastId in it }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    /** ★ Phase 6 (#4) — episodeId → ⬇ / Wi-Fi စောင့် / % / ✓ / ⚠ */
    val downloadStates: StateFlow<Map<String, DownloadButtonState>> =
        combine(downloads.statuses, network.status) { statuses, net ->
            statuses.mapValues { (_, status) -> downloadButtonState(status, net) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ──────── Job Management ────────

    private var loadJob: Job? = null

    // ──────── Init ────────

    init {
        load()
    }

    // ──────── Public API ────────

    fun retry() {
        _state.update { it.copy(isLoading = true, error = null, canRetry = false) }
        load()
    }

    /**
     * @param confirmed Data ချွေတာ dialog မှာ "ဖွင့်မည်" နှိပ်ပြီးပြီ
     * @return Player screen ဖွင့်ရမလား (dialog ပြနေရင် false)
     */
    fun playEpisode(episodeId: String, confirmed: Boolean = false): Boolean {
        val queue = PlayerQueue.from(_state.value.episodes, episodeId)
        if (queue.isEmpty) {
            _state.update { it.copy(playError = "ဒီအပိုင်းကို ဖွင့်လို့ မရပါ") }
            return false
        }
        val episode = queue.episodes[queue.startIndex]
        if (!confirmed && needsStreamConfirm(episode)) {
            _state.update { it.copy(pendingStream = PendingStream(episodeId, episode.estimatedSizeBytes())) }
            return false
        }
        player.play(queue, _state.value.podcast?.title.orEmpty())
        return true
    }

    fun playAll(confirmed: Boolean = false): Boolean {
        val queue = PlayerQueue.from(_state.value.episodes, null)
        if (queue.isEmpty) {
            _state.update { it.copy(playError = "ဖွင့်လို့ရတဲ့ အပိုင်း မရှိပါ") }
            return false
        }
        val first = queue.episodes[queue.startIndex]
        if (!confirmed && needsStreamConfirm(first)) {
            _state.update { it.copy(pendingStream = PendingStream(null, first.estimatedSizeBytes())) }
            return false
        }
        player.play(queue, _state.value.podcast?.title.orEmpty())
        return true
    }

    /** Dialog ရဲ့ "ဖွင့်မည်" — @return Player screen ဖွင့်ရမလား */
    fun confirmStream(): Boolean {
        val pending = _state.value.pendingStream ?: return false
        _state.update { it.copy(pendingStream = null) }
        return pending.episodeId?.let { playEpisode(it, confirmed = true) } ?: playAll(confirmed = true)
    }

    fun dismissStream() {
        _state.update { it.copy(pendingStream = null) }
    }

    private fun needsStreamConfirm(episode: Episode): Boolean = shouldConfirmMobileStream(
        dataSaver = settings.value.dataSaver,
        metered = network.status.value.metered,
        downloaded = downloads.localPath(episode.id) != null
    )

    // ──────── ★ Phase 6 (#4) — Download ────────

    fun onDownloadClick(episodeId: String) {
        val episode = _state.value.episodes.find { it.id == episodeId } ?: return
        val net = network.status.value
        when (downloadButtonState(downloads.statuses.value[episodeId], net)) {
            DownloadButtonState.NotDownloaded, DownloadButtonState.Failed -> viewModelScope.launch {
                val wifiOnly = settings.value.wifiOnlyDownload
                downloads.enqueue(episode, _state.value.podcast?.title.orEmpty(), wifiOnly)
                if (wifiOnly && (net.metered || !net.online)) {
                    _state.update { it.copy(message = "Wi-Fi ရရင် ဒေါင်းပါမယ်") }
                }
            }
            DownloadButtonState.WaitingForWifi,
            DownloadButtonState.Queued,
            is DownloadButtonState.Running -> viewModelScope.launch {
                downloads.delete(episodeId)
                _state.update { it.copy(message = "ဒေါင်းလုဒ်ကို ရပ်လိုက်ပါပြီ") }
            }
            DownloadButtonState.Done -> _state.update { it.copy(confirmDeleteDownload = episode) }
        }
    }

    fun confirmDeleteDownload() {
        val episode = _state.value.confirmDeleteDownload ?: return
        _state.update { it.copy(confirmDeleteDownload = null) }
        viewModelScope.launch {
            downloads.delete(episode.id)
            _state.update { it.copy(message = "ဒေါင်းလုဒ်ကို ဖျက်ပြီးပါပြီ") }
        }
    }

    fun dismissDeleteDownload() {
        _state.update { it.copy(confirmDeleteDownload = null) }
    }

    fun messageShown() {
        _state.update { it.copy(message = null) }
    }

    fun playErrorShown() {
        _state.update { it.copy(playError = null) }
    }

    // ★ အသစ် — Subscribe toggle
    fun toggleSubscribe() {
        viewModelScope.launch {
            if (isSubscribed.value) {
                libraryRepo.unsubscribe(podcastId)
            } else {
                libraryRepo.subscribe(podcastId)
            }
        }
    }

    // ──────── Private ────────

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (loadPodcast()) loadEpisodes()
        }
    }

    private suspend fun loadPodcast(): Boolean {
        val podcast = try {
            repo.getPodcast(podcastId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "အင်တာနက် ချိတ်ဆက်မှု မရပါ",
                    canRetry = true
                )
            }
            return false
        }

        if (podcast == null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Podcast ရှာမတွေ့ပါ",
                    canRetry = false
                )
            }
            return false
        }

        _state.update { it.copy(podcast = podcast) }
        return true
    }

    private suspend fun loadEpisodes() {
        repo.getEpisodes(podcastId)
            .catch { _ ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "ပိုင်းများ ဆွဲရာမှာ အမှားဖြစ်ခဲ့သည်",
                        canRetry = true
                    )
                }
            }
            .collect { episodes ->
                val cover = _state.value.podcast?.coverUrl.orEmpty()
                val withCover = episodes.map { ep ->
                    if (ep.coverUrl.isBlank()) ep.copy(coverUrl = cover)
                    else ep
                }
                _state.update {
                    it.copy(
                        episodes = withCover,
                        isLoading = false,
                        error = null,
                        canRetry = false
                    )
                }
            }
    }

    // ──────── Factory ────────

    companion object {
        fun factory(
            podcastId: String,
            repo: PodcastRepository = PodcastRepositoryImpl()
        ) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                PodcastDetailViewModel(
                    podcastId,
                    repo,
                    app.playerController,
                    app.libraryRepository,    // ← ★ ဒါ ထည့်
                    app.downloadRepository,   // ★ Phase 6
                    app.networkMonitor,
                    app.settingsRepository.settings
                )
            }
        }
    }
}