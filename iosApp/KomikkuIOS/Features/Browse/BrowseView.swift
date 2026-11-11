import SwiftUI

struct BrowseView: View {
    @StateObject private var viewModel: BrowseViewModel
    @State private var query: String = ""
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
                .searchable(text: $query, prompt: "搜索 MangaDex")
                .onSubmit(of: .search) {
                    Task { await viewModel.updateQuery(query) }
                }
                .onChange(of: query) { newValue in
                    if newValue.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
                       !viewModel.activeQuery.isEmpty {
                        Task { await viewModel.updateQuery("") }
                    }
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
                if manga.isEmpty {
                    Text(viewModel.activeQuery.isEmpty ? "暂时没有可显示的作品。" : "没有找到「\(viewModel.activeQuery)」。")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .padding(.top, 48)
                }
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

