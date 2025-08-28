// SPDX-License-Identifier: MIT OR Apache-2.0
@file:Suppress("SpellCheckingInspection")

val os = org.gradle.internal.os.OperatingSystem.current()!!

kotlin {
    sourceSets {
        // Add generated Kotlin sources (image data) to the correct native source set(s)
        if (os.isWindows || rootProject.hasProperty("publishMode")) {
            val windows64Main by getting {
                kotlin.srcDir("src/nativeMain/resources")
            }
        }
        if (os.isLinux || rootProject.hasProperty("publishMode")) {
            val linuxMain by getting {
                kotlin.srcDir("src/nativeMain/resources")
            }
        }
        if (os.isMacOsX || rootProject.hasProperty("publishMode")) {
            val macosxMain by getting {
                kotlin.srcDir("src/nativeMain/resources")
            }
        }
    }
}
