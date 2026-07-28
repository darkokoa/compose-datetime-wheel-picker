package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.darkokoa.datetimewheelpicker.theme.AppTheme
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

private val TestDateTime = LocalDateTime(2026, 7, 22, 10, 30)

@OptIn(ExperimentalTestApi::class)
class AppContentTest {

  @Test
  fun selectingTabShowsItsPage() = runComposeUiTest {
    val selectedTab = mutableStateOf(DemoTab.TIME)

    setContent {
      AppTheme {
        AppContent(
          selectedTab = selectedTab.value,
          onTabSelected = { selectedTab.value = it },
          nowProvider = { TestDateTime },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }

    onNodeWithText("Default time picker").assertIsDisplayed()
    onNodeWithText("Date").performClick()

    onNodeWithText("Date").assertIsSelected()
    onNodeWithText("Default date picker").assertIsDisplayed()
    runOnIdle { assertEquals(DemoTab.DATE, selectedTab.value) }
  }

  @Test
  fun selectingDemoShowsOnlyItsContent() = runComposeUiTest {
    val selectedTab = mutableStateOf(DemoTab.TIME)

    setContent {
      AppTheme {
        AppContent(
          selectedTab = selectedTab.value,
          onTabSelected = { selectedTab.value = it },
          nowProvider = { TestDateTime },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }

    onNodeWithText("AM/PM format").assertDoesNotExist()
    onNodeWithText("AM/PM").performClick()

    onNodeWithText("AM/PM").assertIsSelected()
    onNodeWithText("AM/PM format").assertIsDisplayed()
    onNodeWithText("Default time picker").assertDoesNotExist()
  }

  @Test
  fun timeSelectionSurvivesDemoAndTabSwitches() = runComposeUiTest {
    val selectedTab = mutableStateOf(DemoTab.TIME)

    setContent {
      AppTheme {
        AppContent(
          selectedTab = selectedTab.value,
          onTabSelected = { selectedTab.value = it },
          nowProvider = { TestDateTime },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }

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
}
