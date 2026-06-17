import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
  android {
    namespace = "dev.darkokoa.datetimewheelpicker.sample"
    compileSdk = 36
    minSdk = 24

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  jvm()

  js {
    browser()
    binaries.executable()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser {
      commonWebpackConfig {
        outputFileName = "composeApp.js"
      }
    }

    binaries.executable()
  }

  listOf(
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
        optIn("kotlin.time.ExperimentalTime")
      }
    }
    commonMain.dependencies {
      implementation(project(":datetime-wheel-picker"))

      implementation(compose.runtime)
      implementation(compose.material3)
      implementation(libs.kotlinx.datetime)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    androidMain.dependencies {
    }

    jvmMain.dependencies {
//      implementation(compose.desktop.common)
      implementation(compose.desktop.currentOs)
    }

    jsMain.dependencies {
      implementation(compose.html.core)
    }

    wasmJsMain.dependencies {
    }

    iosMain.dependencies {
    }

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

// https://youtrack.jetbrains.com/issue/CMP-4906
tasks.withType<org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest> {
  enabled = false
}

tasks.withType<Test> {
  failOnNoDiscoveredTests.set(false)
}
