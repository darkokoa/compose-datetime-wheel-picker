package dev.darkokoa.datetimewheelpicker.annotation

import kotlin.RequiresOptIn.Level

@RequiresOptIn(
  message = "This WheelDataTimePicker API is experimental and is likely to change or to be removed in the future.",
  level = Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalWheelDataTimePickerApi