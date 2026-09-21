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
   * A wheel showing [count] rows from rim to rim, the outermost foreshortened against the edge.
   * The intrinsic picker height is `128.dp / 3` per row, so the default three rows measure the
   * historical 128.dp.
   *
   * The selected row sits at the center of the drum with as many whole rows above it as below,
   * so the drum always holds an odd number of rows. An even [count] is laid out as the next odd
   * number, `Count(4)` showing the same five rows as `Count(5)`, rather than cutting half a row
   * off at each rim. [count] itself is kept as given for equality and display.
   */
  @Immutable
  class Count(val count: Int) : WheelRows {
    init {
      require(count > 0) { "count must be positive, was $count" }
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
 * Rows actually laid out on the drum for a [WheelRows.Count]: the requested count, or the next
 * odd number when it is even, so the selected row is centered with whole rows on both sides.
 * All geometry goes through this rather than [WheelRows.Count.count].
 */
internal val WheelRows.Count.drumRows: Int
  get() = if (count % 2 == 0) count + 1 else count

/**
 * Height of a single row in the flat list backing the wheel. This is the arc length each row
 * occupies on the drum, which is also the on-screen height of the centered row since the
 * projection is linear near the center. Use it for the selector too.
 */
internal fun WheelRows.rowHeight(viewportHeight: Dp, barrelProperties: BarrelProperties): Dp =
  when (this) {
    is WheelRows.Count -> barrelProperties.listHeight(viewportHeight) / drumRows
    is WheelRows.Height -> rowHeight
  }

/** Height a picker takes on an axis the caller left unconstrained. */
internal val WheelRows.intrinsicHeight: Dp
  get() = when (this) {
    is WheelRows.Count -> DefaultWheelRowHeight * drumRows
    is WheelRows.Height -> rowHeight * DEFAULT_HEIGHT_MODE_ROWS
  }
