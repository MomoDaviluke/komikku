// KMK -->
package tachiyomi.domain.manga.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MangaCoverTest {

    private fun mangaCover(id: Long) = MangaCover(
        mangaId = id,
        sourceId = 1L,
        isMangaFavorite = false,
        ogUrl = null,
        lastModified = 0L,
    )

    @Test
    fun removeFavoriteCoverDataKeepsVibrantColor() {
        val mangaId = 910001L
        val cover = mangaCover(mangaId)
        cover.vibrantCoverColor = 0x112233
        cover.dominantCoverColors = 0x445566 to 0x778899
        cover.ratio = 1.5f

        MangaCover.removeFavoriteCoverData(mangaId)

        assertEquals(0x112233, cover.vibrantCoverColor)
        assertNull(cover.dominantCoverColors)
        assertNull(cover.ratio)
    }

    @Test
    fun removeCoverDataClearsEverything() {
        val mangaId = 910002L
        val cover = mangaCover(mangaId)
        cover.vibrantCoverColor = 0x112233
        cover.dominantCoverColors = 0x445566 to 0x778899
        cover.ratio = 1.5f

        MangaCover.removeCoverData(mangaId)

        assertNull(cover.vibrantCoverColor)
        assertNull(cover.dominantCoverColors)
        assertNull(cover.ratio)
    }
}
// KMK <--
