import XCTest

final class KomikkuIOSUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    private func launchStubApp() -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = ["-uiTestStub"]
        app.launch()
        return app
    }

    func testBrowseShowsStubbedManga() {
        let app = launchStubApp()

        XCTAssertTrue(
            app.navigationBars["浏览"].waitForExistence(timeout: 20),
            "浏览页未出现，App 可能启动失败"
        )

        let firstCard = app.descendants(matching: .any)["manga.card.stub-1"]
        XCTAssertTrue(firstCard.waitForExistence(timeout: 20), "stub 漫画卡片未渲染")
    }

    func testAboutScreenInteraction() {
        let app = launchStubApp()

        let aboutTab = app.tabBars.buttons["关于"]
        XCTAssertTrue(aboutTab.waitForExistence(timeout: 20), "底部导航未出现")
        aboutTab.tap()

        let title = app.staticTexts["smoke.title"]
        XCTAssertTrue(title.waitForExistence(timeout: 10), "关于页标题未出现")

        let button = app.buttons["smoke.tapButton"]
        XCTAssertTrue(button.waitForExistence(timeout: 5), "交互按钮未出现")
        XCTAssertTrue(button.label.contains("0"), "初始计数应为 0，实际 label=\(button.label)")

        button.tap()
        XCTAssertTrue(button.label.contains("1"), "点击后计数应变为 1，实际 label=\(button.label)")
    }
}