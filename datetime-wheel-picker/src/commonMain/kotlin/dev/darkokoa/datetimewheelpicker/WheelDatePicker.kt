package dev.darkokoa.datetimewheelpicker

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.CYB3R_1N1T_ZOLL
import dev.darkokoa.datetimewheelpicker.core.AdaptiveWheelDatePicker
import dev.darkokoa.datetimewheelpicker.core.DatePickerEventSink
import dev.darkokoa.datetimewheelpicker.core.EPOCH
import dev.darkokoa.datetimewheelpicker.core.SelectorProperties
import dev.darkokoa.datetimewheelpicker.core.SnappedDate
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import kotlinx.datetime.LocalDate

@Composable
fun WheelDatePicker(
  state: WheelDatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.FULL,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll
  ),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
) {
  AdaptiveWheelDatePicker(
    state = state,
    modifier = modifier,
    dateFormatter = dateFormatter,
    size = size,
    rowCount = rowCount,
    textStyle = textStyle,
    textColor = textColor,
    selectorProperties = selectorProperties,
  )
}

@Composable
fun WheelDatePicker(
  selectedDate: LocalDate,
  onDateChange: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
  dateFormatter: DateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.FULL,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll
  ),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
) {
  val state = rememberWheelDatePickerState(
    initialSelectedDate = selectedDate,
    minDate = minDate,
    maxDate = maxDate,
    yearsRange = yearsRange,
  )

  val settledDate = state.selectedDate
  LaunchedEffect(selectedDate, settledDate, state) {
    if (state.selectedDate != state.coerceDate(selectedDate)) {
      state.scrollToDate(selectedDate)
    }
  }

  AdaptiveWheelDatePicker(
    state = state,
    modifier = modifier,
    dateFormatter = dateFormatter,
    size = size,
    rowCount = rowCount,
    textStyle = textStyle,
    textColor = textColor,
    selectorProperties = selectorProperties,
    eventSink = object : DatePickerEventSink {
      override fun onDateSettled(snappedDate: SnappedDate): Int? {
        if (snappedDate.snappedLocalDate != state.coerceDate(selectedDate)) {
          onDateChange(snappedDate.snappedLocalDate)
        }
        return snappedDate.snappedIndex
      }
    },
  )
}
