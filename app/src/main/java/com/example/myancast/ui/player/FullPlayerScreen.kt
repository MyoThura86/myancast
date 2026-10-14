package com.example.myancast.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myancast.domain.model.SleepTimerOption
import com.example.myancast.domain.util.SLEEP_TIMER_MINUTES
import com.example.myancast.domain.util.sleepTimerOptionLabel
import com.example.myancast.ui.components.EmptyView
import com.example.myancast.ui.components.PlayerControls
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.LiveRed
import com.example.myancast.ui.theme.MyanCastTheme


// ─────────────────────────────────────────────
// ၁။ Public Entry Point — NavHost က ဒါကို ခေါ်
// ─────────────────────────────────────────────

@Composable
fun FullPlayerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    FullPlayerContent(
        state = state,
        onBack = onBack,
        onPlayPause = vm::togglePlayPause,
        onSkipBack = vm::skipBack,
        onSkipForward = vm::skipForward,
        onNext = vm::next,
        onPrev = vm::previous,
        onSeek = vm::seekToFraction,
        onSpeedClick = vm::cycleSpeed,
        modifier = modifier,
        onSleepTimerSelect = vm::setSleepTimer
    )
}


// ─────────────────────────────────────────────
// ၂။ Stateless Content
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullPlayerContent(
    state: PlayerUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Float) -> Unit,
    onSpeedClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSleepTimerSelect: (SleepTimerOption) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "ချုံ့မည်",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (!state.hasEpisode) {
            EmptyView(
                message = "ဖွင့်ထားတာ မရှိသေးပါ",
                modifier = Modifier.padding(padding)
            )
        } else {
            PlayerBody(
                state = state,
                onPlayPause = onPlayPause,
                onSkipBack = onSkipBack,
                onSkipForward = onSkipForward,
                onNext = onNext,
                onPrev = onPrev,
                onSeek = onSeek,
                onSpeedClick = onSpeedClick,
                onSleepTimerSelect = onSleepTimerSelect,
                modifier = Modifier.padding(padding)
            )
        }
    }
}


// ─────────────────────────────────────────────
// ၃။ Player Body — cover, titles, slider, controls, speed
// ─────────────────────────────────────────────

@Composable
private fun PlayerBody(
    state: PlayerUiState,
    onPlayPause: () -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Float) -> Unit,
    onSpeedClick: () -> Unit,
    onSleepTimerSelect: (SleepTimerOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSleepSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Cover
        AsyncImage(
            model = state.coverUrl,
            contentDescription = state.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(28.dp))

        // Episode title — Myanmar → headline/body style
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = state.podcastTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        SeekBar(state = state, onSeek = onSeek)

        // Error — playback နောက်တစ်ပိုင်းကို ဆက်သွားနိုင်လို့ screen မဖုံး
        state.error?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = LiveRed,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        PlayerControls(
            isPlaying = state.isPlaying,
            isBuffering = state.isBuffering,
            onPlayPause = onPlayPause,
            onSkipBack = onSkipBack,
            onSkipForward = onSkipForward,
            onNext = onNext,
            onPrev = onPrev,
            hasNext = state.hasNext,
            hasPrevious = state.hasPrevious
        )

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Speed chip — "1.5x" က ASCII → label style
            AssistChip(
                onClick = onSpeedClick,
                label = {
                    Text(
                        text = state.speedText,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = AssistChipDefaults.assistChipColors(labelColor = GoldPrimary)
            )

            // ★ Phase 6 (#3) — Sleep timer chip — မြန်မာစာ → body style (Poppins မဟုတ်)
            AssistChip(
                onClick = { showSleepSheet = true },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Bedtime,
                        contentDescription = null,
                        tint = if (state.sleepTimerActive) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = {
                    Text(
                        text = state.sleepTimerLabel.ifEmpty { "အိပ်ချိန်" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = if (state.sleepTimerActive) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showSleepSheet) {
        SleepTimerSheet(
            active = state.sleepTimerActive,
            atEpisodeEnd = state.sleepAtEpisodeEnd,
            onSelect = { option ->
                onSleepTimerSelect(option)
                showSleepSheet = false
            },
            onDismiss = { showSleepSheet = false }
        )
    }
}


// ─────────────────────────────────────────────
// ★ Phase 6 (#3) — Sleep timer sheet
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(
    active: Boolean,
    atEpisodeEnd: Boolean,
    onSelect: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "အိပ်ချိန် တိုင်မာ",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = "သတ်မှတ်ချိန် ရောက်ရင် အသံ တဖြည်းဖြည်း လျော့သွားပြီး ရပ်ပါမယ်",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            SLEEP_TIMER_MINUTES.forEach { minutes ->
                SleepTimerOptionRow(
                    label = sleepTimerOptionLabel(minutes),
                    selected = false,
                    onClick = { onSelect(SleepTimerOption.Minutes(minutes)) }
                )
            }
            SleepTimerOptionRow(
                label = "ဒီပိုင်း ဆုံးရင်",
                selected = atEpisodeEnd,
                onClick = { onSelect(SleepTimerOption.EndOfEpisode) }
            )
            if (active) {
                SleepTimerOptionRow(
                    label = "အိပ်ချိန် ပိတ်မည်",
                    selected = false,
                    color = LiveRed,
                    onClick = { onSelect(SleepTimerOption.Off) }
                )
            }
        }
    }
}

@Composable
private fun SleepTimerOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "ရွေးထားသည်", tint = GoldPrimary)
        }
    }
}


