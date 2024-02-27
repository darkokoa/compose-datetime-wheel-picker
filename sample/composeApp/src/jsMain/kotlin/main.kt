import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import dev.darkokoa.datetimewheelpicker.App
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  onWasmReady {
    CanvasBasedWindow("datetime-wheel-picker") {
      App()
    }
  }
}
