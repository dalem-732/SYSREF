import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

val appVersion: String = Regex("""versionName\s*=\s*"([^"]+)"""")
    .find(rootProject.file("app/build.gradle.kts").readText())
    ?.groupValues?.get(1)
    ?: error("versionName not found in app/build.gradle.kts")

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.sysref.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "SYSREF"
            packageVersion = appVersion
            description = "Offline reference of Linux and Windows admin commands"
            vendor = "Antiokh Lebedev"
            copyright = "© 2026 Antiokh Lebedev. MIT License."
            licenseFile.set(rootProject.file("LICENSE"))

            windows {
                iconFile.set(project.file("icons/sysref.ico"))
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
