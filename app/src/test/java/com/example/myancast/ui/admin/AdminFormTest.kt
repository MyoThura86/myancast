package com.example.myancast.ui.admin

import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.model.MediaInput
import com.example.myancast.domain.model.Podcast
import com.example.myancast.ui.home.ALL_CATEGORY
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class AdminFormTest {

    private val validPodcast = PodcastForm(
        title = "နည်းပညာ",
        category = "နည်းပညာ",
        cover = MediaField(SourceMode.URL, urlText = "https://a.com/c.jpg")
    )

    private val validEpisode = EpisodeForm(
        title = "အပိုင်း ၁",
        durationText = "24:30",
        audio = MediaField(SourceMode.URL, urlText = "https://a.com/e.mp3")
    )

    // ─── Podcast ───

    @Test
    fun `valid podcast form has no errors`() {
        assertFalse(validatePodcastForm(validPodcast, coverLoadFailed = false).hasAny)
    }

    @Test
    fun `blank title and category are reported`() {
        val errors = validatePodcastForm(validPodcast.copy(title = "  ", category = ""), false)
        assertNotNull(errors.title)
        assertNotNull(errors.category)
    }

    @Test
    fun `the All filter sentinel cannot be a category`() {
        val errors = validatePodcastForm(validPodcast.copy(category = ALL_CATEGORY), false)
        assertNotNull(errors.category)
    }

    @Test
    fun `title longer than the limit is rejected`() {
        val errors = validatePodcastForm(validPodcast.copy(title = "က".repeat(TITLE_MAX_LENGTH + 1)), false)
        assertNotNull(errors.title)
    }

    @Test
    fun `podcast cover is required and must load`() {
        assertNotNull(validatePodcastForm(validPodcast.copy(cover = MediaField(SourceMode.DEVICE)), false).cover)
        assertNotNull(validatePodcastForm(validPodcast.copy(cover = MediaField(SourceMode.URL, "not a url")), false).cover)
        assertNotNull(validatePodcastForm(validPodcast, coverLoadFailed = true).cover)
        assertNull(
            validatePodcastForm(validPodcast.copy(cover = MediaField(SourceMode.DEVICE, localUri = "content://1")), false).cover
        )
    }

    // ─── Episode ───

    @Test
    fun `valid episode form with verified audio has no errors`() {
        assertFalse(validateEpisodeForm(validEpisode, audioVerified = true, coverLoadFailed = false).hasAny)
    }

    @Test
    fun `unverified or missing audio is an error`() {
        assertNotNull(validateEpisodeForm(validEpisode, audioVerified = false, coverLoadFailed = false).audio)
        assertNotNull(
            validateEpisodeForm(validEpisode.copy(audio = MediaField(SourceMode.DEVICE)), true, false).audio
        )
    }

    @Test
    fun `bad duration text is an error`() {
        val errors = validateEpisodeForm(validEpisode.copy(durationText = "24:99"), true, false)
        assertEquals(DURATION_FORMAT_ERROR, errors.duration)
    }

    @Test
    fun `episode cover is optional`() {
        assertNull(validateEpisodeForm(validEpisode.copy(cover = MediaField(SourceMode.NONE)), true, false).cover)
    }

    // ─── MediaField ───

    @Test
    fun `media field converts to the right input`() {
        assertEquals(MediaInput.Remote("https://a.com/e.mp3"), MediaField(SourceMode.URL, " https://a.com/e.mp3 ").toInput())
        assertEquals(MediaInput.Local("content://1"), MediaField(SourceMode.DEVICE, localUri = "content://1").toInput())
        assertEquals(MediaInput.None, MediaField(SourceMode.DEVICE).toInput())
        assertEquals(MediaInput.None, MediaField(SourceMode.NONE, urlText = "https://x.com").toInput())
    }

    @Test
    fun `saved url restores URL mode, blank restores the empty mode`() {
        assertEquals(SourceMode.URL, MediaField.fromSavedUrl("https://a.com/x.jpg", SourceMode.NONE).mode)
        assertEquals(SourceMode.NONE, MediaField.fromSavedUrl("", SourceMode.NONE).mode)
    }

    // ─── Display ───

    @Test
    fun `audio summary hides unknown parts`() {
        assertEquals("MP3 · ၂၄ မိနစ် · ~၁၂ MB", audioSummary(AudioInfo(1470, 11_760_000L, true)))
        assertEquals("MP3 · ၂၄ မိနစ်", audioSummary(AudioInfo(1470, 0L, true)))
    }

    @Test
    fun `summary counts podcasts and episodes in Myanmar digits`() {
        val podcasts = listOf(Podcast(id = "1", episodeCount = 12), Podcast(id = "2", episodeCount = 3))
        assertEquals("Podcast ၂ ခု · အပိုင်း ၁၅ ခု", adminSummary(podcasts))
    }

    @Test
    fun `recently updated podcasts come first, console ones last`() {
        val old = Podcast(id = "old", title = "B", updatedAt = Timestamp(Date(1_000)))
        val new = Podcast(id = "new", title = "C", updatedAt = Timestamp(Date(9_000)))
        val console = Podcast(id = "console", title = "A")
        assertEquals(listOf("new", "old", "console"), sortPodcastsForAdmin(listOf(console, old, new)).map { it.id })
    }

    @Test
    fun `category suggestions exclude the All sentinel`() {
        val podcasts = listOf(Podcast(category = "သတင်း"), Podcast(category = "နည်းပညာ"), Podcast(category = "သတင်း"))
        val suggestions = categorySuggestions(podcasts)
        assertTrue(ALL_CATEGORY !in suggestions)
        assertEquals(2, suggestions.size)
    }
}
