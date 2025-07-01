// SPDX-License-Identifier: MIT OR Apache-2.0
@file:Suppress("SpellCheckingInspection")

kotlin {
    sourceSets {
        val nativeMain by getting {
            kotlin.srcDir("src/nativeMain/resources")
        }
    }
}
