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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Composable
internal fun StandardWheelDatePicker(
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

  val months = rememberFormattedMonths(itemWidth, dateFormatter)

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
              startIndex = dayOfMonths.find { it.value == startDate.day }?.index ?: 0,
              onScrollFinished = { snappedIndex ->
                val newDayOfMonth = dayOfMonths.find { it.index == snappedIndex }?.value

                newDayOfMonth?.let {
                  val newDate = snappedDate.withDayOfMonth(newDayOfMonth)

                  if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                    snappedDate = newDate
                  }

                  val newIndex = dayOfMonths.find { it.value == snappedDate.day }?.index

                  newIndex?.let {
                    onSnappedDate(
                      SnappedDate.DayOfMonth(
                        localDate = snappedDate,
                        index = newIndex
                      )
                    )?.let { return@WheelTextPicker it }
                  }
                }

                return@WheelTextPicker dayOfMonths.find { it.value == snappedDate.day }?.index
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
              startIndex = months.find { it.value == startDate.month.number }?.index ?: 0,
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

                  val newIndex = months.find { it.value == snappedDate.month.number }?.index

                  newIndex?.let {
                    onSnappedDate(
                      SnappedDate.Month(
                        localDate = snappedDate,
                        index = newIndex
                      )
                    )?.let { return@WheelTextPicker it }
                  }
                }

                return@WheelTextPicker months.find { it.value == snappedDate.month.number }?.index
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