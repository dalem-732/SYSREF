import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
}

compose.desktop {
    application {
        mainClass = "com.sysref.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "SYSREF"
            // Keep in step with versionName in app/build.gradle.kts.
            packageVersion = "1.2.0"
            description = "Offline reference of Linux and Windows admin commands"
            vendor = "Antiokh Lebedev"
            copyright = "© 2026 Antiokh Lebedev. MIT License."
            licenseFile.set(rootProject.file("LICENSE"))

            windows {
                iconFile.set(project.file("icons/sysref.ico"))
                // Never change: Windows uses it to upgrade an installed SYSREF in place.
                upgradeUuid = "54040c1d-c253-4261-9252-675c4fb65f23"
                menu = true
                menuGroup = "SYSREF"
                shortcut = true
                dirChooser = true
                perUserInstall = true
            }
            linux {
                iconFile.set(project.file("icons/sysref.png"))
                packageName = "sysref"
                menuGroup = "Development;System;"
                appCategory = "devel"
                appRelease = "1"
                debMaintainer = "dalem662@hotmail.com"
                rpmLicenseType = "MIT"
                shortcut = true
            }
        }
    }
}
