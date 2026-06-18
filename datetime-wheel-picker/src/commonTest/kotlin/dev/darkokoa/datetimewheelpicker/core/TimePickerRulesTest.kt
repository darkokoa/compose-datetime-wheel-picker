package dev.darkokoa.datetimewheelpicker.core

import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimePickerRulesTest {

  @Test
  fun amPmHourFromTime_mapsMidnightNoonAndAfternoon() {
    assertEquals(12, amPmHourFromTime(LocalTime(0, 0)))
    assertEquals(12, amPmHourFromTime(LocalTime(12, 0)))
    assertEquals(1, amPmHourFromTime(LocalTime(13, 30)))
  }

  @Test
  fun timeFromIndex_updatesAmPmHourUsingCurrentPeriod() {
    val current = LocalTime(13, 30)
    val actual = timeFromIndex(current, TimePickerField.Hour, 2, TimeFormat.AM_PM)

    assertEquals(LocalTime(15, 30), actual)
  }

  @Test
  fun timeFromIndex_updatesPeriod() {
    val current = LocalTime(9, 15)
    val actual = timeFromIndex(current, TimePickerField.Period, 1, TimeFormat.AM_PM)

    assertEquals(LocalTime(21, 15), actual)
  }

  @Test
  fun timeIndex_mapsAmPmPeriod() {
    assertEquals(0, timeIndex(LocalTime(9, 15), TimePickerField.Period, TimeFormat.AM_PM))
    assertEquals(1, timeIndex(LocalTime(21, 15), TimePickerField.Period, TimeFormat.AM_PM))
    assertEquals(null, timeIndex(LocalTime(21, 15), TimePickerField.Period, TimeFormat.HOUR_24))
  }

  @Test
  fun coerceTime_appliesMinAndMaxAtMinutePrecision() {
    val min = LocalTime(9, 30)
    val max = LocalTime(17, 45)

    assertEquals(min, coerceTime(LocalTime(8, 0), min, max))
    assertEquals(max, coerceTime(LocalTime(18, 0), min, max))
  }

  @Test
  fun isTimeSelectable_respectsRange() {
    val min = LocalTime(9, 30)
    val max = LocalTime(17, 45)

    assertTrue(isTimeSelectable(LocalTime(12, 0), min, max))
    assertFalse(isTimeSelectable(LocalTime(18, 0), min, max))
  }
}
