package com.example.myancast.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myancast.domain.model.Podcast
import com.example.myancast.ui.components.EmptyView
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.components.PodcastListItem
import com.example.myancast.ui.theme.MyanCastTheme


// ─────────────────────────────────────────────
// ၁။ Public Entry Point — NavHost က ဒါကို ခေါ်
// ─────────────────────────────────────────────

@Composable
fun SearchScreen(
    onPodcastClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: SearchViewModel = viewModel(factory = SearchViewModel.factory())
) {
    val state by vm.state.collectAsStateWithLifecycle()

    SearchContent(
        state = state,
        onQueryChange = vm::onQueryChange,
        onClear = vm::clearQuery,
        onPodcastClick = onPodcastClick,
        modifier = modifier
    )
}


// ─────────────────────────────────────────────
// ၂။ Stateless Content
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onPodcastClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ရှာဖွေရန်",
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
        Column(Modifier.padding(padding).fillMaxSize()) {

            SearchField(
                query = state.query,
                onQueryChange = onQueryChange,
                onClear = onClear
            )

            when {
                state.isLoading -> LoadingView()

                state.error != null -> EmptyView(message = state.error)

                // မရိုက်ရသေးတာ နဲ့ ရှာလို့ မတွေ့တာ မတူဘူး — စာ ၂ မျိုး ခွဲပြ
                state.isIdle -> EmptyView(message = "ရှာလိုသည်ကို ရိုက်ထည့်ပါ")

                state.results.isEmpty() -> EmptyView(message = "ရှာမတွေ့ပါ")

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(state.results) { podcast ->
                        PodcastListItem(
                            podcast = podcast,
                            onClick = { onPodcastClick(podcast.id) }
                        )
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// ၃။ Search field
// ─────────────────────────────────────────────

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "podcast အမည် / အမျိုးအစား",
                // ★ TextField မှာ style မသတ်မှတ်ရင် Poppins ဖြစ်ပြီး မြန်မာစာ ပုံပျက်မယ်
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "ရှင်းမည်", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}


// ─────────────────────────────────────────────
// ၄။ @Preview — stateless content ကိုသာ preview
// ─────────────────────────────────────────────

private val previewPodcasts = listOf(
    Podcast("p1", "နည်းပညာ စကားဝိုင်း", "desc", "https://picsum.photos/seed/t/200", "နည်းပညာ", 12),
    Podcast("p2", "Myanmar Tech Talk", "desc", "https://picsum.photos/seed/m/200", "နည်းပညာ", 8)
)

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun SearchIdlePreview() {
    MyanCastTheme {
        SearchContent(
            state = SearchUiState(isLoading = false),
            onQueryChange = {}, onClear = {}, onPodcastClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun SearchResultsPreview() {
    MyanCastTheme {
        SearchContent(
            state = SearchUiState(
                query = "နည်း",
                results = previewPodcasts,
                isIdle = false,
                isLoading = false
            ),
            onQueryChange = {}, onClear = {}, onPodcastClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun SearchNotFoundPreview() {
    MyanCastTheme {
        SearchContent(
            state = SearchUiState(query = "ဘောလုံး", isIdle = false, isLoading = false),
            onQueryChange = {}, onClear = {}, onPodcastClick = {}
        )
    }
}
