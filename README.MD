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
- **Script-aware locale matching**: A locale is matched first by its `language`+`script` subtags, then by `language` alone, and finally falls back to English. Examples: `uz-Arab` → `uz-Arab` (direct script match), `uz-Latn` → `uz` (script not bundled, base language used), `zh-Hant` → `zh` (same).
- **Limitations**: BCP 47 `-u-*` Unicode extension subtags (e.g. `-u-nu-*` numbering system, `-u-ca-*` calendar) are ignored — only the `language` and `script` subtags influence resolution.

**Currently Supported Languages (30):**
Arabic (العربية), Bengali (বাংলা), Chinese (中文), Czech (Čeština), Danish (Dansk), Dutch (Nederlands), English, Finnish (Suomi), French (Français), German (Deutsch), Greek (Ελληνικά), Hebrew (עברית), Hindi (हिन्दी), Indonesian (Bahasa Indonesia), Italian (Italiano), Japanese (日本語), Korean (한국어), Norwegian (Norsk — `nb`, `no`), Persian (فارسی), Polish (Polski), Portuguese (Português), Romanian (Română), Russian (Русский), Spanish (Español), Swedish (Svenska), Thai (ไทย), Turkish (Türkçe), Ukrainian (Українська), Uzbek (Oʻzbekcha / Ўзбекча / اۉزبېکچه — `uz`, `uz-Cyrl`, `uz-Arab`), Vietnamese (Tiếng Việt)

> **Contributions welcome**: If you find any translation errors or want to add support for a new language, please [open an issue](../../issues) or submit a pull request.

### 🎨 Visual Customization
- Modifier-driven responsive sizing, fixed row count or fixed row height, inactive and selected text styles, and colors
- Customizable selector appearance (shape, color, border)
- Cylindrical barrel projection with a configurable rim angle
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
  modifier = Modifier.size(200.dp, 100.dp),
  rows = WheelRows.Count(5),
  textStyle = MaterialTheme.typography.titleSmall,
  textColor = Color(0xFFffc300),
  selectedTextStyle = MaterialTheme.typography.titleSmall.copy(
    fontWeight = FontWeight.Bold
  ),
  selectedTextColor = Color.Black,
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

## Sizing

Picker size is controlled entirely through `Modifier` (since 1.4.0). When the caller does not
constrain an axis, the picker supplies its intrinsic default on that axis: 256.dp width for
`WheelDatePicker`/`WheelDateTimePicker`, 128.dp for `WheelTimePicker`/`WheelTextPicker`, and a
height that follows `rows`: ~42.7.dp per row for `WheelRows.Count` (the default `Count(3)` →
exactly 128.dp), or seven rows for `WheelRows.Height`. Larger counts grow the wheel instead of
squeezing rows.

```kotlin
WheelDatePicker { }                                          // intrinsic 256 x 128.dp
WheelDatePicker(rows = WheelRows.Count(5)) { }               // intrinsic height ~213.dp
WheelDatePicker(rows = WheelRows.Height(32.dp)) { }          // intrinsic height 224.dp
WheelDatePicker(modifier = Modifier.fillMaxWidth()) { }      // parent width, intrinsic height
WheelDatePicker(modifier = Modifier.height(200.dp)) { }      // fixed height
WheelDatePicker(modifier = Modifier.size(300.dp, 160.dp)) { }        // fixed size
WheelDatePicker(
  modifier = Modifier
    .widthIn(min = 240.dp, max = 400.dp)
    .heightIn(min = 128.dp),
) { }
```

Standard Compose constraint rules apply: fixed/min/max constraints from the `modifier` or the
parent override or clamp the intrinsic default, and pickers shrink to fit parents narrower than
their intrinsic width.

**Migrating from `size: DpSize` (removed in 1.4.0, source-breaking only — hidden 1.3.x overloads
keep old binaries linking until the next major release):**

```kotlin
// Before
WheelDatePicker(size = DpSize(300.dp, 160.dp)) { }
// After
WheelDatePicker(modifier = Modifier.size(300.dp, 160.dp)) { }

// Before workaround for responsive width
BoxWithConstraints(Modifier.fillMaxWidth()) {
  WheelDatePicker(size = DpSize(maxWidth, 200.dp)) { }
}
// After
WheelDatePicker(modifier = Modifier.fillMaxWidth().height(200.dp)) { }
```

For more than three rows, callers that depended on the old squeezed 128.dp total height should
state it explicitly with `Modifier.height(128.dp)`.

