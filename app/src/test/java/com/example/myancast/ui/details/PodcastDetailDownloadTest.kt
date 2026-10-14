package com.example.myancast.ui.details

import com.example.myancast.FakeDownloadRepository
import com.example.myancast.FakeLibraryRepository
import com.example.myancast.FakeNetworkMonitor
import com.example.myancast.FakePlayerController
import com.example.myancast.FakePodcastRepository
import com.example.myancast.domain.model.AppSettings
import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.model.DownloadedEpisode
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** ★ Phase 6 (#4 ဒေါင်းလုဒ် · #5 Data ချွေတာ) */
@OptIn(ExperimentalCoroutinesApi::class)
class PodcastDetailDownloadTest {

    private val dispatcher = StandardTestDispatcher()
    private val podcast = Podcast(id = "p1", title = "နည်းပညာ", category = "နည်းပညာ")
    private val ep1 = Episode(id = "e1", podcastId = "p1", title = "၁", audioUrl = "https://a.com/1.mp3", duration = 1470)
    private val ep2 = Episode(id = "e2", podcastId = "p1", title = "၂", audioUrl = "https://a.com/2.mp3", duration = 600)

    private lateinit var downloads: FakeDownloadRepository
    private lateinit var network: FakeNetworkMonitor
    private lateinit var player: FakePlayerController

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        downloads = FakeDownloadRepository()
        network = FakeNetworkMonitor()
        player = FakePlayerController()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(settings: AppSettings = AppSettings()) = PodcastDetailViewModel(
        podcastId = "p1",
        repo = FakePodcastRepository(listOf(podcast), listOf(ep1, ep2)),
        player = player,
        libraryRepo = FakeLibraryRepository(),
        downloads = downloads,
        network = network,
        settings = MutableStateFlow(settings)
    )

    @Test
    fun `tapping download enqueues with the wifi only setting`() = runTest {
        val vm = vm(AppSettings(wifiOnlyDownload = true))
        advanceUntilIdle()

        vm.onDownloadClick("e1")
        advanceUntilIdle()

        val (episode, podcastTitle, wifiOnly) = downloads.enqueued.single()
        assertEquals("e1", episode.id)
        assertEquals("နည်းပညာ", podcastTitle)
        assertTrue(wifiOnly)
        assertNull(vm.state.value.message)   // Wi-Fi ပေါ်မှာ — စောင့်စရာ မလို
    }

    @Test
    fun `on mobile data with wifi only the user is told it will wait`() = runTest {
        network.set(online = true, metered = true)
        val vm = vm(AppSettings(wifiOnlyDownload = true))
        advanceUntilIdle()

        vm.onDownloadClick("e1")
        advanceUntilIdle()

        assertEquals("Wi-Fi ရရင် ဒေါင်းပါမယ်", vm.state.value.message)
    }

    @Test
    fun `tapping a running download cancels it`() = runTest {
        downloads.setStatus("e1", DownloadStatus.Running(40))
        val vm = vm()
        advanceUntilIdle()

        vm.onDownloadClick("e1")
        advanceUntilIdle()

        assertEquals(listOf("e1"), downloads.deleted)
    }

    @Test
    fun `tapping a finished download asks before deleting`() = runTest {
        downloads.setStatus("e1", DownloadStatus.Done("/f/e1.mp3", 1L))
        val vm = vm()
        advanceUntilIdle()

        vm.onDownloadClick("e1")
        assertEquals(ep1.id, vm.state.value.confirmDeleteDownload?.id)
        assertTrue(downloads.deleted.isEmpty())

        vm.confirmDeleteDownload()
        advanceUntilIdle()
        assertEquals(listOf("e1"), downloads.deleted)
        assertNull(vm.state.value.confirmDeleteDownload)
    }

    @Test
    fun `data saver on mobile data asks before streaming`() = runTest {
        network.set(online = true, metered = true)
        val vm = vm(AppSettings(dataSaver = true))
        advanceUntilIdle()

        val opened = vm.playEpisode("e1")

        assertFalse(opened)
        assertEquals(0, player.playCallCount)
        val pending = vm.state.value.pendingStream
        assertNotNull(pending)
        assertEquals(11_760_000L, pending?.sizeBytes)   // 24:30 × 64 kbps

        assertTrue(vm.confirmStream())
        assertEquals(1, player.playCallCount)
        assertNull(vm.state.value.pendingStream)
    }

    @Test
    fun `downloaded episode plays without asking even on mobile data`() = runTest {
        network.set(online = true, metered = true)
        downloads = FakeDownloadRepository(listOf(DownloadedEpisode(ep1, "နည်းပညာ", 1L)))
        val vm = vm(AppSettings(dataSaver = true))
        advanceUntilIdle()

        assertTrue(vm.playEpisode("e1"))
        assertEquals(1, player.playCallCount)
    }

    @Test
    fun `wifi or data saver off plays immediately`() = runTest {
        val vm = vm(AppSettings(dataSaver = true))   // Wi-Fi (metered = false)
        advanceUntilIdle()

        assertTrue(vm.playAll())
        assertEquals(1, player.playCallCount)
    }

    @Test
    fun `dismissing the dialog does not play`() = runTest {
        network.set(online = true, metered = true)
        val vm = vm(AppSettings(dataSaver = true))
        advanceUntilIdle()

        vm.playAll()
        vm.dismissStream()

        assertNull(vm.state.value.pendingStream)
        assertEquals(0, player.playCallCount)
    }
}
