package com.example.docscanner.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Locale
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/**
 * One page, already laid out in PostScript points with the origin at the **top
 * left** — the space `android.graphics.Canvas` uses, so the layout arithmetic is
 * shared with [PdfGenerator] instead of being written twice.
 */
internal class PdfPagePlan(
    val widthPt: Float,
    val heightPt: Float,
    val image: PdfPageImage?,
    val left: Float,
    val top: Float,
    val drawWidth: Float,
    val drawHeight: Float,
    /** Printable box the image is clipped to; `null` when nothing overflows. */
    val clip: RectF?,
    /** Baseline of the footer from the top of the page, or `null` for no footer. */
    val footerBaseline: Float?,
    val footer: String?,
    /** Grey notice drawn when the page image could not be read. */
    val notice: String? = null
)

/** An encoded page image, ready to be written as an `/XObject`. */
internal class PdfPageImage(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    /** `true` for DCT (JPEG), `false` for a lossless Flate stream. */
    val jpeg: Boolean
)

/**
 * A small, complete PDF 1.4 writer.
 *
 * `android.graphics.pdf.PdfDocument` is the right engine for a plain export — it
 * is the platform's own and this app's default output has come from it since
 * round 1. It has exactly two gaps: it cannot encrypt, and it cannot be told how
 * to encode a page image (it writes every bitmap losslessly, which is why a photo
 * scan becomes a multi-megabyte attachment). Both of those live here.
 *
 * Only used when the user asks for a password, for a smaller file, or for a
 * merged document; every other export still goes through the platform writer, so
 * the ordinary path is exactly the code it always was.
 *
 * The file is written **sequentially to disk**, one page at a time, and the xref
 * table is appended at the end. Peak memory is therefore one encoded page, not the
 * whole document — a 60-page merged PDF would otherwise have to be built in RAM.
 */
