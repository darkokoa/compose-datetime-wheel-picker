package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Largest rim angle the automatic default ever picks. Past it the outermost rows stop being
 * readable: at 70° a rim row is already squeezed to a third of its height (`cos 70°`).
 */
internal const val MAX_AUTO_RIM_ANGLE = 70f

/** Fade applied unless the caller asks otherwise: rows are fully transparent at the edge. */
internal const val DEFAULT_BARREL_FADE = 1f

/** Degrees of drum surface the automatic default gives each row away from the center. */
internal const val AUTO_RIM_DEGREES_PER_ROW = 13f

/**
 * Rim angle the automatic default gives a [WheelRows.Height] wheel: the full half cylinder of a
 * native iOS picker. Such a wheel promises no row count, so there is no outermost row that has to
 * stay readable at the rim.
 */
internal const val HEIGHT_MODE_RIM_ANGLE = 90f

/**
 * Distance from the viewer to the drum surface at the center row, as a multiple of the viewport
 * height. It only feeds the depth scale that shrinks rows as they recede; see
 * [calculateBarrelTransform].
 */
private const val EYE_DISTANCE_MULTIPLIER = 2f

/**
 * `GraphicsLayerScope.cameraDistance` given to every row.
 *
 * The projection computes its own perspective (position, tilt, and scale) from one shared
 * viewpoint so that all rows read as a single cylinder. A per-layer camera would add a second
 * vanishing point at each row's own center on top of that, so it is pushed far enough away to make
 * the platform transform orthographic. Compose documents the unit as pixels, but both backends
 * treat it as Skia camera inches at 72 px each, so this is about 720 000 px: for any plausible row
 * height the residual foreshortening is well under a thousandth.
 */
private const val ORTHOGRAPHIC_CAMERA_DISTANCE = 10_000f

private const val HALF_PI = (PI / 2).toFloat()

/**
 * Controls the cylindrical ("barrel") projection applied to wheel rows.
 *
 * The wheel's rows are laid out on the front of a vertical cylinder whose rim coincides with the
 * top and bottom edges of the viewport. Rows tilt away from the viewer, bunch up toward the rim,
 * and fade out as they leave it, giving the wheel the look of a physical drum. See [WheelRows]
 * for how the rows are sized on that drum.
 *
 * Create instances through [WheelPickerDefaults.barrelProperties], or let
 * [WheelPickerDefaults.barrelPropertiesFor] pick an angle suited to the rows.
 *
 * @property rimAngle Rotation, in degrees, of the drum surface where it meets the top and bottom
 * edges of the viewport. Must be in `[0, 90]`. Larger values bend the wheel more and compress the
 * outer rows harder; `90` shows the full half cylinder, matching a native iOS picker, and `0`
 * disables the projection entirely for a flat, evenly spaced wheel.
 * @property fadeStrength How strongly rows fade toward the top and bottom edges of the viewport,
 * in `[0, 1]`. A row's alpha is `1 - fadeStrength · t²`, where `t` is its on-screen distance
 * from the center as a fraction of half the viewport height: at `1` a row is fully transparent
 * by the time it reaches the edge, at `0` every row stays opaque and only the geometry conveys
 * depth. The fade follows the projected position rather than the drum angle, so it looks the
 * same on a gently curved wheel as on a full drum and also applies to a flat wheel; on a 90°
 * drum it coincides with `cos²` of the row's angle.
 */
@Immutable
class BarrelProperties internal constructor(
  val rimAngle: Float,
  val fadeStrength: Float,
) {
  init {
    require(rimAngle >= 0f && rimAngle <= 90f) {
      "rimAngle must be in [0, 90], was $rimAngle"
    }
    require(fadeStrength >= 0f && fadeStrength <= 1f) {
      "fadeStrength must be in [0, 1], was $fadeStrength"
    }
  }

  fun copy(
    rimAngle: Float = this.rimAngle,
    fadeStrength: Float = this.fadeStrength,
  ): BarrelProperties = BarrelProperties(rimAngle = rimAngle, fadeStrength = fadeStrength)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BarrelProperties) return false
    return rimAngle == other.rimAngle && fadeStrength == other.fadeStrength
  }

  override fun hashCode(): Int = 31 * rimAngle.hashCode() + fadeStrength.hashCode()

  override fun toString(): String =
    "BarrelProperties(rimAngle=$rimAngle, fadeStrength=$fadeStrength)"
}

/**
 * Ratio between the length of the visible drum surface (the "unrolled" arc from rim to rim) and
 * the viewport height. For a rim angle θ the cylinder radius is `(H / 2) / sin θ` and the visible
 * arc is `2 · R · θ`, so the ratio is `θ / sin θ`; `1` for a flat wheel.
 */
internal val BarrelProperties.arcLengthRatio: Float
  get() {
    if (rimAngle == 0f) return 1f
    val rimAngleRadians = rimAngle.toRadians()
    return rimAngleRadians / sin(rimAngleRadians)
  }

/**
 * Height of the flat list backing the wheel: the unrolled length of the visible drum surface,
 * which is longer than [viewportHeight] unless the wheel is flat.
 */
