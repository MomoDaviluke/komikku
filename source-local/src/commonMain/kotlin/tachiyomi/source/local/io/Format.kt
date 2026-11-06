// KMK -->
package tachiyomi.source.local.io

import tachiyomi.core.file.PlatformFile
import tachiyomi.source.local.io.Archive.isSupported as isArchiveSupported

sealed interface Format {
    data class Directory(val file: PlatformFile) : Format
    data class Archive(val file: PlatformFile) : Format
    data class Epub(val file: PlatformFile) : Format

    class UnknownFormatException : Exception()

    companion object {

        fun valueOf(file: PlatformFile) = when {
            file.isDirectory -> Directory(file)
            file.extension.equals("epub", true) -> Epub(file)
            isArchiveSupported(file) -> Archive(file)
            else -> throw UnknownFormatException()
        }
    }
}
// KMK <--
