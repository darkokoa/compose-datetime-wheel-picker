package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

@Composable
internal fun WheelPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  count: Int,
  rows: WheelRows,
  viewportSize: DpSize = DpSize(128.dp, 128.dp),
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  barrelProperties: BarrelProperties = WheelPickerDefaults.barrelPropertiesFor(rows),
  onScrollChanged: (snappedIndex: Int) -> Unit = {},
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
  content: @Composable LazyItemScope.(index: Int, isSelected: Boolean) -> Unit,
) {
  require(viewportSize.height.isFinite && viewportSize.height > 0.dp) {
    "viewportSize.height must be finite and positive, was ${viewportSize.height}"
  }
  val lazyListState = rememberLazyListState(startIndex)
  val flingBehavior = rememberSnapFlingBehavior(lazyListState)
  val latestOnScrollChanged by rememberUpdatedState(onScrollChanged)
  val latestOnScrollFinished by rememberUpdatedState(onScrollFinished)
  val density = LocalDensity.current
  val viewportHeightPx = remember(viewportSize, density) {
    with(density) { viewportSize.height.toPx() }
  }
  // The flat list is the unrolled surface of the drum: longer than the viewport, with each row
  // taking the arc length it occupies on the cylinder. The projection folds it back into the
  // viewport and the clip discards whatever is left over.
  val rowHeight = rows.rowHeight(viewportSize.height, barrelProperties)
  val listHeight = barrelProperties.listHeight(viewportSize.height)
  val singleViewPortHeightPx = remember(rowHeight, density) {
    with(density) { rowHeight.toPx() }
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
        // A finished drag may be immediately followed by a snap fling. Wait one frame so this
        // drag-to-fling handoff gap is not misreported as a finished scroll, which would fire
        // onScrollFinished twice for a single gesture.
        withFrameNanos { }
        if (lazyListState.isScrollInProgress) return@collect
        val snappedIndex = calculateSnappedItemIndex(lazyListState)
        latestOnScrollFinished(snappedIndex)
          ?.takeIf { it != snappedIndex }
          ?.let { lazyListState.scrollToItem(it) }
      }
  }

  // A Height row taller than the whole drum still needs a non-negative padding to center on.
  val contentPadding = ((listHeight - rowHeight) / 2).coerceAtLeast(0.dp)

  Box(
    modifier = modifier.size(viewportSize).clipToBounds(),
    contentAlignment = Alignment.Center
  ) {
    WheelSelector(width = viewportSize.width, height = rowHeight, properties = selectorProperties)
    LazyColumn(
      modifier = Modifier
        .requiredHeight(listHeight)
        .width(viewportSize.width),
      state = lazyListState,
      contentPadding = PaddingValues(vertical = contentPadding),
      flingBehavior = flingBehavior
    ) {
      items(count) { index ->
        val isSelected by remember(snappedItemIndexState, index) {
          derivedStateOf { index == snappedItemIndexState.value }
        }
        Box(
          modifier = Modifier
            .height(rowHeight)
            .width(viewportSize.width)
            .graphicsLayer {
              val centerIndex = lazyListState.firstVisibleItemIndex
              val centerIndexOffset = lazyListState.firstVisibleItemScrollOffset
              val distanceToCenterIndex = index - centerIndex
              val distanceToIndexSnap = distanceToCenterIndex * singleViewPortHeightPx - centerIndexOffset
              calculateBarrelTransform(
                distanceToCenterPx = distanceToIndexSnap,
                viewportHeightPx = viewportHeightPx,
                rimAngle = barrelProperties.rimAngle,
                fadeStrength = barrelProperties.fadeStrength,
              ).applyTo(this)
            },
          contentAlignment = Alignment.Center
        ) {
          content(index, isSelected)
        }
      }
    }
  }
}

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
   * Creates a [BarrelProperties] describing the cylindrical projection of wheel rows.
   *
   * [rimAngle] is the rotation, in degrees, of the drum surface at the viewport edges, in
   * `[0, 90]`. `90` matches a native iOS picker; `0` is a flat wheel. Use [barrelPropertiesFor]
   * to let the angle follow the picker's [WheelRows] instead. [fadeStrength], in `[0, 1]`, scales
   * how much rows dim as they turn toward the rim. See [BarrelProperties].
   */
  fun barrelProperties(
    rimAngle: Float,
    fadeStrength: Float = DEFAULT_BARREL_FADE,
  ): BarrelProperties = BarrelProperties(rimAngle = rimAngle, fadeStrength = fadeStrength)

  /**
   * The [BarrelProperties] a picker uses when none is passed: a rim angle suited to [rows].
   *
   * For [WheelRows.Count], each row away from the center adds 13 degrees, so a 3-row wheel stays
   * gently curved at 26° while 7-row or taller wheels reach the 70° cap, at which point every row
   * is still readable. [WheelRows.Height] wheels do not promise a row count, so they use the full
   * 90° half cylinder of a native iOS picker.
   */
  fun barrelPropertiesFor(rows: WheelRows): BarrelProperties = when (rows) {
    is WheelRows.Count -> BarrelProperties(
      rimAngle = (AUTO_RIM_DEGREES_PER_ROW * (rows.count - 1))
        .coerceIn(AUTO_RIM_DEGREES_PER_ROW, MAX_AUTO_RIM_ANGLE),
      fadeStrength = DEFAULT_BARREL_FADE,
    )
    is WheelRows.Height -> BarrelProperties(rimAngle = 90f, fadeStrength = DEFAULT_BARREL_FADE)
  }

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
 * The highlight drawn behind the centered row of a wheel, or nothing when [properties] disables
 * it. [height] should be the picker's row height (see [BarrelProperties.rowHeight]) so the
 * highlight matches the centered row on the drum; the caller centers it in the viewport.
 */
@Composable
internal fun WheelSelector(
  width: Dp,
  height: Dp,
  properties: SelectorProperties,
) {
  if (!properties.enabled().value) return
  Surface(
    modifier = Modifier.size(width, height),
    shape = properties.shape().value,
    color = properties.color().value,
    border = properties.border().value,
  ) {}
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
