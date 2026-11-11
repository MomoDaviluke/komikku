import Foundation

struct Chapter: Identifiable, Hashable {
    let id: String
    let number: String
    let title: String?
    let pages: Int
    let publishAt: String?

    var displayName: String {
        let numbered = number.isEmpty ? "无编号" : "第 \(number) 话"
        guard let title, !title.isEmpty else { return numbered }
        return "\(numbered) · \(title)"
    }

    var pageLabel: String {
        pages > 0 ? "\(pages) 页" : "页数未知"
    }
}