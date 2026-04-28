package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import java.awt.KeyboardFocusManager
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
 * Process-wide cached state. The JDK exposes no event for system-level 12/24h
 * preference changes, so we re-evaluate whenever the application transitions
 * from "no window focused" to "some window focused" — i.e. the user comes
 * back to the app after potentially toggling the system preference.
 *
 * A single global [KeyboardFocusManager] PropertyChangeListener is registered
 * here, regardless of how many Picker instances or windows the application
 * has. Compared to the previous per-Composable `LaunchedEffect(focused)`
 * implementation that ran once per Picker per window-focus flip and once per
 * in-app screen navigation, this:
 *  - reduces the number of listeners from O(Pickers) to exactly 1, and
 *  - decouples re-evaluation from in-app screen navigation entirely (the
 *    AWT focused-window event does not fire when only the Compose subtree
 *    changes inside the same Window).
 *
 * The `oldValue == null && newValue != null` filter narrows the trigger to
 * "the application as a whole regained keyboard focus", ignoring purely
 * intra-process focus shuffles between sibling windows (e.g. opening a
 * dialog) which cannot affect the 12/24h preference. Writes go through a
 * value-equality check to suppress redundant recompositions.
 *
 * The listener lives for the lifetime of the process — this matches the
 * iOS / JS / WasmJS / Android implementations, which also register
 * process-scoped observers.
 *
 * Headless mode (`java.awt.headless=true`) is a benign degradation: the
 * initial value is read at first access, and no focus events will ever fire
 * because there is no window to focus, but no Picker UI is visible there
 * either.
 */
private val is24HourState: MutableState<Boolean> = mutableStateOf(localeImplies24Hour()).also { state ->
  KeyboardFocusManager.getCurrentKeyboardFocusManager()
    .addPropertyChangeListener("focusedWindow") { evt ->
      if (evt.oldValue == null && evt.newValue != null) {
        val v = localeImplies24Hour()
        if (v != state.value) state.value = v
      }
    }
}

@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  val value by is24HourState
  return value
}