internal class PdfWriter(
    private val title: String,
    private val password: String?
) {

    private val documentId: ByteArray = ByteArray(16).also { java.util.Random().nextBytes(it) }
    private val offsets = HashMap<Int, Long>()
    private var nextObject = 1
    private var sink: Sink? = null

    private val catalogObject = reserve()
    private val pagesObject = reserve()
    private val fontObject = reserve()
    private val infoObject = reserve()
    private val security: PdfSecurity? =
        password?.takeIf { it.isNotBlank() }?.let { PdfSecurity(it, documentId) }

    /** Reserved *before* any page, so a page number never has to be reused. */
    private val encryptObject: Int = if (security != null) reserve() else 0

    private val pageObjects = ArrayList<Int>()

    /**
     * Writes the document. [body] is handed an `addPage` callback so the caller can
     * decode, encode and recycle one page at a time instead of holding them all.
     */
    fun writeTo(file: File, body: (addPage: (PdfPagePlan) -> Unit) -> Unit) {
        BufferedOutputStream(FileOutputStream(file), STREAM_BUFFER_BYTES).use { output ->
            val target = Sink(output)
            sink = target
            target.write("%PDF-1.4\n")
            // A binary comment line: the marker every tool uses to see that this is
            // not a text file that happens to start with a percent sign.
            target.write(byteArrayOf(0x25, 0xE2.toByte(), 0xE3.toByte(), 0xCF.toByte(), 0xD3.toByte(), 0x0A))

            body(::addPage)

            writeCatalog()
            writePagesRoot()
            writeFont()
            writeInfo()
            writeEncryptDictionary()
            writeCrossReference()

            target.write("startxref\n")
            target.write(xrefOffset.toString())
            target.write("\n%%EOF\n")
        }
        sink = null
    }

    // ------------------------------------------------------------------ pages

    private fun addPage(plan: PdfPagePlan) {
        val pageObject = reserve()
        val contentObject = reserve()
        val imageObject = if (plan.image != null) reserve() else 0

        if (plan.image != null && imageObject != 0) {
            writeStream(imageObject, imageDictionary(plan.image), plan.image.bytes)
        }
        writeStream(contentObject, "", contentStream(plan, imageObject))
        writeObject(pageObject, pageDictionary(plan, contentObject, imageObject))
        pageObjects += pageObject
    }

    private fun imageDictionary(image: PdfPageImage): String = buildString {
        append("/Type /XObject /Subtype /Image")
        append(" /Width ").append(image.width)
        append(" /Height ").append(image.height)
        append(" /ColorSpace /DeviceRGB /BitsPerComponent 8")
        if (image.jpeg) {
            append(" /Filter /DCTDecode")
        } else {
            // Predictor 15 = PNG "Up" filter per row, which turns a photo's smooth
            // gradients into small deltas. Same bytes out, several times smaller
            // than raw RGB through Deflate alone.
            append(" /Filter /FlateDecode")
            append(" /DecodeParms << /Predictor 15 /Colors 3 /BitsPerComponent 8 /Columns ")
            append(image.width).append(" >>")
        }
    }

    private fun contentStream(plan: PdfPagePlan, imageObject: Int): ByteArray {
        val content = StringBuilder()
        if (plan.image != null && imageObject != 0 && plan.drawWidth > 0f && plan.drawHeight > 0f) {
            content.append("q\n")
            plan.clip?.let { clip ->
                // The printable box, in PDF space (bottom-left origin, y up).
                content.append(num(clip.left)).append(' ')
                    .append(num(plan.heightPt - clip.bottom)).append(' ')
                    .append(num(clip.width())).append(' ')
                    .append(num(clip.height())).append(" re W n\n")
            }
            content.append(num(plan.drawWidth)).append(" 0 0 ")
                .append(num(plan.drawHeight)).append(' ')
                .append(num(plan.left)).append(' ')
                .append(num(plan.heightPt - plan.top - plan.drawHeight))
                .append(" cm\n/Im0 Do\nQ\n")
        }
        plan.notice?.let { notice ->
            val size = NOTICE_SIZE
            content.append(text(notice, size, plan.widthPt * 0.5f, plan.heightPt * 0.5f, GREY, centred = true))
        }
        val footer = plan.footer
        val baseline = plan.footerBaseline
        if (!footer.isNullOrEmpty() && baseline != null) {
            content.append(
                text(
                    footer,
                    FOOTER_SIZE,
                    plan.widthPt * 0.5f,
                    plan.heightPt - baseline,
                    FOOTER_GREY,
                    centred = true
                )
            )
        }
        return content.toString().toByteArray(Charsets.ISO_8859_1)
    }

    /** One `BT … ET` block; `x` is the centre when [centred]. */
    private fun text(
        value: String,
        size: Float,
        x: Float,
        baselineFromBottom: Float,
        grey: String,
        centred: Boolean
    ): String {
        val safe = escapeText(value)
        val start = if (centred) x - helveticaWidth(safe, size) / 2f else x
        // The colour operator has to come *outside* the text object: `BT`…`ET` may
        // only contain text operators, and `rg` between them is a specification
        // violation that tolerant readers ignore and strict ones do not.
        return "$grey rg\nBT /F1 $size Tf 1 0 0 1 ${num(start)} ${num(baselineFromBottom)} Tm ($safe) Tj ET\n"
    }

    private fun pageDictionary(plan: PdfPagePlan, contentObject: Int, imageObject: Int): ByteArray {
        val resources = if (imageObject != 0) {
            "<< /XObject << /Im0 $imageObject 0 R >> /Font << /F1 $fontObject 0 R >> >>"
        } else {
            "<< /Font << /F1 $fontObject 0 R >> >>"
        }
        return buildString {
            append("<< /Type /Page /Parent ").append(pagesObject).append(" 0 R")
            append(" /MediaBox [0 0 ").append(num(plan.widthPt)).append(' ')
                .append(num(plan.heightPt)).append(']')
            append(" /Resources ").append(resources)
            append(" /Contents ").append(contentObject).append(" 0 R >>")
        }.toByteArray(Charsets.ISO_8859_1)
    }

    // ----------------------------------------------------------------- header

    private fun writeCatalog() {
        writeObject(catalogObject, "<< /Type /Catalog /Pages $pagesObject 0 R >>".toByteArray(Charsets.ISO_8859_1))
    }

    private fun writePagesRoot() {
        val kids = pageObjects.joinToString(" ") { "$it 0 R" }
        writeObject(
            pagesObject,
            "<< /Type /Pages /Kids [$kids] /Count ${pageObjects.size} >>".toByteArray(Charsets.ISO_8859_1)
        )
    }

    private fun writeFont() {
        // Helvetica is one of the 14 base fonts: no embedding, no subsetting, and
        // every reader already has it. Only page numbers and failure notices use it.
        writeObject(
            fontObject,
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"
                .toByteArray(Charsets.ISO_8859_1)
        )
    }

    private fun writeInfo() {
        val body = if (security != null) {
            // A string in the document body is encrypted under its own object key;
            // the dictionary itself is not. A hex string carries raw bytes, so the
            // escapes of a literal string must not be applied first — the reader
            // decrypts to the value, it does not un-escape afterwards.
            val raw = sanitiseText(title).toByteArray(Charsets.ISO_8859_1)
            "<< /Title <${security.encrypt(infoObject, raw).toHex()}> >>"
        } else {
            "<< /Title (${escapeText(title)}) >>"
        }
        writeObject(infoObject, body.toByteArray(Charsets.ISO_8859_1))
    }

    private fun writeEncryptDictionary() {
        val handler = security ?: return
        val body = buildString {
            append("<< /Filter /Standard /V 2 /R 3 /Length ${PdfSecurity.KEY_LENGTH * 8}")
            append(" /P ${handler.permissions}")
            append(" /O <").append(handler.ownerEntry.toHex()).append('>')
            append(" /U <").append(handler.userEntry.toHex()).append('>')
            append(" >>")
        }
        writeObject(encryptObject, body.toByteArray(Charsets.ISO_8859_1))
    }

    // ----------------------------------------------------------------- output

    private fun writeObject(number: Int, body: ByteArray) {
        val target = requireNotNull(sink) { "PDF writer is not open" }
        offsets[number] = target.position
        target.write("$number 0 obj\n")
        target.write(body)
        target.write("\nendobj\n")
    }

    private fun writeStream(number: Int, dictionary: String, plain: ByteArray) {
        val target = requireNotNull(sink) { "PDF writer is not open" }
        val data = security?.encrypt(number, plain) ?: plain
        val header = if (dictionary.isEmpty()) {
            "<< /Length ${data.size} >>\nstream\n"
        } else {
            "<< $dictionary /Length ${data.size} >>\nstream\n"
        }
        offsets[number] = target.position
        target.write("$number 0 obj\n")
        target.write(header)
        target.write(data)
        target.write("\nendstream\nendobj\n")
    }

    private fun writeCrossReference() {
        val target = requireNotNull(sink) { "PDF writer is not open" }
        val start = target.position
        target.write("xref\n0 $nextObject\n")
        target.write("0000000000 65535 f \n")
        for (number in 1 until nextObject) {
            val offset = offsets[number]
            if (offset == null) {
                target.write("0000000000 65535 f \n")
            } else {
                target.write(String.format(Locale.US, "%010d 00000 n \n", offset))
            }
        }
        val id = documentId.toHex()
        val trailer = buildString {
            append("<< /Size $nextObject /Root $catalogObject 0 R /Info $infoObject 0 R")
            append(" /ID [<$id> <$id>]")
            if (security != null) append(" /Encrypt $encryptObject 0 R")
            append(" >>")
        }
        target.write("trailer\n")
        target.write(trailer)
        target.write("\n")
        // `startxref` follows the table, so it is written by the caller; the offset
        // it needs is the position the table started at.
        xrefOffset = start
    }

    private var xrefOffset = 0L

    private fun reserve(): Int = nextObject++

    /** Buffered, and counting, so object offsets can be recorded without seeking. */
    private class Sink(private val output: OutputStream) {
        var position = 0L
            private set

        fun write(bytes: ByteArray) {
            output.write(bytes)
            position += bytes.size
        }

        fun write(text: String) = write(text.toByteArray(Charsets.ISO_8859_1))
    }

    companion object {
        private const val STREAM_BUFFER_BYTES = 64 * 1024
        private const val FOOTER_SIZE = 9f
        private const val NOTICE_SIZE = 12f
        private const val GREY = "0.5 0.5 0.5"
        private const val FOOTER_GREY = "0.2 0.2 0.2"

        private fun num(value: Float): String = String.format(Locale.US, "%.3f", value)

        /**
         * Printable ASCII only. The only strings this writer emits are the document
         * title, page numbers and a failure notice, so anything outside Latin-1 is
         * replaced rather than written as a byte the font cannot draw. Watermarks are
         * drawn into the page bitmap instead (see [PdfImages.composeWatermark]),
         * which is why they keep their Unicode text.
         */
        fun sanitiseText(raw: String): String {
            val out = StringBuilder(raw.length)
            for (char in raw) {
                when {
                    char.code < 32 -> out.append(' ')
                    char.code > 126 -> out.append('?')
                    else -> out.append(char)
                }
            }
            return out.toString()
        }

        /** The same, as a literal-string body: `(` `)` and `\\` have to be escaped. */
        fun escapeText(raw: String): String =
            sanitiseText(raw)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")

        /**
         * Advance width of [text] in Helvetica at [size], in points.
         *
         * Helvetica's digits are all 556/1000 em, and space, slash, period and comma
         * are 278. That makes page-number centring exact; anything else falls back to
         * 556 (the digit width), which is close enough for a grey notice that is only
         * ever seen when a page failed to load.
         */
        fun helveticaWidth(text: String, size: Float): Float {
            var units = 0
            for (char in text) {
                units += when (char) {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> 556
                    ' ', '/', '.', ',' -> 278
                    'i', 'l', 'j', 'I', '!', '\'', '|' -> 222
                    else -> 556
                }
            }
            return units * size / 1000f
        }
    }
}

