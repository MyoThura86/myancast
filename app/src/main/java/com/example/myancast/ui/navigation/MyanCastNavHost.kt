// ui/navigation/MyanCastNavHost.kt
package com.example.myancast.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.myancast.MyanCastApp
import com.example.myancast.data.repository.PodcastRepositoryImpl
import com.example.myancast.ui.components.MiniPlayer
import com.example.myancast.ui.details.PodcastDetailScreen
import com.example.myancast.ui.home.HomeScreen
import com.example.myancast.ui.library.LibraryScreen
import com.example.myancast.ui.library.LibraryViewModel
import com.example.myancast.ui.news.NewsDetailScreen
import com.example.myancast.ui.news.NewsScreen
import com.example.myancast.ui.player.FullPlayerScreen
import com.example.myancast.ui.player.MiniPlayerViewModel
import com.example.myancast.ui.search.SearchScreen
import com.example.myancast.ui.settings.SettingsScreen
import com.example.myancast.ui.theme.AppConfig
import com.example.myancast.ui.theme.GoldPrimary

@Composable
fun MyanCastNavHost(
    navController: NavHostController,
    config: AppConfig,
    onConfigChange: (AppConfig) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // MiniPlayerViewModel
    val app = LocalContext.current.applicationContext as MyanCastApp
    val miniVm: MiniPlayerViewModel = viewModel(
        factory = MiniPlayerViewModel.factory(app.playerController)
    )

    val miniState by miniVm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column{
                // ★ MiniPlayer — Player screen မှာ ဖျောက်၊ episode ရှိမှ ပြ
                if (miniState.visible && currentRoute != Screen.Player.route) {
                    MiniPlayer(
                        title = miniState.title,
                        subtitle = miniState.subtitle,
                        coverUrl = miniState.coverUrl,
                        progress = miniState.progress,
                        isPlaying = miniState.isPlaying,
                        hasNext = miniState.hasNext,
                        onExpand = {
                            navController.navigate(Screen.Player.route) {
                                launchSingleTop = true
                            }
                        },
                        onPlayPause = miniVm::togglePlayPause,
                        onNext = miniVm::next
                    )
                }

                // Bottom nav
                if (currentRoute in bottomNavRoutes) {
                    MyanCastBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> navController.navigateToTab(route) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // ─── Bottom nav destinations ───
            composable(Screen.Home.route) {
                HomeScreen(
                    onPodcastClick = { id ->
                        navController.navigate(Screen.PodcastDetail.create(id))
                    },
                    onSettingsClick = { navController.navigateToTab(Screen.Settings.route) },
                    // ဆက်နားထောင်ရန် card နှိပ်ရင် Full Player (၂ ခါ ထပ်မဖွင့်အောင် launchSingleTop)
                    onOpenPlayer = {
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.News.route) {
                NewsScreen(
                    onNewsClick = { id ->
                        navController.navigate(Screen.NewsDetail.create(id))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onPodcastClick = { id ->
                        navController.navigate(Screen.PodcastDetail.create(id))
                    }
                )
            }

            composable(Screen.Library.route) {
                val app = LocalContext.current.applicationContext as MyanCastApp
                val libraryVm: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.factory(
                        libraryRepo = app.libraryRepository,
                        podcastRepo = PodcastRepositoryImpl()
                    )
                )
                LibraryScreen(
                    onPodcastClick = { id ->
                        navController.navigate(Screen.PodcastDetail.create(id))
                    },
                    vm = libraryVm
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    config = config,
                    onConfigChange = onConfigChange
                )
            }

            // ─── Detail destinations ───
            composable(
                route = Screen.PodcastDetail.route,
                arguments = listOf(
                    navArgument(Screen.ARG_PODCAST_ID) { type = NavType.StringType }
                )
            ) { entry ->
                PodcastDetailScreen(
                    podcastId = entry.arguments?.getString(Screen.ARG_PODCAST_ID).orEmpty(),
                    // Back မြန်မြန် အကြိမ်ကြိမ် နှိပ်ရင် Home ပါ pop မသွားအောင် —
                    // လက်ရှိ ပေါ်နေတဲ့ entry ကသာ pop လုပ်ခွင့်ရှိ
                    onBack = {
                        if (navController.currentBackStackEntry == entry) {
                            navController.popBackStack()
                        }
                    },
                    // Episode ၂ ခါ မြန်မြန် နှိပ်ရင် Player ၂ ခု မထပ်အောင် (Phase 3 device test #15)
                    onEpisodeClick = {
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.NewsDetail.route,
                arguments = listOf(
                    navArgument(Screen.ARG_NEWS_ID) { type = NavType.StringType }
                )
            ) { entry ->
                NewsDetailScreen(
                    newsId = entry.arguments?.getString(Screen.ARG_NEWS_ID).orEmpty(),
                    onBack = { navController.popBackStack() },
                    // သတင်းမှာ အသံပါပြီး ဖွင့်လို့ရမှသာ ခေါ်တယ် (screen ထဲက စစ်ပြီးသား)
                    onPlay = {
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Player.route) {
                FullPlayerScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * Tab တစ်ခုကို ပြောင်းတဲ့အခါ back stack မကြီးလာအောင် —
 * start destination အထိ pop ပြီး state ကို ပြန်သိမ်း/ပြန်ယူတယ်။
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun MyanCastBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall  // Myanmar family (Poppins မှာ မြန်မာ glyph မရှိ)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GoldPrimary,
                    selectedTextColor = GoldPrimary,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
