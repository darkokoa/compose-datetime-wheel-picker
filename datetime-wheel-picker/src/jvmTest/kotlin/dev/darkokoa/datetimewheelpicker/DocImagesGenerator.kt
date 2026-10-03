package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.WheelRows
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

/**
 * Renders the README images off-screen. Does nothing unless the `DOC_IMAGES_DIR` environment
 * variable names an output directory, so it is inert during `./gradlew check`:
 *
 * ```
 * DOC_IMAGES_DIR=$PWD/docs/images ./gradlew :datetime-wheel-picker:jvmTest --tests '*DocImagesGenerator*'
 * ```
 */
@OptIn(ExperimentalTestApi::class)
class DocImagesGenerator {

  private val outputDir: File? = System.getenv("DOC_IMAGES_DIR")?.let(::File)

  private val date = LocalDate(2026, 10, 20)
  private val time = LocalTime(9, 41)
  private val dateTime = LocalDateTime(2026, 10, 20, 9, 41)

  @Test
  fun hero() = renderBoth("hero") {
    val dateFormatter = dateFormatter(Locale("en-US"), MonthDisplayStyle.SHORT, CjkSuffixConfig.HideAll)
    val timeFormatter = timeFormatter(Locale("en-US"))
    Labeled("WheelDateTimePicker") {
      WheelDateTimePicker(
        startDateTime = dateTime,
        dateFormatter = dateFormatter,
        timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24),
        rows = WheelRows.Count(5),
        modifier = Modifier.size(280.dp, 213.dp),
      )
    }
    Labeled("WheelDatePicker · iOS style") {
      WheelDatePicker(
        startDate = date,
        dateFormatter = dateFormatter,
        rows = WheelRows.Height(32.dp),
        barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 90f),
        modifier = Modifier.size(256.dp, 213.dp),
      )
    }
    Labeled("WheelTimePicker · AM/PM") {
      WheelTimePicker(
        startTime = time,
        timeFormatter = timeFormatter,
        rows = WheelRows.Count(5),
        modifier = Modifier.size(160.dp, 213.dp),
      )
    }
  }

  @Test
  fun rimAngle() = renderBoth("rim-angle") {
    val timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24)
    listOf(
      "rimAngle = 0°" to WheelPickerDefaults.barrelProperties(rimAngle = 0f),
      "rimAngle = 45°" to WheelPickerDefaults.barrelProperties(rimAngle = 45f),
      "rimAngle = 90°" to WheelPickerDefaults.barrelProperties(rimAngle = 90f),
      "default (70° at 7 rows)" to WheelPickerDefaults.barrelPropertiesFor(WheelRows.Count(7)),
    ).forEach { (label, barrel) ->
      Labeled(label) {
        WheelTimePicker(
          startTime = time,
          timeFormatter = timeFormatter,
          rows = WheelRows.Count(7),
          barrelProperties = barrel,
          modifier = Modifier.size(128.dp, 224.dp),
        )
      }
    }
  }

  @Test
  fun rows() = renderBoth("rows") {
    val timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24)
    listOf(
      "Count(3) · default" to WheelRows.Count(3),
      "Count(5)" to WheelRows.Count(5),
      "Height(32.dp)" to WheelRows.Height(32.dp),
    ).forEach { (label, rows) ->
      Labeled(label) {
        WheelTimePicker(
          startTime = time,
          timeFormatter = timeFormatter,
          rows = rows,
          modifier = Modifier.size(128.dp, 224.dp),
        )
      }
    }
  }

  private fun renderBoth(name: String, content: @Composable () -> Unit) {
    val dir = outputDir ?: return
    dir.mkdirs()
    for (dark in listOf(false, true)) {
      val file = File(dir, "$name-${if (dark) "dark" else "light"}.png")
      runDesktopComposeUiTest(width = 2400, height = 1200) {
        setContent {
          CompositionLocalProvider(LocalDensity provides Density(2f)) {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
              Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.testTag("scene"),
              ) {
                Row(
                  modifier = Modifier.padding(24.dp),
                  horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) { content() }
              }
            }
          }
        }
        waitForIdle()
        val image = onNodeWithTag("scene").captureToImage().toAwtImage()
        check(ImageIO.write(image, "png", file)) { "No PNG writer available" }
      }
      println("Wrote $file")
    }
  }

  @Composable
  private fun Labeled(label: String, picker: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      picker()
      Spacer(Modifier.height(12.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
