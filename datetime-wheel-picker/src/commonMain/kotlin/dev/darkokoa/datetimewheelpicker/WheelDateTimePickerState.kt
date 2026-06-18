package dev.darkokoa.datetimewheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.darkokoa.datetimewheelpicker.core.CYB3R_1N1T_ZOLL
import dev.darkokoa.datetimewheelpicker.core.EPOCH
import dev.darkokoa.datetimewheelpicker.core.MAX
import dev.darkokoa.datetimewheelpicker.core.MIN
import dev.darkokoa.datetimewheelpicker.core.SnappedDate
import dev.darkokoa.datetimewheelpicker.core.SnappedDateTime
import dev.darkokoa.datetimewheelpicker.core.SnappedTime
import dev.darkokoa.datetimewheelpicker.core.coerceDateTime
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.isDateTimeSelectable
import dev.darkokoa.datetimewheelpicker.core.localTimeToAmPmHour
import dev.darkokoa.datetimewheelpicker.core.now
import dev.darkokoa.datetimewheelpicker.core.withDate
import dev.darkokoa.datetimewheelpicker.core.withTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

@Stable
class WheelDateTimePickerState internal constructor(
  initialSelectedDateTime: LocalDateTime,
  initialDisplayedDateTime: LocalDateTime,
  val minDateTime: LocalDateTime,
  val maxDateTime: LocalDateTime,
  val yearsRange: IntRange?,
  internal val dateState: WheelDatePickerState,
  internal val timeState: WheelTimePickerState,
) {
  var displayedDateTime by mutableStateOf(coerceDateTime(initialDisplayedDateTime, minDateTime, maxDateTime, yearsRange))
    private set

  var selectedDateTime by mutableStateOf(coerceDateTime(initialSelectedDateTime, minDateTime, maxDateTime, yearsRange))
    private set

  internal var isProgrammaticScrollInProgress by mutableStateOf(false)
    private set

  val isScrollInProgress: Boolean
    get() = dateState.isScrollInProgress || timeState.isScrollInProgress

  fun coerceDateTime(dateTime: LocalDateTime): LocalDateTime {
    return coerceDateTime(dateTime, minDateTime, maxDateTime, yearsRange)
  }

  fun isDateTimeSelectable(dateTime: LocalDateTime): Boolean {
    return isDateTimeSelectable(dateTime, minDateTime, maxDateTime, yearsRange)
  }

  suspend fun scrollToDateTime(dateTime: LocalDateTime): LocalDateTime {
    return scrollToDateTime(dateTime, animate = false)
  }

  suspend fun animateScrollToDateTime(dateTime: LocalDateTime): LocalDateTime {
    return scrollToDateTime(dateTime, animate = true)
  }

  internal fun updateDisplayedDateTime(snappedDate: SnappedDate): DateTimePickerSnap {
    val target = coerceDateTime(displayedDateTime.withDate(snappedDate.snappedLocalDate))
    displayedDateTime = target
    return target.toSnappedDateTime(snappedDate).toPickerSnap()
  }

  internal fun settleDateTime(snappedDate: SnappedDate): DateTimePickerSnap {
    val target = syncToDateTime(selectedDateTime.withDate(snappedDate.snappedLocalDate))
    return target.toSnappedDateTime(snappedDate).toPickerSnap()
  }

  internal fun updateDisplayedDateTime(snappedTime: SnappedTime, timeFormat: TimeFormat): DateTimePickerSnap {
    val target = coerceDateTime(displayedDateTime.withTime(snappedTime.snappedLocalTime))
    displayedDateTime = target
    return target.toSnappedDateTime(snappedTime, timeFormat).toPickerSnap()
  }

  internal fun settleDateTime(snappedTime: SnappedTime, timeFormat: TimeFormat): DateTimePickerSnap {
    val target = syncToDateTime(selectedDateTime.withTime(snappedTime.snappedLocalTime))
    return target.toSnappedDateTime(snappedTime, timeFormat).toPickerSnap()
  }

  internal suspend fun snapWheelsToDateTime(dateTime: LocalDateTime) {
    val target = coerceDateTime(dateTime)
    isProgrammaticScrollInProgress = true
    try {
      syncToDateTime(target)
      dateState.snapWheelsToDate(target.date)
      timeState.snapWheelsToTime(target.time)
    } finally {
      isProgrammaticScrollInProgress = false
    }
  }

  private suspend fun scrollToDateTime(dateTime: LocalDateTime, animate: Boolean): LocalDateTime {
    val target = coerceDateTime(dateTime)
    isProgrammaticScrollInProgress = true
    try {
      syncToDateTime(target)
      if (animate) {
        dateState.animateScrollToDate(target.date)
        timeState.animateScrollToTime(target.time)
      } else {
        dateState.scrollToDate(target.date)
        timeState.scrollToTime(target.time)
      }
    } finally {
      isProgrammaticScrollInProgress = false
    }
    return target
  }

  private fun syncToDateTime(dateTime: LocalDateTime): LocalDateTime {
    val target = coerceDateTime(dateTime)
    displayedDateTime = target
    selectedDateTime = target
    dateState.syncToDate(target.date)
    timeState.syncToTime(target.time)
    return target
  }
}

