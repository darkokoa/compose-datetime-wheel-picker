import com.google.devtools.ksp.gradle.KspTaskMetadata
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.android.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.ksp)
}

kotlin {
  applyDefaultHierarchyTemplate()

  androidTarget {
    publishLibraryVariants("release")

    compilations.all {
      compileTaskProvider.configure {
        compilerOptions {
          jvmTarget.set(JvmTarget.JVM_17)
        }
      }
    }
  }

  jvm {
    compilations.all {
      compileTaskProvider.configure {
        compilerOptions {
          jvmTarget.set(JvmTarget.JVM_17)
        }
      }
    }
  }

  js {
    browser()
    binaries.executable()
  }

  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
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

      }
    }
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.material3)
      implementation(libs.kotlinx.datetime)

      implementation(libs.lyricist)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    androidMain.dependencies {
    }

    jvmMain.dependencies {
    }

    jsMain.dependencies {
    }

    wasmJsMain.dependencies {
    }

    iosMain.dependencies {
    }

  }
}

dependencies {
  add("kspCommonMainMetadata", libs.lyricist.processor)
}

kotlin.sourceSets.commonMain {
  tasks.withType<KspTaskMetadata> { kotlin.srcDir(destinationDirectory) }
}

ksp {
  arg("lyricist.internalVisibility", "true")
  arg("lyricist.packageName", "dev.darkokoa.datetimewheelpicker")
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

mavenPublishing {
  publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01, automaticRelease = true)
  signAllPublications()
}