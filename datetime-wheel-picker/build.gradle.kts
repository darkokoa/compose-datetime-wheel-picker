import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.compose)
  alias(libs.plugins.android.library)
  alias(libs.plugins.maven.publish)
}

kotlin {
  applyDefaultHierarchyTemplate()

  androidTarget {
    publishLibraryVariants("release")

    compilations.all {
      kotlinOptions {
        jvmTarget = "17"
      }
    }
  }

  jvm()

  js {
    browser()
    binaries.executable()
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    all {
      languageSettings {
//        optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
      }
    }
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
//      @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
//      implementation(compose.components.resources)
      implementation(libs.kotlinx.datetime)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    androidMain.dependencies {
      implementation(libs.androidx.activityCompose)
    }

    jvmMain.dependencies {
      implementation(compose.desktop.common)
      implementation(compose.desktop.currentOs)
    }

    jsMain.dependencies {
      implementation(compose.html.core)
    }

    iosMain.dependencies {
    }

  }
}

android {
  namespace = "dev.darkokoa.datetimewheelpicker"
  compileSdk = 34

  defaultConfig {
    minSdk = 21
  }
  sourceSets["main"].apply {
    manifest.srcFile("src/androidMain/AndroidManifest.xml")
    res.srcDirs("src/androidMain/resources")
    resources.srcDirs("src/commonMain/resources")
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "dev.darkokoa.datetimewheelpicker.desktopApp"
      packageVersion = "1.0.0"
    }
  }
}

compose.experimental {
  web.application {}
}

mavenPublishing {
  publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01, automaticRelease = true)
  signAllPublications()
}