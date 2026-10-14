package com.example.myancast.data.admin

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.model.MediaInput
import com.example.myancast.domain.util.isMp3
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class AndroidAudioInspector(context: Context) : AudioInspector {

    private val appContext = context.applicationContext

    override suspend fun inspect(input: MediaInput): AudioInfo? = withContext(Dispatchers.IO) {
        try {
            when (input) {
                is MediaInput.Local -> inspectLocal(Uri.parse(input.uri))
                is MediaInput.Remote -> inspectRemote(input.url.trim())
                MediaInput.None -> null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "inspect failed: ${e.message}")
            null
        }
    }

    private fun inspectLocal(uri: Uri): AudioInfo? {
        val resolver = appContext.contentResolver
        var name = ""
        var size = 0L
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) name = cursor.getString(nameIndex).orEmpty()
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
                }
            }

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)
            val durationMs = retriever.durationMs() ?: return null
            val mime = resolver.getType(uri) ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            AudioInfo(
                durationSeconds = msToSeconds(durationMs),
                sizeBytes = size,
                isMp3 = isMp3(mime, name),
                displayName = name
            )
        } finally {
            retriever.release()
        }
    }

    private fun inspectRemote(url: String): AudioInfo? {
        val head = headRequest(url)
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(url, HashMap())
            val durationMs = retriever.durationMs() ?: return null
            val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                ?: head?.contentType
            AudioInfo(
                durationSeconds = msToSeconds(durationMs),
                sizeBytes = head?.contentLength ?: 0L,
                isMp3 = isMp3(mime, url)
            )
        } finally {
            retriever.release()
        }
    }

    private data class HeadResult(val contentType: String?, val contentLength: Long)

    /** File size အတွက် — server က HEAD မထောက်ပံ့ရင် null (size က "မသိ" ဖြစ်ရုံ) */
    private fun headRequest(url: String): HeadResult? {
        val connection = (URL(url).openConnection() as? HttpURLConnection) ?: return null
        return try {
            connection.requestMethod = "HEAD"
            connection.instanceFollowRedirects = true
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            if (connection.responseCode !in 200..299) return null
            HeadResult(
                contentType = connection.contentType,
                contentLength = connection.contentLengthLong.coerceAtLeast(0L)
            )
        } catch (e: Exception) {
            Log.w(TAG, "HEAD $url failed: ${e.message}")
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun MediaMetadataRetriever.durationMs(): Long? =
        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.takeIf { it > 0 }

    /** 1,470,400ms → 1470s (စက္ကန့် အပြည့် — "24:30") */
    private fun msToSeconds(ms: Long): Int = ((ms + 500) / 1000).toInt()

    private companion object {
        const val TAG = "AudioInspector"
        const val TIMEOUT_MS = 15_000
    }
}
