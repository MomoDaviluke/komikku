// KMK -->
package tachiyomi.source.local.io

import com.hippo.unifile.UniFile
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import tachiyomi.core.file.PlatformFile
import java.io.InputStream
import java.io.OutputStream

/**
 * Android adapter for [PlatformFile] backed by SAF-aware [UniFile].
 */
class UniFilePlatformFile(val uniFile: UniFile) : PlatformFile {

    override val name: String? get() = uniFile.name

    override val uri: String get() = uniFile.uri.toString()

    override val isDirectory: Boolean get() = uniFile.isDirectory

    override val isFile: Boolean get() = uniFile.isFile

    override val lastModified: Long get() = uniFile.lastModified()

    override fun listFiles(): List<PlatformFile> =
        uniFile.listFiles().orEmpty().map(::UniFilePlatformFile)

    override fun findFile(name: String): PlatformFile? =
        uniFile.findFile(name)?.let(::UniFilePlatformFile)

    override fun createFile(name: String): PlatformFile? =
        uniFile.createFile(name)?.let(::UniFilePlatformFile)

    override fun createDirectory(name: String): PlatformFile? =
        uniFile.createDirectory(name)?.let(::UniFilePlatformFile)

    override fun openSource(): BufferedSource? =
        uniFile.openInputStream()?.source()?.buffer()

    override fun openSink(): BufferedSink? =
        uniFile.openOutputStream()?.sink()?.buffer()

    override fun delete(): Boolean = uniFile.delete()
}

/**
 * Android-only access to the underlying [UniFile] for archive/cover APIs that still require it.
 */
fun PlatformFile.requireUniFile(): UniFile =
    (this as? UniFilePlatformFile)?.uniFile
        ?: error("Expected a UniFile-backed PlatformFile")

/**
 * Nullable variants preserve the old `UniFile` stream semantics for call sites that can skip
 * unreadable files instead of failing the whole operation.
 */
fun PlatformFile.openInputStream(): InputStream? = openSource()?.inputStream()

fun PlatformFile.openOutputStream(): OutputStream? = openSink()?.outputStream()

fun PlatformFile.requireInputStream(): InputStream =
    openInputStream() ?: error("Cannot open $uri for reading")

fun PlatformFile.requireOutputStream(): OutputStream =
    openOutputStream() ?: error("Cannot open $uri for writing")
// KMK <--
