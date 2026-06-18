package dev.darkokoa.datetimewheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.darkokoa.datetimewheelpicker.core.MAX
import dev.darkokoa.datetimewheelpicker.core.MIN
import dev.darkokoa.datetimewheelpicker.core.SnappedTime
import dev.darkokoa.datetimewheelpicker.core.TimePickerField
import dev.darkokoa.datetimewheelpicker.core.WheelPickerState
import dev.darkokoa.datetimewheelpicker.core.coerceTime
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.isTimeSelectable
import dev.darkokoa.datetimewheelpicker.core.now
import dev.darkokoa.datetimewheelpicker.core.timeFromIndex
import dev.darkokoa.datetimewheelpicker.core.timeIndex
import kotlinx.datetime.LocalTime

@Stable
class WheelTimePickerState internal constructor(
  initialSelectedTime: LocalTime,
  initialDisplayedTime: LocalTime,
  val minTime: LocalTime,
  val maxTime: LocalTime,
  internal val hour24WheelState: WheelPickerState,
  internal val amPmHourWheelState: WheelPickerState,
  internal val minuteWheelState: WheelPickerState,
  internal val periodWheelState: WheelPickerState,
) {
  var displayedTime by mutableStateOf(coerceTime(initialDisplayedTime, minTime, maxTime))
    private set

  var selectedTime by mutableStateOf(coerceTime(initialSelectedTime, minTime, maxTime))
    private set

  internal var isProgrammaticScrollInProgress by mutableStateOf(false)
    private set

  val isScrollInProgress: Boolean
    get() = hour24WheelState.isScrollInProgress ||
      amPmHourWheelState.isScrollInProgress ||
      minuteWheelState.isScrollInProgress ||
      periodWheelState.isScrollInProgress

  fun coerceTime(time: LocalTime): LocalTime {
    return coerceTime(time, minTime, maxTime)
  }

  fun isTimeSelectable(time: LocalTime): Boolean {
    return isTimeSelectable(time, minTime, maxTime)
  }

  suspend fun scrollToTime(time: LocalTime): LocalTime {
    return scrollToTime(time, animate = false)
  }

  suspend fun animateScrollToTime(time: LocalTime): LocalTime {
    return scrollToTime(time, animate = true)
  }

  internal fun updateDisplayedTime(field: TimePickerField, index: Int, timeFormat: TimeFormat): TimePickerSnap? {
    val pendingTime = timeFromIndex(displayedTime, field, index, timeFormat) ?: return null
    val coercedTime = coerceTime(pendingTime)
    displayedTime = coercedTime
    val targetIndex = indexFor(field, timeFormat = timeFormat, time = coercedTime) ?: index
    return TimePickerSnap(snappedTime = coercedTime.toSnappedTime(field, targetIndex), index = targetIndex)
  }

  internal fun settleTime(field: TimePickerField, index: Int, timeFormat: TimeFormat): TimePickerSnap? {
    val pendingTime = timeFromIndex(selectedTime, field, index, timeFormat) ?: return null
    val coercedTime = syncToTime(pendingTime)
    val targetIndex = indexFor(field, timeFormat = timeFormat, time = coercedTime) ?: index
    return TimePickerSnap(snappedTime = coercedTime.toSnappedTime(field, targetIndex), index = targetIndex)
  }

  internal fun syncToTime(time: LocalTime): LocalTime {
    val targetTime = coerceTime(time)
    displayedTime = targetTime
    selectedTime = targetTime
    return targetTime
  }

  internal suspend fun snapWheelsToTime(time: LocalTime) {
    isProgrammaticScrollInProgress = true
    try {
      scrollWheelsToTime(syncToTime(time), animate = false)
    } finally {
      isProgrammaticScrollInProgress = false
    }
  }

  private suspend fun scrollToTime(time: LocalTime, animate: Boolean): LocalTime {
    isProgrammaticScrollInProgress = true
    try {
      val targetTime = syncToTime(time)
      scrollWheelsToTime(targetTime, animate)
      return targetTime
    } finally {
      isProgrammaticScrollInProgress = false
    }
  }

  private suspend fun scrollWheelsToTime(time: LocalTime, animate: Boolean) {
    suspend fun WheelPickerState.scroll(index: Int) {
      if (animate) animateScrollToIndex(index) else scrollToIndex(index)
    }

    hour24WheelState.scroll(timeIndex(time, TimePickerField.Hour, TimeFormat.HOUR_24) ?: 0)
    amPmHourWheelState.scroll(timeIndex(time, TimePickerField.Hour, TimeFormat.AM_PM) ?: 0)
    minuteWheelState.scroll(timeIndex(time, TimePickerField.Minute, TimeFormat.HOUR_24) ?: 0)
    periodWheelState.scroll(timeIndex(time, TimePickerField.Period, TimeFormat.AM_PM) ?: 0)
  }

  internal fun indexFor(
    field: TimePickerField,
    timeFormat: TimeFormat,
    time: LocalTime = selectedTime,
  ): Int? {
    return timeIndex(time, field, timeFormat)
  }
}

internal data class TimePickerSnap(
  val snappedTime: SnappedTime,
  val index: Int,
)

@Composable
fun rememberWheelTimePickerState(
  initialSelectedTime: LocalTime = LocalTime.now(),
  minTime: LocalTime = LocalTime.MIN,
  maxTime: LocalTime = LocalTime.MAX,
): WheelTimePickerState {
  return rememberWheelTimePickerState(
    initialSelectedTime = initialSelectedTime,
    minTime = minTime,
    maxTime = maxTime,
    resetKey = null,
  )
}

@Composable
internal fun rememberWheelTimePickerState(
  initialSelectedTime: LocalTime,
  minTime: LocalTime,
  maxTime: LocalTime,
  resetKey: Any?,
): WheelTimePickerState {
  val selectedTime = coerceTime(initialSelectedTime, minTime, maxTime)
  val hour24WheelState = remember(minTime, maxTime, resetKey) {
    WheelPickerState(timeIndex(selectedTime, TimePickerField.Hour, TimeFormat.HOUR_24) ?: 0)
  }
  val amPmHourWheelState = remember(minTime, maxTime, resetKey) {
    WheelPickerState(timeIndex(selectedTime, TimePickerField.Hour, TimeFormat.AM_PM) ?: 0)
  }
  val minuteWheelState = remember(minTime, maxTime, resetKey) {
    WheelPickerState(timeIndex(selectedTime, TimePickerField.Minute, TimeFormat.HOUR_24) ?: 0)
  }
  val periodWheelState = remember(minTime, maxTime, resetKey) {
    WheelPickerState(timeIndex(selectedTime, TimePickerField.Period, TimeFormat.AM_PM) ?: 0)
  }

  return remember(minTime, maxTime, resetKey, hour24WheelState, amPmHourWheelState, minuteWheelState, periodWheelState) {
    WheelTimePickerState(
      initialSelectedTime = selectedTime,
      initialDisplayedTime = selectedTime,
      minTime = minTime,
      maxTime = maxTime,
      hour24WheelState = hour24WheelState,
      amPmHourWheelState = amPmHourWheelState,
      minuteWheelState = minuteWheelState,
      periodWheelState = periodWheelState,
    )
  }
}

private fun LocalTime.toSnappedTime(field: TimePickerField, index: Int): SnappedTime {
  return when (field) {
    TimePickerField.Hour,
    TimePickerField.Period -> SnappedTime.Hour(localTime = this, index = index)
    TimePickerField.Minute -> SnappedTime.Minute(localTime = this, index = index)
  }
}
