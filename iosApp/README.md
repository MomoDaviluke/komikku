# Komikku iOS（v0.1 起步）

这个目录是 Komikku iOS 端的第一块砖：一个最小的 SwiftUI App + 一条只产出**未签名 IPA**的 GitHub Actions 流水线。它先证明“Windows 写代码 → macOS 云端构建 → iPhone 侧载”这条链路能跑通，再往上接 Komikku 的 KMP 内核。

## 当前范围

- 纯 SwiftUI：`TabView` 两个页签——「浏览」（MangaDex 热门列表，`LazyVGrid` + `AsyncImage`，加载/失败/下拉刷新）与「关于」（smoke 信息与点击计数）。
- 数据层是临时 Swift 实现（`MangaSource` 协议 + `MangaDexSource` + `StubMangaSource`），后续会被 KMP 共享内核替换；UI 测试通过 `-uiTestStub` 注入 stub，不依赖网络。
- 部署目标 iOS 16.0，仅 iPhone（`TARGETED_DEVICE_FAMILY=1`），Bundle ID `app.komikku.ios.smoke`。
- 工程文件由 [XcodeGen](https://github.com/yonaskolb/XcodeGen) 从 `project.yml` 生成，不提交 `.xcodeproj`。
- CI 先在 `iPhone 16` 模拟器跑 UI 测试，再做 `CODE_SIGNING_ALLOWED=NO` 的设备构建，产出 `Payload/KomikkuIOS.app` 并打包成未签名 IPA。

## 触发构建

推送任何 `feat/ios-*` 分支（或手动 `workflow_dispatch`）即可触发 `.github/workflows/ios-smoke.yml`。构建完成后在 Actions 的 Artifacts 里下载：

- `KomikkuIOS-unsigned-ipa`（含 `.ipa` 与 sha256）

模拟器 UI 测试会验证：浏览页出现、stub 漫画卡片渲染、关于页点击计数从 0 变 1。

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

1. CI 绿 + 真机装上前提下，接 KMP 共享内核（先只读 MangaDex 的 browse/detail/chapter）。
2. 用 SwiftUI 做 browse → detail → reader 三页最小闭环。
3. 再加书库、搜索、设置与本地存储。

Roadmap 见 `../.workbuddy/ROADMAP-2026-11-11.md` 的 §12/§13（该目录不进 Git，正式文档后续迁到 `docs/ios/`）。