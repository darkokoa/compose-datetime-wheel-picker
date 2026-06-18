package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.TimePickerSnap
import dev.darkokoa.datetimewheelpicker.WheelTimePickerState
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormatter
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

@Composable
internal fun StandardWheelTimePicker(
  state: WheelTimePickerState,
  modifier: Modifier = Modifier,
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  size: DpSize = DpSize(128.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  eventSink: TimePickerEventSink = NoOpTimePickerEventSink,
) {

  val timeFormat = timeFormatter.timeFormat
  val itemCount = remember(timeFormat) {
    if (timeFormat == TimeFormat.AM_PM) 3 else 2
  }

  val itemWidth = remember(itemCount) { size.width / itemCount }
  val scope = rememberCoroutineScope()

  val hours = rememberHours(timeFormatter)
  val amPmHours = rememberAmPmHours(timeFormatter)
  val minutes = rememberMinutes(timeFormatter)
  val amPms = rememberAmPm(timeFormatter)

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    if (selectorProperties.enabled().value) {
      Surface(
        modifier = Modifier.size(size.width, size.height / rowCount),
        shape = selectorProperties.shape().value,
        color = selectorProperties.color().value,
        border = selectorProperties.border().value
      ) {}
    }
    Row(modifier = Modifier.height(size.height)) {
      //Hour
      WheelTextPicker(
        state = if (timeFormat == TimeFormat.HOUR_24) state.hour24WheelState else state.amPmHourWheelState,
        size = DpSize(
          width = itemWidth,
          height = size.height
        ),
        texts = if (timeFormat == TimeFormat.HOUR_24) hours.map { it.text } else amPmHours.map { it.text },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        onScrollFinished = { snappedIndex ->
          val snap = state.settleTime(TimePickerField.Hour, snappedIndex, timeFormat)
          if (snap != null) {
            scope.launch { state.snapWheelsToTime(snap.snappedTime.snappedLocalTime) }
            val sinkIndex = if (!state.isProgrammaticScrollInProgress) {
              eventSink.onTimeSettled(snap.snappedTime, timeFormat)
            } else {
              null
            }
            return@WheelTextPicker settledTimeWheelIndex(TimePickerField.Hour, snap, sinkIndex)
          }

          return@WheelTextPicker state.indexFor(TimePickerField.Hour, timeFormat)
        },
        onScrollChanged = { snappedIndex ->
          val snap = state.updateDisplayedTime(TimePickerField.Hour, snappedIndex, timeFormat)
          if (snap != null && !state.isProgrammaticScrollInProgress) {
            eventSink.onDisplayedTimeChanged(snap.snappedTime, timeFormat)
          }
        }
      )

      //Colon
      TimeSeparator(
        modifier = Modifier.align(Alignment.CenterVertically).width(0.dp),
        textStyle = textStyle.copy(color = textColor),
      )

      //Minute
      WheelTextPicker(
        state = state.minuteWheelState,
        size = DpSize(
          width = itemWidth,
          height = size.height
        ),
        texts = minutes.map { it.text },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        onScrollFinished = { snappedIndex ->
          val snap = state.settleTime(TimePickerField.Minute, snappedIndex, timeFormat)
          if (snap != null) {
            scope.launch { state.snapWheelsToTime(snap.snappedTime.snappedLocalTime) }
            val sinkIndex = if (!state.isProgrammaticScrollInProgress) {
              eventSink.onTimeSettled(snap.snappedTime, timeFormat)
            } else {
              null
            }
            return@WheelTextPicker settledTimeWheelIndex(TimePickerField.Minute, snap, sinkIndex)
          }

          return@WheelTextPicker state.indexFor(TimePickerField.Minute, timeFormat)
        },
        onScrollChanged = { snappedIndex ->
          val snap = state.updateDisplayedTime(TimePickerField.Minute, snappedIndex, timeFormat)
          if (snap != null && !state.isProgrammaticScrollInProgress) {
            eventSink.onDisplayedTimeChanged(snap.snappedTime, timeFormat)
          }
        }
      )
      //AM_PM
      if (timeFormat == TimeFormat.AM_PM) {
        WheelTextPicker(
          state = state.periodWheelState,
          size = DpSize(
            width = itemWidth,
            height = size.height
          ),
          texts = amPms.map { it.text },
          rowCount = rowCount,
          style = textStyle,
          color = textColor,
          selectorProperties = WheelPickerDefaults.selectorProperties(
            enabled = false
          ),
          onScrollFinished = { snappedIndex ->
            val snap = state.settleTime(TimePickerField.Period, snappedIndex, timeFormat)
            if (snap != null) {
              scope.launch { state.snapWheelsToTime(snap.snappedTime.snappedLocalTime) }
              val sinkIndex = if (!state.isProgrammaticScrollInProgress) {
                eventSink.onTimeSettled(snap.snappedTime, timeFormat)
              } else {
                null
              }
              return@WheelTextPicker settledTimeWheelIndex(TimePickerField.Period, snap, sinkIndex)
            }

            return@WheelTextPicker state.indexFor(TimePickerField.Period, timeFormat)
          },
          onScrollChanged = { snappedIndex ->
            val snap = state.updateDisplayedTime(TimePickerField.Period, snappedIndex, timeFormat)
            if (snap != null && !state.isProgrammaticScrollInProgress) {
              eventSink.onDisplayedTimeChanged(snap.snappedTime, timeFormat)
            }
          }
        )
      }
    }
  }
}

