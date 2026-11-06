# core:file

Platform-neutral file abstraction for the KMP/iOS work.

## Contents

- `PlatformFile` — file/directory handle with metadata (`name`, `uri`, `isDirectory`, `isFile`,
  `lastModified`, `extension`) and operations (`listFiles`, `findFile`, `createFile`,
  `createDirectory`, `openSource`, `openSink`, `delete`).
- `PlatformFileSystem` — entry point for the platform storage root.
- `JvmPlatformFile` / `JvmPlatformFileSystem` (`jvmMain`) — `java.io.File` implementation used by
  tests and non-Android JVM tooling.

Streams use **Okio** (`BufferedSource`/`BufferedSink`) so the abstraction is multiplatform and
supports archives and large files without loading them fully into memory.

## Implementations

| Platform | Adapter | Location |
|---|---|---|
| Android (SAF) | `UniFilePlatformFile` | `source-local/src/androidMain/.../io/UniFilePlatformFile.kt` |
| JVM (tests) | `JvmPlatformFile` | this module's `jvmMain` |
| iOS | to be added on macOS | — |

Android archives/covers still need the underlying `UniFile`; `requireUniFile()` is an
Android-only bridge used by those call sites. Do not move it into this module.

## Rules

- No `android.*`, `androidx.*`, `com.hippo.*` or `java.io.File` in `commonMain`.
- Add platform behavior through implementations, not `expect`/`actual` typealiases.
- Every new operation must have a JVM test in `jvmTest`.
