package dev.darkokoa.datetimewheelpicker

import dev.darkokoa.datetimewheelpicker.core.MAX
import dev.darkokoa.datetimewheelpicker.core.MIN
import dev.darkokoa.datetimewheelpicker.core.SnappedDate
import dev.darkokoa.datetimewheelpicker.core.SnappedTime
import dev.darkokoa.datetimewheelpicker.core.TimePickerField
import dev.darkokoa.datetimewheelpicker.core.WheelPickerState
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import kotlin.test.Test
import kotlin.test.assertEquals

class WheelDateTimePickerStateTest {

  @Test
  fun settleDateTime_returnsCoercedTimeIndexWhenBoundaryClampsTime() {
    val min = LocalDateTime(2025, 6, 18, 9, 30)
    val max = LocalDateTime(2025, 6, 18, 17, 45)
    val state = wheelDateTimePickerState(
      initialDateTime = min,
      minDateTime = min,
      maxDateTime = max,
    )

    val snap = state.settleDateTime(
      snappedTime = SnappedTime.Hour(localTime = LocalTime(8, 30), index = 8),
      timeFormat = TimeFormat.HOUR_24,
    )

    assertEquals(min, snap.snappedDateTime.snappedLocalDateTime)
    assertEquals(9, snap.index)
  }

  @Test
  fun settleDateTime_synchronizesChildStateWhenBoundaryClampsSiblingWheel() {
    val min = LocalDateTime(2025, 6, 18, 9, 30)
    val max = LocalDateTime(2025, 6, 20, 17, 45)
    val state = wheelDateTimePickerState(
      initialDateTime = LocalDateTime(2025, 6, 19, 8, 0),
      minDateTime = min,
      maxDateTime = max,
    )

    state.settleDateTime(SnappedDate.DayOfMonth(localDate = min.date, index = min.date.day - 1))

    assertEquals(min, state.selectedDateTime)
    assertEquals(min.date, state.dateState.displayedDate)
    assertEquals(min.date, state.dateState.selectedDate)
    assertEquals(min.time, state.timeState.displayedTime)
    assertEquals(min.time, state.timeState.selectedTime)

    val timeSnap = requireNotNull(state.timeState.settleTime(
      field = TimePickerField.Minute,
      index = 45,
      timeFormat = TimeFormat.HOUR_24,
    ))

    val dateTimeSnap = state.settleDateTime(
      snappedTime = timeSnap.snappedTime,
      timeFormat = TimeFormat.HOUR_24,
    )

    assertEquals(LocalDateTime(2025, 6, 18, 9, 45), dateTimeSnap.snappedDateTime.snappedLocalDateTime)
    assertEquals(45, dateTimeSnap.index)
  }

  private fun wheelDateTimePickerState(
    initialDateTime: LocalDateTime,
    minDateTime: LocalDateTime,
    maxDateTime: LocalDateTime,
  ): WheelDateTimePickerState {
    val yearsRange = minDateTime.year..maxDateTime.year
    return WheelDateTimePickerState(
      initialSelectedDateTime = initialDateTime,
      initialDisplayedDateTime = initialDateTime,
      minDateTime = minDateTime,
      maxDateTime = maxDateTime,
      yearsRange = yearsRange,
      dateState = wheelDatePickerState(
        initialDate = initialDateTime.date,
        minDate = minDateTime.date,
        maxDate = maxDateTime.date,
        yearsRange = yearsRange,
      ),
      timeState = wheelTimePickerState(initialDateTime.time),
    )
  }

  private fun wheelDatePickerState(
    initialDate: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
    yearsRange: IntRange,
  ): WheelDatePickerState {
    return WheelDatePickerState(
      initialSelectedDate = initialDate,
      initialDisplayedDate = initialDate,
      minDate = minDate,
      maxDate = maxDate,
      yearsRange = yearsRange,
      dayWheelState = WheelPickerState(initialDate.day - 1),
      monthWheelState = WheelPickerState(initialDate.month.number - 1),
      yearWheelState = WheelPickerState(initialDate.year - yearsRange.first),
    )
  }

  private fun wheelTimePickerState(initialTime: LocalTime): WheelTimePickerState {
    return WheelTimePickerState(
      initialSelectedTime = initialTime,
      initialDisplayedTime = initialTime,
      minTime = LocalTime.MIN,
      maxTime = LocalTime.MAX,
      hour24WheelState = WheelPickerState(initialTime.hour),
      amPmHourWheelState = WheelPickerState(0),
      minuteWheelState = WheelPickerState(initialTime.minute),
      periodWheelState = WheelPickerState(0),
    )
  }
}
