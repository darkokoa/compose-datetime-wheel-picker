package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import dev.darkokoa.datetimewheelpicker.theme.AppTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun App() = AppTheme {
  Surface(
    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      TimePickerChip()
      StateBasedTimePicker()
      ValueBasedTimePicker()
      ValueBasedDatePicker()
      CustomizedDateTimePicker()
    }
  }
}

@Composable
private fun StateBasedTimePicker() {
  val timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24)
  val state = rememberWheelTimePickerState()
  val scope = rememberCoroutineScope()

  Row(verticalAlignment = Alignment.CenterVertically) {
    Text("State time: ${state.selectedTime}")
    TextButton(
      onClick = {
        scope.launch {
          state.animateScrollToTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time)
        }
      }
    ) {
      Text("Now")
    }
  }

  WheelTimePicker(
    state = state,
    timeFormatter = timeFormatter
  )
}

@Composable
private fun ValueBasedTimePicker() {
  var selectedTime by remember { mutableStateOf(LocalTime(9, 30)) }

  Text("Value time: $selectedTime")
  WheelTimePicker(
    selectedTime = selectedTime,
    onTimeChange = { selectedTime = it }
  )
}

@Composable
private fun ValueBasedDatePicker() {
  var selectedDate by remember { mutableStateOf(LocalDate(2025, 6, 18)) }

  Text("Value date: $selectedDate")
  WheelDatePicker(
    selectedDate = selectedDate,
    onDateChange = { selectedDate = it },
  )
}

@Composable
private fun CustomizedDateTimePicker() {
  val timeFormatter = timeFormatter(timeFormat = TimeFormat.AM_PM)
  val minDateTime = remember {
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
  }
  val state = rememberWheelDateTimePickerState(
    initialSelectedDateTime = LocalDateTime(2025, 10, 20, 5, 30),
    minDateTime = minDateTime,
    maxDateTime = LocalDateTime(2027, 10, 20, 5, 30),
  )

  WheelDateTimePicker(
    state = state,
    timeFormatter = timeFormatter,
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
  )
}

@Composable
private fun TimePickerChip() {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  var selectedTimeStr by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedTime = selectedTimeStr?.let { LocalTime.parse(it) }

  Button(onClick = { showDialog = true }) {
    Text(selectedTime?.let {"Selected time: $it"} ?: "Select time")
  }

  if (showDialog) {
    TimePickerDialog(
      initialTime = selectedTime ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
      onConfirm = { time ->
        selectedTimeStr = time.toString()
        showDialog = false
      },
      onDismiss = { showDialog = false }
    )
  }
}

@Composable
private fun TimePickerDialog(
  initialTime: LocalTime,
  onConfirm: (LocalTime) -> Unit,
  onDismiss: () -> Unit,
) {
  val state = rememberWheelTimePickerState(initialSelectedTime = initialTime)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select time") },
    text = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        WheelTimePicker(
          state = state,
        )
      }
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(state.selectedTime) }) {
        Text("OK")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
