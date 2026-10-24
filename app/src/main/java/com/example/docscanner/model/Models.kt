package com.example.docscanner.model

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable

@Serializable
enum class FilterType(val label: String) {
    ORIGINAL("Original"),
    MAGIC_COLOR("Magic Color"),
    CLEAR_BW("Clear B&W"),
    GRAYSCALE("Grayscale"),
    LIGHTEN("Lighten")
}

@Serializable
enum class PaperSize(val label: String, val widthPt: Int, val heightPt: Int) {
    A4("A4 (210 × 297 mm)", 595, 842),
    LETTER("US Letter (8.5 × 11 in)", 612, 792),
    LEGAL("US Legal (8.5 × 14 in)", 612, 1008),
    A3("A3 (297 × 420 mm)", 842, 1191),
    A5("A5 (148 × 210 mm)", 420, 595),
    BUSINESS_CARD("Business Card", 240, 144),
    RECEIPT("Receipt (80 mm roll)", 226, 600)
}

@Serializable
enum class PdfMargin(val label: String, val marginPt: Int) {
    NONE("No Margin", 0),
    COMPACT("Compact (10 pt)", 10),
    STANDARD("Standard (25 pt)", 25),
    WIDE("Wide (40 pt)", 40)
}

@Serializable
data class QuadCorners(
    val topLeftX: Float = 0.05f,
    val topLeftY: Float = 0.05f,
    val topRightX: Float = 0.95f,
    val topRightY: Float = 0.05f,
    val bottomRightX: Float = 0.95f,
    val bottomRightY: Float = 0.95f,
    val bottomLeftX: Float = 0.05f,
    val bottomLeftY: Float = 0.95f
)

@Serializable
data class ScannedPage(
    val id: String,
    val documentId: String,
    val pageIndex: Int,
    val imageUri: String,
    val filterType: FilterType = FilterType.MAGIC_COLOR,
    val rotationDegrees: Float = 0f,
    val corners: QuadCorners = QuadCorners(),
    val ocrText: String = "",
    val watermarkText: String? = null,
    val signatureOverlayUri: String? = null
)

@Serializable
data class Document(
    val id: String,
    val title: String,
    val createdDate: Long,
    val modifiedDate: Long,
    val folderId: String? = null,
    val isTrash: Boolean = false,
    val pages: List<ScannedPage> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: String = ""
)

@Serializable
data class Folder(
    val id: String,
    val name: String,
    val colorHex: Long = 0xFF6750A4,
    val createdDate: Long = System.currentTimeMillis()
)

@Serializable
enum class DocumentSortOption(val label: String) {
    MODIFIED_NEWEST("Recently Modified"),
    MODIFIED_OLDEST("Oldest Modified"),
    CREATED_NEWEST("Recently Created"),
    TITLE_AZ("Name (A to Z)"),
    TITLE_ZA("Name (Z to A)")
}

data class PdfExportConfig(
    val paperSize: PaperSize = PaperSize.A4,
    val margins: PdfMargin = PdfMargin.STANDARD,
    val qualityPercent: Int = 90,
    val password: String? = null,
    val includePageNumbers: Boolean = true
)
