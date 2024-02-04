package dev.darkokoa.chronowheelpicker

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.chronowheelpicker.core.CYBER_ERA
import dev.darkokoa.chronowheelpicker.core.DefaultWheelDateTimePicker
import dev.darkokoa.chronowheelpicker.core.EPOCH
import dev.darkokoa.chronowheelpicker.core.SelectorProperties
import dev.darkokoa.chronowheelpicker.core.TimeFormat
import dev.darkokoa.chronowheelpicker.core.WheelPickerDefaults
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun WheelDateTimePicker(
  modifier: Modifier = Modifier,
  startDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
  minDateTime: LocalDateTime = LocalDateTime.EPOCH,
  maxDateTime: LocalDateTime = LocalDateTime.CYBER_ERA,
  yearsRange: IntRange? = IntRange(minDateTime.year, maxDateTime.year),
  timeFormat: TimeFormat = TimeFormat.HOUR_24,
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedDateTime: (snappedDateTime: LocalDateTime) -> Unit = {}
) {
  DefaultWheelDateTimePicker(
    modifier,
    startDateTime,
    minDateTime,
    maxDateTime,
    yearsRange,
    timeFormat,
    size,
    rowCount,
    textStyle,
    textColor,
    selectorProperties,
    onSnappedDateTime = { snappedDateTime ->
      onSnappedDateTime(snappedDateTime.snappedLocalDateTime)
      snappedDateTime.snappedIndex
    }
  )
}