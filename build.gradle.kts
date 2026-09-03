plugins {
  alias(libs.plugins.multiplatform).apply(false)
  alias(libs.plugins.compose).apply(false)
  alias(libs.plugins.compose.compiler).apply(false)
  id("com.android.application") apply false
  id("com.android.kotlin.multiplatform.library") apply false
  id("com.android.library") apply false
  alias(libs.plugins.maven.publish).apply(false)
  alias(libs.plugins.ksp).apply(false)
}
