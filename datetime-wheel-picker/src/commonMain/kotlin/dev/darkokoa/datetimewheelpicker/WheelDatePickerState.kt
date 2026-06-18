package dev.darkokoa.datetimewheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.darkokoa.datetimewheelpicker.core.CYB3R_1N1T_ZOLL
import dev.darkokoa.datetimewheelpicker.core.EPOCH
import dev.darkokoa.datetimewheelpicker.core.SnappedDate
import dev.darkokoa.datetimewheelpicker.core.WheelPickerState
import dev.darkokoa.datetimewheelpicker.core.coerceDate
import dev.darkokoa.datetimewheelpicker.core.dateFromIndex
import dev.darkokoa.datetimewheelpicker.core.dateIndex
import dev.darkokoa.datetimewheelpicker.core.format.DateField
import dev.darkokoa.datetimewheelpicker.core.isDateSelectable
import dev.darkokoa.datetimewheelpicker.core.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Stable
class WheelDatePickerState internal constructor(
  initialSelectedDate: LocalDate,
  initialDisplayedDate: LocalDate,
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val yearsRange: IntRange?,
  internal val dayWheelState: WheelPickerState,
  internal val monthWheelState: WheelPickerState,
  internal val yearWheelState: WheelPickerState?,
) {
  var displayedDate by mutableStateOf(coerceDate(initialDisplayedDate, minDate, maxDate, yearsRange))
    private set

  var selectedDate by mutableStateOf(coerceDate(initialSelectedDate, minDate, maxDate, yearsRange))
    private set

  internal var isProgrammaticScrollInProgress by mutableStateOf(false)
    private set

  val isScrollInProgress: Boolean
    get() = dayWheelState.isScrollInProgress ||
      monthWheelState.isScrollInProgress ||
      yearWheelState?.isScrollInProgress == true

  fun coerceDate(date: LocalDate): LocalDate {
    return coerceDate(date, minDate, maxDate, yearsRange)
  }

  fun isDateSelectable(date: LocalDate): Boolean {
    return isDateSelectable(date, minDate, maxDate, yearsRange)
  }

  suspend fun scrollToDate(date: LocalDate): LocalDate {
    return scrollToDate(date, animate = false)
  }

  suspend fun animateScrollToDate(date: LocalDate): LocalDate {
    return scrollToDate(date, animate = true)
  }

  internal fun updateDisplayedDate(field: DateField, index: Int): DatePickerSnap? {
    val pendingDate = dateFromIndex(displayedDate, field, index, yearsRange) ?: return null
    val coercedDate = coerceDate(pendingDate)
    displayedDate = coercedDate
    val targetIndex = indexFor(field, coercedDate) ?: index
    return DatePickerSnap(snappedDate = coercedDate.toSnappedDate(field, targetIndex), index = targetIndex)
  }

  internal fun settleDate(field: DateField, index: Int): DatePickerSnap? {
    val pendingDate = dateFromIndex(selectedDate, field, index, yearsRange) ?: return null
    val coercedDate = syncToDate(pendingDate)
    val targetIndex = indexFor(field, coercedDate) ?: index
    return DatePickerSnap(snappedDate = coercedDate.toSnappedDate(field, targetIndex), index = targetIndex)
  }

  internal fun syncToDate(date: LocalDate): LocalDate {
    val targetDate = coerceDate(date)
    displayedDate = targetDate
    selectedDate = targetDate
    return targetDate
  }

  internal suspend fun snapWheelsToDate(date: LocalDate) {
    isProgrammaticScrollInProgress = true
    try {
      scrollWheelsToDate(syncToDate(date), animate = false)
    } finally {
      isProgrammaticScrollInProgress = false
    }
  }

  private suspend fun scrollToDate(date: LocalDate, animate: Boolean): LocalDate {
    isProgrammaticScrollInProgress = true
    try {
      val targetDate = syncToDate(date)
      scrollWheelsToDate(targetDate, animate)
      return targetDate
    } finally {
      isProgrammaticScrollInProgress = false
    }
  }

  private suspend fun scrollWheelsToDate(date: LocalDate, animate: Boolean) {
    suspend fun WheelPickerState.scroll(index: Int) {
      if (animate) animateScrollToIndex(index) else scrollToIndex(index)
    }

    dayWheelState.scroll(indexFor(DateField.DAY, date) ?: 0)
    monthWheelState.scroll(indexFor(DateField.MONTH, date) ?: 0)
    yearWheelState?.scroll(indexFor(DateField.YEAR, date) ?: 0)
  }

  internal fun indexFor(field: DateField, date: LocalDate = selectedDate): Int? {
    return dateIndex(date, field, yearsRange)
  }
}

internal data class DatePickerSnap(
  val snappedDate: SnappedDate,
  val index: Int,
)

@Composable
fun rememberWheelDatePickerState(
  initialSelectedDate: LocalDate = LocalDate.now(),
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
): WheelDatePickerState {
  val selectedDate = coerceDate(initialSelectedDate, minDate, maxDate, yearsRange)
  val dayWheelState = remember(minDate, maxDate, yearsRange) {
    WheelPickerState(dateIndex(selectedDate, DateField.DAY, yearsRange) ?: 0)
  }
  val monthWheelState = remember(minDate, maxDate, yearsRange) {
    WheelPickerState(dateIndex(selectedDate, DateField.MONTH, yearsRange) ?: 0)
  }
  val yearWheelState = yearsRange?.let {
    remember(minDate, maxDate, yearsRange) {
      WheelPickerState(dateIndex(selectedDate, DateField.YEAR, yearsRange) ?: 0)
    }
  }

  return remember(minDate, maxDate, yearsRange, dayWheelState, monthWheelState, yearWheelState) {
    WheelDatePickerState(
      initialSelectedDate = selectedDate,
      initialDisplayedDate = selectedDate,
      minDate = minDate,
      maxDate = maxDate,
      yearsRange = yearsRange,
      dayWheelState = dayWheelState,
      monthWheelState = monthWheelState,
      yearWheelState = yearWheelState,
    )
  }
}

private fun LocalDate.toSnappedDate(field: DateField, index: Int): SnappedDate {
  return when (field) {
    DateField.DAY -> SnappedDate.DayOfMonth(localDate = this, index = index)
    DateField.MONTH -> SnappedDate.Month(localDate = this, index = index)
    DateField.YEAR -> SnappedDate.Year(localDate = this, index = index)
  }
}
