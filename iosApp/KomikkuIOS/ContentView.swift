import SwiftUI
import UIKit

struct ContentView: View {
    @State private var tapCount = 0

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "book.closed.fill")
                .font(.system(size: 64))
                .foregroundStyle(.tint)

            Text("Komikku iOS")
                .font(.largeTitle.bold())

            Text("构建流水线 smoke test")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Button("点我验证交互（\(tapCount)）") {
                tapCount += 1
            }
            .buttonStyle(.borderedProminent)

            VStack(spacing: 4) {
                Text("iOS \(UIDevice.current.systemVersion)")
                Text(UIDevice.current.model)
                Text("v0.1 smoke · unsigned build")
            }
            .font(.footnote)
            .foregroundStyle(.secondary)
        }
        .padding(32)
    }
}

#Preview {
    ContentView()
}