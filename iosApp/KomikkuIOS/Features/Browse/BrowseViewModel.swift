import Foundation
import Combine

final class BrowseViewModel: ObservableObject {
    enum State: Equatable {
        case loading
        case loaded([Manga])
        case failed(String)
    }

    @Published private(set) var state: State = .loading
    @Published private(set) var activeQuery: String = ""

    private let source: MangaSource

    init(source: MangaSource) {
        self.source = source
    }

    func loadIfNeeded() async {
        guard case .loading = state else { return }
        await reload()
    }

    func updateQuery(_ query: String) async {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmed != activeQuery else { return }
        await setState(.loading)
        activeQuery = trimmed
        await load()
    }

    func reload() async {
        await setState(.loading)
        await load()
    }

    private func load() async {
        do {
            let manga: [Manga]
            if activeQuery.isEmpty {
                manga = try await source.popular(limit: 24)
            } else {
                manga = try await source.search(query: activeQuery, limit: 24)
            }
            await setState(.loaded(manga))
        } catch {
            await setState(.failed(error.localizedDescription))
        }
    }

    @MainActor
    private func setState(_ newState: State) {
        state = newState
    }
}