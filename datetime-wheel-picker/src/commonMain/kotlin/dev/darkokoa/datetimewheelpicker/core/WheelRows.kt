package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite

/** Per-row height of the default wheel viewport: 3 rows == the historical 128.dp default. */
internal val DefaultWheelRowHeight: Dp = 128.dp / 3

/**
 * Number of rows' worth of viewport a [WheelRows.Height] wheel gets when the caller does not
 * constrain its height. Combined with the 90° rim angle it shows about eleven rows, which is what
 * a native iOS picker does at its default 216pt / 32pt.
 */
internal const val DEFAULT_HEIGHT_MODE_ROWS = 7

/**
 * How a wheel divides its viewport into rows. Together with the viewport height and the barrel
 * angle this fixes the geometry of the drum; you choose which quantity to hold constant.
 *
 * - [Count] fixes the number of rows spanning the drum from rim to rim. The row height follows
 *   from the viewport height, so taller pickers get taller rows.
 * - [Height] fixes the height of a row. The number of rows on the drum follows from the viewport
 *   height and is usually fractional, with the outermost rows cut off at the rim exactly like a
 *   native iOS picker.
 */
@Immutable
sealed interface WheelRows {

  /**
   * A wheel showing exactly [count] rows from rim to rim, the outermost foreshortened against the
   * edge. The intrinsic picker height is `128.dp / 3` per row, so the default three rows measure
   * the historical 128.dp.
   *
   * The selected row sits at the center of the drum with as many whole rows above it as below,
   * so [count] must be odd: an even count could only be honored by cutting half a row off at
   * each rim, which is neither `count` rows nor `count + 1`.
   *
   * @throws IllegalArgumentException if [count] is not a positive odd number.
   */
  @Immutable
  class Count(val count: Int) : WheelRows {
    init {
      require(count > 0) { "count must be positive, was $count" }
      require(count % 2 == 1) {
        "count must be odd so the selected row sits at the center, was $count"
      }
    }

    override fun equals(other: Any?): Boolean = other is Count && other.count == count
    override fun hashCode(): Int = count
    override fun toString(): String = "WheelRows.Count($count)"
  }

  /**
   * A wheel whose rows are [rowHeight] tall regardless of the picker height. The selector matches
   * that height, and the drum shows as many rows as fit. The intrinsic picker height is seven
   * rows, which at the 90° default angle displays about eleven.
   */
  @Immutable
  class Height(val rowHeight: Dp) : WheelRows {
    init {
      require(rowHeight.isFinite && rowHeight > 0.dp) {
        "rowHeight must be finite and positive, was $rowHeight"
      }
    }

    override fun equals(other: Any?): Boolean = other is Height && other.rowHeight == rowHeight
    override fun hashCode(): Int = rowHeight.hashCode()
    override fun toString(): String = "WheelRows.Height($rowHeight)"
  }
}

/**
 * Height of a single row in the flat list backing the wheel. This is the arc length each row
 * occupies on the drum, which is also the on-screen height of the centered row since the
 * projection is linear near the center. Use it for the selector too.
 */
internal fun WheelRows.rowHeight(viewportHeight: Dp, barrelProperties: BarrelProperties): Dp =
  when (this) {
    is WheelRows.Count -> barrelProperties.listHeight(viewportHeight) / count
    is WheelRows.Height -> rowHeight
  }

/** Height a picker takes on an axis the caller left unconstrained. */
internal val WheelRows.intrinsicHeight: Dp
  get() = when (this) {
    is WheelRows.Count -> DefaultWheelRowHeight * count
    is WheelRows.Height -> rowHeight * DEFAULT_HEIGHT_MODE_ROWS
  }

/**
 * Maps a 1.3.x `rowCount` to [WheelRows.Count] for the hidden binary-compatibility overloads.
 * Those callers could pass an even count, which [WheelRows.Count] rejects; rounding up to the
 * next odd number keeps old binaries rendering instead of throwing, with the selected row now
 * properly centered.
 */
internal fun legacyRowCount(rowCount: Int): WheelRows.Count =
  WheelRows.Count(if (rowCount > 0 && rowCount % 2 == 0) rowCount + 1 else rowCount)
