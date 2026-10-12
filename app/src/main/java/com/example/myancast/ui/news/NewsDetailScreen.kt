// ui/news/NewsDetailScreen.kt
package com.example.myancast.ui.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myancast.domain.model.NewsItem
import com.example.myancast.domain.util.formatRelativeDate
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.theme.AppConfig
import com.example.myancast.ui.theme.BgDark
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.MyanCastTheme

// ═══════════════════════════════════════════════
// ၁။ Public Entry Point
// ═══════════════════════════════════════════════
@Composable
fun NewsDetailScreen(
    newsId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit = {},
    vm: NewsDetailViewModel = viewModel(
        factory = NewsDetailViewModel.factory(newsId)
    )
) {
    val state by vm.state.collectAsStateWithLifecycle()

    NewsDetailContent(
        state = state,
        onBack = onBack,
        // ဖွင့်လို့ရမှသာ Player screen ကို သွား — မရရင် snackbar ပဲ ပြ
        onPlay = { if (vm.playAudio()) onPlay() },
        onRetry = vm::retry,
        onPlayErrorShown = vm::playErrorShown,
        modifier = modifier
    )
}

// ═══════════════════════════════════════════════
// ၂။ Stateless Content
// ═══════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsDetailContent(
    state: NewsDetailUiState,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayErrorShown: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.playError) {
        val message = state.playError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onPlayErrorShown()
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
            state.isLoading -> LoadingView(Modifier.padding(padding))

            state.news == null -> ErrorView(
                message = state.error ?: "သတင်း ရှာမတွေ့ပါ",
                onRetry = onRetry,
                modifier = Modifier.padding(padding)
            )

            else -> NewsArticle(
                news = state.news,
                hasAudio = state.hasAudio,
                onPlay = onPlay,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun NewsArticle(
    news: NewsItem,
    hasAudio: Boolean,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (news.imageUrl.isNotBlank()) {
            AsyncImage(
                model = news.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = news.headline,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            // category · ၂ ရက် အရင်က
            Text(
                text = listOf(news.category, formatRelativeDate(news.publishedAt))
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,     // မြန်မာစာမို့ label* မသုံး
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ─── Audio ရှိမှသာ ခလုတ် ပြ ───
            if (hasAudio) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = BgDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "နားထောင်မည်",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = news.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════
// ၃။ Preview
// ═══════════════════════════════════════════════
private val previewNews = NewsItem(
    id = "1",
    headline = "မြန်မာနိုင်ငံ နည်းပညာကဏ္ဍ တိုးတက်လာ",
    body = "ယခုနှစ်အတွင်း နည်းပညာ startup များ တိုးပွားလာပြီး၊ လူငယ်များအတွက် " +
        "အလုပ်အကိုင် အခွင့်အလမ်းများ ပိုမို ရရှိလာသည်။",
    imageUrl = "https://picsum.photos/600/340",
    audioUrl = "https://example.com/news.mp3",
    category = "နည်းပညာ"
)

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun NewsDetailWithAudioPreview() {
    MyanCastTheme(config = AppConfig(darkMode = true, zawgyi = false)) {
        NewsDetailContent(
            state = NewsDetailUiState(news = previewNews, isLoading = false),
            onBack = {},
            onPlay = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun NewsDetailNoAudioPreview() {
    MyanCastTheme {
        NewsDetailContent(
            state = NewsDetailUiState(
                news = previewNews.copy(audioUrl = null),
                isLoading = false
            ),
            onBack = {},
            onPlay = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun NewsDetailErrorPreview() {
    MyanCastTheme {
        NewsDetailContent(
            state = NewsDetailUiState(isLoading = false, error = "သတင်း ရှာမတွေ့ပါ"),
            onBack = {},
            onPlay = {},
            onRetry = {}
        )
    }
}
