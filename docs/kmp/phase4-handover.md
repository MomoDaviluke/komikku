# Phase 4 Handover — KMP 深化（2026-11-09 更新）

> 基线：`master@383ffaa81` + 18 commits（`refactor/kmp-common@89d5b97e8`）。
> 当前工作分支：`feat/kmp-phase4-d1b1`（从 89d5b97e8 起，已推 `588a27e08`，本轮增量待提交）。
> 前置阅读：`.workbuddy/PROJECT-STATUS-2026-11-08.md`（事实底座）、
> `.workbuddy/PHASE4-DECISIONS.md`（D1–D4 决策）、`.workbuddy/CODE-REVIEW-2026-11-08.md`（审查结论）、
> `.workbuddy/DEV-PLAN-2026-11-09.md`（Sprint A/B/C 计划）。

## 1. 本轮做了什么

### 1.1 D1-B1：8 个纯 Kotlin 文件迁入 commonMain

`git mv` 到 `core/common-kmp/src/commonMain/kotlin`（包名不变，调用点零改动）：

| 文件 | 目标包 |
|------|--------|
| `ProgressListener.kt` | `eu.kanade.tachiyomi.network` |
| `NetworkPreferences.kt` | `eu.kanade.tachiyomi.network` |
| `PrivacyPreferences.kt` | `eu.kanade.tachiyomi.core.security` |
| `DelegateSourcePreferences.kt` | `exh.pref` |
| `Constants.kt` | `tachiyomi.core.common` |
| `UrlUtils.kt` | `tachiyomi.core.common.util.system` |
| `FeatureFlags.kt` | `mihon.core.common` |
| `Set.kt` | `mihon.core.common.utils` |

`core/common/build.gradle.kts:23` 已有 `api(projects.core.commonKmp)`，依赖方向不变。
`CoroutinesExtensions.kt` 未迁：与 common-kmp 的 `IoCoroutinesExtensions` 重名，需先合并语义。

### 1.2 commonMain 平台 API 泄漏修复

新增时钟抽象：

- `core/common-kmp/src/commonMain/kotlin/tachiyomi/core/common/util/system/Clock.kt`
  ```kotlin
  expect fun currentTimeMillis(): Long
  ```
- `androidMain/.../Clock.android.kt`、`jvmMain/.../Clock.jvm.kt` 均为 `actual = System.currentTimeMillis()`。
- `GetApplicationRelease.kt` 改调 `currentTimeMillis()`（原直接 `System.currentTimeMillis()`）。

### 1.3 审查修复（阻断项收口）

- `domain-common/.../GetApplicationRelease.kt`：`isNewVersion` 段数取 max、缺段按 0、`toIntOrNull` 容错、
  `NON_NUMERIC` 收敛、提为 top-level `internal` 便于测试；节流注释与常量对齐（2 days）。
- `source-api-common/.../SManga.kt:78-100`：顶层 `copy()` 补 `update_strategy`。
- `source-api/.../SManga.kt:116-142`：同一契约的 ABI 侧 `copy()` 也补 `update_strategy`（增量复核 M1）。
- `app/.../manga/model/Manga.kt:40-53`：`toSManga()` 带 `update_strategy = updateStrategy.toAndroid()`。
- `MangaCover.kt` 补闭合 `// KMK <--`；`GetApplicationRelease.kt` 修 `// KMK --.`；
  `LibraryPreferences.kt` 删悬挂 close。
- 4 个 CI workflow 补 `jvmTest`（benchmark/preview/push/release）。
- `Clock.kt`/`Clock.android.kt`/`Clock.jvm.kt`/`ReleaseVersionTest.kt` 补 EOF 换行
  （增量复核 blocker：原提交 blob 末字节 0x2d，CI `spotlessCheck` 必挂）。

### 1.4 去重与依赖显式化（T3 前置）

- 新建 `domain/src/main/java/tachiyomi/domain/manga/model/UpdateStrategyMappings.kt`：
  `UpdateStrategy.toCommon()` / `CommonUpdateStrategy.toAndroid()` 的唯一实现。
- `MangaUpdateMapper.kt` 只保留 `Manga.toMangaUpdate()`；删除重复 `toCommon`。
- `app/.../bridge/CommonMappings.kt` 删除枚举映射函数，改 import domain 扩展；测试同步。
- 删除 `source-api-common/.../QuerySanitizer.kt`（62 行重复实现），统一用
  `core/common-kmp/.../QuerySanitizer`；`Source.kt`、`ModelTest.kt` 改 import。
