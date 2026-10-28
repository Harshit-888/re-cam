package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.docscanner.model.ScannedPage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Single owner of where page pixels live on disk.
 *
 * Layout under [Context.getFilesDir]:
 * ```
 * files/pages/<pageId>.jpg          pristine capture / first bake source
 * files/pages/<pageId>_edited.jpg   warp + rotation + filter bake shown to the user
 * files/thumbs/<sha1>.jpg           one 768 px cover per page, written on save
 * files/signatures/<uuid>.png       transparent signature strokes
 * cache/exports/<title>_<ts>.pdf    generated PDFs
 * ```
 *
 * Everything here is blocking file I/O — call it from `Dispatchers.IO` or from a
 * thread that is allowed to block (e.g. a `Modifier.pointerInput` handler that
 * only touches a small thumbnail). Never from a composable's composition phase.
 */
object ImageStore {

    private const val PAGES_DIR = "pages"
    private const val SIGNATURES_DIR = "signatures"
    private const val EXPORTS_DIR = "exports"

    /**
     * Longest edge kept for a captured page — **3200 px**.
     *
     * Matched to `DocumentFilters.MAX_PROCESS_DIMENSION` and to
     * `ImageQuality.HIGH`. A page cannot be saved sharper than it was captured, so
     * capping here at 2600 while the pipeline asked for 3200 would have silently
     * made the A4 page a 243 DPI scan instead of a 300 DPI one — the difference
     * between text that survives a zoom and text that turns into blocks.
     */
    const val MAX_CAPTURE_DIMENSION = 3200

    const val JPEG_QUALITY = 92

    // ---------------------------------------------------------------- paths

    fun pagesDir(context: Context): File =
        File(context.filesDir, PAGES_DIR).apply { if (!exists()) mkdirs() }

    fun signaturesDir(context: Context): File =
        File(context.filesDir, SIGNATURES_DIR).apply { if (!exists()) mkdirs() }

    fun exportsDir(context: Context): File =
        File(context.cacheDir, EXPORTS_DIR).apply { if (!exists()) mkdirs() }

    /** Canonical pristine file for a page id. */
    fun pageFile(context: Context, pageId: String): File =
        File(pagesDir(context), "$pageId.jpg")

    /** File that holds the rendered result of the editor's bake. */
    fun editedFile(context: Context, pageId: String): File =
        File(pagesDir(context), "${pageId}_edited.jpg")

    /**
     * Lossless twin of [editedFile], used for the filters that produce flat or
     * bi-level output.
     *
     * *Clear B&W* is the reason this exists. A JPEG is a 8 × 8 DCT of a block of
     * glyph edges: at quality 88–95 the ringing around every stroke is baked in,
     * and since the PDF embeds the decoded bitmap losslessly that damage becomes
     * the PDF's ceiling. A PNG of binary text is both smaller and exact.
     *
     * The two files coexist deliberately: pages baked before this change keep
     * their `.jpg` and remain valid, because the stored `imageUri` is an absolute
     * path, not a name convention.
     */
    fun editedLosslessFile(context: Context, pageId: String): File =
        File(pagesDir(context), "${pageId}_edited.png")

    fun signatureFile(context: Context, signatureId: String): File =
        File(signaturesDir(context), "$signatureId.png")

    /**
     * Tolerant lookup of any path-ish string we may have stored.
     *
     * Accepts an absolute path, a `file://` URI, or a bare filename inside
     * [pagesDir]. Returns `null` for `content://` URIs (callers must import
     * first) and for files that no longer exist.
     */
    fun resolveFile(context: Context, imageUri: String?): File? {
        if (imageUri.isNullOrBlank()) return null
        val candidate = when {
            imageUri.startsWith("content://") -> return null
            imageUri.startsWith("file://") ->
                Uri.parse(imageUri).path?.let(::File) ?: return null
            imageUri.contains('/') -> File(imageUri)
            else -> File(pagesDir(context), imageUri)
        }
        return candidate.takeIf { it.isFile && it.length() > 0L }
    }

    // ------------------------------------------------------------- importing

