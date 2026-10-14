package com.example.myancast.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.util.formatDurationLabel
import com.example.myancast.domain.util.formatMegabytes
import com.example.myancast.domain.util.toMyanmarDigits
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myancast.domain.model.Podcast
import com.example.myancast.ui.components.EmptyView
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.components.PodcastListItem
import com.example.myancast.ui.theme.LiveRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onPodcastClick: (String) -> Unit,
    vm: LibraryViewModel,
    onOpenPlayer: () -> Unit = {}
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "စာကြည့်တိုက်",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            state.error != null -> ErrorView(
                message = state.error ?: "Error",
                onRetry = vm::retry,
                modifier = Modifier.padding(padding)
            )
            else -> LibraryContent(
                state = state,
                onTabSelect = vm::selectTab,
                onPodcastClick = onPodcastClick,
                modifier = Modifier.padding(padding),
                onDownloadClick = { id -> if (vm.playDownloaded(id)) onOpenPlayer() },
                onDeleteDownload = { id -> confirmDelete = id }
            )
        }
    }

    // ★ Phase 6 (#4)
    confirmDelete?.let { episodeId ->
        val title = state.downloads.find { it.episode.id == episodeId }?.episode?.title.orEmpty()
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("ဒေါင်းလုဒ် ဖျက်မလား?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "“$title” ကို ဖုန်းထဲက ဖျက်ပါမယ်။ နောက်မှ ပြန်ဒေါင်းလို့ရပါတယ်။",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteDownload(episodeId)
                    confirmDelete = null
                }) { Text("ဖျက်မည်", style = MaterialTheme.typography.bodyMedium, color = LiveRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text("မလုပ်တော့ပါ", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }
}

@Composable
private fun LibraryContent(
    state: LibraryUiState,
    onTabSelect: (LibraryTab) -> Unit,
    onPodcastClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDownloadClick: (String) -> Unit = {},
    onDeleteDownload: (String) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row
        TabRow(
            selectedTabIndex = state.tab.ordinal,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Tab(
                selected = state.tab == LibraryTab.SUBSCRIBED,
                onClick = { onTabSelect(LibraryTab.SUBSCRIBED) },
                text = { Text("သိမ်းထားသော") }
            )
            Tab(
                selected = state.tab == LibraryTab.HISTORY,
                onClick = { onTabSelect(LibraryTab.HISTORY) },
                text = { Text("မှတ်တမ်း") }
            )
            Tab(
                selected = state.tab == LibraryTab.DOWNLOADS,
                onClick = { onTabSelect(LibraryTab.DOWNLOADS) },
                text = { Text("ဒေါင်းလုဒ်") }
            )
        }

        // Content
        when (state.tab) {
            LibraryTab.SUBSCRIBED -> SubscribedList(
                podcasts = state.subscribed,
                onPodcastClick = onPodcastClick
            )
            LibraryTab.HISTORY -> HistoryList(
                history = state.history,
                onPodcastClick = onPodcastClick
            )
            LibraryTab.DOWNLOADS -> DownloadList(
                downloads = state.downloads,
                total = state.downloadsTotal,
                onPlay = onDownloadClick,
                onDelete = onDeleteDownload
            )
        }
    }
}

@Composable
private fun SubscribedList(
    podcasts: List<Podcast>,
    onPodcastClick: (String) -> Unit
) {
    if (podcasts.isEmpty()) {
        EmptyView("သိမ်းထားတာ မရှိသေးပါ")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(podcasts) { podcast ->
                PodcastListItem(
                    podcast = podcast,
                    onClick = { onPodcastClick(podcast.id) }
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    history: List<HistoryRow>,
    onPodcastClick: (String) -> Unit
) {
    if (history.isEmpty()) {
        EmptyView("နားထောင်ထားတာ မရှိသေးပါ")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(history) { row ->
                HistoryItem(
                    row = row,
                    // သတင်း audio က podcast မရှိလို့ နှိပ်လို့ မရ
                    onClick = row.podcastId.takeIf { it.isNotBlank() }
                        ?.let { id -> { onPodcastClick(id) } }
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    row: HistoryRow,
    onClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = row.episodeTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = row.podcastTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Progress bar
        LinearProgressIndicator(
            progress = { row.progress.fraction },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(3.dp)
        )
    }
}

// ─────────────────────────────────────────────
// ★ Phase 6 (#4) — ဒေါင်းလုဒ် tab
// ─────────────────────────────────────────────

@Composable
private fun DownloadList(
    downloads: List<DownloadedEpisode>,
    total: String,
    onPlay: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (downloads.isEmpty()) {
        EmptyView("ဒေါင်းလုဒ် မရှိသေးပါ — Podcast ထဲက ⬇ ကို နှိပ်ပြီး ဒေါင်းထားရင် အင်တာနက် မရှိလည်း နားထောင်လို့ရပါတယ်")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "${downloads.size.toMyanmarDigits()} ပိုင်း · စုစုပေါင်း $total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        items(downloads, key = { it.episode.id }) { item ->
            DownloadItem(item = item, onPlay = { onPlay(item.episode.id) }, onDelete = { onDelete(item.episode.id) })
        }
    }
}

@Composable
private fun DownloadItem(
    item: DownloadedEpisode,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.episode.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.episode.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOf(item.podcastTitle, formatDurationLabel(item.episode.duration), formatMegabytes(item.sizeBytes))
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "ဒေါင်းလုဒ် ဖျက်ရန်", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
