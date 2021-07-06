import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.compose") version "0.5.0-build225"
}

group = "at.themrcodes"
version = "1.0.0"


repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

val kotlinCoroutinesVersion: String by project
dependencies {
    // Kotlin & reflection
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("test"))

    // Dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")    // Coroutines

    // Compose UI
    implementation(compose.desktop.currentOs)

    implementation(project(":ComposeUITerminalLib"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

compose.desktop {
    application {
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "compose-ui-terminal-lib"
        }
    }
}
