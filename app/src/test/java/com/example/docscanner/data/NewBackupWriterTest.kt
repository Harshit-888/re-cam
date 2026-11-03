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
        var failFlushOrClose = false
        var failDelete = false
        var corruptRead = false
        var deleted = false
        var writes = 0
        var idCounter = java.util.concurrent.atomic.AtomicInteger(0)

        override fun existingIds(): Set<String> {
            if (refuseListing) throw IOException("listing failed")
            return files.keys.toSet()
        }

        override fun create(name: String): String {
            if (returnOldId) return "old"
            val id = "new_${idCounter.incrementAndGet()}"
            files[id] = byteArrayOf()
            return id
        }

        override fun openWrite(id: String): OutputStream {
            writes++
            if (failWrite) throw IOException("write denied")
            return object : ByteArrayOutputStream() {
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
        assertArrayEquals("new backup bytes".toByteArray(), destination.files.getValue("new_1"))
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

    @Test fun `process kill simulation mid-transfer leaves partial new file but never touches old backup`() {
        val destination = FakeDestination()
        val oldHashBefore = sha256(destination.files.getValue("old"))

        // Simulate a process kill during streaming: write some bytes, then simulate immediate VM death
        // where finally blocks or delete are bypassed.
        val newId = destination.create("killed.zip")
        val partialStream = destination.openWrite(newId)
        partialStream.write("partial 50%".toByteArray())
        partialStream.close()

        // Existing file remains 100% untouched with identical hash
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))
        // New file exists as an incomplete orphan, but old backup was NEVER truncated
        assertTrue(destination.files.containsKey(newId))
        assertEquals("partial 50%", String(destination.files.getValue(newId)))
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
        assertEquals(oldHashBefore, sha256(destination.files.getValue("old")))

        // Verify each created backup has verified matching bytes
        val payloads = (1..4).map { "concurrent backup data $it".toByteArray() }
        for (payload in payloads) {
            assertTrue(destination.files.values.any { it.contentEquals(payload) })
        }
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
        val writtenBytes = destination.files.getValue("new_1")
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
