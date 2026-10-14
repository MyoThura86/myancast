package com.example.myancast.ui.details

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import com.example.myancast.domain.util.formatMegabytes
import com.example.myancast.domain.util.toMyanmarDigits
import com.example.myancast.ui.components.DownloadButtonState
import com.example.myancast.ui.components.EmptyView
import com.example.myancast.ui.components.EpisodeRow
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.components.SectionHeader
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.LiveRed
import com.example.myancast.ui.theme.MyanCastTheme


// ─────────────────────────────────────────────
// ၁။ Public Entry Point
// ─────────────────────────────────────────────

@Composable
fun PodcastDetailScreen(
    podcastId: String,
    onBack: () -> Unit,
    onEpisodeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: PodcastDetailViewModel = viewModel(
        factory = PodcastDetailViewModel.factory(podcastId)
    )

    val state by vm.state.collectAsStateWithLifecycle()
    val nowPlayingId by vm.nowPlayingId.collectAsStateWithLifecycle()
    val isSubscribed by vm.isSubscribed.collectAsStateWithLifecycle()
    val downloadStates by vm.downloadStates.collectAsStateWithLifecycle()

    PodcastDetailContent(
        state = state,
        nowPlayingId = nowPlayingId,
        isSubscribed = isSubscribed,
        onToggleSubscribe = vm::toggleSubscribe,
        onBack = onBack,
        onEpisodeClick = { id -> if (vm.playEpisode(id)) onEpisodeClick(id) },
        onPlayAll = { vm.playAll() },
        onRetry = vm::retry,
        onPlayErrorShown = vm::playErrorShown,
        modifier = modifier,
        downloadStates = downloadStates,
        onDownloadClick = vm::onDownloadClick,
        onMessageShown = vm::messageShown
    )

    // ★ Phase 6 (#5) — Data ချွေတာ mode + mobile data
    state.pendingStream?.let { pending ->
        StreamConfirmDialog(
            pending = pending,
            onConfirm = { if (vm.confirmStream()) onEpisodeClick(pending.episodeId.orEmpty()) },
            onDismiss = vm::dismissStream
        )
    }
    // ★ Phase 6 (#4)
    state.confirmDeleteDownload?.let { episode ->
        AlertDialog(
            onDismissRequest = vm::dismissDeleteDownload,
            title = { Text("ဒေါင်းလုဒ် ဖျက်မလား?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "“${episode.title}” ကို ဖုန်းထဲက ဖျက်ပါမယ်။ နောက်မှ ပြန်ဒေါင်းလို့ရပါတယ်။",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = vm::confirmDeleteDownload) {
                    Text("ဖျက်မည်", style = MaterialTheme.typography.bodyMedium, color = LiveRed)
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissDeleteDownload) {
                    Text("မလုပ်တော့ပါ", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }
}

@Composable
private fun StreamConfirmDialog(
    pending: PendingStream,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val size = formatMegabytes(pending.sizeBytes)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mobile data နဲ့ ဖွင့်မလား?", style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                text = when {
                    pending.episodeId == null ->
                        "ဒေါင်းလုဒ် မလုပ်ရသေးတဲ့ အပိုင်းတွေကို mobile data နဲ့ ဖွင့်ပါမယ်။"
                    size.isNotEmpty() ->
                        "ဒီအပိုင်းကို ဒေါင်းလုဒ် မလုပ်ရသေးပါ။ နားထောင်ရင် mobile data $size ခန့် ကုန်ပါမယ်။"
                    else -> "ဒီအပိုင်းကို ဒေါင်းလုဒ် မလုပ်ရသေးပါ။ နားထောင်ရင် mobile data ကုန်ပါမယ်။"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("ဖွင့်မည်", style = MaterialTheme.typography.bodyMedium, color = GoldPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("မဖွင့်တော့ပါ", style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}


// ─────────────────────────────────────────────
// ၂။ Stateful Shell
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PodcastDetailContent(
    state: PodcastDetailUiState,
    onBack: () -> Unit,
    onEpisodeClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    nowPlayingId: String? = null,
    isSubscribed: Boolean = false,
    onToggleSubscribe: () -> Unit = {},
    onPlayAll: () -> Unit = {},
    onPlayErrorShown: () -> Unit = {},
    downloadStates: Map<String, DownloadButtonState> = emptyMap(),
    onDownloadClick: (String) -> Unit = {},
    onMessageShown: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.playError) {
        val message = state.playError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onPlayErrorShown()
    }
    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageShown()
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "နောက်သို့"
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
        when {
            state.isLoading -> {
                LoadingView(Modifier.padding(padding))
            }

            state.error != null && state.canRetry -> {
                ErrorView(
                    message = state.error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding)
                )
            }

            state.error != null -> {
                EmptyView(
                    message = state.error,
                    modifier = Modifier.padding(padding)
                )
            }

            state.podcast == null -> {
                EmptyView(
                    message = "Podcast ရှာမတွေ့ပါ",
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                val podcast = state.podcast
                DetailContent(
                    podcast = podcast,
                    episodes = state.episodes,
                    onEpisodeClick = onEpisodeClick,
                    isSubscribed = isSubscribed,
                    onToggleSubscribe = onToggleSubscribe,
                    modifier = Modifier.padding(padding),
                    nowPlayingId = nowPlayingId,
                    onPlayAll = onPlayAll,
                    downloadStates = downloadStates,
                    onDownloadClick = onDownloadClick
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// ၃။ Content
// ─────────────────────────────────────────────

@Composable
private fun DetailContent(
    podcast: Podcast,
    episodes: List<Episode>,
    onEpisodeClick: (String) -> Unit,
    isSubscribed: Boolean,
    onToggleSubscribe: () -> Unit,
    modifier: Modifier = Modifier,
    nowPlayingId: String? = null,
    onPlayAll: () -> Unit = {},
    downloadStates: Map<String, DownloadButtonState> = emptyMap(),
    onDownloadClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            PodcastHeader(podcast = podcast)
        }

        item {
            ActionButtons(
                isSubscribed = isSubscribed,
                onToggleSubscribe = onToggleSubscribe,
                onPlayAll = onPlayAll
            )
        }

        item {
            SectionHeader(title = "ပိုင်းများ (${episodes.size.toMyanmarDigits()})")
        }

        if (episodes.isEmpty()) {
            item {
                EmptyView("ပိုင်း မရှိသေးပါ")
            }
        } else {
            itemsIndexed(episodes) { i, ep ->
                EpisodeRow(
                    index = i + 1,
                    episode = ep,
                    onPlay = { onEpisodeClick(ep.id) },
                    isPlaying = ep.id == nowPlayingId,
                    // audio မရှိတဲ့ အပိုင်းကို ဒေါင်းလို့ မရ — ခလုတ် မပြ
                    downloadState = if (ep.audioUrl.isBlank()) null
                    else downloadStates[ep.id] ?: DownloadButtonState.NotDownloaded,
                    onDownloadClick = { onDownloadClick(ep.id) }
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// ၄။ PodcastHeader
// ─────────────────────────────────────────────

@Composable
private fun PodcastHeader(
    podcast: Podcast,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = podcast.coverUrl,
            contentDescription = podcast.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = podcast.title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${podcast.category} · ${podcast.episodeCount.toMyanmarDigits()} ပိုင်း",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = podcast.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
    }
}


// ─────────────────────────────────────────────
// ၅။ ActionButtons
// ─────────────────────────────────────────────

@Composable
private fun ActionButtons(
    isSubscribed: Boolean,
    onToggleSubscribe: () -> Unit,
    onPlayAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPlayAll,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("အားလုံး ဖွင့်")
        }

        OutlinedButton(onClick = onToggleSubscribe) {
            Icon(
                imageVector = if (isSubscribed) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null
            )
            Spacer(Modifier.width(6.dp))
            Text(if (isSubscribed) "သိမ်းပြီး" else "သိမ်း")
        }
    }
}


// ─────────────────────────────────────────────
// ၆။ Previews
// ─────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun PodcastDetailContentPreview() {
    MyanCastTheme {
        PodcastDetailContent(
            state = PodcastDetailUiState(
                podcast = fakePodcast,
                episodes = fakeEpisodes,
                isLoading = false
            ),
            onBack = {},
            onEpisodeClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun PodcastDetailNotFoundPreview() {
    MyanCastTheme {
        PodcastDetailContent(
            state = PodcastDetailUiState(isLoading = false, error = "Podcast ရှာမတွေ့ပါ"),
            onBack = {},
            onEpisodeClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun DetailContentPreview() {
    MyanCastTheme {
        DetailContent(
            podcast = fakePodcast,
            episodes = fakeEpisodes,
            onEpisodeClick = {},
            isSubscribed = false,
            onToggleSubscribe = {},
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun DetailContentEmptyEpisodesPreview() {
    MyanCastTheme {
        DetailContent(
            podcast = fakePodcast,
            episodes = emptyList(),
            onEpisodeClick = {},
            isSubscribed = false,
            onToggleSubscribe = {},
            modifier = Modifier
        )
    }
}


// ─────────────────────────────────────────────
// ၇။ Fake Data
// ─────────────────────────────────────────────

private val fakePodcast = Podcast(
    id = "p1",
    title = "မြန်မာ့ သတင်း အစီအစဉ်",
    description = "နေ့စဉ် မြန်မာ့ သတင်းတွေကို အသံနဲ့ နားထောင်ပါ။ " +
            "နိုင်ငံရေး၊ စီးပွားရေး၊ လူမှုရေး သတင်းများ အပါအဝင်။",
    coverUrl = "https://picsum.photos/seed/p1/400",
    category = "သတင်း",
    episodeCount = 4
)

private val fakeEpisodes = listOf(
    Episode(
        id = "e1",
        title = "အပိုင်း ၁ — မနက်ခင်း သတင်း",
        audioUrl = "https://example.com/e1.mp3",
        podcastId = "p1",
        duration = 1800
    ),
    Episode(
        id = "e2",
        title = "အပိုင်း ၂ — စီးပွားရေး သတင်း",
        audioUrl = "https://example.com/e2.mp3",
        podcastId = "p1",
        duration = 2100
    ),
    Episode(
        id = "e3",
        title = "အပိုင်း ၃ — နိုင်ငံရေး သတင်း",
        audioUrl = "https://example.com/e3.mp3",
        podcastId = "p1",
        duration = 1500
    ),
    Episode(
        id = "e4",
        title = "အပိုင်း ၄ — အားကစား သတင်း",
        audioUrl = "https://example.com/e4.mp3",
        podcastId = "p1",
        duration = 2400
    )
)