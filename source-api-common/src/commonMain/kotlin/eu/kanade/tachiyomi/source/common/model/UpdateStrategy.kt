// KMK -->
package eu.kanade.tachiyomi.source.common.model

/**
 * Define the update strategy for a single [SManga].
 * The strategy used will only take effect on the library update.
 */
enum class UpdateStrategy {
    /**
     * Series marked as always update will be included in the library
     * update if they aren't excluded by additional restrictions.
     */
    ALWAYS_UPDATE,

    /**
     * Series marked as only fetch once will be automatically skipped
     * during library updates.
     */
    ONLY_FETCH_ONCE,
}
// KMK <--
