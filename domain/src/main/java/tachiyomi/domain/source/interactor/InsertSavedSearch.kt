package tachiyomi.domain.source.interactor

import tachiyomi.core.common.util.system.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.source.model.SavedSearch
import tachiyomi.domain.source.repository.SavedSearchRepository

class InsertSavedSearch(
    private val savedSearchRepository: SavedSearchRepository,
) {

    suspend fun await(savedSearch: SavedSearch): Long? {
        return try {
            savedSearchRepository.insert(savedSearch)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.stackTraceToString() }
            null
        }
    }

    suspend fun awaitAll(savedSearch: List<SavedSearch>) {
        try {
            savedSearchRepository.insertAll(savedSearch)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.stackTraceToString() }
        }
    }
}
