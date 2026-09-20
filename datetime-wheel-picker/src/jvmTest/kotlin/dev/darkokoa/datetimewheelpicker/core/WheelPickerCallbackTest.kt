package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Contract tests for [WheelPicker] callback timing, guarding against the regression in
 * https://github.com/darkokoa/compose-datetime-wheel-picker/issues/71.
 */
@OptIn(ExperimentalTestApi::class)
class WheelPickerCallbackTest {

  // viewportSize height 300.dp with 3 rows and a flat wheel => each item is exactly 100.dp tall.
  private val viewportSize = DpSize(120.dp, 300.dp)
  private val itemHeight = 100.dp

  @Test
  fun firstCompositionDoesNotFireCallbacks() = runComposeUiTest {
    val changed = mutableListOf<Int>()
    var finishedCount = 0
    setContent {
      WheelPicker(
        count = 10,
        rows = WheelRows.Count(3),
        startIndex = 5,
        viewportSize = viewportSize,
        onScrollChanged = { changed += it },
        onScrollFinished = { finishedCount++; null },
      ) { index, _ -> Text("item-$index") }
    }
    waitForIdle()
    assertTrue(changed.isEmpty(), "onScrollChanged must not fire on first composition, but got $changed")
    assertEquals(0, finishedCount, "onScrollFinished must not fire on first composition")
    onNodeWithText("item-5").assertIsDisplayed()
  }

  @Test
  fun swipingOneItemFiresEachCallbackExactlyOnce() = runComposeUiTest {
    val changed = mutableListOf<Int>()
    val finished = mutableListOf<Int>()
    setContent {
      WheelPicker(
        modifier = Modifier.testTag("wheel"),
        count = 10,
        rows = WheelRows.Count(3),
        startIndex = 5,
        viewportSize = viewportSize,
        onScrollChanged = { changed += it },
        onScrollFinished = { finished += it; null },
      ) { index, _ -> Text("item-$index") }
    }
    onNodeWithTag("wheel").performTouchInput { swipeUpOneItem() }
    waitForIdle()
    assertEquals(listOf(6), changed, "onScrollChanged must fire exactly once with the new index")
    assertEquals(listOf(6), finished, "onScrollFinished must fire exactly once with the new index")
    onNodeWithText("item-6").assertIsDisplayed()
  }

  @Test
  fun correctionReturnedFromOnScrollFinishedDoesNotFireSecondCallback() = runComposeUiTest {
    val finished = mutableListOf<Int>()
    setContent {
      WheelPicker(
        modifier = Modifier.testTag("wheel"),
        count = 31,
        rows = WheelRows.Count(3),
        startIndex = 29,
        viewportSize = viewportSize,
        // Simulates a date picker correction, e.g. Jan 31 -> Feb snapping back to day 28.
        onScrollFinished = { finished += it; 27 },
      ) { index, _ -> Text("item-$index") }
    }
    onNodeWithTag("wheel").performTouchInput { swipeUpOneItem() }
    waitForIdle()
    assertEquals(listOf(30), finished, "the corrective scrollToItem must not re-fire onScrollFinished")
    onNodeWithText("item-27").assertIsDisplayed()
  }

  @Test
  fun correctionEqualToSnappedIndexKeepsWheelInPlace() = runComposeUiTest {
    val finished = mutableListOf<Int>()
    setContent {
      WheelPicker(
        modifier = Modifier.testTag("wheel"),
        count = 10,
        rows = WheelRows.Count(3),
        startIndex = 5,
        viewportSize = viewportSize,
        onScrollFinished = { finished += it; it },
      ) { index, _ -> Text("item-$index") }
    }
    onNodeWithTag("wheel").performTouchInput { swipeUpOneItem() }
    waitForIdle()
    assertEquals(listOf(6), finished)
    onNodeWithText("item-6").assertIsDisplayed()
  }

  @Test
  fun barrelProjectionKeepsSnappingAndCallbacksIntact() = runComposeUiTest {
    val changed = mutableListOf<Int>()
    val finished = mutableListOf<Int>()
    setContent {
      WheelPicker(
        modifier = Modifier.testTag("wheel"),
        count = 10,
        rows = WheelRows.Count(3),
        startIndex = 5,
        viewportSize = viewportSize,
        barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 70f),
        onScrollChanged = { changed += it },
        onScrollFinished = { finished += it; null },
      ) { index, _ -> Text("item-$index") }
    }
    onNodeWithText("item-5").assertIsDisplayed()
    onNodeWithTag("wheel").performTouchInput { swipeUpOneItem() }
    waitForIdle()
    assertEquals(listOf(6), changed, "barrel projection must not alter onScrollChanged")
    assertEquals(listOf(6), finished, "barrel projection must not alter onScrollFinished")
    onNodeWithText("item-6").assertIsDisplayed()
  }

  @Test
  fun barrelProjectionForeshortensRowsTowardTheRim() = runComposeUiTest {
    setContent {
      WheelPicker(
        count = 10,
        rows = WheelRows.Count(3),
        startIndex = 5,
        viewportSize = viewportSize,
        barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 70f),
      ) { index, _ -> Text("item-$index") }
    }
    waitForIdle()
    val center = onNodeWithText("item-5").getBoundsInRoot()
    val above = onNodeWithText("item-4").getBoundsInRoot()
    val below = onNodeWithText("item-6").getBoundsInRoot()

    // The centered row is untouched; its neighbours are tilted and scaled, so they come out
    // shorter on screen and stay within the viewport.
    assertTrue(above.height < center.height, "row above should be foreshortened: $above vs $center")
    assertTrue(below.height < center.height, "row below should be foreshortened: $below vs $center")
    assertEquals(above.height.value, below.height.value, absoluteTolerance = 0.01f, "projection must be symmetric: $above vs $below")
    assertTrue(above.top >= 0.dp && below.bottom <= viewportSize.height, "rows must stay on the drum")
  }

  @Test
  fun heightRowsSnapByRowHeightNotViewport() = runComposeUiTest {
    val changed = mutableListOf<Int>()
    val finished = mutableListOf<Int>()
    setContent {
      WheelPicker(
        modifier = Modifier.testTag("wheel"),
        count = 10,
        // 100.dp rows in a 300.dp flat viewport: geometrically identical to Count(3) at 0°.
        rows = WheelRows.Height(itemHeight),
        startIndex = 5,
        viewportSize = viewportSize,
        barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 0f),
        onScrollChanged = { changed += it },
        onScrollFinished = { finished += it; null },
      ) { index, _ -> Text("item-$index") }
    }
    onNodeWithText("item-5").assertIsDisplayed()
    onNodeWithTag("wheel").performTouchInput { swipeUpOneItem() }
    waitForIdle()
    assertEquals(listOf(6), changed)
    assertEquals(listOf(6), finished)
    onNodeWithText("item-6").assertIsDisplayed()
  }

  /**
   * Swipes up by exactly one item height, slowly enough that the snap fling settles on the
   * adjacent item instead of flinging across several items.
   */
  private fun TouchInjectionScope.swipeUpOneItem() {
    swipe(
      start = center,
      end = center - Offset(0f, itemHeight.toPx()),
      durationMillis = 1000
    )
  }
}
