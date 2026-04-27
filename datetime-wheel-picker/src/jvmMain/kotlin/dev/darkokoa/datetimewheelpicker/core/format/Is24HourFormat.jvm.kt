package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalWindowInfo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Infer 12/24h preference from the FORMAT locale's CLDR data.
 *
 * Assumption: the user's 12/24h preference agrees with their FORMAT locale's
 * CLDR convention. Unlike Android (`Settings.System.TIME_12_24`) or iOS (system
 * preference), JVM desktop has no cross-platform API to read the OS-level 12/24h
 * toggle directly, so this function cannot reflect a manual override that
 * diverges from the locale.
 */
internal fun localeImplies24Hour(locale: Locale): Boolean {
  val df = DateFormat.getTimeInstance(DateFormat.SHORT, locale) as? SimpleDateFormat
  val pattern = df?.toPattern().orEmpty()
  // Unicode LDML: 'a' = AM/PM marker, 'h' = 1-12 hour, 'K' = 0-11 hour.
  return !(pattern.contains('a') || pattern.contains('h') || pattern.contains('K'))
}

private fun localeImplies24Hour(): Boolean =
  localeImplies24Hour(Locale.getDefault(Locale.Category.FORMAT))

/**
 * Process-wide cached state. The JDK does not expose an event for system-level
 * 12/24h preference changes, so we re-evaluate whenever the host window
 * regains focus — a cheap heuristic that covers the realistic flow of
 * "user opens system settings → changes preference → returns to the app".
 *
 * Multiple Picker instances share this single state; concurrent writes go
 * through a value-equality check to avoid spurious recompositions.
 */
private val is24HourState: MutableState<Boolean> = mutableStateOf(localeImplies24Hour())

@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  val focused = LocalWindowInfo.current.isWindowFocused
  LaunchedEffect(focused) {
    if (focused) {
      val v = localeImplies24Hour()
      if (v != is24HourState.value) is24HourState.value = v
    }
  }
  val value by is24HourState
  return value
}
