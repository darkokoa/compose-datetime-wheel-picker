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
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormatter
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import kotlinx.datetime.LocalTime

@Composable
internal fun DefaultWheelTimePicker(
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
  onSnappedTime: (snappedTime: SnappedTime, timeFormat: TimeFormat) -> Int? = { _, _ -> null },
) {

  val itemCount = remember(timeFormatter.timeFormat) {
    if (timeFormatter.timeFormat == TimeFormat.AM_PM) 3 else 2
  }

  val itemWidth = remember(itemCount) { size.width / itemCount }

  val hours = rememberHours(timeFormatter)
  val amPmHours = rememberAmPmHours(timeFormatter)
  val minutes = rememberMinutes(timeFormatter)
  val amPms = rememberAmPm(timeFormatter)

  var snappedTime by remember { mutableStateOf(LocalTime(startTime.hour, startTime.minute)) }

  var snappedAmPm by remember {
    mutableStateOf(amPms.find { it.value == amPmValueFromTime(startTime) } ?: amPms[0])
  }

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
        size = DpSize(
          width = itemWidth,
          height = size.height
        ),
        texts = if (timeFormatter.timeFormat == TimeFormat.HOUR_24) hours.map { it.text } else amPmHours.map { it.text },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        startIndex = if (timeFormatter.timeFormat == TimeFormat.HOUR_24) {
          hours.find { it.value == startTime.hour }?.index ?: 0
        } else amPmHours.find { it.value == localTimeToAmPmHour(startTime) }?.index ?: 0,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        onScrollFinished = { snappedIndex ->

          val newHour = if (timeFormatter.timeFormat == TimeFormat.HOUR_24) {
            hours.find { it.index == snappedIndex }?.value
          } else {
            amPmHourToHour24(
              amPmHours.find { it.index == snappedIndex }?.value ?: 0,
              snappedTime.minute,
              snappedAmPm.value
            )
          }

          newHour?.let {

            val newTime = snappedTime.withHour(newHour)

            if (!newTime.isBefore(minTime) && !newTime.isAfter(maxTime)) {
              snappedTime = newTime
            }

            val newIndex = if (timeFormatter.timeFormat == TimeFormat.HOUR_24) {
              hours.find { it.value == snappedTime.hour }?.index
            } else {
              amPmHours.find { it.value == localTimeToAmPmHour(snappedTime) }?.index
            }

            newIndex?.let {
              onSnappedTime(
                SnappedTime.Hour(
                  localTime = snappedTime,
                  index = newIndex
                ),
                timeFormatter.timeFormat
              )?.let { return@WheelTextPicker it }
            }
          }

          return@WheelTextPicker if (timeFormatter.timeFormat == TimeFormat.HOUR_24) {
            hours.find { it.value == snappedTime.hour }?.index
          } else {
            amPmHours.find { it.value == localTimeToAmPmHour(snappedTime) }?.index
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
        size = DpSize(
          width = itemWidth,
          height = size.height
        ),
        texts = minutes.map { it.text },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        startIndex = minutes.find { it.value == startTime.minute }?.index ?: 0,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        onScrollFinished = { snappedIndex ->

          val newMinute = minutes.find { it.index == snappedIndex }?.value

          val newHour = if (timeFormatter.timeFormat == TimeFormat.HOUR_24) {
            hours.find { it.value == snappedTime.hour }?.value
          } else {
            amPmHourToHour24(
              amPmHours.find { it.value == localTimeToAmPmHour(snappedTime) }?.value ?: 0,
              snappedTime.minute,
              snappedAmPm.value
            )
          }

          newMinute?.let {
            newHour?.let {
              val newTime = snappedTime.withMinute(newMinute).withHour(newHour)

              if (!newTime.isBefore(minTime) && !newTime.isAfter(maxTime)) {
                snappedTime = newTime
              }

              val newIndex = minutes.find { it.value == snappedTime.minute }?.index

              newIndex?.let {
                onSnappedTime(
                  SnappedTime.Minute(
                    localTime = snappedTime,
                    index = newIndex
                  ),
                  timeFormatter.timeFormat
                )?.let { return@WheelTextPicker it }
              }
            }
          }

          return@WheelTextPicker minutes.find { it.value == snappedTime.minute }?.index
        }
      )
      //AM_PM
      if (timeFormatter.timeFormat == TimeFormat.AM_PM) {
        WheelTextPicker(
          size = DpSize(
            width = itemWidth,
            height = size.height
          ),
          texts = amPms.map { it.text },
          rowCount = rowCount,
          style = textStyle,
          color = textColor,
          startIndex = amPms.find { it.value == amPmValueFromTime(startTime) }?.index ?: 0,
          selectorProperties = WheelPickerDefaults.selectorProperties(
            enabled = false
          ),
          onScrollFinished = { snappedIndex ->

            val newAmPm = amPms.find {
              if (snappedIndex == 2) {
                it.index == 1
              } else {
                it.index == snappedIndex
              }
            }

            newAmPm?.let {
              snappedAmPm = newAmPm
            }

            val newMinute = minutes.find { it.value == snappedTime.minute }?.value

            val newHour = amPmHourToHour24(
              amPmHours.find { it.value == localTimeToAmPmHour(snappedTime) }?.value ?: 0,
              snappedTime.minute,
              snappedAmPm.value
            )

            newMinute?.let {
              val newTime = snappedTime.withMinute(newMinute).withHour(newHour)

              if (!newTime.isBefore(minTime) && !newTime.isAfter(maxTime)) {
                snappedTime = newTime
              }

              val newIndex = minutes.find { it.value == snappedTime.hour }?.index

              newIndex?.let {
                onSnappedTime(
                  SnappedTime.Hour(
                    localTime = snappedTime,
                    index = newIndex
                  ),
                  timeFormatter.timeFormat
                )
              }
            }

            return@WheelTextPicker snappedIndex
          }
        )
      }
    }
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