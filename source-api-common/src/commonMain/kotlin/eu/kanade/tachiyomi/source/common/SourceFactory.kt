// KMK -->
package eu.kanade.tachiyomi.source.common

/**
 * A factory for creating sources at runtime.
 */
interface SourceFactory {
    fun createSources(): List<Source>
}
// KMK <--
