package tachiyomi.domain.library.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR

// KMK -->
fun LibraryGroup.groupTypeStringRes(type: Int): StringResource {
    return when (type) {
        LibraryGroup.BY_STATUS -> MR.strings.status
        LibraryGroup.BY_SOURCE -> MR.strings.label_sources
        LibraryGroup.BY_TRACK_STATUS -> SYMR.strings.tracking_status
        LibraryGroup.UNGROUPED -> SYMR.strings.ungrouped
        else -> MR.strings.categories
    }
}
// KMK <--
