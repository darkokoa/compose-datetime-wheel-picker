package dev.darkokoa.datetimewheelpicker

import kotlinx.browser.window

internal actual fun openUrl(url: String?) {
  url?.let { window.open(it) }
}