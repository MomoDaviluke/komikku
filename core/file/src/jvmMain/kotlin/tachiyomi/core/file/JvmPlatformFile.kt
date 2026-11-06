// KMK -->
package tachiyomi.core.file

import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import java.io.File

/**
 * JVM implementation backed by `java.io.File`. Used for unit tests and non-Android JVM tooling.
 */
class JvmPlatformFile(private val delegate: File) : PlatformFile {

    override val name: String? get() = delegate.name

    override val uri: String get() = delegate.toURI().toString()

    override val isDirectory: Boolean get() = delegate.isDirectory

    override val isFile: Boolean get() = delegate.isFile

    override val lastModified: Long get() = delegate.lastModified()

    override fun listFiles(): List<PlatformFile> =
        delegate.listFiles().orEmpty().map(::JvmPlatformFile)

    override fun findFile(name: String): PlatformFile? =
        File(delegate, name).takeIf { it.exists() }?.let(::JvmPlatformFile)

    override fun createFile(name: String): PlatformFile? {
        val file = File(delegate, name)
        return when {
            file.isFile -> JvmPlatformFile(file)
            file.exists() -> null
            runCatching { file.createNewFile() }.getOrDefault(false) -> JvmPlatformFile(file)
            else -> null
        }
    }

    override fun createDirectory(name: String): PlatformFile? {
        val directory = File(delegate, name)
        return when {
            directory.isDirectory -> JvmPlatformFile(directory)
            directory.mkdirs() -> JvmPlatformFile(directory)
            else -> null
        }
    }

    override fun openSource(): BufferedSource? =
        delegate.takeIf { it.isFile }?.source()?.buffer()

    override fun openSink(): BufferedSink? =
        delegate.takeIf { !it.isDirectory && it.parentFile?.exists() != false }?.sink()?.buffer()

    override fun delete(): Boolean = delegate.delete()
}

class JvmPlatformFileSystem(baseDirectory: File?) : PlatformFileSystem {
    private val base = baseDirectory?.takeIf { it.isDirectory }?.let(::JvmPlatformFile)

    override fun baseDirectory(): PlatformFile? = base
}
// KMK <--
