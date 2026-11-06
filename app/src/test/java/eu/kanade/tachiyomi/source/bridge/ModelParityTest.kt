// KMK -->
package eu.kanade.tachiyomi.source.bridge

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import eu.kanade.tachiyomi.source.common.model.Page as CommonPage
import eu.kanade.tachiyomi.source.common.model.SChapter as CommonChapter
import eu.kanade.tachiyomi.source.common.model.SManga as CommonManga
import eu.kanade.tachiyomi.source.common.model.UpdateStrategy as CommonUpdateStrategy
import eu.kanade.tachiyomi.source.model.Page as AndroidPage
import eu.kanade.tachiyomi.source.model.SChapter as AndroidChapter
import eu.kanade.tachiyomi.source.model.SManga as AndroidManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy as AndroidUpdateStrategy

/**
 * Guards the Android extension ABI models against drift from the platform-neutral KMP contract.
 * The two model sets are intentionally separate, so their public surface must stay in sync.
 */
class ModelParityTest {

    @Test
    fun `SManga contracts expose the same properties`() {
        getters(AndroidManga::class.java) shouldBe getters(CommonManga::class.java)
    }

    @Test
    fun `SChapter contracts expose the same properties`() {
        getters(AndroidChapter::class.java) shouldBe getters(CommonChapter::class.java)
    }

    @Test
    fun `Page common contract only omits the deprecated Android uri`() {
        (getters(AndroidPage::class.java) - getters(CommonPage::class.java)) shouldBe setOf("uri")
        (getters(CommonPage::class.java) - getters(AndroidPage::class.java)) shouldBe emptySet()
    }

    @Test
    fun `update strategies match`() {
        AndroidUpdateStrategy.entries.map { it.name } shouldBe CommonUpdateStrategy.entries.map { it.name }
    }

    private fun getters(type: Class<*>): Set<String> =
        type.methods
            .filter {
                it.parameterCount == 0 &&
                    it.name.startsWith("get") &&
                    it.name != "getClass" &&
                    '$' !in it.name
            }
            .map { it.name.removePrefix("get").replaceFirstChar { char -> char.lowercase() } }
            .toSet()
}
// KMK <--
