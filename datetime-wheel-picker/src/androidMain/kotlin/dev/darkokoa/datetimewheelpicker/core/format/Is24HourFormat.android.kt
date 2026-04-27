package dev.darkokoa.datetimewheelpicker.core.format

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Reads the system 12/24-hour preference via [DateFormat.is24HourFormat], which
 * is backed by an in-process cache of `Settings.System.TIME_12_24` and is cheap
 * enough to call on every recomposition — no extra caching layer is needed
 * (unlike the iOS / JVM / JS / WasmJS actual implementations).
 *
 * No explicit `LocalConfiguration.current` subscription is required:
 *  - `Settings.System.TIME_12_24` is **not** part of `Configuration`, so a
 *    Configuration subscription would not catch the user toggling 12/24h anyway.
 *  - Locale changes propagate through the usual recomposition paths
 *    (Activity recreation or `LocalContext` invalidation), which already
 *    re-invoke this function.
 */
@Composable
internal actual fun isSystem24HourFormat(): Boolean =
  DateFormat.is24HourFormat(LocalContext.current)
