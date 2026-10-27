package com.example.docscanner.util

import com.example.docscanner.data.LibraryStore
import com.example.docscanner.model.Document
import com.example.docscanner.model.Folder
import com.example.docscanner.model.LibrarySnapshot
import com.example.docscanner.model.ScannedPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * The library as one `.zip` file: `library.json`, the page images it references, and
 * a manifest that says what should be inside.
 *
 * ## Why a manifest at all
 *
 * A zip is a directory listing, not a promise. Restoring from one is the single most
 * dangerous operation in this app — it overwrites the library that is the user's
 * scans — so the archive states its own format, its schema version, every entry it
 * contains, and the SHA-256 of each. On restore, anything that does not match is
 * refused: an entry in the zip but not the manifest, an entry in the manifest but not
 * the zip, a hash that differs, a path that is not `pages/<file>` or
 * `signatures/<file>`, an entry count or a total size past the caps below. Refusing
 * loudly is the whole point; a half-restored library is worse than a failed restore.
 *
 * ## Paths
 *
 * `ScannedPage` stores **absolute** paths (`/data/user/0/…/files/pages/<id>.jpg`).
 * Those are meaningless on another device, or after a reinstall. The archive
 * therefore stores `pages/<file name>` and `signatures/<file name>`, and
 * [restore] rewrites them back to absolute paths under the *current* directories.
 *
 * ## Atomicity
 *
 * [restore] extracts into a staging directory next to the real one (same filesystem,
 * so the move is a rename) and returns a [RestoredBackup]. Nothing is touched until
 * [RestoredBackup.commit] is called; if anything fails before that, the live library
 * is untouched and [RestoredBackup.discard] cleans up. There is no window in which a
 * page has been overwritten but the JSON has not.
 *
 * Pure JVM: no Android imports, so every rule here is unit-tested on the host.
 */
object BackupArchive {

    const val FORMAT = "docscanner-backup"
    const val FORMAT_VERSION = 1
    const val EXTENSION = "zip"
    const val MIME_TYPE = "application/zip"

    /** Entry names inside the archive. */
    const val MANIFEST_ENTRY = "backup.json"
    const val LIBRARY_ENTRY = "library.json"

    private const val PAGES = "pages"
    private const val SIGNATURES = "signatures"

    /** `pages/<name>` or `signatures/<name>`, nothing else, no dot-segments. */
    private val ENTRY_PATTERN = Regex("^($PAGES|$SIGNATURES)/[A-Za-z0-9][A-Za-z0-9._-]{0,119}$")

    /** Caps. Generous for a library of thousands of pages, brutal to a zip bomb. */
    const val MAX_ENTRIES = 20_000
    const val MAX_ENTRY_BYTES = 256L * 1024 * 1024
    const val MAX_TOTAL_BYTES = 2L * 1024 * 1024 * 1024
    const val MAX_MANIFEST_BYTES = 8L * 1024 * 1024

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

    /** One file in the archive. [path] is also the zip entry name. */
    @Serializable
    data class BackupFile(val path: String, val bytes: Long, val sha256: String)

    @Serializable
    data class BackupManifest(
        val format: String = FORMAT,
        val formatVersion: Int = FORMAT_VERSION,
        val schemaVersion: Int,
        val createdAt: Long,
        val documentCount: Int,
        val pageCount: Int,
        val files: List<BackupFile> = emptyList()
    )

    /** What a create or an inspect found. */
    data class Summary(
        val documents: Int,
        val pages: Int,
        val files: Int,
        val bytes: Long,
        /** Pages whose image could not be found on disk and so is not in the archive. */
        val missingFiles: Int = 0
    )

    // ------------------------------------------------------------------ writing

