package com.example.docscanner.data

import com.example.docscanner.model.Document
import com.example.docscanner.model.Folder
import com.example.docscanner.model.LibrarySnapshot
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.util.BackupArchive
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class NewBackupWriterTest {
    @get:Rule val temp = TemporaryFolder()

    private class FakeDestination : NewBackupDestination {
        val files = ConcurrentHashMap<String, ByteArray>().apply {
            put("old", "existing backup payload with valuable user scans".toByteArray())
        }
        var refuseListing = false
        var returnOldId = false
        var failWrite = false
        var failMidStream = false
        var failFlushOrClose = false
        var failDelete = false
        var corruptRead = false
        var deleted = false
        var writes = 0

        /** Ids handed out by [create], in creation order. */
        val createdIds: MutableList<String> = java.util.Collections.synchronizedList(mutableListOf())

        /** Names handed to [create], in creation order. Production mints UUID names. */
        val createdNames: MutableList<String> = java.util.Collections.synchronizedList(mutableListOf())

        /** Bytes of the most recently created document. */
        fun newest(): ByteArray = synchronized(files) { files.getValue(createdIds.last()) }

        override fun existingIds(): Set<String> {
            if (refuseListing) throw IOException("listing failed")
            return files.keys.toSet()
        }

        override fun create(name: String): String {
            if (returnOldId) return "old"
            // UUID ids, like DocumentsContract.createDocument hands back. Never
            // sequential: a numeric scheme would hide a collision bug.
            val id = "doc-${UUID.randomUUID()}"
            files[id] = byteArrayOf()
            createdIds.add(id)
            createdNames.add(name)
            return id
        }

        override fun openWrite(id: String): OutputStream {
            writes++
            // failWrite = the provider refuses to open a stream at all (0 bytes
            // written). failMidStream = the stream opens, accepts bytes, then dies
            // — the case a provider that runs out of quota actually produces.
            if (failWrite) throw IOException("write denied")
            return object : ByteArrayOutputStream() {
                private var chunks = 0
                override fun write(b: ByteArray, off: Int, len: Int) {
                    if (failMidStream && chunks++ >= 1) throw IOException("write failed mid-stream")
                    super.write(b, off, len)
                }
                override fun flush() {
                    if (failFlushOrClose) throw IOException("flush failure")
                    super.flush()
                }
                override fun close() {
                    if (failFlushOrClose) throw IOException("close failure")
                    files[id] = toByteArray()
                    super.close()
                }
            }
        }

        override fun openRead(id: String): InputStream = ByteArrayInputStream(
            if (corruptRead) byteArrayOf(42) else files.getValue(id)
        )

        override fun delete(id: String): Boolean {
            deleted = true
            if (failDelete) throw IOException("delete refusal from provider")
            return files.remove(id) != null
        }
    }

    companion object {
        const val EXPECTED_OLD_FIXTURE_SHA256 = "b43030fec31739bcf20fe483bfb2dce8cde3b4ab2265b2994a413e091cc7594c"
    }

    private fun stage(content: String = "new backup bytes") = temp.newFile().apply { writeText(content) }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    @Test fun `new backup is read back and old backup remains intact`() {
        val destination = FakeDestination()
        val oldHashBefore = sha256(destination.files.getValue("old"))
        assertEquals(EXPECTED_OLD_FIXTURE_SHA256, oldHashBefore)
        writeNewBackup(stage(), destination, "new.zip")

        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
        assertArrayEquals("new backup bytes".toByteArray(), destination.newest())
        assertFalse(destination.deleted)
    }

    @Test fun `listing failure never creates or writes`() {
        val destination = FakeDestination().apply { refuseListing = true }
        val oldHashBefore = sha256(destination.files.getValue("old"))

        try {
            writeNewBackup(stage(), destination, "new.zip")
            throw AssertionError("expected failure")
        } catch (_: IOException) { }

        assertEquals(0, destination.writes)
        assertEquals(setOf("old"), destination.files.keys)
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
    }

    @Test fun `provider returning old id is never written or deleted`() {
        val destination = FakeDestination().apply { returnOldId = true }
        val oldHashBefore = sha256(destination.files.getValue("old"))

        try {
            writeNewBackup(stage(), destination, "new.zip")
            throw AssertionError("expected failure")
        } catch (_: IOException) { }

        assertEquals(0, destination.writes)
        assertFalse(destination.deleted)
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
    }

    @Test fun `write failure and corrupt readback remove new file only`() {
        for (failure in listOf("write", "readback")) {
            val destination = FakeDestination().apply {
                failWrite = failure == "write"
                corruptRead = failure == "readback"
            }
            val oldHashBefore = sha256(destination.files.getValue("old"))

            try {
                writeNewBackup(stage(), destination, "new.zip")
                throw AssertionError("expected failure")
            } catch (_: IOException) { }

            assertTrue(destination.deleted)
            assertEquals(setOf("old"), destination.files.keys)
            assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
        }
    }

    @Test fun `flush and close errors leave old backup intact and clean up new file`() {
        val destination = FakeDestination().apply { failFlushOrClose = true }
        val oldHashBefore = sha256(destination.files.getValue("old"))

        try {
            writeNewBackup(stage(), destination, "new.zip")
            throw AssertionError("expected failure")
        } catch (e: IOException) {
            assertTrue(e.message?.contains("flush failure") == true || e.message?.contains("close failure") == true)
        }

        assertTrue(destination.deleted)
        assertEquals(setOf("old"), destination.files.keys)
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
    }

    @Test fun `cleanup refusal retains original error and preserves old backup`() {
        val destination = FakeDestination().apply {
            failWrite = true
            failDelete = true
        }
        val oldHashBefore = sha256(destination.files.getValue("old"))

        try {
            writeNewBackup(stage(), destination, "new.zip")
            throw AssertionError("expected failure")
        } catch (e: IOException) {
            // Original write failure must be preserved, not masked by delete exception
            assertEquals("write denied", e.message)
        }

        assertTrue(destination.deleted)
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
    }

    /**
     * Mid-stream provider failure, through the real [writeNewBackup] path.
     *
     * The stream opens, accepts one chunk, then throws. This is the case a provider
     * that runs out of quota or loses its remote connection actually produces, and it
     * is distinct from [FakeDestination.failWrite], which never writes a byte.
     */
    @Test fun `write failure after bytes were transferred removes the new file and keeps the old one`() {
        val destination = FakeDestination().apply { failMidStream = true }
        val oldHashBefore = sha256(destination.files.getValue("old"))

        try {
            writeNewBackup(stage("x".repeat(40_000)), destination, "mid.zip")
            throw AssertionError("expected failure")
        } catch (e: IOException) {
            assertEquals("write failed mid-stream", e.message)
        }

        assertEquals(1, destination.writes)
        assertTrue(destination.deleted)
        assertEquals(setOf("old"), destination.files.keys)
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
    }

    /**
     * Process death, as far as [writeNewBackup]'s contract can express it in a JVM test.
     *
     * A real `kill -9` cannot be simulated in a unit test and must be run on a device
     * (see the H004 verification list). What *can* be pinned here is the property the
     * device test would check: the writer only ever touches the id it just created, so
     * death at any point can leave an orphan but can never truncate a previous backup.
     *
     * So: run the writer to the point where bytes have landed and the readback is due,
     * then make the readback throw — the closest in-process analogue of "the process
     * stopped here". Afterwards the old backup must be byte-identical and the id the
     * writer touched must be one it created itself.
     */
    @Test fun `death before verification never touches the previous backup`() {
        val destination = FakeDestination()
        val oldHashBefore = sha256(destination.files.getValue("old"))

        // Throw from openRead: bytes are already written, verification never completes.
        val dying = object : NewBackupDestination by destination {
            override fun openRead(id: String): InputStream =
                throw IOException("process died before verification")
        }

        try {
            writeNewBackup(stage(), dying, "dying.zip")
            throw AssertionError("expected failure")
        } catch (e: IOException) {
            assertEquals("process died before verification", e.message)
        }

        // The id the writer touched is one it created — never the pre-existing "old".
        assertEquals(1, destination.createdIds.size)
        assertNotEquals("old", destination.createdIds.single())

        // The previous backup is untouched whether or not the cleanup delete ran.
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
    }

    @Test fun `concurrent exports to same destination create distinct files without collision or data loss`() {
        val destination = FakeDestination()
        val oldHashBefore = sha256(destination.files.getValue("old"))

        val executor = Executors.newFixedThreadPool(4)
        val errors = ConcurrentHashMap.newKeySet<Throwable>()

        for (i in 1..4) {
            executor.submit {
                try {
                    val stagedFile = stage("concurrent backup data $i")
                    writeNewBackup(stagedFile, destination, "backup_$i.zip")
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }
        }

        executor.shutdown()
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
        assertTrue("No concurrent export errors, but got: $errors", errors.isEmpty())

        // 4 new files created + 1 old file = 5 files total
        assertEquals(5, destination.files.size)
        assertEquals(4, destination.createdIds.toSet().size)
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))

        // Every created document was given the caller's own name, and ids are opaque
        // provider ids rather than a shared counter (a counter is what would hide a
        // collision on a provider that hands back duplicates).
        assertEquals((1..4).map { "backup_$it.zip" }.toSet(), destination.createdNames.toSet())
        assertTrue(destination.createdIds.all { it.startsWith("doc-") })

        // Verify each created backup has verified matching bytes
        val payloads = (1..4).map { "concurrent backup data $it".toByteArray() }
        for (payload in payloads) {
            assertTrue(destination.files.values.any { it.contentEquals(payload) })
        }
    }

    /**
     * Boundary test: an id created *after* the listing is outside the refusal guarantee.
     *
     * `writeNewBackup` refuses ids that appear in its pre-create listing. It cannot refuse
     * an id that did not exist when it listed — there is no provider API to ask "is this id
     * mine?". So if a nonconforming provider hands back another export's document id, this
     * writer will write to it. What remains guaranteed, and is asserted below, is the thing
     * that matters for user data: the *pre-existing* backup is never opened, truncated or
     * deleted.
     *
     * Recorded as a test rather than left implicit so the guarantee's edge is documented
     * and cannot regress silently. Closing it needs a create-then-verify-exclusivity API
     * that SAF does not offer.
     */
    @Test fun `ids created after the listing are outside the refusal guarantee`() {
        val destination = FakeDestination()
        val oldHashBefore = sha256(destination.files.getValue("old"))

        val racing = object : NewBackupDestination by destination {
            override fun create(name: String): String {
                // Another export lands between our listing and our create.
                val theirs = destination.create("other-export.zip")
                destination.files[theirs] = "another export in flight".toByteArray()
                return theirs
            }
        }

        // The id is not in the listing the writer took, so the writer cannot know it is
        // foreign. The guarantee that still holds is the one for user data: the existing
        // backup is untouched. This is the documented edge of the contract, not a bug.
        writeNewBackup(stage(), racing, "racing.zip")
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
    }

    @Test fun `real BackupArchive zip written and read back can be cleanly restored`() {
        val pagesDir = temp.newFolder("pages")
        val signaturesDir = temp.newFolder("signatures")
        val restorePagesDir = temp.newFolder("restore_pages")
        val restoreSignaturesDir = temp.newFolder("restore_signatures")
        val restoreRoot = temp.newFolder("restore_root")

        // Create a dummy page image
        val pageFile = File(pagesDir, "page_1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4, 5)) }

        val snapshot = LibrarySnapshot(
            schemaVersion = 1,
            folders = listOf(Folder(id = "work", name = "Work")),
            documents = listOf(
                Document(
                    id = "doc_1",
                    title = "Tax Form",
                    createdDate = 100L,
                    modifiedDate = 200L,
                    folderId = "work",
                    pages = listOf(
                        ScannedPage(
                            id = "p1",
                            documentId = "doc_1",
                            pageIndex = 0,
                            imageUri = pageFile.absolutePath,
                            revision = 1
                        )
                    )
                )
            )
        )

        val stagedArchive = temp.newFile("staged_backup.zip")
        stagedArchive.outputStream().use { out ->
            BackupArchive.create(out, snapshot, pagesDir, signaturesDir)
        }

        val destination = FakeDestination()
        val oldHashBefore = sha256(destination.files.getValue("old"))

        // Write to destination
        writeNewBackup(stagedArchive, destination, "tax_backup.zip")

        // Old file must have unchanged hash
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))

        // Read back written file and restore
        val writtenBytes = destination.newest()
        val restoredArchiveFile = temp.newFile("readback_restored.zip").apply { writeBytes(writtenBytes) }

        val restored = BackupArchive.restore(
            archive = restoredArchiveFile,
            pagesDir = restorePagesDir,
            signaturesDir = restoreSignaturesDir,
            stagingParent = restoreRoot
        )

        assertEquals(1, restored.summary.documents)
        assertEquals(1, restored.summary.pages)
        assertEquals(1, restored.snapshot.folders.size)
        assertEquals("Tax Form", restored.snapshot.documents.first().title)
        restored.commit()
        assertTrue(File(restorePagesDir, "page_1.jpg").exists())
    }
}
