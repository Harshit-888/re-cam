package com.example.docscanner

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNav : NavKey

@Serializable
data class ScannerCameraNav(val targetDocId: String? = null) : NavKey

/**
 * Opens the perspective/filter editor.
 *
 * @param review opens in post-capture review mode: borders are auto-detected and
 *   *Save & Next* walks the rest of the document before landing on its detail
 *   screen. This is the step the original app showed right after scanning.
 */
@Serializable
data class CropAndFilterNav(
    val docId: String,
    val pageId: String,
    val review: Boolean = false,
    /** Open on the border stage — what the Crop button means. */
    val borders: Boolean = false
) : NavKey

/**
 * Handwriting on one page.
 *
 * Its own route rather than a dialog: annotating is the most precise gesture in the
 * app and it deserves the whole display (see `MarkupScreen`).
 */
@Serializable
data class MarkupNav(val docId: String, val pageId: String) : NavKey

@Serializable
data class DocumentDetailNav(val docId: String) : NavKey

@Serializable
data class PageViewerNav(val docId: String, val pageId: String) : NavKey

@Serializable
data object TrashNav : NavKey

/** Preferences. Reachable from the dashboard's settings icon. */
@Serializable
data object SettingsNav : NavKey
