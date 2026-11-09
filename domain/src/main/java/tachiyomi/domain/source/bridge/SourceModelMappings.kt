// KMK -->
package tachiyomi.domain.source.bridge

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import tachiyomi.domain.manga.model.toAndroid
import tachiyomi.domain.manga.model.toCommon
import eu.kanade.tachiyomi.source.common.model.Filter as CommonFilter
import eu.kanade.tachiyomi.source.common.model.FilterList as CommonFilterList
import eu.kanade.tachiyomi.source.common.model.MangasPage as CommonMangasPage
import eu.kanade.tachiyomi.source.common.model.Page as CommonPage
import eu.kanade.tachiyomi.source.common.model.SChapter as CommonChapter
import eu.kanade.tachiyomi.source.common.model.SManga as CommonManga

/**
 * Bidirectional mapping between the Android extension ABI models (`source-api`) and the
 * platform-neutral contract models (`source-api-common`).
 *
 * The two model universes are intentionally separate: the ABI one is frozen for extensions,
 * while the common one is what iOS consumes. This bridge lives in `domain` (an Android library
 * that already depends on both) instead of `domain-common`, because the ABI models expose
 * `android.net.Uri` / `java.io.Serializable` / Compose annotations and cannot be referenced from
 * a `commonMain` source set.
 *
 * Known contract differences (deliberate, not accidental):
 * - [Page] loses the deprecated Android `uri` field; the common page only carries `url`/`imageUrl`.
 * - [MangasPage] loses the SY-only `MetadataMangasPage` payload (`RaisedSearchMetadata`, `nextKey`);
 *   local source only produces the base shape.
 * - [FilterList] maps `Filter.Sort` only for now; other filter types are skipped and must be
 *   mapped when a source exposing them is adapted (B1b).
 */

fun SManga.toCommon(): CommonManga = CommonManga.create().also {
    it.url = url
    it.title = title
    it.thumbnail_url = thumbnail_url
    it.artist = artist
    it.author = author
    it.status = status
    it.description = description
    it.genre = genre
    it.update_strategy = update_strategy.toCommon()
    it.initialized = initialized
    it.memo = memo
}

fun CommonManga.toAndroid(): SManga = SManga.create().also {
    it.url = url
    it.title = title
    it.thumbnail_url = thumbnail_url
    it.artist = artist
    it.author = author
    it.status = status
    it.description = description
    it.genre = genre
    it.update_strategy = update_strategy.toAndroid()
    it.initialized = initialized
    it.memo = memo
}

fun SChapter.toCommon(): CommonChapter = CommonChapter.create().also {
    it.url = url
    it.name = name
    it.chapter_number = chapter_number
    it.scanlator = scanlator
    it.date_upload = date_upload
    it.memo = memo
}

fun CommonChapter.toAndroid(): SChapter = SChapter.create().also {
    it.url = url
    it.name = name
    it.chapter_number = chapter_number
    it.scanlator = scanlator
    it.date_upload = date_upload
    it.memo = memo
}

/** Drops the deprecated Android `uri`; common page state is reconstructed by consumers. */
fun Page.toCommon(): CommonPage = CommonPage(
    index = index,
    url = url,
    imageUrl = imageUrl,
)

/** The reverse direction leaves `uri` null, matching the common contract. */
fun CommonPage.toAndroid(): Page = Page(
    index = index,
    url = url,
    imageUrl = imageUrl,
)

fun MangasPage.toCommon(): CommonMangasPage = CommonMangasPage(
    mangas = mangas.map { it.toCommon() },
    hasNextPage = hasNextPage,
)

fun CommonMangasPage.toAndroid(): MangasPage = MangasPage(
    mangas = mangas.map { it.toAndroid() },
    hasNextPage = hasNextPage,
)

fun FilterList.toCommon(): CommonFilterList = CommonFilterList(
    mapNotNull { it.toCommonOrNull() },
)

fun CommonFilterList.toAndroid(): FilterList = FilterList(
    mapNotNull { it.toAndroidOrNull() },
)

private fun Filter<*>.toCommonOrNull(): CommonFilter<*>? = when (this) {
    is Filter.Sort -> CommonMappedSort(
        name = name,
        values = values,
        selection = state?.let { CommonFilter.Sort.Selection(it.index, it.ascending) },
    )
    else -> null
}

private fun CommonFilter<*>.toAndroidOrNull(): Filter<*>? = when (this) {
    is CommonFilter.Sort -> MappedSort(
        name = name,
        values = values,
        selection = state?.let { Filter.Sort.Selection(it.index, it.ascending) },
    )
    else -> null
}

private class MappedSort(
    name: String,
    values: Array<String>,
    selection: Filter.Sort.Selection?,
) : Filter.Sort(name, values, selection)

private class CommonMappedSort(
    name: String,
    values: Array<String>,
    selection: CommonFilter.Sort.Selection?,
) : CommonFilter.Sort(name, values, selection)
// KMK <--
