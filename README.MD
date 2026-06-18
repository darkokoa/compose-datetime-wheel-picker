# datetime-wheel-picker (work-in-progress 👷🔧️👷‍♀️⛏)

![badge-android][badge-android]
![badge-jvm][badge-jvm]
![badge-ios][badge-ios]
![badge-js][badge-js]
![badge-wasm][badge-wasm]

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) datetime picker implementation featuring **highly customizable** wheel pickers for date, time, and datetime selection.

| Picker                                                                                                                                     | Basic Usage                                                                                            |
|--------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| <img src="https://user-images.githubusercontent.com/50905347/201921058-82c7813d-b9c4-448c-a296-62465845152d.gif" width="256" height="256"> | ```WheelDateTimePicker(state = rememberWheelDateTimePickerState())```                                  |
| <img src="https://user-images.githubusercontent.com/50905347/201921069-14a8410b-5952-4130-80b0-71f9ca286a93.gif" width="256" height="256"> | ```WheelDatePicker(state = rememberWheelDatePickerState())```                                          |
| <img src="https://user-images.githubusercontent.com/50905347/201921066-b94b9fcd-c447-4b01-833f-03600e20ed44.gif" width="256" height="256"> | ```WheelTimePicker(state = rememberWheelTimePickerState())```                                          |
| <img src="https://user-images.githubusercontent.com/50905347/205661315-2eac971a-2dd9-41dc-93e7-de2be0514a9e.gif" width="256" height="256"> | ```WheelTimePicker(selectedTime = time, onTimeChange = { time = it })```                               |

## Key Features

### 🎯 Flexible Date Field Configuration
- **Customizable date order**: DMY (Day-Month-Year), MDY (Month-Day-Year), or YMD (Year-Month-Day)
- **Hide year picker**: Set `yearsRange = null` to create **day-month only** or **month-day only** pickers
- **Custom year range**: Limit year selection to specific ranges (e.g., next 10 years, last 50 years)
- **Multiple month display styles**: Full names, short names, or numeric format

### 🌍 Internationalization & Localization
- **Auto-adapts to locale**: Date order and month names automatically match the current locale
- **CJK language support**: Special handling for Chinese (年/月/日), Japanese (年/月/日), Korean (년/월/일) with customizable year/month/day suffixes
- **Localized numerals**: Supports Eastern Arabic numerals and other numeral systems
- **Script-aware locale matching**: A locale is matched first by its `language`+`script` subtags, then by `language` alone, and finally falls back to English. Examples: `uz-Arab` → `uz-Arab` (direct script match), `uz-Latn` → `uz` (script not bundled, base language used), `zh-Hant` → `zh` (same).
- **Limitations**: BCP 47 `-u-*` Unicode extension subtags (e.g. `-u-nu-*` numbering system, `-u-ca-*` calendar) are ignored — only the `language` and `script` subtags influence resolution.

**Currently Supported Languages (23):**
Arabic (العربية), Bengali (বাংলা), Chinese (中文), Dutch (Nederlands), English, French (Français), German (Deutsch), Hebrew (עברית), Hindi (हिन्दी), Indonesian (Bahasa Indonesia), Italian (Italiano), Japanese (日本語), Korean (한국어), Persian (فارسی), Polish (Polski), Portuguese (Português), Russian (Русский), Spanish (Español), Thai (ไทย), Turkish (Türkçe), Ukrainian (Українська), Uzbek (Oʻzbekcha / Ўзбекча / اۉزبېکچه — `uz`, `uz-Cyrl`, `uz-Arab`), Vietnamese (Tiếng Việt)

> **Contributions welcome**: If you find any translation errors or want to add support for a new language, please [open an issue](../../issues) or submit a pull request.

### 🎨 Visual Customization
- Configurable size, row count, text style, and colors
- Customizable selector appearance (shape, color, border)
- Material Design integration

### ⏰ Time Picker Features
- 12-hour (AM/PM) and 24-hour formats
- Customizable time display

## Common Use Cases

### State-based usage