- `source-api/build.gradle.kts`、`source-api-common/build.gradle.kts` 的 commonMain 显式
  `api(projects.core.commonKmp)`，不再依赖 `androidMain` 隐式传递（增量复核 R1）。

### 1.5 T1：commonMain 平台 import 门禁

- `buildSrc/src/main/kotlin/mihon/buildlogic/tasks/CommonMainImportCheckTask.kt`：
  `@InputDirectory commonMainDir` + `@OutputFile reportFile`；去注释但保留行号；三规则：
  - `FORBIDDEN_IMPORT`：`^import\s+(android|androidx|java|javax|kotlin\.jvm)\..*`
  - `JVM_SYSTEM_API`：`System.(currentTimeMillis|nanoTime|getenv|getProperty|getProperties|exit|err|out)`
  - `PLATFORM_FQCN`：`(android|androidx|java|javax).(app|content|graphics|lang|math|net|nio|os|text|time|util|view|widget).[A-Z]`
  - 失败抛 `GradleException`，报告写 `build/reports/commonMain-imports.txt`。
- `buildSrc/src/main/kotlin/mihon.library.gradle.kts`：`pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform")`
  时注册并挂 `check`；`source-api` 豁免（其 commonMain 仍是伪平台中立，等 D2 拆分）。
- 5 个 workflow（benchmark/preview/pull_request/push/release）在 `spotlessCheck` 后加
  `./gradlew verifyCommonMainImports`。
- 负向验证：临时 `tmp/GateProbe.kt`（`import android.content.Context` + `System.currentTimeMillis()`）
  使 `:core:common-kmp:verifyCommonMainImports` 按预期 FAILED，探针已删除。

### 1.6 新增测试

- `source-api-common/src/commonTest/.../ModelTest.kt`：`mangaCopyPreservesUpdateStrategy`。
- `domain-common/src/commonTest/.../ReleaseVersionTest.kt`：稳定版/预览版比较、缺段、非法 tag。
- `domain/src/test/.../GetApplicationReleaseTest.kt`：改为显式期望对象，消除对 `domain-common`
  internal `getLatest` 的跨模块引用（该测试 phase3 后从未编译过，属潜伏断裂）。

### 1.7 A2 review blocker 收口（2026-11-09 第四轮）

> 用户 m01401 授权「清单→顺序→逐项制作」；本轮完成 A2 五项 + `source-local` `jvm()` 前置。

| 项 | 处置 |
|----|------|
| A2-1 `ChapterSort` 方向 | 不改。与 `master` 同名文件逐字节一致（历史 `efca89916`/`c06f89268` 仅移动），属上游继承行为。 |
| A2-2 `MangaCover` vibrant 短路 | `MangaCover.removeFavoriteCoverData(mangaId)` 只清 `dominantCoverColorsMap` + `coverRatios`，保留 `vibrantCoverColors`；`MangaCoverMetadata.kt:83-86` 非收藏分支改用它后再判 vibrant。 |
| A2-3 `MangaCustomInfo` | provider/installed 均 `@Volatile`，新增 `isInstalled`；未安装返回 null（不 `require` 崩），`install()` 同时置两者。 |
| A2-4 `track!!` | 全仓 11 处清零（`TrackInfoDialog` 9 + `MangaScreenModel` 2）；4 个 ID 选择器（status/chapter/score/date）加 `isMissing` 终态：ERROR 日志 + `navigator.pop()`。 |
| A2-5 标记闭合 | `app/.../presentation/manga/components/MangaCover.kt:177` 补 `// KMK <--`（6 开/6 闭）。 |
| 前置 | `source-local/build.gradle.kts` 加 `jvm()`，为 commonMain 契约提供 JVM 验证 target（暂无测试源）。 |

新增测试：`domain-common/src/commonTest/.../MangaCoverTest.kt`（2 例）、`MangaCustomInfoTest.kt`（1 例）。

## 2. 当前状态

- 分支 `feat/kmp-phase4-d1b1`，HEAD `bba90cf9d`（A2 收口，2026-11-09；9 files，+234/−20）：
  A2 五项修复 + 2 个新测试 + `source-local` `jvm()`。
- 前一提交 `8fe6e3d1e`（T1 门禁 + T3 去重 + ABI/依赖显式化，26 files）；
  更早 `588a27e08`（D1-B1 + 首轮审查修复）。
