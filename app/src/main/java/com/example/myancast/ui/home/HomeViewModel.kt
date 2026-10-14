// ui/home/HomeViewModel.kt
package com.example.myancast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.data.repository.LibraryRepository
import com.example.myancast.data.repository.PodcastRepository
import com.example.myancast.data.network.AlwaysOnlineNetworkMonitor
import com.example.myancast.data.network.NetworkMonitor
import com.example.myancast.data.repository.PodcastRepositoryImpl
import com.example.myancast.domain.model.PlaybackProgress
import com.example.myancast.domain.model.Podcast
import com.example.myancast.player.PlayerController
import com.example.myancast.player.PlayerQueue
import com.example.myancast.ui.components.offlineErrorOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════
// UI STATE
// ═══════════════════════════════════════════════
data class HomeUiState(
    val allPodcasts: List<Podcast> = emptyList(),
    val podcasts: List<Podcast> = emptyList(),
    val trending: List<Podcast> = emptyList(),
    val categories: List<String> = listOf(ALL_CATEGORY),
    val selectedCategory: String = ALL_CATEGORY,
    /** ဆက်နားထောင်ရန် — history ကနေ (ပြီးသွားတာ ဆို null) */
    val lastPlayed: PlaybackProgress? = null,
    /** ခုလက်ရှိ **ဖွင့်နေတဲ့** episode ID — ရပ်ထားရင် null */
    val playingEpisodeId: String? = null,
    /** ဆက်ဖွင့်လို့ မရတဲ့အခါ snackbar နဲ့ ပြဖို့ (တိတ်တဆိတ် မရပ်သွားအောင်) */
    val resumeError: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /** ဆက်နားထောင်ရန် card မှာ ⏸ ပြမလား ▶ ပြမလား */
    val isLastPlayedPlaying: Boolean
        get() = lastPlayed != null && lastPlayed.episodeId == playingEpisodeId
}