> **Known limitation**: the picker resolves its size via subcomposition and does not support
> intrinsic-measurement parents (`IntrinsicSize.Min`/`Max` will throw). Pass an explicit
> `width`/`height` instead.

## Rows

`rows` decides how the wheel divides its height. Together with the height and the barrel angle it
fixes the geometry of the drum, and you choose which quantity stays constant:

```kotlin
WheelDatePicker(rows = WheelRows.Count(5)) { }        // exactly five rows from rim to rim
WheelDatePicker(rows = WheelRows.Height(32.dp)) { }   // 32.dp rows, as many as fit
```

- **`WheelRows.Count(n)`** (default `Count(3)`) shows exactly `n` rows on the drum from rim to
  rim, the outermost foreshortened against the edge. The row height follows from the picker
  height, so a taller picker gets taller rows, and the selector is sized to the centered row.
  `n` is always odd so the selected row sits at the center with whole rows on both sides; an
  even `n` is rounded up (`Count(4)` behaves as `Count(5)`).
- **`WheelRows.Height(h)`** keeps every row and the selector `h` tall whatever the picker height,
  and shows as many rows as fit; the number is usually fractional, with the outermost rows cut off
  at the rim exactly like a native iOS picker. Use it when you want the wheel to grow and shrink
  with its container without the text changing size. iOS uses 32pt rows in a 216pt picker.

## Barrel projection

Wheel rows are laid out on a vertical cylinder, giving the wheel the curved, drum-like appearance
of a native picker.

How strongly the drum bends is controlled by `rimAngle`, the rotation in degrees of the drum
surface where it meets the top and bottom edges of the viewport. When you do not pass
`barrelProperties`, the picker uses `WheelPickerDefaults.barrelPropertiesFor(rows)`. For
`WheelRows.Count` that adds 13° per row away from the center and caps at 70°, so a 3-row wheel
stays gently curved while a 7-row or taller wheel gets the full drum with every row readable. For
`WheelRows.Height` it is the full 90°, since no row count is promised. Pass
`WheelPickerDefaults.barrelProperties(rimAngle)`
with a value in `[0, 90]` to fix it. `90` shows the full half cylinder, matching iOS, at the cost
of the outermost rows becoming nearly unreadable against the rim; `0` is a flat, evenly spaced
wheel with no projection at all. `fadeStrength`, in `[0, 1]`, scales how much rows dim as they turn toward
the rim: `1` (default) is a `cos²` falloff, `0` keeps every row opaque. The fade follows the
angle, so it has no effect on a flat wheel.

```kotlin
WheelDateTimePicker(
  modifier = Modifier.height(240.dp),
  rows = WheelRows.Count(11),
  barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 90f, fadeStrength = 0.8f),
) { snappedDateTime -> }
```

The picker always occupies exactly the height you give it (or its intrinsic default); the
cylinder is sized to fit. With `WheelRows.Count` the outer rows are compressed, so the centered
row gets more room than `height / count` and the selector is sized to match it; with
`WheelRows.Height` the row and selector height are fixed and the angle only changes how many rows
are visible. Sizing, snapping, and callbacks are unaffected by the angle. The same parameter is
available on `WheelDatePicker`,
`WheelTimePicker`, and `WheelTextPicker`.

## API Reference

