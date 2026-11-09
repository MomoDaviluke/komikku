import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("mihon.library")
    kotlin("multiplatform")
}

kotlin {
    androidTarget()
    // KMK -->
    // JVM target gives the commonMain contract (common Source adapter, file system) a
    // platform-neutral verification target; B1b/B1c move more code here.
    jvm()
    // KMK <--
    sourceSets {
        val commonMain by getting {
            dependencies {
                // KMK -->
                api(projects.core.file)
                // KMK <--
            }
        }
        // KMK -->
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.bundles.test)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
        // KMK <--

        val androidMain by getting {
            dependencies {
                // KMK -->
                api(projects.sourceApi)
                api(libs.unifile)
                implementation(projects.i18n)
                implementation(projects.i18nSy)
                // KMK <--

                implementation(projects.core.archive)
                implementation(projects.core.common)
                implementation(projects.coreMetadata)

                // Move ChapterRecognition to separate module?
                implementation(projects.domain)

                implementation(kotlinx.bundles.serialization)
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}

android {
    namespace = "tachiyomi.source.local"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}
