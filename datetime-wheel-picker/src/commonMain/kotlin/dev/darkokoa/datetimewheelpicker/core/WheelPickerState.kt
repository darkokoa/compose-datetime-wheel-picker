package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
class WheelPickerState internal constructor(
  internal val lazyListState: LazyListState,
) {
  constructor(initialIndex: Int = 0) : this(
    lazyListState = LazyListState(firstVisibleItemIndex = initialIndex)
  )

  val snappedIndex: Int
    get() = calculateSnappedItemIndex(lazyListState)

  val isScrollInProgress: Boolean
    get() = lazyListState.isScrollInProgress

  suspend fun scrollToIndex(index: Int) {
    lazyListState.scrollToItem(index)
  }

  suspend fun animateScrollToIndex(index: Int) {
    lazyListState.animateScrollToItem(index)
  }
}

@Composable
fun rememberWheelPickerState(
  initialIndex: Int = 0,
): WheelPickerState = remember {
  WheelPickerState(initialIndex)
}
