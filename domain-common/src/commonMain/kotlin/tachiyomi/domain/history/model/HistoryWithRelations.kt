package tachiyomi.domain.history.model

import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.manga.service.MangaCustomInfo

data class HistoryWithRelations(
    val id: Long,
    val chapterId: Long,
    val mangaId: Long,
    // SY -->
    val ogTitle: String,
    // SY <--
    val chapterNumber: Double,
    // KMK -->
    val read: Boolean,
    val lastPageRead: Long,
    val totalCountCalculated: Long,
    val readCountCalculated: Long,
    // KMK <--
    // KMK --> Platform-neutral epoch millis
    val readAt: Long?,
    // KMK <--
    val readDuration: Long,
    val coverData: MangaCover,
) {
    // SY -->
    val title: String = MangaCustomInfo.get(mangaId)?.title ?: ogTitle
    // SY <--

    // KMK -->
    val unreadCount
        get() = totalCountCalculated - readCountCalculated
    // KMK <--
}
