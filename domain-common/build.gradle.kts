// KMK -->
plugins {
    id("mihon.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget()
    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.commonKmp)
                api(projects.sourceApiCommon)
                api(kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions)
            }
        }
    }
}

android {
    namespace = "tachiyomi.domain.common"
}
// KMK <--
