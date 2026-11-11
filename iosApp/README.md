# Komikku iOS（v0.1 起步）

这个目录是 Komikku iOS 端的第一块砖：一个最小的 SwiftUI App + 一条只产出**未签名 IPA**的 GitHub Actions 流水线。它先证明“Windows 写代码 → macOS 云端构建 → iPhone 侧载”这条链路能跑通，再往上接 Komikku 的 KMP 内核。

## 当前范围

- 纯 SwiftUI：`TabView` 三个页签——「浏览」（MangaDex 热门/搜索，`LazyVGrid` + `AsyncImage`，加载/失败/下拉刷新）、「书库」（本地收藏，UserDefaults 持久化）、「关于」（smoke 信息与点击计数）。
- 纵向阅读闭环已打通：浏览 → 详情（封面/简介/章节列表）→ 阅读器（纵向图片流）。章节来自 `/manga/{id}/feed`，图片来自 `/at-home/server/{chapterId}`。
- 数据层是临时 Swift 实现（`MangaSource` 协议 + `MangaDexSource` + `StubMangaSource`），后续会被 KMP 共享内核替换；UI 测试通过 `-uiTestStub` 注入 stub，不依赖网络。
- 部署目标 iOS 16.0，仅 iPhone（`TARGETED_DEVICE_FAMILY=1`），Bundle ID `app.komikku.ios.smoke`。
- 工程文件由 [XcodeGen](https://github.com/yonaskolb/XcodeGen) 从 `project.yml` 生成，不提交 `.xcodeproj`。
- CI 分两个 job：设备构建（`CODE_SIGNING_ALLOWED=NO` → 未签名 IPA artifact）与 UI 测试编译（`build-for-testing`，仅编译不跑模拟器——当前 macos-15 runner 上模拟器启动会挂起）。

## 触发构建

推送任何 `feat/ios-*` 分支（或手动 `workflow_dispatch`）即可触发 `.github/workflows/ios-smoke.yml`。构建完成后在 Actions 的 Artifacts 里下载：

- `KomikkuIOS-unsigned-ipa`（含 `.ipa` 与 sha256）

`KomikkuIOSUITests` 里已有浏览卡片、详情/章节/阅读器、收藏、搜索、关于页交互的用例；当前 CI 只编译测试包，待模拟器问题解决后再恢复执行。

## 在 Windows 上侧载到 iPhone

1. 安装 [Sideloadly](https://sideloadly.io/) 与 iTunes（Windows 版，提供 Apple 设备驱动）。
2. iPhone 用数据线连接 Windows，信任此电脑。
3. Sideloadly 选择下载的 `KomikkuIOS-unsigned.ipa`，Apple ID 填自己的免费账号，开始签名安装。
4. 首次运行需在 iPhone「设置 → 通用 → VPN 与设备管理」信任开发者证书。
5. 免费证书 7 天过期；到期后用同一 IPA 重新签一次即可。AltStore/SideStore 可自动续签，但需要一台常开的 Windows。

> 未签名 IPA 不能直接安装；必须经过 Sideloadly/AltStore 之类工具用 Apple ID 重签。

## 本地（仅 macOS）构建

```bash
brew install xcodegen
cd iosApp
xcodegen generate
xcodebuild -project KomikkuIOS.xcodeproj -scheme KomikkuIOS \
  -configuration Release -sdk iphoneos -derivedDataPath build \
  CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY="" build
```

## 下一步

1. 真机安装并跑通浏览/详情/阅读器后，把临时 Swift 数据层换成 KMP 共享内核（source-api-common / domain-common）。
2. 增加第二个、第三个内置源（v0.1 目标 3 个源），并统一分页与错误重试。
3. 补设置页、阅读进度、离线缓存等 v0.5 功能。

Roadmap 见 `../.workbuddy/ROADMAP-2026-11-11.md` 的 §12/§13（该目录不进 Git，正式文档后续迁到 `docs/ios/`）。