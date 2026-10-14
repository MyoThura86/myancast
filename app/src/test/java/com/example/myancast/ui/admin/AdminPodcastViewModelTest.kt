package com.example.myancast.ui.admin

import com.example.myancast.FakeAdminContentRepository
import com.example.myancast.FakeMediaUploader
import com.example.myancast.FakeNetworkMonitor
import com.example.myancast.FakePodcastRepository
import com.example.myancast.data.admin.AdminException
import com.example.myancast.data.admin.MediaKind
import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast
import com.example.myancast.ui.navigation.Screen
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminPodcastViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val existing = Podcast(
        id = "p1",
        title = "နည်းပညာ",
        description = "desc",
        coverUrl = "https://firebasestorage.googleapis.com/v0/b/t/o/podcasts%2Fp1%2Fcover_1.jpg",
        category = "နည်းပညာ",
        episodeCount = 2
    )
    private val episodes = listOf(
        Episode(id = "e1", podcastId = "p1", title = "၁", audioUrl = "https://a.com/1.mp3"),
        Episode(id = "e2", podcastId = "p1", title = "၂", audioUrl = "https://a.com/2.mp3")
    )

    private lateinit var content: FakeAdminContentRepository
    private lateinit var uploader: FakeMediaUploader
    private lateinit var network: FakeNetworkMonitor

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        content = FakeAdminContentRepository()
        uploader = FakeMediaUploader()
        network = FakeNetworkMonitor()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(id: String) = AdminPodcastViewModel(
        podcastIdArg = id,
        repo = FakePodcastRepository(listOf(existing), episodes),
        content = content,
        uploader = uploader,
        network = network,
        now = { 42L }
    )

    @Test
    fun `existing podcast loads into the form with its episodes`() = runTest {
        val vm = vm("p1")
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isNew)
        assertEquals("နည်းပညာ", state.form.title)
        assertEquals(SourceMode.URL, state.form.cover.mode)
        assertEquals(2, state.episodes.size)
        assertFalse(state.isDirty)
        assertEquals(listOf("နည်းပညာ"), state.categorySuggestions)
    }

    @Test
    fun `new podcast with device cover uploads then saves with episodeCount 0`() = runTest {
        val vm = vm(Screen.NEW_ID)
        advanceUntilIdle()

        vm.onTitleChange("  တရားတော်  ")
        vm.onCategoryChange("တရားတော်")
        vm.onCoverPicked("content://media/1")
        vm.save()
        advanceUntilIdle()

        val (saved, isNew) = content.savedPodcasts.single()
        assertTrue(isNew)
        assertEquals("တရားတော်", saved.title)                       // trim
        assertEquals(0, saved.episodeCount)
        assertEquals(MediaKind.IMAGE, uploader.uploads.single().second)
        assertEquals("podcasts/${saved.id}/cover_42.jpg", uploader.uploads.single().third)
        assertTrue(saved.coverUrl.startsWith("https://firebasestorage"))
        // သိမ်းပြီးရင် edit mode ကို ပြောင်း၊ form "သန့်"
        assertFalse(vm.state.value.isNew)
        assertFalse(vm.state.value.isDirty)
        assertEquals(SaveStatus.Idle, vm.state.value.saveStatus)
    }

    @Test
    fun `invalid form is not saved`() = runTest {
        val vm = vm(Screen.NEW_ID)
        advanceUntilIdle()

        vm.save()
        advanceUntilIdle()

        assertTrue(content.savedPodcasts.isEmpty())
        assertNotNull(vm.state.value.errors.title)
        assertNotNull(vm.state.value.errors.cover)
        assertEquals(FIX_ERRORS_MESSAGE, vm.state.value.message)
    }

    @Test
    fun `offline save is blocked before uploading`() = runTest {
        network.set(online = false)
        val vm = vm("p1")
        advanceUntilIdle()

        vm.onTitleChange("အသစ်")
        vm.save()
        advanceUntilIdle()

        assertTrue(content.savedPodcasts.isEmpty())
        assertEquals(OFFLINE_SAVE_MESSAGE, vm.state.value.message)
    }

    @Test
    fun `replacing the cover deletes the old uploaded cover`() = runTest {
        val vm = vm("p1")
        advanceUntilIdle()

        vm.onCoverModeChange(SourceMode.URL)
        vm.onCoverUrlChange("https://example.com/new.jpg")
        vm.onCoverUrlLoadResult(true)
        vm.save()
        advanceUntilIdle()

        assertEquals(false, content.savedPodcasts.single().second)
        assertEquals(2, content.savedPodcasts.single().first.episodeCount)   // edit — count မထိ
        assertEquals(listOf(existing.coverUrl), uploader.deleted)
    }

    @Test
    fun `broken image url blocks save`() = runTest {
        val vm = vm("p1")
        advanceUntilIdle()

        vm.onCoverUrlChange("https://example.com/broken.jpg")
        vm.onCoverUrlLoadResult(false)
        vm.save()
        advanceUntilIdle()

        assertTrue(content.savedPodcasts.isEmpty())
        assertNotNull(vm.state.value.errors.cover)
    }

    @Test
    fun `failed firestore write removes the just uploaded file`() = runTest {
        content.failWith = AdminException("ခွင့်ပြုချက် မရှိပါ")
        val vm = vm("p1")
        advanceUntilIdle()

        vm.onCoverPicked("content://media/9")
        vm.save()
        advanceUntilIdle()

        assertEquals("ခွင့်ပြုချက် မရှိပါ", vm.state.value.message)
        assertEquals(1, uploader.deleted.size)
        assertTrue(uploader.deleted.single().contains("cover_42"))
        assertEquals(SaveStatus.Idle, vm.state.value.saveStatus)
    }

    @Test
    fun `delete removes podcast and its owned media`() = runTest {
        val vm = vm("p1")
        advanceUntilIdle()

        vm.delete()
        advanceUntilIdle()

        assertEquals(listOf("p1"), content.deletedPodcastIds)
        assertTrue(vm.state.value.deleted)
        assertTrue(existing.coverUrl in uploader.deleted)
        assertTrue("https://a.com/1.mp3" in uploader.deleted)   // uploader က ကိုယ်ပိုင်လား ဆုံးဖြတ်
    }

    @Test
    fun `missing podcast shows a load error`() = runTest {
        val vm = vm("missing")
        advanceUntilIdle()

        assertNotNull(vm.state.value.loadError)
        assertFalse(vm.state.value.isLoading)
    }
}
