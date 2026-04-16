package dev.darkokoa.datetimewheelpicker.core.format

import dev.darkokoa.datetimewheelpicker.strings.Strings
import kotlin.test.Test
import kotlin.test.assertEquals

class NumeralConversionTest {

  // Default Strings — ASCII digits (digit0 = '0')
  private val asciiStrings = Strings()

  // Extended Arabic-Indic (used by uz-Arab, fa): ۰-۹ (U+06F0 - U+06F9)
  private val extendedArabicIndicStrings = Strings(
    digit0 = '\u06F0', digit1 = '\u06F1', digit2 = '\u06F2', digit3 = '\u06F3', digit4 = '\u06F4',
    digit5 = '\u06F5', digit6 = '\u06F6', digit7 = '\u06F7', digit8 = '\u06F8', digit9 = '\u06F9',
  )

  // Standard Arabic-Indic (used by ar): ٠-٩ (U+0660 - U+0669)
  private val arabicIndicStrings = Strings(
    digit0 = '\u0660', digit1 = '\u0661', digit2 = '\u0662', digit3 = '\u0663', digit4 = '\u0664',
    digit5 = '\u0665', digit6 = '\u0666', digit7 = '\u0667', digit8 = '\u0668', digit9 = '\u0669',
  )

  // ---- ASCII passthrough ----

  @Test
  fun asciiDigitsUnchanged_whenDigit0IsAsciiZero() {
    assertEquals("2025", "2025".toLocalizedNumerals(asciiStrings))
  }

  @Test
  fun asciiWithLeadingZero_unchanged() {
    assertEquals("09", "09".toLocalizedNumerals(asciiStrings))
  }

  // ---- Extended Arabic-Indic (uz-Arab, fa) ----

  @Test
  fun extendedArabicIndic_fullYear() {
    assertEquals("۲۰۲۵", "2025".toLocalizedNumerals(extendedArabicIndicStrings))
  }

  @Test
  fun extendedArabicIndic_leadingZero() {
    assertEquals("۰۹", "09".toLocalizedNumerals(extendedArabicIndicStrings))
  }

  // ---- Standard Arabic-Indic (ar) ----

  @Test
  fun standardArabicIndic_fullYear() {
    assertEquals("٢٠٢٥", "2025".toLocalizedNumerals(arabicIndicStrings))
  }

  // ---- Mixed content ----

  @Test
  fun nonDigitCharactersUnchanged() {
    assertEquals("Jan ۱۲", "Jan 12".toLocalizedNumerals(extendedArabicIndicStrings))
  }

  // ---- Edge cases ----

  @Test
  fun emptyStringReturnsEmpty() {
    assertEquals("", "".toLocalizedNumerals(extendedArabicIndicStrings))
  }

  @Test
  fun stringWithNoDigitsUnchanged() {
    assertEquals("Hello", "Hello".toLocalizedNumerals(extendedArabicIndicStrings))
  }

  @Test
  fun allDigits_0through9() {
    assertEquals("۰۱۲۳۴۵۶۷۸۹", "0123456789".toLocalizedNumerals(extendedArabicIndicStrings))
    assertEquals("٠١٢٣٤٥٦٧٨٩", "0123456789".toLocalizedNumerals(arabicIndicStrings))
  }
}
