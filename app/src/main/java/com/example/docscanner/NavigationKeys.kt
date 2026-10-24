package com.example.docscanner

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNav : NavKey

@Serializable
data class ScannerCameraNav(val targetDocId: String? = null) : NavKey

@Serializable
data class CropAndFilterNav(val docId: String, val pageId: String) : NavKey

@Serializable
data class DocumentDetailNav(val docId: String) : NavKey

@Serializable
data class PageViewerNav(val docId: String, val pageId: String) : NavKey

@Serializable
data object TrashNav : NavKey
