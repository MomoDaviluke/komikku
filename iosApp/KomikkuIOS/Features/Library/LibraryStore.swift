import Foundation
import Combine

/// Local-only library for the first iOS slice. Manga is small (no chapters),
/// so the whole list is persisted as JSON in UserDefaults. This is deliberately
/// a thin stand-in until the shared KMP data layer is wired in.
final class LibraryStore: ObservableObject {
    @Published private(set) var manga: [Manga] = []

    private let defaults: UserDefaults
    private let storageKey = "komikku.library.v1"

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        if ProcessInfo.processInfo.arguments.contains("-resetLibrary") {
            defaults.removeObject(forKey: storageKey)
        }
        load()
    }

    func isSaved(_ item: Manga) -> Bool {
        manga.contains { $0.id == item.id }
    }

    func toggle(_ item: Manga) {
        if let index = manga.firstIndex(where: { $0.id == item.id }) {
            manga.remove(at: index)
        } else {
            manga.insert(item, at: 0)
        }
        persist()
    }

    private func load() {
        guard let data = defaults.data(forKey: storageKey) else { return }
        manga = (try? JSONDecoder().decode([Manga].self, from: data)) ?? []
    }

    private func persist() {
        guard let data = try? JSONEncoder().encode(manga) else { return }
        defaults.set(data, forKey: storageKey)
    }
}