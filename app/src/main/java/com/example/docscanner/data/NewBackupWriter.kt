package com.example.docscanner.data

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

/** Minimal provider operations for creating, but never replacing, a backup document. */
internal interface NewBackupDestination {
    /** All existing child document IDs. Failure to enumerate must throw. */
    fun existingIds(): Set<String>
    /** Create a new uniquely named child, returning its opaque document ID. */
    fun create(name: String): String
    fun openWrite(id: String): OutputStream
    fun openRead(id: String): InputStream
    fun delete(id: String): Boolean
}

/**
 * Writes to a freshly created document and verifies the provider's readback.
 * This is not an atomic provider transaction: process death can leave a partial
 * *new* document, and a malicious or nonconforming provider can violate its API.
 */
internal fun writeNewBackup(staged: File, destination: NewBackupDestination, name: String) {
    val oldIds = destination.existingIds()
    val newId = destination.create(name)
    // Never write to or delete an existing document if the provider returned it.
    if (newId in oldIds) throw IOException("Provider returned an existing document; no write attempted")
    var complete = false
    try {
        staged.inputStream().use { input ->
            destination.openWrite(newId).use { output -> input.copyTo(output) }
        }
        val localHash = staged.inputStream().use(::sha256)
        val remoteHash = destination.openRead(newId).use(::sha256)
        if (!MessageDigest.isEqual(localHash, remoteHash)) {
            throw IOException("The provider returned different backup bytes")
        }
        complete = true
    } finally {
        if (!complete) {
            // Do not mask the original error if the provider cannot delete the new file.
            try { destination.delete(newId) } catch (_: Exception) { }
        }
    }
}

private fun sha256(input: InputStream): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        digest.update(buffer, 0, count)
    }
    return digest.digest()
}
