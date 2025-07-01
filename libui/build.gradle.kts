// SPDX-License-Identifier: MIT OR Apache-2.0

@file:Suppress("SpellCheckingInspection")

import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget.*

plugins {
    kotlin("multiplatform")
    id("de.undercouch.download")
    id("maven-publish")
}

object Libui {
    const val version = "alpha4.1-openfolder"
    const val repo = "https://github.com/msink/libui"
}

object Publish {
    const val group = "com.github.msink"
    const val user = "msink"
    object pom {
        const val url = "https://github.com/msink/kotlin-libui"
        const val connection = "scm:git:https://github.com/msink/kotlin-libui.git"
        const val devConnection = "scm:git:git@github.com:msink/kotlin-libui.git"
    }
}

val VERSION_NAME: String by project
val VERSION_SUFFIX: String by project
val BINTRAY_REPO: String by project

group = Publish.group
version = "$VERSION_NAME$VERSION_SUFFIX"

val os = org.gradle.internal.os.OperatingSystem.current()!!
val isRunningInIde: Boolean = System.getProperty("idea.active") == "true"

kotlin {
    val publishModeEnabled = rootProject.hasProperty("publishMode")
    println("publishModeEnabled: $publishModeEnabled")

    if (publishModeEnabled || os.isWindows) {
        mingwX64("windows64")

    }
    if (publishModeEnabled || os.isLinux) {
        linuxX64("linux")
    }
    if (publishModeEnabled || os.isMacOsX) {
        macosX64("macosx")
    }

    val commonMain by sourceSets.getting
    val nativeMain by sourceSets.creating {
        dependsOn(commonMain)
        sourceSets.all {
            languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
            languageSettings.optIn("kotlinx.cinterop.UnsafeNumber")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }


    targets.withType<KotlinNativeTarget> {
        sourceSets["${targetName}Main"].apply {
            dependsOn(nativeMain)
        }
        compilations["main"].apply {
            cinterops.create("libui") {
                includeDirs("$buildDir/libui/${konanTarget.name}")
                defFile(project.file("src/nativeInterop/cinterop/libui.def"))
            }
            compilerOptions.options.freeCompilerArgs.addAll(listOf(
                "-include-binary", "$buildDir/libui/${konanTarget.name}/libui.a"
            ))
        }
    }
}

tasks.withType<CInteropProcess> {
    val archiveFile = File("$buildDir/libui/${konanTarget.name}",
        "libui.${if (konanTarget.family == Family.MINGW) "zip" else "tgz"}")

    val downloadArchive = tasks.register<Download>(name.replaceFirst("cinterop", "download")) {
        val release = "${Libui.repo}/releases/download/${Libui.version}/libui-${Libui.version}"
        when (konanTarget) {
            MINGW_X64 -> src("$release-windows-amd64-mingw-static.zip")
            LINUX_X64 -> src("$release-linux-amd64-static.tgz")
            MACOS_X64 -> src("$release-darwin-amd64-static.tgz")
            else -> {}
        }
        dest(archiveFile)
        overwrite(false)
    }

    val unpackArchive = tasks.register<Copy>(name.replaceFirst("cinterop", "unpack")) {
        if (konanTarget.family == Family.MINGW) {
            from(zipTree(archiveFile))
        } else {
            from(tarTree(resources.gzip(archiveFile)))
        }
        into("$buildDir/libui/${konanTarget.name}")
        dependsOn(downloadArchive)
    }

    dependsOn(unpackArchive)
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("libui")
            description.set("Kotlin/Native interop to libui: a portable GUI library")
            url.set(Publish.pom.url)
            licenses {
                license {
                    name.set("MIT License")
                    url.set(Publish.pom.url)
                    distribution.set("repo")
                }
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("msink")
                    name.set("Mike Sinkovsky")
                    email.set("msink@permonline.ru")
                }
            }
            scm {
                url.set(Publish.pom.url)
                connection.set(Publish.pom.connection)
                developerConnection.set(Publish.pom.devConnection)
            }
        }
    }

    repositories {
        maven("https://api.bintray.com/maven/${Publish.user}/$BINTRAY_REPO/libui/;publish=0;override=1") {
            credentials {
                username = Publish.user
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}

tasks.withType<AbstractPublishToMaven> {
    onlyIf { !name.startsWith("publishWindows") || os.isWindows }
    onlyIf { !name.startsWith("publishMacosx") || os.isMacOsX }
    onlyIf { !name.startsWith("publishLinux") || os.isLinux }
}
