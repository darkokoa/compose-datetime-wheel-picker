package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.EventListener

internal fun resolveHour12(): Boolean {
  val opts = js("new Intl.DateTimeFormat(undefined,{hour:'numeric'}).resolvedOptions()")
  val h12: dynamic = opts.hour12
  if (h12 != null) return h12.unsafeCast<Boolean>()
  val cycle: String? = opts.hourCycle as? String
  return cycle == "h11" || cycle == "h12"
}

private fun systemPrefers24Hour(): Boolean = !resolveHour12()

/**
 * Process-wide cached state. Listeners cover:
 *  - `languagechange` on `window` — precise signal for `navigator.language`.
 *  - `visibilitychange` on `document` — heuristic refresh when the user
 *    returns to the tab (mirrors JVM's "window regained focus" approach).
 *
 * Listeners live for the lifetime of the page; no removal required.
 * A value-equality check prevents redundant recompositions.
 */
private val is24HourState: MutableState<Boolean> = mutableStateOf(systemPrefers24Hour()).also { state ->
  fun refresh() {
    val v = systemPrefers24Hour()
    if (v != state.value) state.value = v
  }
  window.addEventListener("languagechange", EventListener { refresh() })
  document.addEventListener(
    "visibilitychange",
    EventListener {
      val visible: dynamic = document.asDynamic().visibilityState
      if (visible == "visible") refresh()
    }
  )
}

@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  val value by is24HourState
  return value
}
