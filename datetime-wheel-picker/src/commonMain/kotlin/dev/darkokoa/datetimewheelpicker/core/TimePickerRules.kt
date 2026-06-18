package dev.darkokoa.datetimewheelpicker.core

import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import kotlinx.datetime.LocalTime

internal enum class TimePickerField {
  Hour,
  Minute,
  Period,
}

internal enum class TimePeriod {
  AM,
  PM,
}

internal fun coerceTime(
  time: LocalTime,
  minTime: LocalTime,
  maxTime: LocalTime,
): LocalTime {
  require(minTime <= maxTime) { "minTime must be before or equal to maxTime." }

  var coerced = time.truncatedToMinute()
  if (coerced < minTime) coerced = minTime.truncatedToMinute()
  if (coerced > maxTime) coerced = maxTime.truncatedToMinute()
  return coerced
}

internal fun isTimeSelectable(
  time: LocalTime,
  minTime: LocalTime,
  maxTime: LocalTime,
): Boolean {
  return time.truncatedToMinute() in minTime.truncatedToMinute()..maxTime.truncatedToMinute()
}

internal fun timeFromIndex(
  currentTime: LocalTime,
  field: TimePickerField,
  index: Int,
  timeFormat: TimeFormat,
): LocalTime? {
  return when (field) {
    TimePickerField.Hour -> {
      val hour = when (timeFormat) {
        TimeFormat.HOUR_24 -> index.takeIf { it in 0..23 }
        TimeFormat.AM_PM -> {
          val amPmHour = (index + 1).takeIf { it in 1..12 } ?: return null
          hour24FromAmPm(amPmHour, periodFromTime(currentTime))
        }
      } ?: return null

      currentTime.withHour(hour).truncatedToMinute()
    }

    TimePickerField.Minute -> {
      val minute = index.takeIf { it in 0..59 } ?: return null
      currentTime.withMinute(minute).truncatedToMinute()
    }

    TimePickerField.Period -> {
      if (timeFormat != TimeFormat.AM_PM) return null
      val period = periodFromIndex(index) ?: return null
      currentTime.withHour(hour24FromAmPm(amPmHourFromTime(currentTime), period)).truncatedToMinute()
    }
  }
}

internal fun timeIndex(
  time: LocalTime,
  field: TimePickerField,
  timeFormat: TimeFormat,
): Int? {
  return when (field) {
    TimePickerField.Hour -> when (timeFormat) {
      TimeFormat.HOUR_24 -> time.hour
      TimeFormat.AM_PM -> amPmHourFromTime(time) - 1
    }

    TimePickerField.Minute -> time.minute
    TimePickerField.Period -> when (timeFormat) {
      TimeFormat.HOUR_24 -> null
      TimeFormat.AM_PM -> when (periodFromTime(time)) {
        TimePeriod.AM -> 0
        TimePeriod.PM -> 1
      }
    }
  }
}

internal fun amPmHourFromTime(time: LocalTime): Int {
  return when (val hour = time.hour) {
    0 -> 12
    in 1..12 -> hour
    else -> hour - 12
  }
}

internal fun hour24FromAmPm(
  amPmHour: Int,
  period: TimePeriod,
): Int {
  return when (period) {
    TimePeriod.AM -> if (amPmHour == 12) 0 else amPmHour
    TimePeriod.PM -> if (amPmHour == 12) 12 else amPmHour + 12
  }
}

internal fun periodFromTime(time: LocalTime): TimePeriod {
  return if (time.hour >= 12) TimePeriod.PM else TimePeriod.AM
}

private fun periodFromIndex(index: Int): TimePeriod? {
  return when (index) {
    0 -> TimePeriod.AM
    1 -> TimePeriod.PM
    else -> null
  }
}

internal fun LocalTime.truncatedToMinute(): LocalTime {
  return LocalTime(hour, minute)
}
