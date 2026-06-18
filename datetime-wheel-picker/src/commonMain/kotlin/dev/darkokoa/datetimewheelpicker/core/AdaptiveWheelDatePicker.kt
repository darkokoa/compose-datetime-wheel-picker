package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.WheelDatePickerState
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter

@Composable
internal fun AdaptiveWheelDatePicker(
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
  eventSink: DatePickerEventSink = NoOpDatePickerEventSink,
) {
  if (Locale.current.isCjkLanguage) {
    CJKWheelDatePicker(
      state = state,
      modifier = modifier,
      dateFormatter = dateFormatter,
      size = size,
      rowCount = rowCount,
      textStyle = textStyle,
      textColor = textColor,
      selectorProperties = selectorProperties,
      eventSink = eventSink,
    )
  } else {
    StandardWheelDatePicker(
      state = state,
      modifier = modifier,
      dateFormatter = dateFormatter,
      size = size,
      rowCount = rowCount,
      textStyle = textStyle,
      textColor = textColor,
      selectorProperties = selectorProperties,
      eventSink = eventSink,
    )
  }
}
