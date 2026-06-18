package dev.darkokoa.datetimewheelpicker.core

import dev.darkokoa.datetimewheelpicker.TimePickerSnap
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimePickerWheelIndexTest {

  @Test
  fun settledTimeWheelIndex_preservesPeriodWheelIndex() {
    val periodSnap = TimePickerSnap(
      snappedTime = SnappedTime.Hour(localTime = LocalTime(15, 30), index = 1),
      index = 1,
    )

    assertEquals(
      expected = 1,
      actual = settledTimeWheelIndex(
        field = TimePickerField.Period,
        snap = periodSnap,
        eventSinkIndex = 2,
      ),
    )
  }

  @Test
  fun settledTimeWheelIndex_allowsHourAndMinuteCorrection() {
    val hourSnap = TimePickerSnap(
      snappedTime = SnappedTime.Hour(localTime = LocalTime(15, 30), index = 2),
      index = 2,
    )

    assertEquals(
      expected = 3,
      actual = settledTimeWheelIndex(
        field = TimePickerField.Hour,
        snap = hourSnap,
        eventSinkIndex = 3,
      ),
    )
  }
}
