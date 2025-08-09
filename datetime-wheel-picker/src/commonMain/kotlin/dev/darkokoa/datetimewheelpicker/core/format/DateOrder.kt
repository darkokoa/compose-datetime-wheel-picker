package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.ui.text.intl.Locale
import dev.darkokoa.datetimewheelpicker.core.isCjkLanguage

enum class DateOrder(val fields: List<DateField>) {
  DMY(listOf(DateField.DAY, DateField.MONTH, DateField.YEAR)),   // Most European countries
  MDY(listOf(DateField.MONTH, DateField.DAY, DateField.YEAR)),   // USA
  YMD(listOf(DateField.YEAR, DateField.MONTH, DateField.DAY));   // East Asian countries, ISO


  companion object {
    fun match(locale: Locale): DateOrder {
      return when {
        locale.language == "en" && locale.region == "US" -> MDY
        locale.isCjkLanguage -> YMD
        else -> DMY
      }
    }
  }
}