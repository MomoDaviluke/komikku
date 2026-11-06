# domain-common

The platform-neutral first slice of `domain`: chapter model and chapter-domain logic.

## Contents

- `tachiyomi.domain.chapter.model.Chapter`
- `tachiyomi.domain.chapter.model.ChapterUpdate`
- `tachiyomi.domain.chapter.service.ChapterRecognition`
- `tachiyomi.domain.chapter.service.MissingChapters`

## Why

`Chapter` is already pure Kotlin (`kotlinx.serialization` + `JsonObject.EMPTY` from
`core:common-kmp`). Moving it here lets iOS share the chapter model and chapter-number logic.

`ChapterSort` still depends on `Manga`, which is Android/JVM-bound (`java.time.Instant`,
`java.io.Serializable`, Compose `@Immutable`, `source-api.UpdateStrategy`). It stays in `domain`
until `Manga` is made platform-neutral.

## Rules

- No `android.*`, `java.*`, Compose or `source-api` types.
- Same package/FQCN as before, so `domain` and its consumers keep working via
  `domain`'s `api(projects.domainCommon)` dependency.

## Verification

```bash
./gradlew :domain-common:jvmTest
./gradlew :domain:compileDebugKotlin
```
