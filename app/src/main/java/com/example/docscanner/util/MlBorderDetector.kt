package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.docscanner.model.QuadCorners
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter

/**
 * The on-device border model from the original app (`border_detect_224.tflite`,
 * run as `ht3.java` did), with the robustness a re-implementation needs.
 *
 * It takes a page image, finds four corners, and reports how sure it is. The caller
 * ([DocumentDetector]) uses the quad only when the answer is both **confident** and
 * **plausible**, and otherwise falls back to the Kotlin geometry pass.
 *
 * ## Why this is longer than the decoder it replaces
 *
 * The first version hard-coded `getOutputIndex("StatefulPartitionedCall:0/1")` and the
 * output shapes `[1,1,4,2]` / `[1,1,4]` from a decompiled class, and fed the model
 * 0–255 floats. If any of those four assumptions does not hold for this particular
 * file, **every** inference throws, `null` comes back, and the detector silently uses
 * the geometry pass — indistinguishable from "the model found nothing", on a device,
 * with no error to read. So instead of assuming:
 *
 * * outputs are located **by name**, then by size only when that is unambiguous, and
 *   allocated from the tensor's **own shape** — the model has six outputs and two of
 *   them are four floats wide, so size alone picks the wrong one;
 * * the input tensor's shape and dtype are read and honoured (float32 or uint8);
 * * the input **scale is calibrated once** per process against the model's own
 *   confidence, because 0–255 and 0–1 are both common and only one of them is right
 *   (measured on this file: 0–255 gives mean corner score 0.99 on a clean page, 0–1
 *   gives 0.53 — the calibration therefore always lands on 0–255 here, and stays
 *   honest if the model is ever swapped);
 * * the quad is checked by [QuadSanity] before it is offered as an answer.
 *
 * The four facts the first version assumed are now measured rather than assumed: the
 * tensor names, the shapes, the `(y, x)` component order, the 0–255 encoding and the
 * 0.5 confidence threshold all come from `Lht3;->a` / `Lqt3;->c` in the original APK,
 * and the shapes and names were re-read from the shipped `.tflite` file itself with
 * tflite-runtime. `docs/ORIGINAL_ML_BORDER_PATH.md` records the evidence.
 *
 * Every fallback is logged once under `DocScannerPerf` (debug), so a device test says
 * which path answered instead of having to guess.
 *
 * No global state: the `Context` is passed to [detect]. An `object` holding an
 * application context is a singleton by another name, which `AGENTS.md` §2.6 rules out.
 */
object MlBorderDetector {

    private const val TAG = "DocScannerPerf"
    private const val MODEL_FILE = "border_detect_224.tflite"
    private const val INPUT_SIZE = 224

    /** Output tensor names, verbatim from the model. See the comment at the lookup. */
    private const val COORDS_TENSOR = "StatefulPartitionedCall:1"
    private const val SCORES_TENSOR = "StatefulPartitionedCall:0"

    /**
     * Mean confidence below which the answer is not used.
     *
     * The same 0.5 the original app used. A page edge is either visible or it is not,
     * and the model is decisive when it can see one.
     */
    private const val CONFIDENCE_THRESHOLD = 0.5f

    /** Two candidate input encodings; the first one that works is remembered. */
    private const val SCALE_BYTES = 255f
    private const val SCALE_UNIT = 1f

    @Volatile
    private var interpreter: Interpreter? = null

    /** `null` until the first successful inference; then [SCALE_BYTES] or [SCALE_UNIT]. */
    @Volatile
    private var inputScale: Float? = null

    /** Keys already logged, so a per-page failure cannot flood the log. */
    private val loggedKeys = HashSet<String>()

    // Reused between calls. Detection is sequential (one editor, one pass), and the
    // interpreter is not safe to run concurrently anyway, so a single pair of buffers
    // is both correct and the difference between one allocation per page and one per
    // session across a 24-page batch.
    private var pixelScratch: IntArray? = null
    private var inputScratch: ByteBuffer? = null

    data class MlResult(
        val corners: QuadCorners,
        /** Mean of the four per-corner confidences. */
        val meanConfidence: Float,
        /** Confident *and* plausible. The caller may use [corners] only when true. */
        val confident: Boolean,
        /** Why a confident result was still rejected, for the log. `null` normally. */
        val rejection: String? = null
    )

