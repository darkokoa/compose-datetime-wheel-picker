# Changelog

All notable changes to this project will be documented in this file.

## 1.4.0 - Unreleased

### Breaking changes

- Renamed the public `WheelTextPicker` parameters `style` and `color` to
  `textStyle` and `textColor`.
- Added `selectedTextStyle` and `selectedTextColor` to the public
  `WheelDatePicker`, `WheelTimePicker`, `WheelDateTimePicker`, and
  `WheelTextPicker` APIs. This changes their binary signatures.
- Applications and libraries compiled against 1.3.x must be recompiled after
  upgrading. Positional calls that pass arguments after `textColor` may also
  need to be updated.

#### Migration

Before:

```kotlin
WheelTextPicker(
  texts = values,
  rowCount = 3,
  style = MaterialTheme.typography.titleMedium,
  color = LocalContentColor.current,
)
```

After:

```kotlin
WheelTextPicker(
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
