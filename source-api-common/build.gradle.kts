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
                // KMK -->
                api(projects.core.commonKmp)
                // KMK <--
                api(kotlinx.serialization.json)
                api(kotlinx.coroutines.core)
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
    namespace = "eu.kanade.tachiyomi.source.common"
}
// KMK <--
