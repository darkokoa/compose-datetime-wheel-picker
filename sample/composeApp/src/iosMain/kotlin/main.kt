import androidx.compose.ui.window.ComposeUIViewController
import dev.darkokoa.datetimewheelpicker.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController(
  configure = { enforceStrictPlistSanityCheck = false },
  content = { App() }
)
