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
                api(kotlinx.coroutines.core)
                api(kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlinx.coroutines.test)
            }
        }
    }
}

android {
    namespace = "tachiyomi.core.common.kmp"
}
// KMK <--