### WheelDatePicker Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startDate` | `LocalDate` | `LocalDate.now()` | Initial selected date |
| `minDate` | `LocalDate` | `LocalDate.EPOCH` | Minimum selectable date |
| `maxDate` | `LocalDate` | `LocalDate.CYB3R_1N1T_ZOLL` | Maximum selectable date |
| `yearsRange` | `IntRange?` | `IntRange(minDate.year, maxDate.year)` | Year range to display. **Set to `null` to hide year picker** |
| `dateFormatter` | `DateFormatter` | Auto-detected | Controls date order, month style, and CJK suffixes |
| `modifier` | `Modifier` | `Modifier` | Sizing and placement. Unconstrained axes use the intrinsic default (256.dp × 128.dp at 3 rows). See [Sizing](#sizing) |
| `rows` | `WheelRows` | `WheelRows.Count(3)` | Exact (odd) row count, or a fixed row height. See [Rows](#rows) |
| `textStyle` | `TextStyle` | `MaterialTheme.typography.titleMedium` | Text styling for inactive items |
| `textColor` | `Color` | `LocalContentColor.current` | Text color for inactive items |
| `selectedTextStyle` | `TextStyle` | `textStyle` | Text styling for the selected (centered) item |
| `selectedTextColor` | `Color` | `textColor` | Text color for the selected (centered) item |
| `selectorProperties` | `SelectorProperties` | Default | Selector appearance (shape, color, border) |
| `barrelProperties` | `BarrelProperties` | `WheelPickerDefaults.barrelPropertiesFor(rows)` | Rim angle and edge fade of the cylindrical row projection. See [Barrel projection](#barrel-projection) |
| `onSnappedDateChanged` | `(LocalDate) -> Unit` | `{}` | Callback fired **during scrolling** every time the snapped date changes (live updates) |
| `onSnappedDate` | `(LocalDate) -> Unit` | `{}` | Callback fired **when scrolling settles** on the final selected date |

> **`onSnappedDateChanged` vs `onSnappedDate`**
> - `onSnappedDateChanged` is invoked continuously while the user is scrolling, each time a different item snaps into the selector. Useful for live previews, syncing UI, or tracking the in-flight value.
> - `onSnappedDate` is invoked only once after the wheel comes to rest, representing the user's final choice. Use it for committing the selection (saving, navigating, etc.).
>
> The same pattern applies to `WheelTimePicker` (`onSnappedTimeChanged` / `onSnappedTime`) and `WheelDateTimePicker` (`onSnappedDateTimeChanged` / `onSnappedDateTime`).

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
| `modifier` | `Modifier` | `Modifier` | Sizing and placement. Intrinsic default 128.dp wide (narrower than date picker). See [Sizing](#sizing) |
| Other params | - | Same as `WheelDatePicker` | rows, textStyle, textColor, selectedTextStyle, selectedTextColor, selectorProperties, barrelProperties, etc. |

**TimeFormat**:
- `TimeFormat.HOUR_24` - 24-hour format (00:00 - 23:59)
- `TimeFormat.AM_PM` - 12-hour format with AM/PM

### WheelDateTimePicker Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startDateTime` | `LocalDateTime` | `LocalDateTime.now()` | Initial selected date-time |
| `minDateTime` | `LocalDateTime` | `LocalDateTime.EPOCH` | Minimum selectable date-time |
| `maxDateTime` | `LocalDateTime` | `LocalDateTime.CYB3R_1N1T_ZOLL` | Maximum selectable date-time |
| `yearsRange` | `IntRange?` | `IntRange(minDateTime.year, maxDateTime.year)` | Year range to display. **Set to `null` to hide year picker** |
| `dateFormatter` | `DateFormatter` | Auto-detected | Controls date order, month style, and CJK suffixes |
| `timeFormatter` | `TimeFormatter` | Auto-detected | Controls 12/24 hour format |
| `modifier` | `Modifier` | `Modifier` | Sizing and placement. Intrinsic default 256.dp × 128.dp at 3 rows. See [Sizing](#sizing) |
| `onSnappedDateTimeChanged` | `(LocalDateTime) -> Unit` | `{}` | Callback fired **during scrolling** every time the snapped date-time changes (live updates) |
| `onSnappedDateTime` | `(LocalDateTime) -> Unit` | `{}` | Callback fired **when scrolling settles** on the final selected date-time |
| Other params | - | Same as `WheelDatePicker` | rows, textStyle, textColor, selectedTextStyle, selectedTextColor, selectorProperties, barrelProperties |

### Styling the selected item

Each picker accepts `selectedTextStyle` / `selectedTextColor` to style the **centered (snapped) item** differently from inactive items. By default, they use the same values as `textStyle` / `textColor`.

```kotlin
WheelDatePicker(
  textStyle = MaterialTheme.typography.titleMedium,
  textColor = LocalContentColor.current,
  selectedTextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
  selectedTextColor = MaterialTheme.colorScheme.primary,
)
```

> `selectedTextColor` overrides `selectedTextStyle.color`, mirroring how `Text(color = ...)` overrides `TextStyle.color` in Compose. The same parameters are available on `WheelTimePicker`, `WheelDateTimePicker`, and the internal text pickers.

> In `WheelTimePicker` and `WheelDateTimePicker`, the colon separator sits in the center row next to the selected hour and minute, so it follows `selectedTextStyle` / `selectedTextColor`. If you don't set the `selected*` params (defaults equal `textStyle` / `textColor`), the colon is unchanged.

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
