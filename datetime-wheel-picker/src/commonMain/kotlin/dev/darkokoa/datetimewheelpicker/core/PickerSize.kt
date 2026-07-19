package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** Per-row height of the default wheel viewport: 3 rows == the historical 128.dp default. */
internal val DefaultWheelRowHeight: Dp = 128.dp / 3

internal fun pickerDefaultSize(defaultWidth: Dp, rowCount: Int): DpSize {
  require(rowCount > 0) { "rowCount must be positive, was $rowCount" }
  return DpSize(defaultWidth, DefaultWheelRowHeight * rowCount)
}

/**
 * Resolves the effective picker viewport from the incoming [constraints] and the picker's
 * intrinsic [default] size. Each axis resolves independently: a loose axis keeps the exact
 * default Dp value (no px round-trip), a bound axis is clamped/forced by the constraints,
 * and a zero-forced axis falls back to the default so wheel snap math stays finite.
 */
internal fun Density.resolvePickerSize(
  constraints: Constraints,
  default: DpSize,
): DpSize = DpSize(
  width = resolvePickerAxis(default.width, constraints.minWidth, constraints.maxWidth),
  height = resolvePickerAxis(default.height, constraints.minHeight, constraints.maxHeight),
)

private fun Density.resolvePickerAxis(default: Dp, minPx: Int, maxPx: Int): Dp {
  val defaultPx = default.roundToPx()
  val constrainedPx = defaultPx.coerceIn(minPx, maxPx)
  return when {
    constrainedPx == defaultPx -> default
    constrainedPx > 0 -> constrainedPx.toDp()
    else -> default
  }
}
