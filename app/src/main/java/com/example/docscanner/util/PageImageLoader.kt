package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Decodes page images off the main thread, in front of two caches.
 *
 * # The shape of the problem
 *
 * A grid tile is ~470 px wide; the page behind it is up to 3200 px, and for
 * *Clear B&W* / *Grayscale* a **PNG**. JPEG has DCT scaling, so a sampled decode
 * is cheap; a PNG does not, so every sampled decode still inflates the whole
 * image — ~30 MB of transient allocation and tens of milliseconds of CPU, per
 * tile, while the user scrolls.
 *
 * Three layers keep that off the scroll path:
 *
 * 1. [ThumbnailStore] holds one ~768 px JPEG per page, written when the page was
 *    saved (see [ImageStore]) — so a cover never decodes a page at all.
 * 2. The in-memory cache holds bitmaps already scaled to the size the UI asked
 *    for, keyed by page *and* request size.
 * 3. [prefetch] warms the memory cache for tiles just below the viewport, so one
 *    entering the screen finds a bitmap instead of starting a decode.
 *
 * The slow path still exists — a page imported before [ThumbnailStore] existed, or
 * a re-baked page whose key changed — but it is the exception, and
 * [primeThumbnails] mops those up once at start-up instead of during a fling.
 *
 * Bitmaps returned here are **shared** and must not be recycled or mutated by
 * callers. [ImageProcessingUtils.applyEnhancements] never recycles its source, so
 * passing a cached bitmap into it is safe.
 */
object PageImageLoader {

    /**
     * Fraction of the heap the bitmap cache may use.
     *
     * A 470x600 cover is ~1.1 MB, and the grid keeps a screenful plus the prefetch
     * window live — so the cap has to hold tens of them, not four. The previous
     * floor of 8 MB did not, which made the cache thrash on small-heap devices:
     * every tile was evicted before the next frame needed it.
     */
    private val maxCacheKilobytes: Int = (Runtime.getRuntime().maxMemory() / 1024L / 8L)
        .toInt()
        .coerceAtLeast(24 * 1024)

