import SwiftUI

struct BrowseView: View {
    @StateObject private var viewModel: BrowseViewModel
    private let source: MangaSource

    private let columns = [GridItem(.adaptive(minimum: 140), spacing: 12)]

    init(source: MangaSource) {
        self.source = source
        _viewModel = StateObject(wrappedValue: BrowseViewModel(source: source))
    }

    var body: some View {
        NavigationStack {
            content
                .navigationTitle("浏览")
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button {
                            Task { await viewModel.reload() }
                        } label: {
                            Image(systemName: "arrow.clockwise")
                        }
                        .accessibilityIdentifier("browse.reload")
                    }
                }
                .task {
                    await viewModel.loadIfNeeded()
                }
                .navigationDestination(for: Manga.self) { manga in
                    MangaDetailView(manga: manga, source: source)
                }
                .navigationDestination(for: ChapterRoute.self) { route in
                    ReaderView(manga: route.manga, chapter: route.chapter, source: source)
                }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .loading:
            ProgressView("正在加载 MangaDex…")
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .loaded(let manga):
            ScrollView {
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(manga) { item in
                        NavigationLink(value: item) {
                            MangaCard(manga: item)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(16)
            }
            .refreshable {
                await viewModel.reload()
            }
            .accessibilityIdentifier("browse.grid")
        case .failed(let message):
            VStack(spacing: 16) {
                Image(systemName: "wifi.exclamationmark")
                    .font(.system(size: 42))
                    .foregroundStyle(.secondary)
                Text("加载失败")
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
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

private struct MangaCard: View {
    let manga: Manga

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            cover
                .aspectRatio(2.0 / 3.0, contentMode: .fit)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))

            Text(manga.title)
                .font(.subheadline.weight(.semibold))
                .lineLimit(2)

            if !manga.subtitle.isEmpty {
                Text(manga.subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
        }
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("manga.card.\(manga.id)")
    }

    @ViewBuilder
    private var cover: some View {
        if let url = manga.coverURL {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image.resizable()
                case .failure:
                    placeholder
                case .empty:
                    ZStack {
                        placeholder
                        ProgressView()
                    }
                @unknown default:
                    placeholder
                }
            }
        } else {
            placeholder
        }
    }

    private var placeholder: some View {
        ZStack {
            Rectangle().fill(.quaternary)
            Image(systemName: "book.closed")
                .font(.title)
                .foregroundStyle(.secondary)
        }
    }
}