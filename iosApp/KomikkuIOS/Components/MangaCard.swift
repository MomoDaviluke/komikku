import SwiftUI

struct MangaCard: View {
    let manga: Manga

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            cover
                .aspectRatio(2.0 / 3.0, contentMode: .fit)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))

            Text(manga.title)
                .font(.subheadline.weight(.semibold))
                .lineLimit(2)

            if !manga.subtitle.isEmpty {
                Text(manga.subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
        }
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("manga.card.\(manga.id)")
    }

    @ViewBuilder
    private var cover: some View {
        if let url = manga.coverURL {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image.resizable()
                case .failure:
                    placeholder
                case .empty:
                    ZStack {
                        placeholder
                        ProgressView()
                    }
                @unknown default:
                    placeholder
                }
            }
        } else {
            placeholder
        }
    }

    private var placeholder: some View {
        ZStack {
            Rectangle().fill(.quaternary)
            Image(systemName: "book.closed")
                .font(.title)
                .foregroundStyle(.secondary)
        }
    }
}