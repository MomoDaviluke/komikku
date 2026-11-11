import Foundation

struct StubMangaSource: MangaSource {
    func popular(limit: Int) async throws -> [Manga] {
        try? await Task.sleep(nanoseconds: 150_000_000)
        return Array(Manga.stub.prefix(limit))
    }

    func search(query: String, limit: Int) async throws -> [Manga] {
        try? await Task.sleep(nanoseconds: 150_000_000)
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return try await popular(limit: limit) }
        return Manga.stub.filter { $0.title.localizedCaseInsensitiveContains(trimmed) }
    }

    func chapters(mangaID: String) async throws -> [Chapter] {
        try? await Task.sleep(nanoseconds: 100_000_000)
        return [
            Chapter(id: "\(mangaID)-1", number: "1", title: "开始", pages: 3, publishAt: nil),
            Chapter(id: "\(mangaID)-2", number: "2", title: "相遇", pages: 3, publishAt: nil),
            Chapter(id: "\(mangaID)-3", number: "3", title: "出发", pages: 3, publishAt: nil)
        ]
    }

    func pages(chapterID: String) async throws -> [URL] {
        (1...3).map { index in
            URL(string: "https://example.invalid/\(chapterID)/\(index).jpg")!
        }
    }
}