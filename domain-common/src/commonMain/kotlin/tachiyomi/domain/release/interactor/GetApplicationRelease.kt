package tachiyomi.domain.release.interactor

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.util.system.currentTimeMillis
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.ReleaseService

// KMK -->
private const val CHECK_INTERVAL_MILLIS = 2L * 24 * 60 * 60 * 1000
private val NON_NUMERIC = "[^\\d.]".toRegex()
// KMK <--

class GetApplicationRelease(
    private val service: ReleaseService,
    private val preferenceStore: PreferenceStore,
) {

    private val lastChecked: Preference<Long> by lazy {
        preferenceStore.getLong(Preference.appStateKey("last_app_check"), 0)
    }

    suspend fun await(arguments: Arguments): Result {
        val now = currentTimeMillis()

        // Limit checks to once every 2 days at most
        val nextCheckTime = lastChecked.get() + CHECK_INTERVAL_MILLIS
        if (!arguments.forceCheck && now < nextCheckTime) {
            return Result.NoNewUpdate
        }

        // KMK -->
        val releases = service.releaseNotes(arguments)
            .filter {
                !it.preRelease &&
                    isNewVersion(
                        arguments.isPreview,
                        arguments.commitCount,
                        arguments.versionName,
                        it.version,
                    )
            }

        val latest = releases.getLatest() ?: return Result.NoNewUpdate
        // KMK <--

        lastChecked.set(now)

        // Check if latest version is different from current version
        val isNewVersion = isNewVersion(
            isPreview = arguments.isPreview,
            commitCount = arguments.commitCount,
            versionName = arguments.versionName,
            versionTag = latest.version,
        )
        return when {
            isNewVersion -> Result.NewUpdate(latest)
            else -> Result.NoNewUpdate
        }
    }

    // KMK -->
    suspend fun awaitReleaseNotes(arguments: Arguments): Result {
        val releases = service.releaseNotes(arguments)
            .filter { !it.preRelease }

        val latest = releases.getLatest() ?: return Result.NoNewUpdate
        return Result.NewUpdate(latest)
    }
    // KMK <--

    data class Arguments(
        val isFoss: Boolean,
        /** If current version is Preview (beta) build */
        val isPreview: Boolean,
        /** Commit count of current version */
        val commitCount: Int,
        /** Current version name, could be version tag (v0.1.2) or commit count (r1234) */
        val versionName: String,
        /** Repository name */
        val repository: String,
        /** Force check for new update */
        val forceCheck: Boolean = false,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}

/**
 * [isPreview] is if current version is Preview (beta) build
 *
 * [versionTag] is the version of new release
 *
 * Release (stable) version will compare with current's [versionName] ("v0.1.2")
 *
 * Preview (beta) version will compare with current's [commitCount] ("r1234")
 */
internal fun isNewVersion(
    isPreview: Boolean,
    commitCount: Int,
    versionName: String,
    versionTag: String,
): Boolean {
    // Removes prefixes like "r" or "v"
    val newVersion = versionTag.replace(NON_NUMERIC, "")
    return if (isPreview) {
        // Preview builds: based on releases in "komikku-app/komikku-preview" repo
        // tagged as something like "r1234"
        newVersion.toIntOrNull()?.let { it > commitCount } ?: false
    } else {
        // Release builds: based on releases in "komikku-app/komikku" repo
        // tagged as something like "v0.1.2"
        val oldVersion = versionName.replace(NON_NUMERIC, "")

        val newSemVer = newVersion.split(".").mapNotNull(String::toIntOrNull)
        val oldSemVer = oldVersion.split(".").mapNotNull(String::toIntOrNull)
        if (newSemVer.isEmpty() || oldSemVer.isEmpty()) return false

        // Compare segment-wise and treat missing segments as 0, so that e.g.
        // "v0.1" vs "v0.1.2" is not an update instead of crashing.
        val segmentCount = maxOf(newSemVer.size, oldSemVer.size)
        for (index in 0 until segmentCount) {
            val newPart = newSemVer.getOrElse(index) { 0 }
            val oldPart = oldSemVer.getOrElse(index) { 0 }
            if (newPart != oldPart) return newPart > oldPart
        }
        false
    }
}

// KMK -->
internal fun List<Release>.getLatest(): Release? {
    return firstOrNull()
        ?.copy(
            info = joinToString("\r-----\r") {
                "## ${it.version}\r\r" +
                    it.info
            },
        )
}
// KMK <--
