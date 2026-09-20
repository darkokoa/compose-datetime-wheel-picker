package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.runtime.Immutable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal const val DEFAULT_BARREL_MAX_ANGLE = 70f

/**
 * Multiplier applied to the viewport height to obtain the perspective camera distance. Compose
 * recommends a camera distance of at least the size of the rotated layer to avoid content ending
 * up behind the camera.
 */
private const val CAMERA_DISTANCE_MULTIPLIER = 2f

/**
 * Controls the optional cylindrical ("barrel") projection applied to wheel rows.
 *
 * When [enabled], rows are projected onto the front of a vertical cylinder so the wheel resembles
 * an iOS-style drum picker: rows tilt away from the viewer, bunch up toward the top and bottom
 * edges, and fade out as they leave the drum. When disabled (the default) the wheel keeps its
 * original flat appearance.
 *
 * Create instances through [WheelPickerDefaults.barrelProperties] so defaults stay centralized.
 *
 * @property enabled Whether rows are projected onto the cylinder.
 * @property maxAngle Rotation, in degrees, of the rows at the very top and bottom edges of the
 * wheel viewport. Must be in `(0, 90]`. Larger values produce a more pronounced curvature.
 */
@Immutable
class BarrelProperties internal constructor(
  val enabled: Boolean,
  val maxAngle: Float,
) {
  init {
    require(maxAngle > 0f && maxAngle <= 90f) {
      "maxAngle must be in (0, 90], was $maxAngle"
    }
  }

  fun copy(
    enabled: Boolean = this.enabled,
    maxAngle: Float = this.maxAngle,
  ): BarrelProperties = BarrelProperties(enabled = enabled, maxAngle = maxAngle)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BarrelProperties) return false
    return enabled == other.enabled && maxAngle == other.maxAngle
  }

  override fun hashCode(): Int = 31 * enabled.hashCode() + maxAngle.hashCode()

  override fun toString(): String = "BarrelProperties(enabled=$enabled, maxAngle=$maxAngle)"
}

/**
 * Per-row graphics layer values produced by [calculateBarrelTransform].
 *
 * [scale] applies to both axes; [cameraDistance] is expressed in pixels, matching
 * `GraphicsLayerScope.cameraDistance`.
 */
internal class BarrelTransform(
  val alpha: Float,
  val rotationX: Float,
  val translationY: Float,
  val scale: Float,
  val cameraDistance: Float,
)

/**
 * Projects an equally spaced wheel row onto the front of a vertical cylinder.
 *
 * The row's angle on the cylinder is proportional to its untransformed distance from the wheel
 * center, reaching [maxAngle] at the viewport edges. Its displayed Y position follows the
 * cylinder's sine curve, its plane is rotated tangent to the cylinder, and its depth is conveyed
 * by perspective scaling. Alpha fades linearly from 1 at the center to 0 at the viewport edges.
 *
 * Inputs are expected to be validated by the caller: [viewportHeightPx] positive and [maxAngle]
 * in `(0, 90]` (see [BarrelProperties]). This function runs every frame for every visible row, so
 * it deliberately performs no validation.
 */
internal fun calculateBarrelTransform(
  distanceToCenterPx: Float,
  viewportHeightPx: Float,
  maxAngle: Float,
): BarrelTransform {
  val halfViewportHeight = viewportHeightPx / 2f
  val normalizedDistance = (distanceToCenterPx / halfViewportHeight).coerceIn(-1f, 1f)
  val maxAngleRadians = maxAngle.toRadians()
  val angleRadians = normalizedDistance * maxAngleRadians
  val projectedDistance = sin(angleRadians) / sin(maxAngleRadians) * halfViewportHeight
  val depth = halfViewportHeight * (1f - cos(angleRadians))
  val cameraDistance = viewportHeightPx * CAMERA_DISTANCE_MULTIPLIER

  return BarrelTransform(
    alpha = (1f - abs(distanceToCenterPx) / halfViewportHeight).coerceIn(0f, 1f),
    rotationX = -normalizedDistance * maxAngle,
    translationY = projectedDistance - distanceToCenterPx,
    scale = cameraDistance / (cameraDistance + depth),
    cameraDistance = cameraDistance,
  )
}

private fun Float.toRadians(): Float = this / 180f * PI.toFloat()
