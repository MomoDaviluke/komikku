import mihon.buildlogic.configureAndroid
import mihon.buildlogic.configureTest
import mihon.buildlogic.tasks.CommonMainImportCheckTask

plugins {
    id("com.android.library")

    id("mihon.code.lint")
}

android {
    configureAndroid(this)
    configureTest()
}

// KMK -->
// Reject platform-only APIs in KMP `commonMain` before a Native target exposes them at compile time.
// `source-api` is intentionally excluded: its commonMain still carries Android/JVM dependencies until
// the pending source-api-common contract migration lands.
val projectName = name
pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    if (projectName != "source-api") {
        val commonMainKotlin = layout.projectDirectory.dir("src/commonMain/kotlin")
        if (commonMainKotlin.asFile.isDirectory) {
            val verifyCommonMainImports = tasks.register<CommonMainImportCheckTask>("verifyCommonMainImports") {
                commonMainDir.set(commonMainKotlin)
                reportFile.set(layout.buildDirectory.file("reports/commonMain-imports.txt"))
            }
            tasks.matching { it.name == "check" }.configureEach {
                dependsOn(verifyCommonMainImports)
            }
        }
    }
}
// KMK <--
