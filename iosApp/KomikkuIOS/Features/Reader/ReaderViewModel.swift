import Foundation
import Combine

final class ReaderViewModel: ObservableObject {
    enum State: Equatable {
        case loading
        case loaded([URL])
        case failed(String)
    }

    @Published private(set) var state: State = .loading

    private let chapterID: String
    private let source: MangaSource

    init(chapterID: String, source: MangaSource) {
        self.chapterID = chapterID
        self.source = source
    }

    func loadIfNeeded() async {
        guard case .loading = state else { return }
        await reload()
    }

    func reload() async {
        await setState(.loading)
        do {
            let pages = try await source.pages(chapterID: chapterID)
            await setState(.loaded(pages))
        } catch {
            await setState(.failed(error.localizedDescription))
        }
    }

    @MainActor
    private func setState(_ newState: State) {
        state = newState
    }
}