    /**
     * Writes [snapshot] and every image it references to [output].
     *
     * A page whose image is missing on disk is still written to the archive, with its
     * absolute path left untouched; [Summary.missingFiles] counts them so the caller
     * can say so instead of shipping a backup that silently lost pages.
     */
    fun create(
        output: OutputStream,
        snapshot: LibrarySnapshot,
        pagesDir: File,
        signaturesDir: File,
        createdAt: Long = System.currentTimeMillis()
    ): Summary {
        val files = LinkedHashMap<String, File>()
        var missing = 0

        fun rewrite(uri: String?): String? {
            if (uri.isNullOrBlank()) return null
            if (uri.startsWith("content://")) return uri // not ours to back up
            val absolute = File(uri).absoluteFile
            val entry = entryFor(absolute, pagesDir, signaturesDir) ?: return uri
            if (files.containsKey(entry)) return entry
            if (!absolute.isFile || absolute.length() == 0L) {
                missing++
                return uri
            }
            files[entry] = absolute
            return entry
        }

        val rewritten = snapshot.copy(
            documents = snapshot.documents.map { document ->
                document.copy(
                    pages = document.pages.map { page ->
                        page.copy(
                            imageUri = rewrite(page.imageUri) ?: page.imageUri,
                            originalImageUri = rewrite(page.originalImageUri),
                            signatureOverlayUri = rewrite(page.signatureOverlayUri)
                        )
                    }
                )
            }
        )

        val written = ArrayList<BackupFile>(files.size)
        var total = 0L
        ZipOutputStream(output.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(LIBRARY_ENTRY))
            zip.write(json.encodeToString(LibrarySnapshot.serializer(), rewritten).toByteArray())
            zip.closeEntry()

            files.forEach { (entry, file) ->
                zip.putNextEntry(ZipEntry(entry))
                val digest = MessageDigest.getInstance("SHA-256")
                var bytes = 0L
                file.inputStream().use { input ->
                    val buffer = ByteArray(COPY_BUFFER)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        digest.update(buffer, 0, read)
                        zip.write(buffer, 0, read)
                        bytes += read
                    }
                }
                zip.closeEntry()
                written += BackupFile(entry, bytes, hex(digest.digest()))
                total += bytes
            }

