package com.example.myancast.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.data.admin.AdminContentRepository
import com.example.myancast.data.admin.MediaKind
import com.example.myancast.data.admin.MediaUploader
import com.example.myancast.data.admin.STORAGE_PODCASTS
import com.example.myancast.data.network.NetworkMonitor
import com.example.myancast.data.repository.PodcastRepository
import com.example.myancast.data.repository.PodcastRepositoryImpl
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import com.example.myancast.ui.navigation.Screen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminPodcastUiState(
    val podcastId: String = "",
    val isNew: Boolean = true,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val form: PodcastForm = PodcastForm(),
    /** နောက်ဆုံး သိမ်းထားတဲ့ form — ပြင်ထားတာ ရှိမရှိ (`isDirty`) နှိုင်းယှဉ်ဖို့ */
    val savedForm: PodcastForm = PodcastForm(),
    val errors: PodcastFormErrors = PodcastFormErrors(),
    val coverLoadFailed: Boolean = false,
    val categorySuggestions: List<String> = emptyList(),
    val episodes: List<Episode> = emptyList(),
    val episodesError: String? = null,
    val saveStatus: SaveStatus = SaveStatus.Idle,
    /** Snackbar (တစ်ခါ ပြပြီးရင် `messageShown()`) */
    val message: String? = null,
    /** ဖျက်ပြီး → Dashboard ကို ပြန် */
    val deleted: Boolean = false
) {
    val isDirty: Boolean get() = form != savedForm
    val isBusy: Boolean get() = saveStatus != SaveStatus.Idle
}

/**
 * Podcast အသစ် ထည့်/ပြင်/ဖျက် + သူ့ အပိုင်း စာရင်း။
 * @param podcastIdArg route ကနေ — [Screen.NEW_ID] ဆို အသစ်
 */
