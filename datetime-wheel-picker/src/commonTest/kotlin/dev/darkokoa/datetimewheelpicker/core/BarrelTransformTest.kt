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
  fun defaultAngleFollowsRows() {
    assertEquals(13f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(1)).rimAngle)
    assertEquals(26f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(3)).rimAngle)
    assertEquals(52f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(5)).rimAngle)
    assertEquals(70f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(7)).rimAngle)
    assertEquals(70f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(11)).rimAngle)
    assertEquals(90f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Height(32.dp)).rimAngle)
  }

  @Test
  fun rimAngleIsValidatedOnConstruction() {
    assertEquals(45f, WheelPickerDefaults.barrelProperties(rimAngle = 45f).rimAngle)
    assertEquals(0f, WheelPickerDefaults.barrelProperties(rimAngle = 0f).rimAngle)
    assertEquals(90f, WheelPickerDefaults.barrelProperties(rimAngle = 90f).rimAngle)
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(rimAngle = -0.1f)
    }
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(rimAngle = 90.1f)
    }
    assertFailsWith<IllegalArgumentException> {
      WheelPickerDefaults.barrelProperties(rimAngle = 45f).copy(rimAngle = -1f)
    }
  }

  @Test
  fun fadeIsValidatedAndDefaultsToFull() {
    assertEquals(1f, WheelPickerDefaults.barrelProperties(rimAngle = 45f).fadeStrength)
    assertEquals(1f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(5)).fadeStrength)
    assertEquals(1f, WheelPickerDefaults.barrelPropertiesFor(WheelRows.Height(32.dp)).fadeStrength)
    assertEquals(0.4f, WheelPickerDefaults.barrelProperties(rimAngle = 45f, fadeStrength = 0.4f).fadeStrength)
    assertFailsWith<IllegalArgumentException> { WheelPickerDefaults.barrelProperties(45f, fadeStrength = -0.1f) }
    assertFailsWith<IllegalArgumentException> { WheelPickerDefaults.barrelProperties(45f, fadeStrength = 1.1f) }
  }

  @Test
  fun fadeBlendsBetweenOpaqueAndCosineSquared() {
    val angle = 60f
    val cos2 = cos(angle / 180f * PI).toFloat().let { it * it }
    fun alphaAt(fadeStrength: Float) = calculateBarrelTransform(
      // At rimAngle 90 the radius is half the viewport, so an arc of R·θ lands at angle θ.
      distanceToCenterPx = 120f * (angle / 180f * PI).toFloat(),
      viewportHeightPx = 240f,
      rimAngle = 90f,
      fadeStrength = fadeStrength,
    ).alpha

    assertEquals(cos2, alphaAt(1f), absoluteTolerance = 0.0001f)
    assertEquals(1f, alphaAt(0f), absoluteTolerance = 0.0001f)
    assertEquals(1f - 0.5f * (1f - cos2), alphaAt(0.5f), absoluteTolerance = 0.0001f)
  }

  @Test
  fun rowsBehindTheRimStayHiddenRegardlessOfFade() {
    val transform = calculateBarrelTransform(
      distanceToCenterPx = 240f,
      viewportHeightPx = 240f,
      rimAngle = 90f,
      fadeStrength = 0f,
    )

    assertEquals(0f, transform.alpha)
  }

  @Test
  fun propertiesUseValueEquality() {
    val a = WheelPickerDefaults.barrelProperties(rimAngle = 60f)
    val b = WheelPickerDefaults.barrelProperties(rimAngle = 60f)

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
    assertNotEquals(a, a.copy(rimAngle = 30f))
    assertNotEquals(a, a.copy(fadeStrength = 0.5f))
    assertEquals(WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(3)), a.copy(rimAngle = 26f))
  }

  @Test
  fun flatWheelKeepsViewportGeometry() {
    val flat = WheelPickerDefaults.barrelProperties(rimAngle = 0f)

    assertEquals(1f, flat.arcLengthRatio)
    assertEquals(240.dp, flat.listHeight(240.dp))
    assertEquals(240.dp / 7, WheelRows.Count(7).rowHeight(240.dp, flat))
    assertEquals(32.dp, WheelRows.Height(32.dp).rowHeight(240.dp, flat))

    val transform = calculateBarrelTransform(distanceToCenterPx = 100f, viewportHeightPx = 240f, rimAngle = 0f)
    assertEquals(1f, transform.alpha)
    assertEquals(0f, transform.rotationX)
    assertEquals(0f, transform.translationY)
    assertEquals(1f, transform.scale)
  }

  @Test
  fun listIsTheUnrolledDrumSurface() {
    val barrel = WheelPickerDefaults.barrelProperties(rimAngle = 70f)
    val expectedRatio = rad70 / sin(rad70)

    assertEquals(expectedRatio, barrel.arcLengthRatio, absoluteTolerance = 0.0001f)
    assertEquals(240.dp * expectedRatio, barrel.listHeight(240.dp))
    assertEquals(240.dp * expectedRatio / 11, WheelRows.Count(11).rowHeight(240.dp, barrel))
    assertTrue(WheelRows.Count(11).rowHeight(240.dp, barrel) > 240.dp / 11)
    // Height rows ignore the angle entirely.
    assertEquals(32.dp, WheelRows.Height(32.dp).rowHeight(240.dp, barrel))
  }

  @Test
  fun defaultSizeIsIndependentOfBarrelAngle() {
    val rows = WheelRows.Count(11)
    val size = pickerDefaultSize(256.dp, rows)

    // Same viewport whatever the angle; the drum gives the centered row more than the flat row height.
    assertEquals(DefaultWheelRowHeight * 11, size.height)
    assertTrue(rows.rowHeight(size.height, WheelPickerDefaults.barrelPropertiesFor(rows)) > DefaultWheelRowHeight)
    assertEquals(DefaultWheelRowHeight, rows.rowHeight(size.height, WheelPickerDefaults.barrelProperties(rimAngle = 0f)))
  }

  @Test
  fun rowsSpanTheDrumFromRimToRim() {
    // Eleven rows in a 240px viewport: the outermost row center sits just inside the rim.
    val barrel = WheelPickerDefaults.barrelProperties(rimAngle = 70f)
    val rowHeightPx = 240f * barrel.arcLengthRatio / 11

    val outer = calculateBarrelTransform(
      distanceToCenterPx = rowHeightPx * 5,
      viewportHeightPx = 240f,
      rimAngle = 70f,
    )
    val rim = calculateBarrelTransform(
      distanceToCenterPx = rowHeightPx * 5.5f,
      viewportHeightPx = 240f,
      rimAngle = 70f,
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
      rimAngle = 45f,
    )

    assertEquals(1f, transform.alpha)
    assertEquals(0f, transform.rotationX, absoluteTolerance = 0.0001f)
    assertEquals(0f, transform.translationY, absoluteTolerance = 0.0001f)
    assertEquals(1f, transform.scale)
  }

  @Test
  fun rowsRotateAndCompressTowardTheRim() {
    val barrel = WheelPickerDefaults.barrelProperties(rimAngle = 70f)
    val rowHeightPx = 240f * barrel.arcLengthRatio / 11
    val transform = calculateBarrelTransform(
      distanceToCenterPx = rowHeightPx,
      viewportHeightPx = 240f,
      rimAngle = 70f,
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
      rimAngle = 45f,
    )
    val below = calculateBarrelTransform(
      distanceToCenterPx = 80f,
      viewportHeightPx = 240f,
      rimAngle = 45f,
    )

    assertEquals(above.alpha, below.alpha)
    assertEquals(above.scale, below.scale)
    assertEquals(-above.rotationX, below.rotationX)
    assertTrue(abs(above.translationY + below.translationY) < 0.0001f)
  }

  @Test
  fun rowsBehindTheRimAreHidden() {
    // rimAngle 90: the radius equals half the viewport, so twice that arc is behind the drum.
    val transform = calculateBarrelTransform(
      distanceToCenterPx = 240f,
      viewportHeightPx = 240f,
      rimAngle = 90f,
    )

    assertEquals(0f, transform.alpha)
  }
}
