package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import datetime_wheel_picker.datetime_wheel_picker.generated.resources.*
import kotlinx.datetime.Month
import kotlinx.datetime.number
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Stable
interface DateFormatter {
  val dateOrder: DateOrder
  val monthDisplayStyle: MonthDisplayStyle
  val formatYear: (Int) -> String
  val formatMonth: @Composable (Month, MonthDisplayStyle) -> String
  val formatDay: (Int) -> String
}

@Composable
fun DateFormatter.formatMonth(month: Month) = formatMonth(month, monthDisplayStyle)

private class DateFormatterImpl(
  override val dateOrder: DateOrder,
  override val monthDisplayStyle: MonthDisplayStyle,
  override val formatYear: (Int) -> String,
  override val formatMonth: @Composable (Month, MonthDisplayStyle) -> String,
  override val formatDay: (Int) -> String
) : DateFormatter

fun dateFormatter(
  dateOrder: DateOrder = DateOrder.DMY,
  monthDisplayStyle: MonthDisplayStyle = MonthDisplayStyle.FULL,
  formatYear: (Int) -> String = { it.toString() },
  formatMonth: @Composable (Month, MonthDisplayStyle) -> String = { month, style ->
    when (style) {
      MonthDisplayStyle.FULL -> stringResource(month.fullStringRes)
      MonthDisplayStyle.SHORT -> stringResource(month.shortStringRes)
      MonthDisplayStyle.NUMERIC -> month.number.toString()
    }
  },
  formatDay: (Int) -> String = { it.toString() }
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
) = remember(locale, monthDisplayStyle) {
  dateFormatter(
    dateOrder = DateOrder.match(locale),
    monthDisplayStyle = monthDisplayStyle
  )
}


private val Month.fullStringRes: StringResource
  get() = when (number) {
    1 -> Res.string.month_january_full
    2 -> Res.string.month_february_full
    3 -> Res.string.month_march_full
    4 -> Res.string.month_april_full
    5 -> Res.string.month_may_full
    6 -> Res.string.month_june_full
    7 -> Res.string.month_july_full
    8 -> Res.string.month_august_full
    9 -> Res.string.month_september_full
    10 -> Res.string.month_october_full
    11 -> Res.string.month_november_full
    12 -> Res.string.month_december_full
    else -> error("Unknown month: $number")
  }

private val Month.shortStringRes: StringResource
  get() = when (number) {
    1 -> Res.string.month_january_short
    2 -> Res.string.month_february_short
    3 -> Res.string.month_march_short
    4 -> Res.string.month_april_short
    5 -> Res.string.month_may_short
    6 -> Res.string.month_june_short
    7 -> Res.string.month_july_short
    8 -> Res.string.month_august_short
    9 -> Res.string.month_september_short
    10 -> Res.string.month_october_short
    11 -> Res.string.month_november_short
    12 -> Res.string.month_december_short
    else -> error("Unknown month: $number")
  }
