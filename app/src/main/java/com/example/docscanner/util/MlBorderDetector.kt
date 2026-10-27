package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import com.example.docscanner.model.QuadCorners
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * On-device border detector matching the original app's ML pipeline (`ht3.java` & `qt3.java`).
 *
 * Runs `assets/border_detect_224.tflite` on a 224x224 RGB image input, extracting 4 corner
 * keypoints (normalized [0..1]) and per-corner confidence scores.
 * If the mean confidence is >= 0.5f, the detected quad is used; otherwise it falls back to
 * heuristic / full-frame contour detection.
 */
object MlBorderDetector {
    private const val MODEL_FILE = "border_detect_224.tflite"
    private const val INPUT_SIZE = 224

    @Volatile
    var appContext: Context? = null

    @Volatile
    private var interpreter: Interpreter? = null
    private var isInitialized = false

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    @Synchronized
    private fun getOrInit(context: Context): Interpreter? {
        if (interpreter != null) return interpreter
        if (isInitialized) return null
        return try {
            val assetFd = context.assets.openFd(MODEL_FILE)
            FileInputStream(assetFd.fileDescriptor).use { fis ->
                val channel = fis.channel
                val buffer = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFd.startOffset,
                    assetFd.declaredLength
                )
                val options = Interpreter.Options().apply {
                    setNumThreads(4)
                }
                Interpreter(buffer, options).also {
                    interpreter = it
                    isInitialized = true
                }
            }
        } catch (_: Throwable) {
            isInitialized = true
            null
        }
    }

    data class MlResult(
        val corners: QuadCorners,
        val meanConfidence: Float,
        val confident: Boolean
    )

    fun detect(bitmap: Bitmap, context: Context? = appContext): MlResult? {
        val ctx = context ?: return null
        if (bitmap.isRecycled) return null
        val interp = getOrInit(ctx) ?: return null

        return try {
            val scaled = if (bitmap.width == INPUT_SIZE && bitmap.height == INPUT_SIZE) {
                bitmap
            } else {
                Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            }

            val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
            if (scaled !== bitmap && !scaled.isRecycled) {
                scaled.recycle()
            }

            for (p in pixels) {
                inputBuffer.putFloat(((p shr 16) and 0xFF).toFloat())
                inputBuffer.putFloat(((p shr 8) and 0xFF).toFloat())
                inputBuffer.putFloat((p and 0xFF).toFloat())
            }
            inputBuffer.rewind()

            // Allocate output structures matching ht3.java
            val coordsIndex = interp.getOutputIndex("StatefulPartitionedCall:1")
            val scoresIndex = interp.getOutputIndex("StatefulPartitionedCall:0")

            val outCoords = Array(1) { Array(1) { Array(4) { FloatArray(2) } } } // [1, 1, 4, 2]
            val outScores = Array(1) { Array(1) { FloatArray(4) } }             // [1, 1, 4]

            val outputs = mutableMapOf<Int, Any>(
                coordsIndex to outCoords,
                scoresIndex to outScores
            )

            for (i in 0 until interp.outputTensorCount) {
                if (i != coordsIndex && i != scoresIndex) {
                    val shape = interp.getOutputTensor(i).shape()
                    outputs[i] = createDummyOutput(shape)
                }
            }

            interp.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

            val rawCoords = outCoords[0][0] // 4 points of [y, x]
            val scores = outScores[0][0]   // 4 floats

            val meanScore = (scores[0] + scores[1] + scores[2] + scores[3]) / 4f

            // In ht3.java:
            // Point 0 (TopLeft): x = rawCoords[0][1], y = rawCoords[0][0]
            // Point 1 (TopRight): x = rawCoords[1][1], y = rawCoords[1][0]
            // Point 2 (BottomRight): x = rawCoords[2][1], y = rawCoords[2][0]
            // Point 3 (BottomLeft): x = rawCoords[3][1], y = rawCoords[3][0]
            val quad = QuadCorners(
                topLeftX = rawCoords[0][1].coerceIn(0f, 1f),
                topLeftY = rawCoords[0][0].coerceIn(0f, 1f),
                topRightX = rawCoords[1][1].coerceIn(0f, 1f),
                topRightY = rawCoords[1][0].coerceIn(0f, 1f),
                bottomRightX = rawCoords[2][1].coerceIn(0f, 1f),
                bottomRightY = rawCoords[2][0].coerceIn(0f, 1f),
                bottomLeftX = rawCoords[3][1].coerceIn(0f, 1f),
                bottomLeftY = rawCoords[3][0].coerceIn(0f, 1f)
            )

            MlResult(
                corners = quad,
                meanConfidence = meanScore,
                confident = meanScore >= 0.5f
            )
        } catch (_: Throwable) {
            null
        }
    }

    private fun createDummyOutput(shape: IntArray): Any {
        return when (shape.size) {
            1 -> FloatArray(shape[0])
            2 -> Array(shape[0]) { FloatArray(shape[1]) }
            3 -> Array(shape[0]) { Array(shape[1]) { FloatArray(shape[2]) } }
            4 -> Array(shape[0]) { Array(shape[1]) { Array(shape[2]) { FloatArray(shape[3]) } } }
            else -> FloatArray(1)
        }
    }
}