For local UI state, create a picker state and pass it to the picker. The state object is the source of truth for this overload and does not take an `onChange` callback:

```kotlin
val state = rememberWheelTimePickerState()
val scope = rememberCoroutineScope()

WheelTimePicker(state = state)

Button(
  onClick = {
    scope.launch {
      state.animateScrollToTime(LocalTime(9, 30))
    }
  }
) {
  Text("09:30")
}
```

For MVI or externally managed screen state, use the controlled value overload:

```kotlin
WheelDatePicker(
  selectedDate = uiState.date,
  onDateChange = viewModel::onDateChange
)
```

The public picker API is intentionally limited to state-based and controlled value-based overloads.

<details>
<summary><b>Day-Month Picker (No Year)</b></summary>

Perfect for birthdays, anniversaries, or recurring events:

```kotlin
val state = rememberWheelDatePickerState(
  initialSelectedDate = LocalDate(2025, 6, 15),
  yearsRange = null,  // Hides the year picker
)

WheelDatePicker(
  state = state,
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.DMY,
    monthDisplayStyle = MonthDisplayStyle.FULL
  )
)
```

> **Note**: This uses the non-Composable `dateFormatter()` overload that accepts `dateOrder` parameter.

</details>

<details>
<summary><b>Month-Day Picker (US Format)</b></summary>

For US-style date input without year:

```kotlin
val state = rememberWheelDatePickerState(yearsRange = null)

WheelDatePicker(
  state = state,
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.MDY,
    monthDisplayStyle = MonthDisplayStyle.SHORT
  )
)
```
</details>

<details>
<summary><b>Limited Year Range</b></summary>

Restrict year selection to a specific range:

```kotlin
// Only allow next 10 years
WheelDatePicker(
  selectedDate = selectedDate,
  onDateChange = { selectedDate = it },
  yearsRange = IntRange(2025, 2035),
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.YMD
  )
)

// Only allow past 50 years (for birthdate)
val currentYear = Clock.System.now()
  .toLocalDateTime(TimeZone.currentSystemDefault()).year
WheelDatePicker(
  selectedDate = selectedDate,
  onDateChange = { selectedDate = it },
  yearsRange = IntRange(currentYear - 50, currentYear),
  dateFormatter = dateFormatter(dateOrder = DateOrder.DMY)
)
```
</details>

<details>
<summary><b>Chinese/Japanese/Korean Format</b></summary>

With native year-month-day suffixes:
- Chinese: "2025年1月15日"
- Korean: "2025년1월15일"
- Japanese: "2025年1月15日"

```kotlin
val state = rememberWheelDatePickerState()

WheelDatePicker(
  state = state,
  dateFormatter = dateFormatter(
    locale = Locale("zh"),  // "zh" for Chinese, "ja" for Japanese, "ko" for Korean
    monthDisplayStyle = MonthDisplayStyle.NUMERIC,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll
  )
)

// Without suffixes
WheelDatePicker(
  state = state,
  dateFormatter = dateFormatter(
    locale = Locale("zh"),
    monthDisplayStyle = MonthDisplayStyle.NUMERIC,
    cjkSuffixConfig = CjkSuffixConfig.HideAll
  )
)
```

> **Note**: This uses the Composable `dateFormatter()` overload. Date order (YMD for CJK) is auto-detected from locale.

</details>

<details>
<summary><b>Numeric Month Display</b></summary>

Show months as numbers instead of names:

```kotlin
val state = rememberWheelDatePickerState()

WheelDatePicker(
  state = state,
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.DMY,  // or MDY, YMD based on your preference
    monthDisplayStyle = MonthDisplayStyle.NUMERIC
  )
)
```
</details>

## Full Customization Example

<table>
<tr>
<td>

