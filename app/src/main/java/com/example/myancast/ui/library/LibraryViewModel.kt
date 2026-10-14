package com.example.myancast.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.data.download.DownloadRepository
import com.example.myancast.data.download.NoDownloadRepository
import com.example.myancast.data.download.totalDownloadBytes
import com.example.myancast.data.repository.LibraryRepository
import com.example.myancast.data.repository.PodcastRepository
import com.example.myancast.domain.util.formatMegabytes
import com.example.myancast.player.PlayerController
import com.example.myancast.player.PlayerQueue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val libraryRepo: LibraryRepository,
    private val podcastRepo: PodcastRepository,
    // ★ Phase 6 (#4) — default ပါလို့ ရှိပြီးသား test တွေ မပြင်ရ
    private val downloads: DownloadRepository = NoDownloadRepository(),
    /** ဒေါင်းထားတာ ဖွင့်ဖို့ · null = ဖွင့်လို့ မရ (test) */
    private val player: PlayerController? = null
) : ViewModel() {

    private val _tab = MutableStateFlow(LibraryTab.SUBSCRIBED)
    val tab: StateFlow<LibraryTab> = _tab.asStateFlow()

    /**
     * `catch` ပြီးရင် combine flow က ဆုံးသွားတယ် — ပြန်မစရင် error ကနေ ဘယ်တော့မှ မထွက်ဘူး။
     * တန်ဖိုး ပြောင်းတိုင်း flow ကို အသစ်ကနေ ပြန်စ (`retry()`)။
     */
    private val reload = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LibraryUiState> = reload
        .flatMapLatest { libraryFlow() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(isLoading = true)
        )

    fun selectTab(tab: LibraryTab) {
        _tab.update { tab }
    }

    fun retry() {
        reload.update { it + 1 }
    }

    /** ★ Phase 6 (#4) — @return Player screen ဖွင့်ရမလား */
    fun playDownloaded(episodeId: String): Boolean {
        val items = state.value.downloads
        val queue = PlayerQueue.from(downloadedQueueFor(items, episodeId), episodeId)
        val podcastTitle = items.find { it.episode.id == episodeId }?.podcastTitle.orEmpty()
        val p = player ?: return false
        if (queue.isEmpty) return false
        p.play(queue, podcastTitle)
        return true
    }

    fun deleteDownload(episodeId: String) {
        viewModelScope.launch { downloads.delete(episodeId) }
    }

    private fun libraryFlow(): Flow<LibraryUiState> = combine(
        _tab,
        podcastRepo.getPodcasts(),
        libraryRepo.subscribedIds(),
        libraryRepo.history(),
        downloads.downloadedEpisodes()
    ) { tab, podcasts, subscribedIds, history, downloaded ->
        LibraryUiState(
            tab = tab,
            subscribed = podcasts.filter { it.id in subscribedIds },
            history = history.map { progress ->
                HistoryRow(
                    episodeId = progress.episodeId,
                    episodeTitle = progress.episodeTitle,
                    podcastId = progress.podcastId,
                    podcastTitle = progress.podcastTitle,
                    coverUrl = progress.coverUrl,
                    progress = progress
                )
            },
            downloads = downloaded,
            downloadsTotal = formatMegabytes(totalDownloadBytes(downloaded)),
            isLoading = false,
            error = null
        )
    }
        .catch { e ->
            emit(
                LibraryUiState(
                    isLoading = false,
                    error = e.message ?: "Data ဆွဲမရပါ"
                )
            )
        }
        .onStart { emit(LibraryUiState(tab = _tab.value, isLoading = true)) }

    companion object {
        fun factory(
            libraryRepo: LibraryRepository,
            podcastRepo: PodcastRepository,
            downloads: DownloadRepository = NoDownloadRepository(),
            player: PlayerController? = null
        ) = viewModelFactory {
            initializer { LibraryViewModel(libraryRepo, podcastRepo, downloads, player) }
        }
    }
}