plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "dev.darkokoa.datetimewheelpicker.androidapp"
  compileSdk = 36

  defaultConfig {
    applicationId = "dev.darkokoa.datetimewheelpicker.androidApp"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {
  implementation(project(":sample:composeApp"))
  implementation(libs.androidx.activityCompose)
}
