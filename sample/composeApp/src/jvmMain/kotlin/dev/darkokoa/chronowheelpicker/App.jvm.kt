package dev.darkokoa.chronowheelpicker

import java.awt.Desktop
import java.net.URI

internal actual fun openUrl(url: String?) {
  val uri = url?.let { URI.create(it) } ?: return
  Desktop.getDesktop().browse(uri)
}