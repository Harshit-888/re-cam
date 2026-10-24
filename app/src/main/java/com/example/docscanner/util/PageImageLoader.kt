package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decodes page JPEGs off the main thread and keeps a bounded bitmap cache.
 *
 * Cache keys include the file's size and mtime, so re-saving a page (which
 * rewrites `_edited.jpg`) automatically invalidates the stale entry without any
 * manual eviction bookkeeping.
 *
 * Bitmaps returned here are **shared** and must not be recycled or mutated by
 * callers. [ImageProcessingUtils.applyEnhancements] never recycles its source, so
 * passing a cached bitmap into it is safe.
 */
object PageImageLoader {

    private val maxCacheKilobytes: Int =
        (Runtime.getRuntime().maxMemory() / 1024L / 8L).toInt().coerceAtLeast(8 * 1024)

    private val cache = object : LruCache<String, Bitmap>(maxCacheKilobytes) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    /** Decodes at a sampling rate appropriate for [reqWidth] x [reqHeight]. */
    suspend fun load(
        context: Context,
        imageUri: String?,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (imageUri.isNullOrBlank() || reqWidth <= 0 || reqHeight <= 0) return@withContext null

        val key = cacheKey(context, imageUri, reqWidth, reqHeight)
        cache.get(key)?.let { return@withContext it }

        val bitmap = ImageStore.loadBitmap(context, imageUri, reqWidth, reqHeight)
            ?: return@withContext null
        cache.put(key, bitmap)
        bitmap
    }

    /** Full-resolution decode for editing/export, bypassing the cache. */
    suspend fun loadForEdit(context: Context, imageUri: String?, maxDimension: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(context, imageUri, maxDimension)
        }

    private fun cacheKey(context: Context, imageUri: String, reqWidth: Int, reqHeight: Int): String {
        val file = ImageStore.resolveFile(context, imageUri)
        val stamp = if (file == null) "missing" else "${file.length()}:${file.lastModified()}"
        return "$imageUri@${reqWidth}x$reqHeight#$stamp"
    }

    fun clear() = cache.evictAll()
}