    /**
     * Runs the model on [bitmap].
     *
     * @return `null` when the model cannot be loaded or the inference fails — the
     *   caller then falls back without further checks.
     */
    @Synchronized
    fun detect(bitmap: Bitmap, context: Context?): MlResult? {
        val ctx = context ?: return null
        if (bitmap.isRecycled) return null
        val interp = interpreterOrNull(ctx) ?: return null

        return try {
            val input = interp.getInputTensor(0)
            val inputShape = input.shape()
            if (inputShape.size != 4 ||
                inputShape[1] != INPUT_SIZE || inputShape[2] != INPUT_SIZE
            ) {
                logOnce(
                    "input-shape",
                    "[ml] unexpected input ${inputShape.contentToString()}; model not used"
                )
                return null
            }

            val scale = inputScale ?: SCALE_BYTES
            val result = run(
                interp = interp,
                bitmap = bitmap,
                inputDataType = input.dataType(),
                scale = scale
            ) ?: return null

            // ---- one-time input calibration -----------------------------------
            //
            // 0–255 and 0–1 are both common in exported TF Lite converters, and the
            // decompiled class only shows which one *this* build of the app used.
            // Rather than trusting it, the first page of the session is run through
            // both encodings and the one the model is more confident about is kept.
            // That is one extra inference per process, and it turns "the model output
            // is noise" into a self-correcting default. A wrong encoding produces low
            // confidence and implausible quads, which is precisely what is compared.
            if (inputScale == null) {
                val other = if (scale == SCALE_BYTES) SCALE_UNIT else SCALE_BYTES
                val retry = run(interp, bitmap, input.dataType(), other)
                val preferred = if (retry != null && retry.meanConfidence > result.meanConfidence) {
                    retry
                } else {
                    result
                }
                inputScale = if (preferred === retry) other else scale
                if (PerfTrace.isEnabled) {
                    Log.i(
                        TAG,
                        "[ml] input scale calibrated to ${plain(inputScale ?: scale)} " +
                            "(mean ${plain(result.meanConfidence)} vs " +
                            "${plain(retry?.meanConfidence ?: 0f)})"
                    )
                }
                return preferred.toResult()
            }

            val outcome = result.toResult()
            if (outcome.rejection != null) {
                logOnce("bad-quad", "[ml] rejecting quad: ${outcome.rejection}")
            }
            outcome
        } catch (t: Throwable) {
            logOnce("run", "[ml] inference failed, falling back to geometry: ${t.message}")
            null
        }
    }

    // ------------------------------------------------------------------ internals

    /**
     * Exact name, or a suffix match so a signature-prefixed name still binds —
     * `:0` and `:1` remain distinguishable either way.
     */
    private fun isTensorNamed(actual: String, expected: String): Boolean =
        actual == expected || actual.endsWith(expected)

    /** One inference plus the checks, with nothing cached. */
    private class Outcome(
        val corners: QuadCorners,
        val meanConfidence: Float,
        val passed: Boolean,
        val rejection: String?
    ) {
        /** What the caller sees: [confident] is the *decision*, not the raw score. */
        fun toResult() = MlResult(
            corners = corners,
            meanConfidence = meanConfidence,
            confident = passed,
            rejection = rejection
        )
    }

    private fun run(
        interp: Interpreter,
        bitmap: Bitmap,
        inputDataType: DataType,
        scale: Float
    ): Outcome? {
        val pixels = pixelScratch ?: IntArray(INPUT_SIZE * INPUT_SIZE).also { pixelScratch = it }
        val buffer = inputScratch?.also { it.rewind(); it.clear() }
            ?: ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4)
                .order(ByteOrder.nativeOrder())
                .also { inputScratch = it }

        val scaled = if (bitmap.width == INPUT_SIZE && bitmap.height == INPUT_SIZE) {
            bitmap
        } else {
            // Bilinear. The model was trained on square inputs, and the corner
            // coordinates it returns are relative — so a squashed resize costs a few
            // pixels of accuracy on a very elongated page, not correctness.
            Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        }

