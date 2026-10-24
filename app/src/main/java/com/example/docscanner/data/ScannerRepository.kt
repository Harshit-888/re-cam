package com.example.docscanner.data

import com.example.docscanner.model.Document
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.Folder
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.model.ScannedPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class ScannerRepository {

    private val _folders = MutableStateFlow<List<Folder>>(
        listOf(
            Folder(id = "folder_work", name = "Work & Contracts", colorHex = 0xFF3F51B5),
            Folder(id = "folder_tax", name = "Tax & Financials", colorHex = 0xFF4CAF50),
            Folder(id = "folder_personal", name = "Personal & ID", colorHex = 0xFFFF9800)
        )
    )
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _documents = MutableStateFlow<List<Document>>(
        createInitialMockDocuments()
    )
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    fun getDocumentById(id: String): Document? {
        return _documents.value.find { it.id == id }
    }

    fun addDocument(title: String, folderId: String? = null, pages: List<ScannedPage>): Document {
        val now = System.currentTimeMillis()
        val docId = UUID.randomUUID().toString()
        val formattedPages = pages.mapIndexed { index, p ->
            p.copy(id = UUID.randomUUID().toString(), documentId = docId, pageIndex = index)
        }
        val newDoc = Document(
            id = docId,
            title = title.ifBlank { "Scanned Document ${now % 10000}" },
            createdDate = now,
            modifiedDate = now,
            folderId = folderId,
            pages = formattedPages
        )
        _documents.update { listOf(newDoc) + it }
        return newDoc
    }

    fun updateDocumentTitle(id: String, newTitle: String) {
        _documents.update { docs ->
            docs.map { if (it.id == id) it.copy(title = newTitle, modifiedDate = System.currentTimeMillis()) else it }
        }
    }

    fun moveDocumentToFolder(docId: String, folderId: String?) {
        _documents.update { docs ->
            docs.map { if (it.id == docId) it.copy(folderId = folderId, modifiedDate = System.currentTimeMillis()) else it }
        }
    }

    fun updatePage(docId: String, pageId: String, update: (ScannedPage) -> ScannedPage) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id == docId) {
                    val updatedPages = doc.pages.map { page ->
                        if (page.id == pageId) update(page) else page
                    }
                    doc.copy(pages = updatedPages, modifiedDate = System.currentTimeMillis())
                } else {
                    doc
                }
            }
        }
    }

    fun addPageToDocument(docId: String, page: ScannedPage) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id == docId) {
                    val newPages = doc.pages + page.copy(
                        id = UUID.randomUUID().toString(),
                        documentId = docId,
                        pageIndex = doc.pages.size
                    )
                    doc.copy(pages = newPages, modifiedDate = System.currentTimeMillis())
                } else {
                    doc
                }
            }
        }
    }

    fun deletePage(docId: String, pageId: String) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id == docId) {
                    val remaining = doc.pages.filterNot { it.id == pageId }
                        .mapIndexed { index, p -> p.copy(pageIndex = index) }
                    doc.copy(pages = remaining, modifiedDate = System.currentTimeMillis())
                } else {
                    doc
                }
            }
        }
    }

    fun moveToTrash(docId: String) {
        _documents.update { docs ->
            docs.map { if (it.id == docId) it.copy(isTrash = true, modifiedDate = System.currentTimeMillis()) else it }
        }
    }

    fun restoreFromTrash(docId: String) {
        _documents.update { docs ->
            docs.map { if (it.id == docId) it.copy(isTrash = false, modifiedDate = System.currentTimeMillis()) else it }
        }
    }

    fun deletePermanently(docId: String) {
        _documents.update { docs -> docs.filterNot { it.id == docId } }
    }

    fun emptyTrash() {
        _documents.update { docs -> docs.filterNot { it.isTrash } }
    }

    fun addFolder(name: String, colorHex: Long) {
        val newFolder = Folder(id = UUID.randomUUID().toString(), name = name, colorHex = colorHex)
        _folders.update { it + newFolder }
    }

    companion object {
        val instance by lazy { ScannerRepository() }

        private fun createInitialMockDocuments(): List<Document> {
            val now = System.currentTimeMillis()
            return listOf(
                Document(
                    id = "doc_contract_01",
                    title = "Independent Contractor Agreement",
                    createdDate = now - 86400000 * 2,
                    modifiedDate = now - 3600000 * 5,
                    folderId = "folder_work",
                    tags = listOf("Legal", "Signed", "Urgent"),
                    notes = "Signed agreement with client for mobile app architecture.",
                    pages = listOf(
                        ScannedPage(
                            id = "p1",
                            documentId = "doc_contract_01",
                            pageIndex = 0,
                            imageUri = "mock_contract_p1",
                            filterType = FilterType.MAGIC_COLOR,
                            rotationDegrees = 0f,
                            ocrText = "INDEPENDENT CONTRACTOR AGREEMENT\nBetween Indy Mobile Ltd and Client\nEffective Date: October 2026\nTerms: Confidential and Binding."
                        ),
                        ScannedPage(
                            id = "p2",
                            documentId = "doc_contract_01",
                            pageIndex = 1,
                            imageUri = "mock_contract_p2",
                            filterType = FilterType.CLEAR_BW,
                            rotationDegrees = 0f,
                            ocrText = "Section 4. Deliverables and Timeline\nFinal delivery verified and approved by both parties."
                        )
                    )
                ),
                Document(
                    id = "doc_invoice_02",
                    title = "Q4 Tax Invoice #2026-881",
                    createdDate = now - 86400000 * 5,
                    modifiedDate = now - 86400000 * 1,
                    folderId = "folder_tax",
                    tags = listOf("Tax", "Expense"),
                    notes = "Hardware equipment purchase receipt for deduction.",
                    pages = listOf(
                        ScannedPage(
                            id = "p3",
                            documentId = "doc_invoice_02",
                            pageIndex = 0,
                            imageUri = "mock_invoice_p1",
                            filterType = FilterType.MAGIC_COLOR,
                            ocrText = "TAX INVOICE\nVendor: Tech World Solutions\nTotal: $1,450.00\nPayment Status: PAID"
                        )
                    )
                ),
                Document(
                    id = "doc_passport_03",
                    title = "Passport & Travel Visa",
                    createdDate = now - 86400000 * 14,
                    modifiedDate = now - 86400000 * 14,
                    folderId = "folder_personal",
                    tags = listOf("ID", "Travel"),
                    notes = "Scanned international travel passport and entry clearance.",
                    pages = listOf(
                        ScannedPage(
                            id = "p4",
                            documentId = "doc_passport_03",
                            pageIndex = 0,
                            imageUri = "mock_passport_p1",
                            filterType = FilterType.ORIGINAL,
                            ocrText = "PASSPORT\nType: P\nCode: USA\nSurname: DOE\nGiven Names: JOHN"
                        )
                    )
                )
            )
        }
    }
}
