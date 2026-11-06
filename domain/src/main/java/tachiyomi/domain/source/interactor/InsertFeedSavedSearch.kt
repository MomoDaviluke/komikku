package tachiyomi.domain.source.interactor

import tachiyomi.core.common.util.system.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.source.model.FeedSavedSearch
import tachiyomi.domain.source.repository.FeedSavedSearchRepository

class InsertFeedSavedSearch(
    private val feedSavedSearchRepository: FeedSavedSearchRepository,
) {

    suspend fun await(feedSavedSearch: FeedSavedSearch): Long? {
        return try {
            feedSavedSearchRepository.insert(feedSavedSearch)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.stackTraceToString() }
            null
        }
    }

    suspend fun awaitAll(feedSavedSearch: List<FeedSavedSearch>) {
        try {
            feedSavedSearchRepository.insertAll(feedSavedSearch)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.stackTraceToString() }
        }
    }
}
