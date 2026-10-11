// ui/news/NewsScreen.kt
package com.example.myancast.ui.news

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myancast.domain.model.NewsItem
import com.example.myancast.ui.components.CategoryChips
import com.example.myancast.ui.components.EmptyView
import com.example.myancast.ui.components.ErrorView
import com.example.myancast.ui.components.FeaturedNewsCard
import com.example.myancast.ui.components.LoadingView
import com.example.myancast.ui.components.NewsListItem
import com.example.myancast.ui.components.SectionHeader
import com.example.myancast.ui.home.ALL_CATEGORY
import com.example.myancast.ui.theme.AppConfig
import com.example.myancast.ui.theme.MyanCastTheme

// ═══════════════════════════════════════════════
// ၁။ Public Entry Point
// ═══════════════════════════════════════════════
@Composable
fun NewsScreen(
    onNewsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: NewsViewModel = viewModel(factory = NewsViewModel.factory())
) {
    val state by vm.state.collectAsStateWithLifecycle()

    NewsContent(
        state = state,
        onCategorySelect = vm::selectCategory,
        onNewsClick = onNewsClick,
        onRetry = vm::refresh,
        modifier = modifier
    )
}

// ═══════════════════════════════════════════════
// ၂။ Stateless Content
// ═══════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsContent(
    state: NewsUiState,
    onCategorySelect: (String) -> Unit,
    onNewsClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "သတင်း",
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
            // ၁။ Loading
            state.isLoading -> LoadingView(Modifier.padding(padding))

            // ၂။ Error
            state.error != null -> ErrorView(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding)
            )

            // ၃။ Empty — သတင်း တစ်ခုမှ မရှိ
            state.allNews.isEmpty() -> EmptyView(
                message = "သတင်း မရှိသေးပါ",
                modifier = Modifier.padding(padding)
            )

            // ၄။ Content
            else -> NewsList(
                state = state,
                onCategorySelect = onCategorySelect,
                onNewsClick = onNewsClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun NewsList(
    state: NewsUiState,
    onCategorySelect: (String) -> Unit,
    onNewsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ─── Featured (အသစ်ဆုံး တစ်ခု) ───
        state.featured?.let { featured ->
            item {
                FeaturedNewsCard(
                    news = featured,
                    onClick = { onNewsClick(featured.id) }
                )
            }
        }

        // ─── Categories ───
        item {
            Spacer(Modifier.height(8.dp))
            CategoryChips(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = onCategorySelect
            )
            Spacer(Modifier.height(8.dp))
        }

        // ─── ကျန် သတင်းများ ───
        if (state.news.isEmpty()) {
            // Category filter ကြောင့် ဘာမှ မကျန်တဲ့ အခြေအနေ
            item { EmptyView(message = "ဒီအမျိုးအစားမှာ သတင်း မရှိသေးပါ") }
        } else if (state.rest.isNotEmpty()) {
            item { SectionHeader(title = "နောက်ထပ် သတင်းများ") }
            items(state.rest, key = { it.id }) { news ->
                NewsListItem(
                    news = news,
                    onClick = { onNewsClick(news.id) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
// ၃။ Preview
// ═══════════════════════════════════════════════
private val previewNews = listOf(
    NewsItem(
        id = "1",
        headline = "မြန်မာနိုင်ငံ နည်းပညာကဏ္ဍ တိုးတက်လာ",
        body = "ယခုနှစ်အတွင်း နည်းပညာ startup များ တိုးပွားလာသည်။",
        imageUrl = "https://picsum.photos/600/340",
        category = "နည်းပညာ"
    ),
    NewsItem(
        id = "2",
        headline = "အားကစားပွဲတော် ကျင်းပမည်",
        body = "လာမည့်လတွင် နိုင်ငံလုံးဆိုင်ရာ ပြိုင်ပွဲ ကျင်းပမည်။",
        imageUrl = "https://picsum.photos/600/341",
        category = "အားကစား"
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun NewsContentPreview() {
    MyanCastTheme(config = AppConfig(darkMode = true, zawgyi = false)) {
        NewsContent(
            state = NewsUiState(
                allNews = previewNews,
                news = previewNews,
                categories = listOf(ALL_CATEGORY, "နည်းပညာ", "အားကစား"),
                isLoading = false
            ),
            onCategorySelect = {},
            onNewsClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun NewsEmptyPreview() {
    MyanCastTheme {
        NewsContent(
            state = NewsUiState(isLoading = false),
            onCategorySelect = {},
            onNewsClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0D0B)
@Composable
private fun NewsErrorPreview() {
    MyanCastTheme {
        NewsContent(
            state = NewsUiState(isLoading = false, error = "အင်တာနက် ချိတ်ဆက်မှု မရပါ"),
            onCategorySelect = {},
            onNewsClick = {},
            onRetry = {}
        )
    }
}
