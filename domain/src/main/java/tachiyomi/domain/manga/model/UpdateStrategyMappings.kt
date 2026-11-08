// KMK -->
package tachiyomi.domain.manga.model

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.common.model.UpdateStrategy as CommonUpdateStrategy

/**
 * Single source of truth for mapping the frozen Android extension ABI enum to the
 * platform-neutral contract enum (and back).
 *
 * The Android enum must stay ABI-stable for extensions, while shared/KMP code uses the
 * common enum. Keeping both directions in one place avoids the duplicate mappings that
 * previously lived in the app bridge and in [MangaUpdateMapper].
 */
fun UpdateStrategy.toCommon(): CommonUpdateStrategy = when (this) {
    UpdateStrategy.ALWAYS_UPDATE -> CommonUpdateStrategy.ALWAYS_UPDATE
    UpdateStrategy.ONLY_FETCH_ONCE -> CommonUpdateStrategy.ONLY_FETCH_ONCE
}

fun CommonUpdateStrategy.toAndroid(): UpdateStrategy = when (this) {
    CommonUpdateStrategy.ALWAYS_UPDATE -> UpdateStrategy.ALWAYS_UPDATE
    CommonUpdateStrategy.ONLY_FETCH_ONCE -> UpdateStrategy.ONLY_FETCH_ONCE
}
// KMK <--