    /**
     * Copies an image picked from the gallery (or any `content://` source) into
     * private storage, correcting EXIF orientation and downscaling to
     * [MAX_CAPTURE_DIMENSION].
     *
     * @param maxDimension longest edge kept after downscaling (Settings → Image
     *   quality). Defaults to [MAX_CAPTURE_DIMENSION], i.e. keep everything.
     * @param quality JPEG quality used when re-encoding.
     * @return the absolute path of the stored JPEG, or `null` on failure.
     */
    fun importFromUri(
        context: Context,
        uri: Uri,
        pageId: String,
        maxDimension: Int = MAX_CAPTURE_DIMENSION,
        quality: Int = JPEG_QUALITY
    ): String? = try {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null || bytes.isEmpty()) {
            null
        } else {
            val orientation = readExifOrientation(bytes)
            val decoded = decodeSampled(bytes, maxDimension, Bitmap.Config.ARGB_8888)
            if (decoded == null) {
                null
            } else {
                val oriented = applyExifOrientation(decoded, orientation)
                val target = pageFile(context, pageId)
                writeJpeg(oriented, target, quality)
                // The cover every screen will show, built now while the pixels are
                // already decoded. See ThumbnailStore for why this cannot wait for
                // the home screen to ask for it.
                ThumbnailStore.writeFor(context, target.absolutePath, oriented)
                if (oriented !== decoded) decoded.recycle()
                target.absolutePath
            }
        }
    } catch (_: Throwable) {
        null
    }

    /**
     * Imports a JPEG that something else already wrote to disk — used after a
     * CameraX capture, which hands us a temp file in `cacheDir`.
     *
     * Applies EXIF orientation, downscales, and deletes the temp file.
     */
    fun importFromFile(
        context: Context,
        sourceFile: File,
        pageId: String,
        maxDimension: Int = MAX_CAPTURE_DIMENSION,
        quality: Int = JPEG_QUALITY
    ): String? = try {
        val bytes = sourceFile.readBytes()
        if (bytes.isEmpty()) {
            null
        } else {
            val orientation = readExifOrientation(bytes)
            val decoded = decodeSampled(bytes, maxDimension, Bitmap.Config.ARGB_8888)
            if (decoded == null) {
                null
            } else {
                val oriented = applyExifOrientation(decoded, orientation)
                val target = pageFile(context, pageId)
                writeJpeg(oriented, target, quality)
                ThumbnailStore.writeFor(context, target.absolutePath, oriented)
                if (oriented !== decoded) decoded.recycle()
                target.absolutePath
            }
        }
    } catch (_: Throwable) {
        null
    } finally {
        runCatching { sourceFile.delete() }
    }

    /** Convenience: import a picked URI straight into a new [ScannedPage]. */
    fun createPageFromUri(
        context: Context,
        uri: Uri,
        documentId: String,
        pageIndex: Int,
        maxDimension: Int = MAX_CAPTURE_DIMENSION,
        quality: Int = JPEG_QUALITY
    ): ScannedPage? {
        val pageId = UUID.randomUUID().toString()
        val path = importFromUri(context, uri, pageId, maxDimension, quality) ?: return null
        return ScannedPage(
            id = pageId,
            documentId = documentId,
            pageIndex = pageIndex,
            imageUri = path
        )
    }

    /**
     * Copies a page's files to a **new page id** and returns the copied record.
     *
     * Duplicating a page cannot simply reuse its paths: files are named by page id, and
     * deleting either copy would delete the other's pixels out from under it. So the
     * pristine capture, the bake (whichever of `.jpg`/`.png` it is) and a fresh
     * thumbnail are all written under [newPageId].
     *
     * The signature overlay is deliberately **shared**: signature files are keyed by
     * signature id and [deletePageFiles] never touches them, so one copy is correct.
     *
     * @return the new page, or `null` if the pristine capture could not be copied.
     */
    fun copyPageFiles(
        context: Context,
        source: ScannedPage,
        newPageId: String
    ): ScannedPage? {
        val pristine = resolveFile(context, source.originalImageUri ?: source.imageUri)
            ?: return null
        val pristineTarget = pageFile(context, newPageId)
        return runCatching {
            pristine.parentFile?.let { if (!it.exists()) it.mkdirs() }
            pristine.copyTo(pristineTarget, overwrite = true)
            pristineTarget.setLastModified(nextStamp())
        }.mapCatching {
            // The baked file may be absent (a page never edited) — then there is
            // nothing to copy and the new page starts from its pristine capture.
            val edited = listOf(editedFile(context, source.id), editedLosslessFile(context, source.id))
                .firstOrNull { it.isFile }
            var newUri = pristineTarget.absolutePath
            var revision = 0
            if (edited != null) {
                val target = if (edited.extension.equals("png", true)) {
                    editedLosslessFile(context, newPageId)
                } else {
                    editedFile(context, newPageId)
                }
                edited.copyTo(target, overwrite = true)
                target.setLastModified(nextStamp())
                newUri = target.absolutePath
                revision = source.revision
            }
            // A fresh cover for the fresh page: the source's thumbnail key belongs to
            // the source file's size and mtime, so it cannot be reused.
            loadBitmap(context, pristineTarget.absolutePath, ThumbnailStore.LONG_EDGE)?.let { bitmap ->
                ThumbnailStore.writeFor(context, newUri, bitmap)
            }
            source.copy(
                id = newPageId,
                imageUri = newUri,
                originalImageUri = pristineTarget.absolutePath,
                revision = revision
            )
        }.getOrNull()
    }

    // ---------------------------------------------------------------- saving

    /**
     * Persists the editor's bake. The pristine file is never overwritten; the
     * result goes to [editedFile] and becomes the page's `imageUri`.
     *
     * @return absolute path of the baked file, or `null` on failure.
     */
    fun saveEditedPage(
        context: Context,
        pageId: String,
        bitmap: Bitmap,
        quality: Int = JPEG_QUALITY,
        lossless: Boolean = false
    ): String? = try {
        // Whichever we write, the other format's stale file must go: a later
        // rebake with the other filter would otherwise leave two candidates on
        // disk and `imageUri` decides which one wins. Deleted *after* the new file
        // is written — if the write throws, the previous page must still exist.
        val stale = if (lossless) editedFile(context, pageId) else editedLosslessFile(context, pageId)
        val target = if (lossless) editedLosslessFile(context, pageId) else editedFile(context, pageId)
        if (lossless) writePng(bitmap, target) else writeJpeg(bitmap, target, quality)
        runCatching { stale.delete() }
        // The key includes the page's mtime, so this deliberately runs *after* the
        // write: the old cover is invalidated by the new mtime and a fresh one is
        // stored under the new key, with no bookkeeping anywhere else.
        ThumbnailStore.writeFor(context, target.absolutePath, bitmap)
        target.absolutePath
    } catch (_: Throwable) {
        null
    }

    /**
     * Writes [bitmap] to [target] as a PNG.
     *
     * For *Clear B&W* and *Grayscale* this is both more faithful and smaller than a
     * JPEG: binary glyphs are exactly what DEFLATE is good at, and it cannot ring.
     */
    fun writePng(bitmap: Bitmap, target: File) {
        target.parentFile?.let { if (!it.exists()) it.mkdirs() }
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        // Bitmap and thumbnail cache keys include the file's mtime; make sure it
        // strictly advances (see [nextStamp]).
        target.setLastModified(nextStamp())
    }

    /**
     * Writes [bitmap] to [target] as a JPEG.
     *
     * Always rewrites and always bumps the file's mtime: the bitmap cache key
     * includes mtime, so a re-bake must invalidate stale thumbnails even when
     * the new bytes happen to be identical.
     */
    fun writeJpeg(bitmap: Bitmap, target: File, quality: Int = JPEG_QUALITY) {
        target.parentFile?.let { if (!it.exists()) it.mkdirs() }
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.flush()
        }
        // Bitmap and thumbnail cache keys include the file's mtime; make sure it
        // strictly advances (see [nextStamp]).
        target.setLastModified(nextStamp())
    }

    /**
     * A modification stamp that is **strictly increasing within this process**.
     *
     * Both caches ([PageImageLoader]'s bitmaps and [ThumbnailStore]'s files) treat
     * "same path, same length, same mtime" as "same pixels". `currentTimeMillis()`
     * does not guarantee that: a re-bake that produces the same byte count within
     * the same millisecond — or within the same *second* on a filesystem with
     * coarse timestamps — would keep the old key and show the pre-edit cover.
     * Handing out a stamp that only ever moves forward closes that hole without
     * letting timestamps drift into the future by more than the number of writes
     * made inside one millisecond.
     */
    private var lastStamp = 0L

    private fun nextStamp(): Long = synchronized(this) {
        val candidate = maxOf(System.currentTimeMillis(), lastStamp + 1L)
        lastStamp = candidate
        candidate
    }

    /** Saves a signature PNG (transparent background). */
    fun saveSignature(context: Context, bitmap: Bitmap): String? = try {
        val target = signatureFile(context, UUID.randomUUID().toString())
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        target.absolutePath
    } catch (_: Throwable) {
        null
    }

    // --------------------------------------------------------------- loading

    /**
     * Decodes with `inSampleSize` chosen for [reqWidth] x [reqHeight].
     *
     * [config] exists for the one caller that draws decoration rather than a
     * document: a library cover is shown at a few hundred pixels behind a ripple
     * and a scrim, and `RGB_565` halves both its heap footprint and the texture
     * the GPU uploads for it. Editing, viewing and export always ask for
     * `ARGB_8888`, which stays the default so nothing can lose colour by accident.
     */
    fun loadBitmap(
        context: Context,
        imageUri: String?,
        reqWidth: Int,
        reqHeight: Int,
        config: Bitmap.Config = Bitmap.Config.ARGB_8888
    ): Bitmap? {
        val file = resolveFile(context, imageUri) ?: return null
        return decodeFile(file, maxOf(reqWidth, reqHeight), config)
    }

    /** Full-fidelity decode, capped at [maxDimension] on the longest edge. */
    fun loadBitmap(context: Context, imageUri: String?, maxDimension: Int): Bitmap? {
        val file = resolveFile(context, imageUri) ?: return null
        return decodeFile(file, maxDimension, Bitmap.Config.ARGB_8888)
    }

    private fun decodeFile(file: File, maxDimension: Int, config: Bitmap.Config): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            inPreferredConfig = config
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    fun decodeSampled(bytes: ByteArray, maxDimension: Int, config: Bitmap.Config): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            inPreferredConfig = config
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        if (maxDimension <= 0) return 1
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / 2 >= maxDimension) {
            longest /= 2
            sample *= 2
        }
        return sample
    }

    // -------------------------------------------------------------- EXIF

    fun readExifOrientation(bytes: ByteArray): Int = try {
        ExifInterface(bytes.inputStream()).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } catch (_: Throwable) {
        ExifInterface.ORIENTATION_NORMAL
    }

    fun readExifOrientation(stream: InputStream): Int = try {
        ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } catch (_: Throwable) {
        ExifInterface.ORIENTATION_NORMAL
    }

    /**
     * Bakes EXIF rotation/flip into pixels. Cameras store portrait shots as
     * landscape pixels plus an orientation tag; without this every portrait
     * capture lands sideways.
     */
    fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrDefault(bitmap)
    }

    // -------------------------------------------------------------- cleanup

    /** Deletes both the pristine and edited files for a page. */
    fun deletePageFiles(context: Context, page: ScannedPage) {
        // Resolve the covers while the files still exist: their keys are derived
        // from the file's size and mtime, so once it is gone the name is unknown.
        val thumbKeys = listOfNotNull(
            ThumbnailStore.key(context, pageFile(context, page.id).absolutePath),
            ThumbnailStore.key(context, editedFile(context, page.id).absolutePath),
            ThumbnailStore.key(context, editedLosslessFile(context, page.id).absolutePath)
        )
        runCatching { pageFile(context, page.id).delete() }
        runCatching { editedFile(context, page.id).delete() }
        runCatching { editedLosslessFile(context, page.id).delete() }
        thumbKeys.forEach { key ->
            runCatching { ThumbnailStore.file(context, key).delete() }
        }
        // Drop the memoised keys as well: nothing should keep answering for a page
        // whose files are gone.
        ThumbnailStore.invalidate(pageFile(context, page.id).absolutePath)
        ThumbnailStore.invalidate(editedFile(context, page.id).absolutePath)
        ThumbnailStore.invalidate(editedLosslessFile(context, page.id).absolutePath)
    }

    fun deleteDocumentFiles(context: Context, pages: List<ScannedPage>) {
        pages.forEach { deletePageFiles(context, it) }
    }

    /**
     * Removes page files that no document references.
     *
     * Two rules make this safe to run automatically, and both are load-bearing:
     *
     * * [referencedPaths] must contain **all three** names a page can occupy —
     *   `pages/<id>.jpg`, `<_edited.jpg>` and `<_edited.png>`. The model only ever
     *   names the baked file once a page has been edited; sweeping on that alone
     *   would delete every pristine capture in the library, which is the source
     *   every later re-edit is rendered from.
     * * Anything modified inside [ORPHAN_GRACE_MS] is left alone. The camera writes
     *   a page the moment the shutter fires, before any document points at it, so a
     *   sweep that ran during a capture would delete the page the user is about to
     *   keep.
     *
     * @return how many files were deleted (for logs/tests).
     */
    fun sweepOrphans(
        context: Context,
        referencedPaths: Set<String>,
        graceMs: Long = ORPHAN_GRACE_MS
    ): Int {
        var deleted = 0
        runCatching {
            val now = System.currentTimeMillis()
            pagesDir(context).listFiles()?.forEach { file ->
                val orphaned = isOrphanedPageFile(
                    filePath = file.absolutePath,
                    lastModified = file.lastModified(),
                    referencedPaths = referencedPaths,
                    now = now,
                    graceMs = graceMs
                )
                if (orphaned && runCatching { file.delete() }.getOrDefault(false)) deleted++
            }
        }
        return deleted
    }

    /** Files younger than this may belong to a capture that has not been committed. */
    internal const val ORPHAN_GRACE_MS = 10 * 60 * 1000L
}

/**
 * Whether [filePath] may be deleted because nothing in the library needs it.
 *
 * Split out of [ImageStore.sweepOrphans] and kept pure so the two rules that make
 * the sweep safe can be tested without a `Context`. Getting either wrong destroys
 * user data: the first would delete the pristine capture of every edited page, the
 * second would delete a page the camera has just written and is about to commit.
 */
internal fun isOrphanedPageFile(
    filePath: String,
    lastModified: Long,
    referencedPaths: Set<String>,
    now: Long,
    graceMs: Long = ImageStore.ORPHAN_GRACE_MS
): Boolean = filePath !in referencedPaths && lastModified < now - graceMs
