package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.format.DateField
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.formatMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlin.math.min

@Composable
internal fun DefaultWheelDatePicker(
  modifier: Modifier = Modifier,
  startDate: LocalDate = LocalDate.now(),
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
  dateFormatter: DateFormatter = dateFormatter(Locale.current, MonthDisplayStyle.SHORT),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedDate: (snappedDate: SnappedDate) -> Int? = { _ -> null }
) {
  val itemCount = if (yearsRange == null) 2 else 3
  val itemWidth = size.width / itemCount

  var snappedDate by remember { mutableStateOf(startDate) }

  val dayOfMonths =
    rememberFormattedDayOfMonths(snappedDate.month.number, snappedDate.year, dateFormatter)

  val months = rememberFormattedMonths(dateFormatter, size)

  val years = rememberFormattedYears(yearsRange, dateFormatter)

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    if (selectorProperties.enabled().value) {
      Surface(
        modifier = Modifier
          .size(size.width, size.height / rowCount),
        shape = selectorProperties.shape().value,
        color = selectorProperties.color().value,
        border = selectorProperties.border().value
      ) {}
    }
    Row {
      dateFormatter.dateOrder.fields.forEach { dateField ->
        when (dateField) {
          DateField.DAY -> {
            WheelTextPicker(
              size = DpSize(
                width = itemWidth,
                height = size.height
              ),
              texts = dayOfMonths.map { it.text },
              rowCount = rowCount,
              style = textStyle,
              color = textColor,
              selectorProperties = WheelPickerDefaults.selectorProperties(
                enabled = false
              ),
              startIndex = dayOfMonths.find { it.value == startDate.dayOfMonth }?.index ?: 0,
              onScrollFinished = { snappedIndex ->
                val newDayOfMonth = dayOfMonths.find { it.index == snappedIndex }?.value

                newDayOfMonth?.let {
                  val newDate = snappedDate.withDayOfMonth(newDayOfMonth)

                  if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                    snappedDate = newDate
                  }

                  val newIndex = dayOfMonths.find { it.value == snappedDate.dayOfMonth }?.index

                  newIndex?.let {
                    onSnappedDate(
                      SnappedDate.DayOfMonth(
                        localDate = snappedDate,
                        index = newIndex
                      )
                    )?.let { return@WheelTextPicker it }
                  }
                }

                return@WheelTextPicker dayOfMonths.find { it.value == snappedDate.dayOfMonth }?.index
              }
            )
          }

          DateField.MONTH -> {
            WheelTextPicker(
              size = DpSize(
                width = itemWidth,
                height = size.height
              ),
              texts = months.map { it.text },
              rowCount = rowCount,
              style = textStyle,
              color = textColor,
              selectorProperties = WheelPickerDefaults.selectorProperties(
                enabled = false
              ),
              startIndex = months.find { it.value == startDate.monthNumber }?.index ?: 0,
              onScrollFinished = { snappedIndex ->
                val newMonth = months.find { it.index == snappedIndex }?.value
                newMonth?.let {
                  val newDate = snappedDate.withMonthNumber(newMonth)

                  if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                    snappedDate = newDate
                  }

//                  dayOfMonths = calculateDayOfMonths(
//                    snappedDate.month.number,
//                    snappedDate.year,
//                    dateFormatter.formatDay
//                  )

                  val newIndex = months.find { it.value == snappedDate.monthNumber }?.index

                  newIndex?.let {
                    onSnappedDate(
                      SnappedDate.Month(
                        localDate = snappedDate,
                        index = newIndex
                      )
                    )?.let { return@WheelTextPicker it }
                  }
                }

                return@WheelTextPicker months.find { it.value == snappedDate.monthNumber }?.index
              }
            )
          }

          DateField.YEAR -> {
            years?.let { years ->
              WheelTextPicker(
                size = DpSize(
                  width = itemWidth,
                  height = size.height
                ),
                texts = years.map { it.text },
                rowCount = rowCount,
                style = textStyle,
                color = textColor,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                  enabled = false
                ),
                startIndex = years.find { it.value == startDate.year }?.index ?: 0,
                onScrollFinished = { snappedIndex ->
                  val newYear = years.find { it.index == snappedIndex }?.value

                  newYear?.let {
                    val newDate = snappedDate.withYear(newYear)

                    if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                      snappedDate = newDate
                    }

//                    dayOfMonths = calculateDayOfMonths(
//                      snappedDate.month.number,
//                      snappedDate.year,
//                      dateFormatter.formatDay
//                    )

                    val newIndex = years.find { it.value == snappedDate.year }?.index

                    newIndex?.let {
                      onSnappedDate(
                        SnappedDate.Year(
                          localDate = snappedDate,
                          index = newIndex
                        )
                      )?.let { return@WheelTextPicker it }
                    }
                  }

                  return@WheelTextPicker years.find { it.value == snappedDate.year }?.index
                }
              )
            }
          }
        }
      }
    }
  }
}

private data class DayOfMonth(
  val text: String,
  val value: Int,
  val index: Int
)

private data class Month(
  val text: String,
  val value: Int,
  val index: Int
)

private data class Year(
  val text: String,
  val value: Int,
  val index: Int
)

@Composable
private fun rememberFormattedDayOfMonths(
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
private fun rememberFormattedMonths(
  dateFormatter: DateFormatter,
  size: DpSize
) = remember(dateFormatter, size.width) {
  (1..12).map {
    val monthName = dateFormatter.formatMonth(kotlinx.datetime.Month(it))
    Month(
      text = if (size.width / 3 < 55.dp) {
        monthName.substring(0, min(monthName.length, 3))
      } else monthName,
      value = it,
      index = it - 1
    )
  }
}

@Composable
private fun rememberFormattedYears(
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
