plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

group = "com.github.grasski"
version = "1.0.0"

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "com.dabi.easylocalgamekmplibrary"
        compileSdk = 36
        minSdk = 29

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "EasyLocalGameKmpLibraryKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // Serialization
                implementation(libs.kotlinx.serialization.json)
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
                // Koin DI
                api(libs.koin.core)
                // Lifecycle ViewModel for templates
                implementation(libs.androidx.lifecycle.viewmodelCompose)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Google Nearby Connections
                implementation(libs.play.services.nearby)
                // Koin Android
                implementation(libs.koin.android)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                // iOS-specific dependencies will be handled through Swift/CocoaPods
            }
        }
    }

}

// Task to copy Swift source files so they can be distributed with the library
tasks.register<Copy>("copySwiftSources") {
    from("src/iosMain/swift")
    into("$buildDir/swift")
    include("*.swift")
}

// Ensure Swift sources are copied when building iOS frameworks
tasks.matching { it.name.startsWith("link") && it.name.contains("Framework") }.configureEach {
    dependsOn("copySwiftSources")
}

// Publishing configuration for JitPack
publishing {
    publications {
        // KMP automatically creates publications for each target
        // JitPack will build and publish all configured targets
    }
}

// Disable signing for JitPack (it doesn't support GPG signing)
tasks.withType<Sign>().configureEach {
    enabled = false
}