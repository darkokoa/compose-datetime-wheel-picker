package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.darkokoa.datetimewheelpicker.theme.AppTheme
import dev.darkokoa.datetimewheelpicker.theme.appColorScheme
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val TestDateTime = LocalDateTime(2026, 7, 22, 10, 30)

private const val SwitchToDark = "Switch to dark theme"
private const val SwitchToLight = "Switch to light theme"

@OptIn(ExperimentalTestApi::class)
class AppContentTest {

  private fun ComposeUiTest.setAppContent(
    selectedTab: MutableState<DemoTab>,
    isDarkTheme: MutableState<Boolean> = mutableStateOf(false),
  ) {
    setContent {
      AppTheme(darkTheme = isDarkTheme.value) {
        AppContent(
          selectedTab = selectedTab.value,
          onTabSelected = { selectedTab.value = it },
          isDarkTheme = isDarkTheme.value,
          onToggleDarkTheme = { isDarkTheme.value = !isDarkTheme.value },
          nowProvider = { TestDateTime },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }

  /**
   * Reads the colors actually painted at the page (middle left edge) and the bottom bar, which
   * must match the scheme: the original bug painted a dark page under a light bar.
   */
  private fun ComposeUiTest.assertPaintedWith(dark: Boolean) {
    val scheme = appColorScheme(dark)
    val pixels = onRoot().captureToImage().toPixelMap()
    val x = 2
    assertEquals(scheme.background.toArgb(), pixels[x, pixels.height / 2].toArgb(), "page, dark=$dark")
    assertEquals(scheme.surface.toArgb(), pixels[x, 2].toArgb(), "top bar, dark=$dark")
    assertEquals(scheme.surfaceContainer.toArgb(), pixels[x, pixels.height - 2].toArgb(), "bottom bar, dark=$dark")
  }

  @Test
  fun selectingTabShowsItsPage() = runComposeUiTest {
    val selectedTab = mutableStateOf(DemoTab.TIME)

    setAppContent(selectedTab)

    onNodeWithText("Default time picker").assertIsDisplayed()
    onNodeWithText("Date").performClick()

    onNodeWithText("Date").assertIsSelected()
    onNodeWithText("Default date picker").assertIsDisplayed()
    runOnIdle { assertEquals(DemoTab.DATE, selectedTab.value) }
  }

  @Test
  fun selectingDemoShowsOnlyItsContent() = runComposeUiTest {
    val selectedTab = mutableStateOf(DemoTab.TIME)

    setAppContent(selectedTab)

    onNodeWithText("AM/PM format").assertDoesNotExist()
    onNodeWithText("AM/PM").performClick()

    onNodeWithText("AM/PM").assertIsSelected()
    onNodeWithText("AM/PM format").assertIsDisplayed()
    onNodeWithText("Default time picker").assertDoesNotExist()
  }

  @Test
  fun timeSelectionSurvivesDemoAndTabSwitches() = runComposeUiTest {
    val selectedTab = mutableStateOf(DemoTab.TIME)

    setAppContent(selectedTab)

    onNodeWithText("Dialog").performClick()
    onNodeWithText("Select time").performClick()
    onNodeWithText("OK").performClick()
    onNodeWithText("Selected time: 10:30").assertIsDisplayed()

    onNodeWithText("Default").performClick()
    onNodeWithText("Dialog").performClick()
    onNodeWithText("Selected time: 10:30").assertIsDisplayed()

    onNodeWithText("Date").performClick()
    onNodeWithText("Time").performClick()

    onNodeWithText("Dialog").assertIsSelected()
    onNodeWithText("Selected time: 10:30").assertIsDisplayed()
  }

  @Test
  fun themeToggleReportsClickAndNamesTheOppositeTheme() = runComposeUiTest {
    val isDarkTheme = mutableStateOf(false)

    setAppContent(mutableStateOf(DemoTab.TIME), isDarkTheme)

    onNodeWithContentDescription(SwitchToDark).performClick()
    onNodeWithContentDescription(SwitchToLight).assertIsDisplayed()
    runOnIdle { assertTrue(isDarkTheme.value) }

    onNodeWithContentDescription(SwitchToLight).performClick()
    onNodeWithContentDescription(SwitchToDark).assertIsDisplayed()
    runOnIdle { assertEquals(false, isDarkTheme.value) }
  }

  @Test
  fun lightThemePaintsPageAndBarsWithLightColors() = runComposeUiTest {
    setAppContent(mutableStateOf(DemoTab.TIME), mutableStateOf(false))

    assertPaintedWith(dark = false)
  }

  @Test
  fun darkThemePaintsPageAndBarsWithDarkColors() = runComposeUiTest {
    setAppContent(mutableStateOf(DemoTab.TIME), mutableStateOf(true))

    assertPaintedWith(dark = true)
  }

  @Test
  fun appToggleOverridesSystemThemeAndRepaints() = runComposeUiTest {
    setContent { App() }

    // The initial theme follows the host's system setting, so derive it instead of assuming one.
    val startsLight = onAllNodesWithContentDescription(SwitchToDark).fetchSemanticsNodes().isNotEmpty()
    assertPaintedWith(dark = !startsLight)

    onNodeWithContentDescription(if (startsLight) SwitchToDark else SwitchToLight).performClick()
    onNodeWithContentDescription(if (startsLight) SwitchToLight else SwitchToDark).assertIsDisplayed()
    waitForIdle()
    assertPaintedWith(dark = startsLight)

    onNodeWithContentDescription(if (startsLight) SwitchToLight else SwitchToDark).performClick()
    waitForIdle()
    assertPaintedWith(dark = !startsLight)
  }
}
