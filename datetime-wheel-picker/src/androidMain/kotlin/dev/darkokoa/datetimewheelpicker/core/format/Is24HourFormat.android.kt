package dev.darkokoa.datetimewheelpicker.core.format

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext

/**
 * Process-wide reference to the application [Context], populated lazily on the
 * very first Composable call site. The application context is leak-free and is
 * the only piece of [Context] required for both reading
 * [DateFormat.is24HourFormat] and registering a [ContentObserver] on
 * `Settings.System.TIME_12_24`.
 */
private lateinit var appContext: Context

/**
 * Process-wide cached state. Initialized lazily so that the surrounding
 * top-level `val` does not need a [Context] at class-init time — the [lazy]
 * block runs on the first Composable read, by which point [appContext] is
 * guaranteed to have been populated by [isSystem24HourFormat].
 *
 * On first access this:
 * 1. Reads the initial 12/24h value via [DateFormat.is24HourFormat], which is
 *    a cheap in-process cached read of `Settings.System.TIME_12_24`.
 * 2. Subscribes a single [ContentObserver] to the precise `Uri` for
 *    `Settings.System.TIME_12_24`. Reading and observing this setting do not
 *    require any permission; only writing it would (`WRITE_SETTINGS`).
 *
 * The observer lives for the lifetime of the process — this matches the iOS /
 * JS / WasmJS implementations, where listeners are also process-scoped. All
 * Picker call sites share this single state and a single observer regardless
 * of how many [isSystem24HourFormat] call sites are present, eliminating the
 * per-composition `LifecycleResumeEffect` registration of the previous
 * implementation. Writes go through a value-equality check to suppress
 * redundant recompositions.
 */
private val is24HourState: MutableState<Boolean> by lazy {
  val state = mutableStateOf(DateFormat.is24HourFormat(appContext))
  val uri = Settings.System.getUriFor(Settings.System.TIME_12_24)
  appContext.contentResolver.registerContentObserver(
    uri,
    /* notifyForDescendants = */ false,
    object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean) {
        val v = DateFormat.is24HourFormat(appContext)
        if (v != state.value) state.value = v
      }
    }
  )
  state
}

/**
 * Returns whether the device prefers the 24-hour clock.
 *
 * Reads the system 12/24-hour preference via [DateFormat.is24HourFormat], which
 * is backed by an in-process cache of `Settings.System.TIME_12_24`.
 *
 * `Settings.System.TIME_12_24` is **not** part of `Configuration`, so toggling
 * it does not invalidate `LocalContext`/`LocalConfiguration`. To still pick up
 * the change in real time without polling, a single process-wide
 * [ContentObserver] is registered against the precise setting `Uri` (see
 * [is24HourState]). The Composable call site itself is a pure read of the
 * shared [MutableState]; no per-composition effects are registered.
 */
@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  if (!::appContext.isInitialized) {
    appContext = LocalContext.current.applicationContext
  }
  val value by is24HourState
  return value
}
