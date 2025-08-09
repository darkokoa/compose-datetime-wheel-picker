package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateField
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import dev.darkokoa.datetimewheelpicker.rememberStrings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Composable
internal fun CJKWheelDatePicker(
  modifier: Modifier = Modifier,
  startDate: LocalDate = LocalDate.now(),
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
  dateFormatter: DateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.SHORT,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll
  ),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedDate: (snappedDate: SnappedDate) -> Int? = { _ -> null }
) {
  val currentLocale = Locale.current
  val strings = rememberStrings(currentLanguageTag = currentLocale.language).strings

  val itemWidth = Dp.Infinity

  var snappedDate by remember { mutableStateOf(startDate) }

  val dayOfMonths =
    rememberFormattedDayOfMonths(snappedDate.month.number, snappedDate.year, dateFormatter)

  val months = rememberFormattedMonths(Dp.Hairline, dateFormatter)

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
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(size.width)) {
      dateFormatter.dateOrder.fields.forEach { dateField ->
        when (dateField) {
          DateField.DAY -> {
            WheelTextPickerWithSuffix(
              modifier = Modifier.weight(1f),
              size = DpSize(
                width = itemWidth,
                height = size.height
              ),
              texts = dayOfMonths.map { it.text },
              suffix = if (dateFormatter.cjkSuffixConfig.showDaySuffix) strings.daySuffix else "",
              textToSuffixSpacing = dateFormatter.cjkSuffixConfig.daySuffixSpacing,
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
                    )?.let { return@WheelTextPickerWithSuffix it }
                  }
                }

                return@WheelTextPickerWithSuffix dayOfMonths.find { it.value == snappedDate.day }?.index
              }
            )
          }

          DateField.MONTH -> {
            WheelTextPickerWithSuffix(
              modifier = Modifier.weight(1f),
              size = DpSize(
                width = itemWidth,
                height = size.height
              ),
              texts = months.map { it.text },
              suffix = if (dateFormatter.cjkSuffixConfig.showMonthSuffix) strings.monthSuffix else "",
              textToSuffixSpacing = dateFormatter.cjkSuffixConfig.monthSuffixSpacing,
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

                  val newIndex = months.find { it.value == snappedDate.month.number }?.index

                  newIndex?.let {
                    onSnappedDate(
                      SnappedDate.Month(
                        localDate = snappedDate,
                        index = newIndex
                      )
                    )?.let { return@WheelTextPickerWithSuffix it }
                  }
                }

                return@WheelTextPickerWithSuffix months.find { it.value == snappedDate.month.number }?.index
              }
            )
          }

          DateField.YEAR -> {
            years?.let { years ->
              WheelTextPickerWithSuffix(
                modifier = Modifier.weight(1.4f),
                size = DpSize(
                  width = itemWidth,
                  height = size.height
                ),
                texts = years.map { it.text },
                suffix = if (dateFormatter.cjkSuffixConfig.showYearSuffix) strings.yearSuffix else "",
                textToSuffixSpacing = dateFormatter.cjkSuffixConfig.yearSuffixSpacing,
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

                    val newIndex = years.find { it.value == snappedDate.year }?.index

                    newIndex?.let {
                      onSnappedDate(
                        SnappedDate.Year(
                          localDate = snappedDate,
                          index = newIndex
                        )
                      )?.let { return@WheelTextPickerWithSuffix it }
                    }
                  }

                  return@WheelTextPickerWithSuffix years.find { it.value == snappedDate.year }?.index
                }
              )
            }
          }
        }
      }
    }
  }
}