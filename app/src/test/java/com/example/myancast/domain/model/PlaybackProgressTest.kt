package com.example.myancast.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ဆက်နားထောင်ရန် card က `isFinished` (၉၅%) ပေါ်မှာ မူတည်ပြီး ပေါ်/ပျောက် */
class PlaybackProgressTest {

    private fun progress(positionMs: Long, durationMs: Long) = PlaybackProgress(
        episodeId = "e1",
        podcastId = "p1",
        positionMs = positionMs,
        durationMs = durationMs,
        updatedAt = 0L
    )

    @Test
    fun `fraction is zero when duration unknown`() {
        assertEquals(0f, progress(30_000, 0).fraction, 0f)
        assertFalse(progress(30_000, 0).isFinished)
    }

    @Test
    fun `fraction is position over duration`() {
        assertEquals(0.5f, progress(30_000, 60_000).fraction, 0.0001f)
    }

    @Test
    fun `just under 95 percent is not finished`() {
        assertFalse(progress(94_000, 100_000).isFinished)
    }

    @Test
    fun `95 percent or more is finished`() {
        assertTrue(progress(95_000, 100_000).isFinished)
        assertTrue(progress(120_000, 100_000).isFinished)   // clamp — duration ကျော်လည်း ပြီး
    }
}
