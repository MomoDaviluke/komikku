// KMK -->
package tachiyomi.core.file

import okio.BufferedSink
import okio.BufferedSource

/**
 * Platform-neutral file handle.
 *
 * Android wraps `UniFile` (SAF aware); JVM uses `java.nio` for tests; iOS will wrap `NSFileManager`.
 * Streams use Okio so the abstraction stays multiplatform and supports archives/large files.
 */
interface PlatformFile {

    val name: String?

    val uri: String

    val isDirectory: Boolean

    val isFile: Boolean

    val lastModified: Long

    val extension: String?
        // Matches `tachiyomi.core.common.storage.UniFile.extension` exactly.
        get() = name?.substringAfterLast('.')

    fun listFiles(): List<PlatformFile>

    fun findFile(name: String): PlatformFile?

    fun createFile(name: String): PlatformFile?

    fun createDirectory(name: String): PlatformFile?

    fun openSource(): BufferedSource?

    fun openSink(): BufferedSink?

    fun delete(): Boolean
}

/**
 * Entry point for the platform's local storage root (SAF tree, filesystem path, ...).
 */
interface PlatformFileSystem {
    fun baseDirectory(): PlatformFile?
}
// KMK <--
