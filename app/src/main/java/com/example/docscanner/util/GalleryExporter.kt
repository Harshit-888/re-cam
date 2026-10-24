package com.example.docscanner.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Copies a captured page into the device gallery (`Pictures/DocScanner`).
 *
 * Backs the *Auto-save to Gallery* setting. Two code paths are needed:
 *
 * * **Android 10+** — scoped storage. The image is inserted through
 *   `MediaStore` with `IS_PENDING` set while the bytes are written, so a gallery
 *   app can never observe a half-written file. No permission required.
 * * **Android 8–9** — `MediaStore` does not take an arbitrary path, so the file
 *   is written to the public Pictures directory and announced with
 *   `MediaScannerConnection`. This does need `WRITE_EXTERNAL_STORAGE`, which is
 *   why the settings row asks for it at that point rather than at launch.
 *
 * Every failure returns `false`; saving a copy must never be able to crash a
 * capture that otherwise succeeded.
 */
object GalleryExporter {

    private const val ALBUM = "DocScanner"

    /** `DocScanner_20261025_124500_ab12.jpg` — sortable and collision-proof. */
    fun pageFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val suffix = (0..0xFFFF).random().toString(16).padStart(4, '0')
        return "DocScanner_${stamp}_$suffix.jpg"
    }

    fun saveToGallery(context: Context, source: File, displayName: String): Boolean = try {
        if (!source.exists() || source.length() == 0L) {
            false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveScoped(context, source, displayName)
        } else {
            saveLegacy(context, source, displayName)
        }
    } catch (_: Throwable) {
        false
    }

    private fun saveScoped(context: Context, source: File, displayName: String): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$ALBUM"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false

        val copied = runCatching {
            resolver.openOutputStream(uri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
                output.flush()
            } != null
        }.getOrDefault(false)

        if (!copied) {
            runCatching { resolver.delete(uri, null, null) }
            return false
        }

        val publish = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
        resolver.update(uri, publish, null, null)
        return true
    }

    private fun saveLegacy(context: Context, source: File, displayName: String): Boolean {
        @Suppress("DEPRECATION")
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val dir = File(pictures, ALBUM)
        if (!dir.exists() && !dir.mkdirs()) return false

        val target = File(dir, displayName)
        source.inputStream().use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(target.absolutePath),
            arrayOf("image/jpeg"),
            null
        )
        return true
    }
}
