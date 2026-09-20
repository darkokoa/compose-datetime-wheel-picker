package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BarrelTransformTest {

  private val rad70 = (70.0 * PI / 180).toFloat()

  @Test
  fun defaultAngleFollowsRowCount() {
    assertEquals(13f, WheelPickerDefaults.barrelPropertiesFor(1).maxAngle)
    assertEquals(26f, WheelPickerDefaults.barrelPropertiesFor(3).maxAngle)
    assertEquals(52f, WheelPickerDefaults.barrelPropertiesFor(5).maxAngle)
    assertEquals(70f, WheelPickerDefaults.barrelPropertiesFor(7).maxAngle)
    assertEquals(70f, WheelPickerDefaults.barrelPropertiesFor(11).maxAngle)
    assertFailsWith<IllegalArgumentException> { WheelPickerDefaults.barrelPropertiesFor(0) }
  }

  @Test
  fun maxAngleIsValidatedOnConstruction() {
    assertEquals(45f, WheelPickerDefaults.barrelProperties(maxAngle = 45f).maxAngle)
    assertEquals(0f, WheelPickerDefaults.barrelProperties(maxAngle = 0f).maxAngle)
    assertEquals(90f, WheelPickerDefaults.barrelProperties(maxAngle = 90f).maxAngle)
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(maxAngle = -0.1f)
    }
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(maxAngle = 90.1f)
    }
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(maxAngle = 45f).copy(maxAngle = -1f)
    }
  }

  @Test
  fun propertiesUseValueEquality() {
    val a = WheelPickerDefaults.barrelProperties(maxAngle = 60f)
    val b = WheelPickerDefaults.barrelProperties(maxAngle = 60f)

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
    assertNotEquals(a, a.copy(maxAngle = 30f))
    assertEquals(WheelPickerDefaults.barrelPropertiesFor(3), a.copy(maxAngle = 26f))
  }

  @Test
  fun flatWheelKeepsViewportGeometry() {
    val flat = WheelPickerDefaults.barrelProperties(maxAngle = 0f)

    assertEquals(1f, flat.arcLengthRatio)
    assertEquals(240.dp, flat.listHeight(240.dp))
    assertEquals(240.dp / 7, flat.rowHeight(240.dp, 7))

    val transform = calculateBarrelTransform(distanceToCenterPx = 100f, viewportHeightPx = 240f, maxAngle = 0f)
    assertEquals(1f, transform.alpha)
    assertEquals(0f, transform.rotationX)
    assertEquals(0f, transform.translationY)
    assertEquals(1f, transform.scale)
  }

  @Test
  fun listIsTheUnrolledDrumSurface() {
    val barrel = WheelPickerDefaults.barrelProperties(maxAngle = 70f)
    val expectedRatio = rad70 / sin(rad70)

    assertEquals(expectedRatio, barrel.arcLengthRatio, absoluteTolerance = 0.0001f)
    assertEquals(240.dp * expectedRatio, barrel.listHeight(240.dp))
    assertEquals(240.dp * expectedRatio / 11, barrel.rowHeight(240.dp, 11))
    assertTrue(barrel.rowHeight(240.dp, 11) > 240.dp / 11)
  }

  @Test
  fun defaultSizeIsIndependentOfBarrelAngle() {
    val size = pickerDefaultSize(256.dp, 11)

    // Same viewport whatever the angle; the drum gives the centered row more than the flat row height.
    assertEquals(DefaultWheelRowHeight * 11, size.height)
    assertTrue(WheelPickerDefaults.barrelPropertiesFor(11).rowHeight(size.height, 11) > DefaultWheelRowHeight)
    assertEquals(DefaultWheelRowHeight, WheelPickerDefaults.barrelProperties(maxAngle = 0f).rowHeight(size.height, 11))
  }

  @Test
  fun rowsSpanTheDrumFromRimToRim() {
    // Eleven rows in a 240px viewport: the outermost row center sits just inside the rim.
    val barrel = WheelPickerDefaults.barrelProperties(maxAngle = 70f)
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
    val barrel = WheelPickerDefaults.barrelProperties(maxAngle = 70f)
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