internal fun settledTimeWheelIndex(
  field: TimePickerField,
  snap: TimePickerSnap,
  eventSinkIndex: Int?,
): Int {
  return when (field) {
    TimePickerField.Period -> snap.index
    TimePickerField.Hour,
    TimePickerField.Minute -> eventSinkIndex ?: snap.index
  }
}

@Composable
fun TimeSeparator(
  modifier: Modifier = Modifier,
  textStyle: TextStyle = TextStyle.Default,
  dotSizeRatio: Float = 0.135f,
  spacingRatio: Float = 0.25f
) {
  val density = LocalDensity.current

  val fontSize = textStyle.fontSize
  val fontWeight = textStyle.fontWeight ?: FontWeight.Normal
  val color = textStyle.color

  val fontSizePx = with(density) { fontSize.toPx() }
  val baseDotSizePx = fontSizePx * dotSizeRatio

  val weightFactor = when (fontWeight) {
    FontWeight.Thin -> 0.7f
    FontWeight.ExtraLight -> 0.8f
    FontWeight.Light -> 0.9f
    FontWeight.Normal -> 1.0f
    FontWeight.Medium -> 1.1f
    FontWeight.SemiBold -> 1.2f
    FontWeight.Bold -> 1.3f
    FontWeight.ExtraBold -> 1.4f
    FontWeight.Black -> 1.5f
    else -> 1.0f
  }

  val dotSizePx = baseDotSizePx * weightFactor
  val spacingPx = fontSizePx * spacingRatio
  val totalHeightPx = dotSizePx * 2 + spacingPx

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Canvas(
      modifier = Modifier.size(
        width = with(density) { dotSizePx.toDp() },
        height = with(density) { totalHeightPx.toDp() }
      )
    ) {
      drawCircle(
        color = color,
        radius = dotSizePx / 2,
        center = Offset(size.width / 2, dotSizePx / 2),
        style = Fill
      )

      drawCircle(
        color = color,
        radius = dotSizePx / 2,
        center = Offset(size.width / 2, totalHeightPx - dotSizePx / 2),
        style = Fill
      )
    }
  }

}

private data class Hour(
  val text: String,
  val value: Int,
  val index: Int
)

private data class AmPmHour(
  val text: String,
  val value: Int,
  val index: Int
)

internal fun localTimeToAmPmHour(localTime: LocalTime): Int {
  if (
    isBetween(
      localTime,
      LocalTime(0, 0),
      LocalTime(0, 59)
    )
  ) {
    return localTime.hour + 12
  }

  if (
    isBetween(
      localTime,
      LocalTime(1, 0),
      LocalTime(11, 59)
    )
  ) {
    return localTime.hour
  }

  if (
    isBetween(
      localTime,
      LocalTime(12, 0),
      LocalTime(12, 59)
    )
  ) {
    return localTime.hour
  }

  if (
    isBetween(
      localTime,
      LocalTime(13, 0),
      LocalTime(23, 59)
    )
  ) {
    return localTime.hour - 12
  }

  return localTime.hour
}

private fun isBetween(localTime: LocalTime, startTime: LocalTime, endTime: LocalTime): Boolean {
  return localTime in startTime..endTime
}

private fun amPmHourToHour24(amPmHour: Int, amPmMinute: Int, amPmValue: AmPmValue): Int {

  return when (amPmValue) {
    AmPmValue.AM -> {
      if (amPmHour == 12 && amPmMinute <= 59) {
        0
      } else {
        amPmHour
      }
    }

    AmPmValue.PM -> {
      if (amPmHour == 12 && amPmMinute <= 59) {
        amPmHour
      } else {
        amPmHour + 12
      }
    }
  }
}

private data class Minute(
  val text: String,
  val value: Int,
  val index: Int
)

private data class AmPm(
  val text: String,
  val value: AmPmValue,
  val index: Int?
)

private enum class AmPmValue {
  AM, PM
}

private fun amPmValueFromTime(time: LocalTime): AmPmValue {
  return if (time.hour > 11) AmPmValue.PM else AmPmValue.AM
}

@Composable
private fun rememberHours(timeFormatter: TimeFormatter) = remember(timeFormatter) {
  (0..23).map {
    Hour(
      text = timeFormatter.formatHour(it),
      value = it,
      index = it
    )
  }
}

@Composable
private fun rememberAmPmHours(timeFormatter: TimeFormatter) = remember(timeFormatter) {
  (1..12).map {
    AmPmHour(
      text = timeFormatter.formatHour(it),
      value = it,
      index = it - 1
    )
  }
}

@Composable
private fun rememberMinutes(timeFormatter: TimeFormatter) = remember(timeFormatter) {
  (0..59).map {
    Minute(
      text = timeFormatter.formatMinute(it),
      value = it,
      index = it
    )
  }
}

@Composable
private fun rememberAmPm(timeFormatter: TimeFormatter) = remember(timeFormatter) {
  listOf(
    AmPm(
      text = timeFormatter.formatAmText(),
      value = AmPmValue.AM,
      index = 0
    ),
    AmPm(
      text = timeFormatter.formatPmText(),
      value = AmPmValue.PM,
      index = 1
    )
  )
}
