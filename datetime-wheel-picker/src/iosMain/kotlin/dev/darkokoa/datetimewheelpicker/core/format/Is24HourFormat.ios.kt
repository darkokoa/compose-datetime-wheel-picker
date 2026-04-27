package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import platform.Foundation.NSCurrentLocaleDidChangeNotification
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSSystemClockDidChangeNotification
import platform.Foundation.currentLocale

private fun systemPrefers24Hour(): Boolean {
  // Apple QA1480: ask ICU for the locale's preferred hour symbol via the "j" template.
  val pattern = NSDateFormatter.dateFormatFromTemplate(
    "j",
    0u,
    NSLocale.currentLocale
  ).orEmpty()
  // Presence of 'a' indicates an AM/PM marker → 12-hour format.
  return !pattern.contains('a')
}

/**
 * Process-wide cached state. A single observer reacts to:
 *  - `NSCurrentLocaleDidChangeNotification` — language/region/hourCycle changes.
 *  - `NSSystemClockDidChangeNotification`   — extra safety net for 12/24h toggle.
 *
 * Pending notifications are delivered when the app returns to the foreground,
 * so an explicit foreground refresh is unnecessary. The observer lives for the
 * lifetime of the process and does not need to be removed.
 */
private val is24HourState: MutableState<Boolean> = mutableStateOf(systemPrefers24Hour()).also { state ->
  val center = NSNotificationCenter.defaultCenter
  val refresh: (platform.Foundation.NSNotification?) -> Unit = {
    val v = systemPrefers24Hour()
    if (v != state.value) state.value = v
  }
  center.addObserverForName(
    name = NSCurrentLocaleDidChangeNotification,
    `object` = null,
    queue = NSOperationQueue.mainQueue,
    usingBlock = refresh
  )
  center.addObserverForName(
    name = NSSystemClockDidChangeNotification,
    `object` = null,
    queue = NSOperationQueue.mainQueue,
    usingBlock = refresh
  )
}

@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  val value by is24HourState
  return value
}
