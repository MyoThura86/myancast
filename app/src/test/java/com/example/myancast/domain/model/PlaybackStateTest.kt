package com.example.myancast.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 3 contract — UI အားလုံး ဒီ computed property တွေကို မှီခိုတယ် */
class PlaybackStateTest {

    private val queue = listOf(
        Episode(id = "e1"),
        Episode(id = "e2"),
        Episode(id = "e3")
    )

    // ──────── currentEpisode ────────

    @Test
    fun `default state has no episode`() {
        val state = PlaybackState()
        assertNull(state.currentEpisode)
        assertFalse(state.hasEpisode)
        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    @Test
    fun `currentEpisode follows currentIndex`() {
        val state = PlaybackState(queue = queue, currentIndex = 1)
        assertEquals("e2", state.currentEpisode?.id)
        assertTrue(state.hasEpisode)
    }

    @Test
    fun `out of range index means no episode`() {
        assertNull(PlaybackState(queue = queue, currentIndex = 3).currentEpisode)
        assertNull(PlaybackState(queue = queue, currentIndex = -1).currentEpisode)
    }

    // ──────── hasNext / hasPrevious (⏮ ⏭ disable) ────────

    @Test
    fun `first episode has next but no previous`() {
        val state = PlaybackState(queue = queue, currentIndex = 0)
        assertTrue(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    @Test
    fun `last episode has previous but no next`() {
        val state = PlaybackState(queue = queue, currentIndex = 2)
        assertFalse(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun `single episode queue has neither next nor previous`() {
        val state = PlaybackState(queue = listOf(Episode(id = "only")), currentIndex = 0)
        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    // ──────── progress ────────

    @Test
    fun `progress is zero when duration unknown`() {
        assertEquals(0f, PlaybackState(positionMs = 5_000, durationMs = 0).progress, 0f)
    }

    @Test
    fun `progress is position over duration`() {
        assertEquals(0.25f, PlaybackState(positionMs = 15_000, durationMs = 60_000).progress, 0.0001f)
    }

    @Test
    fun `progress is clamped to one`() {
        assertEquals(1f, PlaybackState(positionMs = 70_000, durationMs = 60_000).progress, 0f)
    }
}
