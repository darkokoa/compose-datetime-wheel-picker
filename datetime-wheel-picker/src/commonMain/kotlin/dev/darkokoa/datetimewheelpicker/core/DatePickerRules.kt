package dev.darkokoa.datetimewheelpicker.core

import dev.darkokoa.datetimewheelpicker.core.format.DateField
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

internal fun daysInMonth(year: Int, month: Int): Int {
  return when (month) {
    2 -> if (isLeapYear(year)) 29 else 28
    4, 6, 9, 11 -> 30
    1, 3, 5, 7, 8, 10, 12 -> 31
    else -> error("Invalid month number: $month")
  }
}

internal fun coerceDate(
  date: LocalDate,
  minDate: LocalDate,
  maxDate: LocalDate,
  yearsRange: IntRange?,
): LocalDate {
  require(minDate <= maxDate) { "minDate must be before or equal to maxDate." }

  var coerced = date
  val effectiveYearsRange = yearsRange?.effectiveYearsRange(minDate, maxDate)
  if (effectiveYearsRange != null && coerced.year !in effectiveYearsRange) {
    coerced = coerced.withYear(coerced.year.coerceIn(effectiveYearsRange.first, effectiveYearsRange.last))
  }

  if (coerced < minDate) coerced = minDate
  if (coerced > maxDate) coerced = maxDate

  return coerced
}

internal fun isDateSelectable(
  date: LocalDate,
  minDate: LocalDate,
  maxDate: LocalDate,
  yearsRange: IntRange?,
): Boolean {
  val effectiveYearsRange = yearsRange?.effectiveYearsRange(minDate, maxDate)
  return date in minDate..maxDate &&
    (effectiveYearsRange == null || date.year in effectiveYearsRange)
}

internal fun dateFromIndex(
  currentDate: LocalDate,
  field: DateField,
  index: Int,
  yearsRange: IntRange?,
): LocalDate? {
  return when (field) {
    DateField.DAY -> {
      val day = (index + 1).coerceIn(1, daysInMonth(currentDate.year, currentDate.month.number))
      currentDate.withDayOfMonth(day)
    }

    DateField.MONTH -> {
      val month = (index + 1).takeIf { it in 1..12 } ?: return null
      currentDate.withMonthNumber(month)
    }

    DateField.YEAR -> {
      val year = yearsRange?.yearAt(index) ?: return null
      currentDate.withYear(year)
    }
  }
}

internal fun dateIndex(
  date: LocalDate,
  field: DateField,
  yearsRange: IntRange?,
): Int? {
  return when (field) {
    DateField.DAY -> date.day - 1
    DateField.MONTH -> date.month.number - 1
    DateField.YEAR -> yearsRange?.indexOfYear(date.year)
  }
}

private fun IntRange.effectiveYearsRange(
  minDate: LocalDate,
  maxDate: LocalDate,
): IntRange {
  val start = maxOf(first, minDate.year)
  val end = minOf(last, maxDate.year)
  require(start <= end) {
    "yearsRange must overlap minDate..maxDate."
  }
  return start..end
}

private fun IntRange.yearAt(index: Int): Int? {
  if (index < 0 || index >= count()) return null
  return first + index
}

private fun IntRange.indexOfYear(year: Int): Int? {
  if (year !in this) return null
  return year - first
}
