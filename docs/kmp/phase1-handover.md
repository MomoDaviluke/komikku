# KMP Phase 1 — Handover

**Branch:** `refactor/kmp-phase1-commonmain`
**Status:** implementation complete and verified with a clean build
**Goal:** extract platform-neutral code into KMP modules (`androidTarget` + `jvm`) without changing the Android extension ABI or user-facing behavior.

## Module map

| Module | Source sets | Contents |
|--------|-------------|----------|
| `core:common-kmp` | `commonMain`, `androidMain`, `jvmMain`, `commonTest` | Preferences (`Preference`, `PreferenceStore`, `InMemoryPreferenceStore`, `TriState`, `CheckboxState`), `Logger` + `LogPriority` (expect/actual), `SortUtil` (expect/actual Collator), `QuerySanitizer`, `JsonObject`/`List`/`StringBuilder` extensions, coroutine helpers |
| `core:file` | `commonMain`, `jvmMain`, `jvmTest` | `PlatformFile` / `PlatformFileSystem` backed by Okio streams; `JvmPlatformFile` for tests/previews |
| `domain-common` | `commonMain`, `commonTest` | `Chapter`, `ChapterUpdate`, `ChapterRecognition`, `MissingChapters` (same package as before) |
| `source-api-common` | `commonMain`, `commonTest` | Suspend-only `Source` contract and `SManga`/`SChapter`/`Page`/`Filter`/`FilterList`/`MangasPage`/`SMangaUpdate`/`UpdateStrategy` in `eu.kanade.tachiyomi.source.common.model` |

## Dependency wiring

```
app -> source-api-common
app -> source-local (api: source-api, core:file, unifile)
app -> core:common (api: core:common-kmp)
app -> domain (api: domain-common)
domain -> core:common
source-local/androidMain -> source-api, core:common, core:archive, core-metadata, domain
```

`source-api` (Android extension ABI) is intentionally **not** modified or re-exported by
`source-api-common`. The two model sets are bridged explicitly in
`app/src/main/java/eu/kanade/tachiyomi/source/bridge/CommonMappings.kt`.

## Key decisions

1. **Same-package moves for core/domain.** `core:common-kmp` and `domain-common` keep the original
   package names, so call sites only needed dependency/import fixes. This keeps Phase 1 mechanical.
2. **Parallel contract for sources.** The Android `source-api` models are frozen by the extension ABI
   (RxJava, `android.net.Uri`, `java.io.Serializable`). `source-api-common` is a separate suspend-only
   contract for future iOS clients; `ModelParityTest` guards against drift.
3. **`PlatformFile` instead of `UniFile` in `source-local/commonMain`.** Android SAF access lives in
   `UniFilePlatformFile` (`source-local/androidMain`). `openInputStream()`/`openOutputStream()` keep
   nullable `UniFile` semantics; `requireInputStream()`/`requireOutputStream()` are the throwing variants.
4. **Logger expect/actual.** `logcat.LogPriority` was replaced with `tachiyomi.core.common.util.system.LogPriority`.
   The `logcat` library remains only for the XLog bridge in `app`.
5. **Voyager screens must carry IDs/primitives.** `MangaNotesScreen` now takes `mangaId` and loads the
   manga itself. This deliberately diverges from upstream mihon#3523: `Manga` is no longer
   `java.io.Serializable`. `ScreenSerializationTest` covers the notes screen.
6. **Tests live in `commonTest`** for the KMP modules and are executed by both `testReleaseUnitTest`
   (Android target) and `jvmTest` (JVM target). CI now runs `./gradlew testReleaseUnitTest jvmTest`.

## Verification

Run before merge (keep `clean` in its own invocation):

```bash
./gradlew clean
./gradlew spotlessCheck
./gradlew assembleDebug
./gradlew testReleaseUnitTest jvmTest
./gradlew :source-local:testDebugUnitTest
```

`core:file`'s `jvmTest` is the only test task not reachable from `testReleaseUnitTest`; it is covered
by the `jvmTest` entry above.

If Spotless reports ktlint `NoClassDefFoundError: EditorConfigDefaults` or
`A non-empty set of 'ruleProviders' need to be provided`, the local Gradle dependency metadata cache
is stale; rerun the affected task with `--refresh-dependencies`.

Drive-by fix: `generateLocalesConfig` now declares its inputs/outputs and writes in `doLast`.
Previously it wrote during configuration, so `gradlew clean assembleDebug` in a single invocation
deleted the generated `xml/locales_config.xml` and failed resource linking.

## Known gaps / follow-ups

- `source-api-common` is not consumed by production code yet (bridge + tests only). Phase 2 should
  either make `source-local/commonMain` implement the common `Source` contract or document a sync policy.
- `Manga` is still Android-only (`androidx.compose.runtime.Immutable`, `java.time.Instant`, Injekt).
  It is the biggest blocker for moving the domain layer; extract `GetCustomMangaInfo` first.
- `Track`, `Category`, `LibraryUpdateError*` still implement `java.io.Serializable` and rely on the
  ProGuard keep rules in `app/proguard-rules.pro`. Audit usages before removing.
- `source-api-common` has no iOS target yet; Kotlin/Native verification requires a macOS host.
- `PlatformFileSystem` is scaffolding for the iOS implementation; only the JVM fake uses it today.

## Phase 2 candidates (recommended order)

1. Move platform-free domain models (`MangaUpdate`, `MergedMangaReference`, `FavoriteEntry`,
   `CustomMangaInfo`, `MangaWithChapterCount`, `SavedSearch`) into `domain-common`.
2. Replace `java.util.Date` in `History`/`HistoryUpdate` with `Long` or `kotlinx-datetime`.
3. Move repository interfaces and preference services that only depend on `core:common-kmp`.
4. Implement the common `Source` contract in `source-local/commonMain` with an Android adapter.
5. Add iOS targets and `PlatformFile`/`Logger` actuals on macOS CI.
