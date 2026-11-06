// KMK -->
package tachiyomi.source.local.io

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import okio.BufferedSink
import okio.BufferedSource
import org.junit.jupiter.api.Test
import tachiyomi.core.file.PlatformFile

class FormatTest {

    @Test
    fun `archive support is case insensitive`() {
        Archive.isSupported(file("chapter.cbz")) shouldBe true
        Archive.isSupported(file("chapter.CBZ")) shouldBe true
        Archive.isSupported(file("chapter.zip")) shouldBe true
        Archive.isSupported(file("chapter.epub")) shouldBe false
        Archive.isSupported(file("chapter.txt")) shouldBe false
        Archive.isSupported(file(null)) shouldBe false
    }

    @Test
    fun `archive support keeps UniFile extension semantics`() {
        // `UniFile.extension` returns the whole name when there is no dot.
        Archive.isSupported(file("zip")) shouldBe true
    }

    @Test
    fun `directory resolves to Directory`() {
        Format.valueOf(file("manga", isDirectory = true)).shouldBeInstanceOf<Format.Directory>()
    }

    @Test
    fun `epub is detected case insensitively`() {
        Format.valueOf(file("chapter.EPUB")).shouldBeInstanceOf<Format.Epub>()
    }

    @Test
    fun `supported archive resolves to Archive`() {
        Format.valueOf(file("chapter.cbz")).shouldBeInstanceOf<Format.Archive>()
    }

    @Test
    fun `unknown format throws`() {
        shouldThrow<Format.UnknownFormatException> {
            Format.valueOf(file("chapter.txt"))
        }
    }

    private fun file(name: String?, isDirectory: Boolean = false): PlatformFile = TestFile(name, isDirectory)

    private class TestFile(
        override val name: String?,
        override val isDirectory: Boolean = false,
    ) : PlatformFile {
        override val uri: String = "test://${name.orEmpty()}"

        override val isFile: Boolean = !isDirectory

        override val lastModified: Long = 0L

        override fun listFiles(): List<PlatformFile> = emptyList()

        override fun findFile(name: String): PlatformFile? = null

        override fun createFile(name: String): PlatformFile? = null

        override fun createDirectory(name: String): PlatformFile? = null

        override fun openSource(): BufferedSource? = null

        override fun openSink(): BufferedSink? = null

        override fun delete(): Boolean = true
    }
}
// KMK <--
