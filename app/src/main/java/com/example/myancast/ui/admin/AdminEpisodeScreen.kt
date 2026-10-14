package com.example.myancast.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.util.formatPublishDateTime
import com.example.myancast.domain.util.hourAndMinute
import com.example.myancast.domain.util.toDatePickerMillis
import com.example.myancast.domain.util.toMyanmarDigits
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.LiveRed
import com.example.myancast.ui.theme.MyanCastTheme

@Composable
fun AdminEpisodeScreen(
    podcastId: String,
    episodeId: String,
    onBack: () -> Unit,
    onFinished: (message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: AdminEpisodeViewModel = viewModel(factory = AdminEpisodeViewModel.factory(podcastId, episodeId))
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finishedMessage) {
        state.finishedMessage?.let(onFinished)
    }

    AdminEpisodeContent(
        state = state,
        actions = AdminEpisodeActions(
            onBack = onBack,
            onRetryLoad = vm::retryLoad,
            onTitleChange = vm::onTitleChange,
            onDescriptionChange = vm::onDescriptionChange,
            onDurationChange = vm::onDurationChange,
            onPublishDateChange = vm::onPublishDateChange,
            onPublishTimeChange = vm::onPublishTimeChange,
            onAudioModeChange = vm::onAudioModeChange,
            onAudioPicked = vm::onAudioPicked,
            onAudioUrlChange = vm::onAudioUrlChange,
            onVerifyAudioUrl = vm::verifyAudioUrl,
            onCoverModeChange = vm::onCoverModeChange,
            onCoverPicked = vm::onCoverPicked,
            onCoverUrlChange = vm::onCoverUrlChange,
            onCoverUrlLoadResult = vm::onCoverUrlLoadResult,
            onSave = vm::save,
            onDelete = vm::delete,
            onMessageShown = vm::messageShown
        ),
        modifier = modifier
    )
}

