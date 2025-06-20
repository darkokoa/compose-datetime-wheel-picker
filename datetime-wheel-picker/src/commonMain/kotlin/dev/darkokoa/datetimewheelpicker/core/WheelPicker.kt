package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WheelPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  count: Int,
  rowCount: Int,
  size: DpSize = DpSize(128.dp, 128.dp),
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
  content: @Composable LazyItemScope.(index: Int) -> Unit,
) {
  val lazyListState = rememberLazyListState(startIndex)
  val flingBehavior = rememberSnapFlingBehavior(lazyListState)
  val isScrollInProgress = lazyListState.isScrollInProgress
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(isScrollInProgress, count) {
    if (!isScrollInProgress) {
      onScrollFinished(calculateSnappedItemIndex(lazyListState))?.let {
        lazyListState.scrollToItem(it)
      }
    }
  }

  Box(
    modifier = modifier
      .pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Scroll) {
              val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
              
              // Normalize scroll delta for consistent behavior across platforms
              val normalizedDelta = normalizeScrollDelta(scrollDelta)
              
              if (normalizedDelta != 0f && !lazyListState.isScrollInProgress) {
                coroutineScope.launch {
                  // Calculate target item index based on scroll direction
                  val currentIndex = calculateSnappedItemIndex(lazyListState)
                  val targetIndex = if (normalizedDelta > 0) {
                    (currentIndex + 1).coerceAtMost(count - 1)
                  } else {
                    (currentIndex - 1).coerceAtLeast(0)
                  }
                  
                  if (targetIndex != currentIndex) {
                    lazyListState.animateScrollToItem(targetIndex)
                  }
                }
              }
              
              // Mark scroll event as consumed to prevent it from reaching LazyColumn
              event.changes.forEach { it.consume() }
            }
          }
        }
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragEnd = {
            // Snap to nearest item after drag ends
            coroutineScope.launch {
              val currentIndex = calculateSnappedItemIndex(lazyListState)
              lazyListState.animateScrollToItem(currentIndex)
            }
          }
        ) { change, dragAmount ->
          coroutineScope.launch {
            // Handle touch drag by directly manipulating the scroll position
            try {
              lazyListState.scrollBy(-dragAmount.y)
            } catch (_: Exception) {
              // Handle any scroll exceptions gracefully
            }
          }
        }
      },
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
      flingBehavior = flingBehavior,
      userScrollEnabled = false
    ) {
      items(count) { index ->
        val (newAlpha, newRotationX) = calculateAnimatedAlphaAndRotationX(
          lazyListState = lazyListState,
          index = index,
          rowCount = rowCount
        )

        Box(
          modifier = Modifier
            .height(size.height / rowCount)
            .width(size.width)
            .alpha(newAlpha)
            .graphicsLayer {
              rotationX = newRotationX
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

@Composable
private fun calculateAnimatedAlphaAndRotationX(
  lazyListState: LazyListState,
  index: Int,
  rowCount: Int
): Pair<Float, Float> {

  val layoutInfo = remember { derivedStateOf { lazyListState.layoutInfo } }.value
  val viewPortHeight = layoutInfo.viewportSize.height.toFloat()
  val singleViewPortHeight = viewPortHeight / rowCount

  val centerIndex = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }.value
  val centerIndexOffset = remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }.value

  val distanceToCenterIndex = index - centerIndex

  val distanceToIndexSnap = distanceToCenterIndex * singleViewPortHeight.toInt() - centerIndexOffset
  val distanceToIndexSnapAbs = abs(distanceToIndexSnap)

  val animatedAlpha = if (abs(distanceToIndexSnap) in 0..singleViewPortHeight.toInt()) {
    1.2f - (distanceToIndexSnapAbs / singleViewPortHeight)
  } else {
    0.2f
  }

  val animatedRotationX = (-20 * (distanceToIndexSnap / singleViewPortHeight)).takeUnless { it.isNaN() } ?: 0f

  return animatedAlpha to animatedRotationX
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

/**
 * Normalizes scroll delta values for consistent behavior across platforms.
 * JS/WASM platforms typically have much smaller delta values compared to desktop.
 */
private fun normalizeScrollDelta(delta: Float): Float {
  return when {
    // For very small deltas (typical of JS/WASM), amplify them
    abs(delta) < 0.1f && delta != 0f -> sign(delta) * 1f
    // For medium deltas, reduce amplification
    abs(delta) < 1f -> delta * 2f
    // For large deltas (desktop-like), use as is but cap at reasonable values
    abs(delta) > 10f -> sign(delta) * 1f
    else -> delta
  }
}


