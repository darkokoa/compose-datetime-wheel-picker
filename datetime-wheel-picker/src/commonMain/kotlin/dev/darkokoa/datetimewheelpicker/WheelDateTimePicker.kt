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
import dev.darkokoa.datetimewheelpicker.core.AdaptiveWheelDateTimePicker
import dev.darkokoa.datetimewheelpicker.core.CYB3R_1N1T_ZOLL
import dev.darkokoa.datetimewheelpicker.core.DateTimePickerEventSink
import dev.darkokoa.datetimewheelpicker.core.EPOCH
import dev.darkokoa.datetimewheelpicker.core.SelectorProperties
import dev.darkokoa.datetimewheelpicker.core.SnappedDateTime
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormatter
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import kotlinx.datetime.LocalDateTime

@Composable
fun WheelDateTimePicker(
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
) {
  AdaptiveWheelDateTimePicker(
    state = state,
    modifier = modifier,
    dateFormatter = dateFormatter,
    timeFormatter = timeFormatter,
    size = size,
    rowCount = rowCount,
    textStyle = textStyle,
    textColor = textColor,
    selectorProperties = selectorProperties,
  )
}

@Composable
fun WheelDateTimePicker(
  selectedDateTime: LocalDateTime,
  onDateTimeChange: (LocalDateTime) -> Unit,
  modifier: Modifier = Modifier,
  minDateTime: LocalDateTime = LocalDateTime.EPOCH,
  maxDateTime: LocalDateTime = LocalDateTime.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDateTime.year, maxDateTime.year),
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
) {
  val state = rememberWheelDateTimePickerState(
    initialSelectedDateTime = selectedDateTime,
    minDateTime = minDateTime,
    maxDateTime = maxDateTime,
    yearsRange = yearsRange,
  )

  val settledDateTime = state.selectedDateTime
  LaunchedEffect(selectedDateTime, settledDateTime, state) {
    if (state.selectedDateTime != state.coerceDateTime(selectedDateTime)) {
      state.scrollToDateTime(selectedDateTime)
    }
  }

  AdaptiveWheelDateTimePicker(
    state = state,
    modifier = modifier,
    dateFormatter = dateFormatter,
    timeFormatter = timeFormatter,
    size = size,
    rowCount = rowCount,
    textStyle = textStyle,
    textColor = textColor,
    selectorProperties = selectorProperties,
    eventSink = object : DateTimePickerEventSink {
      override fun onDateTimeSettled(snappedDateTime: SnappedDateTime): Int? {
        if (snappedDateTime.snappedLocalDateTime != state.coerceDateTime(selectedDateTime)) {
          onDateTimeChange(snappedDateTime.snappedLocalDateTime)
        }
        return snappedDateTime.snappedIndex
      }
    },
  )
}
