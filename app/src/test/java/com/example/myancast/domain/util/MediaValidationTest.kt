package com.example.myancast.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class MediaValidationTest {

    private val yangon = TimeZone.getTimeZone("Asia/Yangon")   // UTC+6:30

    // ─── URL ───

    @Test
    fun `https and http links with a host are valid`() {
        assertTrue(isValidWebUrl("https://example.com/a.mp3"))
        assertTrue(isValidWebUrl("http://cdn.example.org/path/cover.jpg?x=1"))
        assertTrue(isValidWebUrl("  https://example.com  "))   // ရှေ့နောက် space ကို trim
    }

    @Test
    fun `missing scheme, spaces or host are invalid`() {
        assertFalse(isValidWebUrl(""))
        assertFalse(isValidWebUrl("example.com/a.mp3"))
        assertFalse(isValidWebUrl("ftp://example.com/a.mp3"))
        assertFalse(isValidWebUrl("https://exa mple.com"))
        assertFalse(isValidWebUrl("https://"))
        assertFalse(isValidWebUrl("https://nohost"))
    }

    // ─── MP3 ───

    @Test
    fun `mp3 mime types are accepted`() {
        assertTrue(isMp3("audio/mpeg", "x"))
        assertTrue(isMp3("audio/mp3", "x"))
        assertTrue(isMp3("audio/mpeg; charset=binary", "x"))
    }

    @Test
    fun `unknown mime falls back to the file extension`() {
        assertTrue(isMp3(null, "episode-01.MP3"))
        assertTrue(isMp3("application/octet-stream", "https://a.com/ep.mp3?token=1"))
        assertFalse(isMp3(null, "episode.wav"))
    }

    @Test
    fun `other known audio types are rejected even with mp3 extension`() {
        assertFalse(isMp3("audio/mp4", "fake.mp3"))
        assertFalse(isMp3("audio/x-wav", "x.wav"))
    }

    // ─── Duration ───

    @Test
    fun `duration input parses minutes, mm-ss and h-mm-ss`() {
        assertEquals(1470, parseDurationInput("24:30"))
        assertEquals(3725, parseDurationInput("1:02:05"))
        assertEquals(2700, parseDurationInput("45"))
        assertEquals(5, parseDurationInput("0:05"))
    }

    @Test
    fun `invalid duration input returns null`() {
        assertNull(parseDurationInput(""))
        assertNull(parseDurationInput("24:75"))      // စက္ကန့် ၆၀ ကျော်
        assertNull(parseDurationInput("abc"))
        assertNull(parseDurationInput("0:00"))       // 0 = မသိ — မလက်ခံ
        assertNull(parseDurationInput("1:2:3:4"))
    }

    @Test
    fun `Myanmar keyboard digits are accepted too`() {
        assertEquals(1470, parseDurationInput("၂၄:၃၀"))
    }

    @Test
    fun `formatDurationInput round trips with parse`() {
        assertEquals("24:30", formatDurationInput(1470))
        assertEquals("1:02:05", formatDurationInput(3725))
        assertEquals("", formatDurationInput(0))
        assertEquals(1470, parseDurationInput(formatDurationInput(1470)))
    }

    // ─── Date / time ───

    @Test
    fun `combineDateAndTime keeps the picked calendar day in the local zone`() {
        // DatePicker — 2026-10-15 UTC သန်းခေါင်
        val pickedUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear(); set(2026, Calendar.OCTOBER, 15)
        }.timeInMillis

        val millis = combineDateAndTime(pickedUtc, 18, 30, yangon)

        val local = Calendar.getInstance(yangon).apply { timeInMillis = millis }
        assertEquals(15, local.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, local.get(Calendar.MONTH))
        assertEquals(18, local.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, local.get(Calendar.MINUTE))
    }

    @Test
    fun `toDatePickerMillis is the inverse of combineDateAndTime for the date part`() {
        val original = combineDateAndTime(
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(2026, Calendar.JANUARY, 1) }.timeInMillis,
            1, 15, yangon   // မြန်မာ မနက် ၁ နာရီ = UTC မှာ ယခင်နေ့ — ရက် မလွဲရ
        )
        val back = toDatePickerMillis(original, yangon)
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = back }
        assertEquals(1, utc.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JANUARY, utc.get(Calendar.MONTH))
        assertEquals(1 to 15, hourAndMinute(original, yangon))
    }

    @Test
    fun `publish label uses Myanmar month and digits`() {
        val millis = Calendar.getInstance(yangon).apply {
            clear(); set(2026, Calendar.OCTOBER, 15, 18, 30)
        }.timeInMillis
        assertEquals("၁၅ အောက်တိုဘာ ၂၀၂၆ · ၁၈:၃၀", formatPublishDateTime(millis, yangon))
    }
}