/** Encoding page bitmaps into PDF image streams. */
internal object PdfImages {

    /**
     * Lossless: raw RGB through Deflate with a per-row PNG "Up" predictor.
     *
     * The predictor is what makes this worth doing at all — a photo's neighbouring
     * rows are similar, so the deltas are small and Deflate gets roughly half of what
     * it would get from the raw bytes. Decoded output is bit-identical to the input
     * bitmap, which was checked against pypdf and PDFium before this was written.
     *
     * Rows are streamed, so the peak allocation is the deflated page (plus two
     * rows), not the whole image twice.
     */
    fun encodeLossless(bitmap: Bitmap): PdfPageImage {
        val width = bitmap.width
        val height = bitmap.height
        val rowBytes = width * 3
        val row = IntArray(width)
        var previous = ByteArray(rowBytes)
        var current = ByteArray(rowBytes)
        val buffer = ByteArrayOutputStream(rowBytes * 8)
        DeflaterOutputStream(buffer, Deflater(Deflater.DEFAULT_COMPRESSION), STREAM_BUFFER_BYTES).use { deflater ->
            val upFilter = byteArrayOf(2)
            for (y in 0 until height) {
                bitmap.getPixels(row, 0, width, 0, y, width, 1)
                var index = 0
                for (x in 0 until width) {
                    val pixel = row[x]
                    current[index] = (((pixel shr 16) and 0xFF) - previous[index]).toByte()
                    index++
                    current[index] = (((pixel shr 8) and 0xFF) - previous[index]).toByte()
                    index++
                    current[index] = ((pixel and 0xFF) - previous[index]).toByte()
                    index++
                }
                deflater.write(upFilter)
                deflater.write(current, 0, rowBytes)
                val recycled = previous
                previous = current
                current = recycled
            }
        }
        return PdfPageImage(buffer.toByteArray(), width, height, jpeg = false)
    }

