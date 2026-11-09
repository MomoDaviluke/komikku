// KMK -->
package tachiyomi.source.local.bridge

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import tachiyomi.domain.source.bridge.toAndroid
import tachiyomi.domain.source.bridge.toCommon
import tachiyomi.source.local.LocalSource
import eu.kanade.tachiyomi.source.common.Source as CommonSource
import eu.kanade.tachiyomi.source.common.model.Filter as CommonFilter
import eu.kanade.tachiyomi.source.common.model.FilterList as CommonFilterList
import eu.kanade.tachiyomi.source.common.model.MangasPage as CommonMangasPage
import eu.kanade.tachiyomi.source.common.model.Page as CommonPage
import eu.kanade.tachiyomi.source.common.model.SChapter as CommonChapter
import eu.kanade.tachiyomi.source.common.model.SManga as CommonManga
import eu.kanade.tachiyomi.source.common.model.SMangaUpdate as CommonMangaUpdate

/**
 * Android-side adapter that exposes the frozen extension ABI [Source] as the platform-neutral
 * [CommonSource] contract.
 *
 * B1a transitional adapter: it lives in `androidMain` because `LocalSource` still depends on
 * `Context`, `AndroidXmlReader`, `core:archive` and `core-metadata`. Once B1b/B1c remove those
 * dependencies the adapter can move to `commonMain`.
 *
 * Scope: local source only. [filterListProvider] lets the local source keep its `OrderBy`
 * implementation (the generic Android `Filter.Sort` would be ignored by `LocalSource`).
 * Non-`Sort` filters are not mapped yet (B1b).
 */
class CommonLocalSource(
    private val delegate: Source,
    private val filterListProvider: (CommonFilter.Sort.Selection?) -> FilterList = { selection ->
        defaultSortFilters(delegate.getFilterList(), selection)
    },
) : CommonSource {

    override val id: Long get() = delegate.id

    override val name: String get() = delegate.name

    override val lang: String get() = delegate.lang

    override val supportsLatest: Boolean get() = delegate.supportsLatest

    override fun getFilterList(): CommonFilterList = delegate.getFilterList().toCommon()

    override suspend fun getPopularManga(page: Int): CommonMangasPage =
        delegate.getPopularManga(page).toCommon()

    override suspend fun getLatestUpdates(page: Int): CommonMangasPage =
        delegate.getLatestUpdates(page).toCommon()

    override suspend fun getSearchManga(
        page: Int,
        query: String,
        filters: CommonFilterList,
    ): CommonMangasPage {
        val selection = filters.filterIsInstance<CommonFilter.Sort>().firstOrNull()?.state
        return delegate.getSearchManga(page, query, filterListProvider(selection)).toCommon()
    }

    override suspend fun getMangaUpdate(
        manga: CommonManga,
        chapters: List<CommonChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): CommonMangaUpdate {
        val result: SMangaUpdate = delegate.getMangaUpdate(
            manga = manga.toAndroid(),
            chapters = chapters.map { it.toAndroid() },
            fetchDetails = fetchDetails,
            fetchChapters = fetchChapters,
        )
        return CommonMangaUpdate(result.manga.toCommon(), result.chapters.map { it.toCommon() })
    }

    override suspend fun getPageList(chapter: CommonChapter): List<CommonPage> =
        delegate.getPageList(chapter.toAndroid()).map { it.toCommon() }
}

/**
 * Copies the requested selection onto a freshly created ABI sort filter.
 *
 * Never writes into [defaults]: delegates may cache their filter list (or return a shared
 * instance), so mutating the received filter would leak the requested state into later calls.
 * The returned filter is a plain [Filter.Sort], which means a delegate whose own filters are
 * specialised (e.g. `LocalSource`'s `OrderBy`) must be wrapped through `LocalSource.asCommonSource()`
 * to keep source-specific sorting semantics.
 */
private fun defaultSortFilters(
    defaults: FilterList,
    selection: CommonFilter.Sort.Selection?,
): FilterList {
    if (selection == null) return defaults
    val sort = defaults.filterIsInstance<Filter.Sort>().firstOrNull() ?: return defaults
    return FilterList(
        object : Filter.Sort(
            sort.name,
            sort.values,
            Filter.Sort.Selection(selection.index, selection.ascending),
        ) {},
    )
}

/**
 * Common-contract view of [LocalSource]. Keeps `OrderBy.Popular`/`OrderBy.Latest` semantics by
 * building the filter list from the source's own instances (`LocalSource.filterListFor`).
 */
fun LocalSource.asCommonSource(): CommonSource = CommonLocalSource(this) { selection ->
    filterListFor(selection?.let { Filter.Sort.Selection(it.index, it.ascending) })
}
// KMK <--
