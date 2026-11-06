// KMK -->
package tachiyomi.core.file

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlatformFileTest {

    @Test
    fun readWriteAndMetadata() {
        val root = createTempDirectory("core-file").toFile()
        try {
            val base = assertNotNull(JvmPlatformFileSystem(root).baseDirectory())

            val directory = assertNotNull(base.createDirectory("manga"))
            assertTrue(directory.isDirectory)
            assertFalse(directory.isFile)

            val chapter = assertNotNull(directory.createFile("chapter-1.cbz"))
            assertTrue(chapter.isFile)
            assertEquals("cbz", chapter.extension)

            assertNotNull(chapter.openSink()).use { it.writeUtf8("hello") }
            assertEquals("hello", assertNotNull(chapter.openSource()).use { it.readUtf8() })
            assertTrue(chapter.lastModified > 0L)

            assertEquals(listOf("chapter-1.cbz"), directory.listFiles().map { it.name })
            assertNotNull(directory.findFile("chapter-1.cbz"))
            assertNull(directory.findFile("missing.cbz"))

            assertTrue(chapter.delete())
            assertFalse(chapter.isFile)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun createFileIsIdempotentAndCreatesOnDemand() {
        val root = createTempDirectory("core-file").toFile()
        try {
            val base = assertNotNull(JvmPlatformFileSystem(root).baseDirectory())

            val first = assertNotNull(base.createFile("file.txt"))
            assertNotNull(first.openSink()).use { it.writeUtf8("a") }

            val second = assertNotNull(base.createFile("file.txt"))
            assertEquals(first.uri, second.uri)
            assertEquals("a", assertNotNull(second.openSource()).use { it.readUtf8() })
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun extensionMatchesUniFileSemantics() {
        val root = createTempDirectory("core-file").toFile()
        try {
            val base = assertNotNull(JvmPlatformFileSystem(root).baseDirectory())

            // `UniFile.extension` returns the whole name when there is no dot.
            assertEquals("README", assertNotNull(base.createFile("README")).extension)
            assertEquals("cbz", assertNotNull(base.createFile("chapter.cbz")).extension)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun createFileRejectsDirectories() {
        val root = createTempDirectory("core-file").toFile()
        try {
            val base = assertNotNull(JvmPlatformFileSystem(root).baseDirectory())
            val directory = assertNotNull(base.createDirectory("existing"))

            assertNull(base.createFile("existing"))
            assertNull(directory.openSink())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun nullBaseDirectoryIsSupported() {
        assertNull(JvmPlatformFileSystem(null).baseDirectory())
        assertNull(JvmPlatformFileSystem(File("does-not-exist")).baseDirectory()?.createFile("x"))
    }
}
// KMK <--
