package com.example.docscanner.util

import com.example.docscanner.model.Document
import com.example.docscanner.model.Folder
import com.example.docscanner.model.LibrarySnapshot
import com.example.docscanner.model.ScannedPage
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * The restore path is the only place in this app that overwrites the user's library,
 * so the rules are tested rather than trusted: what goes in comes out, and everything
 * that is not exactly what [BackupArchive.create] wrote is refused.
 */
class BackupArchiveTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    // ------------------------------------------------------------------ helpers

    private fun dirs(): Triple<File, File, File> {
        val root = temp.newFolder()
        val pages = File(root, "pages").apply { mkdirs() }
        val signatures = File(root, "signatures").apply { mkdirs() }
        return Triple(root, pages, signatures)
    }

    private fun page(id: String, uri: String, original: String? = null) = ScannedPage(
        id = id,
        documentId = "doc",
        pageIndex = 0,
        imageUri = uri,
        originalImageUri = original
    )

    private fun library(pages: List<ScannedPage>) = LibrarySnapshot(
        schemaVersion = 1,
        folders = listOf(Folder(id = "folder_a", name = "Work")),
        documents = listOf(
            Document(
                id = "doc",
                title = "Contract",
                createdDate = 1L,
                modifiedDate = 2L,
                folderId = "folder_a",
                pages = pages
            )
        )
    )

    private fun writeArchive(
        target: File,
        manifest: BackupArchive.BackupManifest,
        library: LibrarySnapshot,
        entries: Map<String, ByteArray>
    ) {
        FileOutputStream(target).use { out ->
            ZipOutputStream(out).use { zip ->
                zip.putNextEntry(ZipEntry(BackupArchive.LIBRARY_ENTRY))
                zip.write(json.encodeToString(LibrarySnapshot.serializer(), library).toByteArray())
                zip.closeEntry()
                entries.forEach { (name, bytes) ->
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(bytes)
                    zip.closeEntry()
                }
                zip.putNextEntry(ZipEntry(BackupArchive.MANIFEST_ENTRY))
                zip.write(json.encodeToString(BackupArchive.BackupManifest.serializer(), manifest).toByteArray())
                zip.closeEntry()
            }
        }
    }

    private fun expectRefusal(block: () -> Unit, because: String) {
        try {
            block()
            fail("Expected the archive to be refused: $because")
        } catch (expected: BackupArchive.BackupException) {
            assertNotNull(expected.message)
        }
    }

    @Test
    fun `commit refuses an existing file and rolls back earlier files`() {
        val (root, pages, _) = dirs()
        val staging = File(root, "staged").apply { mkdirs() }
        val first = File(staging, "first").apply { writeText("new first") }
        val second = File(staging, "second").apply { writeText("new second") }
        val newTarget = File(pages, "first.jpg")
        val existing = File(pages, "second.jpg").apply { writeText("original") }
        val restored = BackupArchive.RestoredBackup(
            snapshot = library(emptyList()),
            summary = BackupArchive.Summary(0, 0, 0, 0),
            manifest = BackupArchive.BackupManifest(schemaVersion = 1, createdAt = 0, documentCount = 0, pageCount = 0),
            moves = listOf(first to newTarget, second to existing),
            staging = staging
        )
        try {
            restored.commit()
            fail("Expected collision to fail")
        } catch (_: java.io.IOException) {
            assertEquals("original", existing.readText())
            assertTrue(!newTarget.exists())
            assertTrue(first.exists())
            assertTrue(second.exists())
        } finally {
            restored.discard()
        }
    }

    @Test
    fun `commit rolls back files when a staged source cannot be copied`() {
        val (root, pages, _) = dirs()
        val staging = File(root, "staged").apply { mkdirs() }
        val first = File(staging, "first").apply { writeText("new first") }
        val missing = File(staging, "missing")
        val firstTarget = File(pages, "first.jpg")
        val secondTarget = File(pages, "second.jpg")
        val restored = BackupArchive.RestoredBackup(
            snapshot = library(emptyList()),
            summary = BackupArchive.Summary(0, 0, 0, 0),
            manifest = BackupArchive.BackupManifest(schemaVersion = 1, createdAt = 0, documentCount = 0, pageCount = 0),
            moves = listOf(first to firstTarget, missing to secondTarget),
            staging = staging
        )
        try {
            restored.commit()
            fail("Expected copy to fail")
        } catch (_: java.io.IOException) {
            assertTrue(!firstTarget.exists())
            assertTrue(!secondTarget.exists())
        } finally {
            restored.discard()
        }
    }

    // -------------------------------------------------------------- round trip

    @Test
    fun `a backup restores the library and its images`() {
        val (root, pages, signatures) = dirs()
        val jpeg = File(pages, "11111111-2222-3333-4444-555555555555.jpg")
        val png = File(pages, "11111111-2222-3333-4444-555555555555_edited.png")
        jpeg.writeBytes(ByteArray(4096) { (it % 251).toByte() })
        png.writeBytes(ByteArray(1024) { (it % 97).toByte() })
        // Two pages: one references the JPEG twice (capture + bake, as a real page
        // does) and must still be packed once; the other is a lossless PNG bake.
        val snapshot = library(
            listOf(
                page("p1", jpeg.absolutePath, jpeg.absolutePath),
                page("p2", png.absolutePath)
            )
        )

        val archive = File(root, "backup.zip")
        FileOutputStream(archive).use { out ->
            val summary = BackupArchive.create(out, snapshot, pages, signatures, createdAt = 42L)
            assertEquals(1, summary.documents)
            assertEquals(2, summary.pages)
            assertEquals(2, summary.files)
            assertEquals(0, summary.missingFiles)
            assertEquals(5120L, summary.bytes)
        }

        val inspected = BackupArchive.inspect(archive)
        assertEquals(2, inspected.files)
        assertEquals(5120L, inspected.bytes)

        // Restore somewhere else entirely, as if on another install.
        val (root2, pages2, signatures2) = dirs()
        val restored = BackupArchive.restore(archive, pages2, signatures2, root2)
        assertEquals(1, restored.snapshot.documents.size)
        val restoredPages = restored.snapshot.documents.single().pages
        assertEquals(2, restoredPages.size)
        // Absolute paths, rewritten for *this* install — not the ones in the archive.
        assertTrue(restoredPages[0].imageUri.startsWith(pages2.absolutePath))
        assertTrue(restoredPages[0].originalImageUri!!.startsWith(pages2.absolutePath))
        assertTrue(restoredPages[1].imageUri.startsWith(pages2.absolutePath))

        // Nothing is written before commit().
        assertTrue(pages2.listFiles()!!.isEmpty())
        restored.commit()

        assertEquals(jpeg.readBytes().toList(), File(restoredPages[0].imageUri).readBytes().toList())
        assertEquals(png.readBytes().toList(), File(restoredPages[1].imageUri).readBytes().toList())
        // The staging directory is gone from the parent, not from pages/.
        assertTrue(root2.listFiles()!!.none { it.name.startsWith("restore-") })
    }

    @Test
    fun `a page whose image is missing is counted, not silently dropped`() {
        val (root, pages, signatures) = dirs()
        val present = File(pages, "aaaa.jpg").apply { writeBytes(ByteArray(10) { 7 }) }
        val absent = File(pages, "bbbb.jpg")
        val snapshot = library(
            listOf(
                page("p1", present.absolutePath),
                page("p2", absent.absolutePath)
            )
        )
        val archive = File(root, "backup.zip")
        val summary = FileOutputStream(archive).use { out ->
            BackupArchive.create(out, snapshot, pages, signatures)
        }
        assertEquals(2, summary.pages)
        assertEquals(1, summary.files)
        assertEquals(1, summary.missingFiles)

        val (root2, pages2, signatures2) = dirs()
        val restored = BackupArchive.restore(archive, pages2, signatures2, root2)
        assertEquals(2, restored.snapshot.documents.single().pages.size)
        restored.commit()
        // The localisable one is rewritten; the missing one keeps its old absolute path.
        assertEquals(2, restored.snapshot.documents.single().pages.size)
        assertTrue(File(pages2, "aaaa.jpg").isFile)
    }

    @Test
    fun `discarding a restore leaves the library untouched`() {
        val (root, pages, signatures) = dirs()
        File(pages, "cccc.jpg").writeBytes(ByteArray(32) { 1 })
        val archive = File(root, "backup.zip")
        FileOutputStream(archive).use { out ->
            BackupArchive.create(out, library(listOf(page("p1", File(pages, "cccc.jpg").absolutePath))), pages, signatures)
        }
        val (root2, pages2, signatures2) = dirs()
        val restored = BackupArchive.restore(archive, pages2, signatures2, root2)
        restored.discard()
        assertTrue(pages2.listFiles()!!.isEmpty())
        assertEquals(0, root2.listFiles()!!.count { it.name.startsWith("restore-") })
    }

    // ------------------------------------------------------------- refusals

    @Test
    fun `an entry that escapes its directory is refused`() {
        val (root, pages, signatures) = dirs()
        val manifest = BackupArchive.BackupManifest(
            schemaVersion = 1,
            createdAt = 0,
            documentCount = 0,
            pageCount = 0,
            files = listOf(BackupArchive.BackupFile("pages/../../evil.jpg", 4, "00"))
        )
        val archive = File(root, "evil.zip")
        writeArchive(archive, manifest, library(emptyList()), mapOf("pages/../../evil.jpg" to ByteArray(4)))
        expectRefusal({ BackupArchive.restore(archive, pages, signatures, root) }, "path traversal")
    }

    @Test
    fun `an undeclared entry is refused`() {
        val (root, pages, signatures) = dirs()
        val bytes = ByteArray(8) { 3 }
        val manifest = BackupArchive.BackupManifest(
            schemaVersion = 1,
            createdAt = 0,
            documentCount = 0,
            pageCount = 0,
            files = emptyList()
        )
        val archive = File(root, "smuggle.zip")
        writeArchive(archive, manifest, library(emptyList()), mapOf("pages/sneaky.jpg" to bytes))
        expectRefusal({ BackupArchive.restore(archive, pages, signatures, root) }, "undeclared entry")
    }

    @Test
    fun `a file whose bytes do not match its hash is refused`() {
        val (root, pages, signatures) = dirs()
        val manifest = BackupArchive.BackupManifest(
            schemaVersion = 1,
            createdAt = 0,
            documentCount = 0,
            pageCount = 0,
            files = listOf(BackupArchive.BackupFile("pages/x.jpg", 4, "0".repeat(64)))
        )
        val archive = File(root, "tampered.zip")
        writeArchive(archive, manifest, library(emptyList()), mapOf("pages/x.jpg" to ByteArray(4) { 9 }))
        expectRefusal({ BackupArchive.restore(archive, pages, signatures, root) }, "checksum")
    }

    @Test
    fun `a file the manifest lists but the archive lacks is refused`() {
        val (root, pages, signatures) = dirs()
        val manifest = BackupArchive.BackupManifest(
            schemaVersion = 1,
            createdAt = 0,
            documentCount = 1,
            pageCount = 1,
            files = listOf(BackupArchive.BackupFile("pages/gone.jpg", 4, "ab"))
        )
        val archive = File(root, "short.zip")
        writeArchive(archive, manifest, library(emptyList()), emptyMap())
        expectRefusal({ BackupArchive.inspect(archive) }, "missing file")
    }

    @Test
    fun `a newer archive format is refused`() {
        val (root, pages, signatures) = dirs()
        val manifest = BackupArchive.BackupManifest(
            formatVersion = BackupArchive.FORMAT_VERSION + 1,
            schemaVersion = 1,
            createdAt = 0,
            documentCount = 0,
            pageCount = 0
        )
        val archive = File(root, "future.zip")
        writeArchive(archive, manifest, library(emptyList()), emptyMap())
        expectRefusal({ BackupArchive.inspect(archive) }, "newer format")
    }

    @Test
    fun `a zip that is not one of ours is refused`() {
        val (root, pages, signatures) = dirs()
        val manifest = BackupArchive.BackupManifest(
            format = "someone-elses-backup",
            schemaVersion = 1,
            createdAt = 0,
            documentCount = 0,
            pageCount = 0
        )
        val archive = File(root, "foreign.zip")
        writeArchive(archive, manifest, library(emptyList()), emptyMap())
        expectRefusal({ BackupArchive.inspect(archive) }, "wrong format")
    }

    // ---------------------------------------------------------------- merging

    @Test
    fun `restoring keeps documents the backup never knew about`() {
        val local = LibrarySnapshot(
            schemaVersion = 1,
            folders = listOf(Folder(id = "f1", name = "Local")),
            documents = listOf(
                Document(id = "kept", title = "This morning", createdDate = 1, modifiedDate = 1),
                Document(id = "shared", title = "Old title", createdDate = 1, modifiedDate = 1)
            )
        )
        val incoming = LibrarySnapshot(
            schemaVersion = 1,
            folders = listOf(Folder(id = "f2", name = "From backup")),
            documents = listOf(
                Document(id = "shared", title = "Backup title", createdDate = 1, modifiedDate = 1),
                Document(id = "new", title = "Only in backup", createdDate = 1, modifiedDate = 1)
            )
        )

        val merged = BackupArchive.merge(local, incoming)

        assertEquals(
            listOf("kept", "shared", "new"),
            merged.documents.map { it.id }
        )
        // The backup wins a collision, and local-only data survives.
        assertEquals("Backup title", merged.documents.first { it.id == "shared" }.title)
        assertEquals(setOf("f1", "f2"), merged.folders.map { it.id }.toSet())
    }
}
