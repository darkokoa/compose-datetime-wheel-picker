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
import dev.darkokoa.datetimewheelpicker.WheelDateTimePickerState
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormatter
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import kotlinx.coroutines.launch

@Composable
internal fun AdaptiveWheelDateTimePicker(
  state: WheelDateTimePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.SHORT,
    cjkSuffixConfig = CjkSuffixConfig.HideAll
  ),
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  eventSink: DateTimePickerEventSink = NoOpDateTimePickerEventSink,
) {
  val scope = rememberCoroutineScope()

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
      //Date
      AdaptiveWheelDatePicker(
        state = state.dateState,
        dateFormatter = dateFormatter,
        size = DpSize(
          width = if (state.yearsRange == null) size.width * 3 / 6 else size.width * 3 / 5,
          height = size.height
        ),
        rowCount = rowCount,
        textStyle = textStyle,
        textColor = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        eventSink = object : DatePickerEventSink {
          override fun onDateSettled(snappedDate: SnappedDate): Int? {
            val snap = state.settleDateTime(snappedDate)
            scope.launch { state.snapWheelsToDateTime(snap.snappedDateTime.snappedLocalDateTime) }
            if (!state.isProgrammaticScrollInProgress) {
              eventSink.onDateTimeSettled(snap.snappedDateTime)?.let { return it }
            }
            return snap.index
          }

          override fun onDisplayedDateChanged(snappedDate: SnappedDate) {
            state.updateDisplayedDateTime(snappedDate)
          }
        },
      )
      //Time
      StandardWheelTimePicker(
        state = state.timeState,
        timeFormatter = timeFormatter,
        size = DpSize(
          width = if (state.yearsRange == null) size.width * 3 / 6 else size.width * 2 / 5,
          height = size.height
        ),
        rowCount = rowCount,
        textStyle = textStyle,
        textColor = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        eventSink = object : TimePickerEventSink {
          override fun onTimeSettled(snappedTime: SnappedTime, timeFormat: TimeFormat): Int? {
            val snap = state.settleDateTime(snappedTime, timeFormat)
            scope.launch { state.snapWheelsToDateTime(snap.snappedDateTime.snappedLocalDateTime) }
            if (!state.isProgrammaticScrollInProgress) {
              eventSink.onDateTimeSettled(snap.snappedDateTime)?.let { return it }
            }
            return snap.index
          }

          override fun onDisplayedTimeChanged(snappedTime: SnappedTime, timeFormat: TimeFormat) {
            state.updateDisplayedDateTime(snappedTime, timeFormat)
          }
        },
      )
    }
  }
}





