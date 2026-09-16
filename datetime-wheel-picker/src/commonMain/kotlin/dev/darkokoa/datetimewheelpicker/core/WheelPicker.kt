package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val DEFAULT_BARREL_MAX_ANGLE = 70f
private const val CAMERA_DISTANCE_MULTIPLIER = 2f

/** State used to observe and programmatically scroll a wheel picker. */
@Stable
class WheelPickerState internal constructor(
  internal val lazyListState: LazyListState,
) {
  internal var isScrollFinishedCallbackPending by mutableStateOf(false)

  val isScrollInProgress: Boolean
    get() = lazyListState.isScrollInProgress || isScrollFinishedCallbackPending

  /** Animates the wheel until [index] is centered. */
  suspend fun animateScrollToItem(index: Int) {
    lazyListState.animateScrollToItem(index)
  }
}

/** Creates and remembers a [WheelPickerState] initially positioned at [initialIndex]. */
@Composable
fun rememberWheelPickerState(initialIndex: Int = 0): WheelPickerState {
  val lazyListState = rememberLazyListState(initialIndex)
  return remember(lazyListState) { WheelPickerState(lazyListState) }
}

@Composable
internal fun WheelPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  count: Int,
  rowCount: Int,
  viewportSize: DpSize = DpSize(128.dp, 128.dp),
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  barrelProperties: BarrelProperties = WheelPickerDefaults.barrelProperties(),
  onScrollChanged: (snappedIndex: Int) -> Unit = {},
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
  state: WheelPickerState = rememberWheelPickerState(startIndex),
  content: @Composable LazyItemScope.(index: Int, isSelected: Boolean) -> Unit,
) {
  require(rowCount > 0) { "rowCount must be positive, was $rowCount" }
  require(viewportSize.height.isFinite && viewportSize.height > 0.dp) {
    "viewportSize.height must be finite and positive, was ${viewportSize.height}"
  }
  val lazyListState = state.lazyListState
  val flingBehavior = rememberSnapFlingBehavior(lazyListState)
  val latestOnScrollChanged by rememberUpdatedState(onScrollChanged)
  val latestOnScrollFinished by rememberUpdatedState(onScrollFinished)
  val density = LocalDensity.current
  val singleViewPortHeightPx = remember(viewportSize, rowCount, density) {
    with(density) { viewportSize.height.toPx() } / rowCount
  }
  val viewportHeightPx = remember(viewportSize, density) {
    with(density) { viewportSize.height.toPx() }
  }
  val snappedItemIndexState = remember(lazyListState) {
    derivedStateOf { calculateSnappedItemIndex(lazyListState) }
  }

  LaunchedEffect(lazyListState) {
    snapshotFlow { snappedItemIndexState.value }
      .distinctUntilChanged()
      .drop(1)
      .collect { latestOnScrollChanged(it) }
  }

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.isScrollInProgress }
      .distinctUntilChanged()
      .drop(1)
      .filter { !it }
      .collect {
        state.isScrollFinishedCallbackPending = true
        try {
          // A finished drag may be immediately followed by a snap fling. Wait one frame so this
          // drag-to-fling handoff gap is not misreported as a finished scroll, which would fire
          // onScrollFinished twice for a single gesture.
          withFrameNanos { }
          if (lazyListState.isScrollInProgress) return@collect
          val snappedIndex = calculateSnappedItemIndex(lazyListState)
          latestOnScrollFinished(snappedIndex)
            ?.takeIf { it != snappedIndex }
            ?.let { lazyListState.scrollToItem(it) }
        } finally {
          state.isScrollFinishedCallbackPending = false
        }
      }
  }

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    if (selectorProperties.enabled().value) {
      Surface(
        modifier = Modifier
          .size(viewportSize.width, viewportSize.height / rowCount),
        shape = selectorProperties.shape().value,
        color = selectorProperties.color().value,
        border = selectorProperties.border().value
      ) {}
    }
    LazyColumn(
      modifier = Modifier
        .height(viewportSize.height)
        .width(viewportSize.width),
      state = lazyListState,
      contentPadding = PaddingValues(vertical = viewportSize.height / rowCount * ((rowCount - 1) / 2)),
      flingBehavior = flingBehavior
    ) {
      items(count) { index ->
        val isSelected by remember(snappedItemIndexState, index) {
          derivedStateOf { index == snappedItemIndexState.value }
        }
        Box(
          modifier = Modifier
            .height(viewportSize.height / rowCount)
            .width(viewportSize.width)
            .graphicsLayer {
              val centerIndex = lazyListState.firstVisibleItemIndex
              val centerIndexOffset = lazyListState.firstVisibleItemScrollOffset
              val distanceToCenterIndex = index - centerIndex
              val distanceToIndexSnap = distanceToCenterIndex * singleViewPortHeightPx - centerIndexOffset
              if (barrelProperties.enabled) {
                val transform = calculateBarrelTransform(
                  distanceToCenterPx = distanceToIndexSnap,
                  viewportHeightPx = viewportHeightPx,
                  maxAngle = barrelProperties.maxAngle,
                )

                alpha = transform.alpha
                rotationX = transform.rotationX
                translationY = transform.translationY
                scaleX = transform.depthScale
                scaleY = transform.depthScale
                cameraDistance = viewportHeightPx * CAMERA_DISTANCE_MULTIPLIER
              } else {
                val distanceToIndexSnapAbs = abs(distanceToIndexSnap)
                alpha = if (distanceToIndexSnapAbs <= singleViewPortHeightPx) {
                  1.2f - distanceToIndexSnapAbs / singleViewPortHeightPx
                } else {
                  0.2f
                }
                rotationX = -20f * distanceToIndexSnap / singleViewPortHeightPx
              }
            },
          contentAlignment = Alignment.Center
        ) {
          content(index, isSelected)
        }
      }
    }
  }
}