internal fun BarrelProperties.listHeight(viewportHeight: Dp): Dp = viewportHeight * arcLengthRatio

/**
 * Per-row graphics layer values produced by [calculateBarrelTransform]; [scale] applies to both
 * axes. Apply them to a row with [applyTo].
 */
internal class BarrelTransform(
  val alpha: Float,
  val rotationX: Float,
  val translationY: Float,
  val scale: Float,
) {
  /** Writes this transform to [scope], the graphics layer of the row it was computed for. */
  fun applyTo(scope: GraphicsLayerScope) {
    scope.alpha = alpha
    scope.rotationX = rotationX
    scope.translationY = translationY
    scope.scaleX = scale
    scope.scaleY = scale
    scope.cameraDistance = ORTHOGRAPHIC_CAMERA_DISTANCE
  }
}

private val IdentityTransform = BarrelTransform(alpha = 1f, rotationX = 0f, translationY = 0f, scale = 1f)
private val HiddenTransform = BarrelTransform(alpha = 0f, rotationX = 0f, translationY = 0f, scale = 1f)

/**
 * Alpha of a row whose on-screen center is [edgeFraction] of the way from the viewport center to
 * its edge: opaque at the center, `1 - fadeStrength` at the edge, quadratic in between so the
 * rows next to the center stay crisp and the fade accelerates toward the edge.
 */
private fun edgeFade(edgeFraction: Float, fadeStrength: Float): Float =
  (1f - fadeStrength * edgeFraction * edgeFraction).coerceIn(0f, 1f)

/**
 * Projects a row of the flat list onto the front of a vertical cylinder.
 *
 * The flat list is treated as the unrolled surface of the drum: [distanceToCenterPx] is an arc
 * length, so the row's angle is simply `distance / radius`, where the radius is chosen so that
 * a row rotated by [rimAngle] lands exactly on the viewport edge. The displayed Y position follows
 * the cylinder's sine curve, the row plane is rotated tangent to the cylinder, and its depth is
 * conveyed by a uniform scale from a viewer [EYE_DISTANCE_MULTIPLIER] viewport heights in front of
 * the center row (the platform's own per-layer camera is left orthographic, see
 * [ORTHOGRAPHIC_CAMERA_DISTANCE]). Alpha follows the projected position, not the angle: it is
 * `1 - fadeStrength · t²` for a row `t` of the way from the center to the viewport edge (see
 * [edgeFade]), and anything behind the rim is fully transparent whatever the strength.
 *
 * The position is deliberately an orthographic projection while only the size is perspective:
 * the scale pivots on the row's own center and never moves it. A true perspective would also
 * pull the row's position toward the center by the same factor, so the outermost rows would
 * stop short of the viewport edge (about 15 px on a 240 px, 11-row wheel at 70°) unless the
 * radius were enlarged to compensate, which in turn changes [arcLengthRatio] and every row
 * height. Keeping the position orthographic keeps the rim on the viewport edge and the sizing
 * math in [BarrelProperties] exact; the scale is a depth cue, not a camera.
 *
 * A [rimAngle] of `0` is a flat wheel: rows keep their geometry and only the fade applies.
 *
 * Inputs are expected to be validated by the caller: [viewportHeightPx] positive, [rimAngle] in
 * `[0, 90]` and [fadeStrength] in `[0, 1]` (see [BarrelProperties]). This function runs every
 * frame for every visible row, so it deliberately performs no validation.
 */
internal fun calculateBarrelTransform(
  distanceToCenterPx: Float,
  viewportHeightPx: Float,
  rimAngle: Float,
  fadeStrength: Float = DEFAULT_BARREL_FADE,
): BarrelTransform {
  val halfViewportPx = viewportHeightPx / 2f
  if (rimAngle == 0f) {
    if (fadeStrength == 0f) return IdentityTransform
    return BarrelTransform(
      alpha = edgeFade(distanceToCenterPx / halfViewportPx, fadeStrength),
      rotationX = 0f,
      translationY = 0f,
      scale = 1f,
    )
  }

  val radius = halfViewportPx / sin(rimAngle.toRadians())
  val angleRadians = distanceToCenterPx / radius

  // Behind the rim of the drum: hide the row rather than letting it wrap back into view.
  if (abs(angleRadians) >= HALF_PI) return HiddenTransform

  val cosAngle = cos(angleRadians)
  val projectedDistance = radius * sin(angleRadians)
  val depth = radius * (1f - cosAngle)
  val eyeDistance = viewportHeightPx * EYE_DISTANCE_MULTIPLIER

  return BarrelTransform(
    alpha = edgeFade(projectedDistance / halfViewportPx, fadeStrength),
    rotationX = -angleRadians.toDegrees(),
    translationY = projectedDistance - distanceToCenterPx,
    scale = eyeDistance / (eyeDistance + depth),
  )
}

private fun Float.toRadians(): Float = this / 180f * PI.toFloat()

private fun Float.toDegrees(): Float = this * 180f / PI.toFloat()
