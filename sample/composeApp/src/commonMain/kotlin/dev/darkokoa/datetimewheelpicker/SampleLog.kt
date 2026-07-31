package dev.darkokoa.datetimewheelpicker

import co.touchlab.kermit.Logger

private val sampleLogger = Logger.withTag("DateTimeWheelPickerSample")

private var callbackCounter = 0

/**
 * Logs a picker callback with a global sequence number so that repeated emissions
 * for a single scroll gesture are easy to spot.
 *
 * @param section identifies the concrete demo picker the callback comes from.
 */
internal fun logPickerCallback(section: String, callbackName: String, value: Any?) {
  callbackCounter += 1
  sampleLogger.d { "#$callbackCounter $section | $callbackName = $value" }
}
