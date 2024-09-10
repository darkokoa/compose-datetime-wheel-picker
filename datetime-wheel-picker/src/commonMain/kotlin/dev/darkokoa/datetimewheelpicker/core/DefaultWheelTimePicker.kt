package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

@Composable
internal fun DefaultWheelTimePicker(
  modifier: Modifier = Modifier,
  startTime: LocalTime = LocalTime.now(),
  minTime: LocalTime = LocalTime.MIN,
  maxTime: LocalTime = LocalTime.MAX,
  timeComponents: TimeComponents = TimeComponents.HOUR_MINUTE,
  is24Hour: Boolean = true,
  size: DpSize = DpSize.Unspecified,
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedTime: (snappedTime: SnappedTime, is24Hour: Boolean) -> Int? = { _, _ -> null },
) {
  val itemCount = calculateTimePickerChildItemCount(timeComponents, is24Hour)
  val itemWidth = size.width / itemCount

  var snappedTime by remember { mutableStateOf(LocalTime(startTime.hour, startTime.minute)) }

  val hours = (0..23).map {
    Hour(
      text = it.toString().padStart(2, '0'),
      value = it,
      index = it
    )
  }
  val amPmHours = (1..12).map {
    AmPmHour(
      text = it.toString(),
      value = it,
      index = it - 1
    )
  }

  val minutes = (0..59).map {
    Minute(
      text = it.toString().padStart(2, '0'),
      value = it,
      index = it
    )
  }

  val seconds = if (timeComponents == TimeComponents.HOUR_MINUTE_SECOND) {
    (0..59).map {
      Second(
        text = it.toString().padStart(2, '0'),
        value = it,
        index = it
      )
    }
  } else emptyList()

  val amPms = listOf(
    AmPm(
      text = "AM",
      value = AmPmValue.AM,
      index = 0
    ),
    AmPm(
      text = "PM",
      value = AmPmValue.PM,
      index = 1
    )
  )

  var snappedAmPm by remember {
    mutableStateOf(
      amPms.find { it.value == amPmValueFromTime(startTime) } ?: amPms[0]
    )
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
    Row {
      //Hour
      WheelTextPicker(
        size = DpSize(
          width = itemWidth,
          height = size.height
        ),
        texts = if (is24Hour) hours.map { it.text } else amPmHours.map { it.text },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        startIndex = if (is24Hour) {
          hours.find { it.value == startTime.hour }?.index ?: 0
        } else amPmHours.find { it.value == localTimeToAmPmHour(startTime) }?.index ?: 0,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        onScrollFinished = { snappedIndex ->

          val newHour = if (is24Hour) {
            hours.find { it.index == snappedIndex }?.value
          } else {
            amPmHourToHour24(
              amPmHours.find { it.index == snappedIndex }?.value ?: 0,
              snappedTime.minute,
              snappedTime.second,
              snappedAmPm.value
            )
          }

          newHour?.let {

            val newTime = snappedTime.withHour(newHour)

            if (!newTime.isBefore(minTime) && !newTime.isAfter(maxTime)) {
              snappedTime = newTime
            }

            val newIndex = if (is24Hour) {
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
                is24Hour
              )?.let { return@WheelTextPicker it }
            }
          }

          return@WheelTextPicker if (is24Hour) {
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

          val newHour = if (is24Hour) {
            hours.find { it.value == snappedTime.hour }?.value
          } else {
            amPmHourToHour24(
              amPmHours.find { it.value == localTimeToAmPmHour(snappedTime) }?.value ?: 0,
              snappedTime.minute,
              snappedTime.second,
              snappedAmPm.value,
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
                  is24Hour
                )?.let { return@WheelTextPicker it }
              }
            }
          }

          return@WheelTextPicker minutes.find { it.value == snappedTime.minute }?.index
        }
      )

      if (seconds.isNotEmpty()) {
        //Colon
        TimeSeparator(
          modifier = Modifier.align(Alignment.CenterVertically).width(0.dp),
          textStyle = textStyle.copy(color = textColor),
        )

        //Second
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

            val newSecond = seconds.find { it.index == snappedIndex }?.value

            val newHour = if (is24Hour) {
              hours.find { it.value == snappedTime.hour }?.value
            } else {
              amPmHourToHour24(
                amPmHours.find { it.value == localTimeToAmPmHour(snappedTime) }?.value ?: 0,
                snappedTime.minute,
                snappedTime.second,
                snappedAmPm.value
              )
            }

            newSecond?.let {
              newHour?.let {
                val newTime = snappedTime.withSecond(newSecond).withHour(newHour)

                if (!newTime.isBefore(minTime) && !newTime.isAfter(maxTime)) {
                  snappedTime = newTime
                }

                val newIndex = seconds.find { it.value == snappedTime.second }?.index

                newIndex?.let {
                  onSnappedTime(
                    SnappedTime.Second(
                      localTime = snappedTime,
                      index = newIndex
                    ),
                    is24Hour
                  )?.let { return@WheelTextPicker it }
                }
              }
            }

            return@WheelTextPicker seconds.find { it.value == snappedTime.minute }?.index
          }
        )
      }

      //AM_PM
      if (!is24Hour) {
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
              snappedTime.second,
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
                  is24Hour
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

internal fun calculateTimePickerChildItemCount(
  timeComponents: TimeComponents,
  is24Hour: Boolean
): Int {
  return when (timeComponents) {
    TimeComponents.HOUR_MINUTE -> 2
    TimeComponents.HOUR_MINUTE_SECOND -> 3
  } + if (is24Hour) 0 else 1
}

enum class TimeComponents {
  HOUR_MINUTE,
  HOUR_MINUTE_SECOND,
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

private fun amPmHourToHour24(
  amPmHour: Int,
  amPmMinute: Int,
  amPmSecond: Int,
  amPmValue: AmPmValue
): Int {
  return when (amPmValue) {
    AmPmValue.AM -> {
      if (amPmHour == 12 && amPmMinute <= 59 && amPmSecond <= 59) {
        0
      } else {
        amPmHour
      }
    }

    AmPmValue.PM -> {
      if (amPmHour == 12 && amPmMinute <= 59 && amPmSecond <= 59) {
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

private data class Second(
  val text: String,
  val value: Int,
  val index: Int
)

private data class AmPm(
  val text: String,
  val value: AmPmValue,
  val index: Int?
)

internal enum class AmPmValue {
  AM, PM
}

private fun amPmValueFromTime(time: LocalTime): AmPmValue {
  return if (time.hour > 11) AmPmValue.PM else AmPmValue.AM
}











