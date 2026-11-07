package tachiyomi.domain.manga.model

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.common.model.UpdateStrategy as CommonUpdateStrategy

// KMK -->
fun Manga.toMangaUpdate(): MangaUpdate {
    return MangaUpdate(
        id = id,
        source = source,
        favorite = favorite,
        lastUpdate = lastUpdate,
        nextUpdate = nextUpdate,
        fetchInterval = fetchInterval,
        dateAdded = dateAdded,
        viewerFlags = viewerFlags,
        chapterFlags = chapterFlags,
        coverLastModified = coverLastModified,
        url = url,
        // SY -->
        title = ogTitle,
        artist = ogArtist,
        author = ogAuthor,
        thumbnailUrl = ogThumbnailUrl,
        description = ogDescription,
        genre = ogGenre,
        status = ogStatus,
        // SY <--
        updateStrategy = updateStrategy,
        initialized = initialized,
        version = version,
        notes = notes,
        memo = memo,
    )
}

fun UpdateStrategy.toCommon(): CommonUpdateStrategy = when (this) {
    UpdateStrategy.ALWAYS_UPDATE -> CommonUpdateStrategy.ALWAYS_UPDATE
    UpdateStrategy.ONLY_FETCH_ONCE -> CommonUpdateStrategy.ONLY_FETCH_ONCE
}
// KMK <--
