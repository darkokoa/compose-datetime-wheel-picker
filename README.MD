# datetime-wheel-picker (work-in-progress 👷🔧️👷‍♀️⛏)

![badge-android][badge-android]
![badge-jvm][badge-jvm]
![badge-ios][badge-ios]
![badge-js][badge-js]
![badge-wasm][badge-wasm]

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) datetime picker implementation featuring **highly customizable** wheel pickers for date, time, and datetime selection.

| Picker                                                                                                                                     | Basic Usage                                                                                            |
|--------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| <img src="https://user-images.githubusercontent.com/50905347/201921058-82c7813d-b9c4-448c-a296-62465845152d.gif" width="256" height="256"> | ```WheelDateTimePicker { snappedDateTime -> }```                                                       |
| <img src="https://user-images.githubusercontent.com/50905347/201921069-14a8410b-5952-4130-80b0-71f9ca286a93.gif" width="256" height="256"> | ```WheelDatePicker { snappedDate -> }```                                                               |
| <img src="https://user-images.githubusercontent.com/50905347/201921066-b94b9fcd-c447-4b01-833f-03600e20ed44.gif" width="256" height="256"> | ```WheelTimePicker { snappedTime -> }```                                                               |
| <img src="https://user-images.githubusercontent.com/50905347/205661315-2eac971a-2dd9-41dc-93e7-de2be0514a9e.gif" width="256" height="256"> | ```WheelTimePicker(timeFormatter = timeFormatter(timeFormat = TimeFormat.AM_PM)) { snappedTime -> }``` |

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

**Currently Supported Languages (22):**
Arabic (العربية), Bengali (বাংলা), Chinese (中文), Dutch (Nederlands), English, French (Français), German (Deutsch), Hebrew (עברית), Hindi (हिन्दी), Indonesian (Bahasa Indonesia), Italian (Italiano), Japanese (日本語), Korean (한국어), Persian (فارسی), Polish (Polski), Portuguese (Português), Russian (Русский), Spanish (Español), Thai (ไทย), Turkish (Türkçe), Ukrainian (Українська), Vietnamese (Tiếng Việt)

> **Contributions welcome**: If you find any translation errors or want to add support for a new language, please [open an issue](../../issues) or submit a pull request.

### 🎨 Visual Customization
- Configurable size, row count, text style, and colors
- Customizable selector appearance (shape, color, border)
- Material Design integration

### ⏰ Time Picker Features
- 12-hour (AM/PM) and 24-hour formats
- Customizable time display

## Common Use Cases

<details>
<summary><b>Day-Month Picker (No Year)</b></summary>

Perfect for birthdays, anniversaries, or recurring events:

```kotlin
WheelDatePicker(
  startDate = LocalDate(2025, 6, 15),
  yearsRange = null,  // Hides the year picker
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.DMY,
    monthDisplayStyle = MonthDisplayStyle.FULL
  )
) { snappedDate ->
  // snappedDate.month and snappedDate.dayOfMonth
}
```

> **Note**: This uses the non-Composable `dateFormatter()` overload that accepts `dateOrder` parameter.

</details>

<details>
<summary><b>Month-Day Picker (US Format)</b></summary>

For US-style date input without year:

```kotlin
WheelDatePicker(
  yearsRange = null,
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.MDY,
    monthDisplayStyle = MonthDisplayStyle.SHORT
  )
) { snappedDate -> }
```
</details>

<details>
<summary><b>Limited Year Range</b></summary>

Restrict year selection to a specific range:

```kotlin
// Only allow next 10 years
WheelDatePicker(
  yearsRange = IntRange(2025, 2035),
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.YMD
  )
) { snappedDate -> }

// Only allow past 50 years (for birthdate)
val currentYear = Clock.System.now()
  .toLocalDateTime(TimeZone.currentSystemDefault()).year
WheelDatePicker(
  yearsRange = IntRange(currentYear - 50, currentYear),
  dateFormatter = dateFormatter(dateOrder = DateOrder.DMY)
) { snappedDate -> }
```
</details>

