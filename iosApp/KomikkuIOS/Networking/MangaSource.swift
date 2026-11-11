import Foundation

protocol MangaSource: Sendable {
    func popular(limit: Int) async throws -> [Manga]
}

enum MangaSourceError: LocalizedError {
    case invalidResponse
    case httpStatus(Int)
    case emptyResponse

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "服务器返回了无法识别的响应。"
        case .httpStatus(let code):
            return "请求失败（HTTP \(code)）。"
        case .emptyResponse:
            return "暂时没有可显示的作品。"
        }
    }
}