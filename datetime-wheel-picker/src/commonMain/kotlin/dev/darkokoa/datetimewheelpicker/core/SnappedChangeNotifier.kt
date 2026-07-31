package dev.darkokoa.datetimewheelpicker.core

/**
 * Deduplicates consecutive change notifications that resolve to the same key, so that
 * multiple wheels reporting the same snapped value only notify the listener once.
 */
internal class SnappedChangeNotifier<K : Any> {

  private var lastKey: K? = null

  fun notifyIfChanged(key: K, notify: () -> Unit) {
    if (key == lastKey) return
    lastKey = key
    notify()
  }
}
