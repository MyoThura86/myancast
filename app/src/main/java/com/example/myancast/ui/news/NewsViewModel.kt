// ui/news/NewsViewModel.kt
package com.example.myancast.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.data.network.AlwaysOnlineNetworkMonitor
import com.example.myancast.data.network.NetworkMonitor
import com.example.myancast.data.repository.FirestoreNewsRepository
import com.example.myancast.data.repository.NewsRepository
import com.example.myancast.domain.model.NewsItem
import com.example.myancast.ui.components.offlineErrorOrNull
import com.example.myancast.ui.home.ALL_CATEGORY
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════
// UI STATE
// ═══════════════════════════════════════════════
data class NewsUiState(
    val allNews: List<NewsItem> = emptyList(),
    val news: List<NewsItem> = emptyList(),          // category filter ပြီးသား
    val categories: List<String> = listOf(ALL_CATEGORY),
    val selectedCategory: String = ALL_CATEGORY,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /** ထိပ်ဆုံး (အသစ်ဆုံး) သတင်းကို ကြီးကြီး ပြဖို့ — filter ပြီးတဲ့ list ထဲကပဲ */
    val featured: NewsItem? get() = news.firstOrNull()

    /** Featured ကို နှစ်ခါ မပြအောင် ဖယ်ထား */
    val rest: List<NewsItem> get() = news.drop(1)
}

// ═══════════════════════════════════════════════
// VIEWMODEL
// ═══════════════════════════════════════════════
class NewsViewModel(
    private val repo: NewsRepository,
    private val network: NetworkMonitor = AlwaysOnlineNetworkMonitor()
) : ViewModel() {

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadNews()
        observeNetwork()
    }

    // ─── Load ───────────────────────────────────
    private fun loadNews() {
        loadJob?.cancel()          // ← Phase 2 ရဲ့ bug — refresh နှိပ်တိုင်း collector မပွားအောင်

        loadJob = viewModelScope.launch {
            repo.getNews()
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "သတင်း ဆွဲမရပါ"
                        )
                    }
                }
                .collect { list ->
                    _state.update { current ->
                        current.copy(
                            allNews = list,
                            categories = buildNewsCategories(list),
                            news = filterNews(list, current.selectedCategory),
                            isLoading = false,
                            // ★ Phase 6 (#2) — offline + cache အလွတ် = error
                            error = offlineErrorOrNull(list.isEmpty(), network.status.value.online)
                        )
                    }
                }
        }
    }

    /** ★ Phase 6 (#2) — လိုင်း ပြန်ရလာရင် အလိုအလျောက် ပြန်ဆွဲ */
    private fun observeNetwork() {
        viewModelScope.launch {
            network.status
                .map { it.online }
                .distinctUntilChanged()
                .drop(1)
                .collect { online ->
                    val current = _state.value
                    if (online && (current.error != null || current.allNews.isEmpty())) refresh()
                }
        }
    }

    // ─── Actions ────────────────────────────────
    fun selectCategory(category: String) {
        _state.update {
            it.copy(
                selectedCategory = category,
                news = filterNews(it.allNews, category)
            )
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        loadNews()
    }

    // ─── Factory ────────────────────────────────
    companion object {
        fun factory(
            repo: NewsRepository = FirestoreNewsRepository()
        ) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                NewsViewModel(repo, app.networkMonitor)
            }
        }
    }
}
