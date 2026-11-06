// KMK -->
@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.source.common.model

import kotlinx.serialization.json.JsonObject

/**
 * Platform-neutral chapter model for the shared KMP contract.
 *
 * Kept separate from [eu.kanade.tachiyomi.source.model.SChapter] (Android extension ABI).
 */
interface SChapter {

    var url: String

    var name: String

    var chapter_number: Float

    var scanlator: String?

    var date_upload: Long

    var memo: JsonObject

    fun copyFrom(other: SChapter) {
        name = other.name
        url = other.url
        date_upload = other.date_upload
        chapter_number = other.chapter_number
        scanlator = other.scanlator
        memo = other.memo
    }

    companion object {
        fun create(): SChapter = SChapterImpl()

        operator fun invoke(
            name: String,
            url: String,
            date_upload: Long = 0,
            chapter_number: Float = -1F,
            scanlator: String? = null,
        ): SChapter = create().apply {
            this.name = name
            this.url = url
            this.date_upload = date_upload
            this.chapter_number = chapter_number
            this.scanlator = scanlator
        }
    }
}
// KMK <--
