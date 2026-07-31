# Changelog

All notable changes to this project will be documented in this file.

## 1.4.1 - Unreleased

### Fixed

- Wheel callbacks no longer fire on first composition: initially positioning
  the wheels at `startDate`/`startTime` no longer triggers `onScrollChanged` /
  `onSnappedDateChanged` ([#71](https://github.com/darkokoa/compose-datetime-wheel-picker/issues/71)).
- Wheel callbacks no longer fire twice for a single scroll gesture: the brief
  idle gap between the end of a drag and the start of the snap fling could be
  misreported as a finished scroll ([#71](https://github.com/darkokoa/compose-datetime-wheel-picker/issues/71)).
- `startDate`/`startTime` outside the `minDate`/`maxDate` (`minTime`/`maxTime`)
  bounds is now coerced into range, keeping the initial wheel position
  consistent with the reported callback values.
- Date pickers now deduplicate consecutive `onSnappedDateChanged` notifications
  that resolve to the same date, so multiple wheel columns reporting the same
  snapped date only notify once.

## 1.4.0 - 2026-07-28

### Breaking changes

- **Removed the public `size: DpSize` parameter** from `WheelDatePicker`,
  `WheelTimePicker`, `WheelDateTimePicker`, and `WheelTextPicker`. Picker size
  is now controlled entirely through `Modifier` (`Modifier.size`, `width`,
  `height`, `fillMaxWidth`, `widthIn`, `weight`, ...). Pickers honor
  constraints from their root `Modifier` and parent layout, and shrink to fit
  parents narrower than their intrinsic width. `BoxWithConstraints`
  workarounds for responsive sizing can be removed.
- With no height constraint, the default wheel height now scales with
  `rowCount` (~42.7dp per row; `rowCount = 3` stays exactly 128.dp). Use
  `Modifier.height(128.dp)` to keep the former fixed total height for larger
  row counts.
- Renamed the public `WheelTextPicker` parameters `style` and `color` to
  `textStyle` and `textColor`.
- Added `selectedTextStyle` and `selectedTextColor` to the public
  `WheelDatePicker`, `WheelTimePicker`, `WheelDateTimePicker`, and
  `WheelTextPicker` APIs.
- These changes are **source-breaking**. Binaries compiled against 1.3.x keep
  linking through hidden compatibility overloads that restore the exact 1.3.x
  signatures; the hidden overloads will be removed in the next major release.

### Known limitations

- Pickers resolve their size via subcomposition and no longer support
  intrinsic-measurement parents (`IntrinsicSize.Min`/`Max` will throw). Pass
  an explicit `width`/`height` instead.

#### Migration

Before:

```kotlin
WheelDatePicker(size = DpSize(300.dp, 160.dp)) { }

BoxWithConstraints(Modifier.fillMaxWidth()) {
  WheelDatePicker(size = DpSize(maxWidth, 200.dp)) { }
}

WheelTextPicker(
  texts = values,
  rowCount = 3,
  size = DpSize(96.dp, 128.dp),
  style = MaterialTheme.typography.titleMedium,
  color = LocalContentColor.current,
)
```

After:

```kotlin
WheelDatePicker(modifier = Modifier.size(300.dp, 160.dp)) { }

WheelDatePicker(modifier = Modifier.fillMaxWidth().height(200.dp)) { }

WheelTextPicker(
  modifier = Modifier.size(96.dp, 128.dp),
  texts = values,
  rowCount = 3,
  textStyle = MaterialTheme.typography.titleMedium,
  textColor = LocalContentColor.current,
  selectedTextStyle = MaterialTheme.typography.titleMedium.copy(
    fontWeight = FontWeight.Bold,
  ),
  selectedTextColor = MaterialTheme.colorScheme.primary,
)
```

### Added

- Added independent text styles and colors for the selected (centered) wheel
  item.
- Applied selected-item styling to date, time, date-time, and text pickers,
  including time separators and CJK date suffixes.

### Changed

- Partial picker text styles are now resolved against the default and inactive
  styles.
- Reduced selected-index recomposition to rows whose selected state changes.
- Reduced text measurement work for CJK pickers.
- Time picker column widths now update live when the container resizes
  (removed a stale width cache).