internal data class DateTimePickerSnap(
  val snappedDateTime: SnappedDateTime,
  val index: Int,
)

@Composable
fun rememberWheelDateTimePickerState(
  initialSelectedDateTime: LocalDateTime = LocalDateTime.now(),
  minDateTime: LocalDateTime = LocalDateTime.EPOCH,
  maxDateTime: LocalDateTime = LocalDateTime.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDateTime.year, maxDateTime.year),
): WheelDateTimePickerState {
  val selectedDateTime = coerceDateTime(initialSelectedDateTime, minDateTime, maxDateTime, yearsRange)
  val dateState = rememberWheelDatePickerState(
    initialSelectedDate = selectedDateTime.date,
    minDate = minDateTime.date,
    maxDate = maxDateTime.date,
    yearsRange = yearsRange,
  )
  val timeState = rememberWheelTimePickerState(
    initialSelectedTime = selectedDateTime.time,
    minTime = LocalTime.MIN,
    maxTime = LocalTime.MAX,
    resetKey = DateTimePickerTimeStateKey(
      selectedDateTime = selectedDateTime,
      minDateTime = minDateTime,
      maxDateTime = maxDateTime,
      yearsRange = yearsRange,
    ),
  )

  return remember(minDateTime, maxDateTime, yearsRange, dateState, timeState) {
    WheelDateTimePickerState(
      initialSelectedDateTime = selectedDateTime,
      initialDisplayedDateTime = selectedDateTime,
      minDateTime = minDateTime,
      maxDateTime = maxDateTime,
      yearsRange = yearsRange,
      dateState = dateState,
      timeState = timeState,
    )
  }
}

private fun LocalDateTime.toSnappedDateTime(snappedDate: SnappedDate): SnappedDateTime {
  return when (snappedDate) {
    is SnappedDate.DayOfMonth -> SnappedDateTime.DayOfMonth(this, day - 1)
    is SnappedDate.Month -> SnappedDateTime.Month(this, month.number - 1)
    is SnappedDate.Year -> SnappedDateTime.Year(this, snappedDate.snappedIndex)
  }
}

private fun LocalDateTime.toSnappedDateTime(snappedTime: SnappedTime, timeFormat: TimeFormat): SnappedDateTime {
  return when (snappedTime) {
    is SnappedTime.Hour -> SnappedDateTime.Hour(
      localDateTime = this,
      index = if (timeFormat == TimeFormat.HOUR_24) hour else localTimeToAmPmHour(time) - 1,
    )

    is SnappedTime.Minute -> SnappedDateTime.Minute(this, minute)
  }
}

private fun SnappedDateTime.toPickerSnap(): DateTimePickerSnap {
  return DateTimePickerSnap(snappedDateTime = this, index = snappedIndex)
}

private data class DateTimePickerTimeStateKey(
  val selectedDateTime: LocalDateTime,
  val minDateTime: LocalDateTime,
  val maxDateTime: LocalDateTime,
  val yearsRange: IntRange?,
)
