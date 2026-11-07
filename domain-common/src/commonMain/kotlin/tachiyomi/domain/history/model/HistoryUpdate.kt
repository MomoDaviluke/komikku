package tachiyomi.domain.history.model

// KMK --> Platform-neutral epoch millis
data class HistoryUpdate(
    val chapterId: Long,
    val readAt: Long,
    val sessionReadDuration: Long,
)
// KMK <--
