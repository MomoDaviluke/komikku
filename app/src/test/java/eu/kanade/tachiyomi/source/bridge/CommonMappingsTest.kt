// KMK -->
package eu.kanade.tachiyomi.source.bridge

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import eu.kanade.tachiyomi.source.common.model.UpdateStrategy as CommonUpdateStrategy

class CommonMappingsTest {

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
}
// KMK <--
