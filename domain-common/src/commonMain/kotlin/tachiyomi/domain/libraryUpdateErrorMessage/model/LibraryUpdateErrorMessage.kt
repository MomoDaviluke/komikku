package tachiyomi.domain.libraryUpdateErrorMessage.model

// KMK -->
// Platform-neutral model: Java serialization is Android-only and must not leak into commonMain.
// KMK <--
data class LibraryUpdateErrorMessage(
    val id: Long,
    val message: String,
)
