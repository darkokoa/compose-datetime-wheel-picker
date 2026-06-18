package dev.darkokoa.datetimewheelpicker

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Suppress("unused")
private class PickerApiCompileTest {

  @Composable
  fun datePickerStateBasedApiCompiles() {
    val state = rememberWheelDatePickerState(
      initialSelectedDate = LocalDate(2025, 6, 18),
    )

    WheelDatePicker(state = state)
  }

  @Composable
  fun datePickerValueBasedApiCompiles() {
    WheelDatePicker(
      selectedDate = LocalDate(2025, 6, 18),
      onDateChange = { _: LocalDate -> },
    )
  }

  @Composable
  fun timePickerStateBasedApiCompiles() {
    val state = rememberWheelTimePickerState(
      initialSelectedTime = LocalTime(9, 30),
    )

    WheelTimePicker(state = state)
  }

  @Composable
  fun timePickerValueBasedApiCompiles() {
    WheelTimePicker(
      selectedTime = LocalTime(9, 30),
      onTimeChange = { _: LocalTime -> },
    )
  }

  @Composable
  fun dateTimePickerStateBasedApiCompiles() {
    val state = rememberWheelDateTimePickerState(
      initialSelectedDateTime = LocalDateTime(2025, 6, 18, 9, 30),
    )

    WheelDateTimePicker(state = state)
  }

  @Composable
  fun dateTimePickerValueBasedApiCompiles() {
    WheelDateTimePicker(
      selectedDateTime = LocalDateTime(2025, 6, 18, 9, 30),
      onDateTimeChange = { _: LocalDateTime -> },
    )
  }
}