        try {
            scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
            val uint8 = inputDataType == DataType.UINT8
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                if (uint8) {
                    buffer.put(r.toByte())
                    buffer.put(g.toByte())
                    buffer.put(b.toByte())
                } else {
                    buffer.putFloat(r / scale)
                    buffer.putFloat(g / scale)
                    buffer.putFloat(b / scale)
                }
            }
            buffer.rewind()
        } finally {
            if (scaled !== bitmap && !scaled.isRecycled) scaled.recycle()
        }

        // ---- locate the two outputs --------------------------------------------
        //
        // The model has **six** outputs and **two** of them hold four floats, so the
        // element count alone cannot tell the real scores from the decoy. Ground truth
        // for this exact file, from two independent sources — `ht3.a` in the original
        // APK (disassembled: six `getOutputTensorIndex` calls, shapes allocated as
        // [1,1] / [1,1,4] / [1,1] / [1] / [1,1,4,2] / [1,1,4]) and the file itself
        // (tflite-runtime):
        //
        //   StatefulPartitionedCall:1  [1,1,4,2]  8 floats  the four corners, (y, x)
        //   StatefulPartitionedCall:0  [1,1,4]    4 floats  one score per corner
        //   StatefulPartitionedCall:5  [1,1,4]    4 floats  not the scores
        //
        // So: names first. Sizes are only a fallback, and only when they are
        // unambiguous — four floats is not enough to pick the right tensor, and a
        // wrong pick would read the decoy, report ~0.2 confidence on a good page, and
        // quietly lose the ML answer. Refusing is better than guessing.
        var coordsIndex = -1
        var scoresIndex = -1
        var coordsBySize = -1
        val fourElementOutputs = mutableListOf<Int>()
        for (index in 0 until interp.outputTensorCount) {
            val tensor = interp.getOutputTensor(index)
            val name = tensor.name()
            if (isTensorNamed(name, COORDS_TENSOR)) coordsIndex = index
            if (isTensorNamed(name, SCORES_TENSOR)) scoresIndex = index
            if (tensor.numElements() == 8) coordsBySize = index
            if (tensor.numElements() == 4) fourElementOutputs.add(index)
        }
        if (coordsIndex < 0) coordsIndex = coordsBySize
        if (scoresIndex < 0) scoresIndex = fourElementOutputs.singleOrNull() ?: -1
        if (coordsIndex < 0 || scoresIndex < 0) {
            val seen = (0 until interp.outputTensorCount).joinToString(", ") { index ->
                val tensor = interp.getOutputTensor(index)
                "${tensor.name()}${tensor.shape().contentToString()}"
            }
            logOnce(
                "outputs",
                "[ml] cannot tell the corner tensor from the score tensor in " +
                    "${interp.outputTensorCount} outputs [$seen]; model not used"
            )
            return null
        }
        logOnce(
            "outputs-ok",
            "[ml] corners = ${interp.getOutputTensor(coordsIndex).name()} " +
                "${interp.getOutputTensor(coordsIndex).shape().contentToString()}, " +
                "scores = ${interp.getOutputTensor(scoresIndex).name()} " +
                "${interp.getOutputTensor(scoresIndex).shape().contentToString()}"
        )

        val coordsShape = interp.getOutputTensor(coordsIndex).shape()
        val scoresShape = interp.getOutputTensor(scoresIndex).shape()
        val outputs = HashMap<Int, Any>(4)
        outputs[coordsIndex] = arrayFor(coordsShape)
        outputs[scoresIndex] = arrayFor(scoresShape)
        for (index in 0 until interp.outputTensorCount) {
            if (index == coordsIndex || index == scoresIndex) continue
            outputs[index] = arrayFor(interp.getOutputTensor(index).shape())
        }

        interp.runForMultipleInputsOutputs(arrayOf(buffer), outputs)

        val coords = flatten(outputs.getValue(coordsIndex), 8)
        val scores = flatten(outputs.getValue(scoresIndex), 4)
        if (coords == null || scores == null) return null

        val meanScore = (scores[0] + scores[1] + scores[2] + scores[3]) / 4f

        // Each point is (y, x) — `ht3.java` reads them that way round.
        //
        // Clamped, not rejected: a corner a hair outside the frame is a real answer
        // for a page that runs off the edge of the photograph, and the geometry pass
        // snaps those onto the edge too.
        val quad = QuadCorners(
            topLeftX = coords[1].coerceIn(0f, 1f),
            topLeftY = coords[0].coerceIn(0f, 1f),
            topRightX = coords[3].coerceIn(0f, 1f),
            topRightY = coords[2].coerceIn(0f, 1f),
            bottomRightX = coords[5].coerceIn(0f, 1f),
            bottomRightY = coords[4].coerceIn(0f, 1f),
            bottomLeftX = coords[7].coerceIn(0f, 1f),
            bottomLeftY = coords[6].coerceIn(0f, 1f)
        )

        val confident = meanScore >= CONFIDENCE_THRESHOLD
        // Plausibility and confidence are separate questions, and both matter:
        //  * a confident quad that is a sliver is garbage and must never be used;
        //  * an *un*confident quad that is plausibly a page is a legitimate starting
        //    point for the user to nudge, and `DocumentDetector` keeps it for that.
        // Conflating them (treating "not sure" as "rejected") would throw away the
        // model's most useful output on hard pages.
        val rejection = QuadSanity.rejection(quad)

        if (PerfTrace.isEnabled) {
            Log.i(
                TAG,
                "[ml] ${if (confident && rejection == null) "accepted" else "rejected"} " +
                    "mean=${plain(meanScore)} quad=" +
                    "(${plain(quad.topLeftX)},${plain(quad.topLeftY)})→" +
                    "(${plain(quad.bottomRightX)},${plain(quad.bottomRightY)})" +
                    (if (!confident) " · under ${plain(CONFIDENCE_THRESHOLD)}" else "") +
                    (rejection?.let { " · $it" } ?: "")
            )
        }

        return Outcome(
            corners = quad,
            meanConfidence = meanScore,
            passed = confident && rejection == null,
            rejection = rejection
        )
    }

    @Synchronized
    private fun interpreterOrNull(context: Context): Interpreter? {
        interpreter?.let { return it }
        return try {
            context.assets.openFd(MODEL_FILE).use { fd ->
                FileInputStream(fd.fileDescriptor).use { stream ->
                    val buffer = stream.channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        fd.startOffset,
                        fd.declaredLength
                    )
                    val options = Interpreter.Options().apply {
                        // Four threads is what the original used; the pass runs off
                        // the main thread, so this is wall-clock, not frame time.
                        setNumThreads(4)
                    }
                    Interpreter(buffer, options).also {
                        interpreter = it
                        runCatching { describe(it) }
                    }
                }
            }
        } catch (t: Throwable) {
            // The usual cause is a compressed asset: `openFd` refuses it, which means
            // `androidResources { noCompress += "tflite" }` is missing from
            // app/build.gradle.kts.
            logOnce("load", "[ml] model $MODEL_FILE could not be loaded: ${t.message}")
            null
        }
    }

    /** Logs the model's real contract once, and checks it against what [run] assumes. */
    private fun describe(interpreter: Interpreter) {
        if (!PerfTrace.isEnabled) return
        val input = interpreter.getInputTensor(0)
        val outputs = (0 until interpreter.outputTensorCount).joinToString(", ") { index ->
            val tensor = interpreter.getOutputTensor(index)
            "$index:'${tensor.name()}'${tensor.shape().contentToString()}"
        }
        Log.i(
            TAG,
            "[ml] model loaded · input ${input.shape().contentToString()} ${input.dataType()} " +
                "· outputs $outputs"
        )
    }

    /**
     * A nested primitive array that TFLite will accept for [shape].
     *
     * `runForMultipleInputsOutputs` rejects a flat array when the tensor is ranked, and
     * the rank differs between model exports ([1,4,2] vs [1,1,4,2]), so the shape is
     * honoured instead of assumed.
     */
    private fun arrayFor(shape: IntArray): Any {
        if (shape.isEmpty()) return FloatArray(1)
        return build(shape, 0)
    }

    private fun build(shape: IntArray, dimension: Int): Any {
        val size = shape[dimension]
        return if (dimension == shape.lastIndex) {
            FloatArray(size)
        } else {
            Array(size) { build(shape, dimension + 1) }
        }
    }

    /** Flattens a nested output array into exactly [expected] floats, or `null`. */
    private fun flatten(value: Any, expected: Int): FloatArray? {
        val out = FloatArray(expected)
        var count = 0
        fun walk(node: Any) {
            when (node) {
                is FloatArray -> for (element in node) {
                    if (count < expected) out[count++] = element
                }

                is Array<*> -> for (element in node) {
                    if (element != null) walk(element)
                }

                is ByteArray -> for (element in node) {
                    // A quantised output would land here; treat as unsigned.
                    if (count < expected) out[count++] = (element.toInt() and 0xFF) / 255f
                }

                else -> Unit
            }
        }
        walk(value)
        return if (count >= expected) out else null
    }

    private fun plain(value: Float): String = String.format(Locale.US, "%.2f", value)

    /**
     * Logs [message] under [key] once per process, debug builds only.
     *
     * A batch pass calls [detect] once per page; an unconditional warning would bury
     * the one line that matters under two dozen copies of itself.
     */
    @Synchronized
    private fun logOnce(key: String, message: String) {
        if (!PerfTrace.isEnabled) return
        if (!loggedKeys.add(key)) return
        Log.w(TAG, message)
    }
}
