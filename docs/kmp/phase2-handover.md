# KMP Phase 2 — Domain models

**Branch:** `refactor/kmp-phase2-domain-models` (stacked on Phase 1)
**Status:** implementation complete and verified with a clean build
**Goal:** move platform-neutral domain models into `domain-common` and remove JVM-only types from them.

## Moved to `domain-common`

Same packages as before, so call sites only needed dependency changes (already provided by
`domain`'s `api(projects.domainCommon)`):

- `category`: `Category`
- `chapter`: `NoChaptersException`
- `history`: `History`, `HistoryUpdate`
- `library`: `Flag`/`Mask`/`FlagWithMask`, `GroupLibraryMode`, `LibraryDisplayMode`, `LibrarySort`
- `libraryUpdateError`: `LibraryUpdateError`, `LibraryUpdateErrorWithRelations`
- `libraryUpdateErrorMessage`: `LibraryUpdateErrorMessage`
- `manga`: `CustomMangaInfo`, `FavoriteEntryAlternative`, `MergedMangaReference`, `applyFilter`
- `source`: `FeedSavedSearch`, `FeedSavedSearchUpdate`, `Pin`/`Pins`, `SavedSearch`, `Source`, `SourceWithCount`

## Serialization changes

- Removed `java.io.Serializable` from `Category`, `LibraryUpdateError`, and `LibraryUpdateErrorMessage`.
  None of them is carried by a Voyager `Screen`.
- `Track` intentionally stays in `domain` with `java.io.Serializable`: `TrackInfoDialog.kt` has
  private Voyager screens whose constructor arguments include `Track`. Moving it requires an
  ID-based screen refactor first.
- `Manga` remains Android-only (Compose `@Immutable`, `java.time.Instant`, Injekt); see blockers.

## History timestamps

`History.readAt`, `HistoryUpdate.readAt`, and `HistoryWithRelations.readAt` are now epoch millis
(`Long`) instead of `java.util.Date`. The SQLDelight column still uses `INTEGER AS Date`, so the
conversion happens at the data boundary:

- `HistoryMapper`: `Date.time` when mapping rows
- `HistoryRepositoryImpl` / `MangaRestorer`: `Date(epochMillis)` when upserting
- backup `BackupHistory` continues to store `Long`
- UI uses `Long.toLocalDate()` / the new `Long.toTimestampString()`

## Remaining blockers (Phase 3 candidates)

| Model | Blocker |
|-------|---------|
| `Manga` | `androidx.compose.runtime.Immutable`, `java.time.Instant`, Injekt-backed custom info |
| `MangaUpdate` | Android ABI `UpdateStrategy` from `source-api` |
| `MangaCover` | Injekt + `ConcurrentHashMap` caches |
| `MangaWithChapterCount`, `LibraryManga` | hold `Manga` |
| `HistoryWithRelations`, `UpdatesWithRelations` | Injekt + `Manga` |
| `Track` | Voyager screens carry `Track` directly |
| `FavoriteEntry` | `EHentaiSearchMetadata` from `source-api` |
| `EXHSavedSearch`, `StubSource` | Android ABI `FilterList` / `Source` |
| `LibraryGroup` | moko `StringResource` + `i18n` modules |

Next steps after this phase: refactor the track selector screens to IDs, extract
`GetCustomMangaInfo` out of `Manga`, then move `Manga`/`MangaUpdate` and finally make
`source-local/commonMain` implement the common `Source` contract.

## Verification

```bash
./gradlew clean
./gradlew spotlessCheck
./gradlew assembleDebug
./gradlew testReleaseUnitTest jvmTest
```

If Spotless reports ktlint `NoClassDefFoundError` or `ruleProviders`, run the failing module with
`--refresh-dependencies`; the local Gradle metadata cache can go stale.