    private val cache = object : LruCache<String, Bitmap>(maxCacheKilobytes) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            value.allocationByteCount / 1024
    }

    /**
     * Decode budget: two at a time.
     *
     * A fling can put a dozen tiles on screen at once. Letting all of them decode
     * in parallel floods the CPU the render thread needs, so the list stutters
     * exactly when it is moving fastest. Two decodes keep the queue moving without
     * starving the UI.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val decodeDispatcher = Dispatchers.IO.limitedParallelism(2)

    /**
     * Loads in flight, keyed exactly like [cache].
     *
     * A fling disposes and recomposes tiles faster than an image can be decoded, so
     * the *same* page is frequently requested twice at once — once by the tile that
     * is leaving, once by the tile that is arriving, and a third time by prefetch.
     * Without this map all three decode the same file.
     *
     * Waiters must never take a [decodeDispatcher] slot while they wait: the
     * limiter holds its permit for the whole coroutine body, so two waiters would
     * starve the decoder they are waiting for. Only the leader decodes.
     */
    private val inFlight = ConcurrentHashMap<String, CompletableDeferred<Bitmap?>>()

    /** Decodes at a sampling rate appropriate for [reqWidth] x [reqHeight]. */
    suspend fun load(
        context: Context,
        imageUri: String?,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        if (imageUri.isNullOrBlank() || reqWidth <= 0 || reqHeight <= 0) return null

        // The key stats the page file. `load` is called from `produceState`, i.e.
        // on the main dispatcher, so this is kept off it deliberately.
        val thumbKey = withContext(Dispatchers.IO) { ThumbnailStore.key(context, imageUri) }
            ?: return null
        val key = "$thumbKey@${reqWidth}x$reqHeight"
        cache.get(key)?.let {
            PerfTrace.onCacheHit()
            return it
        }

        val gate = CompletableDeferred<Bitmap?>()
        val leader = inFlight.putIfAbsent(key, gate)
        if (leader != null) {
            val shared = leader.await()
            // The leader was cancelled mid-decode (its tile scrolled away). Show
            // this caller the image anyway rather than a permanent placeholder.
            return shared ?: decodeAndCache(context, thumbKey, imageUri, reqWidth, reqHeight)
        }

        return try {
            val bitmap = decodeAndCache(context, thumbKey, imageUri, reqWidth, reqHeight)
            gate.complete(bitmap)
            bitmap
        } catch (t: Throwable) {
            // Wake waiters even on failure; they fall back to decoding themselves.
            gate.complete(null)
            throw t
        } finally {
            inFlight.remove(key, gate)
        }
    }

    /**
     * Warms the memory cache for a tile that is about to become visible.
     *
     * Fire-and-forget: the result is cached, so the tile that later asks for it
     * either finds it in memory or joins this decode as a waiter.
     */
    suspend fun prefetch(context: Context, imageUri: String?, reqWidth: Int, reqHeight: Int) {
        try {
            load(context, imageUri, reqWidth, reqHeight)
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            // Never swallow cancellation: a stale prefetch must stop the caller's
            // loop too, not quietly walk on to the next tile.
            throw cancellation
        } catch (_: Throwable) {
            // A prefetch failing is not an error anyone can act on: the tile will
            // ask for itself when it becomes visible.
        }
    }

    /**
     * Writes thumbnails for pages that predate [ThumbnailStore], or whose entry was
     * swept, for up to [limit] pages that still need one.
     *
     * Deliberately a chunk rather than a sweep: the caller runs it while the list is
     * idle and stops when a scroll starts, and cancellation is honoured between
     * pages. Pages it does not reach are still served by the slow path, so the only
     * cost of never finishing is a decode the first time one of them is shown.
     *
     * @return how many thumbnails were written; `0` means the library is warm.
     */
    suspend fun primeThumbnails(
        context: Context,
        imageUris: List<String>,
        limit: Int = PRIME_LIMIT
    ): Int = withContext(Dispatchers.IO) {
        var written = 0
        imageUris.asSequence()
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { uri ->
                if (written >= limit) return@forEach
                ensureActive()
                val key = ThumbnailStore.key(context, uri) ?: return@forEach
                if (ThumbnailStore.exists(context, key)) return@forEach
                val decoded = ImageStore.loadBitmap(context, uri, ThumbnailStore.LONG_EDGE)
                    ?: return@forEach
                if (ThumbnailStore.writeFor(context, uri, decoded)) written++
                decoded.recycle()
            }
        written
    }

    /**
     * The decode itself: memory cache, then the stored thumbnail, then the page.
     *
     * Cancelling before the decode starts is what keeps a fast fling cheap. An
     * in-flight `BitmapFactory.decodeFile` cannot be interrupted, so the only
     * useful moment to give up is here — otherwise tiles that have already left
     * the screen keep occupying both decode slots while the tiles the user is
     * actually looking at queue behind them. Stale prefetches die the same way.
     */
    private suspend fun decodeAndCache(
        context: Context,
        thumbKey: String,
        imageUri: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? = withContext(decodeDispatcher) {
        coroutineContext.ensureActive()
        val key = "$thumbKey@${reqWidth}x$reqHeight"
        cache.get(key)?.let { return@withContext it }

        // Fast path: a JPEG a few hundred pixels wide, written when the page was
        // saved. This is what a scroll should always hit.
        if (maxOf(reqWidth, reqHeight) <= ThumbnailStore.LONG_EDGE) {
            ThumbnailStore.read(context, thumbKey, reqWidth, reqHeight)?.let { thumb ->
                val fitted = scaleToFit(thumb, reqWidth, reqHeight)
                cache.put(key, fitted)
                PerfTrace.onThumbnailDecode()
                return@withContext fitted
            }
        }

        // Slow path: no stored thumbnail (legacy or re-baked page, or a request
        // larger than a thumbnail should be). Decode the page and leave a
        // thumbnail behind so the next request — and every other screen — is fast.
        val decoded = ImageStore.loadBitmap(context, imageUri, reqWidth, reqHeight)
            ?: return@withContext null
        PerfTrace.onPageDecode()
        if (maxOf(reqWidth, reqHeight) <= ThumbnailStore.LONG_EDGE) {
            // Written from the full bitmap, *before* scaleToFit may recycle it.
            ThumbnailStore.writeFor(context, imageUri, decoded)
        }

        // `inSampleSize` only halves, so a 3200 px page asked for at 470 px comes
        // back at 800 px: 2.4 MB held per tile instead of 1.1 MB, and a texture
        // the GPU re-uploads on every frame of a fling. Finishing the job here
        // costs a few milliseconds off the main thread and makes the cached bitmap
        // exactly the size the tile draws.
        val bitmap = scaleToFit(decoded, reqWidth, reqHeight)
        cache.put(key, bitmap)
        bitmap
    }

    /**
     * Shrinks [source] to fit inside [maxWidth] x [maxHeight], keeping its aspect
     * ratio, and recycles the original when it had to make a copy.
     *
     * Only ever shrinks: a small page is never upscaled to fill the box.
     */
    private fun scaleToFit(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (source.width <= maxWidth && source.height <= maxHeight) return source
        val scale = minOf(
            maxWidth.toFloat() / source.width,
            maxHeight.toFloat() / source.height
        )
        val width = (source.width * scale).roundToInt().coerceAtLeast(1)
        val height = (source.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = runCatching {
            Bitmap.createScaledBitmap(source, width, height, true)
        }.getOrNull() ?: return source
        if (scaled !== source) source.recycle()
        return scaled
    }

    /** Full-resolution decode for editing/export, bypassing both caches. */
    suspend fun loadForEdit(context: Context, imageUri: String?, maxDimension: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(context, imageUri, maxDimension)
        }

    /** Empties the memory cache, and the stored thumbnails when [context] is given. */
    fun clear(context: Context? = null) {
        cache.evictAll()
        if (context != null) ThumbnailStore.clear(context)
    }

    /** Pages repaired per idle chunk. Small enough that one chunk is never felt. */
    private const val PRIME_LIMIT = 8
}
