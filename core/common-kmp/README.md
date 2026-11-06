# core:common-kmp

The platform-neutral subset of `core:common`.

## Why

`core:common` is an Android library with OkHttp, QuickJS, SAF, Android `Bitmap`, WebView and
`logcat`. Moving all of it to KMP at once is not on the critical path for the local-reader iOS
slice. This module extracts only the pieces that are already pure Kotlin and needed by
`domain`/`data`.

## Contents

- `tachiyomi.core.common.preference`: `Preference`, `PreferenceStore`, `TriState`,
  `InMemoryPreferenceStore`, `CheckboxState`
- `tachiyomi.core.common.util.QuerySanitizer`
- `tachiyomi.core.common.util.lang.BooleanExtensions`
- `mihon.core.common.extensions.JsonObject` (`EMPTY`)
- `exh.util.ListUtil`, `exh.util.StringBuilderExtensions`

## Rules

- No `android.*`, `androidx.*`, `java.*`, OkHttp, UniFile or `logcat`.
- Platform-bound helpers (`CoroutinesExtensions` with dispatchers, `SortUtil` with `Collator`,
  `Localize` with `Context`, `FolderProvider` with `java.io.File`, `RxCoroutineBridge`) stay in
  `core:common` until their expect/actual or interface design is settled.
- `core:common` depends on this module with `api`, so existing consumers keep working.

## Verification

```bash
./gradlew :core:common-kmp:jvmTest
./gradlew :core:common:compileDebugKotlin
```
