package com.example.myancast.domain.util

import com.example.myancast.domain.model.Episode
import org.junit.Assert.assertEquals
import org.junit.Test

class DataSizeFormatterTest {

    @Test
    fun `exact fileSizeBytes wins over the estimate`() {
        val ep = Episode(duration = 1470, fileSizeBytes = 20_000_000L)
        assertEquals(20_000_000L, ep.estimatedSizeBytes())
    }

    @Test
    fun `missing size is estimated from duration at 64 kbps`() {
        // 24:30 × 64 kbps = 1470 × 8000 B
        assertEquals(11_760_000L, Episode(duration = 1470).estimatedSizeBytes())
    }

    @Test
    fun `unknown size and duration gives zero`() {
        assertEquals(0L, Episode().estimatedSizeBytes())
        assertEquals(0L, Episode(duration = -5).estimatedSizeBytes())
    }

    @Test
    fun `megabytes are rounded and shown in Myanmar digits`() {
        assertEquals("~၁၂ MB", formatMegabytes(11_760_000L))
        assertEquals("~၁၁ MB", formatMegabytes(11_400_000L))
        assertEquals("~၁ MB", formatMegabytes(1_000_000L))
        assertEquals("~၁၂၀ MB", formatMegabytes(120_000_000L))   // နာရီကျော် ပိုင်း
    }

    @Test
    fun `under one megabyte and zero`() {
        assertEquals("~၁ MB အောက်", formatMegabytes(400_000L))
        assertEquals("", formatMegabytes(0L))
        assertEquals("", formatMegabytes(-1L))
    }
}
