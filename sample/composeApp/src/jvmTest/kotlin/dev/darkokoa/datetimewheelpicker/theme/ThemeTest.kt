package dev.darkokoa.datetimewheelpicker.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the contract that went wrong before: the light scheme had a dark background with white
 * text, and surface roles that were never set fell back to the stock purple M3 defaults.
 */
class ThemeTest {

  @Test
  fun lightSchemeIsLightEverywhere() = assertSchemeSide(dark = false)

  @Test
  fun darkSchemeIsDarkEverywhere() = assertSchemeSide(dark = true)

  @Test
  fun lightSchemeTextIsReadable() = assertReadable(dark = false)

  @Test
  fun darkSchemeTextIsReadable() = assertReadable(dark = true)

  @Test
  fun rootBackgroundAndSurfaceAgree() {
    for (dark in listOf(false, true)) {
      val scheme = appColorScheme(dark)
      // Scaffold paints `background`, a bare Surface paints `surface`; they must not differ.
      assertEquals(scheme.background, scheme.surface, "dark=$dark")
    }
  }

  private fun assertSchemeSide(dark: Boolean) {
    val scheme = appColorScheme(dark)
    val surfaces = mapOf(
      "background" to scheme.background,
      "surface" to scheme.surface,
      "surfaceVariant" to scheme.surfaceVariant,
      "surfaceContainerLowest" to scheme.surfaceContainerLowest,
      "surfaceContainerLow" to scheme.surfaceContainerLow,
      "surfaceContainer" to scheme.surfaceContainer,
      "surfaceContainerHigh" to scheme.surfaceContainerHigh,
      "surfaceContainerHighest" to scheme.surfaceContainerHighest,
    )
    surfaces.forEach { (name, color) ->
      assertEquals(!dark, color.luminance() > 0.5f, "dark=$dark: $name is on the wrong side")
    }
  }

  private fun assertReadable(dark: Boolean) {
    val s = appColorScheme(dark)
    // Foreground / background pairs that the sample actually draws.
    val pairs = listOf(
      Triple("onBackground on background", s.onBackground, s.background),
      Triple("onSurface on surface", s.onSurface, s.surface),
      Triple("onSurface on surfaceContainerHigh (dialog)", s.onSurface, s.surfaceContainerHigh),
      Triple("onSurfaceVariant on surfaceVariant (callback card)", s.onSurfaceVariant, s.surfaceVariant),
      Triple("onSurfaceVariant on surfaceContainer (nav bar)", s.onSurfaceVariant, s.surfaceContainer),
      Triple("onSecondaryContainer on secondaryContainer (chip, nav indicator)", s.onSecondaryContainer, s.secondaryContainer),
      Triple("onPrimaryContainer on primaryContainer", s.onPrimaryContainer, s.primaryContainer),
      Triple("onPrimary on primary (button)", s.onPrimary, s.primary),
      Triple("primary on background", s.primary, s.background),
      Triple("primary on surfaceVariant (callback names)", s.primary, s.surfaceVariant),
      Triple("primary on surfaceContainerHigh (dialog buttons)", s.primary, s.surfaceContainerHigh),
      Triple("secondary on background (demo labels)", s.secondary, s.background),
      Triple("tertiary on background (custom picker)", s.tertiary, s.background),
    )
    pairs.forEach { (name, foreground, background) ->
      val ratio = contrastRatio(foreground, background)
      assertTrue(ratio >= MinTextContrast, "dark=$dark: $name has contrast $ratio, expected >= $MinTextContrast")
    }
  }

  private fun contrastRatio(a: Color, b: Color): Float {
    val lighter = maxOf(a.luminance(), b.luminance())
    val darker = minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
  }

  private companion object {
    /** WCAG AA for normal text. */
    const val MinTextContrast = 4.5f
  }
}
