package com.example.docscanner.util

import com.example.docscanner.model.ScannedPage
import java.io.File

internal data class PageCopySources(val current: File, val pristine: File, val isEdited: Boolean)

/** Resolve only the referenced pixels; a similarly named legacy bake is not a substitute. */
internal fun resolvePageCopySources(
    page: ScannedPage,
    resolve: (String) -> File?
): PageCopySources? {
    val current = resolve(page.imageUri) ?: return null
    val originalPath = page.originalImageUri
    val pristine = if (originalPath != null) {
        resolve(originalPath) ?: return null
    } else {
        // A legacy edited path without a pristine reference is not a safe original.
        if (page.imageUri.contains("_edited")) return null
        current
    }
    return PageCopySources(current, pristine, originalPath != null && current.absoluteFile != pristine.absoluteFile)
}
