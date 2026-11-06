package tachiyomi.domain.track.interactor

import tachiyomi.core.common.util.system.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.track.repository.TrackRepository

class DeleteTrack(
    private val trackRepository: TrackRepository,
) {

    suspend fun await(mangaId: Long, trackerId: Long) {
        try {
            trackRepository.delete(mangaId, trackerId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }
}
