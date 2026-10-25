package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * One small JPEG per page, written **when the page is written**.
 *
 * # Why this exists at all
 *
 * A scan is stored at up to 3200 px, and *Clear B&W* / *Grayscale* pages are
 * stored as **PNG** because those outputs are worth keeping losslessly. A grid
 * cover is ~470 px wide. Before this class, the cover was produced by decoding the
 * whole page in the UI: for a PNG there is no DCT-style shortcut, so every tile
 * inflated a ~7.7 megapixel image (tens of milliseconds, ~30 MB of transient
 * allocation, plus a GC that pauses the render thread) — and the window is what
 * the user is scrolling. That is the home screen's jank: not layout, not
 * recomposition, image decoding.
 *
 * The fix is not "decode faster". It is **not decoding a page on the scroll path
 * at all**: the pixels are already in memory when a page is saved
 * ([ImageStore.writeJpeg] / [ImageStore.writePng] are called with the decoded
 * bitmap in hand), so scaling one down there costs a few milliseconds at a moment
 * when nobody is waiting, and every later cover is a ~40 KB JPEG.
 *
 * # Why `filesDir` and not `cacheDir`
 *
 * These used to live in `cacheDir`, which the OS may clear at any time and which
 * every "free up space" tool targets. Losing them silently put the expensive path
 * back on the next launch — a perf fix that quietly expires. They are app data,
 * they are derived from files we own, and they are swept by [MAX_FILES].
 *
 * # Staleness
 *
 * The key is derived from the page path **and the page file's length and mtime**,
 * so a re-baked page (which [ImageStore] always rewrites with a fresh mtime) gets
 * a different key and a stale thumbnail can never be shown. Nothing has to
 * remember to invalidate anything.
 */
object ThumbnailStore {

    /**
     * Longest edge of a stored thumbnail, in pixels.
     *
     * Sized for the largest request a scrolling list makes: a two-column grid cell
     * is ~150–180 dp wide, which is 460–630 px on a 3x–3.5x display, and the cover
     * is 1/0.78 of that tall — up to ~800 px. 768 is the point where every phone
     * cover is *downscaled* into its box instead of upscaled into it, while a
     * decoded thumbnail stays under 1.8 MB. Anything larger is a viewer/editor
     * request and deliberately decodes the page itself.
     */
    const val LONG_EDGE = 768

    /** Opaque page pixels: 85 keeps text sharp at this size and stays ~40 KB. */
    private const val QUALITY = 85

    /** Upper bound on stored thumbnails before the oldest half is swept. */
    private const val MAX_FILES = 600

    fun dir(context: Context): File =
        File(context.filesDir, "thumbs").apply { if (!exists()) mkdirs() }

    /**
     * Last computed key per page, so the scroll path does not touch the filesystem.
     *
     * `key` used to stat the page file on every request — four syscalls per tile per
     * appearance, including a `length()` that is only there to make the key unique.
     * The value only changes when the page itself changes, and every page write in
     * the app goes through [ImageStore], which calls [writeFor] and refreshes this
     * entry, so the memo cannot go stale behind our back.
     */
    private val keyMemo = ConcurrentHashMap<String, String>()

    /**
     * Cache key for the page behind [imageUri], or `null` when there is no such
     * page (a `content://` URI that was never imported, or a deleted file).
     *
     * Memoised: a hit does no I/O at all.
     */
    fun key(context: Context, imageUri: String?): String? {
        if (imageUri.isNullOrBlank()) return null
        keyMemo[imageUri]?.let { return it }
        return computeKey(context, imageUri)?.also { keyMemo[imageUri] = it }
    }

    /**
     * The memoised key, without falling back to the filesystem — or `null` if this
     * page has never been looked at in this process.
     *
     * For callers that must not block, such as the first frame of a tile.
     */
    fun memoizedKey(imageUri: String?): String? =
        if (imageUri.isNullOrBlank()) null else keyMemo[imageUri]

    /** Forgets a page's key, e.g. once its files are gone. */
    fun invalidate(imageUri: String?) {
        if (imageUri != null) keyMemo.remove(imageUri)
    }

    private fun computeKey(context: Context, imageUri: String): String? {
        val file = ImageStore.resolveFile(context, imageUri) ?: return null
        return "$imageUri#${file.length()}:${file.lastModified()}"
    }

    fun file(context: Context, key: String): File = File(dir(context), sha1(key) + ".jpg")

    fun exists(context: Context, key: String): Boolean = file(context, key).isFile

