// KMK -->
@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.source.common.model

import kotlinx.serialization.json.JsonObject

/**
 * Platform-neutral manga model for the shared KMP contract.
 *
 * This is intentionally separate from [eu.kanade.tachiyomi.source.model.SManga], which belongs to
 * the Android extension ABI. Android adapters map between the two; iOS consumes this model directly.
 */
interface SManga {

    var url: String

    var title: String

    var thumbnail_url: String?

    var artist: String?

    var author: String?

    var status: Int

    var description: String?

    var genre: String?

    var update_strategy: UpdateStrategy

    var initialized: Boolean

    var memo: JsonObject

    fun getGenres(): List<String>? {
        if (genre.isNullOrBlank()) return null
        return genre?.split(", ")?.map { it.trim() }?.filterNot { it.isBlank() }?.distinct()
    }

    val originalTitle: String
    val originalAuthor: String?
    val originalArtist: String?
    val originalThumbnailUrl: String?
    val originalDescription: String?
    val originalGenre: String?
    val originalStatus: Int

    fun copy() = create().also {
        it.url = url
        it.title = originalTitle
        it.artist = originalArtist
        it.author = originalAuthor
        it.thumbnail_url = originalThumbnailUrl
        it.description = originalDescription
        it.genre = originalGenre
        it.status = originalStatus
        it.update_strategy = update_strategy
        it.initialized = initialized
        it.memo = memo
    }

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
        const val ON_HIATUS = 6

        fun create(): SManga = SMangaImpl()
    }
}

fun SManga.copy(
    url: String = this.url,
    title: String = this.originalTitle,
    artist: String? = this.originalArtist,
    author: String? = this.originalAuthor,
    description: String? = this.originalDescription,
    genre: String? = this.originalGenre,
    status: Int = this.status,
    thumbnail_url: String? = this.originalThumbnailUrl,
    initialized: Boolean = this.initialized,
    memo: JsonObject = this.memo,
) = SManga.create().also {
    it.url = url
    it.title = title
    it.artist = artist
    it.author = author
    it.description = description
    it.genre = genre
    it.status = status
    it.thumbnail_url = thumbnail_url
    it.initialized = initialized
    it.memo = memo
}
// KMK <--
