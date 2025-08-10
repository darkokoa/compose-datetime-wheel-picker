package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.formatMonth
import kotlinx.datetime.LocalDate

internal data class DayOfMonth(
  val text: String,
  val value: Int,
  val index: Int
)

internal data class Month(
  val text: String,
  val value: Int,
  val index: Int
)

internal data class Year(
  val text: String,
  val value: Int,
  val index: Int
)

@Composable
internal fun rememberFormattedDayOfMonths(
  month: Int,
  year: Int,
  dateFormatter: DateFormatter
) = remember(month, year, dateFormatter) {
  val daysInMonth = when (month) {
    2 -> if (LocalDate(year, month, 1).isLeapYear) 29 else 28
    4, 6, 9, 11 -> 30
    1, 3, 5, 7, 8, 10, 12 -> 31
    else -> error("Invalid month number: $month")
  }

  (1..daysInMonth).map {
    DayOfMonth(
      text = dateFormatter.formatDay(it),
      value = it,
      index = it - 1
    )
  }
}

@Composable
internal fun rememberFormattedMonths(
  datePickerWidth: Dp,
  dateFormatter: DateFormatter,
) = remember(dateFormatter, datePickerWidth) {
  (1..12).map {
    val monthName = dateFormatter.formatMonth(kotlinx.datetime.Month(it))
    val monthShortName = dateFormatter.formatMonth(kotlinx.datetime.Month(it), MonthDisplayStyle.SHORT)
    Month(
      text = if (datePickerWidth / 3 < 55.dp) monthShortName else monthName,
      value = it,
      index = it - 1
    )
  }
}

@Composable
internal fun rememberFormattedYears(
  yearsRange: IntRange?,
  dateFormatter: DateFormatter
) = remember(yearsRange, dateFormatter) {
  yearsRange?.map {
    Year(
      text = dateFormatter.formatYear(it),
      value = it,
      index = yearsRange.indexOf(it)
    )
  }
}
