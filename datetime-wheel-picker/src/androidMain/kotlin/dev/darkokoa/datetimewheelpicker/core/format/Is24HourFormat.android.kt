package dev.darkokoa.datetimewheelpicker.core.format

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Reads the system 12/24-hour preference via [DateFormat.is24HourFormat], which
 * is backed by an in-process cache of `Settings.System.TIME_12_24` and is cheap
 * to call.
 *
 * `Settings.System.TIME_12_24` is **not** part of `Configuration` and toggling
 * it does not invalidate `LocalContext`/`LocalConfiguration`, so a Picker
 * sitting in composition would otherwise never recompose after the user
 * returns from "Settings → Date & time → Use 24-hour format".
 *
 * The bridge is a single [LifecycleResumeEffect] that re-reads the value on
 * each `ON_RESUME` — the realistic flow of "user opens system settings →
 * changes preference → returns to the app". The state is held in [remember]
 * so the very first composition already reflects the correct value (no
 * flicker), and writes go through a value-equality check to avoid spurious
 * recompositions.
 */
@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  val context = LocalContext.current
  var value by remember(context) { mutableStateOf(DateFormat.is24HourFormat(context)) }
  LifecycleResumeEffect(context) {
    val v = DateFormat.is24HourFormat(context)
    if (v != value) value = v
    onPauseOrDispose { }
  }
  return value
}
