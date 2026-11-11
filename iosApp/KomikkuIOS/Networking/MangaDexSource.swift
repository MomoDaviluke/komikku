import Foundation

/// Minimal MangaDex client used for the first iOS vertical slice.
/// It intentionally mirrors the browse query used by the Android app:
/// popular manga with cover art, safe/suggestive content only.
struct MangaDexSource: MangaSource {
    private let session: URLSession

    init(session: URLSession = .shared) {
        self.session = session
    }

    func popular(limit: Int) async throws -> [Manga] {
        var components = URLComponents(string: "https://api.mangadex.org/manga")!
        components.queryItems = [
            URLQueryItem(name: "limit", value: String(max(1, min(limit, 100)))),
            URLQueryItem(name: "includes[]", value: "cover_art"),
            URLQueryItem(name: "order[followedCount]", value: "desc"),
            URLQueryItem(name: "contentRating[]", value: "safe"),
            URLQueryItem(name: "contentRating[]", value: "suggestive")
        ]

        guard let url = components.url else { throw MangaSourceError.invalidResponse }

        var request = URLRequest(url: url)
        request.timeoutInterval = 20
        request.setValue("Komikku-iOS/0.1 (https://github.com/MomoDaviluke/komikku)", forHTTPHeaderField: "User-Agent")

        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw MangaSourceError.invalidResponse
        }
        guard (200..<300).contains(http.statusCode) else {
            throw MangaSourceError.httpStatus(http.statusCode)
        }

        let payload = try JSONDecoder().decode(Response.self, from: data)
        guard !payload.data.isEmpty else {
            throw MangaSourceError.emptyResponse
        }
        return payload.data.map { $0.toManga() }
    }
}

private extension MangaDexSource {
    struct Response: Decodable {
        let data: [Entry]
    }

    struct Entry: Decodable {
        struct Attributes: Decodable {
            let title: [String: String]
            let description: [String: String]?
            let year: Int?
            let status: String?
        }

        struct Relationship: Decodable {
            struct Attributes: Decodable {
                let fileName: String?
            }

            let type: String
            let attributes: Attributes?
        }

        let id: String
        let attributes: Attributes
        let relationships: [Relationship]?
    }
}

private extension MangaDexSource.Entry {
    func toManga() -> Manga {
        let localizedTitle = attributes.title["en"] ?? attributes.title["ja"] ?? attributes.title.values.first
        let localizedDescription = attributes.description?["en"] ?? attributes.description?["ja"] ?? attributes.description?.values.first
        let coverFileName = relationships?
            .first { $0.type == "cover_art" }?
            .attributes?
            .fileName

        var coverURL: URL?
        if let coverFileName, !coverFileName.isEmpty {
            coverURL = URL(string: "https://uploads.mangadex.org/covers/\(id)/\(coverFileName).256.jpg")
        }

        return Manga(
            id: id,
            title: localizedTitle ?? "未命名作品",
            description: localizedDescription?
                .replacingOccurrences(of: "\n", with: " ")
                .trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
            coverURL: coverURL,
            year: attributes.year,
            status: attributes.status
        )
    }
}