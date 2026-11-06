# source-api-common

Shared, platform-neutral source contract for Komikku's KMP/iOS work.

## Why this module exists

`source-api` is the **Android extension ABI**. Its public surface is frozen by third-party
extension APKs and is built on RxJava, OkHttp, `android.net.Uri`, Injekt and
`androidx.compose.runtime`. It cannot be compiled for Kotlin/Native without either breaking the
ABI or introducing fake `expect`/`actual` types.

This module is the opposite: a clean, suspend-only contract that iOS can consume directly, while
Android adapts to/from it with explicit, cheap mapping.

## Boundaries

Belongs here:

- Pure Kotlin models: `SManga`, `SChapter`, `Page`, `MangasPage`, `SMangaUpdate`, `Filter`, `FilterList`.
- Suspend-only `Source` / `CatalogueSource` / `SourceFactory` / `UnmeteredSource`.
- `kotlinx.serialization` and `kotlinx.coroutines` only.

Does **not** belong here:

- RxJava, OkHttp, `android.*`, `androidx.*`, Injekt, Jsoup, Compose annotations.
- Anything from the Android extension ABI (`source-api`).

## Relationship to `source-api`

The two model sets are deliberately separate and must not be merged:

| Android ABI (`source-api`) | Shared contract (`source-api-common`) |
|---|---|
| `eu.kanade.tachiyomi.source.model.SManga` | `eu.kanade.tachiyomi.source.common.model.SManga` |
| RxJava `Observable` compatibility methods | suspend-only methods |
| `Page.uri: android.net.Uri?` | no URI field |
| `java.io.Serializable` | plain Kotlin interfaces |

Android maps between them in `app/.../eu/kanade/tachiyomi/source/bridge/CommonMappings.kt`.
The bridge lives in `app` (the composition root) so `source-api`'s published dependency graph and
extension ABI stay untouched.

## Targets

- `androidTarget()` — consumed by the app.
- `jvm()` — enables real common-code compilation and unit tests on Windows/Linux CI without a Mac.
- iOS targets should be added on a macOS host; the code here already contains no platform types.

## Verification

```bash
./gradlew :source-api-common:jvmTest
./gradlew :source-api-common:compileKotlinJvm
./gradlew :app:compileDebugKotlin
```

## Roadmap

1. Add iOS targets and link the framework on macOS.
2. Move shared, platform-neutral utilities (e.g. query sanitization) into a `core-common` KMP
   module and delete the local copy here.
3. Grow the contract only with features iOS actually implements; do not mirror Android-only APIs.