            val manifest = BackupManifest(
                schemaVersion = snapshot.schemaVersion,
                createdAt = createdAt,
                documentCount = snapshot.documents.size,
                pageCount = snapshot.documents.sumOf { it.pages.size },
                files = written
            )
            // Written last: it can only describe what was actually read.
            zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            zip.write(json.encodeToString(BackupManifest.serializer(), manifest).toByteArray())
            zip.closeEntry()
        }

        return Summary(
            documents = snapshot.documents.size,
            pages = snapshot.documents.sumOf { it.pages.size },
            files = written.size,
            bytes = total,
            missingFiles = missing
        )
    }

    // ---------------------------------------------------------------- inspecting

    /** Reads and validates the manifest without extracting a byte. */
    fun inspect(archive: File): Summary = ZipFile(archive).use { zip ->
        val manifest = readManifest(zip)
        validate(zip, manifest)
        Summary(
            documents = manifest.documentCount,
            pages = manifest.pageCount,
            files = manifest.files.size,
            bytes = manifest.files.sumOf { it.bytes }
        )
    }

    // ----------------------------------------------------------------- restoring

    /**
     * Validates [archive] and extracts it into a staging directory inside
     * [stagingParent]. The library on disk is not touched.
     *
     * @throws BackupException for anything that is not exactly the archive this class
     * wrote: wrong format, newer schema, unlisted entry, bad hash, bad path, a size or
     * count past the caps.
     */
    fun restore(
        archive: File,
        pagesDir: File,
        signaturesDir: File,
        stagingParent: File
    ): RestoredBackup {
        val staging = File(stagingParent, "restore-${System.nanoTime()}")
        val staged = HashMap<String, File>()
        try {
            ZipFile(archive).use { zip ->
                val manifest = readManifest(zip)
                validate(zip, manifest)

                val archivedSnapshot = json.decodeFromString(
                    LibrarySnapshot.serializer(),
                    readEntry(zip, LIBRARY_ENTRY, maxBytes = MAX_MANIFEST_BYTES).decodeToString()
                )
                if (archivedSnapshot.schemaVersion > LibraryStore.CURRENT_SCHEMA_VERSION) {
                    throw BackupException(
                        "Backup was made by a newer version (schema ${archivedSnapshot.schemaVersion})"
                    )
                }

                val declared = manifest.files.associateBy { it.path }

                manifest.files.forEach { file ->
                    val entry = zip.getEntry(file.path)
                        ?: throw BackupException("Missing from archive: ${file.path}")
                    if (entry.size > MAX_ENTRY_BYTES) {
                        throw BackupException("Entry too large: ${file.path}")
                    }
                    val target = File(staging, file.path)
                    target.parentFile?.mkdirs()
                    val digest = MessageDigest.getInstance("SHA-256")
                    var written = 0L
                    zip.getInputStream(entry).use { input ->
                        target.outputStream().buffered().use { out ->
                            val buffer = ByteArray(COPY_BUFFER)
                            while (true) {
                                val read = input.read(buffer)
                                if (read <= 0) break
                                written += read
                                // The header's size is a claim; this is the fact.
                                if (written > MAX_ENTRY_BYTES) {
                                    throw BackupException("Entry too large: ${file.path}")
                                }
                                digest.update(buffer, 0, read)
                                out.write(buffer, 0, read)
                            }
                        }
                    }
                    if (written != file.bytes) {
                        throw BackupException("Size mismatch for ${file.path}")
                    }
                    if (hex(digest.digest()) != file.sha256) {
                        throw BackupException("Checksum mismatch for ${file.path}")
                    }
                    staged[file.path] = target
                }

                if (staged.keys != declared.keys) {
                    throw BackupException("Archive does not contain what its manifest lists")
                }

                val localised = archivedSnapshot.copy(
                    documents = archivedSnapshot.documents.map { document ->
                        document.copy(
                            pages = document.pages.map { page ->
                                localise(page, declared.keys, pagesDir, signaturesDir)
                            }
                        )
                    }
                )

                val moves = staged.map { (path, stagedFile) ->
                    val name = path.substringAfter('/')
                    val target = if (path.startsWith("$PAGES/")) {
                        File(pagesDir, name)
                    } else {
                        File(signaturesDir, name)
                    }
                    stagedFile to target
                }

                return RestoredBackup(
                    snapshot = localised,
                    summary = Summary(
                        documents = manifest.documentCount,
                        pages = manifest.pageCount,
                        files = manifest.files.size,
                        bytes = manifest.files.sumOf { it.bytes }
                    ),
                    manifest = manifest,
                    moves = moves,
                    staging = staging
                )
            }
        } catch (t: Throwable) {
            staging.deleteRecursively()
            throw if (t is BackupException) t else BackupException(t.message ?: "Unreadable backup", t)
        }
    }

    /**
     * A validated, extracted backup that has not been applied yet.
     *
     * [snapshot] already carries absolute paths under the current install, so it can
     * be inspected and shown to the user before [commit].
     */
    class RestoredBackup(
        val snapshot: LibrarySnapshot,
        val summary: Summary,
        val manifest: BackupManifest,
        private val moves: List<Pair<File, File>>,
        private val staging: File
    ) {
        /** Moves the staged files into place. Cheap: same filesystem, so a rename. */
        fun commit() {
            moves.forEach { (from, to) ->
                to.parentFile?.mkdirs()
                if (to.exists()) to.delete()
                if (!from.renameTo(to)) {
                    from.copyTo(to, overwrite = true)
                    from.delete()
                }
            }
            staging.deleteRecursively()
        }

        fun discard() {
            staging.deleteRecursively()
        }
    }

    // ------------------------------------------------------------------- merging

    /**
     * Union of two libraries by id, with [incoming] winning a collision.
     *
     * Restore must not be "delete everything, then import": a backup taken last month
     * does not know about this morning's scan, and silently losing it is the kind of
     * data loss people never forgive. Documents and folders that only exist locally
     * are kept; ones the backup also has are replaced by the backup's version, page
     * files included.
     */
    fun merge(
        local: LibrarySnapshot,
        incoming: LibrarySnapshot
    ): LibrarySnapshot {
        val folders = LinkedHashMap<String, Folder>()
        local.folders.forEach { folders[it.id] = it }
        incoming.folders.forEach { folders[it.id] = it }

        val documents = LinkedHashMap<String, Document>()
        local.documents.forEach { documents[it.id] = it }
        incoming.documents.forEach { documents[it.id] = it }

        return LibrarySnapshot(
            schemaVersion = maxOf(local.schemaVersion, incoming.schemaVersion),
            folders = folders.values.toList(),
            documents = documents.values.toList()
        )
    }

    // ------------------------------------------------------------------ internals

    private const val COPY_BUFFER = 64 * 1024

    private fun entryFor(file: File, pagesDir: File, signaturesDir: File): String? {
        val parent = file.parentFile?.absoluteFile ?: return null
        val name = file.name
        return when {
            parent == pagesDir.absoluteFile -> "$PAGES/$name"
            parent == signaturesDir.absoluteFile -> "$SIGNATURES/$name"
            else -> null
        }
    }

    /**
     * Turns `pages/x.jpg` back into an absolute path under the *current* install.
     *
     * `ScannedPage` holds absolute paths, and the one in the archive was written on
     * some other install — possibly another device. Anything that is not one of our
     * two forms is left exactly as it was: that includes `content://` URIs (not ours
     * to restore) and absolute paths for images that were already missing when the
     * backup was taken, which must stay visibly broken rather than become a path that
     * silently points at nothing.
     */
    private fun localise(
        page: ScannedPage,
        files: Set<String>,
        pagesDir: File,
        signaturesDir: File
    ): ScannedPage {
        fun resolve(ref: String?): String? {
            if (ref.isNullOrBlank()) return ref
            if (!ENTRY_PATTERN.matches(ref) || ref !in files) return ref
            val name = ref.substringAfter('/')
            val dir = if (ref.startsWith("$PAGES/")) pagesDir else signaturesDir
            return File(dir, name).absolutePath
        }
        return page.copy(
            imageUri = resolve(page.imageUri) ?: page.imageUri,
            originalImageUri = resolve(page.originalImageUri),
            signatureOverlayUri = resolve(page.signatureOverlayUri)
        )
    }

    private fun readManifest(zip: ZipFile): BackupManifest {
        val text = readEntry(zip, MANIFEST_ENTRY, maxBytes = MAX_MANIFEST_BYTES).decodeToString()
        val manifest = try {
            json.decodeFromString(BackupManifest.serializer(), text)
        } catch (t: Throwable) {
            throw BackupException("Backup manifest is unreadable", t)
        }
        if (manifest.format != FORMAT) {
            throw BackupException("Not a ${FORMAT} archive (found '${manifest.format}')")
        }
        if (manifest.formatVersion > FORMAT_VERSION) {
            throw BackupException("Backup format ${manifest.formatVersion} is newer than this app")
        }
        return manifest
    }

    private fun validate(zip: ZipFile, manifest: BackupManifest) {
        if (manifest.files.size > MAX_ENTRIES) {
            throw BackupException("Backup has too many files (${manifest.files.size})")
        }
        if (manifest.files.sumOf { it.bytes } > MAX_TOTAL_BYTES) {
            throw BackupException("Backup is too large")
        }

        val seen = HashSet<String>(manifest.files.size)
        manifest.files.forEach { file ->
            if (!ENTRY_PATTERN.matches(file.path)) {
                throw BackupException("Unsafe path in backup: ${file.path}")
            }
            if (!seen.add(file.path)) {
                throw BackupException("Duplicate entry in backup: ${file.path}")
            }
            if (file.bytes < 0 || file.bytes > MAX_ENTRY_BYTES) {
                throw BackupException("Bad size for ${file.path}")
            }
        }

        if (zip.getEntry(LIBRARY_ENTRY) == null) {
            throw BackupException("Archive has no $LIBRARY_ENTRY")
        }

        // Every entry in the zip must be one the manifest declared, and the two
        // reserved names. An unlisted entry is how a crafted archive smuggles files.
        zip.entries().asSequence().forEach { entry ->
            val name = entry.name
            if (name == MANIFEST_ENTRY || name == LIBRARY_ENTRY) return@forEach
            if (entry.isDirectory) throw BackupException("Unexpected directory: $name")
            if (name !in seen) throw BackupException("Undeclared entry in backup: $name")
        }

        manifest.files.forEach { file ->
            if (zip.getEntry(file.path) == null) {
                throw BackupException("Manifest lists a file that is not in the archive: ${file.path}")
            }
        }
    }

    private fun readEntry(zip: ZipFile, name: String, maxBytes: Long): ByteArray {
        val entry = zip.getEntry(name) ?: throw BackupException("Missing $name")
        if (entry.size > maxBytes) throw BackupException("$name is too large")
        zip.getInputStream(entry).use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(COPY_BUFFER)
            var total = 0L
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                total += read
                if (total > maxBytes) throw BackupException("$name is too large")
                out.write(buffer, 0, read)
            }
            return out.toByteArray()
        }
    }

    private fun hex(bytes: ByteArray): String {
        val digits = "0123456789abcdef"
        val builder = StringBuilder(bytes.size * 2)
        bytes.forEach { byte ->
            val value = byte.toInt() and 0xFF
            builder.append(digits[value ushr 4]).append(digits[value and 0x0F])
        }
        return builder.toString()
    }
}
