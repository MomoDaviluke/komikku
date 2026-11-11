import Foundation
import Combine

@MainActor
final class BrowseViewModel: ObservableObject {
    enum State: Equatable {
        case loading
        case loaded([Manga])
        case failed(String)
    }

    @Published private(set) var state: State = .loading

    private let source: MangaSource

    init(source: MangaSource) {
        self.source = source
    }

    func loadIfNeeded() async {
        guard case .loading = state else { return }
        await reload()
    }

    func reload() async {
        state = .loading
        do {
            let manga = try await source.popular(limit: 24)
            state = .loaded(manga)
        } catch {
            state = .failed(error.localizedDescription)
        }
    }
}