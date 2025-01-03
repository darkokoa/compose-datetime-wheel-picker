package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.darkokoa.datetimewheelpicker.core.MonthRepresentation
import dev.darkokoa.datetimewheelpicker.core.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun App() = AppTheme {
  var monthRepresentation by remember {
    mutableStateOf<MonthRepresentation>(MonthRepresentation.Default)
  }
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
      MonthRepresentationSelector(
        modifier = Modifier
          .fillMaxWidth(),
        selectedMonthRepresentation = monthRepresentation,
        onClicked = {
          monthRepresentation = it
        }
      )
      WheelDatePicker(
        monthRepresentation = monthRepresentation,
      ) { snappedDate ->
        println(snappedDate)
      }
      WheelDateTimePicker(
        monthRepresentation = monthRepresentation,
      ) { snappedDateTime ->
        println(snappedDateTime)
      }
      WheelDateTimePicker(
        startDateTime = LocalDateTime(
          2025, 10, 20, 5, 30
        ),
        minDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        maxDateTime = LocalDateTime(
          2025, 10, 20, 5, 30
        ),
        timeFormat = TimeFormat.AM_PM,
        size = DpSize(200.dp, 100.dp),
        rowCount = 5,
        textStyle = MaterialTheme.typography.titleSmall,
        textColor = Color(0xFFffc300),
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = true,
          shape = RoundedCornerShape(0.dp),
          color = Color(0xFFf1faee).copy(alpha = 0.2f),
          border = BorderStroke(2.dp, Color(0xFFf1faee))
        ),
        monthRepresentation = monthRepresentation
      ) { snappedDateTime ->
        println(snappedDateTime)
      }
    }
  }
}

@Composable
internal fun MonthRepresentationSelector(
  modifier: Modifier = Modifier,
  selectedMonthRepresentation: MonthRepresentation,
  onClicked: (MonthRepresentation) -> Unit
) {
  val monthRepresentations = listOf(
    MonthRepresentation.Default,
    MonthRepresentation.FullName,
    MonthRepresentation.ShortName,
    MonthRepresentation.MonthNumber,
    MonthRepresentation.Custom(
      mapper = { monthIndex: Int ->
        "Mon-${monthIndex.plus(1)}"
      }
    )
  )
  LazyRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    items(monthRepresentations) { item ->
      MonthRepresentationSelectorItem(
        monthRepresentation = item,
        onClicked = onClicked,
        isSelected = item == selectedMonthRepresentation
      )
    }
  }
}

@Composable
internal fun MonthRepresentationSelectorItem(
  monthRepresentation: MonthRepresentation,
  isSelected: Boolean,
  onClicked: (MonthRepresentation) -> Unit
) {
  Surface(
    color = Color(0xFFf1faee).copy(alpha = 0.2f),
    onClick = {
      onClicked(monthRepresentation)
    },
    shape = CircleShape
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 20.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(15.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = monthRepresentation::class.simpleName.orEmpty(),
        color = Color.White,
        fontSize = 13.sp
      )

      if(isSelected){
        Icon(
          imageVector = Icons.Outlined.CheckCircle,
          contentDescription = null,
          tint = Color.White
        )
      }
    }
  }
}