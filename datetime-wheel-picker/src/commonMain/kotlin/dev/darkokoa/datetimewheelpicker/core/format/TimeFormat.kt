package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable

enum class TimeFormat {
  HOUR_24,    // 24-hour format
  AM_PM;      // 12-hour format with AM/PM

  companion object {
    /**
     * Returns [HOUR_24] or [AM_PM] based on the current device/environment preference.
     *
     * - **Android**: reads `android.text.format.DateFormat.is24HourFormat(context)`.
     * - **iOS**: derived from the locale-aware hour cycle (Apple QA1480).
     * - **Desktop / Web**: derived from the current locale.
     *
     * The value is read on each composition; it naturally refreshes whenever the
     * surrounding composition recomposes (e.g. on locale or configuration changes).
     */
    @Composable
    fun systemDefault(): TimeFormat =
      if (isSystem24HourFormat()) HOUR_24 else AM_PM
  }
}
