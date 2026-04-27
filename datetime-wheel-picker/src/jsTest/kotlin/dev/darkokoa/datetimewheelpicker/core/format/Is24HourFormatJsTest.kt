package dev.darkokoa.datetimewheelpicker.core.format

import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Smoke tests for the JS `resolveHour12()` interop.
 *
 * These must run in a **browser** environment (`jsBrowserTest`) because the
 * module-level initialization in [Is24HourFormat.js][dev.darkokoa.datetimewheelpicker.core.format]
 * references `window` and `document`.
 */
class Is24HourFormatJsTest {

  @Test
  fun resolveHour12_returnsBoolean() {
    val result = resolveHour12()
    assertIs<Boolean>(result)
  }

  @Test
  fun resolveHour12_isConsistentAcrossCalls() {
    val first = resolveHour12()
    val second = resolveHour12()
    kotlin.test.assertEquals(first, second)
  }
}
