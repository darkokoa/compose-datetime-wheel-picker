package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable

/**
 * Returns whether the current device/environment prefers the 24-hour clock.
 *
 * This is a cheap, side-effect-free query; it is safe (and intended) to be
 * called on every recomposition. The value naturally refreshes whenever the
 * surrounding composition recomposes (locale change, configuration change,
 * navigation, etc.).
 *
 * Platform behavior:
 * - **Android**: `android.text.format.DateFormat.is24HourFormat(context)`
 *   (an in-process cached read of `Settings.System.TIME_12_24`).
 * - **iOS**: Apple QA1480 — `NSDateFormatter.dateFormatFromTemplate("j", …)`
 *   inspected for the AM/PM marker `'a'`.
 * - **JVM (desktop)**: `DateFormat.getTimeInstance(SHORT, Locale.getDefault(FORMAT))` pattern.
 * - **JS / WasmJS**: `Intl.DateTimeFormat().resolvedOptions().hour12` (with `hourCycle` fallback).
 */
@Composable
internal expect fun isSystem24HourFormat(): Boolean
