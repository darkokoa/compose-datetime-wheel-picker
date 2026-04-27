package dev.darkokoa.datetimewheelpicker.core.format

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Is24HourFormatJvmTest {

  private lateinit var originalLocale: Locale

  @BeforeTest
  fun saveLocale() {
    originalLocale = Locale.getDefault()
  }

  @AfterTest
  fun restoreLocale() {
    Locale.setDefault(originalLocale)
  }

  // ---- 24h European locales ----

  @Test
  fun localeImplies24Hour_german() {
    assertTrue(localeImplies24Hour(Locale.of("de", "DE")))
  }

  @Test
  fun localeImplies24Hour_french() {
    assertTrue(localeImplies24Hour(Locale.of("fr", "FR")))
  }

  @Test
  fun localeImplies24Hour_italian() {
    assertTrue(localeImplies24Hour(Locale.of("it", "IT")))
  }

  @Test
  fun localeImplies24Hour_spanish() {
    assertTrue(localeImplies24Hour(Locale.of("es", "ES")))
  }

  @Test
  fun localeImplies24Hour_portuguese() {
    assertTrue(localeImplies24Hour(Locale.of("pt", "PT")))
  }

  @Test
  fun localeImplies24Hour_dutch() {
    assertTrue(localeImplies24Hour(Locale.of("nl", "NL")))
  }

  @Test
  fun localeImplies24Hour_swedish() {
    assertTrue(localeImplies24Hour(Locale.of("sv", "SE")))
  }

  @Test
  fun localeImplies24Hour_russian() {
    assertTrue(localeImplies24Hour(Locale.of("ru", "RU")))
  }

  // ---- 24h Asian locales ----

  @Test
  fun localeImplies24Hour_japanese() {
    assertTrue(localeImplies24Hour(Locale.of("ja", "JP")))
  }

  @Test
  fun localeImplies24Hour_chineseSimplified() {
    assertTrue(localeImplies24Hour(Locale.of("zh", "CN")))
  }

  // ---- 12h locales ----

  @Test
  fun localeImplies24Hour_chineseTraditional_returns12h() {
    // zh_TW CLDR pattern: ah:mm — uses AM/PM
    assertFalse(localeImplies24Hour(Locale.of("zh", "TW")))
  }

  @Test
  fun localeImplies24Hour_korean_returns12h() {
    // ko_KR CLDR pattern: a h:mm — uses AM/PM
    assertFalse(localeImplies24Hour(Locale.of("ko", "KR")))
  }

  @Test
  fun localeImplies24Hour_enUS_returns12h() {
    assertFalse(localeImplies24Hour(Locale.of("en", "US")))
  }

  @Test
  fun localeImplies24Hour_enIN_returns12h() {
    assertFalse(localeImplies24Hour(Locale.of("en", "IN")))
  }

  @Test
  fun localeImplies24Hour_filPH_returns12h() {
    assertFalse(localeImplies24Hour(Locale.of("fil", "PH")))
  }

  // ---- Edge cases ----

  @Test
  fun localeImplies24Hour_rootLocale() {
    // Locale.ROOT has empty language/country — JDK typically defaults to 24h
    val result = localeImplies24Hour(Locale.ROOT)
    // Verify it doesn't crash and returns a consistent boolean
    assertEquals(result, localeImplies24Hour(Locale.ROOT))
  }

  @Test
  fun localeImplies24Hour_unknownLocale_doesNotCrash() {
    // A locale with no CLDR data — should fall back gracefully
    val result = localeImplies24Hour(Locale.of("xx", "YY"))
    assertEquals(result, localeImplies24Hour(Locale.of("xx", "YY")))
  }

  // ---- Pattern sanity ----

  @Test
  fun patternDoesNotContainAmPm_for24hLocale() {
    val locale = Locale.of("de", "DE")
    val df = DateFormat.getTimeInstance(DateFormat.SHORT, locale) as SimpleDateFormat
    val pattern = df.toPattern()
    assertFalse(pattern.contains('a'), "de_DE pattern should not contain AM/PM marker 'a': $pattern")
  }

  @Test
  fun patternContainsAmPm_for12hLocale() {
    val locale = Locale.of("en", "US")
    val df = DateFormat.getTimeInstance(DateFormat.SHORT, locale) as SimpleDateFormat
    val pattern = df.toPattern()
    assertTrue(pattern.contains('a'), "en_US pattern should contain AM/PM marker 'a': $pattern")
  }

  // ---- Locale.setDefault integration ----

  @Test
  fun localeImplies24Hour_respectsDefaultLocaleChange() {
    Locale.setDefault(Locale.of("de", "DE"))
    val resultDE = localeImplies24Hour(Locale.getDefault(Locale.Category.FORMAT))

    Locale.setDefault(Locale.of("en", "US"))
    val resultUS = localeImplies24Hour(Locale.getDefault(Locale.Category.FORMAT))

    assertTrue(resultDE, "de_DE default should imply 24h")
    assertFalse(resultUS, "en_US default should imply 12h")
  }
}
