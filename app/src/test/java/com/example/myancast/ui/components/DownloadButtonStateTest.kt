package com.example.myancast.ui.components

import com.example.myancast.data.network.NetworkStatus
import com.example.myancast.domain.model.DownloadStatus
import com.example.myancast.domain.util.shouldConfirmMobileStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadButtonStateTest {

    private val wifi = NetworkStatus(online = true, metered = false)
    private val mobile = NetworkStatus(online = true, metered = true)
    private val offline = NetworkStatus(online = false, metered = false)

    @Test
    fun `no status means not downloaded`() {
        assertEquals(DownloadButtonState.NotDownloaded, downloadButtonState(null, wifi))
    }

    @Test
    fun `wifi only queue waits on mobile data or offline`() {
        val queued = DownloadStatus.Queued(wifiOnly = true)
        assertEquals(DownloadButtonState.WaitingForWifi, downloadButtonState(queued, mobile))
        assertEquals(DownloadButtonState.WaitingForWifi, downloadButtonState(queued, offline))
        assertEquals(DownloadButtonState.Queued, downloadButtonState(queued, wifi))
        assertEquals(DownloadButtonState.Queued, downloadButtonState(DownloadStatus.Queued(false), mobile))
    }

    @Test
    fun `running done and failed map directly`() {
        assertEquals(DownloadButtonState.Running(42), downloadButtonState(DownloadStatus.Running(42), wifi))
        assertEquals(DownloadButtonState.Done, downloadButtonState(DownloadStatus.Done("/f", 1), offline))
        assertEquals(DownloadButtonState.Failed, downloadButtonState(DownloadStatus.Failed, wifi))
    }

    @Test
    fun `descriptions are Myanmar with Myanmar digits`() {
        assertEquals("ဒေါင်းနေပါတယ် ၄၂% — နှိပ်ရင် ရပ်မယ်", DownloadButtonState.Running(42).description())
    }

    @Test
    fun `mobile stream confirm only when data saver, metered and not downloaded`() {
        assertTrue(shouldConfirmMobileStream(dataSaver = true, metered = true, downloaded = false))
        assertFalse(shouldConfirmMobileStream(dataSaver = true, metered = true, downloaded = true))
        assertFalse(shouldConfirmMobileStream(dataSaver = true, metered = false, downloaded = false))
        assertFalse(shouldConfirmMobileStream(dataSaver = false, metered = true, downloaded = false))
    }
}
