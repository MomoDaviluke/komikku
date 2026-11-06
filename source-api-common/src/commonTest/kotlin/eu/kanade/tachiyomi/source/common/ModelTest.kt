// KMK -->
package eu.kanade.tachiyomi.source.common

import eu.kanade.tachiyomi.source.common.model.FilterList
import eu.kanade.tachiyomi.source.common.model.MangasPage
import eu.kanade.tachiyomi.source.common.model.SChapter
import eu.kanade.tachiyomi.source.common.model.SManga
import eu.kanade.tachiyomi.source.common.model.copy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelTest {

    @Test
    fun mangaDefaultsAndCopy() {
        val manga = SManga.create().apply {
            url = "https://example.com/manga"
            title = "Example"
        }

        assertEquals("Example", manga.originalTitle)
        assertEquals(listOf("a", "b"), manga.copy(genre = "a, b").getGenres())
    }

    @Test
    fun chapterCreate() {
        val chapter = SChapter(name = "Chapter 2", url = "/2", chapter_number = 2f)

        assertEquals(2f, chapter.chapter_number)
        assertEquals("Chapter 2", chapter.name)
    }

    @Test
    fun mangasPageEquality() {
        assertEquals(MangasPage(emptyList(), false), MangasPage(emptyList(), false))
    }

    @Test
    fun filterListBehavesAsList() {
        val filters = FilterList()

        assertTrue(filters.isEmpty())
    }

    @Test
    fun querySanitizerTrimsAndReplaces() {
        assertEquals("test", with(QuerySanitizer) { " --test-- ".sanitize() })
        assertEquals("it's ok", with(QuerySanitizer) { " it’s ok ".sanitize() })
    }
}
// KMK <--
