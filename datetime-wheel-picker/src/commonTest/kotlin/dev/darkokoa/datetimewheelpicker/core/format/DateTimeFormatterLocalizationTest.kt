package dev.darkokoa.datetimewheelpicker.core.format

import dev.darkokoa.datetimewheelpicker.strings.UzArabStrings
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * End-to-end tests that exercise the public formatting path for locales that
 * combine non-Latin month names with non-ASCII numerals (uz-Arab).
 *
 * These go one layer above [NumeralConversionTest] / [LocaleResolutionTest]
 * by invoking the internal `dateFormatter(strings = ...)` / `timeFormatter(strings = ...)`
 * overloads — the same overloads that the public `@Composable` variants ultimately delegate to.
 */
class DateTimeFormatterLocalizationTest {

  // Extended Arabic-Indic digits (U+06F0 series) used by UzArabStrings
  private val uzArabDigits = "۰۱۲۳۴۵۶۷۸۹"

  // ---- DateFormatter (uz-Arab) ----

  @Test
  fun uzArab_fullMonthName() {
    val formatter = dateFormatter(strings = UzArabStrings)
    assertEquals("جنوری", formatter.formatMonth(Month.JANUARY, MonthDisplayStyle.FULL))
    assertEquals("دسمبر", formatter.formatMonth(Month.DECEMBER, MonthDisplayStyle.FULL))
  }

  @Test
  fun uzArab_shortMonthName() {
    val formatter = dateFormatter(strings = UzArabStrings)
    assertEquals("جنو", formatter.formatMonth(Month.JANUARY, MonthDisplayStyle.SHORT))
    assertEquals("دسم", formatter.formatMonth(Month.DECEMBER, MonthDisplayStyle.SHORT))
  }

  @Test
  fun uzArab_numericMonthUsesExtendedArabicIndicDigits() {
    val formatter = dateFormatter(strings = UzArabStrings)
    // Month.JANUARY.number == 1  → "۱"
    assertEquals("۱", formatter.formatMonth(Month.JANUARY, MonthDisplayStyle.NUMERIC))
    // Month.DECEMBER.number == 12 → "۱۲"
    assertEquals("۱۲", formatter.formatMonth(Month.DECEMBER, MonthDisplayStyle.NUMERIC))
  }

  @Test
  fun uzArab_yearAndDayUseExtendedArabicIndicDigits() {
    val formatter = dateFormatter(strings = UzArabStrings)
    assertEquals("۲۰۲۵", formatter.formatYear(2025))
    assertEquals("۹", formatter.formatDay(9))
    assertEquals("۳۱", formatter.formatDay(31))
  }

  @Test
  fun uzArab_allDigitsMapped() {
    val formatter = dateFormatter(strings = UzArabStrings)
    val actual = (0..9).joinToString(separator = "") { formatter.formatDay(it) }
    assertEquals(uzArabDigits, actual)
  }

  // ---- TimeFormatter (uz-Arab) ----

  @Test
  fun uzArab_hourAndMinuteUseExtendedArabicIndicDigits() {
    val formatter = timeFormatter(strings = UzArabStrings)
    // Hours/minutes are zero-padded to 2 characters before digit mapping
    assertEquals("۰۹", formatter.formatHour(9))
    assertEquals("۲۳", formatter.formatHour(23))
    assertEquals("۰۰", formatter.formatMinute(0))
    assertEquals("۵۹", formatter.formatMinute(59))
  }

  @Test
  fun uzArab_amPmTextFromStrings() {
    val formatter = timeFormatter(strings = UzArabStrings)
    assertEquals(UzArabStrings.timeAM, formatter.formatAmText())
    assertEquals(UzArabStrings.timePM, formatter.formatPmText())
  }
}
