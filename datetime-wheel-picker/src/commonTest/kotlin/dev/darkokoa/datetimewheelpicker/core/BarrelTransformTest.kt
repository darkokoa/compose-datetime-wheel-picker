package dev.darkokoa.datetimewheelpicker.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarrelTransformTest {

    @Test
    fun barrelProjectionIsDisabledByDefault() {
        val properties = WheelPickerDefaults.barrelProperties()

        assertFalse(properties.enabled)
        assertEquals(70f, properties.maxAngle)
    }

    @Test
    fun barrelProjectionAngleIsConfigurableAndValidated() {
        val properties = WheelPickerDefaults.barrelProperties(
            enabled = true,
            maxAngle = 45f,
        )

        assertTrue(properties.enabled)
        assertEquals(45f, properties.maxAngle)
        assertFailsWith<IllegalArgumentException> {
            WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 0f)
        }
        assertFailsWith<IllegalArgumentException> {
            WheelPickerDefaults.barrelProperties(enabled = true, maxAngle = 91f)
        }
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
        assertEquals(1f, transform.depthScale)
    }

    @Test
    fun itemsRotateAndMoveOutwardAlongCylinder() {
        val transform = calculateBarrelTransform(
            distanceToCenterPx = 240f / 7f,
            viewportHeightPx = 240f,
            maxAngle = 45f,
        )

        assertEquals(-45f * 2f / 7f, transform.rotationX, absoluteTolerance = 0.0001f)
        assertEquals(5f / 7f, transform.alpha, absoluteTolerance = 0.0001f)
        assertTrue(transform.translationY > 0f)
        assertTrue(transform.depthScale < 1f)
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
        assertEquals(above.depthScale, below.depthScale)
        assertEquals(-above.rotationX, below.rotationX)
        assertTrue(abs(above.translationY + below.translationY) < 0.0001f)
    }

    @Test
    fun transformClampsAtWheelEdges() {
        val transform = calculateBarrelTransform(
            distanceToCenterPx = 240f,
            viewportHeightPx = 240f,
            maxAngle = 45f,
        )

        assertEquals(0f, transform.alpha)
        assertEquals(-45f, transform.rotationX)
    }
}