// ═══════════════════════════════════════════════
// VIEWMODEL
// ═══════════════════════════════════════════════
class HomeViewModel(
    private val repo: PodcastRepository,
    private val libraryRepo: LibraryRepository,
    private val player: PlayerController,
    private val network: NetworkMonitor = AlwaysOnlineNetworkMonitor()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var lastPlayedJob: Job? = null
    private var playbackJob: Job? = null

    init {
        loadPodcasts()
        observeLastPlayed()
        observePlayback()
        observeNetwork()
    }

    // ─── Load ───────────────────────────────────
    private fun loadPodcasts() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            repo.getPodcasts()
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Data ဆွဲမရပါ"
                        )
                    }
                }
                .collect { list ->
                    _state.update { current ->
                        current.copy(
                            allPodcasts = list,
                            trending = list.take(5),
                            categories = buildCategories(list),
                            podcasts = filterByCategory(list, current.selectedCategory),
                            isLoading = false,
                            // ★ Phase 6 (#2) — offline + cache အလွတ် = error ("data မရှိ" မဟုတ်)
                            error = offlineErrorOrNull(list.isEmpty(), network.status.value.online)
                        )
                    }
                }
        }
    }

    /**
     * History ကနေ နောက်ဆုံး နားထောင်ခဲ့တာကို နားထောင်ထားတယ်။
     * ၉၅% ကျော် ပြီးသွားတာ ဆို `null` — "ပြီးသွားတာ" ကို ဆက်နားထောင်ခိုင်းလို့ မဖြစ်။
     * History မရလည်း Home မပျက်ရဘူး (podcast list က အဓိက)။
     */
    private fun observeLastPlayed() {
        lastPlayedJob?.cancel()

        lastPlayedJob = viewModelScope.launch {
            libraryRepo.lastPlayed()
                .catch { /* history ဆွဲမရလည်း Home ကို မထိခိုက်စေရ */ }
                .collect { progress ->
                    _state.update {
                        it.copy(lastPlayed = progress?.takeUnless { p -> p.isFinished })
                    }
                }
        }
    }

    /** ဖွင့်နေတဲ့ episode ဘာလဲ နားထောင်တယ် — card ရဲ့ ▶/⏸ icon အတွက် */
    private fun observePlayback() {
        playbackJob?.cancel()

        playbackJob = viewModelScope.launch {
            player.state
                .map { if (it.isPlaying) it.currentEpisode?.id else null }
                .distinctUntilChanged()
                .collect { episodeId ->
                    _state.update { it.copy(playingEpisodeId = episodeId) }
                }
        }
    }

    /** ★ Phase 6 (#2) — လိုင်း ပြန်ရလာရင် error/အလွတ် ဖြစ်နေတာကို အလိုအလျောက် ပြန်ဆွဲ */
    private fun observeNetwork() {
        viewModelScope.launch {
            network.status
                .map { it.online }
                .distinctUntilChanged()
                .drop(1)                      // စဖွင့်ချိန် တန်ဖိုး — ပြောင်းလဲမှု မဟုတ်
                .collect { online ->
                    val current = _state.value
                    if (online && (current.error != null || current.allPodcasts.isEmpty())) refresh()
                }
        }
    }

    // ─── Actions ────────────────────────────────

    /**
     * ဆက်နားထောင်ရန် card နှိပ်ချိန် — Full Player ဖွင့်ဖို့။
     * Player ထဲမှာ အဲဒီ episode ရှိပြီးသား ဆိုရင် **ပြန်မစဘူး** (ဖွင့်နေတာ မပျက်ရ) —
     * မရှိသေးရင်သာ queue လုပ်ပြီး ရပ်ထားတဲ့ နေရာကနေ စဖွင့်တယ်။
     * @return ဖွင့်စရာ ရှိရင် `true` (caller က Player screen ဖွင့်ဖို့)
     */
    fun resumeLastPlayed(): Boolean {
        val progress = _state.value.lastPlayed ?: return false
        if (!isLoadedInPlayer(progress.episodeId)) startFrom(progress)
        return true
    }

    /**
     * Card ရဲ့ ▶/⏸ ခလုတ် — ဖွင့်ထားပြီးသား ဆို ရပ်/ဖွင့် ပြောင်းရုံ၊
     * မဖွင့်ရသေးရင် ရပ်ထားတဲ့ နေရာကနေ စဖွင့်။
     */
    fun toggleLastPlayed() {
        val progress = _state.value.lastPlayed ?: return
        if (isLoadedInPlayer(progress.episodeId)) player.togglePlayPause() else startFrom(progress)
    }

    private fun isLoadedInPlayer(episodeId: String): Boolean =
        player.state.value.currentEpisode?.id == episodeId

    private fun startFrom(progress: PlaybackProgress) {
        viewModelScope.launch {
            val episodes = try {
                repo.getEpisodes(progress.podcastId).first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // တိတ်တဆိတ် မရပ်သွားရ — user ကို အကြောင်း ပြရမယ်
                _state.update { it.copy(resumeError = "ဆက်ဖွင့်လို့ မရပါ — အင်တာနက် ချိတ်ဆက်မှု စစ်ကြည့်ပါ") }
                return@launch
            }

            val queue = PlayerQueue.from(episodes, progress.episodeId)
            if (queue.isEmpty) {
                _state.update { it.copy(resumeError = "ဒီအပိုင်းကို ဖွင့်လို့ မရပါ") }
                return@launch
            }

            // ⚠️ play() ပြီးမှ seekTo() ခေါ်လို့ မရဘူး — app အသစ်ဖွင့်ချိန်မှာ service
            // မချိတ်ရသေးလို့ seek က ပျောက်ပြီး အစကနေ ပြန်စမိမယ်။ start position ကို play() ကိုပဲ ပေး။
            player.play(queue, progress.podcastTitle, progress.positionMs)
        }
    }

    /** Snackbar ပြပြီးရင် ခေါ် */
    fun resumeErrorShown() {
        _state.update { it.copy(resumeError = null) }
    }

    fun selectCategory(category: String) {
        _state.update {
            it.copy(
                selectedCategory = category,
                podcasts = filterByCategory(it.allPodcasts, category)
            )
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        loadPodcasts()
    }

    // ─── Factory ────────────────────────────────
    companion object {
        fun factory(
            repo: PodcastRepository = PodcastRepositoryImpl()
        ) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                HomeViewModel(repo, app.libraryRepository, app.playerController, app.networkMonitor)
            }
        }
    }
}