<details>
<summary><b>Chinese/Japanese/Korean Format</b></summary>

With native year-month-day suffixes:
- Chinese: "2025年1月15日"
- Korean: "2025년1월15일"
- Japanese: "2025年1月15日"

```kotlin
WheelDatePicker(
  dateFormatter = dateFormatter(
    locale = Locale("zh"),  // "zh" for Chinese, "ja" for Japanese, "ko" for Korean
    monthDisplayStyle = MonthDisplayStyle.NUMERIC,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll
  )
) { snappedDate -> }

// Without suffixes
WheelDatePicker(
  dateFormatter = dateFormatter(
    locale = Locale("zh"),
    monthDisplayStyle = MonthDisplayStyle.NUMERIC,
    cjkSuffixConfig = CjkSuffixConfig.HideAll
  )
) { snappedDate -> }
```

> **Note**: This uses the Composable `dateFormatter()` overload. Date order (YMD for CJK) is auto-detected from locale.

</details>

<details>
<summary><b>Numeric Month Display</b></summary>

Show months as numbers instead of names:

```kotlin
WheelDatePicker(
  dateFormatter = dateFormatter(
    dateOrder = DateOrder.DMY,  // or MDY, YMD based on your preference
    monthDisplayStyle = MonthDisplayStyle.NUMERIC
  )
) { snappedDate -> }
```
</details>

## Full Customization Example

<table>
<tr>
<td>

```kotlin  
WheelDateTimePicker(
  startDateTime = LocalDateTime(
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
) { snappedDateTime -> }
```

</td>
<td>  

<img src="https://user-images.githubusercontent.com/50905347/201922097-86422287-cbd7-40ab-bf3c-5e0475828976.gif" width="256" height="256">

</td>
</tr>
</table>

## API Reference

### WheelDatePicker Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startDate` | `LocalDate` | `LocalDate.now()` | Initial selected date |
| `minDate` | `LocalDate` | `LocalDate.EPOCH` | Minimum selectable date |
| `maxDate` | `LocalDate` | `LocalDate.CYB3R_1N1T_ZOLL` | Maximum selectable date |
| `yearsRange` | `IntRange?` | `IntRange(minDate.year, maxDate.year)` | Year range to display. **Set to `null` to hide year picker** |
| `dateFormatter` | `DateFormatter` | Auto-detected | Controls date order, month style, and CJK suffixes |
| `size` | `DpSize` | `DpSize(256.dp, 128.dp)` | Picker dimensions |
| `rowCount` | `Int` | `3` | Number of visible rows in the wheel |
| `textStyle` | `TextStyle` | `MaterialTheme.typography.titleMedium` | Text styling |
| `textColor` | `Color` | `LocalContentColor.current` | Text color |
| `selectorProperties` | `SelectorProperties` | Default | Selector appearance (shape, color, border) |
| `onSnappedDate` | `(LocalDate) -> Unit` | `{}` | Callback when date is selected |

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

### WheelTimePicker Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startTime` | `LocalTime` | `LocalTime.now()` | Initial selected time |
| `minTime` | `LocalTime` | `LocalTime.MIN` | Minimum selectable time |
| `maxTime` | `LocalTime` | `LocalTime.MAX` | Maximum selectable time |
| `timeFormatter` | `TimeFormatter` | Auto-detected | Controls 12/24 hour format (auto: AM/PM for en-US/GB, 24h for others) |
| `size` | `DpSize` | `DpSize(128.dp, 128.dp)` | Picker dimensions (narrower than date picker) |
| Other params | - | Same as `WheelDatePicker` | rowCount, textStyle, textColor, selectorProperties, etc. |

**TimeFormat**:
- `TimeFormat.HOUR_24` - 24-hour format (00:00 - 23:59)
- `TimeFormat.AM_PM` - 12-hour format with AM/PM

## Setup

[![Maven Central](https://img.shields.io/maven-central/v/io.github.darkokoa/datetime-wheel-picker?style=flat)](https://central.sonatype.com/artifact/io.github.darkokoa/datetime-wheel-picker/1.1.0)

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
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
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
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
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
