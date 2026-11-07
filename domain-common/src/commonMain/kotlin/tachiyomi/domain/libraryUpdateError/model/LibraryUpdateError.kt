package tachiyomi.domain.libraryUpdateError.model

// KMK -->
// Platform-neutral model: Java serialization is Android-only and must not leak into commonMain.
// KMK <--
data class LibraryUpdateError(
    val id: Long,
    val mangaId: Long,
    val messageId: Long,
    val lastUpdate: Long = 0L,
)
