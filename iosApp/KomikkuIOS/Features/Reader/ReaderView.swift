import SwiftUI

struct ReaderView: View {
    let manga: Manga
    let chapter: Chapter
    let source: MangaSource

    @StateObject private var viewModel: ReaderViewModel

    init(manga: Manga, chapter: Chapter, source: MangaSource) {
        self.manga = manga
        self.chapter = chapter
        self.source = source
        _viewModel = StateObject(wrappedValue: ReaderViewModel(chapterID: chapter.id, source: source))
    }

    var body: some View {
        ScrollView {
            content
        }
        .background(Color(.systemBackground))
        .navigationTitle(chapter.displayName)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await viewModel.loadIfNeeded()
        }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .loading:
            ProgressView("正在加载页面…")
                .frame(maxWidth: .infinity)
                .padding(.vertical, 64)
        case .loaded(let pages):
            LazyVStack(spacing: 0) {
                ForEach(Array(pages.enumerated()), id: \.offset) { index, url in
                    page(url: url)
                        .accessibilityIdentifier("reader.page.\(index + 1)")
                }

                Text("本章完 · \(pages.count) 页")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .padding(.vertical, 24)
            }
        case .failed(let message):
            VStack(spacing: 12) {
                Image(systemName: "exclamationmark.triangle")
                    .font(.system(size: 36))
                    .foregroundStyle(.secondary)
                Text("页面加载失败")
                    .font(.headline)
                Text(message)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                Button("重试") {
                    Task { await viewModel.reload() }
                }
                .buttonStyle(.borderedProminent)
            }
            .padding(32)
            .frame(maxWidth: .infinity)
        }
    }

    private func page(url: URL) -> some View {
        AsyncImage(url: url) { phase in
            switch phase {
            case .success(let image):
                image
                    .resizable()
                    .scaledToFit()
                    .frame(maxWidth: .infinity)
            case .failure:
                placeholder
                    .frame(height: 240)
            case .empty:
                ZStack {
                    placeholder
                    ProgressView()
                }
                .frame(height: 240)
            @unknown default:
                placeholder
                    .frame(height: 240)
            }
        }
    }

    private var placeholder: some View {
        ZStack {
            Rectangle().fill(.quaternary)
            Image(systemName: "photo")
                .font(.title)
                .foregroundStyle(.secondary)
        }
    }
}