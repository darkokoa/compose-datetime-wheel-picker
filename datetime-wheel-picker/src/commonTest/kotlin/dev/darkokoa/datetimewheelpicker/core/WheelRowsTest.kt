package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class WheelRowsTest {

  @Test
  fun countMustBePositive() {
    assertEquals(1, WheelRows.Count(1).count)
    assertEquals(3, WheelRows.Count(3).count)
    assertFailsWith<IllegalArgumentException> { WheelRows.Count(0) }
    assertFailsWith<IllegalArgumentException> { WheelRows.Count(-1) }
  }

  @Test
  fun evenCountKeepsItsValueButLaysOutAsTheNextOdd() {
    // The object stays honest about what was asked...
    assertEquals(4, WheelRows.Count(4).count)
    assertEquals("WheelRows.Count(4)", WheelRows.Count(4).toString())
    assertNotEquals(WheelRows.Count(4), WheelRows.Count(5))
    // ...while every geometric quantity uses the odd row count.
    assertEquals(5, WheelRows.Count(4).drumRows)
    assertEquals(3, WheelRows.Count(2).drumRows)
    assertEquals(7, WheelRows.Count(7).drumRows)
    assertEquals(WheelRows.Count(5).intrinsicHeight, WheelRows.Count(4).intrinsicHeight)
    val flat = WheelPickerDefaults.barrelProperties(rimAngle = 0f)
    assertEquals(WheelRows.Count(5).resolveRowHeight(240.dp, flat), WheelRows.Count(4).resolveRowHeight(240.dp, flat))
    assertEquals(
      WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(5)),
      WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(4)),
    )
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
}
