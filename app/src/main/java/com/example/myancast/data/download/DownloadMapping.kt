package com.example.myancast.data.download

import com.example.myancast.data.local.DownloadEntity
import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import com.google.firebase.Timestamp
import java.util.Date

// ★ Phase 6 (#4) — Room entity ↔ domain (pure — Robolectric မလိုဘဲ test လုပ်လို့ရ)

fun DownloadEntity.toStatus(): DownloadStatus = when (status) {
    DownloadEntity.STATUS_RUNNING -> DownloadStatus.Running(progress.coerceIn(0, 100))
    DownloadEntity.STATUS_DONE -> DownloadStatus.Done(localPath, downloadedBytes)
    DownloadEntity.STATUS_FAILED -> DownloadStatus.Failed
    else -> DownloadStatus.Queued(wifiOnly)
}

/** Offline မှာ ဖွင့်လို့ရအောင် — Firestore မလိုဘဲ Episode ပြန်ဆောက် */
fun DownloadEntity.toEpisode(): Episode = Episode(
    id = episodeId,
    podcastId = podcastId,
    title = title,
    description = description,
    audioUrl = audioUrl,
    duration = duration,
    publishedAt = publishedAtMillis?.let { Timestamp(Date(it)) },
    coverUrl = coverUrl,
    fileSizeBytes = fileSizeBytes
)

fun DownloadEntity.toDownloaded(): DownloadedEpisode =
    DownloadedEpisode(episode = toEpisode(), podcastTitle = podcastTitle, sizeBytes = downloadedBytes)

fun Episode.toDownloadEntity(
    podcastTitle: String,
    wifiOnly: Boolean,
    progress: Int,
    now: Long
): DownloadEntity = DownloadEntity(
    episodeId = id,
    podcastId = podcastId,
    podcastTitle = podcastTitle,
    title = title,
    description = description,
    audioUrl = audioUrl,
    duration = duration,
    publishedAtMillis = publishedAt?.toDate()?.time,
    coverUrl = coverUrl,
    fileSizeBytes = fileSizeBytes,
    status = DownloadEntity.STATUS_QUEUED,
    progress = progress,
    wifiOnly = wifiOnly,
    localPath = "",
    downloadedBytes = 0L,
    updatedAt = now
)

/** Library ရဲ့ "စုစုပေါင်း ~၄၅ MB" */
fun totalDownloadBytes(items: List<DownloadedEpisode>): Long = items.sumOf { it.sizeBytes.coerceAtLeast(0L) }

/**
 * Range resume — `.part` ဖိုင်မှာ [existingBytes] ရှိပြီးသား ဆို server ကို ကျန်တာပဲ တောင်း။
 * 206 = ကျန်တာ ပို့ (append) · 200 = Range မထောက်ပံ့ (အစက ပြန်ရေး) · 416 = ဒေါင်းပြီးသား။
 */
enum class ResumeMode { APPEND, RESTART, ALREADY_COMPLETE, ERROR }

fun resumeModeFor(httpCode: Int, existingBytes: Long): ResumeMode = when {
    httpCode == 206 && existingBytes > 0 -> ResumeMode.APPEND
    httpCode == 200 || httpCode == 206 -> ResumeMode.RESTART
    httpCode == 416 && existingBytes > 0 -> ResumeMode.ALREADY_COMPLETE
    else -> ResumeMode.ERROR
}

/** 1% တိုးမှ progress update — Room/WorkManager ကို ခဏခဏ မခေါ်အောင် */
fun downloadPercent(written: Long, total: Long): Int =
    if (total <= 0) 0 else ((written * 100) / total).toInt().coerceIn(0, 100)
