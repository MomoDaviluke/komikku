// KMK -->
package tachiyomi.domain.source.bridge

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import tachiyomi.domain.manga.model.toAndroid
import tachiyomi.domain.manga.model.toCommon
import eu.kanade.tachiyomi.source.common.model.Filter as CommonFilter
import eu.kanade.tachiyomi.source.common.model.UpdateStrategy as CommonUpdateStrategy

class SourceModelMappingsTest {

    @Test
    fun `manga round trip preserves every field`() {
        val memo = JsonObject(mapOf("key" to JsonPrimitive("value")))
        val manga = SManga.create().apply {
            url = "/manga"
            title = "Title"
            thumbnail_url = "https://example.com/cover.jpg"
            artist = "Artist"
            author = "Author"
            status = SManga.COMPLETED
            description = "Description"
            genre = "Action, Drama"
            update_strategy = UpdateStrategy.ONLY_FETCH_ONCE
            initialized = true
            this.memo = memo
        }

        val roundTrip = manga.toCommon().toAndroid()

        roundTrip.url shouldBe "/manga"
        roundTrip.title shouldBe "Title"
        roundTrip.thumbnail_url shouldBe "https://example.com/cover.jpg"
        roundTrip.artist shouldBe "Artist"
        roundTrip.author shouldBe "Author"
        roundTrip.status shouldBe SManga.COMPLETED
        roundTrip.description shouldBe "Description"
        roundTrip.genre shouldBe "Action, Drama"
        roundTrip.update_strategy shouldBe UpdateStrategy.ONLY_FETCH_ONCE
        roundTrip.initialized shouldBe true
        roundTrip.memo shouldBe memo
    }

    @Test
    fun `chapter round trip preserves every field`() {
        val memo = JsonObject(mapOf("key" to JsonPrimitive("value")))
        val chapter = SChapter.create().apply {
            url = "/chapter/1"
            name = "Chapter 1"
            chapter_number = 1.5f
            scanlator = "Scanlator"
            date_upload = 123456789L
            this.memo = memo
        }

        val roundTrip = chapter.toCommon().toAndroid()

        roundTrip.url shouldBe "/chapter/1"
        roundTrip.name shouldBe "Chapter 1"
        roundTrip.chapter_number shouldBe 1.5f
        roundTrip.scanlator shouldBe "Scanlator"
        roundTrip.date_upload shouldBe 123456789L
        roundTrip.memo shouldBe memo
    }

    @Test
    fun `update strategy maps in both directions`() {
        UpdateStrategy.ALWAYS_UPDATE.toCommon() shouldBe CommonUpdateStrategy.ALWAYS_UPDATE
        UpdateStrategy.ONLY_FETCH_ONCE.toCommon() shouldBe CommonUpdateStrategy.ONLY_FETCH_ONCE

        CommonUpdateStrategy.ALWAYS_UPDATE.toAndroid() shouldBe UpdateStrategy.ALWAYS_UPDATE
        CommonUpdateStrategy.ONLY_FETCH_ONCE.toAndroid() shouldBe UpdateStrategy.ONLY_FETCH_ONCE
    }

    @Test
    fun `page round trip keeps index url and image url`() {
        val page = Page(index = 3, url = "/chapter/1/page-1", imageUrl = "https://example.com/p1.jpg")

        val common = page.toCommon()
        common.index shouldBe 3
        common.url shouldBe "/chapter/1/page-1"
        common.imageUrl shouldBe "https://example.com/p1.jpg"

        val roundTrip = common.toAndroid()
        roundTrip.index shouldBe 3
        roundTrip.url shouldBe "/chapter/1/page-1"
        roundTrip.imageUrl shouldBe "https://example.com/p1.jpg"
        // ABI `uri` is deliberately dropped by the common contract.
        roundTrip.uri shouldBe null
    }

    @Test
    fun `mangas page round trip keeps items and has next page`() {
        val manga = SManga.create().apply {
            url = "/m"
            title = "M"
        }
        val page = MangasPage(listOf(manga), hasNextPage = true)

        val common = page.toCommon()
        common.hasNextPage shouldBe true
        common.mangas.single().title shouldBe "M"

        val roundTrip = common.toAndroid()
        roundTrip.hasNextPage shouldBe true
        roundTrip.mangas.single().url shouldBe "/m"
    }

    @Test
    fun `filter list sort round trip keeps name values and selection`() {
        val sort = TestSort("Order by", arrayOf("Title", "Date"), Filter.Sort.Selection(1, false))
        val filters = FilterList(sort)

        val common = filters.toCommon()
        val commonSort = common.single() as CommonFilter.Sort
        commonSort.name shouldBe "Order by"
        commonSort.values.toList() shouldBe listOf("Title", "Date")
        commonSort.state shouldBe CommonFilter.Sort.Selection(1, false)

        val roundTrip = common.toAndroid()
        val androidSort = roundTrip.single() as Filter.Sort
        androidSort.name shouldBe "Order by"
        androidSort.values.toList() shouldBe listOf("Title", "Date")
        androidSort.state shouldBe Filter.Sort.Selection(1, false)
    }

    private class TestSort(
        name: String,
        values: Array<String>,
        selection: Filter.Sort.Selection?,
    ) : Filter.Sort(name, values, selection)
}
// KMK <--
