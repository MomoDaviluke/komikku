import Foundation
import Combine

final class MangaDetailViewModel: ObservableObject {
    enum State: Equatable {
        case loading
        case loaded([Chapter])
        case failed(String)
    }

    @Published private(set) var state: State = .loading

    private let mangaID: String
    private let source: MangaSource

    init(mangaID: String, source: MangaSource) {
        self.mangaID = mangaID
        self.source = source
    }

    func loadIfNeeded() async {
        guard case .loading = state else { return }
        await reload()
    }

    func reload() async {
        await setState(.loading)
        do {
            let chapters = try await source.chapters(mangaID: mangaID)
            await setState(.loaded(chapters))
        } catch {
            await setState(.failed(error.localizedDescription))
        }
    }

    @MainActor
    private func setState(_ newState: State) {
        state = newState
    }
}