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
import dev.darkokoa.datetimewheelpicker.WheelDatePickerState
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateField
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import kotlinx.coroutines.launch
import kotlinx.datetime.number

@Composable
internal fun StandardWheelDatePicker(
  state: WheelDatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.SHORT,
    cjkSuffixConfig = CjkSuffixConfig.HideAll
  ),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  eventSink: DatePickerEventSink = NoOpDatePickerEventSink,
) {
  val itemCount = if (state.yearsRange == null) 2 else 3
  val itemWidth = size.width / itemCount
  val scope = rememberCoroutineScope()

  val dayOfMonths =
    rememberFormattedDayOfMonths(state.displayedDate.month.number, state.displayedDate.year, dateFormatter)

  val months = rememberFormattedMonths(size.width, dateFormatter)

  val years = rememberFormattedYears(state.yearsRange, dateFormatter)

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
              state = state.dayWheelState,
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
              onScrollFinished = { snappedIndex ->
                val snap = state.settleDate(DateField.DAY, snappedIndex)
                if (snap != null) {
                  scope.launch { state.snapWheelsToDate(snap.snappedDate.snappedLocalDate) }
                  if (!state.isProgrammaticScrollInProgress) {
                    eventSink.onDateSettled(snap.snappedDate)?.let { return@WheelTextPicker it }
                  }
                  return@WheelTextPicker snap.index
                }

                return@WheelTextPicker state.indexFor(DateField.DAY)
              },
              onScrollChanged = { snappedIndex ->
                val snap = state.updateDisplayedDate(DateField.DAY, snappedIndex)
                if (snap != null && !state.isProgrammaticScrollInProgress) {
                  eventSink.onDisplayedDateChanged(snap.snappedDate)
                }
              }
            )
          }

          DateField.MONTH -> {
            WheelTextPicker(
              state = state.monthWheelState,
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
              onScrollFinished = { snappedIndex ->
                val snap = state.settleDate(DateField.MONTH, snappedIndex)
                if (snap != null) {
                  scope.launch { state.snapWheelsToDate(snap.snappedDate.snappedLocalDate) }
                  if (!state.isProgrammaticScrollInProgress) {
                    eventSink.onDateSettled(snap.snappedDate)?.let { return@WheelTextPicker it }
                  }
                  return@WheelTextPicker snap.index
                }

                return@WheelTextPicker state.indexFor(DateField.MONTH)
              },
              onScrollChanged = { snappedIndex ->
                val snap = state.updateDisplayedDate(DateField.MONTH, snappedIndex)
                if (snap != null && !state.isProgrammaticScrollInProgress) {
                  eventSink.onDisplayedDateChanged(snap.snappedDate)
                }
              }
            )
          }

          DateField.YEAR -> {
            years?.let { years ->
              WheelTextPicker(
                state = state.yearWheelState ?: return@let,
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
                onScrollFinished = { snappedIndex ->
                  val snap = state.settleDate(DateField.YEAR, snappedIndex)
                  if (snap != null) {
                    scope.launch { state.snapWheelsToDate(snap.snappedDate.snappedLocalDate) }
                    if (!state.isProgrammaticScrollInProgress) {
                      eventSink.onDateSettled(snap.snappedDate)?.let { return@WheelTextPicker it }
                    }
                    return@WheelTextPicker snap.index
                  }

                  return@WheelTextPicker state.indexFor(DateField.YEAR)
                },
                onScrollChanged = { snappedIndex ->
                  val snap = state.updateDisplayedDate(DateField.YEAR, snappedIndex)
                  if (snap != null && !state.isProgrammaticScrollInProgress) {
                    eventSink.onDisplayedDateChanged(snap.snappedDate)
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}
