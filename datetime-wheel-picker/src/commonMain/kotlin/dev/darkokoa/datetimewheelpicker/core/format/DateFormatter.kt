package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import dev.darkokoa.datetimewheelpicker.rememberStrings
import dev.darkokoa.datetimewheelpicker.strings.EnStrings
import dev.darkokoa.datetimewheelpicker.strings.Strings
import kotlinx.datetime.Month
import kotlinx.datetime.number

@Stable
interface DateFormatter {
  val dateOrder: DateOrder
  val monthDisplayStyle: MonthDisplayStyle
  val formatYear: (Int) -> String
  val formatMonth: (Month, MonthDisplayStyle) -> String
  val formatDay: (Int) -> String
}

fun DateFormatter.formatMonth(month: Month) = formatMonth(month, monthDisplayStyle)

private class DateFormatterImpl(
  override val dateOrder: DateOrder,
  override val monthDisplayStyle: MonthDisplayStyle,
  override val formatYear: (Int) -> String,
  override val formatMonth: (Month, MonthDisplayStyle) -> String,
  override val formatDay: (Int) -> String
) : DateFormatter

fun dateFormatter(
  dateOrder: DateOrder = DateOrder.DMY,
  monthDisplayStyle: MonthDisplayStyle = MonthDisplayStyle.FULL,
  formatYear: (Int) -> String = { it.toLocalizedNumerals() },
  formatMonth: (Month, MonthDisplayStyle) -> String = { month, style ->
    val strings = (dev.darkokoa.datetimewheelpicker.Strings[Locale.current.language] ?: EnStrings)
    when (style) {
      MonthDisplayStyle.FULL -> month.fullString(strings)
      MonthDisplayStyle.SHORT -> month.shortString(strings)
      MonthDisplayStyle.NUMERIC -> month.number.toLocalizedNumerals(strings)
    }
  },
  formatDay: (Int) -> String = { it.toLocalizedNumerals() }
): DateFormatter = DateFormatterImpl(
  dateOrder = dateOrder,
  monthDisplayStyle = monthDisplayStyle,
  formatYear = formatYear,
  formatMonth = formatMonth,
  formatDay = formatDay
)

internal fun dateFormatter(
  strings: Strings,
  dateOrder: DateOrder = DateOrder.DMY,
  monthDisplayStyle: MonthDisplayStyle = MonthDisplayStyle.FULL,
  formatYear: (Int) -> String = { it.toLocalizedNumerals(strings) },
  formatMonth: (Month, MonthDisplayStyle) -> String = { month, style ->
    when (style) {
      MonthDisplayStyle.FULL -> month.fullString(strings)
      MonthDisplayStyle.SHORT -> month.shortString(strings)
      MonthDisplayStyle.NUMERIC -> month.number.toLocalizedNumerals(strings)
    }
  },
  formatDay: (Int) -> String = { it.toLocalizedNumerals(strings) }
): DateFormatter = DateFormatterImpl(
  dateOrder = dateOrder,
  monthDisplayStyle = monthDisplayStyle,
  formatYear = formatYear,
  formatMonth = formatMonth,
  formatDay = formatDay
)

@Composable
fun dateFormatter(
  locale: Locale,
  monthDisplayStyle: MonthDisplayStyle
): DateFormatter {
  val lyricist = rememberStrings(currentLanguageTag = locale.language)

  return remember(lyricist.strings, locale, monthDisplayStyle) {
    dateFormatter(
      strings = lyricist.strings,
      dateOrder = DateOrder.match(locale),
      monthDisplayStyle = monthDisplayStyle
    )
  }
}

internal fun Month.fullString(strings: Strings): String {
  return when (number) {
    1 -> strings.monthJanuaryFull
    2 -> strings.monthFebruaryFull
    3 -> strings.monthMarchFull
    4 -> strings.monthAprilFull
    5 -> strings.monthMayFull
    6 -> strings.monthJuneFull
    7 -> strings.monthJulyFull
    8 -> strings.monthAugustFull
    9 -> strings.monthSeptemberFull
    10 -> strings.monthOctoberFull
    11 -> strings.monthNovemberFull
    12 -> strings.monthDecemberFull
    else -> error("Invalid month number: $number")
  }
}

internal fun Month.shortString(strings: Strings): String {
  return when (number) {
    1 -> strings.monthJanuaryShort
    2 -> strings.monthFebruaryShort
    3 -> strings.monthMarchShort
    4 -> strings.monthAprilShort
    5 -> strings.monthMayShort
    6 -> strings.monthJuneShort
    7 -> strings.monthJulyShort
    8 -> strings.monthAugustShort
    9 -> strings.monthSeptemberShort
    10 -> strings.monthOctoberShort
    11 -> strings.monthNovemberShort
    12 -> strings.monthDecemberShort
    else -> error("Invalid month number: $number")
  }
}
