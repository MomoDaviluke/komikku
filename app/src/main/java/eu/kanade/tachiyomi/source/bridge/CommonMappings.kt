// KMK -->
package eu.kanade.tachiyomi.source.bridge

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.common.model.SChapter as CommonChapter
import eu.kanade.tachiyomi.source.common.model.SManga as CommonManga
import eu.kanade.tachiyomi.source.common.model.UpdateStrategy as CommonUpdateStrategy

/**
 * Thin mapping between the Android extension ABI models and the shared KMP contract models.
 *
 * The two model sets are intentionally separate: the Android one is frozen by the extension ABI,
 * while the shared one is platform-neutral. Keeping the mapping explicit makes the cost of the
 * split visible and prevents accidental coupling.
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

fun UpdateStrategy.toCommon(): CommonUpdateStrategy = when (this) {
    UpdateStrategy.ALWAYS_UPDATE -> CommonUpdateStrategy.ALWAYS_UPDATE
    UpdateStrategy.ONLY_FETCH_ONCE -> CommonUpdateStrategy.ONLY_FETCH_ONCE
}

fun CommonUpdateStrategy.toAndroid(): UpdateStrategy = when (this) {
    CommonUpdateStrategy.ALWAYS_UPDATE -> UpdateStrategy.ALWAYS_UPDATE
    CommonUpdateStrategy.ONLY_FETCH_ONCE -> UpdateStrategy.ONLY_FETCH_ONCE
}
// KMK <--