class AdminPodcastViewModel(
    podcastIdArg: String,
    private val repo: PodcastRepository,
    private val content: AdminContentRepository,
    private val uploader: MediaUploader,
    private val network: NetworkMonitor,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val startsNew = podcastIdArg == Screen.NEW_ID
    /** အသစ်ဆိုရင်လည်း ID ကို အခုကတည်းက ယူထား — Storage folder (`podcasts/<id>/`) အတွက် */
    private val podcastId = if (startsNew) content.newPodcastId() else podcastIdArg

    private val _state = MutableStateFlow(
        AdminPodcastUiState(podcastId = podcastId, isNew = startsNew, isLoading = !startsNew)
    )
    val state: StateFlow<AdminPodcastUiState> = _state.asStateFlow()

    /** Firestore ထဲက နောက်ဆုံး version — ပုံ ပြောင်းရင် ပုံဟောင်း ဖျက်ဖို့ */
    private var saved: Podcast? = null

    private var loadJob: Job? = null
    private var episodesJob: Job? = null
    private var saveJob: Job? = null

    init {
        loadCategorySuggestions()
        if (!startsNew) load()
    }

    // ──────── Load ────────

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val podcast = try {
                repo.getPodcast(podcastId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (podcast == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        loadError = "Podcast ကို ဖွင့်လို့ မရပါ — ဖျက်ထားပြီး (သို့) အင်တာနက် မရှိတာ ဖြစ်နိုင်ပါတယ်"
                    )
                }
                return@launch
            }
            saved = podcast
            val form = podcast.toForm()
            _state.update { it.copy(isLoading = false, loadError = null, form = form, savedForm = form) }
            observeEpisodes()
        }
    }

    fun retryLoad() {
        _state.update { it.copy(isLoading = true, loadError = null) }
        load()
    }

    private fun observeEpisodes() {
        episodesJob?.cancel()
        episodesJob = viewModelScope.launch {
            repo.getEpisodes(podcastId)
                .catch { _state.update { it.copy(episodesError = "အပိုင်း စာရင်း ဆွဲမရပါ") } }
                .collect { list -> _state.update { it.copy(episodes = list, episodesError = null) } }
        }
    }

    private fun loadCategorySuggestions() {
        viewModelScope.launch {
            val podcasts = try {
                repo.getPodcasts().first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return@launch   // အကြံပြုချက် မရလည်း ကိုယ်တိုင် ရိုက်လို့ရ
            }
            _state.update { it.copy(categorySuggestions = categorySuggestions(podcasts)) }
        }
    }

    // ──────── Form ────────

    fun onTitleChange(value: String) {
        updateForm { it.copy(title = value) }
        clearError { it.copy(title = null) }
    }

    fun onDescriptionChange(value: String) {
        updateForm { it.copy(description = value) }
    }

    fun onCategoryChange(value: String) {
        updateForm { it.copy(category = value) }
        clearError { it.copy(category = null) }
    }

    fun onCoverModeChange(mode: SourceMode) {
        updateForm { it.copy(cover = it.cover.copy(mode = mode)) }
        _state.update { it.copy(coverLoadFailed = false, errors = it.errors.copy(cover = null)) }
    }

    fun onCoverPicked(uri: String) {
        updateForm { it.copy(cover = it.cover.copy(mode = SourceMode.DEVICE, localUri = uri)) }
        clearError { it.copy(cover = null) }
    }

    fun onCoverUrlChange(value: String) {
        updateForm { it.copy(cover = it.cover.copy(urlText = value)) }
        _state.update { it.copy(coverLoadFailed = false, errors = it.errors.copy(cover = null)) }
    }

    fun onCoverUrlLoadResult(success: Boolean) {
        _state.update {
            it.copy(
                coverLoadFailed = !success,
                errors = if (success) it.errors.copy(cover = null) else it.errors
            )
        }
    }

    // ──────── Save ────────

    fun save() {
        val current = _state.value
        if (current.isBusy) return

        val errors = validatePodcastForm(current.form, current.coverLoadFailed)
        if (errors.hasAny) {
            _state.update { it.copy(errors = errors, message = FIX_ERRORS_MESSAGE) }
            return
        }
        if (!network.status.value.online) {
            _state.update { it.copy(message = OFFLINE_SAVE_MESSAGE) }
            return
        }

        saveJob = viewModelScope.launch {
            var uploadedUrl: String? = null
            try {
                val cover = uploader.resolveToUrl(
                    field = current.form.cover,
                    kind = MediaKind.IMAGE,
                    folder = STORAGE_PODCASTS,
                    ownerId = podcastId,
                    nowMillis = now(),
                    progressLabel = "ပုံ တင်နေပါတယ်",
                    onProgress = { status -> _state.update { it.copy(saveStatus = status) } }
                )
                if (cover.uploaded) uploadedUrl = cover.url

                _state.update { it.copy(saveStatus = SaveStatus.Saving) }
                val podcast = Podcast(
                    id = podcastId,
                    title = current.form.title.trim(),
                    description = current.form.description.trim(),
                    coverUrl = cover.url,
                    category = current.form.category.trim(),
                    episodeCount = saved?.episodeCount ?: 0,
                    updatedAt = saved?.updatedAt
                )
                content.savePodcast(podcast, isNew = current.isNew)

                // ပုံ အသစ် ပြောင်းလိုက်ရင် Storage ထဲက ပုံဟောင်း ဖျက် (အင်တာနက် လင့်ခ်ဆို ဘာမှ မလုပ်)
                val oldCover = saved?.coverUrl.orEmpty()
                if (oldCover.isNotBlank() && oldCover != cover.url) uploader.deleteIfOwned(oldCover)

                saved = podcast
                val savedForm = podcast.toForm()
                _state.update {
                    it.copy(
                        form = savedForm,
                        savedForm = savedForm,
                        isNew = false,
                        saveStatus = SaveStatus.Idle,
                        message = if (current.isNew) "Podcast ထည့်ပြီးပါပြီ — အပိုင်းတွေ ဆက်ထည့်လို့ရပါပြီ"
                        else "သိမ်းပြီးပါပြီ"
                    )
                }
                if (current.isNew) observeEpisodes()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Firestore မှာ မသိမ်းနိုင်ရင် ခုနက တင်လိုက်တဲ့ ပုံ မကျန်ခဲ့အောင်
                uploadedUrl?.let { uploader.deleteIfOwned(it) }
                _state.update { it.copy(saveStatus = SaveStatus.Idle, message = e.adminMessage()) }
            }
        }
    }

    // ──────── Delete ────────

    fun delete() {
        val current = _state.value
        if (current.isBusy || current.isNew) return
        if (!network.status.value.online) {
            _state.update { it.copy(message = OFFLINE_SAVE_MESSAGE) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saveStatus = SaveStatus.Deleting) }
            try {
                content.deletePodcast(podcastId)
                // Storage ထဲက ပုံ/MP3 များ — ဒီ app တင်ထားတာဆိုမှ ဖျက်
                val media = listOf(saved?.coverUrl.orEmpty()) +
                    current.episodes.flatMap { listOf(it.audioUrl, it.coverUrl) }
                media.filter { it.isNotBlank() }.distinct().forEach { uploader.deleteIfOwned(it) }
                _state.update { it.copy(saveStatus = SaveStatus.Idle, deleted = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(saveStatus = SaveStatus.Idle, message = e.adminMessage("ဖျက်လို့ မရပါ — ပြန်ကြိုးစားကြည့်ပါ"))
                }
            }
        }
    }

    fun messageShown() {
        _state.update { it.copy(message = null) }
    }

    /** Episode screen ကနေ ပြန်လာတဲ့ message (SavedStateHandle) */
    fun showMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    // ──────── Helpers ────────

    private fun updateForm(transform: (PodcastForm) -> PodcastForm) {
        _state.update { it.copy(form = transform(it.form)) }
    }

    private fun clearError(transform: (PodcastFormErrors) -> PodcastFormErrors) {
        _state.update { it.copy(errors = transform(it.errors)) }
    }

    companion object {
        fun factory(
            podcastId: String,
            repo: PodcastRepository = PodcastRepositoryImpl()
        ) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                AdminPodcastViewModel(
                    podcastIdArg = podcastId,
                    repo = repo,
                    content = app.adminContent,
                    uploader = app.mediaUploader,
                    network = app.networkMonitor
                )
            }
        }
    }
}

private fun Podcast.toForm() = PodcastForm(
    title = title,
    description = description,
    category = category,
    cover = MediaField.fromSavedUrl(coverUrl, emptyMode = SourceMode.DEVICE)
)
