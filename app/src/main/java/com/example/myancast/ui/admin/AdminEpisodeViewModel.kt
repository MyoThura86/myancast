package com.example.myancast.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myancast.MyanCastApp
import com.example.myancast.data.admin.AdminContentRepository
import com.example.myancast.data.admin.AudioInspector
import com.example.myancast.data.admin.MediaKind
import com.example.myancast.data.admin.MediaUploader
import com.example.myancast.data.admin.STORAGE_EPISODES
import com.example.myancast.data.network.NetworkMonitor
import com.example.myancast.data.repository.PodcastRepository
import com.example.myancast.data.repository.PodcastRepositoryImpl
import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.MediaInput
import com.example.myancast.domain.util.combineDateAndTime
import com.example.myancast.domain.util.formatDurationInput
import com.example.myancast.domain.util.hourAndMinute
import com.example.myancast.domain.util.isValidWebUrl
import com.example.myancast.domain.util.parseDurationInput
import com.example.myancast.domain.util.toDatePickerMillis
import com.example.myancast.ui.navigation.Screen
import com.google.firebase.Timestamp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class AdminEpisodeUiState(
    val podcastId: String = "",
    val episodeId: String = "",
    val isNew: Boolean = true,
    val podcastTitle: String = "",
    val podcastCoverUrl: String = "",
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val form: EpisodeForm = EpisodeForm(),
    val savedForm: EpisodeForm = EpisodeForm(),
    val errors: EpisodeFormErrors = EpisodeFormErrors(),
    val audioCheck: AudioCheck = AudioCheck.Idle,
    val coverLoadFailed: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val message: String? = null,
    /** သိမ်း/ဖျက် ပြီး → Podcast screen ကို ပြန်ပြီး ဒီ message ကို snackbar ပြ */
    val finishedMessage: String? = null
) {
    val isDirty: Boolean get() = form != savedForm
    val isBusy: Boolean get() = saveStatus != SaveStatus.Idle || audioCheck == AudioCheck.Checking
}

/**
 * အပိုင်း အသစ် ထည့်/ပြင်/ဖျက်။ MP3 ကို ရွေးတာနဲ့ စစ်ပြီး ကြာချိန်/အရွယ်ကို အလိုအလျောက် ဖြည့်တယ်။
 * @param episodeIdArg route ကနေ — [Screen.NEW_ID] ဆို အသစ်
 */
