package tachiyomi.domain.manga.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import tachiyomi.domain.manga.service.MangaCustomInfo

/**
 * Contains the required data for MangaCoverFetcher
 */
data class MangaCover(
    val mangaId: Long,
    val sourceId: Long,
    val isMangaFavorite: Boolean,
    // SY -->
    val ogUrl: String?,
    // SY <--
    val lastModified: Long,
) {
    // SY -->
    private val customThumbnailUrl = if (isMangaFavorite) {
        MangaCustomInfo.get(mangaId)?.thumbnailUrl
    } else {
        null
    }
    val url: String? = customThumbnailUrl ?: ogUrl
    // SY <--

    // KMK -->

    /**
     * [vibrantCoverColor] is used to set the color theme in manga detail page.
     * It contains color for all mangas, both in library or browsing.
     *
     * It reads/saves to a process-local cache for multiple mangas.
     */
    var vibrantCoverColor: Int?
        get() = vibrantCoverColors.value[mangaId]
        set(value) {
            vibrantCoverColors.update { it + (mangaId to value) }
        }

    /**
     * [dominantCoverColors] is used to set cover/text's color in Library (Favorite) grid view.
     * It contains only color for in-library (favorite) mangas.
     *
     * Format: <first: cover color, second: text color>.
     *
     * Set in *[MangaCoverMetadata.setRatioAndColors]* whenever browsing meets a favorite manga
     *  by loading from *[CoverCache]*.
     *
     * Get in *[CommonMangaItem.MangaCompactGridItem]*, *[CommonMangaItem.MangaComfortableGridItem]* and
     *  *[CommonMangaItem.MangaListItem]*
     */
    @Suppress("KDocUnresolvedReference")
    var dominantCoverColors: Pair<Int, Int>?
        get() = dominantCoverColorsMap.value[mangaId]
        set(value) {
            value ?: return
            dominantCoverColorsMap.update { it + (mangaId to value) }
        }

    var ratio: Float?
        get() = coverRatios.value[mangaId]
        set(value) {
            value ?: return
            coverRatios.update { it + (mangaId to value) }
        }

    companion object {
        /**
         * Stores color generated while browsing library.
         * It is always empty at the start of each app process.
         */
        private val vibrantCoverColors = MutableStateFlow<Map<Long, Int?>>(emptyMap())

        /**
         * Stores favorite manga's cover & text colors. They are loaded from preferences by
         * *[MangaCoverMetadata.load]* and saved back when *[MainActivity.onPause]* is triggered.
         */
        @Suppress("KDocUnresolvedReference")
        private val dominantCoverColorsMap = MutableStateFlow<Map<Long, Pair<Int, Int>>>(emptyMap())

        private val coverRatios = MutableStateFlow<Map<Long, Float>>(emptyMap())

        fun vibrantCoverColor(mangaId: Long): Int? = vibrantCoverColors.value[mangaId]

        fun dominantCoverColor(mangaId: Long): Pair<Int, Int>? = dominantCoverColorsMap.value[mangaId]

        fun setDominantCoverColors(values: Map<Long, Pair<Int, Int>>) {
            dominantCoverColorsMap.value = values
        }

        fun setCoverRatios(values: Map<Long, Float>) {
            coverRatios.value = values
        }

        fun removeCoverData(mangaId: Long) {
            vibrantCoverColors.update { it - mangaId }
            dominantCoverColorsMap.update { it - mangaId }
            coverRatios.update { it - mangaId }
        }

        fun coverRatiosSnapshot(): Map<Long, Float> = coverRatios.value

        fun dominantCoverColorsSnapshot(): Map<Long, Pair<Int, Int>> = dominantCoverColorsMap.value
    }
    // KMK <--
}

fun Manga.asMangaCover(): MangaCover {
    return MangaCover(
        mangaId = id,
        sourceId = source,
        isMangaFavorite = favorite,
        ogUrl = thumbnailUrl,
        lastModified = coverLastModified,
    )
}
