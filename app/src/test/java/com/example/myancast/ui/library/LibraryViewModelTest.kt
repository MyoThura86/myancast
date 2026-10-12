package com.example.myancast.ui.library

import com.example.myancast.FakeLibraryRepository
import com.example.myancast.FakePodcastRepository
import com.example.myancast.data.repository.PodcastRepository
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.PlaybackProgress
import com.example.myancast.domain.model.Podcast
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val fakePodcasts = listOf(
        Podcast("p1", "Podcast 1", "", "", "Tech", 5),
        Podcast("p2", "Podcast 2", "", "", "News", 3)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `subscribed list filters by subscribedIds`() = runTest {
        val libraryRepo = FakeLibraryRepository(initialSubscribed = setOf("p1"))
        val podcastRepo = FakePodcastRepository(fakePodcasts)
        val vm = LibraryViewModel(libraryRepo, podcastRepo)

        val collectJob = launch { vm.state.collect { } }   // ← ★ ပြင်
        advanceUntilIdle()

        assertEquals(1, vm.state.value.subscribed.size)
        assertEquals("p1", vm.state.value.subscribed[0].id)
        assertFalse(vm.state.value.isLoading)

        collectJob.cancel()
    }

    @Test
    fun `history maps to HistoryRow`() = runTest {
        val progress = PlaybackProgress(
            episodeId = "ep1",
            podcastId = "p1",
            positionMs = 5000L,
            durationMs = 10000L,
            updatedAt = 1L,
            episodeTitle = "Episode 1",
            podcastTitle = "Podcast 1",
            coverUrl = "https://example.com/cover.jpg"
        )
        val libraryRepo = FakeLibraryRepository(initialHistory = listOf(progress))
        val podcastRepo = FakePodcastRepository(fakePodcasts)
        val vm = LibraryViewModel(libraryRepo, podcastRepo)

        val collectJob = launch { vm.state.collect { } }   // ← ★ ပြင်
        advanceUntilIdle()

        assertEquals(1, vm.state.value.history.size)
        assertEquals("Episode 1", vm.state.value.history[0].episodeTitle)
        assertEquals("Podcast 1", vm.state.value.history[0].podcastTitle)

        collectJob.cancel()
    }

    @Test
    fun `error state shows message`() = runTest {
        val vm = LibraryViewModel(FakeLibraryRepository(), FailingPodcastRepository(failures = 1))

        val collectJob = launch { vm.state.collect { } }
        advanceUntilIdle()

        assertEquals("offline", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)

        collectJob.cancel()
    }

    @Test
    fun `retry after error loads data again`() = runTest {
        // ပထမ တစ်ခါ fail၊ ဒုတိယ အောင် — catch ပြီး flow ဆုံးသွားလည်း retry က ပြန်စနိုင်ရမယ်
        val vm = LibraryViewModel(
            FakeLibraryRepository(initialSubscribed = setOf("p1")),
            FailingPodcastRepository(failures = 1, podcasts = fakePodcasts)
        )

        val collectJob = launch { vm.state.collect { } }
        advanceUntilIdle()
        assertEquals("offline", vm.state.value.error)

        vm.retry()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals(listOf("p1"), vm.state.value.subscribed.map { it.id })

        collectJob.cancel()
    }

    @Test
    fun `selected tab survives retry`() = runTest {
        val vm = LibraryViewModel(FakeLibraryRepository(), FailingPodcastRepository(failures = 1))

        val collectJob = launch { vm.state.collect { } }
        vm.selectTab(LibraryTab.HISTORY)
        advanceUntilIdle()

        vm.retry()
        advanceUntilIdle()

        assertEquals(LibraryTab.HISTORY, vm.state.value.tab)

        collectJob.cancel()
    }
}

/** `getPodcasts()` ကို ပထမ [failures] ခါ fail စေတဲ့ fake — retry စမ်းဖို့ */
private class FailingPodcastRepository(
    private var failures: Int,
    private val podcasts: List<Podcast> = emptyList()
) : PodcastRepository {

    override fun getPodcasts(): Flow<List<Podcast>> = flow {
        if (failures > 0) {
            failures--
            throw IOException("offline")
        }
        emit(podcasts)
    }

    override fun getEpisodes(podcastId: String): Flow<List<Episode>> = flowOf(emptyList())

    override suspend fun getPodcast(id: String): Podcast? = podcasts.find { it.id == id }
}