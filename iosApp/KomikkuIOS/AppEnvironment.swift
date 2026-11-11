import Foundation

enum AppEnvironment {
    /// The UI test bundle launches the app with `-uiTestStub` so that no network
    /// access is required while the simulator smoke test runs.
    static func makeSource() -> MangaSource {
        if ProcessInfo.processInfo.arguments.contains("-uiTestStub") {
            return StubMangaSource()
        }
        return MangaDexSource()
    }
}