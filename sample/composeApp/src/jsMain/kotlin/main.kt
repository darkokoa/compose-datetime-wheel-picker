import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.darkokoa.datetimewheelpicker.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  val body = document.body ?: return
  ComposeViewport(body) {
    App()
  }
}
