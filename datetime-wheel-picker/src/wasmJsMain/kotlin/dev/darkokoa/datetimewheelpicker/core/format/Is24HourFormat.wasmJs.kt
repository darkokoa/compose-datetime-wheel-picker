package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.browser.document
import kotlinx.browser.window

internal fun resolveHour12Js(): Boolean =
  js(
    """{
      var o = new Intl.DateTimeFormat(undefined,{hour:'numeric'}).resolvedOptions();
      if (typeof o.hour12 === 'boolean') return o.hour12;
      return o.hourCycle === 'h11' || o.hourCycle === 'h12';
    }"""
  )

private fun isDocumentVisible(): Boolean =
  js("document.visibilityState === 'visible'")

private fun systemPrefers24Hour(): Boolean = !resolveHour12Js()

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
  window.addEventListener("languagechange", { _ -> refresh() })
  document.addEventListener("visibilitychange", { _ ->
    if (isDocumentVisible()) refresh()
  })
}

@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  val value by is24HourState
  return value
}