private data class AdminEpisodeActions(
    val onBack: () -> Unit,
    val onRetryLoad: () -> Unit,
    val onTitleChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onDurationChange: (String) -> Unit,
    val onPublishDateChange: (Long) -> Unit,
    val onPublishTimeChange: (Int, Int) -> Unit,
    val onAudioModeChange: (SourceMode) -> Unit,
    val onAudioPicked: (String) -> Unit,
    val onAudioUrlChange: (String) -> Unit,
    val onVerifyAudioUrl: () -> Unit,
    val onCoverModeChange: (SourceMode) -> Unit,
    val onCoverPicked: (String) -> Unit,
    val onCoverUrlChange: (String) -> Unit,
    val onCoverUrlLoadResult: (Boolean) -> Unit,
    val onSave: () -> Unit,
    val onDelete: () -> Unit,
    val onMessageShown: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminEpisodeContent(
    state: AdminEpisodeUiState,
    actions: AdminEpisodeActions,
    modifier: Modifier = Modifier
) {
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val guardBack = (state.isDirty || state.isBusy) && state.finishedMessage == null
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
                    Column {
                        Text(
                            text = if (state.isNew) "အပိုင်း အသစ်" else "အပိုင်း ပြင်ရန်",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (state.podcastTitle.isNotBlank()) {
                            Text(
                                text = state.podcastTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = requestBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "နောက်သို့")
                    }
                },
                actions = {
                    if (!state.isNew && !state.isLoading && state.loadError == null) {
                        IconButton(onClick = { confirmDelete = true }, enabled = !state.isBusy) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "အပိုင်း ဖျက်ရန်", tint = LiveRed)
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
                    label = if (state.isNew) "အပိုင်း ထည့်မည်" else "သိမ်းမည်",
                    onSave = actions.onSave
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            state.loadError != null -> ErrorView(state.loadError, actions.onRetryLoad, Modifier.padding(padding))
            else -> EpisodeForm(state = state, actions = actions, contentPadding = padding)
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
        AdminConfirmDialog(
            title = "“${state.savedForm.title}” ကို ဖျက်မလား?",
            text = "ဒီအပိုင်းကို အသုံးပြုသူတွေ နားထောင်လို့ မရတော့ပါ။ ပြန်ယူလို့ မရပါ။",
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
private fun EpisodeForm(
    state: AdminEpisodeUiState,
    actions: AdminEpisodeActions,
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
                title = "အသံဖိုင် (MP3)",
                hint = "ဖုန်းထဲက .mp3 ဖိုင် (သို့) အင်တာနက်ပေါ်က .mp3 လင့်ခ်"
            )
            Spacer(Modifier.height(8.dp))
            AudioSourceField(
                field = state.form.audio,
                check = state.audioCheck,
                onModeChange = actions.onAudioModeChange,
                onPicked = actions.onAudioPicked,
                onUrlChange = actions.onAudioUrlChange,
                onVerifyUrl = actions.onVerifyAudioUrl,
                error = state.errors.audio,
                enabled = state.saveStatus == SaveStatus.Idle
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
                placeholder = "ဥပမာ — အပိုင်း ၁ — AI ဆိုတာ ဘာလဲ",
                error = state.errors.title,
                supporting = "${state.form.title.trim().length.toMyanmarDigits()} / ${TITLE_MAX_LENGTH.toMyanmarDigits()}",
                enabled = enabled
            )
            Spacer(Modifier.height(8.dp))
            AdminTextField(
                value = state.form.description,
                onValueChange = actions.onDescriptionChange,
                label = "အကြောင်းအရာ",
                placeholder = "ဒီအပိုင်းမှာ ဘာတွေ ပြောထားလဲ",
                singleLine = false,
                minLines = 3,
                imeAction = ImeAction.Default,
                enabled = enabled
            )
            Spacer(Modifier.height(8.dp))
            AdminTextField(
                value = state.form.durationText,
                onValueChange = actions.onDurationChange,
                label = "ကြာချိန် (မိနစ်:စက္ကန့်)",
                placeholder = "24:30",
                error = state.errors.duration,
                supporting = "MP3 ကို စစ်ပြီး အလိုအလျောက် ဖြည့်ပေးပါတယ် — လိုရင် ပြင်လို့ရပါတယ်",
                enabled = enabled
            )
            Spacer(Modifier.height(8.dp))
            PublishDateRow(
                publishedAtMillis = state.form.publishedAtMillis,
                onDateChange = actions.onPublishDateChange,
                onTimeChange = actions.onPublishTimeChange,
                enabled = enabled
            )
        }

        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }

        item {
            AdminSectionTitle(title = "အပိုင်း ပုံ", hint = "မထည့်လည်း ရပါတယ်")
            Spacer(Modifier.height(8.dp))
            ImageSourceField(
                field = state.form.cover,
                onModeChange = actions.onCoverModeChange,
                onPicked = actions.onCoverPicked,
                onUrlChange = actions.onCoverUrlChange,
                onUrlLoadResult = actions.onCoverUrlLoadResult,
                allowNone = true,
                fallbackUrl = state.podcastCoverUrl,
                error = state.errors.cover ?: COVER_LOAD_FAILED_HINT.takeIf { state.coverLoadFailed },
                enabled = enabled
            )
        }
    }
}

/** ထုတ်လွှင့်ရက် — ရက်စွဲ + အချိန် ခွဲရွေး (အနာဂတ် ရက် ပိတ်ထား — app က ချက်ချင်း ပြလို့) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishDateRow(
    publishedAtMillis: Long,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    enabled: Boolean
) {
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "ထုတ်လွှင့်ရက်",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatPublishDateTime(publishedAtMillis),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { showDate = true }, enabled = enabled) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("ရက်စွဲ ပြောင်းမည်", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(onClick = { showTime = true }, enabled = enabled) {
                Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("အချိန် ပြောင်းမည်", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showDate) {
        val todayUtc = remember { toDatePickerMillis(System.currentTimeMillis()) }
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = toDatePickerMillis(publishedAtMillis),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayUtc
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let(onDateChange)
                    showDate = false
                }) { Text("ရွေးမည်", style = MaterialTheme.typography.bodyMedium, color = GoldPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) {
                    Text("မလုပ်တော့ပါ", style = MaterialTheme.typography.bodyMedium)
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTime) {
        val (hour, minute) = remember(publishedAtMillis) { hourAndMinute(publishedAtMillis) }
        val timeState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("အချိန် ရွေးပါ", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timeState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(timeState.hour, timeState.minute)
                    showTime = false
                }) { Text("ရွေးမည်", style = MaterialTheme.typography.bodyMedium, color = GoldPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) {
                    Text("မလုပ်တော့ပါ", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun AdminEpisodePreview() {
    MyanCastTheme {
        AdminEpisodeContent(
            state = AdminEpisodeUiState(
                isLoading = false,
                podcastTitle = "နည်းပညာ အကြောင်း",
                form = EpisodeForm(
                    title = "အပိုင်း ၁ — AI ဆိုတာ ဘာလဲ",
                    durationText = "24:30",
                    publishedAtMillis = 1_760_000_000_000L,
                    audio = MediaField(SourceMode.DEVICE, localUri = "content://x", localName = "episode-01.mp3")
                ),
                audioCheck = AudioCheck.Ok(AudioInfo(1470, 11_760_000L, isMp3 = true))
            ),
            actions = AdminEpisodeActions(
                {}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
            )
        )
    }
}
