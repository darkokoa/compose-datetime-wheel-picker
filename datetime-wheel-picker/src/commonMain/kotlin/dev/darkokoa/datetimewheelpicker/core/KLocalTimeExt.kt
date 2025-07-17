package dev.darkokoa.datetimewheelpicker.core

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal fun LocalTime.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalTime {
  return Clock.System.now().toLocalDateTime(timeZone).time
}

internal val LocalTime.Companion.MIN: LocalTime get() = LocalTime(0, 0, 0, 0)
internal val LocalTime.Companion.MAX: LocalTime get() = LocalTime(23, 59, 59, 999_999_999)

internal fun LocalTime.withMinute(minute: Int): LocalTime {
  return if (this.minute == minute) {
    this
  } else {
    LocalTime(hour, minute, second, nanosecond)
  }
}

internal fun LocalTime.withHour(hour: Int): LocalTime {
  return if (this.hour == hour) {
    this
  } else {
    LocalTime(hour, minute, second, nanosecond)
  }
}

internal fun LocalTime.isBefore(other: LocalTime): Boolean {
  return compareTo(other) < 0
}

internal fun LocalTime.isAfter(other: LocalTime): Boolean {
  return compareTo(other) > 0
}