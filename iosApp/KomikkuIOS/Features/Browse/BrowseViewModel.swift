import Foundation
import Combine

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
        await setState(.loading)
        do {
            let manga = try await source.popular(limit: 24)
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