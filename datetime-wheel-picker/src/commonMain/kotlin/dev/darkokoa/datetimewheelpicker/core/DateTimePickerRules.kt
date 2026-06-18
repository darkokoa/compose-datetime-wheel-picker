package dev.darkokoa.datetimewheelpicker.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

internal fun coerceDateTime(
  dateTime: LocalDateTime,
  minDateTime: LocalDateTime,
  maxDateTime: LocalDateTime,
  yearsRange: IntRange?,
): LocalDateTime {
  require(minDateTime <= maxDateTime) { "minDateTime must be before or equal to maxDateTime." }

  val coercedDate = coerceDate(
    date = dateTime.date,
    minDate = minDateTime.date,
    maxDate = maxDateTime.date,
    yearsRange = yearsRange,
  )
  var coerced = LocalDateTime(coercedDate, dateTime.time.truncatedToMinute())

  if (coerced < minDateTime) {
    coerced = minDateTime.truncatedTo(ChronoUnit.MINUTES)
  }
  if (coerced > maxDateTime) {
    coerced = maxDateTime.truncatedTo(ChronoUnit.MINUTES)
  }

  return coerced
}

internal fun isDateTimeSelectable(
  dateTime: LocalDateTime,
  minDateTime: LocalDateTime,
  maxDateTime: LocalDateTime,
  yearsRange: IntRange?,
): Boolean {
  val truncated = dateTime.truncatedTo(ChronoUnit.MINUTES)
  return truncated in minDateTime.truncatedTo(ChronoUnit.MINUTES)..maxDateTime.truncatedTo(ChronoUnit.MINUTES) &&
    isDateSelectable(truncated.date, minDateTime.date, maxDateTime.date, yearsRange)
}

internal fun LocalDateTime.withDate(date: LocalDate): LocalDateTime {
  return LocalDateTime(date, time)
}

internal fun LocalDateTime.withTime(time: LocalTime): LocalDateTime {
  return LocalDateTime(date, time)
}
