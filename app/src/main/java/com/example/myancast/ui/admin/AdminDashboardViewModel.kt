package com.example.myancast.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.data.admin.AdminAuthRepository
import com.example.myancast.data.repository.PodcastRepository
import com.example.myancast.data.repository.PodcastRepositoryImpl
import com.example.myancast.domain.model.Podcast
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val podcasts: List<Podcast> = emptyList(),
    val summary: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Sign out လုပ်ပြီး (သို့) session ကုန် → Settings ကို ပြန်သွား */
    val signedOut: Boolean = false
)

class AdminDashboardViewModel(
    private val repo: PodcastRepository,
    private val auth: AdminAuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardUiState())
    val state: StateFlow<AdminDashboardUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            auth.isAdmin.collect { isAdmin ->
                if (!isAdmin) _state.update { it.copy(signedOut = true) }
            }
        }
    }

    /** Firestore snapshot listener — admin သိမ်းတာနဲ့ (တခြား ဖုန်းကနေ ဖြစ်ဖြစ်) စာရင်း ချက်ချင်း ပြောင်း */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repo.getPodcasts()
                .catch { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Podcast စာရင်း ဆွဲမရပါ")
                    }
                }
                .collect { list ->
                    _state.update {
                        it.copy(
                            podcasts = sortPodcastsForAdmin(list),
                            summary = adminSummary(list),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun retry() {
        _state.update { it.copy(isLoading = true, error = null) }
        load()
    }

    fun signOut() {
        auth.signOut()
        _state.update { it.copy(signedOut = true) }
    }

    companion object {
        fun factory(repo: PodcastRepository = PodcastRepositoryImpl()) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                AdminDashboardViewModel(repo, app.adminAuth)
            }
        }
    }
}
