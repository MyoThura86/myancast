// ui/navigation/Screen.kt
package com.example.myancast.ui.navigation

/**
 * App တစ်ခုလုံးရဲ့ route အားလုံး။
 * Route string တွေကို ဒီတစ်နေရာတည်းမှာပဲ ထားတယ် — typo ကြောင့် crash မဖြစ်အောင်။
 */
sealed class Screen(val route: String) {

    // ─── Bottom nav destinations ───
    data object Home     : Screen("home")
    data object News     : Screen("news")
    data object Search   : Screen("search")
    data object Library  : Screen("library")
    data object Settings : Screen("settings")

    // ─── Detail destinations ───
    data object PodcastDetail : Screen("podcast/{$ARG_PODCAST_ID}") {
        fun create(podcastId: String) = "podcast/$podcastId"
    }

    data object NewsDetail : Screen("newsDetail/{$ARG_NEWS_ID}") {
        fun create(newsId: String) = "newsDetail/$newsId"
    }

    data object Player : Screen("player")

    // ─── ★ Phase 6 — Admin (Settings → စီမံခန့်ခွဲသူ) ───
    data object AdminLogin : Screen("admin/login")

    data object AdminDashboard : Screen("admin/dashboard")

    data object AdminPodcast : Screen("admin/podcast/{$ARG_PODCAST_ID}") {
        fun create(podcastId: String) = "admin/podcast/$podcastId"
        fun createNew() = create(NEW_ID)
    }

    data object AdminEpisode : Screen("admin/podcast/{$ARG_PODCAST_ID}/episode/{$ARG_EPISODE_ID}") {
        fun create(podcastId: String, episodeId: String) = "admin/podcast/$podcastId/episode/$episodeId"
        fun createNew(podcastId: String) = create(podcastId, NEW_ID)
    }

    companion object {
        const val ARG_PODCAST_ID = "podcastId"
        const val ARG_NEWS_ID = "newsId"
        const val ARG_EPISODE_ID = "episodeId"

        /** Admin route ထဲမှာ ID အစား ဒါပါရင် "အသစ် ဖန်တီး" (Firestore auto ID က ဒီလို မဖြစ်နိုင်) */
        const val NEW_ID = "new"

        /** Admin screen တစ်ခုက နောက်က screen ကို snackbar message ပို့ဖို့ (SavedStateHandle key) */
        const val RESULT_ADMIN_MESSAGE = "admin_message"

        fun isAdminRoute(route: String?): Boolean = route?.startsWith("admin/") == true
    }
}
