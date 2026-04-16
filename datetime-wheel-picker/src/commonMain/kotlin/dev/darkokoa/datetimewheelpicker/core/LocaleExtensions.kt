package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.text.intl.Locale
import dev.darkokoa.datetimewheelpicker.strings.EnStrings
import dev.darkokoa.datetimewheelpicker.strings.Strings

private val CJK_LANGUAGES = listOf("zh", "ja", "ko")

val Locale.isCjkLanguage: Boolean
  get() = language in CJK_LANGUAGES

internal fun Locale.resolveStrings(
  stringsMap: Map<String, Strings> = dev.darkokoa.datetimewheelpicker.Strings
): Strings = resolveStringsFromComponents(
  script = script, language = language, stringsMap = stringsMap
)

internal fun Locale.resolveLanguageTag(
  stringsMap: Map<String, Strings> = dev.darkokoa.datetimewheelpicker.Strings
): String = resolveLanguageTagFromComponents(
  script = script, language = language, stringsMap = stringsMap
)

internal fun resolveStringsFromComponents(
  script: String,
  language: String,
  stringsMap: Map<String, Strings>
): Strings {
  if (script.isNotEmpty()) {
    stringsMap["$language-$script"]?.let { return it }
  }
  stringsMap[language]?.let { return it }
  return EnStrings
}

internal fun resolveLanguageTagFromComponents(
  script: String,
  language: String,
  stringsMap: Map<String, Strings>
): String {
  if (script.isNotEmpty()) {
    val langScript = "$language-$script"
    if (langScript in stringsMap) return langScript
  }
  if (language in stringsMap) return language
  return "en"
}
