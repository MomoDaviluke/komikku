package tachiyomi.domain.track.model

// KMK -->
// Platform-neutral model: Java serialization is Android-only and must not leak into commonMain.
// KMK <--
data class Track(
    val id: Long,
    val mangaId: Long,
    val trackerId: Long,
    val remoteId: Long,
    val libraryId: Long?,
    val title: String,
    val lastChapterRead: Double,
    val totalChapters: Long,
    val status: Long,
    val score: Double,
    val remoteUrl: String,
    val startDate: Long,
    val finishDate: Long,
    val private: Boolean,
)
