package com.example.myancast.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.util.formatDurationLabel
import com.example.myancast.domain.util.formatMegabytes
import com.example.myancast.domain.util.formatRelativeDate
import com.example.myancast.domain.util.toMyanmarDigits
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.theme.BgDark
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.LiveRed
import com.example.myancast.ui.theme.MyanCastTheme

@Composable
fun AdminPodcastScreen(
    podcastId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEpisodeClick: (podcastId: String, episodeId: String) -> Unit,
    onAddEpisode: (podcastId: String) -> Unit,
    modifier: Modifier = Modifier,
    incomingMessage: String? = null,
    onIncomingMessageShown: () -> Unit = {}
) {
    val vm: AdminPodcastViewModel = viewModel(factory = AdminPodcastViewModel.factory(podcastId))
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }
    LaunchedEffect(incomingMessage) {
        if (incomingMessage != null) {
            vm.showMessage(incomingMessage)
            onIncomingMessageShown()
        }
    }

    AdminPodcastContent(
        state = state,
        actions = AdminPodcastActions(
            onBack = onBack,
            onRetryLoad = vm::retryLoad,
            onTitleChange = vm::onTitleChange,
            onDescriptionChange = vm::onDescriptionChange,
            onCategoryChange = vm::onCategoryChange,
            onCoverModeChange = vm::onCoverModeChange,
            onCoverPicked = vm::onCoverPicked,
            onCoverUrlChange = vm::onCoverUrlChange,
            onCoverUrlLoadResult = vm::onCoverUrlLoadResult,
            onSave = vm::save,
            onDelete = vm::delete,
            onMessageShown = vm::messageShown,
            onEpisodeClick = { episodeId -> onEpisodeClick(state.podcastId, episodeId) },
            onAddEpisode = { onAddEpisode(state.podcastId) }
        ),
        modifier = modifier
    )
}

