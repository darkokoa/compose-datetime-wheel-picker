package dev.darkokoa.datetimewheelpicker.core

import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat

internal interface DatePickerEventSink {
  fun onDisplayedDateChanged(snappedDate: SnappedDate) = Unit

  fun onDateSettled(snappedDate: SnappedDate): Int? = null
}

internal object NoOpDatePickerEventSink : DatePickerEventSink

internal interface TimePickerEventSink {
  fun onDisplayedTimeChanged(snappedTime: SnappedTime, timeFormat: TimeFormat) = Unit

  fun onTimeSettled(snappedTime: SnappedTime, timeFormat: TimeFormat): Int? = null
}

internal object NoOpTimePickerEventSink : TimePickerEventSink

internal interface DateTimePickerEventSink {
  fun onDateTimeSettled(snappedDateTime: SnappedDateTime): Int? = null
}

internal object NoOpDateTimePickerEventSink : DateTimePickerEventSink
