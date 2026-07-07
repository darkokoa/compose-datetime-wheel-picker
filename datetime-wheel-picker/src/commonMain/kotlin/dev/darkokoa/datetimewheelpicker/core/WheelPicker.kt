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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
internal fun WheelPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  count: Int,
  rowCount: Int,
  size: DpSize = DpSize(128.dp, 128.dp),
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollChanged: (snappedIndex: Int) -> Unit = {},
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
  content: @Composable LazyItemScope.(index: Int) -> Unit,
) {
  require(rowCount > 0) { "rowCount must be positive, was $rowCount" }
  require(size.height.isFinite && size.height > 0.dp) {
    "size.height must be finite and positive, was ${size.height}"
  }
  val lazyListState = rememberLazyListState(startIndex)
  val flingBehavior = rememberSnapFlingBehavior(lazyListState)
  val latestOnScrollChanged by rememberUpdatedState(onScrollChanged)
  val latestOnScrollFinished by rememberUpdatedState(onScrollFinished)
  val density = LocalDensity.current
  val singleViewPortHeightPx = remember(size, rowCount, density) {
    with(density) { size.height.toPx() } / rowCount
  }

  LaunchedEffect(lazyListState, count) {
    snapshotFlow { calculateSnappedItemIndex(lazyListState) }
      .distinctUntilChanged()
      .collect { latestOnScrollChanged(it) }
  }

  LaunchedEffect(lazyListState, count) {
    snapshotFlow { lazyListState.isScrollInProgress }
      .filter { !it }
      .collect {
        latestOnScrollFinished(calculateSnappedItemIndex(lazyListState))
          ?.let { lazyListState.scrollToItem(it) }
      }
  }

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    if (selectorProperties.enabled().value) {
      Surface(
        modifier = Modifier
          .size(size.width, size.height / rowCount),
        shape = selectorProperties.shape().value,
        color = selectorProperties.color().value,
        border = selectorProperties.border().value
      ) {}
    }
    LazyColumn(
      modifier = Modifier
        .height(size.height)
        .width(size.width),
      state = lazyListState,
      contentPadding = PaddingValues(vertical = size.height / rowCount * ((rowCount - 1) / 2)),
      flingBehavior = flingBehavior
    ) {
      items(count) { index ->
        Box(
          modifier = Modifier
            .height(size.height / rowCount)
            .width(size.width)
            .graphicsLayer {
              val centerIndex = lazyListState.firstVisibleItemIndex
              val centerIndexOffset = lazyListState.firstVisibleItemScrollOffset
              val distanceToCenterIndex = index - centerIndex
              val distanceToIndexSnap = distanceToCenterIndex * singleViewPortHeightPx - centerIndexOffset
              val distanceToIndexSnapAbs = abs(distanceToIndexSnap)
              alpha = if (distanceToIndexSnapAbs <= singleViewPortHeightPx)
                1.2f - (distanceToIndexSnapAbs / singleViewPortHeightPx) else 0.2f
              rotationX = -20f * (distanceToIndexSnap / singleViewPortHeightPx)
            },
          contentAlignment = Alignment.Center
        ) {
          content(index)
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
