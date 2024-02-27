package dev.darkokoa.datetimewheelpicker.core

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min

internal fun LocalDate.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
  return Clock.System.now().toLocalDateTime(timeZone).date
}

internal val LocalDate.Companion.EPOCH: LocalDate get() = LocalDate(1970, 1, 1)
internal val LocalDate.Companion.CYBER_ERA: LocalDate get() = LocalDate(2077, 12, 31)

internal fun isLeapYear(prolepticYear: Int): Boolean {
  return prolepticYear % 4 == 0 && (prolepticYear % 100 != 0 || prolepticYear % 400 == 0)
}

internal val LocalDate.isLeapYear: Boolean
  get() = isLeapYear(year)

internal fun LocalDate.withDayOfMonth(dayOfMonth: Int): LocalDate {
  return if (this.dayOfMonth == dayOfMonth) {
    this
  } else {
    LocalDate(year, monthNumber, dayOfMonth)
  }
}

internal fun LocalDate.withMonthNumber(monthNumber: Int): LocalDate {
  return if (this.monthNumber == monthNumber) {
    this
  } else {
    resolvePreviousValid(year, monthNumber, dayOfMonth)
  }
}

internal fun LocalDate.withYear(year: Int): LocalDate {
  return if (this.year == year) {
    this
  } else {
    resolvePreviousValid(year, monthNumber, dayOfMonth)
  }
}

internal fun resolvePreviousValid(
  year: Int,
  monthNumber: Int,
  dayOfMonth: Int
): LocalDate {
  val newDayOfMonth = when (monthNumber) {
    2 -> {
      min(dayOfMonth, if (isLeapYear(year)) 29 else 28)
    }

    4, 6, 9, 11 -> {
      min(dayOfMonth, 30)
    }

    else -> {
      dayOfMonth
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
