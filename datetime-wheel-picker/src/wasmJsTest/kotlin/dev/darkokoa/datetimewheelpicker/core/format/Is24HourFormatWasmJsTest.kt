package dev.darkokoa.datetimewheelpicker.core.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Smoke tests for the WasmJS `resolveHour12Js()` interop.
 *
 * These must run in a browser environment (`wasmJsBrowserTest`) because the
 * module-level initialization references `window` and `document`.
 */
class Is24HourFormatWasmJsTest {

  @Test
  fun resolveHour12Js_returnsBoolean() {
    val result = resolveHour12Js()
    assertIs<Boolean>(result)
  }

  @Test
  fun resolveHour12Js_isConsistentAcrossCalls() {
    val first = resolveHour12Js()
    val second = resolveHour12Js()
    assertEquals(first, second)
  }
}
