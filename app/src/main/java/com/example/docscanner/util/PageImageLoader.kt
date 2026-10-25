package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decodes page images off the main thread with two caches in front of them.
 *
 * # Why there is a disk cache
 *
 * A grid thumbnail is ~360 px wide, but the file behind it is a full page: up to
 * 3200 px, and for *Clear B&W* and *Grayscale* a **PNG**, because those pages are
 * stored losslessly. JPEG has DCT scaling, so a sampled decode is cheap; PNG does
 * not, so every sampled decode still inflates the whole image. Scrolling a library
 * of B&W scans therefore paid a full PNG decode per card, on every pass — by far
 * the most expensive thing the home screen did.
 *
 * Small thumbnails are now written to `cacheDir/thumbs` the first time they are
 * needed and decoded from there afterwards, which is a ~10 KB JPEG instead of a
 * multi-megabyte PNG. Large requests (the editor, the viewer) bypass the disk
 * cache: at that size the cache would just be a second copy of the page.
 *
 * The in-memory cache keys include the source file's size and mtime, so re-saving
 * a page (which rewrites `_edited.jpg`) invalidates both caches without any manual
 * eviction bookkeeping — and because the disk file name is derived from that same
 * key, a stale thumbnail can never be shown for a re-baked page.
 *
 * Bitmaps returned here are **shared** and must not be recycled or mutated by
 * callers. [ImageProcessingUtils.applyEnhancements] never recycles its source, so
 * passing a cached bitmap into it is safe.
 */
object PageImageLoader {

    /** Requests at or below this longest edge are cached on disk as well. */
    private const val DISK_CACHE_MAX_REQUEST = 1024

    private const val THUMB_QUALITY = 88

    /** Upper bound on cached thumbnail files before the oldest half is swept. */
    private const val MAX_THUMB_FILES = 400

    private val maxCacheKilobytes: Int =
        (Runtime.getRuntime().maxMemory() / 1024L / 8L).toInt().coerceAtLeast(8 * 1024)

    private val cache = object : LruCache<String, Bitmap>(maxCacheKilobytes) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    /**
     * Decode budget: two at a time.
     *
     * A fling can put a dozen cards on screen at once. Letting all of them decode
     * in parallel floods the CPU the render thread needs, so the list stutters
     * exactly when it is moving fastest. Two decodes keep the queue moving without
     * starving the UI.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val decodeDispatcher = Dispatchers.IO.limitedParallelism(2)

    /** Decodes at a sampling rate appropriate for [reqWidth] x [reqHeight]. */
    suspend fun load(
        context: Context,
        imageUri: String?,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? = withContext(decodeDispatcher) {
        if (imageUri.isNullOrBlank() || reqWidth <= 0 || reqHeight <= 0) return@withContext null

        val key = cacheKey(context, imageUri, reqWidth, reqHeight)
        cache.get(key)?.let { return@withContext it }

        val thumbFile = thumbFile(context, key, reqWidth, reqHeight)
        if (thumbFile != null && thumbFile.exists()) {
            val cached = runCatching {
                BitmapFactory.decodeFile(thumbFile.absolutePath)
            }.getOrNull()
            if (cached != null) {
                cache.put(key, cached)
                return@withContext cached
            }
            // Corrupt or half-written file: drop it and fall through to a rebuild.
            thumbFile.delete()
        }

        val bitmap = ImageStore.loadBitmap(context, imageUri, reqWidth, reqHeight)
            ?: return@withContext null

        if (thumbFile != null) writeThumb(bitmap, thumbFile)
        cache.put(key, bitmap)
        bitmap
    }

    /** Full-resolution decode for editing/export, bypassing both caches. */
    suspend fun loadForEdit(context: Context, imageUri: String?, maxDimension: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(context, imageUri, maxDimension)
        }

    /**
     * Drops the in-memory cache immediately, and the disk thumbnails when a
     * context is supplied (they are cheap to rebuild, so callers that do not have
     * one can simply skip that half).
     */
    fun clear(context: Context? = null) {
        cache.evictAll()
        if (context != null) {
            runCatching { thumbnailDir(context).deleteRecursively() }
        }
    }

    // ---- disk thumbnails ---------------------------------------------------

    private fun thumbnailDir(context: Context): File =
        File(context.cacheDir, "thumbs").apply { if (!exists()) mkdirs() }

    /**
     * Cached thumbnail path for [key], or `null` when this request is too large to
     * be worth caching.
     */
    private fun thumbFile(context: Context, key: String, reqWidth: Int, reqHeight: Int): File? {
        if (maxOf(reqWidth, reqHeight) > DISK_CACHE_MAX_REQUEST) return null
        val name = sha1(key) + ".jpg"
        return File(thumbnailDir(context), name)
    }

    private fun writeThumb(bitmap: Bitmap, target: File) {
        runCatching {
            // Temp file + rename: two cards can request the same page at once, and
            // a reader must never see a half-written JPEG.
            val temp = File(target.parentFile, target.name + ".tmp")
            temp.outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, THUMB_QUALITY, stream)
            }
            if (!temp.renameTo(target)) temp.delete()
            sweepThumbsIfCrowded(target.parentFile)
        }
    }

    /**
     * Keeps the thumbnail cache bounded. Runs only when a *new* thumbnail is
     * written, so the file listing is not on the hot path.
     */
    private fun sweepThumbsIfCrowded(dir: File?) {
        if (dir == null) return
        val files = dir.listFiles() ?: return
        if (files.size <= MAX_THUMB_FILES) return
        files.sortedBy { it.lastModified() }
            .take(files.size - MAX_THUMB_FILES / 2)
            .forEach { it.delete() }
    }

    private fun sha1(value: String): String = runCatching {
        MessageDigest.getInstance("SHA-1")
            .digest(value.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }.getOrElse { value.hashCode().toUInt().toString(16) }

    private fun cacheKey(context: Context, imageUri: String, reqWidth: Int, reqHeight: Int): String {
        val file = ImageStore.resolveFile(context, imageUri)
        val stamp = if (file == null) "missing" else "${file.length()}:${file.lastModified()}"
        return "$imageUri@${reqWidth}x$reqHeight#$stamp"
    }
}
