package com.example.myancast.ui.library

import com.example.myancast.FakeDownloadRepository
import com.example.myancast.FakeLibraryRepository
import com.example.myancast.FakePlayerController
import com.example.myancast.FakePodcastRepository
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/** ★ Phase 6 (#4) — Library "ဒေါင်းလုဒ်" tab (offline နားထောင်ခြင်း) */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryDownloadsTest {

    private val dispatcher = StandardTestDispatcher()

    private fun ep(id: String, podcastId: String, publishedMillis: Long) = Episode(
        id = id, podcastId = podcastId, title = id, audioUrl = "https://a.com/$id.mp3",
        publishedAt = Timestamp(Date(publishedMillis))
    )

    private val downloaded = listOf(
        DownloadedEpisode(ep("old", "p1", 1_000), "နည်းပညာ", 10_000_000L),
        DownloadedEpisode(ep("other", "p2", 5_000), "သတင်း", 2_000_000L),
        DownloadedEpisode(ep("new", "p1", 9_000), "နည်းပညာ", 12_000_000L)
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `downloads tab lists files with the total size`() = runTest {
        val vm = LibraryViewModel(FakeLibraryRepository(), FakePodcastRepository(), FakeDownloadRepository(downloaded))
        backgroundScope.launch { vm.state.collect {} }
        advanceUntilIdle()

        vm.selectTab(LibraryTab.DOWNLOADS)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(LibraryTab.DOWNLOADS, state.tab)
        assertEquals(3, state.downloads.size)
        assertEquals("~၂၄ MB", state.downloadsTotal)
    }

    @Test
    fun `playing a download queues that podcast's downloads newest first`() = runTest {
        val player = FakePlayerController()
        val vm = LibraryViewModel(FakeLibraryRepository(), FakePodcastRepository(), FakeDownloadRepository(downloaded), player)
        backgroundScope.launch { vm.state.collect {} }
        advanceUntilIdle()

        assertTrue(vm.playDownloaded("old"))

        val queue = player.lastQueue
        assertEquals(listOf("new", "old"), queue?.episodes?.map { it.id })   // p2 မပါ
        assertEquals(1, queue?.startIndex)
        assertEquals("နည်းပညာ", player.lastPodcastTitle)
    }

    @Test
    fun `deleting removes the download`() = runTest {
        val downloads = FakeDownloadRepository(downloaded)
        val vm = LibraryViewModel(FakeLibraryRepository(), FakePodcastRepository(), downloads)
        backgroundScope.launch { vm.state.collect {} }
        advanceUntilIdle()

        vm.deleteDownload("other")
        advanceUntilIdle()

        assertEquals(listOf("other"), downloads.deleted)
        assertEquals(2, vm.state.value.downloads.size)
    }

    @Test
    fun `queue helper is empty for an unknown episode`() = runTest {
        assertTrue(downloadedQueueFor(downloaded, "missing").isEmpty())
        assertEquals(listOf("new", "old"), downloadedQueueFor(downloaded, "new").map { it.id })
        // flow ကိုယ်တိုင် စစ် — downloads မရှိရင် list အလွတ်
        assertTrue(FakeDownloadRepository().downloadedEpisodes().first().isEmpty())
    }
}