// ─────────────────────────────────────────────
// ၄။ SeekBar — drag နေတုန်း drag value ကို ပြ (thumb ပြန်မခုန်အောင်)
// ─────────────────────────────────────────────

@Composable
private fun SeekBar(
    state: PlayerUiState,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // null = drag မလုပ်နေ → player position ကို ပြ
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val shownFraction = dragFraction ?: state.progress

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = shownFraction,
            onValueChange = { dragFraction = it },
            onValueChangeFinished = {
                dragFraction?.let(onSeek)
                dragFraction = null
            },
            enabled = state.durationMs > 0,
            colors = SliderDefaults.colors(
                thumbColor = GoldPrimary,
                activeTrackColor = GoldPrimary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                // Drag နေတုန်းမှပဲ ဒီမှာ format — ကျန်တဲ့အချိန် ViewModel ကပေးတဲ့ စာသား
                text = dragFraction?.let { formatMs((state.durationMs * it).toLong()) }
                    ?: state.positionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.durationText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ─────────────────────────────────────────────
// ၅။ @Preview
// ─────────────────────────────────────────────

private val previewState = PlayerUiState(
    hasEpisode = true,
    title = "အပိုင်း ၁ — မနက်ခင်း သတင်း",
    podcastTitle = "မြန်မာ့ သတင်း အစီအစဉ်",
    coverUrl = "https://picsum.photos/seed/p1/400",
    isPlaying = true,
    progress = 0.45f,
    durationMs = 1_470_000L,
    positionText = "11:01",
    durationText = "24:30",
    speedText = "1.5x",
    hasNext = true
)

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun FullPlayerPlayingPreview() {
    MyanCastTheme {
        FullPlayerContent(
            state = previewState,
            onBack = {}, onPlayPause = {}, onSkipBack = {}, onSkipForward = {},
            onNext = {}, onPrev = {}, onSeek = {}, onSpeedClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun FullPlayerBufferingErrorPreview() {
    MyanCastTheme {
        FullPlayerContent(
            state = previewState.copy(
                isPlaying = false,
                isBuffering = true,
                error = "ဖွင့်လို့ မရပါ — အင်တာနက် ချိတ်ဆက်မှု စစ်ကြည့်ပါ"
            ),
            onBack = {}, onPlayPause = {}, onSkipBack = {}, onSkipForward = {},
            onNext = {}, onPrev = {}, onSeek = {}, onSpeedClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun FullPlayerEmptyPreview() {
    MyanCastTheme {
        FullPlayerContent(
            state = PlayerUiState(),
            onBack = {}, onPlayPause = {}, onSkipBack = {}, onSkipForward = {},
            onNext = {}, onPrev = {}, onSeek = {}, onSpeedClick = {}
        )
    }
}
