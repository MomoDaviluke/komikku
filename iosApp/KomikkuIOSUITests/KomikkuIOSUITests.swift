import XCTest

final class KomikkuIOSUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    private func launchStubApp(resetLibrary: Bool = true) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = ["-uiTestStub"]
        if resetLibrary {
            app.launchArguments.append("-resetLibrary")
        }
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

    func testDetailAndReaderWithStub() {
        let app = launchStubApp()

        let firstCard = app.descendants(matching: .any)["manga.card.stub-1"]
        XCTAssertTrue(firstCard.waitForExistence(timeout: 20), "stub 漫画卡片未渲染")
        firstCard.tap()

        XCTAssertTrue(
            app.navigationBars["向日葵"].waitForExistence(timeout: 10),
            "详情页未打开"
        )

        let chapter = app.staticTexts["第 1 话 · 开始"]
        XCTAssertTrue(chapter.waitForExistence(timeout: 10), "章节列表未渲染")
        chapter.tap()

        XCTAssertTrue(
            app.navigationBars["第 1 话 · 开始"].waitForExistence(timeout: 10),
            "阅读器未打开"
        )
        XCTAssertTrue(
            app.descendants(matching: .any)["reader.page.1"].waitForExistence(timeout: 10),
            "阅读器第一页未渲染"
        )
    }

    func testSaveToLibraryWithStub() {
        let app = launchStubApp()

        let firstCard = app.descendants(matching: .any)["manga.card.stub-1"]
        XCTAssertTrue(firstCard.waitForExistence(timeout: 20), "stub 漫画卡片未渲染")
        firstCard.tap()

        let save = app.buttons["detail.save"]
        XCTAssertTrue(save.waitForExistence(timeout: 10), "收藏按钮未出现")
        save.tap()

        app.navigationBars["向日葵"].buttons.element(boundBy: 0).tap()

        let libraryTab = app.tabBars.buttons["书库"]
        XCTAssertTrue(libraryTab.waitForExistence(timeout: 10), "书库 tab 未出现")
        libraryTab.tap()

        XCTAssertTrue(
            app.descendants(matching: .any)["manga.card.stub-1"].waitForExistence(timeout: 10),
            "收藏的作品未出现在书库"
        )
    }

    func testSearchWithStub() {
        let app = launchStubApp()

        let searchField = app.searchFields.firstMatch
        XCTAssertTrue(searchField.waitForExistence(timeout: 20), "搜索框未出现")
        searchField.tap()
        searchField.typeText("夜行\n")

        XCTAssertTrue(
            app.descendants(matching: .any)["manga.card.stub-2"].waitForExistence(timeout: 20),
            "搜索结果中未出现匹配作品"
        )
        XCTAssertFalse(
            app.descendants(matching: .any)["manga.card.stub-1"].exists,
            "搜索结果不应包含不匹配的作品"
        )
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