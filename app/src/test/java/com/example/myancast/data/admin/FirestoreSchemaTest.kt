package com.example.myancast.data.admin

import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier
import java.util.Date

/**
 * Admin ရေးတဲ့ field နာမည်တွေ app က ဖတ်တဲ့ model property နဲ့ တစ်လုံးမှ မလွဲရ —
 * လွဲရင် Firestore ထဲ ရောက်ပေမဲ့ အသုံးပြုသူ ဖုန်းမှာ မပေါ်ဘူး။
 */
class FirestoreSchemaTest {

    /** Kotlin property backing field များ (`$stable` လို static field ဖယ်) */
    private fun modelFields(clazz: Class<*>): Set<String> =
        clazz.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }.map { it.name }.toSet()

    @Test
    fun `episode fields match the Episode model exactly (except document id)`() {
        val fields = Episode(publishedAt = Timestamp(Date(0))).toFirestoreFields().keys
        assertEquals(modelFields(Episode::class.java) - "id", fields)
    }

    @Test
    fun `podcast fields plus counters match the Podcast model exactly`() {
        val fields = Podcast().toEditableFields().keys +
            FirestoreSchema.PodcastField.EPISODE_COUNT + FirestoreSchema.PodcastField.UPDATED_AT
        assertEquals(modelFields(Podcast::class.java) - "id", fields)
    }

    @Test
    fun `episode values keep Firestore types and are trimmed`() {
        val published = Timestamp(Date(1_760_000_000_000L))
        val map = Episode(
            id = "e1",
            podcastId = "p1",
            title = "  အပိုင်း ၁  ",
            audioUrl = " https://a.com/x.mp3 ",
            duration = 1470,
            publishedAt = published,
            fileSizeBytes = 11_760_000L
        ).toFirestoreFields()

        assertEquals("အပိုင်း ၁", map[FirestoreSchema.EpisodeField.TITLE])
        assertEquals("https://a.com/x.mp3", map[FirestoreSchema.EpisodeField.AUDIO_URL])
        assertTrue(map[FirestoreSchema.EpisodeField.DURATION] is Int)             // integer (စက္ကန့်)
        assertTrue(map[FirestoreSchema.EpisodeField.FILE_SIZE_BYTES] is Long)     // integer
        assertEquals(published, map[FirestoreSchema.EpisodeField.PUBLISHED_AT])  // timestamp
        assertEquals("", map[FirestoreSchema.EpisodeField.COVER_URL])            // "" = podcast ပုံ
    }

    @Test
    fun `admin username is case and space insensitive`() {
        assertTrue(AdminConfig.isAdminUsername("admin"))
        assertTrue(AdminConfig.isAdminUsername(" Admin "))
        assertTrue(AdminConfig.isAdminUsername("admin@myancast.app"))     // email အပြည့်လည်း ရ
        assertTrue(AdminConfig.isAdminUsername(" Admin@MyanCast.app "))
        assertTrue(!AdminConfig.isAdminUsername("administrator"))
        assertTrue(!AdminConfig.isAdminUsername(""))
    }

    @Test
    fun `storage paths are per owner and timestamped`() {
        assertEquals("podcasts/p1/cover_42.jpg", mediaStoragePath(STORAGE_PODCASTS, "p1", MediaKind.IMAGE, 42))
        assertEquals("episodes/e1/audio_42.mp3", mediaStoragePath(STORAGE_EPISODES, "e1", MediaKind.AUDIO, 42))
    }
}
