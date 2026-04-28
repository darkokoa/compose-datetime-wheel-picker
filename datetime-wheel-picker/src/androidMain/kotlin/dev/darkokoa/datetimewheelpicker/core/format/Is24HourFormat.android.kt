package dev.darkokoa.datetimewheelpicker.core.format

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
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
 * [DateFormat.is24HourFormat] and registering a process-wide
 * [BroadcastReceiver] for clock/locale change broadcasts.
 */
private lateinit var appContext: Context

/**
 * Process-wide cached state. Initialized lazily so that the surrounding
 * top-level `val` does not need a [Context] at class-init time — the [lazy]
 * block runs on the first Composable read, by which point [appContext] is
 * guaranteed to have been populated by [isSystem24HourFormat].
 *
 * On first access this:
 * 1. Reads the initial 12/24h value via [DateFormat.is24HourFormat], which
 *    reads `Settings.System.TIME_12_24` (no permission required).
 * 2. Subscribes a single [BroadcastReceiver] to the system broadcasts that
 *    accompany a 12/24h preference change:
 *    - [Intent.ACTION_TIME_CHANGED] — fired by the framework
 *      `AlarmManagerService` whenever the user toggles the 12/24h preference
 *      (and on raw clock changes).
 *    - [Intent.ACTION_TIMEZONE_CHANGED] — defensive: some OEM ROMs route
 *      time-format updates through this signal as well.
 *    - [Intent.ACTION_LOCALE_CHANGED] — covers the edge case where changing
 *      the system language flips the locale-derived 12/24h default.
 *
 * Why broadcasts instead of a `ContentObserver` on
 * `Settings.System.TIME_12_24`? On stock Android (e.g. Pixel) the URI observer
 * works, but several OEM ROMs (notably Vivo OriginOS / FuntouchOS, plus other
 * heavily customized ROMs) either store the 12/24h preference in a private
 * provider or suppress the standard `notifyChange` for that URI, which means
 * the observer never fires. The broadcasts above are framework-level
 * protected broadcasts that those ROMs do not modify, making this approach
 * portable across OEMs. None of these broadcasts require any permission.
 *
 * The receiver lives for the lifetime of the process — this matches the iOS /
 * JS / WasmJS implementations, where listeners are also process-scoped. All
 * Picker call sites share this single state and a single receiver regardless
 * of how many [isSystem24HourFormat] call sites are present, eliminating the
 * per-composition `LifecycleResumeEffect` registration of the original
 * implementation. Writes go through a value-equality check to suppress
 * redundant recompositions.
 */
private val is24HourState: MutableState<Boolean> by lazy {
  val state = mutableStateOf(DateFormat.is24HourFormat(appContext))
  registerSystemTimeFormatReceiver(appContext) {
    val v = DateFormat.is24HourFormat(appContext)
    if (v != state.value) state.value = v
  }
  state
}

@SuppressLint("InlinedApi", "UnspecifiedRegisterReceiverFlag")
private fun registerSystemTimeFormatReceiver(context: Context, onChanged: () -> Unit) {
  val filter = IntentFilter().apply {
    addAction(Intent.ACTION_TIME_CHANGED)
    addAction(Intent.ACTION_TIMEZONE_CHANGED)
    addAction(Intent.ACTION_LOCALE_CHANGED)
  }
  val receiver = object : BroadcastReceiver() {
    override fun onReceive(c: Context?, i: Intent?) = onChanged()
  }
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
  } else {
    context.registerReceiver(receiver, filter)
  }
}

/**
 * Returns whether the device prefers the 24-hour clock.
 *
 * Reads the system 12/24-hour preference via [DateFormat.is24HourFormat], which
 * is backed by `Settings.System.TIME_12_24`.
 *
 * `Settings.System.TIME_12_24` is **not** part of `Configuration`, so toggling
 * it does not invalidate `LocalContext`/`LocalConfiguration`. To still pick up
 * the change in real time without polling — and to do so portably across OEM
 * ROMs that do not honor a `ContentObserver` on this setting — a single
 * process-wide [BroadcastReceiver] is registered for time/locale change
 * broadcasts (see [is24HourState]). The Composable call site itself is a
 * pure read of the shared [MutableState]; no per-composition effects are
 * registered.
 */
@Composable
internal actual fun isSystem24HourFormat(): Boolean {
  if (!::appContext.isInitialized) {
    appContext = LocalContext.current.applicationContext
  }
  val value by is24HourState
  return value
}
