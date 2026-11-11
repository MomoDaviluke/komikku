import SwiftUI

struct LibraryView: View {
    @EnvironmentObject private var library: LibraryStore

    private let source: MangaSource
    private let columns = [GridItem(.adaptive(minimum: 140), spacing: 12)]

    init(source: MangaSource) {
        self.source = source
    }

    var body: some View {
        NavigationStack {
            Group {
                if library.manga.isEmpty {
                    emptyState
                } else {
                    ScrollView {
                        LazyVGrid(columns: columns, spacing: 16) {
                            ForEach(library.manga) { item in
                                NavigationLink(value: item) {
                                    MangaCard(manga: item)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(16)
                    }
                    .accessibilityIdentifier("library.grid")
                }
            }
            .navigationTitle("书库")
            .navigationDestination(for: Manga.self) { manga in
                MangaDetailView(manga: manga, source: source)
            }
            .navigationDestination(for: ChapterRoute.self) { route in
                ReaderView(manga: route.manga, chapter: route.chapter, source: source)
            }
        }
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "books.vertical")
                .font(.system(size: 48))
                .foregroundStyle(.secondary)
            Text("书库还是空的")
                .font(.headline)
            Text("在浏览页打开作品，点右上角书签即可收藏。")
                .font(.footnote)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}