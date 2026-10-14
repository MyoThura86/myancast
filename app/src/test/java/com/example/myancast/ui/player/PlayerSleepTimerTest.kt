package com.example.myancast.ui.player

import com.example.myancast.FakePlayerController
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.PlaybackState
import com.example.myancast.domain.model.SleepTimerOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** ★ Phase 6 (#3) */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerSleepTimerTest {

    private val playing = PlaybackState(
        queue = listOf(Episode(id = "e1", title = "x", audioUrl = "https://a.com/1.mp3")),
        currentIndex = 0,
        isPlaying = true
    )

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `menu choice reaches the player controller`() {
        val player = FakePlayerController(playing)
        val vm = PlayerViewModel(player)

        vm.setSleepTimer(SleepTimerOption.Minutes(30))
        assertEquals(SleepTimerOption.Minutes(30), player.lastSleepTimer)

        vm.setSleepTimer(SleepTimerOption.Off)
        assertEquals(SleepTimerOption.Off, player.lastSleepTimer)
    }

    @Test
    fun `ui state shows the remaining minutes`() {
        val ui = playing.copy(sleepTimerEndsAtMs = 10 * 60_000L).toUiState(nowMs = 0L)
        assertEquals("၁၀ မိနစ် ကျန်", ui.sleepTimerLabel)
        assertTrue(ui.sleepTimerActive)
    }

    @Test
    fun `end of episode timer is active with its own label`() {
        val ui = playing.copy(sleepAtEpisodeEnd = true).toUiState(nowMs = 0L)
        assertEquals("ဒီပိုင်း ဆုံးရင် ရပ်မယ်", ui.sleepTimerLabel)
        assertTrue(ui.sleepAtEpisodeEnd)
    }

    @Test
    fun `no timer means inactive`() {
        assertFalse(playing.toUiState(nowMs = 0L).sleepTimerActive)
    }
}
