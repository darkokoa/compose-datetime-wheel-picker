package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BarrelTransformTest {

  private val rad70 = (70.0 * PI / 180).toFloat()

  @Test
  fun barrelProjectionIsDisabledByDefault() {
    val properties = WheelPickerDefaults.barrelProperties()

    assertFalse(properties.enabled)
    assertEquals(70f, properties.maxAngle)
  }

  @Test
  fun maxAngleIsValidatedOnConstruction() {
    val properties = WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 45f)

    assertTrue(properties.enabled)
    assertEquals(45f, properties.maxAngle)
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(maxAngle = 0f)
    }
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(maxAngle = 91f)
    }
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(maxAngle = 45f).copy(maxAngle = -1f)
    }
  }

  @Test
  fun propertiesUseValueEquality() {
    val a = WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 60f)
    val b = WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 60f)

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
    assertNotEquals(a, a.copy(enabled = false))
    assertNotEquals(a, a.copy(maxAngle = 30f))
  }

  @Test
  fun flatWheelKeepsViewportGeometry() {
    val flat = WheelPickerDefaults.barrelProperties()

    assertEquals(1f, flat.arcLengthRatio)
    assertEquals(240.dp, flat.listHeight(240.dp))
    assertEquals(240.dp / 7, flat.rowHeight(240.dp, 7))
  }

  @Test
  fun barrelListIsTheUnrolledDrumSurface() {
    val barrel = WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 70f)
    val expectedRatio = rad70 / sin(rad70)

    assertEquals(expectedRatio, barrel.arcLengthRatio, absoluteTolerance = 0.0001f)
    assertEquals(240.dp * expectedRatio, barrel.listHeight(240.dp))
    assertEquals(240.dp * expectedRatio / 11, barrel.rowHeight(240.dp, 11))
    assertTrue(barrel.rowHeight(240.dp, 11) > 240.dp / 11)
  }

  @Test
  fun defaultSizeGivesEachRowTheDefaultArcLength() {
    val barrel = WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 70f)
    val size = pickerDefaultSize(256.dp, 11, barrel)

    // Folding the default per-row length back onto the drum yields the default row height.
    assertEquals(DefaultWheelRowHeight.value, barrel.rowHeight(size.height, 11).value, absoluteTolerance = 0.001f)
    assertTrue(size.height < pickerDefaultSize(256.dp, 11).height)
  }

  @Test
  fun rowsSpanTheDrumFromRimToRim() {
    // Eleven rows in a 240px viewport: the outermost row center sits just inside the rim.
    val barrel = WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 70f)
    val rowHeightPx = 240f * barrel.arcLengthRatio / 11

    val outer = calculateBarrelTransform(
      distanceToCenterPx = rowHeightPx * 5,
      viewportHeightPx = 240f,
      maxAngle = 70f,
    )
    val rim = calculateBarrelTransform(
      distanceToCenterPx = rowHeightPx * 5.5f,
      viewportHeightPx = 240f,
      maxAngle = 70f,
    )

    assertEquals(-70f * 10 / 11, outer.rotationX, absoluteTolerance = 0.001f)
    assertEquals(-70f, rim.rotationX, absoluteTolerance = 0.001f)
    assertEquals(120f, rowHeightPx * 5.5f + rim.translationY, absoluteTolerance = 0.01f)
    assertTrue(outer.alpha > 0f)
  }

  @Test
  fun centerItemIsNotTransformed() {
    val transform = calculateBarrelTransform(
      distanceToCenterPx = 0f,
      viewportHeightPx = 240f,
      maxAngle = 45f,
    )

    assertEquals(1f, transform.alpha)
    assertEquals(0f, transform.rotationX, absoluteTolerance = 0.0001f)
    assertEquals(0f, transform.translationY, absoluteTolerance = 0.0001f)
    assertEquals(1f, transform.scale)
    assertEquals(480f, transform.cameraDistance)
  }

  @Test
  fun rowsRotateAndCompressTowardTheRim() {
    val barrel = WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 70f)
    val rowHeightPx = 240f * barrel.arcLengthRatio / 11
    val transform = calculateBarrelTransform(
      distanceToCenterPx = rowHeightPx,
      viewportHeightPx = 240f,
      maxAngle = 70f,
    )

    val expectedAngle = 70f * 2 / 11
    assertEquals(-expectedAngle, transform.rotationX, absoluteTolerance = 0.001f)
    val cosAngle = cos(expectedAngle / 180f * PI).toFloat()
    assertEquals(cosAngle * cosAngle, transform.alpha, absoluteTolerance = 0.0001f)
    // The drum never spreads rows apart: the projected position is closer to the center than
    // the arc position.
    assertTrue(transform.translationY < 0f)
    assertTrue(transform.scale < 1f)
  }

  @Test
  fun projectionIsSymmetricAroundCenter() {
    val above = calculateBarrelTransform(
      distanceToCenterPx = -80f,
      viewportHeightPx = 240f,
      maxAngle = 45f,
    )
    val below = calculateBarrelTransform(
      distanceToCenterPx = 80f,
      viewportHeightPx = 240f,
      maxAngle = 45f,
    )

    assertEquals(above.alpha, below.alpha)
    assertEquals(above.scale, below.scale)
    assertEquals(-above.rotationX, below.rotationX)
    assertTrue(abs(above.translationY + below.translationY) < 0.0001f)
  }

  @Test
  fun rowsBehindTheRimAreHidden() {
    // maxAngle 90: the radius equals half the viewport, so twice that arc is behind the drum.
    val transform = calculateBarrelTransform(
      distanceToCenterPx = 240f,
      viewportHeightPx = 240f,
      maxAngle = 90f,
    )

    assertEquals(0f, transform.alpha)
  }
}
