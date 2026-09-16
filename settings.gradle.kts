rootProject.name = "datetime-wheel-picker-root"
include(":sample:androidApp")
include(":sample:composeApp")
include(":datetime-wheel-picker")

// Overwrite gradle from parent project
pluginManagement {
  val versionCatalog =
    file("../gradle/libs.versions.toml").takeIf(File::exists)
      ?: file("gradle/libs.versions.toml")

  val agpVersion =
    Regex("""(?m)^(?:android-gradle-plugin|agp)\s*=\s*"([^"]+)"""")
      .find(versionCatalog.readText())
      ?.groupValues
      ?.get(1)
      ?: error("Android Gradle Plugin version not found in $versionCatalog")

  resolutionStrategy {
    eachPlugin {
      if (
        requested.id.id.startsWith("com.android.")
      ) {
        useVersion(agpVersion)
      }
    }
  }

  repositories {
    google {
      content {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
      }
    }
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      content {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
      }
    }
    mavenCentral()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
