package com.example.myancast.ui.components

import com.example.myancast.FakeLibraryRepository
import com.example.myancast.FakeNetworkMonitor
import com.example.myancast.FakeNewsRepository
import com.example.myancast.FakePlayerController
import com.example.myancast.FakePodcastRepository
import com.example.myancast.domain.model.Podcast
import com.example.myancast.ui.home.HomeViewModel
import com.example.myancast.ui.news.NewsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** ★ Phase 6 (#2) — offline မှာ "data မရှိသေးပါ" အစား error မှန်မှန် ပြရမယ် */
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineStateTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `offline and empty is an error`() {
        assertEquals(OFFLINE_EMPTY_ERROR, offlineErrorOrNull(isEmpty = true, online = false))
    }

    @Test
    fun `cached data offline or empty online is not an error`() {
        assertNull(offlineErrorOrNull(isEmpty = false, online = false))   // cache ပြ + banner
        assertNull(offlineErrorOrNull(isEmpty = true, online = true))     // တကယ် data မရှိ → EmptyView
    }

    @Test
    fun `home shows the offline error on a fresh offline install`() = runTest {
        val vm = HomeViewModel(
            repo = FakePodcastRepository(emptyList()),
            libraryRepo = FakeLibraryRepository(),
            player = FakePlayerController(),
            network = FakeNetworkMonitor(online = false)
        )
        advanceUntilIdle()

        assertEquals(OFFLINE_EMPTY_ERROR, vm.state.value.error)
    }

    @Test
    fun `home with cached data offline shows the data`() = runTest {
        val vm = HomeViewModel(
            repo = FakePodcastRepository(listOf(Podcast(id = "1", title = "x", category = "y"))),
            libraryRepo = FakeLibraryRepository(),
            player = FakePlayerController(),
            network = FakeNetworkMonitor(online = false)
        )
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals(1, vm.state.value.allPodcasts.size)
    }

    @Test
    fun `home clears the offline error when the network comes back`() = runTest {
        val network = FakeNetworkMonitor(online = false)
        val vm = HomeViewModel(
            repo = FakePodcastRepository(emptyList()),
            libraryRepo = FakeLibraryRepository(),
            player = FakePlayerController(),
            network = network
        )
        advanceUntilIdle()

        network.set(online = true)
        advanceUntilIdle()

        assertNull(vm.state.value.error)   // ပြန်ဆွဲပြီး — တကယ်ကို အလွတ်ဆို EmptyView
    }

    @Test
    fun `news shows the offline error on a fresh offline install`() = runTest {
        val vm = NewsViewModel(FakeNewsRepository(emptyList()), FakeNetworkMonitor(online = false))
        advanceUntilIdle()

        assertEquals(OFFLINE_EMPTY_ERROR, vm.state.value.error)
    }
}
