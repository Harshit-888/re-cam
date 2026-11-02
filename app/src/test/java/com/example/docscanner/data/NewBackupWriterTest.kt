package com.example.docscanner.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class NewBackupWriterTest {
    @get:Rule val temp = TemporaryFolder()

    private class FakeDestination : NewBackupDestination {
        val files = mutableMapOf("old" to "old backup".toByteArray())
        var refuseListing = false
        var returnOldId = false
        var failWrite = false
        var corruptRead = false
        var deleted = false
        var writes = 0

        override fun existingIds(): Set<String> {
            if (refuseListing) throw IOException("listing failed")
            return files.keys.toSet()
        }
        override fun create(name: String): String {
            if (returnOldId) return "old"
            files["new"] = byteArrayOf()
            return "new"
        }
        override fun openWrite(id: String): OutputStream {
            writes++
            if (failWrite) throw IOException("write denied")
            return object : ByteArrayOutputStream() {
                override fun close() {
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
            return files.remove(id) != null
        }
    }

    private fun stage() = temp.newFile().apply { writeText("new backup") }

    @Test fun `new backup is read back and old backup remains intact`() {
        val destination = FakeDestination()
        writeNewBackup(stage(), destination, "new.zip")
        assertArrayEquals("old backup".toByteArray(), destination.files.getValue("old"))
        assertArrayEquals("new backup".toByteArray(), destination.files.getValue("new"))
        assertFalse(destination.deleted)
    }

    @Test fun `listing failure never creates or writes`() {
        val destination = FakeDestination().apply { refuseListing = true }
        try { writeNewBackup(stage(), destination, "new.zip"); throw AssertionError("expected failure") }
        catch (_: IOException) { }
        assertEquals(0, destination.writes)
        assertEquals(setOf("old"), destination.files.keys)
    }

    @Test fun `provider returning old id is never written or deleted`() {
        val destination = FakeDestination().apply { returnOldId = true }
        try { writeNewBackup(stage(), destination, "new.zip"); throw AssertionError("expected failure") }
        catch (_: IOException) { }
        assertEquals(0, destination.writes)
        assertFalse(destination.deleted)
        assertArrayEquals("old backup".toByteArray(), destination.files.getValue("old"))
    }

    @Test fun `write failure and corrupt readback remove new file only`() {
        for (failure in listOf("write", "readback")) {
            val destination = FakeDestination().apply {
                failWrite = failure == "write"
                corruptRead = failure == "readback"
            }
            try { writeNewBackup(stage(), destination, "new.zip"); throw AssertionError("expected failure") }
            catch (_: IOException) { }
            assertTrue(destination.deleted)
            assertEquals(setOf("old"), destination.files.keys)
            assertArrayEquals("old backup".toByteArray(), destination.files.getValue("old"))
        }
    }
}
