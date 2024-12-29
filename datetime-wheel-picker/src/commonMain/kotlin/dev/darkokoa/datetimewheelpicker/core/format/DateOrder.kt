package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.ui.text.intl.Locale

enum class DateOrder(val fields: List<DateField>) {
  DMY(listOf(DateField.DAY, DateField.MONTH, DateField.YEAR)),   // Most European countries
  MDY(listOf(DateField.MONTH, DateField.DAY, DateField.YEAR)),   // USA
  YMD(listOf(DateField.YEAR, DateField.MONTH, DateField.DAY));   // East Asian countries, ISO


  companion object {
    fun match(locale: Locale): DateOrder {
      return when {
        locale.language == "en" && locale.region == "US" -> MDY
        locale.language in listOf("zh", "ja", "ko") -> YMD
        else -> DMY
      }
    }
  }
}