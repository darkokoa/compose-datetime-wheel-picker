package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.ui.text.intl.Locale
import dev.darkokoa.datetimewheelpicker.strings.EnStrings
import dev.darkokoa.datetimewheelpicker.strings.Strings

internal fun Int.toLocalizedNumerals(locale: Locale = Locale.current): String {
  return this.toString().toLocalizedNumerals(locale)
}

internal fun String.toLocalizedNumerals(locale: Locale = Locale.current): String {
  return if (locale.language in arrayOf("ar")) {
    val strings = dev.darkokoa.datetimewheelpicker.Strings[locale.language] ?: EnStrings
    this.toLocalizedNumerals(strings)
  } else {
    this
  }
}

internal fun Int.toLocalizedNumerals(strings: Strings): String {
  return this.toString().toLocalizedNumerals(strings)
}

internal fun String.toLocalizedNumerals(strings: Strings): String {
  return this.map { digit ->
    when (digit) {
      '0' -> strings.number0
      '1' -> strings.number1
      '2' -> strings.number2
      '3' -> strings.number3
      '4' -> strings.number4
      '5' -> strings.number5
      '6' -> strings.number6
      '7' -> strings.number7
      '8' -> strings.number8
      '9' -> strings.number9
      else -> digit.toString()
    }
  }.joinToString("")
}