/** Callback များကို တစ်စုတည်း — parameter ၁၅ ခု မဖြစ်အောင် */
private data class AdminPodcastActions(
    val onBack: () -> Unit,
    val onRetryLoad: () -> Unit,
    val onTitleChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onCategoryChange: (String) -> Unit,
    val onCoverModeChange: (SourceMode) -> Unit,
    val onCoverPicked: (String) -> Unit,
    val onCoverUrlChange: (String) -> Unit,
    val onCoverUrlLoadResult: (Boolean) -> Unit,
    val onSave: () -> Unit,
    val onDelete: () -> Unit,
    val onMessageShown: () -> Unit,
    val onEpisodeClick: (String) -> Unit,
    val onAddEpisode: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPodcastContent(
    state: AdminPodcastUiState,
    actions: AdminPodcastActions,
    modifier: Modifier = Modifier
) {
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val guardBack = state.isDirty || state.isBusy
    val requestBack = { if (guardBack) confirmLeave = true else actions.onBack() }

    BackHandler(enabled = guardBack) { confirmLeave = true }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        actions.onMessageShown()
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isNew) "Podcast အသစ်" else "Podcast ပြင်ရန်",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = requestBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "နောက်သို့")
                    }
                },
                actions = {
                    if (!state.isNew && state.loadError == null && !state.isLoading) {
                        IconButton(onClick = { confirmDelete = true }, enabled = !state.isBusy) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Podcast ဖျက်ရန်", tint = LiveRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (!state.isLoading && state.loadError == null) {
                SaveBar(
                    status = state.saveStatus,
                    enabled = !state.isBusy && (state.isNew || state.isDirty),
                    label = if (state.isNew) "Podcast ထည့်မည်" else "သိမ်းမည်",
                    onSave = actions.onSave
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            state.loadError != null -> ErrorView(state.loadError, actions.onRetryLoad, Modifier.padding(padding))
            else -> PodcastForm(state = state, actions = actions, contentPadding = padding)
        }
    }

    if (confirmLeave) {
        UnsavedChangesDialog(
            isBusy = state.isBusy,
            onLeave = {
                confirmLeave = false
                actions.onBack()
            },
            onStay = { confirmLeave = false }
        )
    }
    if (confirmDelete) {
        val count = state.episodes.size
        AdminConfirmDialog(
            title = "“${state.savedForm.title}” ကို ဖျက်မလား?",
            text = if (count > 0) {
                "ဒီ Podcast နဲ့ အပိုင်း ${count.toMyanmarDigits()} ခုလုံး အသုံးပြုသူတွေဆီက ပျောက်သွားပါမယ်။ ပြန်ယူလို့ မရပါ။"
            } else {
                "ဒီ Podcast ကို အသုံးပြုသူတွေ မမြင်ရတော့ပါ။ ပြန်ယူလို့ မရပါ။"
            },
            confirmLabel = "ဖျက်မည်",
            destructive = true,
            onConfirm = {
                confirmDelete = false
                actions.onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun PodcastForm(
    state: AdminPodcastUiState,
    actions: AdminPodcastActions,
    contentPadding: PaddingValues
) {
    val enabled = !state.isBusy
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AdminSectionTitle(
                title = "မျက်နှာဖုံး ပုံ",
                hint = "Home နဲ့ Player မှာ ပြမယ့် ပုံ — စတုရန်း (1:1) ပုံ အကောင်းဆုံးပါ"
            )
            Spacer(Modifier.height(8.dp))
            ImageSourceField(
                field = state.form.cover,
                onModeChange = actions.onCoverModeChange,
                onPicked = actions.onCoverPicked,
                onUrlChange = actions.onCoverUrlChange,
                onUrlLoadResult = actions.onCoverUrlLoadResult,
                // လင့်ခ် ရိုက်ပြီး preview မပေါ်တာနဲ့ ချက်ချင်း ပြော (save နှိပ်မှ မဟုတ်)
                error = state.errors.cover ?: COVER_LOAD_FAILED_HINT.takeIf { state.coverLoadFailed },
                enabled = enabled
            )
        }

        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }

        item {
            AdminSectionTitle(title = "အချက်အလက်")
            Spacer(Modifier.height(8.dp))
            AdminTextField(
                value = state.form.title,
                onValueChange = actions.onTitleChange,
                label = "ခေါင်းစဉ်",
                placeholder = "ဥပမာ — နည်းပညာ အကြောင်း",
                error = state.errors.title,
                supporting = "${state.form.title.trim().length.toMyanmarDigits()} / ${TITLE_MAX_LENGTH.toMyanmarDigits()}",
                enabled = enabled
            )
            Spacer(Modifier.height(8.dp))
            AdminTextField(
                value = state.form.category,
                onValueChange = actions.onCategoryChange,
                label = "အမျိုးအစား",
                placeholder = "ဥပမာ — နည်းပညာ",
                error = state.errors.category,
                supporting = "Home ရဲ့ အမျိုးအစား filter မှာ ပြပါမယ်",
                enabled = enabled
            )
            if (state.categorySuggestions.isNotEmpty()) {
                CategorySuggestionRow(
                    suggestions = state.categorySuggestions,
                    selected = state.form.category.trim(),
                    onPick = actions.onCategoryChange,
                    enabled = enabled
                )
            }
            Spacer(Modifier.height(8.dp))
            AdminTextField(
                value = state.form.description,
                onValueChange = actions.onDescriptionChange,
                label = "အကြောင်းအရာ",
                placeholder = "ဒီ Podcast မှာ ဘာတွေ နားထောင်ရမလဲ",
                singleLine = false,
                minLines = 4,
                imeAction = ImeAction.Default,
                enabled = enabled
            )
        }

        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }

        if (state.isNew) {
            item {
                Text(
                    text = "Podcast ကို အရင် ထည့်ပြီးမှ အပိုင်းတွေ ဆက်ထည့်လို့ရပါမယ်။",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminSectionTitle(
                        title = "အပိုင်းများ (${state.episodes.size.toMyanmarDigits()})",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = actions.onAddEpisode, enabled = enabled) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = GoldPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("အပိုင်း အသစ်", style = MaterialTheme.typography.bodyMedium, color = GoldPrimary)
                    }
                }
            }
            state.episodesError?.let { error -> item { FieldError(error) } }
            if (state.episodes.isEmpty() && state.episodesError == null) {
                item {
                    Text(
                        text = "အပိုင်း မရှိသေးပါ — “အပိုင်း အသစ်” နဲ့ ထည့်ပါ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.episodes, key = { it.id }) { episode ->
                AdminEpisodeRow(
                    episode = episode,
                    fallbackCover = state.savedForm.cover.urlText,
                    onClick = { actions.onEpisodeClick(episode.id) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun CategorySuggestionRow(
    suggestions: List<String>,
    selected: String,
    onPick: (String) -> Unit,
    enabled: Boolean
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(suggestions) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onPick(category) },
                enabled = enabled,
                label = { Text(category, style = MaterialTheme.typography.bodySmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = BgDark
                )
            )
        }
    }
}

@Composable
private fun AdminEpisodeRow(
    episode: Episode,
    fallbackCover: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    // Console က ထည့်ခဲ့တဲ့ data မှာ ပြဿနာ ရှိရင် admin မြင်အောင် အနီနဲ့ ပြ
    val problems = buildList {
        if (episode.audioUrl.isBlank()) add("အသံဖိုင် မရှိ")
        if (episode.duration <= 0) add("ကြာချိန် မရှိ")
    }
    val meta = listOf(
        formatRelativeDate(episode.publishedAt),
        formatDurationLabel(episode.duration),
        formatMegabytes(episode.fileSizeBytes)
    ).filter { it.isNotBlank() }.joinToString(" · ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagePreview(model = episode.coverUrl.ifBlank { fallbackCover }.ifBlank { null }, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = episode.title.ifBlank { "(ခေါင်းစဉ် မရှိ)" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (meta.isNotEmpty()) {
                    Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (problems.isNotEmpty()) {
                    Text("⚠ ${problems.joinToString(" · ")}", style = MaterialTheme.typography.bodySmall, color = LiveRed)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "ပြင်ရန်",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Screen အောက်ခြေ — progress + သိမ်းမည် ခလုတ် (keyboard ပေါ်ရင် အပေါ် တက်လာ) */
@Composable
fun SaveBar(
    status: SaveStatus,
    enabled: Boolean,
    label: String,
    onSave: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SaveProgressCard(status)
            Button(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BgDark)
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun AdminPodcastPreview() {
    MyanCastTheme {
        AdminPodcastContent(
            state = AdminPodcastUiState(
                isNew = false,
                form = PodcastForm(title = "နည်းပညာ အကြောင်း", category = "နည်းပညာ"),
                categorySuggestions = listOf("နည်းပညာ", "သတင်း", "တရားတော်"),
                episodes = listOf(Episode(id = "e1", title = "အပိုင်း ၁ — AI ဆိုတာ", duration = 1470))
            ),
            actions = AdminPodcastActions(
                {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
            )
        )
    }
}
