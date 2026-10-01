# Recipes

Common configurations. Imports are omitted; the types come from
`dev.darkokoa.datetimewheelpicker`, `dev.darkokoa.datetimewheelpicker.core` (`WheelPickerDefaults`,
`WheelRows`) and `dev.darkokoa.datetimewheelpicker.core.format`, plus
[kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime).

## Day-month picker (no year)

For birthdays, anniversaries or recurring events. `yearsRange = null` hides the year wheel.

```kotlin
WheelDatePicker(
  startDate = LocalDate(2026, 6, 15),
  yearsRange = null,
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.DMY,
    monthDisplayStyle = MonthDisplayStyle.FULL,
  ),
) { snappedDate ->
  // use snappedDate.month and snappedDate.day
}
```

US-style month-day:

```kotlin
WheelDatePicker(
  yearsRange = null,
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.MDY,
    monthDisplayStyle = MonthDisplayStyle.SHORT,
  ),
) { snappedDate -> }
```

## Limited year range

```kotlin
val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

// The next 10 years
WheelDatePicker(
  minDate = today,
  maxDate = LocalDate(today.year + 10, 12, 31),
  dateFormatter = dateFormatter(dateOrder = DateOrder.YMD),
) { snappedDate -> }

// A birth date: the past 100 years
WheelDatePicker(
  startDate = LocalDate(today.year - 30, 1, 1),
  minDate = LocalDate(today.year - 100, 1, 1),
  maxDate = today,
  dateFormatter = dateFormatter(dateOrder = DateOrder.DMY),
) { snappedDate -> }
```

`Clock` is `kotlin.time.Clock`. `yearsRange` defaults to `minDate.year..maxDate.year`, so setting
`minDate` / `maxDate` also limits the year wheel.

## Chinese / Japanese / Korean

Dates in CJK locales are ordered year-month-day and can carry the native suffixes:

- Chinese: 2026年1月15日
- Japanese: 2026年1月15日
- Korean: 2026년1월15일

```kotlin
WheelDatePicker(
  dateFormatter = dateFormatter(
    locale = Locale("zh"),            // "zh", "ja" or "ko"
    monthDisplayStyle = MonthDisplayStyle.NUMERIC,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll,
  ),
) { snappedDate -> }

// Without suffixes
WheelDatePicker(
  dateFormatter = dateFormatter(
    locale = Locale("zh"),
    monthDisplayStyle = MonthDisplayStyle.NUMERIC,
    cjkSuffixConfig = CjkSuffixConfig.HideAll,
  ),
) { snappedDate -> }
```

This uses the `@Composable` `dateFormatter(locale, ...)` overload, which detects the date order
(YMD for CJK) from the locale. `WheelDatePicker` already uses it with `Locale.current` by default.

## Numeric months

```kotlin
WheelDatePicker(
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.DMY,        // or MDY, YMD
    monthDisplayStyle = MonthDisplayStyle.NUMERIC,
  ),
) { snappedDate -> }
```

## 12-hour time

```kotlin
WheelTimePicker(
  timeFormatter = timeFormatter(timeFormat = TimeFormat.AM_PM),
) { snappedTime -> }
```

## iOS-style wheel

```kotlin
WheelDateTimePicker(
  modifier = Modifier.size(280.dp, 240.dp),
  rows = WheelRows.Height(32.dp),
  barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 90f),
) { snappedDateTime -> }
```

See [Rows and barrel projection](rows-and-barrel.md) for the other options.

## Fully customized

```kotlin
WheelDateTimePicker(
  startDateTime = LocalDateTime(2026, 10, 20, 5, 30),
  minDateTime = LocalDateTime(2026, 1, 1, 0, 0),
  maxDateTime = LocalDateTime(2026, 12, 31, 23, 59),
  dateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.SHORT,
    cjkSuffixConfig = CjkSuffixConfig.HideAll,
  ),
  timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24),
  modifier = Modifier.size(200.dp, 100.dp),
  rows = WheelRows.Count(5),
  textStyle = MaterialTheme.typography.titleSmall,
  textColor = Color(0xFFffc300),
  selectedTextStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
  selectedTextColor = Color.Black,
  selectorProperties = WheelPickerDefaults.selectorProperties(
    enabled = true,
    shape = RoundedCornerShape(0.dp),
    color = Color(0xFFf1faee).copy(alpha = 0.2f),
    border = BorderStroke(2.dp, Color(0xFFf1faee)),
  ),
) { snappedDateTime -> }
```
