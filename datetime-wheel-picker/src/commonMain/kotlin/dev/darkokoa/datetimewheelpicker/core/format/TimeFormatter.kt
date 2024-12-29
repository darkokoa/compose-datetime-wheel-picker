package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import datetime_wheel_picker.datetime_wheel_picker.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Stable
interface TimeFormatter {
  val timeFormat: TimeFormat
  val formatHour: (Int) -> String
  val formatMinute: (Int) -> String
  val formatAmText: @Composable () -> String
  val formatPmText: @Composable () -> String
}

private class TimeFormatterImpl(
  override val timeFormat: TimeFormat,
  override val formatHour: (Int) -> String,
  override val formatMinute: (Int) -> String,
  override val formatAmText: @Composable () -> String,
  override val formatPmText: @Composable () -> String,
) : TimeFormatter

fun timeFormatter(
  timeFormat: TimeFormat = TimeFormat.HOUR_24,
  formatHour: (Int) -> String = { hour -> hour.toString().padStart(2, '0') },
  formatMinute: (Int) -> String = { minute -> minute.toString().padStart(2, '0') },
  formatAmText: @Composable () -> String = { stringResource(Res.string.time_am) },
  formatPmText: @Composable () -> String = { stringResource(Res.string.time_pm) },
): TimeFormatter = TimeFormatterImpl(
  timeFormat = timeFormat,
  formatHour = formatHour,
  formatMinute = formatMinute,
  formatAmText = formatAmText,
  formatPmText = formatPmText
)

@Composable
fun timeFormatter(
  locale: Locale,
) = remember(locale) {
  timeFormatter(
    timeFormat = when {
      locale.language == "en" || locale.region in listOf("US", "GB") -> TimeFormat.AM_PM
      else -> TimeFormat.HOUR_24
    }
  )
}
