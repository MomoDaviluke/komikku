# KMP Phase 3 — Screens, repositories, preferences

**Branch:** `refactor/kmp-phase3-screens-and-contracts` (stacked on Phase 2)
**Status:** implementation complete and verified with a clean build
**Goal:** remove the remaining Java-serialization constraint from `Track`, and move
platform-neutral repository contracts and preference services into `domain-common`.

## Track screens

`TrackInfoDialog.kt` had six private Voyager screens whose constructor arguments included a
domain `Track` (`TrackStatusSelectorScreen`, `TrackChapterSelectorScreen`,
`TrackScoreSelectorScreen`, `TrackDateSelectorScreen`, `TrackDateRemoverScreen`,
`TrackerRemoveScreen`). They now take `trackId: Long` and load the track in their ScreenModel via
`GetTracks.awaitOne(trackId)`. Selector screens show `LoadingScreen` until the track is available;
the remove dialogs load on demand.

`Track` therefore no longer implements `java.io.Serializable` and now lives in `domain-common`.

## Repositories and preferences moved to `domain-common`

- Repositories: `CategoryRepository`, `ChapterRepository`, `LibraryUpdateErrorRepository`,
  `LibraryUpdateErrorWithRelationsRepository`, `LibraryUpdateErrorMessageRepository`,
  `CustomMangaRepository`, `FeedSavedSearchRepository`, `TrackRepository`
- Model: `CategoryUpdate`
- Preferences/services: `BackupPreferences`, `DownloadPreferences`, `HistoryPreferences`,
  `UpdatesPreferences`, `AppUpdatePolicy`

Still Android-side because they depend on `Manga`, `FolderProvider`, `androidx.paging`, or
`source-api` ABI types: `MangaRepository`, `MangaMergeRepository`, `MangaMetadataRepository`,
`FavoritesEntryRepository`, `HistoryRepository`, `UpdatesRepository`, `SavedSearchRepository`,
`StubSourceRepository`, `SourceRepository`, `LibraryPreferences`, `ChapterSort`,
`StoragePreferences`.

## Manga design proposal (next)

`Manga` is the main remaining blocker. Proposed sequence:

1. Replace the Injekt `GetCustomMangaInfo` lookup inside `Manga` with a
   `MangaCustomInfoProvider` interface in `domain-common`; Android installs its implementation at
   startup (the existing `GetCustomMangaInfo` becomes the adapter). This preserves the
   `title`/`author`/`thumbnailUrl`/... override behavior on Android and gives iOS a platform hook.
2. Change `Manga.updateStrategy` to the common `source-api-common.model.UpdateStrategy` and map at
   the `SManga` boundary (`SManga.toDomainManga`, `Manga.toSManga`) and in the DB mapper.
3. Replace `expectedNextUpdate: Instant?` with epoch millis (`Long?`) or `kotlinx-datetime`, and
   update the upcoming-screen consumers.
4. Drop `@Immutable` from `Manga` (or add Compose runtime to `domain-common`); it is a stability
   hint, not behavior.
5. Move `Manga`, then `MangaUpdate`, `MangaWithChapterCount`, `LibraryManga`, `MangaCover`,
   `HistoryWithRelations`, and `UpdatesWithRelations` (the last three also need their Injekt
   `GetCustomMangaInfo` usages replaced by the provider).

## Verification

```bash
./gradlew clean
./gradlew spotlessCheck
./gradlew assembleDebug
./gradlew testReleaseUnitTest jvmTest
```

`domain-common/commonMain` must stay free of `android.*`, `androidx.*`, `java.*`, `kotlin.jvm.*`,
and Injekt imports.