- 工作区干净；未 push 到上游仓库（仅 origin 自有 fork）；无 force push。
- **B1a 阻塞**：`SChapter/SManga ↔ common` 的唯一桥在 `app/.../bridge/CommonMappings.kt`，
  `source-local` 不能依赖 `app`；需 owner 拍板 D2 后才能开工。
- 未加 iOS target（遵守约束：Windows 禁配；`HostManager.hostIsMac` 全仓不存在，需在 buildSrc 新建）。
- 未改 D1–D4 之外的业务语义；未改 schema；未推上游仓库（仅 origin 自有 fork）。

## 3. 验证（本地全量，JDK 21）

必须用 JDK 21（见 §4）：

```bash
java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain \
  spotlessCheck assembleDebug testReleaseUnitTest jvmTest verifyCommonMainImports \
  --console=plain "-Dorg.gradle.java.home=C:/Users/Administrator/.jdks/jdk-21.0.13.0.2+1"
```

- **BUILD SUCCESSFUL in 3m 21s，EXIT=0**（A2 轮，2026-11-10 00:07；上一轮 T1/T3 为 3m 31s）。
- 新增用例：`MangaCoverTest` 2/2、`MangaCustomInfoTest` 1/1（`domain-common/build/test-results/jvmTest/`）。
- `domain` 的 `GetApplicationReleaseTest`：`tests=4 skipped=0 failures=0 errors=0`
  （`domain/build/test-results/testReleaseUnitTest/TEST-...GetApplicationReleaseTest.xml`）。
- `verifyCommonMainImports` 覆盖 core:common-kmp、core:file、domain-common、
  source-api-common、source-local（本轮起含 `jvm()` target）；`source-api` 暂豁免。
- 负向有效性见 §1.5。
- 工具链按 §4；全量门禁建议 `--no-daemon`（daemon 偶发挂起，本机已踩坑）。

## 4. 工具链要求（本机环境已踩坑）

- **JDK 21 是硬要求**：机器默认 JDK 26 会在 `:app:compileDebugJavaWithJavac` 的 JdkImageTransform
  失败（jlink `--disable-plugin system-modules`）；JBR 25 无 `jlink.exe`。
  本机 JDK 21：`C:\Users\Administrator\.jdks\jdk-21.0.13.0.2+1`（Microsoft Build，201MB）。
- **spotless 必须 8.11.0**：8.8.0 的 ktlint compat 层引用
  `com.pinterest.ktlint.rule.engine.api.EditorConfigDefaults` 在运行时不可见，
  `spotlessCheck` 必报 `NoClassDefFoundError`。已在 `gradle/libs.versions.toml:9` 升级。
- **MSYS 下 `./gradlew` 可能坏**（`Unable to access jarfile C;/...`）：
  用 `java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain <tasks>`。

## 5. 下一步（按顺序）

1. **owner 拍板 D1–D4 + 三个一票否决问题**（`PHASE4-DECISIONS.md`）。
2. 本轮增量 commit + push（全量验证已过）→ 开 PR 拿首次真实 CI（`gh` 不可用，用网页链接）。
3. T2 `UpdateStrategy` 下沉（DB 不再存 source ordinal）→ T3 剩余项（MangaCover vibrant 拆分、
   伪 StateFlow、MangaCustomInfo volatile、`track!!` 终态、ChapterSort 归因）→ T4 paging →
   T5 driver/事务 → T6 storage → T7 LocalSource → T8 archive → T9 ABI/文档。
4. macOS 环境到位后：buildSrc 增 `HostManager`，条件配置 `iosX64/iosArm64/iosSimulatorArm64`，
   补 actual 与 framework。

## 6. 风险

- source-api ABI 未变（正向），但 `UpdateStrategy` 仍由 DB ordinal 持久化，枚举重排会静默错读（T2 必做）。
- `commonMain` 只被 JVM/Android 编译验证过；Native 差异未暴露（Clock 是第一个显式修复）。
- 8 文件迁移后 `core:common` 仍有 37 个 .kt 依赖 Android；D1 是渐进子集，不是终态。
- `source-api` 的 commonMain 门禁豁免是技术债：新增第二 target 前必须完成 D2 拆分。
- 上游补丁失配根因是 `domain-common`/`core:common-kmp` 的 rename（61+12），不是 KMK 标记粒度；
  每次上游同步需按新路径重放补丁。