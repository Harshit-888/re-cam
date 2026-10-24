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
    val review: Boolean = false
) : NavKey

@Serializable
data class DocumentDetailNav(val docId: String) : NavKey

@Serializable
data class PageViewerNav(val docId: String, val pageId: String) : NavKey

@Serializable
data object TrashNav : NavKey
