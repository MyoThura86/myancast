package com.example.myancast.ui.library

import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.PlaybackProgress
import com.example.myancast.domain.model.Podcast

enum class LibraryTab {
    SUBSCRIBED,
    HISTORY,
    DOWNLOADS     // ★ Phase 6 (#4)
}

data class LibraryUiState(
    val tab: LibraryTab = LibraryTab.SUBSCRIBED,
    val subscribed: List<Podcast> = emptyList(),
    val history: List<HistoryRow> = emptyList(),
    // ★ Phase 6 (#4) — offline မှာလည်း ဖွင့်လို့ရ
    val downloads: List<DownloadedEpisode> = emptyList(),
    /** "~၄၅ MB" · ဒေါင်းလုဒ် မရှိရင် "" */
    val downloadsTotal: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

data class HistoryRow(
    val episodeId: String = "",
    val episodeTitle: String = "",
    val podcastId: String = "",
    val podcastTitle: String = "",
    val coverUrl: String = "",
    val progress: PlaybackProgress
)

/**
 * ★ Phase 6 (#4) — ဒေါင်းထားတဲ့ အပိုင်း နှိပ်ရင် ဖွင့်မယ့် queue —
 * Podcast တူတဲ့ ဒေါင်းလုဒ်တွေကို အသစ်ဆုံး အရင် (Detail screen လို) စီ (offline မှာ ⏭ ရအောင်)
 */
fun downloadedQueueFor(items: List<DownloadedEpisode>, episodeId: String): List<Episode> {
    val target = items.find { it.episode.id == episodeId } ?: return emptyList()
    return items
        .filter { it.episode.podcastId == target.episode.podcastId }
        .sortedByDescending { it.episode.publishedAt?.toDate()?.time ?: 0L }
        .map { it.episode }
}
