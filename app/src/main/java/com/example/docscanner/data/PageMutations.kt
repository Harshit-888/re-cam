package com.example.docscanner.data

import com.example.docscanner.model.Document
import com.example.docscanner.model.ScannedPage

/**
 * The pure state transforms behind the repository's bulk page verbs.
 *
 * Split out for one reason: `ScannerRepository` needs a `Context` and a
 * `LibraryStore` to construct, so its state rules cannot be reached from a JVM
 * test. The rules that decide whether a document survives are exactly the ones
 * that shipped a bug once (an emptied document remaining as a card with no cover,
 * no cover and no way to export). They are worth a test that runs on every build.
 *
 * Both functions are **total and pure**: no I/O, no clock, no mutation of the
 * input. The caller applies `modifiedDate` and the CAS.
 */
internal object PageMutations {

    /**
     * Appends [pages] to the document identified by [docId], renumbering
     * `pageIndex` across the whole list.
     *
     * `pageIndex` is recomputed with `mapIndexed` rather than derived from
     * `doc.pages.size`, because a library edited by older versions can hold
     * duplicate or non-contiguous indices. The list order is the truth; the index
     * is a cache of it.
     *
     * Returns the updated [Document], or `null` when no document has that id —
     * which is the signal a caller must not report success.
     */
    fun appendTo(doc: Document, docId: String, pages: List<ScannedPage>): Document? {
        if (doc.id != docId) return null
        if (pages.isEmpty()) return doc
        val appended = doc.pages + pages.map { it.copy(documentId = docId) }
        return doc.copy(pages = appended.mapIndexed { index, page -> page.copy(pageIndex = index) })
    }

    /**
     * Removes [pageIds] from the document, renumbering the survivors, and reports
     * the pages that were actually removed so their pixels can be deleted only
     * after the state change has been published.
     *
     * ## The rule that must survive
     *
     * A document is its pages. Deleting the last page deletes the document. This
     * is not a detail: an emptied document used to survive as a card in the
     * library with no cover, no valid PDF export, and no way to remove it except
     * by trashing it by hand. [removedAll] is the signal.
     *
     * ## Why the result carries the pages
     *
     * The previous single-page implementation captured the page in a `var` written
     * inside the `update {}` lambda. When the CAS loses, that lambda runs again
     * with a fresh state, so the value that survives is the one from the attempt
     * that won — or, if a later attempt removes nothing, the stale one.
     * `AtomicStateUpdateTest` exists because of exactly that hazard. Returning the
     * removed pages as part of the transform's result means the value belongs to
     * the attempt that was published.
     */
    data class Removal(
        /** The document after the removal, or `null` when it had no pages left. */
        val document: Document?,
        /** Pages that existed and were removed. Empty when [pageIds] matched nothing. */
        val removed: List<ScannedPage>
    ) {
        /** True when this removal emptied the document, so the record goes too. */
        val removedAll: Boolean get() = document == null
    }

    fun removeFrom(doc: Document, docId: String, pageIds: Set<String>): Removal? {
        if (doc.id != docId) return null
        val removed = doc.pages.filter { it.id in pageIds }
        if (removed.isEmpty()) return Removal(document = doc, removed = emptyList())
        val remaining = doc.pages
            .filterNot { it.id in pageIds }
            .mapIndexed { index, page -> page.copy(pageIndex = index) }
        return Removal(
            document = remaining.takeIf { it.isNotEmpty() }?.let { doc.copy(pages = it) },
            removed = removed
        )
    }
}
