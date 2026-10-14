package com.example.myancast.ui.admin

import com.example.myancast.FakeAdminContentRepository
import com.example.myancast.FakeAudioInspector
import com.example.myancast.FakeMediaUploader
import com.example.myancast.FakeNetworkMonitor
import com.example.myancast.FakePodcastRepository
import com.example.myancast.data.admin.MediaKind
import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.MediaInput
import com.example.myancast.domain.model.Podcast
import com.example.myancast.domain.util.hourAndMinute
import com.example.myancast.domain.util.toDatePickerMillis
import com.example.myancast.ui.navigation.Screen
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class AdminEpisodeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val podcast = Podcast(id = "p1", title = "နည်းပညာ", coverUrl = "https://a.com/p.jpg")
    private val existing = Episode(
        id = "e1",
        podcastId = "p1",
        title = "အပိုင်း ၁",
        audioUrl = "https://a.com/1.mp3",
        duration = 600,
        publishedAt = Timestamp(Date(1_700_000_000_000L)),
        fileSizeBytes = 4_800_000L
    )

    private lateinit var content: FakeAdminContentRepository
    private lateinit var uploader: FakeMediaUploader
    private lateinit var network: FakeNetworkMonitor

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        content = FakeAdminContentRepository(mutableMapOf("e1" to existing))
        uploader = FakeMediaUploader()
        network = FakeNetworkMonitor()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(
        episodeId: String,
        inspector: FakeAudioInspector = FakeAudioInspector()
    ) = AdminEpisodeViewModel(
        podcastId = "p1",
        episodeIdArg = episodeId,
        repo = FakePodcastRepository(listOf(podcast)),
        content = content,
        uploader = uploader,
        inspector = inspector,
        network = network,
        now = { 1_760_000_000_000L }
    )

    @Test
    fun `new episode starts with now as publish time and podcast info`() = runTest {
        val vm = vm(Screen.NEW_ID)
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.isNew)
        assertEquals("နည်းပညာ", state.podcastTitle)
        assertEquals(1_760_000_000_000L, state.form.publishedAtMillis)
        assertEquals(AudioCheck.Idle, state.audioCheck)
    }

    @Test
    fun `picking a device mp3 inspects it and auto fills the duration`() = runTest {
        val vm = vm(Screen.NEW_ID)
        advanceUntilIdle()

        vm.onAudioPicked("content://audio/1")
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.audioCheck is AudioCheck.Ok)
        assertEquals("24:30", state.form.durationText)
        assertEquals("ep.mp3", state.form.audio.localName)
    }

    @Test
    fun `a non mp3 file is rejected`() = runTest {
        val local = MediaInput.Local("content://audio/wav")
        val vm = vm(Screen.NEW_ID, FakeAudioInspector(mapOf(local to AudioInfo(100, 1L, isMp3 = false))))
        advanceUntilIdle()

        vm.onAudioPicked(local.uri)
        advanceUntilIdle()

        assertEquals(AudioCheck.Failed(NOT_MP3_ERROR), vm.state.value.audioCheck)
    }

    @Test
    fun `manual duration is not overwritten by a new mp3`() = runTest {
        val vm = vm(Screen.NEW_ID)
        advanceUntilIdle()

        vm.onDurationChange("10:00")
        vm.onAudioPicked("content://audio/1")
        advanceUntilIdle()

        assertEquals("10:00", vm.state.value.form.durationText)
    }

    @Test
    fun `saving a new episode uploads mp3 and writes exact fields`() = runTest {
        val vm = vm(Screen.NEW_ID)
        advanceUntilIdle()

        vm.onAudioPicked("content://audio/1")
        advanceUntilIdle()
        vm.onTitleChange("အပိုင်း ၂ — AI")
        vm.save()
        advanceUntilIdle()

        val (episode, isNew) = content.savedEpisodes.single()
        assertTrue(isNew)
        assertEquals("p1", episode.podcastId)
        assertEquals(1470, episode.duration)
        assertEquals(11_760_000L, episode.fileSizeBytes)
        assertEquals(1_760_000_000_000L, episode.publishedAt?.toDate()?.time)
        assertEquals("", episode.coverUrl)                                   // Podcast ပုံ သုံး
        assertEquals(MediaKind.AUDIO, uploader.uploads.single().second)
        assertEquals("episodes/${episode.id}/audio_1760000000000.mp3", uploader.uploads.single().third)
        assertEquals("အပိုင်း အသစ် ထည့်ပြီးပါပြီ", vm.state.value.finishedMessage)
    }

    @Test
    fun `audio url is inspected automatically on save`() = runTest {
        val inspector = FakeAudioInspector()
        val vm = vm(Screen.NEW_ID, inspector)
        advanceUntilIdle()

        vm.onAudioModeChange(SourceMode.URL)
        vm.onAudioUrlChange("https://cdn.example.com/ep.mp3")
        vm.onTitleChange("လင့်ခ် အပိုင်း")
        vm.save()
        advanceUntilIdle()

        assertEquals(listOf<MediaInput>(MediaInput.Remote("https://cdn.example.com/ep.mp3")), inspector.inspected)
        assertEquals("https://cdn.example.com/ep.mp3", content.savedEpisodes.single().first.audioUrl)
        assertTrue(uploader.uploads.isEmpty())                               // လင့်ခ် — တင်စရာ မလို
    }

    @Test
    fun `unreadable audio url blocks save`() = runTest {
        val url = MediaInput.Remote("https://cdn.example.com/broken.mp3")
        val vm = vm(Screen.NEW_ID, FakeAudioInspector(mapOf(url to null)))
        advanceUntilIdle()

        vm.onAudioModeChange(SourceMode.URL)
        vm.onAudioUrlChange(url.url)
        vm.onTitleChange("x")
        vm.save()
        advanceUntilIdle()

        assertTrue(content.savedEpisodes.isEmpty())
        assertEquals(AudioCheck.Failed(AUDIO_UNREADABLE_ERROR), vm.state.value.audioCheck)
    }

    @Test
    fun `editing keeps the saved audio without re-inspecting`() = runTest {
        val inspector = FakeAudioInspector()
        val vm = vm("e1", inspector)
        advanceUntilIdle()

        assertTrue(vm.state.value.audioCheck is AudioCheck.Ok)
        assertEquals("10:00", vm.state.value.form.durationText)

        vm.onTitleChange("အပိုင်း ၁ (ပြင်ပြီး)")
        vm.save()
        advanceUntilIdle()

        val (episode, isNew) = content.savedEpisodes.single()
        assertFalse(isNew)
        assertEquals(existing.audioUrl, episode.audioUrl)
        assertEquals(existing.fileSizeBytes, episode.fileSizeBytes)
        assertTrue(inspector.inspected.isEmpty())
        assertTrue(uploader.deleted.isEmpty())                               // ဖိုင် မပြောင်း — မဖျက်
    }

    @Test
    fun `changing the url after verification requires a new check`() = runTest {
        val vm = vm("e1")
        advanceUntilIdle()

        vm.onAudioUrlChange("https://a.com/other.mp3")

        assertEquals(AudioCheck.Idle, vm.state.value.audioCheck)
        // မူလ လင့်ခ်ကို ပြန်ထည့်ရင် စစ်ပြီးသား ရလဒ် ပြန်သုံး
        vm.onAudioUrlChange(existing.audioUrl)
        assertTrue(vm.state.value.audioCheck is AudioCheck.Ok)
    }

    @Test
    fun `replacing audio deletes the old file after saving`() = runTest {
        val vm = vm("e1")
        advanceUntilIdle()

        vm.onAudioPicked("content://audio/new")
        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()

        assertTrue(content.savedEpisodes.single().first.audioUrl.contains("audio_"))
        assertEquals(listOf(existing.audioUrl), uploader.deleted)
    }

    @Test
    fun `delete removes the episode and finishes`() = runTest {
        val vm = vm("e1")
        advanceUntilIdle()

        vm.delete()
        advanceUntilIdle()

        assertEquals(listOf(existing), content.deletedEpisodes)
        assertEquals("အပိုင်းကို ဖျက်ပြီးပါပြီ", vm.state.value.finishedMessage)
    }

    @Test
    fun `missing episode shows a load error`() = runTest {
        val vm = vm("gone")
        advanceUntilIdle()

        assertNotNull(vm.state.value.loadError)
    }

    @Test
    fun `publish time change keeps the date`() = runTest {
        val vm = vm(Screen.NEW_ID)
        advanceUntilIdle()
        val before = vm.state.value.form.publishedAtMillis

        vm.onPublishTimeChange(7, 5)

        val after = vm.state.value.form.publishedAtMillis
        assertEquals(7 to 5, hourAndMinute(after))
        assertEquals(
            toDatePickerMillis(before),
            toDatePickerMillis(after)
        )
        assertNull(vm.state.value.errors.duration)
    }
}
