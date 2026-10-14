package com.example.myancast.data.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

class FirebaseStorageUploader(
    context: Context,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : MediaUploader {

    private val appContext = context.applicationContext

    init {
        // လိုင်းပြတ်ရင် SDK က ၁၀ မိနစ်အထိ စောင့်တယ် — admin ကို ကြာကြာ မစောင့်ခိုင်းဘဲ error ပြ
        storage.maxUploadRetryTimeMillis = UPLOAD_RETRY_MS
    }

    override fun upload(localUri: String, kind: MediaKind, storagePath: String): Flow<UploadEvent> =
        callbackFlow {
            val ref = storage.reference.child(storagePath)
            val uri = Uri.parse(localUri)

            val task: UploadTask = when (kind) {
                MediaKind.IMAGE -> {
                    // Cover ကို 1080px JPEG ချုံ့ — အသုံးပြုသူ ဖုန်းရဲ့ data နဲ့ storage ကို ချွေတာ
                    val bytes = try {
                        withContext(Dispatchers.IO) { compressImage(uri) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "compress failed: ${e.message}", e)
                        throw AdminException("ပုံကို ဖွင့်လို့ မရပါ — တခြား ပုံ ရွေးကြည့်ပါ", e)
                    }
                    ref.putBytes(bytes, metadata("image/jpeg"))
                }
                MediaKind.AUDIO -> ref.putFile(uri, metadata("audio/mpeg"))
            }

            task.addOnProgressListener { snap ->
                if (snap.totalByteCount > 0) {
                    trySend(UploadEvent.Progress(snap.bytesTransferred.toFloat() / snap.totalByteCount))
                }
            }
            task.addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { url ->
                        trySend(UploadEvent.Done(url.toString()))
                        close()
                    }
                    .addOnFailureListener { close(it.toAdminException()) }
            }
            task.addOnFailureListener { close(it.toAdminException()) }

            awaitClose { if (task.isInProgress) task.cancel() }
        }

    override suspend fun deleteIfOwned(url: String) {
        if (!url.contains("firebasestorage.googleapis.com") && !url.startsWith("gs://")) return
        try {
            val ref = storage.getReferenceFromUrl(url)
            // podcasts/ နဲ့ episodes/ အောက်က (admin တင်ထားတဲ့) ဖိုင်ကိုပဲ ဖျက်
            if (ref.path.startsWith("/$STORAGE_PODCASTS/") || ref.path.startsWith("/$STORAGE_EPISODES/")) {
                ref.delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "deleteIfOwned failed (ignored): ${e.message}")
        }
    }

    // ──────── Image ────────

    private fun compressImage(uri: Uri): ByteArray {
        val decoded = decodeScaled(uri)
        // PNG ရဲ့ ကြည်လင်တဲ့ နောက်ခံ — JPEG မှာ အမည်း ဖြစ်မသွားအောင် အဖြူခံ
        val bitmap = if (decoded.hasAlpha()) decoded.onWhiteBackground() else decoded
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.toByteArray()
        }
    }

    private fun decodeScaled(uri: Uri): Bitmap {
        val resolver = appContext.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder — ကင်မရာ ပုံရဲ့ EXIF လှည့်ထားမှုကိုပါ အလိုအလျောက် ပြင်တယ်
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val longest = max(info.size.width, info.size.height)
                if (longest > MAX_IMAGE_PX) {
                    val scale = MAX_IMAGE_PX.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt().coerceAtLeast(1),
                        (info.size.height * scale).toInt().coerceAtLeast(1)
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_IMAGE_PX) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("decode failed")
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= MAX_IMAGE_PX) return bitmap
        val scale = MAX_IMAGE_PX.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun Bitmap.onWhiteBackground(): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(result).apply {
            drawColor(Color.WHITE)
            drawBitmap(this@onWhiteBackground, 0f, 0f, null)
        }
        return result
    }

    private fun metadata(contentType: String) = StorageMetadata.Builder()
        .setContentType(contentType)
        // ဖိုင်နာမည်မှာ အချိန် ပါလို့ အကြောင်းအရာ မပြောင်း — ကြာကြာ cache လုပ်လို့ရ
        .setCacheControl("public, max-age=31536000")
        .build()

    private companion object {
        const val TAG = "StorageUploader"
        const val MAX_IMAGE_PX = 1080
        const val JPEG_QUALITY = 85
        const val UPLOAD_RETRY_MS = 60_000L
    }
}

private fun Exception.toAdminException(): AdminException {
    if (this is AdminException) return this
    val message = when ((this as? StorageException)?.errorCode) {
        StorageException.ERROR_NOT_AUTHENTICATED,
        StorageException.ERROR_NOT_AUTHORIZED ->
            "ခွင့်ပြုချက် မရှိပါ — Admin အဖြစ် ပြန်ဝင်ပြီး Storage rules ကို စစ်ပါ"
        StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
            "အင်တာနက် ချိတ်ဆက်မှု မရပါ — ပြန်ကြိုးစားကြည့်ပါ"
        StorageException.ERROR_QUOTA_EXCEEDED ->
            "Storage နေရာ ပြည့်နေပါပြီ — Firebase Console ကို စစ်ပါ"
        StorageException.ERROR_BUCKET_NOT_FOUND,
        StorageException.ERROR_PROJECT_NOT_FOUND ->
            "Firebase Storage ကို မဖွင့်ရသေးပါ — Console မှာ Storage ကို ဖွင့်ပါ"
        StorageException.ERROR_CANCELED -> "ဖိုင် တင်တာကို ရပ်လိုက်ပါပြီ"
        else -> "ဖိုင် တင်လို့ မရပါ — ပြန်ကြိုးစားကြည့်ပါ"
    }
    Log.e("StorageUploader", "upload failed: ${this.message}", this)
    return AdminException(message, this)
}
