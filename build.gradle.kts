// SPDX-License-Identifier: MIT OR Apache-2.0
@file:Suppress("SpellCheckingInspection")

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.download) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    repositories {
        mavenCentral()
    }
}

apply {
    from("dokka.gradle")
}
