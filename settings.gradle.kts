rootProject.name = "datetime-wheel-picker"
include(":sample:androidApp")
include(":sample:composeApp")
include(":datetime-wheel-picker")

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
