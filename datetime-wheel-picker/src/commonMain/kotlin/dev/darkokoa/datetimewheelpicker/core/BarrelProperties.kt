package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Maximum rim angle the automatic default ever picks; larger values start hiding rows behind the rim. */
internal const val MAX_AUTO_BARREL_ANGLE = 70f

/** Degrees of drum surface the automatic default gives each row away from the center. */
internal const val AUTO_BARREL_DEGREES_PER_ROW = 13f

/**
 * Multiplier applied to the viewport height to obtain the perspective camera distance. Compose
 * recommends a camera distance of at least the size of the rotated layer to avoid content ending
 * up behind the camera.
 */
private const val CAMERA_DISTANCE_MULTIPLIER = 2f

private const val HALF_PI = (PI / 2).toFloat()

/**
 * Controls the cylindrical ("barrel") projection applied to wheel rows.
 *
 * The wheel's `rowCount` rows are laid out on the front of a vertical cylinder whose rim
 * coincides with the top and bottom edges of the viewport. Rows tilt away from the viewer, bunch
 * up toward the rim, and fade out as they leave it, giving the wheel the look of a physical drum.
 * `rowCount` keeps its meaning: it is the number of rows spanning the visible drum from rim to
 * rim, the outermost ones partially foreshortened.
 *
 * Create instances through [WheelPickerDefaults.barrelProperties], or let
 * [WheelPickerDefaults.barrelPropertiesFor] pick an angle suited to the row count.
 *
 * @property maxAngle Rotation, in degrees, of the drum surface where it meets the top and bottom
 * edges of the viewport. Must be in `[0, 90]`. Larger values bend the wheel more and compress the
 * outer rows harder; `90` shows the full half cylinder, matching a native iOS picker, and `0`
 * disables the projection entirely for a flat, evenly spaced wheel.
 */
@Immutable
class BarrelProperties internal constructor(
  val maxAngle: Float,
) {
  init {
    require(maxAngle >= 0f && maxAngle <= 90f) {
      "maxAngle must be in [0, 90], was $maxAngle"
    }
  }

  fun copy(maxAngle: Float = this.maxAngle): BarrelProperties = BarrelProperties(maxAngle = maxAngle)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BarrelProperties) return false
    return maxAngle == other.maxAngle
  }

  override fun hashCode(): Int = maxAngle.hashCode()

  override fun toString(): String = "BarrelProperties(maxAngle=$maxAngle)"
}

/**
 * Ratio between the length of the visible drum surface (the "unrolled" arc from rim to rim) and
 * the viewport height. For a rim angle θ the cylinder radius is `(H / 2) / sin θ` and the visible
 * arc is `2 · R · θ`, so the ratio is `θ / sin θ`; `1` for a flat wheel.
 */
internal val BarrelProperties.arcLengthRatio: Float
  get() {
    if (maxAngle == 0f) return 1f
    val maxAngleRadians = maxAngle.toRadians()
    return maxAngleRadians / sin(maxAngleRadians)
  }

/**
 * Height of the flat list backing the wheel: the unrolled length of the visible drum surface,
 * which is longer than [viewportHeight] unless the wheel is flat.
 */
internal fun BarrelProperties.listHeight(viewportHeight: Dp): Dp = viewportHeight * arcLengthRatio

/**
 * Height of a single row in the flat list backing the wheel. This is the arc length each row
 * occupies on the drum, which is also the on-screen height of the centered row since the
 * projection is linear near the center. Use it for the selector too.
 */
internal fun BarrelProperties.rowHeight(viewportHeight: Dp, rowCount: Int): Dp =
  listHeight(viewportHeight) / rowCount

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
 * Projects a row of the flat list onto the front of a vertical cylinder.
 *
 * The flat list is treated as the unrolled surface of the drum: [distanceToCenterPx] is an arc
 * length, so the row's angle is simply `distance / radius`, where the radius is chosen so that
 * a row rotated by [maxAngle] lands exactly on the viewport edge. The displayed Y position follows
 * the cylinder's sine curve, the row plane is rotated tangent to the cylinder, and its depth is
 * conveyed by perspective scaling. Alpha follows `cos²` of the angle: near rows stay crisp, rim
 * rows dim quickly, and anything behind the rim is fully transparent.
 *
 * A [maxAngle] of `0` is a flat wheel: every row is returned untouched.
 *
 * Inputs are expected to be validated by the caller: [viewportHeightPx] positive and [maxAngle]
 * in `[0, 90]` (see [BarrelProperties]). This function runs every frame for every visible row, so
 * it deliberately performs no validation.
 */
internal fun calculateBarrelTransform(
  distanceToCenterPx: Float,
  viewportHeightPx: Float,
  maxAngle: Float,
): BarrelTransform {
  val cameraDistance = viewportHeightPx * CAMERA_DISTANCE_MULTIPLIER
  if (maxAngle == 0f) {
    return BarrelTransform(
      alpha = 1f,
      rotationX = 0f,
      translationY = 0f,
      scale = 1f,
      cameraDistance = cameraDistance,
    )
  }
  val radius = viewportHeightPx / 2f / sin(maxAngle.toRadians())
  val angleRadians = distanceToCenterPx / radius

  if (abs(angleRadians) >= HALF_PI) {
    // Behind the rim of the drum: hide the row rather than letting it wrap back into view.
    return BarrelTransform(
      alpha = 0f,
      rotationX = 0f,
      translationY = 0f,
      scale = 1f,
      cameraDistance = cameraDistance,
    )
  }

  val cosAngle = cos(angleRadians)
  val projectedDistance = radius * sin(angleRadians)
  val depth = radius * (1f - cosAngle)

  return BarrelTransform(
    alpha = cosAngle * cosAngle,
    rotationX = -angleRadians.toDegrees(),
    translationY = projectedDistance - distanceToCenterPx,
    scale = cameraDistance / (cameraDistance + depth),
    cameraDistance = cameraDistance,
  )
}

private fun Float.toRadians(): Float = this / 180f * PI.toFloat()

private fun Float.toDegrees(): Float = this * 180f / PI.toFloat()
