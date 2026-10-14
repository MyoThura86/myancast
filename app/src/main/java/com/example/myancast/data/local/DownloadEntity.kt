package com.example.myancast.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ★ Phase 6 (#4) — ဒေါင်းလုဒ် တစ်ခု။ Firestore မရလည်း (offline) Episode ပြန်ဆောက်လို့ရအောင် field အပြည့် သိမ်း။
 * ⚠️ Column တစ်ခု ပြောင်းရင်တောင် `AppDatabase.version` တိုးပြီး Migration ရေးရမယ်။
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val episodeId: String,
    val podcastId: String,
    val podcastTitle: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val duration: Int,
    val publishedAtMillis: Long?,
    val coverUrl: String,
    val fileSizeBytes: Long,
    val status: String,          // STATUS_*
    val progress: Int,           // 0..100
    val wifiOnly: Boolean,
    val localPath: String,       // ဒေါင်းပြီးမှ ရှိ
    val downloadedBytes: Long,
    val updatedAt: Long
) {
    companion object {
        const val STATUS_QUEUED = "queued"
        const val STATUS_RUNNING = "running"
        const val STATUS_DONE = "done"
        const val STATUS_FAILED = "failed"
    }
}
