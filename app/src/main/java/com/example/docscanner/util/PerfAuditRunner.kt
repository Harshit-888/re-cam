package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.PdfCompression
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.model.ScannedPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.system.measureTimeMillis

/**
 * On-device performance benchmark runner implementing Muse's §6 measurement split
 * for docs/PERF_RUST_AUDIT.md.
 */
internal object PerfAuditRunner {

    private const val TAG = "DocScannerPerf"

    /** Creates a realistic 3200x2400 document bitmap with varied text, lines and structures. */
    fun createTestPage(width: Int = 2400, height: Int = 3200): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 20, 20)
            textSize = 42f
        }
        for (i in 0 until 50) {
            canvas.drawText(
                "DocScanner Contract Invoice $i: Standard terms and conditions apply. Performance audit benchmark line.",
                80f,
                100f + i * 58f,
                textPaint
            )
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(40, 80, 160)
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawRect(60f, 60f, width - 60f, height - 60f, borderPaint)
        return bitmap
    }

    suspend fun runAudit(context: Context) = withContext(Dispatchers.Default) {
        Log.i(TAG, "==========================================================")
        Log.i(TAG, " STARTING §6 PERFORMANCE & RUST BENCHMARK AUDIT")
        Log.i(TAG, " Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE}, API ${android.os.Build.VERSION.SDK_INT})")
        Log.i(TAG, " Build Type: ${if ((context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) "debug" else "benchmark"}")
        Log.i(TAG, " Rust .so present in build: ${NativeFilters.isLoaded}")
        Log.i(TAG, "==========================================================")

        // 1. Bake time per filter at 3200 px
        val testPage = createTestPage(2400, 3200)
        val filters = listOf(
            FilterType.ORIGINAL,
            FilterType.LIGHTEN,
            FilterType.GRAYSCALE,
            FilterType.MAGIC_COLOR,
            FilterType.CLEAR_BW,
            FilterType.SMART,
            FilterType.LIGHT_TEXT
        )

        for (filter in filters) {
            // Warm-up run for JIT
            if (filter == FilterType.ORIGINAL) {
                val warm = DocumentFilters.apply(testPage, filter, 3200)
                if (warm !== testPage) warm.recycle()
            }
            val elapsed = measureTimeMillis {
                val res = DocumentFilters.apply(testPage, filter, 3200)
                if (res !== testPage) res.recycle()
            }
            Log.i(TAG, "[audit-1-bake] filter=${filter.name} size=3200x2400 time=${elapsed}ms rust=${if (NativeFilters.isLoaded) "present" else "absent"}")
        }

        // 2. 10-page batch time: sequential vs 2-parallel (FilterType.SMART at 3200 px)
        val seqTime = measureTimeMillis {
            for (i in 0 until 10) {
                val res = DocumentFilters.apply(testPage, FilterType.SMART, 3200)
                if (res !== testPage) res.recycle()
            }
        }
        Log.i(TAG, "[audit-2-batch] batch=10 filter=SMART mode=sequential time=${seqTime}ms rust=${if (NativeFilters.isLoaded) "present" else "absent"}")

        val sem = Semaphore(2)
        val parTime = measureTimeMillis {
            val jobs = (0 until 10).map {
                async(Dispatchers.Default) {
                    sem.acquire()
                    try {
                        val res = DocumentFilters.apply(testPage, FilterType.SMART, 3200)
                        if (res !== testPage) res.recycle()
                    } finally {
                        sem.release()
                    }
                }
            }
            jobs.awaitAll()
        }
        val speedup = String.format(java.util.Locale.US, "%.2f", seqTime.toDouble() / parTime)
        Log.i(TAG, "[audit-2-batch] batch=10 filter=SMART mode=parallel2 time=${parTime}ms speedup=${speedup}x rust=${if (NativeFilters.isLoaded) "present" else "absent"}")

        // 3. Editor preview time: 2400 px vs display size (1080 px)
        val corners = QuadCorners()
        val prev2400 = measureTimeMillis {
            val res = ImageProcessingUtils.applyEnhancements(
                src = testPage,
                filterType = FilterType.SMART,
                rotationDegrees = 0f,
                corners = corners,
                maxDimension = 2400
            )
            if (res !== testPage) res.recycle()
        }
        val prev1080 = measureTimeMillis {
            val res = ImageProcessingUtils.applyEnhancements(
                src = testPage,
                filterType = FilterType.SMART,
                rotationDegrees = 0f,
                corners = corners,
                maxDimension = 1080
            )
            if (res !== testPage) res.recycle()
        }
        val prevSpeedup = String.format(java.util.Locale.US, "%.2f", prev2400.toDouble() / prev1080)
        Log.i(TAG, "[audit-3-preview] filter=SMART dim=2400 time=${prev2400}ms vs dim=1080 time=${prev1080}ms speedup=${prevSpeedup}x rust=${if (NativeFilters.isLoaded) "present" else "absent"}")

        // 4. PDF export time and output size (10 pages)
        val tempDir = File(context.cacheDir, "perf_audit_pdf").apply { mkdirs() }
        val testJpg = File(tempDir, "sample_page.jpg")
        FileOutputStream(testJpg).use { out ->
            testPage.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        val testPages = (0 until 10).map { i ->
            ScannedPage(
                id = "audit_page_$i",
                documentId = "audit_doc",
                pageIndex = i,
                imageUri = testJpg.absolutePath,
                filterType = FilterType.SMART
            )
        }

        // Platform writer (Lossless/ORIGINAL)
        val platformTime = measureTimeMillis {
            PdfGenerator.generatePdf(
                context = context,
                title = "audit_platform",
                pages = testPages,
                config = PdfExportConfig(compression = PdfCompression.ORIGINAL)
            )
        }
        val platformFile = File(context.cacheDir, "exports/audit_platform.pdf")
        Log.i(TAG, "[audit-4-pdf] pages=10 writer=platform time=${platformTime}ms size=${if (platformFile.exists()) platformFile.length() else -1} bytes")

        // Custom PdfWriter (BALANCED / JPEG)
        val customTime = measureTimeMillis {
            PdfGenerator.generatePdf(
                context = context,
                title = "audit_custom",
                pages = testPages,
                config = PdfExportConfig(compression = PdfCompression.BALANCED)
            )
        }
        val customFile = File(context.cacheDir, "exports/audit_custom.pdf")
        Log.i(TAG, "[audit-4-pdf] pages=10 writer=custom(balanced) time=${customTime}ms size=${if (customFile.exists()) customFile.length() else -1} bytes")

        // 5. OCR time + accuracy: 2400 px vs 1600 px
        val ocr2400Text: String?
        val ocr2400Time = measureTimeMillis {
            ocr2400Text = OcrEngine.readPage(testJpg.absolutePath)
        }
        val words2400 = ocr2400Text?.split("\\s+".toRegex())?.filter { it.isNotBlank() }?.size ?: 0

        // Create 1600 px sample
        val test1600 = Bitmap.createScaledBitmap(testPage, 1200, 1600, true)
        val file1600 = File(tempDir, "sample_1600.jpg")
        FileOutputStream(file1600).use { out ->
            test1600.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        test1600.recycle()

        val ocr1600Text: String?
        val ocr1600Time = measureTimeMillis {
            ocr1600Text = OcrEngine.readPage(file1600.absolutePath)
        }
        val words1600 = ocr1600Text?.split("\\s+".toRegex())?.filter { it.isNotBlank() }?.size ?: 0

        val accuracyRatio = if (words2400 > 0) String.format(java.util.Locale.US, "%.1f%%", (words1600.toDouble() / words2400) * 100) else "N/A"
        Log.i(TAG, "[audit-5-ocr] dim=2400 time=${ocr2400Time}ms words=$words2400 vs dim=1600 time=${ocr1600Time}ms words=$words1600 accuracy=$accuracyRatio")

        // Cleanup
        testPage.recycle()
        tempDir.deleteRecursively()

        Log.i(TAG, "==========================================================")
        Log.i(TAG, " §6 BENCHMARK AUDIT FINISHED SUCCESSFULLY")
        Log.i(TAG, "==========================================================")
    }
}
