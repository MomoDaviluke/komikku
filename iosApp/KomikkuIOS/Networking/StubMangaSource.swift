import Foundation

struct StubMangaSource: MangaSource {
    func popular(limit: Int) async throws -> [Manga] {
        try? await Task.sleep(nanoseconds: 150_000_000)
        return Array(Manga.stub.prefix(limit))
    }
}