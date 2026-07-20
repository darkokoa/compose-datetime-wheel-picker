package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.StandardWheelTimePicker
import dev.darkokoa.datetimewheelpicker.core.MAX
import dev.darkokoa.datetimewheelpicker.core.MIN
import dev.darkokoa.datetimewheelpicker.core.SelectorProperties
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormatter
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import dev.darkokoa.datetimewheelpicker.core.now
import dev.darkokoa.datetimewheelpicker.core.pickerDefaultSize
import dev.darkokoa.datetimewheelpicker.core.resolvePickerSize
import kotlinx.datetime.LocalTime

/**
 * A wheel time picker.
 *
 * Sizing is Modifier-driven: any axis left unconstrained by [modifier] and the parent uses the
 * intrinsic default — 128.dp wide, `(128.dp / 3) * rowCount` tall (128.dp at the default three
 * rows). Standard Compose constraints override or clamp the default; see the README "Sizing"
 * section for examples and migration notes.
 *
 * The picker resolves its size via subcomposition and therefore does not support
 * intrinsic-measurement parents (`IntrinsicSize.Min`/`Max`); pass an explicit `width`/`height`
 * instead.
 */
@Composable
fun WheelTimePicker(
  modifier: Modifier = Modifier,
  startTime: LocalTime = LocalTime.now(),
  minTime: LocalTime = LocalTime.MIN,
  maxTime: LocalTime = LocalTime.MAX,
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectedTextStyle: TextStyle = textStyle,
  selectedTextColor: Color = textColor,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedTimeChanged: (snappedTime: LocalTime) -> Unit = {},
  onSnappedTime: (snappedTime: LocalTime) -> Unit = {},
) {
  BoxWithConstraints(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    val effectiveSize = with(LocalDensity.current) {
      resolvePickerSize(
        constraints = constraints,
        default = pickerDefaultSize(defaultWidth = 128.dp, rowCount = rowCount),
      )
    }

    StandardWheelTimePicker(
      modifier = Modifier,
      startTime = startTime,
      minTime = minTime,
      maxTime = maxTime,
      timeFormatter = timeFormatter,
      size = effectiveSize,
      rowCount = rowCount,
      textStyle = textStyle,
      textColor = textColor,
      selectedTextStyle = selectedTextStyle,
      selectedTextColor = selectedTextColor,
      selectorProperties = selectorProperties,
      onSnappedTime = { snappedTime, _ ->
        onSnappedTime(snappedTime.snappedLocalTime)
        snappedTime.snappedIndex
      },
      onSnappedTimeChanged = { snappedTime, _ ->
        onSnappedTimeChanged(snappedTime.snappedLocalTime)
      }
    )
  }
}

// Binary-compatibility shim with the exact 1.3.x released signature (no selectedTextStyle /
// selectedTextColor — those were added after 1.3.3).
@Deprecated(
  message = "Sizing is Modifier-driven. Use Modifier.size/width/height instead of the size parameter.",
  level = DeprecationLevel.HIDDEN,
)
@Composable
fun WheelTimePicker(
  modifier: Modifier = Modifier,
  startTime: LocalTime = LocalTime.now(),
  minTime: LocalTime = LocalTime.MIN,
  maxTime: LocalTime = LocalTime.MAX,
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  size: DpSize = DpSize(128.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedTimeChanged: (snappedTime: LocalTime) -> Unit = {},
  onSnappedTime: (snappedTime: LocalTime) -> Unit = {},
) = WheelTimePicker(
  modifier = modifier.size(size.width, size.height),
  startTime = startTime,
  minTime = minTime,
  maxTime = maxTime,
  timeFormatter = timeFormatter,
  rowCount = rowCount,
  textStyle = textStyle,
  textColor = textColor,
  selectorProperties = selectorProperties,
  onSnappedTimeChanged = onSnappedTimeChanged,
  onSnappedTime = onSnappedTime,
)
