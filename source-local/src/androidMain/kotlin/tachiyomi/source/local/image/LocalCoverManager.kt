// KMK -->
package tachiyomi.source.local.image

import android.content.Context
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.storage.DiskUtil
import mihon.core.archive.ZipWriter
import tachiyomi.core.common.util.system.ImageUtil
import tachiyomi.core.file.PlatformFile
import tachiyomi.source.local.io.LocalSourceFileSystem
import tachiyomi.source.local.io.requireInputStream
import tachiyomi.source.local.io.requireOutputStream
import tachiyomi.source.local.io.requireUniFile
import java.io.InputStream

private const val DEFAULT_COVER_NAME = "cover.jpg"
private const val COVER_ARCHIVE_NAME = "cover.cbi"

/**
 * Android implementation of local cover handling. It depends on SAF (`UniFile`), the Android
 * `ZipWriter` (encryption) and `DiskUtil`, so it intentionally stays in androidMain.
 */
class LocalCoverManager(
    private val context: Context,
    private val fileSystem: LocalSourceFileSystem,
) {

    fun find(mangaUrl: String): PlatformFile? {
        return fileSystem.getFilesInMangaDirectory(mangaUrl)
            // Get all file whose names start with "cover"
            .filter { it.isFile && it.name?.substringBeforeLast('.')?.equals("cover", ignoreCase = true) == true }
            // Get the first actual image
            .firstOrNull {
                ImageUtil.isImage(it.name) { it.requireInputStream() } || it.name == COVER_ARCHIVE_NAME
            }
    }

    fun update(
        manga: SManga,
        inputStream: InputStream,
        // SY -->
        encrypted: Boolean = false,
        // SY <--
    ): PlatformFile? {
        val directory = fileSystem.getMangaDirectory(manga.url)
        if (directory == null) {
            inputStream.close()
            return null
        }

        var targetFile = find(manga.url)
        if (targetFile == null) {
            // SY -->
            targetFile = if (encrypted) {
                directory.createFile(COVER_ARCHIVE_NAME)
            } else {
                directory.createFile(DEFAULT_COVER_NAME)
            }
            // SY <--
        }

        val file = targetFile ?: run {
            inputStream.close()
            return null
        }

        inputStream.use { input ->
            // SY -->
            if (encrypted) {
                ZipWriter(context, file.requireUniFile(), encrypt = true).use { writer ->
                    writer.write(inputStream.readBytes(), DEFAULT_COVER_NAME)
                }
                DiskUtil.createNoMediaFile(directory.requireUniFile(), context)

                manga.thumbnail_url = file.uri
                return file
            } else {
                // SY <--
                file.requireOutputStream().use { out -> input.copyTo(out) }
                DiskUtil.createNoMediaFile(directory.requireUniFile(), context)
                manga.thumbnail_url = file.uri
                return file
            }
        }
    }
}
// KMK <--
