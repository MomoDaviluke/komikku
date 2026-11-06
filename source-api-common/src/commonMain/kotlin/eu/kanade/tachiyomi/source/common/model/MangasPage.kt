// KMK -->
package eu.kanade.tachiyomi.source.common.model

open class MangasPage(
    open val mangas: List<SManga>,
    open val hasNextPage: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MangasPage) return false

        if (mangas != other.mangas) return false
        if (hasNextPage != other.hasNextPage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mangas.hashCode()
        result = 31 * result + hasNextPage.hashCode()
        return result
    }

    override fun toString(): String = "MangasPage(mangas=$mangas, hasNextPage=$hasNextPage)"

    operator fun component1(): List<SManga> = mangas

    operator fun component2(): Boolean = hasNextPage

    fun copy(
        mangas: List<SManga> = this.mangas,
        hasNextPage: Boolean = this.hasNextPage,
    ): MangasPage = MangasPage(mangas, hasNextPage)
}
// KMK <--