```kotlin  
val state = rememberWheelDateTimePickerState(
  initialSelectedDateTime = LocalDateTime(
    year = 2025,
    month = 10,
    day = 20,
    hour = 5,
    minute = 30
  ),
  minDateTime = Clock.System
    .now()
    .toLocalDateTime(TimeZone.currentSystemDefault()),
  maxDateTime = LocalDateTime(
    year = 2025,
    month = 10,
    day = 20,
    hour = 5,
    minute = 30
  ),
)

WheelDateTimePicker(
  state = state,
  dateFormatter = dateFormatter(
    locale = Locale.current, 
    monthDisplayStyle = MonthDisplayStyle.SHORT,
    cjkSuffixConfig = CjkSuffixConfig.HideAll
  ),
  timeFormatter = timeFormatter(
    timeFormat = TimeFormat.HOUR_24
  ),
  size = DpSize(200.dp, 100.dp),
  rowCount = 5,
  textStyle = MaterialTheme.typography.titleSmall,
  textColor = Color(0xFFffc300),
  selectorProperties = WheelPickerDefaults.selectorProperties(
    enabled = true,
    shape = RoundedCornerShape(0.dp),
    color = Color(0xFFf1faee).copy(alpha = 0.2f),
    border = BorderStroke(2.dp, Color(0xFFf1faee))
  )
)
```

</td>
<td>  

<img src="https://user-images.githubusercontent.com/50905347/201922097-86422287-cbd7-40ab-bf3c-5e0475828976.gif" width="256" height="256">

</td>
</tr>
</table>

## API Reference

### WheelDatePicker APIs

State-based overload:

```kotlin
WheelDatePicker(state = rememberWheelDatePickerState())
```

Value-based overload:

```kotlin
WheelDatePicker(
  selectedDate = selectedDate,
  onDateChange = { selectedDate = it },
)
```

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `state` | `WheelDatePickerState` | Required for state-based API | Picker state and programmatic scroll controller |
| `selectedDate` | `LocalDate` | Required for value-based API | Externally owned selected date |
| `onDateChange` | `(LocalDate) -> Unit` | Required for value-based API | Called when the user settles on a new date |
| `minDate` | `LocalDate` | `LocalDate.EPOCH` | Minimum selectable date |
| `maxDate` | `LocalDate` | `LocalDate.CYB3R_1N1T_ZOLL` | Maximum selectable date |
| `yearsRange` | `IntRange?` | `IntRange(minDate.year, maxDate.year)` | Year range to display. **Set to `null` to hide year picker** |
| `dateFormatter` | `DateFormatter` | Auto-detected | Controls date order, month style, and CJK suffixes |
| `size` | `DpSize` | `DpSize(256.dp, 128.dp)` | Picker dimensions |
| `rowCount` | `Int` | `3` | Number of visible rows in the wheel |
| `textStyle` | `TextStyle` | `MaterialTheme.typography.titleMedium` | Text styling |
| `textColor` | `Color` | `LocalContentColor.current` | Text color |
| `selectorProperties` | `SelectorProperties` | Default | Selector appearance (shape, color, border) |

### DateFormatter Options

**DateOrder** (controls field arrangement):
- `DateOrder.DMY` - Day, Month, Year (Europe, most of world)
- `DateOrder.MDY` - Month, Day, Year (US)
- `DateOrder.YMD` - Year, Month, Day (East Asia, ISO 8601)

**MonthDisplayStyle**:
- `MonthDisplayStyle.FULL` - "January", "February", etc.
- `MonthDisplayStyle.SHORT` - "Jan", "Feb", etc.
- `MonthDisplayStyle.NUMERIC` - "1", "2", etc.

**CjkSuffixConfig** (for Chinese/Japanese/Korean):
- `CjkSuffixConfig.ShowAll` - Shows year/month/day suffixes (Chinese/Japanese: 年/月/日, Korean: 년/월/일)
- `CjkSuffixConfig.HideAll` - Hides all suffixes
- Custom: `CjkSuffixConfig(showYearSuffix = true, showMonthSuffix = false, ...)`

### WheelTimePicker APIs

State-based overload:

```kotlin
WheelTimePicker(state = rememberWheelTimePickerState())
```

Value-based overload:

