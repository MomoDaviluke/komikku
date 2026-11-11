import SwiftUI
import UIKit

struct ContentView: View {
    private let source: MangaSource

    init(source: MangaSource = AppEnvironment.makeSource()) {
        self.source = source
    }

    var body: some View {
        TabView {
            BrowseView(source: source)
                .tabItem {
                    Label("浏览", systemImage: "square.grid.2x2")
                }

            AboutView()
                .tabItem {
                    Label("关于", systemImage: "info.circle")
                }
        }
    }
}

private struct AboutView: View {
    @State private var tapCount = 0

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    Image(systemName: "book.closed.fill")
                        .font(.system(size: 64))
                        .foregroundStyle(.tint)

                    Text("Komikku iOS")
                        .font(.largeTitle.bold())
                        .accessibilityIdentifier("smoke.title")

                    Text("v0.1 · 内置 MangaDex 源")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    Button("点我验证交互（\(tapCount)）") {
                        tapCount += 1
                    }
                    .buttonStyle(.borderedProminent)
                    .accessibilityIdentifier("smoke.tapButton")

                    VStack(spacing: 4) {
                        Text("iOS \(UIDevice.current.systemVersion)")
                        Text(UIDevice.current.model)
                        Text("unsigned 侧载构建")
                    }
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                }
                .padding(32)
            }
            .navigationTitle("关于")
        }
    }
}

#Preview {
    ContentView(source: StubMangaSource())
}