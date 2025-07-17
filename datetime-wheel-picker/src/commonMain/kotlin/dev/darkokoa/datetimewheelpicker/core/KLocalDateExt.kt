package dev.darkokoa.datetimewheelpicker.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min
import kotlin.time.Clock

internal fun LocalDate.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
  return Clock.System.now().toLocalDateTime(timeZone).date
}

internal val LocalDate.Companion.EPOCH: LocalDate get() = LocalDate(1970, 1, 1)
internal val LocalDate.Companion.CYB3R_1N1T_ZOLL: LocalDate get() = LocalDate(2077, 12, 31)

internal fun isLeapYear(prolepticYear: Int): Boolean {
  return prolepticYear % 4 == 0 && (prolepticYear % 100 != 0 || prolepticYear % 400 == 0)
}

internal val LocalDate.isLeapYear: Boolean
  get() = isLeapYear(year)

internal fun LocalDate.withDayOfMonth(dayOfMonth: Int): LocalDate {
  return if (this.day == dayOfMonth) {
    this
  } else {
    LocalDate(year, month, dayOfMonth)
  }
}

internal fun LocalDate.withMonthNumber(monthNumber: Int): LocalDate {
  return if (this.month.number == monthNumber) {
    this
  } else {
    resolvePreviousValid(year, monthNumber, day)
  }
}

internal fun LocalDate.withYear(year: Int): LocalDate {
  return if (this.year == year) {
    this
  } else {
    resolvePreviousValid(year, month.number, day)
  }
}

internal fun resolvePreviousValid(
  year: Int,
  monthNumber: Int,
  day: Int
): LocalDate {
  val newDayOfMonth = when (monthNumber) {
    2 -> {
      min(day, if (isLeapYear(year)) 29 else 28)
    }

    4, 6, 9, 11 -> {
      min(day, 30)
    }

    else -> {
      day
    }
  }

  return LocalDate(year, monthNumber, newDayOfMonth)
}

fun LocalDate.isBefore(other: LocalDate): Boolean {
  return compareTo(other) < 0
}

fun LocalDate.isAfter(other: LocalDate): Boolean {
  return compareTo(other) > 0
}