    /**
     * Decodes the stored thumbnail for [key] **at** the size a tile will draw.
     *
     * Two steps, and the second is the one that matters:
     *
     * 1. `inSampleSize` takes the cheap power-of-two cut. On its own it is not
     *    enough — a 768 px thumbnail for a 459 px cover comes back at full size,
     *    and something then has to make a second bitmap and rescale on the CPU.
     * 2. `inDensity` / `inTargetDensity` ask the *decoder* for the final size, so
     *    the scale happens inside the single native pass: no intermediate 3 MB
     *    bitmap, no `createScaledBitmap`, and Skia's resampler rather than a
     *    second-hand bilinear stretch.
     *
     * That removes roughly 3 MB of allocation per tile that enters the screen.
     * During a fling that is the difference between a quiet collector and a young
     * generation filling every couple of hundred milliseconds. If a platform ever
     * ignores the density hint, the result is simply what this method used to
     * return — the caller still scales, so this can only be faster, never wrong.
     */
    fun read(context: Context, key: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val source = file(context, key)
        if (!source.isFile || source.length() == 0L) return null
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(source.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val targetLong = maxOf(reqWidth, reqHeight)
            val sample = ImageStore.calculateInSampleSize(
                bounds.outWidth,
                bounds.outHeight,
                targetLong
            )
            val sampledLong = maxOf(bounds.outWidth, bounds.outHeight) / sample.coerceAtLeast(1)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
                if (sampledLong > targetLong && targetLong > 0) {
                    // Densities are just a ratio here: the source's own long edge
                    // against the long edge of the box the tile draws into.
                    inScaled = true
                    inDensity = sampledLong
                    inTargetDensity = targetLong
                }
            }
            BitmapFactory.decodeFile(source.absolutePath, options)
        }.getOrNull()
    }

    /**
     * Writes (or replaces) the thumbnail for the page at [imageUri] from [source].
     *
     * Called from the page-saving paths while the full bitmap is still in hand, and
     * from [PageImageLoader] when a page predates this class. The source bitmap is
     * never modified — callers may go on to write it to disk.
     *
     * @return `true` when a thumbnail is on disk afterwards.
     */
    fun writeFor(context: Context, imageUri: String?, source: Bitmap): Boolean {
        if (imageUri.isNullOrBlank()) return false
        // Deliberately re-stat rather than trusting the memo: this runs immediately
        // after the page file was written, so it is the one place that must see the
        // new mtime.
        val key = computeKey(context, imageUri) ?: return false
        keyMemo[imageUri] = key
        return writeBitmap(context, key, source)
    }

    fun writeBitmap(context: Context, key: String, source: Bitmap): Boolean = runCatching {
        if (source.width <= 0 || source.height <= 0) return false
        val scaled = scaleLongEdge(source, LONG_EDGE)
        val target = file(context, key)
        // Temp + rename: a reader (or a second writer) must never see half a JPEG.
        val temp = File(target.parentFile, target.name + ".tmp")
        temp.outputStream().use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, stream)
            stream.flush()
        }
        if (scaled !== source) scaled.recycle()
        if (!temp.renameTo(target)) {
            temp.delete()
            return false
        }
        sweep(context)
        true
    }.getOrDefault(false)

    /** Deletes every stored thumbnail. Used when the page store is reset. */
    fun clear(context: Context) {
        runCatching { dir(context).deleteRecursively() }
    }

    /**
     * Keeps the directory bounded. Runs only when a *new* thumbnail is written, so
     * a file listing never happens on a read path.
     */
    private fun sweep(context: Context) {
        runCatching {
            val files = dir(context).listFiles() ?: return
            if (files.size <= MAX_FILES) return
            files.sortedBy { it.lastModified() }
                .take(files.size - MAX_FILES / 2)
                .forEach { it.delete() }
        }
    }

    /** Scales [source] so its longest edge is at most [maxEdge]. Never upscales. */
    private fun scaleLongEdge(source: Bitmap, maxEdge: Int): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (longest <= maxEdge) return source
        val scale = maxEdge.toFloat() / longest
        val width = (source.width * scale).roundToInt().coerceAtLeast(1)
        val height = (source.height * scale).roundToInt().coerceAtLeast(1)
        return runCatching {
            Bitmap.createScaledBitmap(source, width, height, true)
        }.getOrNull() ?: source
    }

    private fun sha1(value: String): String = runCatching {
        MessageDigest.getInstance("SHA-1")
            .digest(value.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }.getOrElse { value.hashCode().toUInt().toString(16) }
}
