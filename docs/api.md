# API reference

> Applies to **1.5.0**. `rows` and `barrelProperties` do not exist in 1.4.x.

All pickers are `@Composable` functions in `dev.darkokoa.datetimewheelpicker`
(`WheelTextPicker` lives in `dev.darkokoa.datetimewheelpicker.core`). `modifier` is the first
parameter and the last parameter is a callback, so the trailing-lambda form
`WheelDatePicker { snappedDate -> }` binds to `onSnappedDate`.

Dates and times use [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) types.
Defaults below are described by value; the helper constants behind them are not public API.

## Parameters shared by all pickers

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `modifier` | `Modifier` | `Modifier` | Sizing and placement. Unconstrained axes use the intrinsic default. See [Sizing](sizing.md) |
| `rows` | `WheelRows` | `WheelRows.Count(3)` | Row count, or a fixed row height. See [Rows](rows-and-barrel.md#rows) |
| `textStyle` | `TextStyle` | `MaterialTheme.typography.titleMedium` | Text style of unselected items |
| `textColor` | `Color` | `LocalContentColor.current` | Text color of unselected items |
| `selectedTextStyle` | `TextStyle` | `textStyle` | Text style of the selected (centered) item |
| `selectedTextColor` | `Color` | `textColor` | Text color of the selected (centered) item |
| `selectorProperties` | `SelectorProperties` | `WheelPickerDefaults.selectorProperties()` | Selector appearance: `enabled`, `shape`, `color`, `border` |
| `barrelProperties` | `BarrelProperties` | `WheelPickerDefaults.barrelPropertiesFor(rows)` | Rim angle and edge fade. See [Barrel projection](rows-and-barrel.md#barrel-projection) |

## WheelDatePicker

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startDate` | `LocalDate` | Today (system time zone) | Initially selected date. Coerced into `minDate..maxDate` |
| `minDate` | `LocalDate` | 1970-01-01 | Minimum selectable date |
| `maxDate` | `LocalDate` | 2077-12-31 | Maximum selectable date |
| `yearsRange` | `IntRange?` | `minDate.year..maxDate.year` | Years to show. **`null` hides the year wheel** |
| `dateFormatter` | `DateFormatter` | `dateFormatter(Locale.current, MonthDisplayStyle.FULL, CjkSuffixConfig.ShowAll)` | Date order, month style, CJK suffixes |
| `onSnappedDateChanged` | `(LocalDate) -> Unit` | `{}` | Fires **during scrolling** whenever the snapped date changes |
| `onSnappedDate` | `(LocalDate) -> Unit` | `{}` | Fires **once scrolling settles** on the final date |

Plus the [shared parameters](#parameters-shared-by-all-pickers).

## WheelTimePicker

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startTime` | `LocalTime` | Now (system time zone) | Initially selected time. Coerced into `minTime..maxTime` |
| `minTime` | `LocalTime` | 00:00 | Minimum selectable time |
| `maxTime` | `LocalTime` | 23:59:59.999999999 | Maximum selectable time |
| `timeFormatter` | `TimeFormatter` | `timeFormatter(Locale.current)` | 12/24-hour format and text. See [TimeFormat](#timeformat) |
| `onSnappedTimeChanged` | `(LocalTime) -> Unit` | `{}` | Fires **during scrolling** whenever the snapped time changes |
| `onSnappedTime` | `(LocalTime) -> Unit` | `{}` | Fires **once scrolling settles** on the final time |

Plus the [shared parameters](#parameters-shared-by-all-pickers). Intrinsic width is 128.dp.

## WheelDateTimePicker

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startDateTime` | `LocalDateTime` | Now (system time zone) | Initially selected value |
| `minDateTime` | `LocalDateTime` | 1970-01-01T00:00 | Minimum selectable value |
| `maxDateTime` | `LocalDateTime` | 2077-12-31T23:59:59.999999999 | Maximum selectable value |
| `yearsRange` | `IntRange?` | `minDateTime.year..maxDateTime.year` | Years to show. **`null` hides the year wheel** |
| `dateFormatter` | `DateFormatter` | `dateFormatter(Locale.current, MonthDisplayStyle.SHORT, CjkSuffixConfig.HideAll)` | Note the defaults differ from `WheelDatePicker` |
| `timeFormatter` | `TimeFormatter` | `timeFormatter(Locale.current)` | 12/24-hour format and text |
| `onSnappedDateTimeChanged` | `(LocalDateTime) -> Unit` | `{}` | Fires **during scrolling** whenever the snapped value changes |
| `onSnappedDateTime` | `(LocalDateTime) -> Unit` | `{}` | Fires **once scrolling settles** on the final value |

Plus the [shared parameters](#parameters-shared-by-all-pickers).

## WheelTextPicker

A wheel over arbitrary strings.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `texts` | `List<String>` | required | Items to show |
| `startIndex` | `Int` | `0` | Initially selected index |
| `onScrollChanged` | `(snappedIndex: Int) -> Unit` | `{}` | Fires during scrolling whenever the snapped index changes |
| `onScrollFinished` | `(snappedIndex: Int) -> Int?` | `{ null }` | Fires once scrolling settles. Return another index to scroll there, or `null` to stay |

Plus the [shared parameters](#parameters-shared-by-all-pickers). Intrinsic width is 128.dp.

## Callbacks: `...Changed` vs final

- `onSnapped…Changed` fires continuously while the user scrolls, each time a different item snaps
  into the selector. Use it for live previews or syncing other UI.
- `onSnapped…` fires once after the wheel comes to rest and represents the user's final choice.
  Use it to commit the selection (save, navigate, ...).

Neither fires for the initial positioning at `startDate` / `startTime`, and a single gesture fires
each callback once.

## Formatters

### `dateFormatter`

There are two public entry points:

```kotlin
// @Composable: resolves strings and date order from the locale
dateFormatter(
  locale = Locale.current,
  monthDisplayStyle = MonthDisplayStyle.FULL,
  cjkSuffixConfig = CjkSuffixConfig.ShowAll,
)

// Plain function: you choose the order; every parameter is optional
dateFormatter(
  dateOrder = DateOrder.DMY,
  monthDisplayStyle = MonthDisplayStyle.FULL,
  cjkSuffixConfig = CjkSuffixConfig.ShowAll,
  // formatYear / formatMonth / formatDay can replace the text of each field
)
```

`Locale` is `androidx.compose.ui.text.intl.Locale`.

**`DateOrder`**

| Value | Order | Typical regions |
|-------|-------|-----------------|
| `DateOrder.DMY` | Day, Month, Year | Europe, most of the world |
| `DateOrder.MDY` | Month, Day, Year | United States |
| `DateOrder.YMD` | Year, Month, Day | East Asia, ISO 8601 |

**`MonthDisplayStyle`**

| Value | Example |
|-------|---------|
| `FULL` | January, February, ... |
| `SHORT` | Jan, Feb, ... |
| `NUMERIC` | 1, 2, ... |

**`CjkSuffixConfig`** (Chinese, Japanese, Korean)

| Value | Effect |
|-------|--------|
| `CjkSuffixConfig.ShowAll` | Shows 年/月/日 (Korean: 년/월/일) |
| `CjkSuffixConfig.HideAll` | Hides all suffixes |
| `CjkSuffixConfig(showYearSuffix = true, showMonthSuffix = false, ...)` | Pick suffixes individually; spacing is configurable |
| `CjkSuffixConfig(CjkSuffixVisibility.YearOnly)` | Presets: `ShowAll`, `HideAll`, `YearOnly`, `MonthDayOnly` |

### `timeFormatter`

```kotlin
timeFormatter(Locale.current)                    // @Composable: 12-hour for English or US/GB, else 24-hour
timeFormatter(timeFormat = TimeFormat.AM_PM)     // plain function: choose explicitly
```

#### `TimeFormat`

- `TimeFormat.HOUR_24`: 00:00 to 23:59
- `TimeFormat.AM_PM`: 12-hour with an AM/PM wheel

The plain function also accepts `formatHour`, `formatMinute`, `formatAmText` and `formatPmText`
to replace the text.

## Styling the selected item

`selectedTextStyle` / `selectedTextColor` style the **centered (snapped) item** differently from the
others. By default they equal `textStyle` / `textColor`.

```kotlin
WheelDatePicker(
  textStyle = MaterialTheme.typography.titleMedium,
  textColor = LocalContentColor.current,
  selectedTextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
  selectedTextColor = MaterialTheme.colorScheme.primary,
)
```

- `selectedTextColor` overrides `selectedTextStyle.color`, mirroring how `Text(color = ...)`
  overrides `TextStyle.color` in Compose.
- In `WheelTimePicker` and `WheelDateTimePicker` the colon separator sits in the center row next to
  the selected hour and minute, so it follows the `selected*` parameters. Without them the colon is
  unchanged.
