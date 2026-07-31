package dev.darkokoa.datetimewheelpicker.core

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SnappedChangeNotifierTest {

  @Test
  fun firstKeyNotifies() {
    val notifier = SnappedChangeNotifier<LocalDate>()
    var notified = 0
    notifier.notifyIfChanged(LocalDate(2025, 1, 31)) { notified++ }
    assertEquals(1, notified)
  }

  @Test
  fun repeatedKeyNotifiesOnlyOnce() {
    val notifier = SnappedChangeNotifier<LocalDate>()
    var notified = 0
    repeat(3) {
      notifier.notifyIfChanged(LocalDate(2025, 1, 31)) { notified++ }
    }
    assertEquals(1, notified)
  }

  @Test
  fun changedKeyNotifiesAgain() {
    val notifier = SnappedChangeNotifier<LocalDate>()
    val notifiedDates = mutableListOf<LocalDate>()
    val jan31 = LocalDate(2025, 1, 31)
    val feb28 = LocalDate(2025, 2, 28)
    notifier.notifyIfChanged(jan31) { notifiedDates += jan31 }
    notifier.notifyIfChanged(feb28) { notifiedDates += feb28 }
    assertEquals(listOf(jan31, feb28), notifiedDates)
  }

  @Test
  fun returningToEarlierKeyNotifiesAgain() {
    val notifier = SnappedChangeNotifier<LocalDate>()
    var notified = 0
    val jan31 = LocalDate(2025, 1, 31)
    val feb28 = LocalDate(2025, 2, 28)
    notifier.notifyIfChanged(jan31) { notified++ }
    notifier.notifyIfChanged(feb28) { notified++ }
    notifier.notifyIfChanged(jan31) { notified++ }
    assertEquals(3, notified)
  }

  @Test
  fun equalButNotSameInstanceKeyIsDeduplicated() {
    val notifier = SnappedChangeNotifier<LocalDate>()
    var notified = 0
    notifier.notifyIfChanged(LocalDate(2025, 1, 31)) { notified++ }
    notifier.notifyIfChanged(LocalDate(2025, 1, 31)) { notified++ }
    assertEquals(1, notified)
  }
}
