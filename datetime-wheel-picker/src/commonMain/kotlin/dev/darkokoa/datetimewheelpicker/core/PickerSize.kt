package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Intrinsic picker size: [defaultWidth] by the height [rows] asks for when unconstrained. The
 * barrel angle plays no part; the rows are laid on a drum inside that viewport.
 */
internal fun pickerDefaultSize(defaultWidth: Dp, rows: WheelRows): DpSize =
  DpSize(defaultWidth, rows.intrinsicHeight)

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
