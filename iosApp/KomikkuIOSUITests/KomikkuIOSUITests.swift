import XCTest

final class KomikkuIOSUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testSmokeScreenLaunchesAndResponds() {
        let app = XCUIApplication()
        app.launch()

        let title = app.staticTexts["smoke.title"]
        XCTAssertTrue(title.waitForExistence(timeout: 20), "标题未出现，App 可能启动失败")

        let button = app.buttons["smoke.tapButton"]
        XCTAssertTrue(button.waitForExistence(timeout: 5), "交互按钮未出现")
        XCTAssertTrue(button.label.contains("0"), "初始计数应为 0，实际 label=\(button.label)")

        button.tap()
        XCTAssertTrue(button.label.contains("1"), "点击后计数应变为 1，实际 label=\(button.label)")
    }
}