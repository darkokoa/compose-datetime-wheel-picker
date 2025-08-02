package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class CjkSuffixConfiguration(
  val showYearSuffix: Boolean = true,
  val showMonthSuffix: Boolean = true,
  val showDaySuffix: Boolean = true,
  val yearSuffixSpacing: Dp = 8.dp,
  val monthSuffixSpacing: Dp = 8.dp,
  val daySuffixSpacing: Dp = 8.dp
) {
  companion object {
    val ShowAll = CjkSuffixConfiguration()

    val HideAll = CjkSuffixConfiguration(
      showYearSuffix = false,
      showMonthSuffix = false,
      showDaySuffix = false,
    )
  }
}