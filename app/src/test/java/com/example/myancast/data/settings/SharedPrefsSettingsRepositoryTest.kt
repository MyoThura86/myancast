package com.example.myancast.data.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.myancast.domain.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** ★ Phase 6 (#1) — app ပိတ်ပြီး ပြန်ဖွင့် (instance အသစ်) လည်း setting မပျောက်ရ */
@RunWith(RobolectricTestRunner::class)
class SharedPrefsSettingsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `fresh install uses the defaults`() {
        assertEquals(AppSettings(), SharedPrefsSettingsRepository(context).settings.value)
    }

    @Test
    fun `changes survive a new instance (app restart)`() {
        SharedPrefsSettingsRepository(context).update {
            it.copy(darkMode = false, dataSaver = true, wifiOnlyDownload = false)
        }

        val reopened = SharedPrefsSettingsRepository(context).settings.value

        assertFalse(reopened.darkMode)
        assertTrue(reopened.dataSaver)
        assertFalse(reopened.wifiOnlyDownload)
    }

    @Test
    fun `update emits immediately to collectors`() {
        val repo = SharedPrefsSettingsRepository(context)
        repo.update { it.copy(darkMode = false) }
        assertFalse(repo.settings.value.darkMode)
    }
}
