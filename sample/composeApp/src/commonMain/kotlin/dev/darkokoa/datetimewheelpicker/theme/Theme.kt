package dev.darkokoa.datetimewheelpicker.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Every role that Material 3 components read is set explicitly. Leaving one out silently falls back
// to the stock purple M3 default, which is what made the sample look half light and half dark.
private val LightColorScheme = lightColorScheme(
  primary = LightAccent,
  onPrimary = Color.White,
  primaryContainer = LightAccentSubtle,
  onPrimaryContainer = LightAccentStrong,
  secondary = LightForegroundMuted,
  onSecondary = Color.White,
  secondaryContainer = LightAccentSubtle,
  onSecondaryContainer = LightAccentStrong,
  tertiary = LightAttention,
  onTertiary = Color.White,
  background = LightCanvas,
  onBackground = LightForeground,
  surface = LightCanvas,
  onSurface = LightForeground,
  surfaceVariant = LightCanvasSubtle,
  onSurfaceVariant = LightForegroundMuted,
  surfaceTint = LightAccent,
  inverseSurface = DarkCanvas,
  inverseOnSurface = DarkForeground,
  inversePrimary = DarkAccent,
  error = LightDanger,
  onError = Color.White,
  outline = LightBorderStrong,
  outlineVariant = LightBorder,
  scrim = Color.Black,
  surfaceBright = LightCanvas,
  surfaceDim = LightContainerHigh,
  surfaceContainerLowest = LightCanvas,
  surfaceContainerLow = LightCanvasSubtle,
  surfaceContainer = LightContainer,
  surfaceContainerHigh = LightContainerHigh,
  surfaceContainerHighest = LightContainerHighest,
)

private val DarkColorScheme = darkColorScheme(
  primary = DarkAccent,
  onPrimary = DarkCanvasInset,
  primaryContainer = DarkAccentSubtle,
  onPrimaryContainer = DarkAccentStrong,
  secondary = DarkForegroundMuted,
  onSecondary = DarkCanvasInset,
  secondaryContainer = DarkAccentSubtle,
  onSecondaryContainer = DarkAccentStrong,
  tertiary = DarkAttention,
  onTertiary = DarkCanvasInset,
  background = DarkCanvas,
  onBackground = DarkForeground,
  surface = DarkCanvas,
  onSurface = DarkForeground,
  surfaceVariant = DarkContainer,
  onSurfaceVariant = DarkForegroundMuted,
  surfaceTint = DarkAccent,
  inverseSurface = LightCanvas,
  inverseOnSurface = LightForeground,
  inversePrimary = LightAccent,
  error = DarkDanger,
  onError = DarkCanvasInset,
  outline = DarkBorderStrong,
  outlineVariant = DarkBorder,
  scrim = Color.Black,
  surfaceBright = DarkContainerHighest,
  surfaceDim = DarkCanvasInset,
  surfaceContainerLowest = DarkCanvasInset,
  surfaceContainerLow = DarkCanvas,
  surfaceContainer = DarkContainer,
  surfaceContainerHigh = DarkContainerHigh,
  surfaceContainerHighest = DarkContainerHighest,
)

private val AppShapes = Shapes(
  extraSmall = RoundedCornerShape(2.dp),
  small = RoundedCornerShape(4.dp),
  medium = RoundedCornerShape(8.dp),
  large = RoundedCornerShape(16.dp),
  extraLarge = RoundedCornerShape(32.dp)
)

private val AppTypography = Typography(
  bodyMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp
  )
)

/** The color scheme [AppTheme] applies; every role is set explicitly, see the note above. */
internal fun appColorScheme(darkTheme: Boolean): ColorScheme =
  if (darkTheme) DarkColorScheme else LightColorScheme

/**
 * Applies the sample's Material 3 theme. [darkTheme] is owned by the caller (see `App`) so it can
 * follow the system setting and still be overridden by an in-app toggle.
 */
@Composable
internal fun AppTheme(
  darkTheme: Boolean,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = appColorScheme(darkTheme),
    typography = AppTypography,
    shapes = AppShapes,
    content = {
      // Scaffold paints `background` and a bare Surface defaults to `surface`; both schemes keep the
      // two equal (see ThemeTest), so the root and the page never disagree.
      Surface(
        color = MaterialTheme.colorScheme.background,
        content = content,
      )
    }
  )
}
