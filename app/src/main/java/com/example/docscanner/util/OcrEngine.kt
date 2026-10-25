package com.example.docscanner.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

/**
 * On-device text recognition, so that a scan's words become searchable.
 *
 * Scans are images: a PDF this app exports has no text layer, and there is
 * nothing in the page record but pixels. Recognition is what turns a page into
 * text, and it therefore runs once per page, in the background, right after the
 * page is saved — see `ScannerRepository.indexTextInBackground`.
 *
 * Everything here is best-effort by design. If recognition is unavailable, throws,
 * or returns nothing, the page simply has no text: search skips it, the rest of
 * the app is unaffected, and the capture is never at risk. Nothing waits on OCR.
 */
internal object OcrEngine {

    private const val TAG = "OcrEngine"

    /**
     * Longest edge handed to the recogniser.
     *
     * A 3200 px page at full size costs ~40 MB to decode and buys no accuracy: the
     * recogniser works on a normalised input and A4 body text is already several
     * pixels tall at 2400 px. Pages are still stored lossless — this is only the
     * copy the recogniser sees.
     */
    private const val MAX_OCR_DIMENSION = 2400

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Recognises the text of one page.
     *
     * @return the recognised text with surrounding blank lines trimmed, or `null`
     *   when the page has no text, the image cannot be decoded, or recognition
     *   fails. Never throws.
     */
    suspend fun readPage(imagePath: String): String? = withContext(Dispatchers.Default) {
        val bitmap = decode(imagePath) ?: return@withContext null
        try {
            val text = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text
            text.trim().takeIf { it.isNotEmpty() }
        } catch (error: Throwable) {
            Log.i(TAG, "No text for ${imagePath.substringAfterLast('/')}: ${error.javaClass.simpleName}")
            null
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    /** Decodes the page at a sane size for recognition, or `null` if it is unreadable. */
    private fun decode(path: String): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        var sample = 1
        val longest = max(bounds.outWidth, bounds.outHeight)
        while (longest / sample > MAX_OCR_DIMENSION) sample *= 2
        BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        )
    }.getOrNull()

    /** Bridges a Play Services [Task] into coroutine suspension without another dependency. */
    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }
}
