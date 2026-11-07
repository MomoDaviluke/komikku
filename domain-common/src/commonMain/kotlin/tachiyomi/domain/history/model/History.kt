package tachiyomi.domain.history.model

// KMK --> Platform-neutral epoch millis
data class History(
    val id: Long,
    val chapterId: Long,
    val readAt: Long?,
    val readDuration: Long,
) {
    companion object {
        fun create() = History(
            id = -1L,
            chapterId = -1L,
            readAt = null,
            readDuration = -1L,
        )
    }
}
// KMK <--
