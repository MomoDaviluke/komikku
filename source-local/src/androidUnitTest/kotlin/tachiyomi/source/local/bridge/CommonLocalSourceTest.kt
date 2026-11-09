// KMK -->
package tachiyomi.source.local.bridge

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import eu.kanade.tachiyomi.source.model.copy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import eu.kanade.tachiyomi.source.common.model.Filter as CommonFilter
import eu.kanade.tachiyomi.source.common.model.FilterList as CommonFilterList
import eu.kanade.tachiyomi.source.common.model.SChapter as CommonChapter
import eu.kanade.tachiyomi.source.common.model.SManga as CommonManga

class CommonLocalSourceTest {

    private val delegate = FakeAndroidSource()
    private val adapter = CommonLocalSource(delegate)

    @Test
    fun `identity and browse delegate and map to common models`() = runBlocking<Unit> {
        adapter.id shouldBe 7L
        adapter.name shouldBe "Fake"
        adapter.lang shouldBe "en"
        adapter.supportsLatest shouldBe true

        val popular = adapter.getPopularManga(1)
        popular.hasNextPage shouldBe true
        popular.mangas.single().title shouldBe "Popular"

        val latest = adapter.getLatestUpdates(1)
        latest.hasNextPage shouldBe false
        latest.mangas.single().title shouldBe "Latest"
        Unit
    }

    @Test
    fun `search maps the common sort selection onto the abi filter list`() = runBlocking<Unit> {
        val requested = CommonFilterList(
            TestCommonSort("Order by", arrayOf("Title", "Date"), CommonFilter.Sort.Selection(1, false)),
        )

        val result = adapter.getSearchManga(1, "query", requested)

        result.mangas.single().title shouldBe "query"
        val received = delegate.receivedFilters?.single() as Filter.Sort
        received.state shouldBe Filter.Sort.Selection(1, false)
        Unit
    }

    @Test
    fun `search does not mutate the delegate's cached filter list`() = runBlocking<Unit> {
        val cachedSort = delegate.cachedFilters.single() as Filter.Sort
        cachedSort.state shouldBe null

        adapter.getSearchManga(
            1,
            "first",
            CommonFilterList(
                TestCommonSort(
                    "Order by",
                    arrayOf("Title", "Date"),
                    CommonFilter.Sort.Selection(1, false),
                ),
            ),
        )
        val firstReceived = delegate.receivedFilters?.single() as Filter.Sort
        firstReceived.state shouldBe Filter.Sort.Selection(1, false)

        adapter.getSearchManga(
            1,
            "second",
            CommonFilterList(
                TestCommonSort(
                    "Order by",
                    arrayOf("Title", "Date"),
                    CommonFilter.Sort.Selection(0, false),
                ),
            ),
        )

        cachedSort.state shouldBe null
        firstReceived.state shouldBe Filter.Sort.Selection(1, false)
        Unit
    }

    @Test
    fun `manga update maps both directions`() = runBlocking<Unit> {
        val manga = CommonManga.create().apply {
            url = "/manga"
            title = "Before"
        }
        val chapter = CommonChapter.create().apply {
            url = "/manga/1"
            name = "1"
        }

        val result = adapter.getMangaUpdate(manga, listOf(chapter), fetchDetails = true, fetchChapters = true)

        result.manga.title shouldBe "Updated"
        result.manga.url shouldBe "/manga"
        result.chapters.single().name shouldBe "1"
        Unit
    }

    @Test
    fun `page list maps each page`() = runBlocking<Unit> {
        val chapter = CommonChapter.create().apply {
            url = "/manga/1"
            name = "1"
        }

        val pages = adapter.getPageList(chapter)

        pages.single().url shouldBe "/manga/1"
        pages.single().index shouldBe 0
        Unit
    }

    @Test
    fun `filter list maps sort filters to the common contract`() {
        val filters = adapter.getFilterList()

        val sort = filters.single() as CommonFilter.Sort
        sort.name shouldBe "Order by"
        sort.values.toList() shouldBe listOf("Title", "Date")
        Unit
    }

    @Test
    fun `page list failures are propagated instead of being swallowed`() = runBlocking<Unit> {
        delegate.pageListFailure = UnsupportedOperationException("Unused")

        shouldThrow<UnsupportedOperationException> {
            adapter.getPageList(
                CommonChapter.create().apply {
                    url = "/manga/1"
                    name = "1"
                },
            )
        }
        Unit
    }

    private class FakeAndroidSource : Source {
        override val id = 7L
        override val name = "Fake"
        override val lang = "en"
        override val supportsLatest = true

        var receivedFilters: FilterList? = null
        var pageListFailure: Throwable? = null
        val cachedFilters: FilterList =
            FilterList(TestAndroidSort("Order by", arrayOf("Title", "Date"), null))

        override fun getFilterList(): FilterList = cachedFilters

        override suspend fun getPopularManga(page: Int): MangasPage =
            MangasPage(listOf(manga("Popular")), hasNextPage = true)

        override suspend fun getLatestUpdates(page: Int): MangasPage =
            MangasPage(listOf(manga("Latest")), hasNextPage = false)

        override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage {
            receivedFilters = filters
            return MangasPage(listOf(manga(query)), hasNextPage = false)
        }

        override suspend fun getMangaUpdate(
            manga: SManga,
            chapters: List<SChapter>,
            fetchDetails: Boolean,
            fetchChapters: Boolean,
        ): SMangaUpdate = SMangaUpdate(manga.copy(title = "Updated"), chapters)

        override suspend fun getPageList(chapter: SChapter): List<Page> {
            pageListFailure?.let { throw it }
            return listOf(Page(0, chapter.url))
        }

        private fun manga(title: String) = SManga.create().apply {
            url = "/manga"
            this.title = title
        }
    }

    private class TestAndroidSort(
        name: String,
        values: Array<String>,
        selection: Filter.Sort.Selection?,
    ) : Filter.Sort(name, values, selection)

    private class TestCommonSort(
        name: String,
        values: Array<String>,
        selection: CommonFilter.Sort.Selection?,
    ) : CommonFilter.Sort(name, values, selection)
}
// KMK <--
