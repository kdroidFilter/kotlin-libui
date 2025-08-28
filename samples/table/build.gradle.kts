// SPDX-License-Identifier: MIT OR Apache-2.0
@file:Suppress("SpellCheckingInspection")

kotlin {
    sourceSets {
        val linuxMain by getting {
            // These are generated Kotlin files with image data, so they must be treated as Kotlin sources
            kotlin.srcDir("src/nativeMain/resources")
        }
        // Optionnel: inclure aussi pour autres plateformes si nécessaires
        // val windows64Main by getting { kotlin.srcDir("src/nativeMain/resources") }
        // val macosxMain by getting { kotlin.srcDir("src/nativeMain/resources") }
    }
}
