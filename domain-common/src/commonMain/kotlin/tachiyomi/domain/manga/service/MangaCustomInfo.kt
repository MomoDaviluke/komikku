// KMK -->
package tachiyomi.domain.manga.service

import tachiyomi.domain.manga.model.CustomMangaInfo

/**
 * Bridge for platform-specific custom manga metadata.
 *
 * Android installs the repository-backed implementation at startup; iOS will install its own.
 */
fun interface MangaCustomInfoProvider {
    fun get(mangaId: Long): CustomMangaInfo?
}

object MangaCustomInfo {
    @Volatile
    private var provider: MangaCustomInfoProvider? = null

    @Volatile
    private var installed: Boolean = false

    /**
     * Whether a platform provider has been installed.
     *
     * Callers that must distinguish "no custom metadata" from "bridge not wired yet" can check
     * this instead of treating a null result as authoritative.
     */
    val isInstalled: Boolean
        get() = installed

    fun install(provider: MangaCustomInfoProvider) {
        this.provider = provider
        this.installed = true
    }

    fun get(mangaId: Long): CustomMangaInfo? = provider?.get(mangaId)
}
// KMK <--