internal data class BarrelTransform(
  val alpha: Float,
  val rotationX: Float,
  val translationY: Float,
  val depthScale: Float,
)

/**
 * Projects an equally spaced list item onto the front of a vertical cylinder.
 *
 * The item's angle is proportional to its untransformed distance from the wheel center. Its
 * displayed Y coordinate follows the cylinder's sine curve, its plane is rotated tangent to the
 * cylinder, and its depth is represented by perspective scaling. This mirrors the geometry used
 * by the Android view picker that this component replaces.
 */
internal fun calculateBarrelTransform(
  distanceToCenterPx: Float,
  viewportHeightPx: Float,
  maxAngle: Float,
): BarrelTransform {
  require(viewportHeightPx > 0f) {
    "viewportHeightPx must be positive, was $viewportHeightPx"
  }
  require(maxAngle > 0f && maxAngle <= 90f) {
    "maxAngle must be in (0, 90], was $maxAngle"
  }

  val halfViewportHeight = viewportHeightPx / 2f
  val normalizedDistance =
    (distanceToCenterPx / halfViewportHeight).coerceIn(-1f, 1f)
  val maxAngleRadians = maxAngle.toRadians()
  val angleRadians = normalizedDistance * maxAngleRadians
  val projectedDistance =
    sin(angleRadians) / sin(maxAngleRadians) * halfViewportHeight
  val depth = halfViewportHeight * (1f - cos(angleRadians))
  val cameraDistance = viewportHeightPx * CAMERA_DISTANCE_MULTIPLIER

  return BarrelTransform(
    alpha = (1f - abs(distanceToCenterPx) / halfViewportHeight).coerceIn(0f, 1f),
    rotationX = -normalizedDistance * maxAngle,
    translationY = projectedDistance - distanceToCenterPx,
    depthScale = cameraDistance / (cameraDistance + depth),
  )
}

private fun Float.toRadians(): Float = this / 180f * PI.toFloat()

private fun calculateSnappedItemIndex(lazyListState: LazyListState): Int {
  val currentItemIndex = lazyListState.firstVisibleItemIndex
  val itemCount = lazyListState.layoutInfo.totalItemsCount
  val offset = lazyListState.firstVisibleItemScrollOffset
  val itemHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: return currentItemIndex

  return if (offset > itemHeight / 2 && currentItemIndex < itemCount - 1) {
    currentItemIndex + 1
  } else {
    currentItemIndex
  }
}

object WheelPickerDefaults {
  /**
   * Configures optional cylindrical projection of wheel rows.
   *
   * Barrel projection is disabled by default to preserve the library's original appearance.
   * [maxAngle] controls the rotation at the top and bottom edges of the wheel viewport.
   */
  fun barrelProperties(
    enabled: Boolean = false,
    maxAngle: Float = DEFAULT_BARREL_MAX_ANGLE,
  ): BarrelProperties = BarrelProperties(
    enabled = enabled,
    maxAngle = maxAngle,
  )

  @Composable
  fun selectorProperties(
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
  ): SelectorProperties = DefaultSelectorProperties(
    enabled = enabled,
    shape = shape,
    color = color,
    border = border
  )
}

/**
 * Controls the optional cylindrical projection applied to wheel rows.
 *
 * Prefer [WheelPickerDefaults.barrelProperties] when using a picker API so new defaults remain
 * centralized.
 */
@Immutable
data class BarrelProperties(
  val enabled: Boolean,
  val maxAngle: Float,
) {
  init {
    require(maxAngle > 0f && maxAngle <= 90f) {
      "maxAngle must be in (0, 90], was $maxAngle"
    }
  }
}

interface SelectorProperties {
  @Composable
  fun enabled(): State<Boolean>

  @Composable
  fun shape(): State<Shape>

  @Composable
  fun color(): State<Color>

  @Composable
  fun border(): State<BorderStroke?>
}

@Immutable
internal class DefaultSelectorProperties(
  private val enabled: Boolean,
  private val shape: Shape,
  private val color: Color,
  private val border: BorderStroke?
) : SelectorProperties {

  @Composable
  override fun enabled(): State<Boolean> {
    return rememberUpdatedState(enabled)
  }

  @Composable
  override fun shape(): State<Shape> {
    return rememberUpdatedState(shape)
  }

  @Composable
  override fun color(): State<Color> {
    return rememberUpdatedState(color)
  }

  @Composable
  override fun border(): State<BorderStroke?> {
    return rememberUpdatedState(border)
  }
}
