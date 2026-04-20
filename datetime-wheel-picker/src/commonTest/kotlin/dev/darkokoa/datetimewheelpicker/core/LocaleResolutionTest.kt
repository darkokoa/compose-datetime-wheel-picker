package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.text.intl.Locale
import dev.darkokoa.datetimewheelpicker.strings.EnStrings
import dev.darkokoa.datetimewheelpicker.strings.JaStrings
import dev.darkokoa.datetimewheelpicker.strings.KoStrings
import dev.darkokoa.datetimewheelpicker.strings.Strings
import dev.darkokoa.datetimewheelpicker.strings.ZhStrings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LocaleResolutionTest {

  // Distinct Strings instances for identity-based assertions
  private val fakeEn = EnStrings
  private val fakeUz = Strings(monthJanuaryFull = "Yanvar")
  private val fakeUzCyrl = Strings(monthJanuaryFull = "Январь")
  private val fakeUzArab = Strings(monthJanuaryFull = "جنوری", digit0 = '۰')
  private val fakeDe = Strings(monthJanuaryFull = "Januar")

  private val stringsMap = mapOf(
    "en" to fakeEn,
    "uz" to fakeUz,
    "uz-Cyrl" to fakeUzCyrl,
    "uz-Arab" to fakeUzArab,
    "de" to fakeDe,
  )

  // ---- resolveStringsFromComponents ----

  @Test
  fun exactScriptMatch_uzArab() {
    val result = resolveStringsFromComponents(script = "Arab", language = "uz", stringsMap)
    assertSame(fakeUzArab, result, "uz-Arab should resolve to UzArabStrings")
  }

  @Test
  fun exactScriptMatch_uzCyrl() {
    val result = resolveStringsFromComponents(script = "Cyrl", language = "uz", stringsMap)
    assertSame(fakeUzCyrl, result, "uz-Cyrl should resolve to UzCyrlStrings")
  }

  @Test
  fun scriptFallsBackToLanguage_uzLatn() {
    // "uz-Latn" is NOT in the map — should fall back to "uz"
    val result = resolveStringsFromComponents(script = "Latn", language = "uz", stringsMap)
    assertSame(fakeUz, result, "uz-Latn should fall back to uz (Latin is the default)")
  }

  @Test
  fun emptyScriptFallsBackToLanguage() {
    val result = resolveStringsFromComponents(script = "", language = "uz", stringsMap)
    assertSame(fakeUz, result, "Empty script with uz should resolve to uz")
  }

  @Test
  fun unknownLanguageFallsBackToEn() {
    val result = resolveStringsFromComponents(script = "", language = "xx", stringsMap)
    assertSame(fakeEn, result, "Unknown language should fall back to en")
  }

  @Test
  fun unknownLanguageAndScriptFallsBackToEn() {
    val result = resolveStringsFromComponents(script = "Zzzz", language = "xx", stringsMap)
    assertSame(fakeEn, result, "Unknown language+script should fall back to en")
  }

  @Test
  fun existingLocaleWithoutScript_backwardCompatible() {
    val result = resolveStringsFromComponents(script = "", language = "de", stringsMap)
    assertSame(fakeDe, result, "Existing locale de should still resolve directly")
  }

  // ---- resolveLanguageTagFromComponents ----

  @Test
  fun languageTag_exactScriptMatch() {
    val tag = resolveLanguageTagFromComponents(script = "Arab", language = "uz", stringsMap)
    assertEquals("uz-Arab", tag)
  }

  @Test
  fun languageTag_withoutScript() {
    val tag = resolveLanguageTagFromComponents(script = "", language = "de", stringsMap)
    assertEquals("de", tag)
  }

  @Test
  fun languageTag_unknownFallsBackToEn() {
    val tag = resolveLanguageTagFromComponents(script = "", language = "xx", stringsMap)
    assertEquals("en", tag)
  }

  @Test
  fun languageTag_unknownScriptFallsBackToLanguage() {
    // "uz-Latn" not in map → falls back to "uz"
    val tag = resolveLanguageTagFromComponents(script = "Latn", language = "uz", stringsMap)
    assertEquals("uz", tag)
  }

  // ---- Integration with the real default Strings map ----
  //
  // These tests exercise the resolution against the real KSP-generated
  // `dev.darkokoa.datetimewheelpicker.Strings` map (no `zh-Hant` / `zh-Hans`
  // entries), locking in the documented CJK fallback behavior.

  @Test
  fun realMap_zhHantFallsBackToZh() {
    val strings = Locale("zh-Hant").resolveStrings()
    assertSame(ZhStrings, strings, "zh-Hant should fall back to ZhStrings (no Hant variant)")
  }

  @Test
  fun realMap_zhHansFallsBackToZh() {
    val strings = Locale("zh-Hans").resolveStrings()
    assertSame(ZhStrings, strings, "zh-Hans should fall back to ZhStrings (no Hans variant)")
  }

  @Test
  fun realMap_zhHantLanguageTag() {
    assertEquals("zh", Locale("zh-Hant").resolveLanguageTag())
  }

  @Test
  fun realMap_jaResolvesToJaStrings() {
    assertSame(JaStrings, Locale("ja").resolveStrings())
    assertEquals("ja", Locale("ja").resolveLanguageTag())
  }

  @Test
  fun realMap_koResolvesToKoStrings() {
    assertSame(KoStrings, Locale("ko").resolveStrings())
    assertEquals("ko", Locale("ko").resolveLanguageTag())
  }

  // ---- isCjkLanguage ----

  @Test
  fun isCjkLanguage_trueForZhJaKo() {
    assertTrue(Locale("zh").isCjkLanguage)
    assertTrue(Locale("ja").isCjkLanguage)
    assertTrue(Locale("ko").isCjkLanguage)
    // Script-tagged Chinese still counts as CJK
    assertTrue(Locale("zh-Hant").isCjkLanguage)
  }

  @Test
  fun isCjkLanguage_falseForOthers() {
    assertFalse(Locale("en").isCjkLanguage)
    assertFalse(Locale("uz-Arab").isCjkLanguage)
    assertFalse(Locale("ar").isCjkLanguage)
  }
}
