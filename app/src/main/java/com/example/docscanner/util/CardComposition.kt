package com.example.docscanner.util

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Where the two sides of an ID card go on one page.
 *
 * The original app's "ID Card" capture mode takes a front and a back and lays them on
 * a single sheet; this is the arithmetic half of that, with no Android imports so it
 * is unit-tested on the host. The drawing half lives in
 * [com.example.docscanner.data.ScannerRepository.createIdCardDocument].
 *
 * ## The rules, and why each one exists
 *
 * * **Split along the page's longer side.** A4 portrait splits left/right, so each
 *   card gets a half-page cell that is taller than it is wide — and a 85.6 × 54 mm
 *   card, which is landscape, fits across the cell's width. Splitting a portrait page
 *   top/bottom instead would give two short wide cells and two tiny cards.
 * * **Aspect ratio is preserved.** Squashing a card to fill a cell is the one failure
 *   nobody notices until they print it and the photo no longer matches the face.
 * * **Both cells are the same size**, so two scans of the same card come out at the
 *   same size even if one was cropped a pixel or two differently.
 * * **Nothing leaves the printable area**, and the two cards never overlap: the gap is
 *   a parameter, not a coincidence of rounding.
 *
 * All values are pixels of whatever page bitmap the caller is drawing into.
 */
object CardComposition {

    /** Which way the page is divided. */
    enum class Axis {
        /** Front on the left, back on the right. */
        SIDE_BY_SIDE,

        /** Front on top, back underneath. */
        STACKED
    }

    data class Size(val width: Int, val height: Int) {
        init {
            require(width > 0 && height > 0) { "size must be positive: ${width}x$height" }
        }
    }

    /** A rectangle in page pixels. Half-open: `right` and `bottom` are exclusive. */
    data class Box(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        val width: Int get() = right - left
        val height: Int get() = bottom - top

        fun overlaps(other: Box): Boolean =
            left < other.right && other.left < right && top < other.bottom && other.top < bottom
    }

    data class Placement(val front: Box, val back: Box, val axis: Axis)

    /** 5 % of the page's short side. A print margin, not a design statement. */
    const val MARGIN_FRACTION = 0.05f

    /** 4 % of the page's short side, between the two cards. */
    const val GAP_FRACTION = 0.04f

    /**
     * A portrait page is divided left/right and a landscape one top/bottom, so the two
     * cells always share the page's long dimension and the cards sit across their
     * width — the way a card actually lies on a sheet.
     */
    fun defaultAxis(page: Size): Axis =
        if (page.height >= page.width) Axis.SIDE_BY_SIDE else Axis.STACKED

    /**
     * Positions [front] and [back] on a [page], or `null` when the page is too small
     * to hold them with the requested margin and gap.
     *
     * The margin and gap default to [MARGIN_FRACTION] and [GAP_FRACTION] of the page's
     * short side; pass them explicitly to pin them for a test.
     */
    fun placement(
        page: Size,
        front: Size,
        back: Size,
        axis: Axis = defaultAxis(page),
        marginPx: Int = defaultMargin(page),
        gapPx: Int = defaultGap(page)
    ): Placement? {
        val usable = Box(
            left = marginPx,
            top = marginPx,
            right = page.width - marginPx,
            bottom = page.height - marginPx
        )
        if (usable.width <= 0 || usable.height <= 0) return null
        if (gapPx < 0) return null

        val firstCell: Box
        val secondCell: Box
        when (axis) {
            Axis.SIDE_BY_SIDE -> {
                val cellWidth = (usable.width - gapPx) / 2
                if (cellWidth <= 0) return null
                firstCell = Box(usable.left, usable.top, usable.left + cellWidth, usable.bottom)
                secondCell = Box(usable.right - cellWidth, usable.top, usable.right, usable.bottom)
            }
            Axis.STACKED -> {
                val cellHeight = (usable.height - gapPx) / 2
                if (cellHeight <= 0) return null
                firstCell = Box(usable.left, usable.top, usable.right, usable.top + cellHeight)
                secondCell = Box(usable.left, usable.bottom - cellHeight, usable.right, usable.bottom)
            }
        }

        val frontBox = fit(front, firstCell)
        val backBox = fit(back, secondCell)
        return Placement(frontBox, backBox, axis)
    }

    fun defaultMargin(page: Size): Int = (min(page.width, page.height) * MARGIN_FRACTION).roundToInt()

    fun defaultGap(page: Size): Int = (min(page.width, page.height) * GAP_FRACTION).roundToInt()

    /**
     * Largest copy of [content] that fits [cell], centred.
     *
     * Rounded independently, then clamped: rounding width and height up can produce a
     * box one pixel wider than the cell it was fitted into, and one pixel is enough to
     * paint over the margin on a small page.
     */
    private fun fit(content: Size, cell: Box): Box {
        val scale = min(
            cell.width.toDouble() / content.width.toDouble(),
            cell.height.toDouble() / content.height.toDouble()
        )
        val width = max(1, (content.width * scale).roundToInt()).coerceAtMost(cell.width)
        val height = max(1, (content.height * scale).roundToInt()).coerceAtMost(cell.height)
        val left = cell.left + (cell.width - width) / 2
        val top = cell.top + (cell.height - height) / 2
        return Box(left, top, left + width, top + height)
    }
}
