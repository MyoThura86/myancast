package com.example.myancast.domain.model

/** ★ Phase 6 (#4) — အပိုင်း တစ်ခုရဲ့ ဒေါင်းလုဒ် အခြေအနေ */
sealed interface DownloadStatus {
    /** စောင့်နေ — [wifiOnly] ဆို Wi-Fi ရမှ စမယ် */
    data class Queued(val wifiOnly: Boolean) : DownloadStatus
    data class Running(val percent: Int) : DownloadStatus
    data class Done(val localPath: String, val sizeBytes: Long) : DownloadStatus
    data object Failed : DownloadStatus
}

/** Library "ဒေါင်းလုဒ်" tab — offline မှာလည်း ဖွင့်လို့ရအောင် Episode အပြည့် သိမ်း */
data class DownloadedEpisode(
    val episode: Episode,
    val podcastTitle: String,
    val sizeBytes: Long
)
