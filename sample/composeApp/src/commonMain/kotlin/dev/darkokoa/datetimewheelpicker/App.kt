package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import dev.darkokoa.datetimewheelpicker.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun App() = AppTheme {
  Surface(
    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      WheelTimePicker { snappedTime ->
        println(snappedTime)
      }
      WheelDatePicker { snappedDate ->
        println(snappedDate)
      }
      WheelDateTimePicker { snappedDateTime ->
        println(snappedDateTime)
      }
      WheelDateTimePicker(
        startDateTime = LocalDateTime(
          2025, 10, 20, 5, 30
        ),
        minDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        maxDateTime = LocalDateTime(
          2027, 10, 20, 5, 30
        ),
        timeFormatter = timeFormatter(timeFormat = TimeFormat.AM_PM),
        size = DpSize(200.dp, 100.dp),
        rowCount = 5,
        textStyle = MaterialTheme.typography.titleSmall,
        textColor = Color(0xFFffc300),
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = true,
          shape = RoundedCornerShape(0.dp),
          color = Color(0xFFf1faee).copy(alpha = 0.2f),
          border = BorderStroke(2.dp, Color(0xFFf1faee))
        )
      ) { snappedDateTime ->
        println(snappedDateTime)
      }
    }
  }
}
