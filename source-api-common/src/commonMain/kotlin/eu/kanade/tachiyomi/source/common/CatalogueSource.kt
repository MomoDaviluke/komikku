// KMK -->
package eu.kanade.tachiyomi.source.common

/**
 * A source that can be browsed by catalogue.
 *
 * In the shared contract the `Source` API is already suspend-only, so this only adds the
 * requirement that a catalogued source exposes its language.
 */
interface CatalogueSource : Source {
    override val lang: String
}
// KMK <--
