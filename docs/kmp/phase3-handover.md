# KMP Phase 3 — Screens, contracts, and Manga

**Branch:** `refactor/kmp-phase3-screens-and-contracts` (stacked on Phase 2)
**Status:** implementation complete and verified with a clean build
**Goal:** remove the last Java-serialization constraint from `Track`, move platform-neutral
contracts/preferences into `domain-common`, and move `Manga` plus its dependent models.

## Track screens

`TrackInfoDialog.kt` had six private Voyager screens whose constructor arguments included a
domain `Track` (`TrackStatusSelectorScreen`, `TrackChapterSelectorScreen`,
`TrackScoreSelectorScreen`, `TrackDateSelectorScreen`, `TrackDateRemoverScreen`,
`TrackerRemoveScreen`). They now take `trackId: Long` and load the track in their ScreenModel via
`GetTracks.awaitOne(trackId)`. Selector screens show `LoadingScreen` until the track is available;
the remove dialogs load on demand.

`Track` therefore no longer implements `java.io.Serializable` and now lives in `domain-common`.

## Manga and custom info

`Manga` now lives in `domain-common` with these changes:

- `GetCustomMangaInfo` is replaced by a `MangaCustomInfo` provider (`fun interface
  MangaCustomInfoProvider`). Android installs the repository-backed implementation in `App.kt`;
  iOS can install its own. `Manga.title/author/artist/thumbnailUrl/description/genre/status`
  keep resolving custom info through it.
- `updateStrategy` uses `source-api-common.model.UpdateStrategy`. The SQLDelight column type, the
  column adapter, backup models, library-update checks, and `SManga` conversions were updated.
- `expectedNextUpdate` is epoch millis (`Long?`) instead of `java.time.Instant`; the manga screen
  converts to `Instant` at the UI boundary.
- Removed the Compose `@Immutable` and Android `@SuppressLint` annotations.

Also moved with `Manga`: `MangaUpdate`, `MangaWithChapterCount`, `MergeMangaSettingsUpdate`,
`LibraryManga`, `MangaRepository`, `MangaMergeRepository`, and `ChapterSort`.

## Cover cache and history/updates

`MangaCover`'s process-local caches used `ConcurrentHashMap`/`HashMap`. They are now
`MutableStateFlow<Map<...>>` with accessor methods (`vibrantCoverColor(id)`,
`dominantCoverColor(id)`, `setCoverRatios`, `removeCoverData`, `...Snapshot`). This removed the last
JVM-only type from the cover model and made it safe to move.

Moved as a result: `MangaCover`, `HistoryWithRelations`, `UpdatesWithRelations`,
`HistoryRepository`, `UpdatesRepository`, and `StoragePreferences`. `FolderProvider` moved to
`core:common-kmp` (the Android implementation keeps its private `File` directory helper).

## Repositories and preferences moved to `domain-common`

- Repositories: `CategoryRepository`, `ChapterRepository`, `MangaRepository`,
  `MangaMergeRepository`, `LibraryUpdateErrorRepository`,
  `LibraryUpdateErrorWithRelationsRepository`, `LibraryUpdateErrorMessageRepository`,
  `CustomMangaRepository`, `FeedSavedSearchRepository`, `TrackRepository`
- Models: `CategoryUpdate`
- Preferences/services: `BackupPreferences`, `DownloadPreferences`, `HistoryPreferences`,
  `UpdatesPreferences`, `LibraryPreferences`, `StoragePreferences`, `AppUpdatePolicy`
- `LibraryGroup` constants moved; its `MR`/`SYMR` label mapping stays in the Android `domain`
  module as the `groupTypeStringRes` extension

Still Android-side: `MangaMetadataRepository` (source metadata types),
`FavoritesEntryRepository` (`FavoriteEntry` uses `EHentaiSearchMetadata`), `SavedSearchRepository`
(`EXHSavedSearch` uses the source ABI `FilterList`), `StubSourceRepository` (`StubSource`),
`SourceRepository` (`androidx.paging`), `StorageManager`, `SourceManager`.

## Follow-ups

- `Manga` lost `@Immutable`; consider a KMP-compatible stability annotation if Compose
  recomposition measurements show a regression.
- `Manga.toSManga()` still does not copy `updateStrategy` back to `SManga` (pre-existing behavior).
- `FavoriteEntry`/`EXHSavedSearch`/`StubSource` still depend on `source-api` ABI types; moving them
  requires a shared metadata/source abstraction.
- iOS targets and Kotlin/Native actuals still require a macOS host.

## Verification

```bash
./gradlew clean
./gradlew spotlessCheck
./gradlew assembleDebug
./gradlew testReleaseUnitTest jvmTest
./gradlew verifySqlDelightMigration
```

`domain-common/commonMain` must stay free of `android.*`, `androidx.*`, `java.*`, `kotlin.jvm.*`,
and Injekt imports.