```kotlin
WheelTimePicker(
  selectedTime = selectedTime,
  onTimeChange = { selectedTime = it },
)
```

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `state` | `WheelTimePickerState` | Required for state-based API | Picker state and programmatic scroll controller |
| `selectedTime` | `LocalTime` | Required for value-based API | Externally owned selected time |
| `onTimeChange` | `(LocalTime) -> Unit` | Required for value-based API | Called when the user settles on a new time |
| `minTime` | `LocalTime` | `LocalTime.MIN` | Minimum selectable time |
| `maxTime` | `LocalTime` | `LocalTime.MAX` | Maximum selectable time |
| `timeFormatter` | `TimeFormatter` | Auto-detected | Controls 12/24 hour format (auto: AM/PM for en-US/GB, 24h for others) |
| `size` | `DpSize` | `DpSize(128.dp, 128.dp)` | Picker dimensions (narrower than date picker) |
| Other params | - | Same as `WheelDatePicker` | rowCount, textStyle, textColor, selectorProperties, etc. |

**TimeFormat**:
- `TimeFormat.HOUR_24` - 24-hour format (00:00 - 23:59)
- `TimeFormat.AM_PM` - 12-hour format with AM/PM

### WheelDateTimePicker APIs

State-based overload:

```kotlin
WheelDateTimePicker(state = rememberWheelDateTimePickerState())
```

Value-based overload:

```kotlin
WheelDateTimePicker(
  selectedDateTime = selectedDateTime,
  onDateTimeChange = { selectedDateTime = it },
)
```

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `state` | `WheelDateTimePickerState` | Required for state-based API | Picker state and programmatic scroll controller |
| `selectedDateTime` | `LocalDateTime` | Required for value-based API | Externally owned selected date-time |
| `onDateTimeChange` | `(LocalDateTime) -> Unit` | Required for value-based API | Called when the user settles on a new date-time |
| `minDateTime` | `LocalDateTime` | `LocalDateTime.EPOCH` | Minimum selectable date-time |
| `maxDateTime` | `LocalDateTime` | `LocalDateTime.CYB3R_1N1T_ZOLL` | Maximum selectable date-time |
| `yearsRange` | `IntRange?` | `IntRange(minDateTime.year, maxDateTime.year)` | Year range to display. **Set to `null` to hide year picker** |
| `dateFormatter` | `DateFormatter` | Auto-detected | Controls date order, month style, and CJK suffixes |
| `timeFormatter` | `TimeFormatter` | Auto-detected | Controls 12/24 hour format |
| `size` | `DpSize` | `DpSize(256.dp, 128.dp)` | Picker dimensions |
| Other params | - | Same as `WheelDatePicker` | rowCount, textStyle, textColor, selectorProperties |

## Setup

[![Maven Central](https://img.shields.io/maven-central/v/io.github.darkokoa/datetime-wheel-picker?style=flat)](https://central.sonatype.com/artifact/io.github.darkokoa/datetime-wheel-picker)

- Add the Maven Central repository if it is not already there:

```kotlin
repositories {
  mavenCentral()
}
```

- In Compose multiplatform projects, add a dependency to the commonMain source set dependencies:

```kotlin
kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("io.github.darkokoa:datetime-wheel-picker:<version>")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
      }
    }
  }
}

```

- To use the library in a single-platform project (such as Android project), add a dependency to the dependencies
  block:

```kotlin
dependencies {
  implementation("io.github.darkokoa:datetime-wheel-picker:<version>")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
}
```

- If your minimum Android platform's API level (minSdk) < 26, please
  enable [Desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) like this:

```kotlin
compileOptions {
  isCoreLibraryDesugaringEnabled = true
}

//...

dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
```

## License

Released under the [Apache License, Version 2.0](https://opensource.org/license/apache-2-0).

## Thx

[WheelPickerCompose](https://github.com/commandiron/WheelPickerCompose)

[badge-android]: https://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat

[badge-jvm]: https://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat

[badge-ios]: https://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat

[badge-js]: https://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat

[badge-wasm]: https://img.shields.io/badge/platform-wasm-654FF0.svg?style=flat
