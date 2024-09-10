package dev.darkokoa.datetimewheelpicker

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.DefaultWheelTimePicker
import dev.darkokoa.datetimewheelpicker.core.MAX
import dev.darkokoa.datetimewheelpicker.core.MIN
import dev.darkokoa.datetimewheelpicker.core.SelectorProperties
import dev.darkokoa.datetimewheelpicker.core.TimeComponents
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.now
import kotlinx.datetime.LocalTime

@Composable
fun WheelTimePicker(
  modifier: Modifier = Modifier,
  startTime: LocalTime = LocalTime.now(),
  minTime: LocalTime = LocalTime.MIN,
  maxTime: LocalTime = LocalTime.MAX,
  timeComponents: TimeComponents = TimeComponents.HOUR_MINUTE,
  is24Hour: Boolean = true,
  size: DpSize = DpSize(
    width = when {
      timeComponents == TimeComponents.HOUR_MINUTE_SECOND && !is24Hour -> 256.dp
      timeComponents == TimeComponents.HOUR_MINUTE_SECOND && is24Hour -> 192.dp
      timeComponents == TimeComponents.HOUR_MINUTE && !is24Hour -> 192.dp
      timeComponents == TimeComponents.HOUR_MINUTE && is24Hour -> 128.dp
      else -> 128.dp
    },
    height = 128.dp
  ),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedTime: (snappedTime: LocalTime) -> Unit = {},
) {
  DefaultWheelTimePicker(
    modifier,
    startTime,
    minTime,
    maxTime,
    timeComponents,
    is24Hour,
    size,
    rowCount,
    textStyle,
    textColor,
    selectorProperties,
    onSnappedTime = { snappedTime, _ ->
      onSnappedTime(snappedTime.snappedLocalTime)
      snappedTime.snappedIndex
    }
  )
}