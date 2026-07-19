package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PickerSizeTest {

  private val density = Density(1f)

  private val dateDefault = pickerDefaultSize(defaultWidth = 256.dp, rowCount = 3)
  private val timeDefault = pickerDefaultSize(defaultWidth = 128.dp, rowCount = 3)

  private fun resolve(constraints: Constraints, default: DpSize = dateDefault): DpSize =
    with(density) { resolvePickerSize(constraints, default) }

  @Test
  fun looseLargeConstraintsReturnExactDateDefault() {
    val resolved = resolve(Constraints(maxWidth = 1000, maxHeight = 1000))
    assertEquals(DpSize(256.dp, 128.dp), resolved)
  }

  @Test
  fun looseLargeConstraintsReturnExactTimeDefault() {
    val resolved = resolve(Constraints(maxWidth = 1000, maxHeight = 1000), timeDefault)
    assertEquals(DpSize(128.dp, 128.dp), resolved)
  }

  @Test
  fun fixedWidthWithLooseHeightForcesWidthKeepsIntrinsicHeight() {
    val resolved = resolve(Constraints(minWidth = 500, maxWidth = 500, maxHeight = 1000))
    assertEquals(DpSize(500.dp, 128.dp), resolved)
  }

  @Test
  fun fixedHeightWithLooseWidthForcesHeightKeepsIntrinsicWidth() {
    val resolved = resolve(Constraints(maxWidth = 1000, minHeight = 200, maxHeight = 200))
    assertEquals(DpSize(256.dp, 200.dp), resolved)
  }

  @Test
  fun bothAxesFixedForceBothDimensions() {
    val resolved = resolve(Constraints(minWidth = 300, maxWidth = 300, minHeight = 160, maxHeight = 160))
    assertEquals(DpSize(300.dp, 160.dp), resolved)
  }

  @Test
  fun narrowMaxWidthClampsDefaultWidth() {
    val resolved = resolve(Constraints(maxWidth = 200, maxHeight = 1000))
    assertEquals(DpSize(200.dp, 128.dp), resolved)
  }

  @Test
  fun minWidthRaisesDefaultWidth() {
    val resolved = resolve(Constraints(minWidth = 300, maxWidth = 1000, maxHeight = 1000))
    assertEquals(DpSize(300.dp, 128.dp), resolved)
  }

  @Test
  fun unboundedMaxHeightReturnsIntrinsicHeightNotInfinity() {
    val resolved = resolve(Constraints(maxWidth = 1000, maxHeight = Constraints.Infinity))
    assertEquals(128.dp, resolved.height)
    assertTrue(resolved.height.value < Constraints.Infinity.toFloat())
  }

  @Test
  fun zeroForcedAxisRetainsFinitePositiveInternalValue() {
    val resolved = resolve(Constraints(minWidth = 0, maxWidth = 0, minHeight = 0, maxHeight = 0))
    assertEquals(DpSize(256.dp, 128.dp), resolved)
  }

  @Test
  fun fractionalIntrinsicPassesThroughIdenticallyOnLooseAxis() {
    val default = DpSize(100.3.dp, 100.3.dp)
    val resolved = resolve(Constraints(maxWidth = 1000, maxHeight = 1000), default)
    assertEquals(default, resolved)
  }

  @Test
  fun defaultSizeAtThreeRowsIsExactly128Tall() {
    assertEquals(128.dp, pickerDefaultSize(256.dp, 3).height)
  }

  @Test
  fun defaultSizeScalesWithRowCount() {
    assertEquals(DefaultWheelRowHeight * 5, pickerDefaultSize(256.dp, 5).height)
    assertEquals(DefaultWheelRowHeight * 7, pickerDefaultSize(128.dp, 7).height)
  }

  @Test
  fun nonPositiveRowCountFails() {
    assertFailsWith<IllegalArgumentException> { pickerDefaultSize(256.dp, 0) }
    assertFailsWith<IllegalArgumentException> { pickerDefaultSize(256.dp, -1) }
  }
}
