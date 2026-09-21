package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class WheelRowsTest {

  @Test
  fun countMustBePositiveAndOdd() {
    assertEquals(1, WheelRows.Count(1).count)
    assertEquals(3, WheelRows.Count(3).count)
    assertEquals(7, WheelRows.Count(7).count)
    assertFailsWith<IllegalArgumentException> { WheelRows.Count(0) }
    assertFailsWith<IllegalArgumentException> { WheelRows.Count(-1) }
    assertFailsWith<IllegalArgumentException> { WheelRows.Count(2) }
    assertFailsWith<IllegalArgumentException> { WheelRows.Count(4) }
  }

  @Test
  fun heightMustBeFiniteAndPositive() {
    assertEquals(32.dp, WheelRows.Height(32.dp).rowHeight)
    assertFailsWith<IllegalArgumentException> { WheelRows.Height(0.dp) }
    assertFailsWith<IllegalArgumentException> { WheelRows.Height((-1).dp) }
    assertFailsWith<IllegalArgumentException> { WheelRows.Height(Dp.Infinity) }
    assertFailsWith<IllegalArgumentException> { WheelRows.Height(Dp.Unspecified) }
  }

  @Test
  fun rowsUseValueEquality() {
    assertEquals(WheelRows.Count(5), WheelRows.Count(5))
    assertEquals(WheelRows.Count(5).hashCode(), WheelRows.Count(5).hashCode())
    assertNotEquals(WheelRows.Count(5), WheelRows.Count(7))
    assertEquals(WheelRows.Height(32.dp), WheelRows.Height(32.dp))
    assertEquals(WheelRows.Height(32.dp).hashCode(), WheelRows.Height(32.dp).hashCode())
    assertNotEquals(WheelRows.Height(32.dp), WheelRows.Height(40.dp))
    assertNotEquals<WheelRows>(WheelRows.Count(5), WheelRows.Height(32.dp))
    assertEquals("WheelRows.Count(5)", WheelRows.Count(5).toString())
    assertEquals("WheelRows.Height(32.0.dp)", WheelRows.Height(32.dp).toString())
  }

  @Test
  fun intrinsicHeightFollowsTheMode() {
    assertEquals(128.dp, WheelRows.Count(3).intrinsicHeight)
    assertEquals(DefaultWheelRowHeight * 5, WheelRows.Count(5).intrinsicHeight)
    assertEquals(32.dp * DEFAULT_HEIGHT_MODE_ROWS, WheelRows.Height(32.dp).intrinsicHeight)
  }

  @Test
  fun legacyRowCountRoundsEvenCountsUpAndRejectsTheRest() {
    assertEquals(WheelRows.Count(3), legacyRowCount(3))
    assertEquals(WheelRows.Count(3), legacyRowCount(2))
    assertEquals(WheelRows.Count(5), legacyRowCount(4))
    assertFailsWith<IllegalArgumentException> { legacyRowCount(0) }
    assertFailsWith<IllegalArgumentException> { legacyRowCount(-2) }
  }
}
