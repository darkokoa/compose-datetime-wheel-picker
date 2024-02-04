rootProject.name = "chrono-wheel-picker"
include(":sample:composeApp")
include(":chrono-wheel-picker")

pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}
