import SwiftUI

struct ChapterRoute: Hashable {
    let manga: Manga
    let chapter: Chapter
}

struct MangaDetailView: View {
    let manga: Manga
    let source: MangaSource

    @StateObject private var viewModel: MangaDetailViewModel
    @EnvironmentObject private var library: LibraryStore

    init(manga: Manga, source: MangaSource) {
        self.manga = manga
        self.source = source
        _viewModel = StateObject(wrappedValue: MangaDetailViewModel(mangaID: manga.id, source: source))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                header

                if !manga.description.isEmpty {
                    Text(manga.description)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                Divider()

                Text("章节")
                    .font(.headline)

                chapters
            }
            .padding(16)
        }
        .navigationTitle(manga.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    library.toggle(manga)
                } label: {
                    Image(systemName: library.isSaved(manga) ? "bookmark.fill" : "bookmark")
                }
                .accessibilityIdentifier("detail.save")
                .accessibilityLabel(library.isSaved(manga) ? "取消收藏" : "收藏")
            }
        }
        .task {
            await viewModel.loadIfNeeded()
        }
    }

    private var header: some View {
        HStack(alignment: .top, spacing: 12) {
            cover
                .frame(width: 96, height: 144)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))

            VStack(alignment: .leading, spacing: 6) {
                Text(manga.title)
                    .font(.title3.weight(.semibold))
                if !manga.subtitle.isEmpty {
                    Text(manga.subtitle)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    @ViewBuilder
    private var cover: some View {
        if let url = manga.coverURL {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image.resizable().scaledToFill()
                case .failure:
                    coverPlaceholder
                case .empty:
                    ZStack {
                        coverPlaceholder
                        ProgressView()
                    }
                @unknown default:
                    coverPlaceholder
                }
            }
        } else {
            coverPlaceholder
        }
    }

    private var coverPlaceholder: some View {
        ZStack {
            Rectangle().fill(.quaternary)
            Image(systemName: "book.closed")
                .font(.title)
                .foregroundStyle(.secondary)
        }
    }

    @ViewBuilder
    private var chapters: some View {
        switch viewModel.state {
        case .loading:
            ProgressView("正在加载章节…")
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.vertical, 24)
        case .loaded(let chapters):
            if chapters.isEmpty {
                Text("暂无可读章节。")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            } else {
                LazyVStack(spacing: 0) {
                    ForEach(chapters) { chapter in
                        NavigationLink(value: ChapterRoute(manga: manga, chapter: chapter)) {
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(chapter.displayName)
                                        .font(.subheadline)
                                        .foregroundStyle(.primary)
                                    Text(chapter.pageLabel)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .font(.caption.weight(.semibold))
                                    .foregroundStyle(.tertiary)
                            }
                            .padding(.vertical, 10)
                        }
                        .buttonStyle(.plain)
                        Divider()
                    }
                }
            }
        case .failed(let message):
            VStack(spacing: 8) {
                Text("章节加载失败")
                    .font(.subheadline.weight(.semibold))
                Text(message)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                Button("重试") {
                    Task { await viewModel.reload() }
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
        }
    }
}