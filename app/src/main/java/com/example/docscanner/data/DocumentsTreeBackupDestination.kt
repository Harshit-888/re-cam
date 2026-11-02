package com.example.docscanner.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Adapter for a user-selected SAF tree. Requires readable children and readback. */
internal class DocumentsTreeBackupDestination(
    private val resolver: ContentResolver,
    private val tree: Uri
) : NewBackupDestination {
    init {
        require(DocumentsContract.isTreeUri(tree)) { "Choose a backup folder, not an existing file" }
    }

    private val parent: Uri = DocumentsContract.buildDocumentUriUsingTree(
        tree, DocumentsContract.getTreeDocumentId(tree)
    )
    private val created = mutableMapOf<String, Uri>()

    override fun existingIds(): Set<String> {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            tree, DocumentsContract.getTreeDocumentId(tree)
        )
        val result = mutableSetOf<String>()
        val cursor = resolver.query(children, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)
            ?: throw IOException("Cannot inspect the selected folder safely")
        cursor.use {
            if (it.extras.getBoolean(DocumentsContract.EXTRA_LOADING, false)) {
                throw IOException("The provider has not finished listing the selected folder")
            }
            val idColumn = it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            if (idColumn < 0) throw IOException("The provider did not return document IDs")
            while (it.moveToNext()) {
                if (it.isNull(idColumn)) throw IOException("The provider returned an invalid document ID")
                result.add(it.getString(idColumn))
            }
        }
        return result
    }

    override fun create(name: String): String {
        val uri = DocumentsContract.createDocument(resolver, parent, "application/zip", name)
            ?: throw IOException("Could not create a new backup in the chosen folder")
        if (uri.authority != tree.authority) throw IOException("The provider returned an unexpected backup URI")
        val id = DocumentsContract.getDocumentId(uri)
        created[id] = uri
        return id
    }

    private fun createdUri(id: String): Uri = created[id] ?: throw IOException("Unknown backup document")
    override fun openWrite(id: String): OutputStream = resolver.openOutputStream(createdUri(id), "w")
        ?: throw IOException("Could not open the new backup for writing")
    override fun openRead(id: String): InputStream = resolver.openInputStream(createdUri(id))
        ?: throw IOException("Could not verify the new backup")
    override fun delete(id: String): Boolean = DocumentsContract.deleteDocument(resolver, createdUri(id))
}
