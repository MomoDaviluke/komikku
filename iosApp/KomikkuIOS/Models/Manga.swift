import Foundation

struct Manga: Identifiable, Hashable {
    let id: String
    let title: String
    let description: String
    let coverURL: URL?
    let year: Int?
    let status: String?

    var subtitle: String {
        [status.map { $0.replacingOccurrences(of: "_", with: " ").capitalized }, year.map { String($0) }]
            .compactMap { $0 }
            .joined(separator: " · ")
    }
}

extension Manga {
    static let stub: [Manga] = [
        Manga(
            id: "stub-1",
            title: "向日葵",
            description: "在乡下小镇长大的少女，与旧书店店主相遇后，开始重新寻找自己的人生方向。",
            coverURL: nil,
            year: 2024,
            status: "ongoing"
        ),
        Manga(
            id: "stub-2",
            title: "夜行列车",
            description: "一列只在午夜出现的列车，载着乘客前往他们最想再见一次的人身边。",
            coverURL: nil,
            year: 2023,
            status: "completed"
        ),
        Manga(
            id: "stub-3",
            title: "海边的餐厅",
            description: "辞职回到故乡的厨师，用一道道家常菜修复与家人之间的关系。",
            coverURL: nil,
            year: 2022,
            status: "ongoing"
        ),
        Manga(
            id: "stub-4",
            title: "纸上宇宙",
            description: "少年在旧书摊发现一本会自己续写故事的书，每翻一页都进入一个新的世界。",
            coverURL: nil,
            year: 2025,
            status: "ongoing"
        )
    ]
}