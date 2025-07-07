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
  val yearDisplayStyle: DisplayStyle
  val monthDisplayStyle: DisplayStyle
  val dayDisplayStyle: DisplayStyle
  val formatYear: (Int, DisplayStyle) -> String
  val formatMonth: (Month, DisplayStyle) -> String
  val formatDay: (Int, DisplayStyle) -> String
}

fun DateFormatter.formatYear(year: Int) = formatYear(year, monthDisplayStyle)
fun DateFormatter.formatMonth(month: Month) = formatMonth(month, monthDisplayStyle)
fun DateFormatter.formatDay(day: Int) = formatDay(day, monthDisplayStyle)

private class DateFormatterImpl(
  override val dateOrder: DateOrder,
  override val dayDisplayStyle: DisplayStyle,
  override val monthDisplayStyle: DisplayStyle,
  override val yearDisplayStyle: DisplayStyle,
  override val formatYear: (Int, DisplayStyle) -> String,
  override val formatMonth: (Month, DisplayStyle) -> String,
  override val formatDay: (Int, DisplayStyle) -> String
) : DateFormatter

fun dateFormatter(
  dateOrder: DateOrder = DateOrder.DMY,
  yearDisplayStyle: DisplayStyle = DisplayStyle.FULL,
  monthDisplayStyle: DisplayStyle = DisplayStyle.FULL,
  dayDisplayStyle: DisplayStyle = DisplayStyle.FULL,
  formatYear: (Int, DisplayStyle) -> String = { year, _ -> year.toLocalizedNumerals() },
  formatMonth: (Month, DisplayStyle) -> String = { month, style ->
    val strings = (dev.darkokoa.datetimewheelpicker.Strings[Locale.current.language] ?: EnStrings)
    when (style) {
      DisplayStyle.FULL -> month.fullString(strings)
      DisplayStyle.SHORT -> month.shortString(strings)
      DisplayStyle.NUMERIC -> month.number.toLocalizedNumerals(strings)
    }
  },
  formatDay: (Int, DisplayStyle) -> String = { day, _ -> day.toLocalizedNumerals() }
): DateFormatter = DateFormatterImpl(
  dateOrder = dateOrder,
  dayDisplayStyle = dayDisplayStyle,
  monthDisplayStyle = monthDisplayStyle,
  yearDisplayStyle = yearDisplayStyle,
  formatYear = formatYear,
  formatMonth = formatMonth,
  formatDay = formatDay
)

internal fun dateFormatter(
  strings: Strings,
  dateOrder: DateOrder = DateOrder.DMY,
  yearDisplayStyle: DisplayStyle = DisplayStyle.FULL,
  monthDisplayStyle: DisplayStyle = DisplayStyle.FULL,
  dayDisplayStyle: DisplayStyle = DisplayStyle.FULL,
  formatYear: (Int, DisplayStyle) -> String = { year, _ -> year.toLocalizedNumerals(strings) },
  formatMonth: (Month, DisplayStyle) -> String = { month, style ->
    when (style) {
      DisplayStyle.FULL -> month.fullString(strings)
      DisplayStyle.SHORT -> month.shortString(strings)
      DisplayStyle.NUMERIC -> month.number.toLocalizedNumerals(strings)
    }
  },
  formatDay: (Int, DisplayStyle) -> String = { day, _ -> day.toLocalizedNumerals(strings) }
): DateFormatter = DateFormatterImpl(
  dateOrder = dateOrder,
  yearDisplayStyle = yearDisplayStyle,
  monthDisplayStyle = monthDisplayStyle,
  dayDisplayStyle = dayDisplayStyle,
  formatYear = formatYear,
  formatMonth = formatMonth,
  formatDay = formatDay
)

@Composable
fun dateFormatter(
  locale: Locale,
  yearDisplayStyle: DisplayStyle,
  monthDisplayStyle: DisplayStyle,
  dayDisplayStyle: DisplayStyle
): DateFormatter {
  val lyricist = rememberStrings(currentLanguageTag = locale.language)

  return remember(lyricist.strings, locale, yearDisplayStyle, monthDisplayStyle, dayDisplayStyle) {
    dateFormatter(
      strings = lyricist.strings,
      dateOrder = DateOrder.match(locale),
      yearDisplayStyle = yearDisplayStyle,
      monthDisplayStyle = monthDisplayStyle,
      dayDisplayStyle = dayDisplayStyle
    )
  }
}

@Composable
fun dateFormatter(
  locale: Locale,
  displayStyle: DisplayStyle,
): DateFormatter {
  val lyricist = rememberStrings(currentLanguageTag = locale.language)

  return remember(lyricist.strings, locale, displayStyle) {
    dateFormatter(
      strings = lyricist.strings,
      dateOrder = DateOrder.match(locale),
      yearDisplayStyle = displayStyle,
      monthDisplayStyle = displayStyle,
      dayDisplayStyle = displayStyle
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
