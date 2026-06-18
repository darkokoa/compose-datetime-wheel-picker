package dev.darkokoa.datetimewheelpicker.core

import dev.darkokoa.datetimewheelpicker.core.format.DateField
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatePickerRulesTest {

  @Test
  fun daysInMonth_handlesLeapYears() {
    assertEquals(29, daysInMonth(2024, 2))
    assertEquals(28, daysInMonth(2025, 2))
  }

  @Test
  fun dateFromIndex_clampsDayWhenMonthChanges() {
    val jan31 = LocalDate(2024, 1, 31)
    val feb29 = dateFromIndex(jan31, DateField.MONTH, 1, 2024..2024)

    assertEquals(LocalDate(2024, 2, 29), feb29)
  }

  @Test
  fun dateFromIndex_clampsDayWhenMonthChangesInNonLeapYear() {
    val jan31 = LocalDate(2025, 1, 31)
    val feb28 = dateFromIndex(jan31, DateField.MONTH, 1, 2025..2025)

    assertEquals(LocalDate(2025, 2, 28), feb28)
  }

  @Test
  fun coerceDate_appliesMinAndMax() {
    val min = LocalDate(2025, 1, 10)
    val max = LocalDate(2025, 1, 20)

    assertEquals(min, coerceDate(LocalDate(2025, 1, 1), min, max, 2025..2025))
    assertEquals(max, coerceDate(LocalDate(2025, 1, 31), min, max, 2025..2025))
  }

  @Test
  fun coerceDate_rejectsYearsRangeOutsideMinMaxYears() {
    val min = LocalDate(2025, 1, 10)
    val max = LocalDate(2025, 1, 20)

    assertFailsWith<IllegalArgumentException> {
      coerceDate(LocalDate(2030, 1, 1), min, max, 2030..2031)
    }
  }

  @Test
  fun yearIndex_isNullWhenYearWheelHidden() {
    assertEquals(null, dateIndex(LocalDate(2025, 6, 18), DateField.YEAR, null))
  }

  @Test
  fun dateFromIndex_returnsNullForYearWhenYearWheelHidden() {
    assertEquals(null, dateFromIndex(LocalDate(2025, 6, 18), DateField.YEAR, 0, null))
  }

  @Test
  fun isDateSelectable_respectsYearRange() {
    val min = LocalDate(2024, 1, 1)
    val max = LocalDate(2026, 12, 31)

    assertTrue(isDateSelectable(LocalDate(2025, 6, 18), min, max, 2025..2025))
    assertFalse(isDateSelectable(LocalDate(2024, 6, 18), min, max, 2025..2025))
  }

  @Test
  fun isDateSelectable_rejectsYearsRangeOutsideMinMaxYears() {
    val min = LocalDate(2025, 1, 10)
    val max = LocalDate(2025, 1, 20)

    assertFailsWith<IllegalArgumentException> {
      isDateSelectable(LocalDate(2030, 1, 1), min, max, 2030..2031)
    }
  }
}
