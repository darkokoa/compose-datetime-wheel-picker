package dev.darkokoa.datetimewheelpicker

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class AndroidApp : Application() {
  companion object {
    lateinit var INSTANCE: AndroidApp
  }

  override fun onCreate() {
    super.onCreate()
    INSTANCE = this
  }
}

class AppActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Transparent system bars on every API level (targetSdk 36 already forces this on Android 15+),
    // so the app's own top and bottom bars show through. This follows the system theme; App() then
    // reports the theme in effect, which the in-app toggle can make differ from the system one.
    enableEdgeToEdge()
    setContent {
      App(onDarkThemeChange = ::applySystemBarStyle)
    }
  }

  /**
   * The window theme follows the system light/dark setting, so when the app's theme is the opposite
   * the bars would be tinted for the wrong background. Running enableEdgeToEdge again with the app's
   * theme as the dark-mode signal fixes the status and navigation bar icon tint, and the navigation
   * bar scrim on API 26-28, which is chosen from the same signal.
   */
  private fun applySystemBarStyle(isDarkTheme: Boolean) {
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDarkTheme },
      navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { isDarkTheme },
    )
  }
}

// The defaults of enableEdgeToEdge(); androidx.activity does not expose them.
private val LightScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
