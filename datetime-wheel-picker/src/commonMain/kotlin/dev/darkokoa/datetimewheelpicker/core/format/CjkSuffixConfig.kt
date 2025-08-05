package dev.darkokoa.datetimewheelpicker.core.format

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CjkSuffixVisibility {
  ShowAll,
  HideAll,
  YearOnly,
  MonthDayOnly
}

private val DefaultCjkSuffixSpacing = 8.dp

@Stable
data class CjkSuffixConfig(
  val showYearSuffix: Boolean = true,
  val showMonthSuffix: Boolean = true,
  val showDaySuffix: Boolean = true,
  val yearSuffixSpacing: Dp = DefaultCjkSuffixSpacing,
  val monthSuffixSpacing: Dp = DefaultCjkSuffixSpacing,
  val daySuffixSpacing: Dp = DefaultCjkSuffixSpacing
) {
  constructor(
    showYearSuffix: Boolean = true,
    showMonthSuffix: Boolean = true,
    showDaySuffix: Boolean = true,
    spacing: Dp = DefaultCjkSuffixSpacing
  ) : this(
    showYearSuffix = showYearSuffix,
    showMonthSuffix = showMonthSuffix,
    showDaySuffix = showDaySuffix,
    yearSuffixSpacing = spacing,
    monthSuffixSpacing = spacing,
    daySuffixSpacing = spacing
  )

  constructor(
    visibility: CjkSuffixVisibility,
    spacing: Dp = DefaultCjkSuffixSpacing
  ) : this(
    showYearSuffix = when (visibility) {
      CjkSuffixVisibility.ShowAll, CjkSuffixVisibility.YearOnly -> true
      CjkSuffixVisibility.HideAll, CjkSuffixVisibility.MonthDayOnly -> false
    },
    showMonthSuffix = when (visibility) {
      CjkSuffixVisibility.ShowAll, CjkSuffixVisibility.MonthDayOnly -> true
      CjkSuffixVisibility.HideAll, CjkSuffixVisibility.YearOnly -> false
    },
    showDaySuffix = when (visibility) {
      CjkSuffixVisibility.ShowAll, CjkSuffixVisibility.MonthDayOnly -> true
      CjkSuffixVisibility.HideAll, CjkSuffixVisibility.YearOnly -> false
    },
    spacing = spacing
  )

  constructor(
    visibility: CjkSuffixVisibility,
    yearSuffixSpacing: Dp = DefaultCjkSuffixSpacing,
    monthSuffixSpacing: Dp = DefaultCjkSuffixSpacing,
    daySuffixSpacing: Dp = DefaultCjkSuffixSpacing
  ) : this(
    showYearSuffix = when (visibility) {
      CjkSuffixVisibility.ShowAll, CjkSuffixVisibility.YearOnly -> true
      CjkSuffixVisibility.HideAll, CjkSuffixVisibility.MonthDayOnly -> false
    },
    showMonthSuffix = when (visibility) {
      CjkSuffixVisibility.ShowAll, CjkSuffixVisibility.MonthDayOnly -> true
      CjkSuffixVisibility.HideAll, CjkSuffixVisibility.YearOnly -> false
    },
    showDaySuffix = when (visibility) {
      CjkSuffixVisibility.ShowAll, CjkSuffixVisibility.MonthDayOnly -> true
      CjkSuffixVisibility.HideAll, CjkSuffixVisibility.YearOnly -> false
    },
    yearSuffixSpacing = yearSuffixSpacing,
    monthSuffixSpacing = monthSuffixSpacing,
    daySuffixSpacing = daySuffixSpacing
  )

  companion object {
    val ShowAll = CjkSuffixConfig(CjkSuffixVisibility.ShowAll)
    val HideAll = CjkSuffixConfig(CjkSuffixVisibility.HideAll)
  }
}