# Phase 4 Handover — KMP 深化（2026-11-08）

> 基线：`master@383ffaa81` + 18 commits（`refactor/kmp-common@89d5b97e8`）。
> 当前工作分支：`feat/kmp-phase4-d1b1`（从 89d5b97e8 起）。
> 前置阅读：`.workbuddy/PROJECT-STATUS-2026-11-08.md`（事实底座）、
> `.workbuddy/PHASE4-DECISIONS.md`（D1–D4 决策）、`.workbuddy/CODE-REVIEW-2026-11-08.md`（审查结论）。

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

### 1.3 审查修复

- `SManga.copy()` 顶层重载补 `update_strategy`（`SManga.kt`）。
- `isNewVersion`：段数不等按 0 补齐、`toIntOrNull` 容错、`NON_NUMERIC` 收敛、提为 top-level
  `internal` 便于测试（`GetApplicationRelease.kt`）。
- `MangaCover.kt` 补闭合 `// KMK <--`；`GetApplicationRelease.kt` 修 `// KMK --.`；
  `LibraryPreferences.kt` 删悬挂 close。
- 4 个 CI workflow 补 `jvmTest`（benchmark/preview/push/release）。

### 1.4 新增测试

- `source-api-common/src/commonTest/.../ModelTest.kt`：`mangaCopyPreservesUpdateStrategy`。
- `domain-common/src/commonTest/.../ReleaseVersionTest.kt`：稳定版/预览版比较、缺段、非法 tag。

## 2. 当前状态

- 8 个 rename + 3 个 Clock 新文件 + 5 个修改文件 + 2 个测试文件；未 commit。
- 未加 iOS target（遵守约束：Windows 禁配；`HostManager.hostIsMac` 全仓不存在，需在 buildSrc 新建）。
- 未改 D1–D4 之外的业务代码；未改 schema；未 push。

## 3. 验证

- 目标命令（本轮替代 spotless 的门禁）：
  ```bash
  ./gradlew :core:common-kmp:jvmTest :source-api-common:jvmTest :domain-common:jvmTest \
    :source-local:testDebugUnitTest :app:compileDebugKotlin
  ```
- 结果：**BUILD SUCCESSFUL in 1m 35s**（329 tasks：107 executed / 213 cached / 9 up-to-date）。
  `ReleaseVersionTest` 2/2 通过（15:49:48Z），`ModelTest` 6/6 通过（15:49:43Z），
  `:app:compileDebugKotlin` 通过，无失败用例。详见 `.workbuddy/CODE-REVIEW-2026-11-08.md` §6。
- `spotlessApply/Check` 本机不可用（环境缺陷，干净树同样复现；已试 daemon 重启、
  `--refresh-dependencies`、清 `jars-9`、Gradle 9.1、关闭 MSYS 路径转换均无效），
  见 CODE-REVIEW §4 / PROJECT-STATUS O6。

## 4. 下一步（按顺序）

1. **owner 拍板 D1–D4 + 三个一票否决问题**（`PHASE4-DECISIONS.md`）。
2. T1 平台 import 门禁（最小、可独立合入）。
3. T2 `UpdateStrategy` 下沉 → T3 去重/终态 → T4 paging → T5 driver/事务 → T6 storage → T7 LocalSource → T8 archive → T9 文档/ABI。
4. 工具链：修复/绕开 spotless ktlint（O6）后跑全量门禁，再 commit + PR。
5. macOS 环境到位后：buildSrc 增 `HostManager`，条件配置 `iosX64/iosArm64/iosSimulatorArm64`，补 actual 与 framework。

## 5. 风险

- source-api ABI 未变（正向），但 `UpdateStrategy` 仍由 DB ordinal 持久化，枚举重排会静默错读（T2 必做）。
- `commonMain` 只被 JVM/Android 编译验证过；Native 差异未暴露（Clock 是第一个显式修复）。
- 8 文件迁移后 `core:common` 仍有 37 个 .kt 依赖 Android；D1 是渐进子集，不是终态。
- 上游补丁失配根因是 `domain-common`/`core:common-kmp` 的 rename（61+12），不是 KMK 标记粒度；
  每次上游同步需按新路径重放补丁。