package dev.darkokoa.datetimewheelpicker.core

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateTimePickerRulesTest {

  @Test
  fun coerceDateTime_appliesMinAndMax() {
    val min = LocalDateTime(2025, 6, 18, 9, 30)
    val max = LocalDateTime(2025, 6, 18, 17, 45)

    assertEquals(min, coerceDateTime(LocalDateTime(2025, 6, 18, 8, 0), min, max, 2025..2025))
    assertEquals(max, coerceDateTime(LocalDateTime(2025, 6, 18, 18, 0), min, max, 2025..2025))
  }

  @Test
  fun coerceDateTime_keepsInteriorDayTimeWhenRangeSpansMultipleDays() {
    val min = LocalDateTime(2025, 6, 18, 9, 30)
    val max = LocalDateTime(2025, 6, 20, 17, 45)

    assertEquals(
      LocalDateTime(2025, 6, 19, 8, 0),
      coerceDateTime(LocalDateTime(2025, 6, 19, 8, 0), min, max, 2025..2025)
    )
  }

  @Test
  fun coerceDateTime_clampsTimeOnBoundaryDays() {
    val min = LocalDateTime(2025, 6, 18, 9, 30)
    val max = LocalDateTime(2025, 6, 20, 17, 45)

    assertEquals(min, coerceDateTime(LocalDateTime(2025, 6, 18, 8, 0), min, max, 2025..2025))
    assertEquals(max, coerceDateTime(LocalDateTime(2025, 6, 20, 18, 0), min, max, 2025..2025))
  }

  @Test
  fun coerceDateTime_clampsYearRangeBeforeDateRange() {
    val min = LocalDateTime(2024, 1, 1, 0, 0)
    val max = LocalDateTime(2026, 12, 31, 23, 59)

    assertEquals(
      LocalDateTime(2025, 6, 18, 12, 0),
      coerceDateTime(LocalDateTime(2024, 6, 18, 12, 0), min, max, 2025..2025)
    )
  }

  @Test
  fun coerceDateTime_rejectsYearsRangeOutsideMinMaxYears() {
    val min = LocalDateTime(2025, 1, 1, 0, 0)
    val max = LocalDateTime(2025, 12, 31, 23, 59)

    assertFailsWith<IllegalArgumentException> {
      coerceDateTime(LocalDateTime(2030, 6, 18, 12, 0), min, max, 2030..2031)
    }
  }

  @Test
  fun isDateTimeSelectable_respectsDateTimeAndYearRanges() {
    val min = LocalDateTime(2024, 1, 1, 0, 0)
    val max = LocalDateTime(2026, 12, 31, 23, 59)

    assertTrue(isDateTimeSelectable(LocalDateTime(2025, 6, 18, 12, 0), min, max, 2025..2025))
    assertFalse(isDateTimeSelectable(LocalDateTime(2024, 6, 18, 12, 0), min, max, 2025..2025))
  }
}
