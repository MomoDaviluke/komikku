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
    private var provider: MangaCustomInfoProvider? = null

    fun install(provider: MangaCustomInfoProvider) {
        this.provider = provider
    }

    fun get(mangaId: Long): CustomMangaInfo? = provider?.get(mangaId)
}
// KMK <--
