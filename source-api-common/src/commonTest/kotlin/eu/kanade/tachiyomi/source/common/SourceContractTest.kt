// KMK -->
package eu.kanade.tachiyomi.source.common

import eu.kanade.tachiyomi.source.common.model.FilterList
import eu.kanade.tachiyomi.source.common.model.MangasPage
import eu.kanade.tachiyomi.source.common.model.Page
import eu.kanade.tachiyomi.source.common.model.SChapter
import eu.kanade.tachiyomi.source.common.model.SManga
import eu.kanade.tachiyomi.source.common.model.SMangaUpdate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SourceContractTest {

    @Test
    fun relatedMangasFromExtensionArePushed() = runTest {
        val source = FakeSource(
            supportsRelatedMangas = true,
            disableRelatedMangasBySearch = true,
            related = listOf(manga("related-1"), manga("related-2")),
        )
        val results = mutableListOf<Pair<String, List<SManga>>>()

        source.getRelatedMangaList(
            manga = manga("Title"),
            exceptionHandler = { throw it },
            pushResults = { pair, _ -> results.add(pair) },
        )

        assertEquals(1, results.size)
        assertEquals("", results.single().first)
        assertEquals(2, results.single().second.size)
    }

    @Test
    fun relatedMangasBySearchUseSanitizedKeywords() = runTest {
        val source = FakeSource(
            supportsRelatedMangas = false,
            disableRelatedMangasBySearch = false,
            search = mapOf(
                "alpha" to listOf(manga("alpha-result")),
                "beta" to listOf(manga("beta-result")),
            ),
        )
        val results = mutableListOf<Pair<String, List<SManga>>>()

        source.getRelatedMangaList(
            manga = manga("alpha - beta"),
            exceptionHandler = { throw it },
            pushResults = { pair, _ -> results.add(pair) },
        )

        assertEquals(setOf("alpha", "beta"), results.map { it.first }.toSet())
        assertTrue(results.all { it.second.isNotEmpty() })
    }

    @Test
    fun disabledRelatedMangasProduceNothing() = runTest {
        val source = FakeSource(
            supportsRelatedMangas = true,
            disableRelatedMangas = true,
            related = listOf(manga("ignored")),
        )
        val results = mutableListOf<Pair<String, List<SManga>>>()

        source.getRelatedMangaList(
            manga = manga("Title"),
            exceptionHandler = { throw it },
            pushResults = { pair, _ -> results.add(pair) },
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun stripKeywordsDropsShortAndNumericOnlyTokens() {
        with(FakeSource()) {
            assertEquals(listOf("hello", "world"), "Hello 12 - world!".stripKeywordForRelatedMangas())
        }
    }

    private fun manga(title: String) = SManga.create().apply {
        url = "/$title"
        this.title = title
    }

    private class FakeSource(
        override val id: Long = 1L,
        override val name: String = "Fake",
        override val supportsLatest: Boolean = true,
        override val supportsRelatedMangas: Boolean = false,
        override val disableRelatedMangasBySearch: Boolean = false,
        override val disableRelatedMangas: Boolean = false,
        private val related: List<SManga> = emptyList(),
        private val search: Map<String, List<SManga>> = emptyMap(),
    ) : Source {
        override suspend fun getPopularManga(page: Int): MangasPage = MangasPage(emptyList(), false)

        override suspend fun getLatestUpdates(page: Int): MangasPage = MangasPage(emptyList(), false)

        override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage =
            MangasPage(search[query].orEmpty(), false)

        override suspend fun getMangaUpdate(
            manga: SManga,
            chapters: List<SChapter>,
            fetchDetails: Boolean,
            fetchChapters: Boolean,
        ): SMangaUpdate = SMangaUpdate(manga, chapters)

        override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()

        override suspend fun fetchRelatedMangaList(manga: SManga): List<SManga> = related
    }
}
// KMK <--
