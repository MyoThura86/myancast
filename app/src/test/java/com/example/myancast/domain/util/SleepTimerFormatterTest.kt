package com.example.myancast.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SleepTimerFormatterTest {

    private val now = 1_000_000L

    @Test
    fun `no timer gives an empty label`() {
        assertEquals("", sleepTimerLabel(endsAtMs = null, atEpisodeEnd = false, nowMs = now))
    }

    @Test
    fun `remaining minutes are rounded up`() {
        assertEquals("၁၂ မိနစ် ကျန်", sleepTimerLabel(now + 11 * 60_000L + 1, false, now))
        assertEquals("၁၅ မိနစ် ကျန်", sleepTimerLabel(now + 15 * 60_000L, false, now))
    }

    @Test
    fun `under a minute or already past still shows one minute`() {
        assertEquals("၁ မိနစ် ကျန်", sleepTimerLabel(now + 5_000L, false, now))
        assertEquals("၁ မိနစ် ကျန်", sleepTimerLabel(now - 5_000L, false, now))   // fade နေတုန်း
    }

    @Test
    fun `end of episode wins over minutes`() {
        assertEquals("ဒီပိုင်း ဆုံးရင် ရပ်မယ်", sleepTimerLabel(null, atEpisodeEnd = true, nowMs = now))
    }

    @Test
    fun `menu labels use minutes and hours`() {
        assertEquals("၁၅ မိနစ်", sleepTimerOptionLabel(15))
        assertEquals("၁ နာရီ", sleepTimerOptionLabel(60))
    }
}