class AdminEpisodeViewModel(
    private val podcastId: String,
    episodeIdArg: String,
    private val repo: PodcastRepository,
    private val content: AdminContentRepository,
    private val uploader: MediaUploader,
    private val inspector: AudioInspector,
    private val network: NetworkMonitor,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val startsNew = episodeIdArg == Screen.NEW_ID
    private val episodeId = if (startsNew) content.newEpisodeId() else episodeIdArg

    private val _state = MutableStateFlow(
        AdminEpisodeUiState(podcastId = podcastId, episodeId = episodeId, isNew = startsNew)
    )
    val state: StateFlow<AdminEpisodeUiState> = _state.asStateFlow()

    private var saved: Episode? = null

    /** စစ်ပြီးသား အသံ input — mode ပြောင်းပြီး ပြန်လာရင် ထပ်မစစ်ရအောင် */
    private var verified: Pair<MediaInput, AudioInfo>? = null

    /** Admin က ကြာချိန်ကို ကိုယ်တိုင် ပြင်ပြီးရင် MP3 အသစ် ရွေးလည်း မဖုံးအုပ်ဘူး */
    private var durationEditedByUser = false

    init {
        load()
    }

    // ──────── Load ────────

    private fun load() {
        viewModelScope.launch {
            try {
                val podcast = repo.getPodcast(podcastId)
                if (podcast == null) {
                    _state.update { it.copy(isLoading = false, loadError = LOAD_ERROR) }
                    return@launch
                }
                val episode = if (startsNew) null else content.getEpisode(episodeId)
                if (!startsNew && episode == null) {
                    _state.update { it.copy(isLoading = false, loadError = "ဒီအပိုင်းကို ရှာမတွေ့ပါ — ဖျက်ထားပြီး ဖြစ်နိုင်ပါတယ်") }
                    return@launch
                }
                saved = episode

                val form = episode?.toForm() ?: EpisodeForm(publishedAtMillis = now())
                // Firestore ထဲက audioUrl က တင်ပြီးသား/စစ်ပြီးသား — ထပ်စစ်စရာ မလို
                val check = episode?.takeIf { it.audioUrl.isNotBlank() }?.let { ep ->
                    val info = AudioInfo(ep.duration, ep.fileSizeBytes, isMp3 = true)
                    verified = form.audio.toInput() to info
                    AudioCheck.Ok(info)
                } ?: AudioCheck.Idle

                _state.update {
                    it.copy(
                        isLoading = false,
                        loadError = null,
                        podcastTitle = podcast.title,
                        podcastCoverUrl = podcast.coverUrl,
                        form = form,
                        savedForm = form,
                        audioCheck = check
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, loadError = e.adminMessage(LOAD_ERROR)) }
            }
        }
    }

    fun retryLoad() {
        _state.update { it.copy(isLoading = true, loadError = null) }
        load()
    }

    // ──────── Text fields ────────

    fun onTitleChange(value: String) {
        updateForm { it.copy(title = value) }
        _state.update { it.copy(errors = it.errors.copy(title = null)) }
    }

    fun onDescriptionChange(value: String) {
        updateForm { it.copy(description = value) }
    }

    fun onDurationChange(value: String) {
        durationEditedByUser = true
        updateForm { it.copy(durationText = value) }
        _state.update { it.copy(errors = it.errors.copy(duration = null)) }
    }

    /** DatePicker — ရက် ပြောင်းရင် အချိန် (နာရီ/မိနစ်) ကို မပြောင်း */
    fun onPublishDateChange(dateUtcMillis: Long) {
        updateForm {
            val (hour, minute) = hourAndMinute(it.publishedAtMillis)
            it.copy(publishedAtMillis = combineDateAndTime(dateUtcMillis, hour, minute))
        }
    }

    /** TimePicker — အချိန် ပြောင်းရင် ရက် ကို မပြောင်း */
    fun onPublishTimeChange(hour: Int, minute: Int) {
        updateForm {
            it.copy(publishedAtMillis = combineDateAndTime(toDatePickerMillis(it.publishedAtMillis), hour, minute))
        }
    }

    // ──────── Audio ────────

    fun onAudioModeChange(mode: SourceMode) {
        updateForm { it.copy(audio = it.audio.copy(mode = mode)) }
        refreshAudioCheck()
    }

    /** ဖုန်းထဲက MP3 ရွေးပြီး — ချက်ချင်း စစ် */
    fun onAudioPicked(uri: String) {
        updateForm { it.copy(audio = it.audio.copy(mode = SourceMode.DEVICE, localUri = uri, localName = "")) }
        refreshAudioCheck()
        if (_state.value.audioCheck !is AudioCheck.Ok) {
            viewModelScope.launch { inspectAudio(MediaInput.Local(uri)) }
        }
    }

    fun onAudioUrlChange(value: String) {
        updateForm { it.copy(audio = it.audio.copy(urlText = value)) }
        refreshAudioCheck()
    }

    /** "စစ်ဆေးမည်" ခလုတ် */
    fun verifyAudioUrl() {
        val input = _state.value.form.audio.toInput()
        if (input !is MediaInput.Remote || !isValidWebUrl(input.url)) {
            _state.update { it.copy(errors = it.errors.copy(audio = "လင့်ခ် မှန်မှန် ထည့်ပါ (https:// နဲ့ စရပါမယ်)")) }
            return
        }
        viewModelScope.launch { inspectAudio(input) }
    }

    /** Input ပြောင်းသွားရင် စစ်ပြီးသား ရလဒ်က အဟောင်းအတွက်ပဲ — ကိုက်မှ Ok ပြ */
    private fun refreshAudioCheck() {
        val input = _state.value.form.audio.toInput()
        val match = verified?.takeIf { it.first == input }
        _state.update {
            it.copy(
                audioCheck = match?.let { (_, info) -> AudioCheck.Ok(info) } ?: AudioCheck.Idle,
                errors = it.errors.copy(audio = null)
            )
        }
    }

    /** @return MP3 မှန်ရင် `true` */
    private suspend fun inspectAudio(input: MediaInput): Boolean {
        _state.update { it.copy(audioCheck = AudioCheck.Checking, errors = it.errors.copy(audio = null)) }
        val info = inspector.inspect(input)

        // စစ်နေတုန်း admin က တခြား ဖိုင်/လင့်ခ် ပြောင်းသွားရင် ဒီရလဒ်ကို မသုံး
        if (_state.value.form.audio.toInput() != input) return false

        return when {
            info == null -> {
                _state.update { it.copy(audioCheck = AudioCheck.Failed(AUDIO_UNREADABLE_ERROR)) }
                false
            }
            !info.isMp3 -> {
                _state.update { it.copy(audioCheck = AudioCheck.Failed(NOT_MP3_ERROR)) }
                false
            }
            else -> {
                verified = input to info
                _state.update { state ->
                    val autoDuration = !durationEditedByUser || state.form.durationText.isBlank()
                    state.copy(
                        audioCheck = AudioCheck.Ok(info),
                        form = state.form.copy(
                            durationText = if (autoDuration) formatDurationInput(info.durationSeconds)
                            else state.form.durationText,
                            audio = if (input is MediaInput.Local) state.form.audio.copy(localName = info.displayName)
                            else state.form.audio
                        ),
                        errors = state.errors.copy(duration = if (autoDuration) null else state.errors.duration)
                    )
                }
                true
            }
        }
    }

    // ──────── Cover ────────

    fun onCoverModeChange(mode: SourceMode) {
        updateForm { it.copy(cover = it.cover.copy(mode = mode)) }
        _state.update { it.copy(coverLoadFailed = false, errors = it.errors.copy(cover = null)) }
    }

    fun onCoverPicked(uri: String) {
        updateForm { it.copy(cover = it.cover.copy(mode = SourceMode.DEVICE, localUri = uri)) }
        _state.update { it.copy(errors = it.errors.copy(cover = null)) }
    }

    fun onCoverUrlChange(value: String) {
        updateForm { it.copy(cover = it.cover.copy(urlText = value)) }
        _state.update { it.copy(coverLoadFailed = false, errors = it.errors.copy(cover = null)) }
    }

    fun onCoverUrlLoadResult(success: Boolean) {
        _state.update {
            it.copy(coverLoadFailed = !success, errors = if (success) it.errors.copy(cover = null) else it.errors)
        }
    }

    // ──────── Save ────────

    fun save() {
        if (_state.value.isBusy) return

        viewModelScope.launch {
            // လင့်ခ် ထည့်ပြီး "စစ်ဆေးမည်" မနှိပ်ရသေးရင် — သိမ်းခါနီး အလိုအလျောက် စစ်
            val input = _state.value.form.audio.toInput()
            val canInspect = input is MediaInput.Local || (input is MediaInput.Remote && isValidWebUrl(input.url))
            if (_state.value.audioCheck !is AudioCheck.Ok && canInspect) {
                if (!network.status.value.online) {
                    _state.update { it.copy(message = OFFLINE_SAVE_MESSAGE) }
                    return@launch
                }
                inspectAudio(input)
            }

            val current = _state.value
            val check = current.audioCheck
            val errors = validateEpisodeForm(current.form, check is AudioCheck.Ok, current.coverLoadFailed)
            val duration = parseDurationInput(current.form.durationText)
            if (errors.hasAny || check !is AudioCheck.Ok || duration == null) {
                _state.update { it.copy(errors = errors, message = FIX_ERRORS_MESSAGE) }
                return@launch
            }
            if (!network.status.value.online) {
                _state.update { it.copy(message = OFFLINE_SAVE_MESSAGE) }
                return@launch
            }

            val uploaded = mutableListOf<String>()
            try {
                val onProgress: (SaveStatus) -> Unit = { status -> _state.update { it.copy(saveStatus = status) } }
                val cover = uploader.resolveToUrl(
                    current.form.cover, MediaKind.IMAGE, STORAGE_EPISODES, episodeId, now(),
                    progressLabel = "ပုံ တင်နေပါတယ်", onProgress = onProgress
                )
                if (cover.uploaded) uploaded += cover.url
                val audio = uploader.resolveToUrl(
                    current.form.audio, MediaKind.AUDIO, STORAGE_EPISODES, episodeId, now(),
                    progressLabel = "အသံဖိုင် တင်နေပါတယ်", onProgress = onProgress
                )
                if (audio.uploaded) uploaded += audio.url

                _state.update { it.copy(saveStatus = SaveStatus.Saving) }
                val episode = Episode(
                    id = episodeId,
                    podcastId = podcastId,
                    title = current.form.title.trim(),
                    description = current.form.description.trim(),
                    audioUrl = audio.url,
                    duration = duration,
                    publishedAt = Timestamp(Date(current.form.publishedAtMillis)),
                    coverUrl = cover.url,
                    fileSizeBytes = check.info.sizeBytes
                )
                content.saveEpisode(episode, isNew = current.isNew)

                // အစားထိုးလိုက်တဲ့ ဖိုင်ဟောင်း (ဒီ app တင်ထားတာဆိုမှ) ဖျက်
                saved?.let { old ->
                    if (old.audioUrl.isNotBlank() && old.audioUrl != episode.audioUrl) uploader.deleteIfOwned(old.audioUrl)
                    if (old.coverUrl.isNotBlank() && old.coverUrl != episode.coverUrl) uploader.deleteIfOwned(old.coverUrl)
                }

                _state.update {
                    it.copy(
                        saveStatus = SaveStatus.Idle,
                        savedForm = it.form,     // ထွက်ခါနီး "မသိမ်းရသေး" မမေးအောင်
                        finishedMessage = if (current.isNew) "အပိုင်း အသစ် ထည့်ပြီးပါပြီ" else "အပိုင်းကို သိမ်းပြီးပါပြီ"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uploaded.forEach { uploader.deleteIfOwned(it) }
                _state.update { it.copy(saveStatus = SaveStatus.Idle, message = e.adminMessage()) }
            }
        }
    }

    // ──────── Delete ────────

    fun delete() {
        val episode = saved ?: return
        if (_state.value.isBusy) return
        if (!network.status.value.online) {
            _state.update { it.copy(message = OFFLINE_SAVE_MESSAGE) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saveStatus = SaveStatus.Deleting) }
            try {
                content.deleteEpisode(episode)
                listOf(episode.audioUrl, episode.coverUrl)
                    .filter { it.isNotBlank() }
                    .forEach { uploader.deleteIfOwned(it) }
                _state.update {
                    it.copy(
                        saveStatus = SaveStatus.Idle,
                        savedForm = it.form,
                        finishedMessage = "အပိုင်းကို ဖျက်ပြီးပါပြီ"
                    )
                }
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

    private fun updateForm(transform: (EpisodeForm) -> EpisodeForm) {
        _state.update { it.copy(form = transform(it.form)) }
    }

    companion object {
        private const val LOAD_ERROR = "ဖွင့်လို့ မရပါ — အင်တာနက် ချိတ်ဆက်မှု စစ်ကြည့်ပါ"

        fun factory(
            podcastId: String,
            episodeId: String,
            repo: PodcastRepository = PodcastRepositoryImpl()
        ) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyanCastApp
                AdminEpisodeViewModel(
                    podcastId = podcastId,
                    episodeIdArg = episodeId,
                    repo = repo,
                    content = app.adminContent,
                    uploader = app.mediaUploader,
                    inspector = app.audioInspector,
                    network = app.networkMonitor
                )
            }
        }
    }
}

private fun Episode.toForm() = EpisodeForm(
    title = title,
    description = description,
    durationText = formatDurationInput(duration),
    publishedAtMillis = publishedAt?.toDate()?.time ?: System.currentTimeMillis(),
    audio = MediaField.fromSavedUrl(audioUrl, emptyMode = SourceMode.DEVICE),
    cover = MediaField.fromSavedUrl(coverUrl, emptyMode = SourceMode.NONE)
)