    /** Lossy: DCT (JPEG) at [quality]. */
    fun encodeJpeg(bitmap: Bitmap, quality: Int): PdfPageImage {
        val buffer = ByteArrayOutputStream(bitmap.width * bitmap.height / 8 + 4096)
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), buffer)
        return PdfPageImage(buffer.toByteArray(), bitmap.width, bitmap.height, jpeg = true)
    }

    /**
     * Draws the page watermark into the bitmap.
     *
     * The watermark is *text*, and the writer's only font is a base-14 Helvetica —
     * which cannot draw a Devanagari or Cyrillic watermark at all. Compositing it
     * with the platform's own text stack instead keeps whatever the user typed,
     * keeps the same rotation (about the page centre, -35°), the same alpha (38/255)
     * and the same relative size as [PdfGenerator]'s canvas path, and costs nothing
     * when a page has no watermark.
     *
     * @return a bitmap to own and recycle; may be [source] itself when it was
     *   already mutable.
     */
    fun composeWatermark(source: Bitmap, text: String, fontSizePx: Float): Bitmap {
        val target = if (source.isMutable && !source.hasAlpha()) {
            source
        } else {
            source.copy(Bitmap.Config.ARGB_8888, true) ?: return source
        }
        val canvas = Canvas(target)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(38, 0, 0, 0)
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            textSize = fontSizePx
        }
        canvas.save()
        canvas.rotate(-35f, target.width / 2f, target.height / 2f)
        canvas.drawText(text, target.width / 2f, target.height / 2f, paint)
        canvas.restore()
        return target
    }

    /** Scales down without ever upscaling; returns [source] when it already fits. */
    fun scaleDown(source: Bitmap, scale: Float): Bitmap {
        if (scale >= 1f) return source
        val width = (source.width * scale).toInt().coerceAtLeast(1)
        val height = (source.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    private const val STREAM_BUFFER_BYTES = 64 * 1024
}
