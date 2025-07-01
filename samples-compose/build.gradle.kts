// SPDX-License-Identifier: MIT OR Apache-2.0
@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
}

val os = org.gradle.internal.os.OperatingSystem.current()!!
val isRunningInIde: Boolean = System.getProperty("idea.active") == "true"

val samplesResourcesDir = "$projectDir/resources"

subprojects {
    apply(plugin = "kotlin-multiplatform")
    apply(plugin = "org.jetbrains.compose")
    apply(plugin = "org.jetbrains.kotlin.plugin.compose")

    configure<KotlinMultiplatformExtension> {
        if (os.isWindows) {
            mingwX64("windows64")
        }
        if (os.isLinux) {
            linuxX64("linux")
        }
        if (os.isMacOsX) {
            macosX64("macosx")
        }

        fun org.jetbrains.kotlin.gradle.plugin.mpp.Executable.windowsResources(rcFileName: String) {
            val taskName = linkTaskName.replaceFirst("link", "windres")
            val inFile = File(rcFileName)
            val outFile = layout.buildDirectory.file("processedResources/$taskName.res").get().asFile

            val windresTask = tasks.register<Exec>(taskName) {
                val konanDataDir = System.getenv("KONAN_DATA_DIR") ?: "${System.getProperty("user.home")}/.konan"
                val toolchainBinDir = when {
                    target.konanTarget.architecture.name.equals("X86", ignoreCase = true) -> 
                        "$konanDataDir/dependencies/msys2-mingw-w64-i686-2/bin"
                    target.konanTarget.architecture.name.equals("X64", ignoreCase = true) -> 
                        "$konanDataDir/dependencies/msys2-mingw-w64-x86_64-2/bin"
                    else -> error("Architecture non prise en charge: ${target.konanTarget.architecture}")
                }

                inputs.file(inFile)
                outputs.file(outFile)
                commandLine("$toolchainBinDir/windres", inFile, "-D_${buildType.name}", "-O", "coff", "-o", outFile)
                environment("PATH", "$toolchainBinDir;${System.getenv("PATH")}")

                dependsOn(compilation.compileTaskProvider)
            }

            linkTaskProvider.configure { dependsOn(windresTask) }
            linkerOpts(outFile.toString())
        }

                    // Configuration des options du compilateur au niveau de l'extension
                    compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
                    }

        sourceSets {
            val nativeMain by creating {
                kotlin.srcDir("src/nativeMain/kotlin")
                dependencies {
                    implementation(project(":libui-compose"))
                }
                languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }

            // Configurer les sourceSets spécifiques à chaque cible
            if (os.isWindows || rootProject.hasProperty("publishMode")) {
                val windows64Main by getting {
                    dependsOn(nativeMain)
                }
            }
            if (os.isLinux || rootProject.hasProperty("publishMode")) {
                val linuxMain by getting {
                    dependsOn(nativeMain)
                }
            }
            if (os.isMacOsX || rootProject.hasProperty("publishMode")) {
                val macosxMain by getting {
                    dependsOn(nativeMain)
                }
            }
        }

        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
            binaries {
                executable(listOf(RELEASE, DEBUG)) {
                    if (konanTarget.family == org.jetbrains.kotlin.konan.target.Family.MINGW) {
                        windowsResources("${rootProject.rootDir}/samples/resources/samples.rc")
                    }
                }
            }
        }
    }
}
