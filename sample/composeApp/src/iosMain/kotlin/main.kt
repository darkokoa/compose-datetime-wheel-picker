import androidx.compose.ui.window.ComposeUIViewController
import dev.darkokoa.chronowheelpicker.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
