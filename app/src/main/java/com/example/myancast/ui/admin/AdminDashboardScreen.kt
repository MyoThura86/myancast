package com.example.myancast.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myancast.domain.model.Podcast
import com.example.myancast.domain.util.toMyanmarDigits
import com.example.myancast.ui.components.EmptyView
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.theme.BgDark
import com.example.myancast.ui.theme.GoldPrimary
import com.example.myancast.ui.theme.MyanCastTheme

@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    onPodcastClick: (String) -> Unit,
    onAddPodcast: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageShown: () -> Unit = {}
) {
    val vm: AdminDashboardViewModel = viewModel(factory = AdminDashboardViewModel.factory())
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) onSignedOut()
    }

    AdminDashboardContent(
        state = state,
        onBack = onBack,
        onSignOut = vm::signOut,
        onRetry = vm::retry,
        onPodcastClick = onPodcastClick,
        onAddPodcast = onAddPodcast,
        message = message,
        onMessageShown = onMessageShown,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardContent(
    state: AdminDashboardUiState,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onRetry: () -> Unit,
    onPodcastClick: (String) -> Unit,
    onAddPodcast: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageShown: () -> Unit = {}
) {
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "စီမံခန့်ခွဲမှု",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "နောက်သို့")
                    }
                },
                actions = {
                    IconButton(onClick = { confirmSignOut = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "အကောင့်မှ ထွက်ရန်")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPodcast,
                containerColor = GoldPrimary,
                contentColor = BgDark,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Podcast အသစ်", style = MaterialTheme.typography.titleSmall) }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            state.error != null -> ErrorView(state.error, onRetry, Modifier.padding(padding))
            state.podcasts.isEmpty() -> EmptyView(
                message = "Podcast မရှိသေးပါ — “Podcast အသစ်” ခလုတ်နဲ့ စထည့်ပါ",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 96.dp)   // FAB နဲ့ မထပ်အောင်
            ) {
                item {
                    Text(
                        text = state.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(state.podcasts, key = { it.id }) { podcast ->
                    AdminPodcastRow(podcast = podcast, onClick = { onPodcastClick(podcast.id) })
                }
            }
        }
    }

    if (confirmSignOut) {
        AdminConfirmDialog(
            title = "အကောင့်မှ ထွက်မလား?",
            text = "ထွက်ပြီးရင် Podcast တွေ ပြင်ဖို့ စကားဝှက်နဲ့ ပြန်ဝင်ရပါမယ်။",
            confirmLabel = "ထွက်မည်",
            onConfirm = {
                confirmSignOut = false
                onSignOut()
            },
            onDismiss = { confirmSignOut = false }
        )
    }
}

@Composable
private fun AdminPodcastRow(
    podcast: Podcast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagePreview(model = podcast.coverUrl.ifBlank { null }, size = 64.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = podcast.title.ifBlank { "(ခေါင်းစဉ် မရှိ)" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOf(podcast.category, "အပိုင်း ${podcast.episodeCount.toMyanmarDigits()} ခု")
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "ပြင်ရန်",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun AdminDashboardPreview() {
    val podcasts = listOf(
        Podcast("p1", "နည်းပညာ အကြောင်း", "", "", "နည်းပညာ", 12),
        Podcast("p2", "မနက်ခင်း တရားတော်", "", "", "တရားတော်", 5)
    )
    MyanCastTheme {
        AdminDashboardContent(
            state = AdminDashboardUiState(podcasts = podcasts, summary = adminSummary(podcasts), isLoading = false),
            onBack = {}, onSignOut = {}, onRetry = {}, onPodcastClick = {}, onAddPodcast = {}
        )
    }
}
