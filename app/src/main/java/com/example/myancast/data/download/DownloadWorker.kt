package com.example.myancast.data.download

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myancast.MyanCastApp
import com.example.myancast.data.local.DownloadEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * ★ Phase 6 (#4) — MP3 တစ်ခု ဒေါင်းတဲ့ worker။ **လိုင်းပြတ်ရင် ရပ်ခဲ့တဲ့ နေရာက ဆက်ဒေါင်း (resume)** —
 * မြန်မာ့ လိုင်းအတွက် အရေးကြီးဆုံး အပိုင်း။
 *
 * ① `filesDir/episodes/<id>.mp3.part` ရှိရင် `Range: bytes=<size>-` နဲ့ ကျန်တာပဲ တောင်း
 * ② 206 → append · 200 → အစက ပြန်ရေး · 416 → ဒေါင်းပြီးသား
 * ③ 1% တိုးမှ progress update
 * ④ IOException → `Result.retry()` (WorkManager backoff) · ၅ ခါ ကျော်ရင် Failed
 * ⑤ Wi-Fi ပြတ်လို့ WorkManager က ရပ်ရင် → Queued ပြန်ထား (Wi-Fi ရရင် ဆက်)
 * ⑥ ပြီးရင် `.part` → `.mp3` rename
 */
class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val episodeId = inputData.getString(KEY_EPISODE_ID) ?: return Result.failure()
        val dao = (applicationContext as MyanCastApp).database.downloadDao()
        // User က ဖျက်လိုက်ပြီ — ဘာမှ မလုပ်
        val entity = dao.get(episodeId) ?: return Result.failure()

        val dir = downloadDir(applicationContext).apply { mkdirs() }
        val part = File(dir, "$episodeId.mp3.part")
        val target = File(dir, "$episodeId.mp3")
        var lastPercent = entity.progress

        return try {
            dao.updateProgress(episodeId, DownloadEntity.STATUS_RUNNING, lastPercent, now())
            downloadTo(entity.audioUrl, part) { percent ->
                if (percent > lastPercent) {
                    lastPercent = percent
                    setProgress(workDataOf(KEY_PROGRESS to percent))
                    dao.updateProgress(episodeId, DownloadEntity.STATUS_RUNNING, percent, now())
                }
            }
            if (target.exists()) target.delete()
            if (!part.renameTo(target)) throw IOException("rename failed")
            dao.markDone(episodeId, target.absolutePath, target.length(), now())
            Result.success()
        } catch (e: CancellationException) {
            // Constraint (Wi-Fi) ပျောက် / user ရပ် — .part ကို ထားခဲ့ (ပြန်စရင် ဆက်ဒေါင်း)
            withContext(NonCancellable) {
                if (dao.get(episodeId) != null) {
                    dao.updateProgress(episodeId, DownloadEntity.STATUS_QUEUED, lastPercent, now())
                }
            }
            throw e
        } catch (e: IOException) {
            Log.w(TAG, "download $episodeId failed (attempt ${runAttemptCount + 1}): ${e.message}")
            if (runAttemptCount + 1 >= MAX_ATTEMPTS) {
                dao.updateProgress(episodeId, DownloadEntity.STATUS_FAILED, lastPercent, now())
                Result.failure()
            } else {
                dao.updateProgress(episodeId, DownloadEntity.STATUS_QUEUED, lastPercent, now())
                Result.retry()
            }
        } catch (e: Exception) {
            // Retry လုပ်လည်း မထူးတဲ့ error (လင့်ခ် ပုံစံ မှား စသည်) — "running" မှာ မကျန်ခဲ့အောင်
            Log.e(TAG, "download $episodeId failed permanently: ${e.message}", e)
            dao.updateProgress(episodeId, DownloadEntity.STATUS_FAILED, lastPercent, now())
            Result.failure()
        }
    }

    /** @param onProgress 0..100 (Content-Length မသိရင် မခေါ်) */
    private suspend fun downloadTo(url: String, part: File, onProgress: suspend (Int) -> Unit) =
        withContext(Dispatchers.IO) {
            val existing = if (part.exists()) part.length() else 0L
            val connection = open(url, existing)
            try {
                val mode = resumeModeFor(connection.responseCode, existing)
                if (mode == ResumeMode.ALREADY_COMPLETE) return@withContext
                if (mode == ResumeMode.ERROR) throw IOException("HTTP ${connection.responseCode}")

                val append = mode == ResumeMode.APPEND
                val startBytes = if (append) existing else 0L
                val length = connection.contentLengthLong
                val total = if (length > 0) startBytes + length else -1L

                FileOutputStream(part, append).use { out ->
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var written = startBytes
                        while (true) {
                            ensureActive()                 // WorkManager ရပ်ရင် ချက်ချင်း ထွက်
                            val read = input.read(buffer)
                            if (read == -1) break
                            out.write(buffer, 0, read)
                            written += read
                            if (total > 0) onProgress(downloadPercent(written, total))
                        }
                    }
                }
                if (total > 0 && part.length() < total) throw IOException("incomplete: ${part.length()}/$total")
            } finally {
                connection.disconnect()
            }
        }

    /** Redirect ကို ကိုယ်တိုင် လိုက် — HttpURLConnection က http ↔ https redirect ကို မလိုက်ဘူး */
    private fun open(url: String, existingBytes: Long): HttpURLConnection {
        var current = URL(url)
        repeat(MAX_REDIRECTS) {
            val connection = current.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            if (existingBytes > 0) connection.setRequestProperty("Range", "bytes=$existingBytes-")
            val code = connection.responseCode
            if (code in REDIRECT_CODES) {
                val location = connection.getHeaderField("Location") ?: throw IOException("redirect without Location")
                connection.disconnect()
                current = URL(current, location)
            } else {
                return connection
            }
        }
        throw IOException("too many redirects")
    }

    private fun now() = System.currentTimeMillis()

    companion object {
        const val KEY_EPISODE_ID = "episodeId"
        const val KEY_PROGRESS = "progress"
        const val MAX_ATTEMPTS = 5

        private const val TAG = "DownloadWorker"
        private const val BUFFER_SIZE = 64 * 1024
        private const val TIMEOUT_MS = 30_000
        private const val MAX_REDIRECTS = 5
        private val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)

        fun downloadDir(context: Context): File = File(context.filesDir, "episodes")
    }
}
