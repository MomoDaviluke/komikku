// KMK -->
package tachiyomi.domain.manga.service

import tachiyomi.domain.manga.model.CustomMangaInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MangaCustomInfoTest {

    @Test
    fun installedProviderIsResolved() {
        val info = CustomMangaInfo(id = 42L, title = "custom title")
        MangaCustomInfo.install { mangaId -> info.takeIf { mangaId == 42L } }

        assertTrue(MangaCustomInfo.isInstalled)
        assertEquals("custom title", MangaCustomInfo.get(42L)?.title)
        assertNull(MangaCustomInfo.get(7L))
    }
}
// KMK <--
