package com.example.docscanner.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

/**
 * Hand annotations drawn on a page: pen, highlighter, and whiteout.
 *
 * Stored on the page rather than flattened into the image, for the same reason the
 * crop corners are: [com.example.docscanner.util.PageRenderer] rebuilds a page from its
 * **pristine capture** every time, so anything painted into the bake would vanish the
 * next time a filter changed. Keeping the strokes here means annotation is
 * non-destructive too — change the filter afterwards and the ink is still there.
 *
 * Every coordinate is **normalised 0…1** against the page, so a stroke made on a
 * 400-px preview lands in the same place on a 3200-px bake.
 */
@Serializable
enum class MarkupTool(val label: String) {
    /** Opaque ink, constant width. */
    PEN("Pen"),

    /** Wide, translucent, multiplied — a highlighter, not a fat pen. */
    HIGHLIGHTER("Marker"),

    /** Paints the paper white: shadows, staples, a fingerprint. */
    WHITEOUT("Erase")
}

@Immutable
@Serializable
data class MarkupPoint(val x: Float, val y: Float)

@Immutable
@Serializable
data class MarkupStroke(
    val tool: MarkupTool,
    /** ARGB. Ignored by [MarkupTool.WHITEOUT], which paints the page's paper colour. */
    val colorArgb: Int = 0xFF000000.toInt(),
    /**
     * Stroke width as a fraction of the page's **shorter side**, so the same stroke
     * looks the same on a portrait page, a landscape page and a phone preview.
     */
    val widthFraction: Float = 0.006f,
    val points: List<MarkupPoint> = emptyList()
) {
    init {
        require(widthFraction > 0f) { "widthFraction must be positive" }
    }

    /** Width in pixels for a page of the given size. */
    fun widthPx(pageWidth: Int, pageHeight: Int): Float =
        max(1f, min(pageWidth, pageHeight) * widthFraction)

    /** True when the stroke has enough points to be visible at all. */
    val isDrawable: Boolean get() = points.size >= 2
}

/**
 * Undo/redo over a list of strokes.
 *
 * A plain undo stack would be enough for a paint app; the two extra behaviours are
 * there because of how people actually annotate:
 *
 * * **Redo survives an undo**, so "undo, undo, redo once" is the fix for an
 *   over-applied whiteout, rather than clearing the page and starting again. Adding a
 *   new stroke after undoing discards the redo branch — the usual editor rule, and the
 *   one that keeps the button honest.
 * * **Clear is a single undoable step.** It is the easiest destructive action in the
 *   dialog to hit by accident, so it restores the whole page in one Undo instead of
 *   being irreversible. Clearing, then drawing, forgets the cleared strokes: they were
 *   not restored by Undo, and pretending otherwise would let a stale redo reappear
 *   behind new work.
 */
class MarkupHistory(initial: List<MarkupStroke> = emptyList()) {

    private val done = ArrayList<MarkupStroke>(initial)

    /** Strokes undone since the last edit, newest last. */
    private val redoBranch = ArrayList<MarkupStroke>()

    /** Strokes dropped by [clear], restorable by one [undo]. `null` when none. */
    private var cleared: List<MarkupStroke>? = null

    val strokes: List<MarkupStroke> get() = done.toList()

    val canUndo: Boolean get() = done.isNotEmpty() || cleared != null
    val canRedo: Boolean get() = redoBranch.isNotEmpty()
    val isEmpty: Boolean get() = done.isEmpty()

    /** Number of strokes currently applied, cheap to read. */
    val size: Int get() = done.size

    fun add(stroke: MarkupStroke) {
        // A tap with no drag is not an annotation; storing it would make Undo appear
        // to do nothing.
        if (!stroke.isDrawable) return
        // New work invalidates a pending restore: the page is no longer the one that
        // was cleared.
        cleared = null
        done.add(stroke)
        redoBranch.clear()
    }

    /**
     * @return the stroke that was removed, or the last stroke restored when undoing a
     *   [clear]; `null` when there was nothing to undo.
     */
    fun undo(): MarkupStroke? {
        cleared?.let { restored ->
            cleared = null
            done.addAll(restored)
            redoBranch.clear()
            return restored.lastOrNull()
        }
        if (done.isEmpty()) return null
        val removed = done.removeAt(done.lastIndex)
        redoBranch.add(removed)
        return removed
    }

    /** @return the stroke that was restored, or `null` when there was nothing to redo. */
    fun redo(): MarkupStroke? {
        if (redoBranch.isEmpty()) return null
        val restored = redoBranch.removeAt(redoBranch.lastIndex)
        done.add(restored)
        return restored
    }

    /** Removes every stroke. One [undo] brings them all back. */
    fun clear() {
        if (done.isEmpty()) return
        cleared = done.toList()
        redoBranch.clear()
        done.clear()
    }
}
