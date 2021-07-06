rootProject.name = "ComposeTerminal"
include("ExampleImplementation")
include("ComposeUITerminalLib")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}