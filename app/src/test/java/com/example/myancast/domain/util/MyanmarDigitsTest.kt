package com.example.myancast.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MyanmarDigitsTest {

    @Test
    fun `all ten digits map to Myanmar digits`() {
        assertEquals("၁၂၃၄၅၆၇၈၉၀", 1234567890L.toMyanmarDigits())
    }

    @Test
    fun `zero and multi digit numbers`() {
        assertEquals("၀", 0.toMyanmarDigits())
        assertEquals("၂၄", 24.toMyanmarDigits())
        assertEquals("၁၀၉", 109.toMyanmarDigits())
    }

    @Test
    fun `negative sign is kept`() {
        assertEquals("-၅", (-5).toMyanmarDigits())
    }
}
