// KMK -->
package tachiyomi.source.local.io

import tachiyomi.core.file.PlatformFile

class LocalSourceFileSystem(
    private val baseDirectoryProvider: () -> PlatformFile?,
) {

    fun getBaseDirectory(): PlatformFile? = baseDirectoryProvider()

    fun getFilesInBaseDirectory(): List<PlatformFile> =
        getBaseDirectory()?.listFiles().orEmpty()

    fun getMangaDirectory(name: String): PlatformFile? =
        getBaseDirectory()?.findFile(name)?.takeIf { it.isDirectory }

    fun getFilesInMangaDirectory(name: String): List<PlatformFile> =
        getMangaDirectory(name)?.listFiles().orEmpty()
}
// KMK <--
