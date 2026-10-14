package com.example.myancast.data.download

import com.example.myancast.data.local.DownloadEntity
import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class DownloadMappingTest {

    private val episode = Episode(
        id = "e1",
        podcastId = "p1",
        title = "အပိုင်း ၁",
        audioUrl = "https://a.com/1.mp3",
        duration = 600,
        publishedAt = Timestamp(Date(1_700_000_000_000L)),
        fileSizeBytes = 4_800_000L
    )

    @Test
    fun `episode survives the round trip through Room (offline playback)`() {
        val entity = episode.toDownloadEntity("နည်းပညာ", wifiOnly = true, progress = 0, now = 1L)
        assertEquals(episode, entity.toEpisode())
        assertEquals(DownloadStatus.Queued(wifiOnly = true), entity.toStatus())
    }

    @Test
    fun `status strings map to domain statuses`() {
        val base = episode.toDownloadEntity("x", wifiOnly = false, progress = 0, now = 1L)
        assertEquals(DownloadStatus.Running(45), base.copy(status = DownloadEntity.STATUS_RUNNING, progress = 45).toStatus())
        assertEquals(
            DownloadStatus.Done("/f/e1.mp3", 123L),
            base.copy(status = DownloadEntity.STATUS_DONE, localPath = "/f/e1.mp3", downloadedBytes = 123L).toStatus()
        )
        assertEquals(DownloadStatus.Failed, base.copy(status = DownloadEntity.STATUS_FAILED).toStatus())
    }

    @Test
    fun `resume mode follows the http status`() {
        assertEquals(ResumeMode.APPEND, resumeModeFor(206, existingBytes = 1000))
        assertEquals(ResumeMode.RESTART, resumeModeFor(200, existingBytes = 1000))   // Range မထောက်ပံ့
        assertEquals(ResumeMode.RESTART, resumeModeFor(200, existingBytes = 0))
        assertEquals(ResumeMode.ALREADY_COMPLETE, resumeModeFor(416, existingBytes = 1000))
        assertEquals(ResumeMode.ERROR, resumeModeFor(404, existingBytes = 0))
        assertEquals(ResumeMode.ERROR, resumeModeFor(416, existingBytes = 0))
    }

    @Test
    fun `percent is clamped and safe without a length`() {
        assertEquals(50, downloadPercent(50, 100))
        assertEquals(100, downloadPercent(150, 100))
        assertEquals(0, downloadPercent(10, -1))
    }

    @Test
    fun `total download bytes sums sizes`() {
        val items = listOf(
            DownloadedEpisode(episode, "x", 10_000_000L),
            DownloadedEpisode(episode.copy(id = "e2"), "x", 2_000_000L)
        )
        assertEquals(12_000_000L, totalDownloadBytes(items))
    }
}
