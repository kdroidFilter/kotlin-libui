import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val os = org.gradle.internal.os.OperatingSystem.current()!!
val isRunningInIde: Boolean = System.getProperty("idea.active") == "true"

kotlin {
    if (os.isWindows) mingwX64("windows")
    if (os.isLinux) linuxX64("linux")
    if (os.isMacOsX) macosX64("macosx")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(compose.runtime)
            }
        }
        val commonTest by getting {
            kotlin.srcDir("src/unitTest/kotlin")
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        // Inclure les sources précédemment dans nativeMain directement dans les sourceSets spécifiques
        if (os.isWindows || rootProject.hasProperty("publishMode")) {
            val windowsMain by getting {
                kotlin.srcDir("src/nativeMain/kotlin")
                dependencies {
                    api(project(":libui"))
                }
            }
        }
        if (os.isLinux || rootProject.hasProperty("publishMode")) {
            val linuxMain by getting {
                kotlin.srcDir("src/nativeMain/kotlin")
                dependencies {
                    api(project(":libui"))
                }
            }
        }
        if (os.isMacOsX || rootProject.hasProperty("publishMode")) {
            val macosxMain by getting {
                kotlin.srcDir("src/nativeMain/kotlin")
                dependencies {
                    api(project(":libui"))
                }
            }
        }
    }
}
