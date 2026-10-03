# Datetime Wheel Picker

[![Maven Central](https://img.shields.io/maven-central/v/io.github.darkokoa/datetime-wheel-picker?style=flat)](https://central.sonatype.com/artifact/io.github.darkokoa/datetime-wheel-picker)
[![Build](https://img.shields.io/github/actions/workflow/status/darkokoa/datetime-wheel-picker/build.yml?branch=main&style=flat)](https://github.com/darkokoa/datetime-wheel-picker/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](LICENSE)

![badge-android][badge-android]
![badge-jvm][badge-jvm]
![badge-ios][badge-ios]
![badge-js][badge-js]
![badge-wasm][badge-wasm]

[English](README.md) | **简体中文**

基于 [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) 的**日期**、**时间**和**日期时间**滚轮选择器,高度可定制。滚轮的行排布在圆柱面("barrel")上,效果接近 iOS 原生选择器,并内置 30 种语言。

> [!NOTE]
> 本文档对应 `main` 分支,即 **1.5.0(尚未发布)**。`rows` 和 `barrelProperties` 参数需要 1.5.0;当前最新发布版本是 1.4.0(该版本使用 `rowCount`)。详见 [MIGRATION.md](MIGRATION.md) 和 [CHANGELOG.md](CHANGELOG.md)(均为英文)。

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="docs/images/hero-dark.png">
  <img alt="WheelDateTimePicker、iOS 风格的 WheelDatePicker 和带 AM/PM 的 WheelTimePicker" src="docs/images/hero-light.png" width="800">
</picture>

| 选择器 | 基本用法 |
|--------|----------|
| 日期时间 | `WheelDateTimePicker { snappedDateTime -> }` |
| 日期 | `WheelDatePicker { snappedDate -> }` |
| 时间(24 小时制) | `WheelTimePicker { snappedTime -> }` |
| 时间(AM/PM) | `WheelTimePicker(timeFormatter = timeFormatter(timeFormat = TimeFormat.AM_PM)) { snappedTime -> }` |

**[在浏览器中体验](https://darkokoa.github.io/datetime-wheel-picker/)**:示例应用的 Wasm 版本,包含所有选择器和 barrel 的各种变体。

## 特性

- **日期、时间、日期时间滚轮**,另有通用的 `WheelTextPicker`,可用于自定义数据。
- **Barrel 圆柱投影**:行绕圆柱面弯曲,边缘角度(rim angle)和边缘淡出均可配置,从平面列表到 iOS 的完整半圆柱都能实现。
- **固定行数或固定行高**:`WheelRows.Count(n)` 或 `WheelRows.Height(h)`。
- **由 Modifier 决定尺寸**:`fillMaxWidth()`、`size()`、`weight()` 等都可直接使用。
- **灵活的日期字段**:支持日-月-年、月-日-年、年-月-日三种顺序;隐藏年份即可得到"日-月"或"月-日"选择器;可限制可选范围。
- **本地化**:30 种语言,按区域自动选择日期顺序和 12/24 小时制,支持中日韩的年/月/日后缀以及本地化数字。
- **样式可定制**:文字样式和颜色(选中项可单独设置)、选择框的形状、颜色和边框,并与 Material 3 集成。
- **回调清晰**:滚动过程中实时回调,滚轮停下后再回调一次最终值。
- **Kotlin Multiplatform**:Android、iOS、桌面(JVM)、JS 和 Wasm。

## 安装

在版本目录(`gradle/libs.versions.toml`)中添加依赖:

```toml
[versions]
datetime-wheel-picker = "1.4.0" # 1.5.0 发布后改为 1.5.0,见上方说明
kotlinx-datetime = "0.8.0"

[libraries]
datetime-wheel-picker = { module = "io.github.darkokoa:datetime-wheel-picker", version.ref = "datetime-wheel-picker" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

在 Compose Multiplatform 项目中,添加到 `commonMain`:

```kotlin
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.datetime.wheel.picker)
      implementation(libs.kotlinx.datetime) // 选择器的 API 使用其中的 LocalDate、LocalTime 等类型
    }
  }
}
```

在单平台项目(例如 Android)中,写在 `dependencies` 块里:

```kotlin
dependencies {
  implementation(libs.datetime.wheel.picker)
  implementation(libs.kotlinx.datetime)
}
```

构件发布在 Maven Central,请确认仓库中包含 `mavenCentral()`。

<details>
<summary><b>不使用版本目录</b></summary>

```kotlin
implementation("io.github.darkokoa:datetime-wheel-picker:<version>")
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
```

</details>

<details>
<summary><b>Android minSdk 低于 26</b></summary>

请启用[核心库脱糖](https://developer.android.com/studio/write/java8-support#library-desugaring):

```kotlin
android {
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
  }
}

dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
```

</details>

### 兼容性

| 库版本 | Kotlin | Compose Multiplatform | kotlinx-datetime | Android minSdk |
|--------|--------|-----------------------|------------------|----------------|
| 1.5.0(`main`) | 2.4.20 | 1.12.1 | 0.8.0 | 21 |
| 1.4.0 | 2.4.0 | 1.11.1 | 0.8.0 | 21 |

表中是各版本的构建所用版本。支持的 target:`android`、`jvm`、`iosArm64`、`iosSimulatorArm64`、`js` 和 `wasmJs`。

## 快速开始

```kotlin
import dev.darkokoa.datetimewheelpicker.WheelDatePicker
import dev.darkokoa.datetimewheelpicker.WheelDateTimePicker
import dev.darkokoa.datetimewheelpicker.WheelTimePicker

@Composable
fun Pickers() {
  // 最终值,滚轮停下后回调一次
  WheelDatePicker { snappedDate -> /* LocalDate */ }

  WheelTimePicker { snappedTime -> /* LocalTime */ }

  WheelDateTimePicker { snappedDateTime -> /* LocalDateTime */ }

  // 滚动过程中的实时值,以及最终值
  WheelDatePicker(
    onSnappedDateChanged = { date -> /* 预览 */ },
    onSnappedDate = { date -> /* 提交 */ },
  )
}
```

限制可选范围、指定初始值,并用 `Modifier` 设置尺寸:

```kotlin
WheelDatePicker(
  modifier = Modifier.fillMaxWidth().height(200.dp),
  startDate = LocalDate(2026, 10, 20),
  minDate = LocalDate(2026, 1, 1),
  maxDate = LocalDate(2026, 12, 31),
) { snappedDate -> }
```

## Barrel 与行数

行被投影到一个竖直圆柱面上。`rows` 决定保持行数不变还是行高不变,`barrelProperties` 决定圆柱的弯曲和淡出程度:

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="docs/images/rim-angle-dark.png">
  <img alt="同一个时间选择器在 rimAngle 为 0、45、90 度以及默认值下的效果" src="docs/images/rim-angle-light.png" width="640">
</picture>

```kotlin
// 三行,轻微弯曲(默认)
WheelDatePicker { }

// 从上边缘到下边缘恰好五行
WheelDatePicker(rows = WheelRows.Count(5)) { }

// iOS 风格:32.dp 行高,能放几行放几行,完整半圆柱
WheelDateTimePicker(
  modifier = Modifier.size(280.dp, 240.dp),
  rows = WheelRows.Height(32.dp),
  barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 90f),
) { }

// 平面列表,所有行都不透明
WheelDatePicker(
  barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 0f, fadeStrength = 0f),
) { }
```

默认值和计算细节见 [Rows and barrel projection](docs/rows-and-barrel.md)(英文)。

## 自定义

```kotlin
WheelDateTimePicker(
  dateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.SHORT,
    cjkSuffixConfig = CjkSuffixConfig.HideAll,
  ),
  timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24),
  textStyle = MaterialTheme.typography.titleSmall,
  textColor = Color(0xFFffc300),
  selectedTextStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
  selectedTextColor = Color.Black,
  selectorProperties = WheelPickerDefaults.selectorProperties(
    shape = RoundedCornerShape(0.dp),
    color = Color(0xFFf1faee).copy(alpha = 0.2f),
    border = BorderStroke(2.dp, Color(0xFFf1faee)),
  ),
) { snappedDateTime -> }
```

更多配置,例如不带年份的"日-月"选择器、生日范围、中日韩后缀等,见 [Recipes](docs/recipes.md)(英文)。

## 本地化

选择器跟随 `Locale.current`:月份名称、AM/PM 文本、日期顺序(例如 `en-US` 为月-日-年,中日韩为年-月-日)、12/24 小时制以及数字。共支持 30 种语言,包括阿拉伯语、中文、英语、法语、德语、印地语、日语、韩语、葡萄牙语、俄语、西班牙语等。完整列表和区域匹配规则见 [Localization](docs/localization.md)(英文)。

发现翻译错误,或希望增加新语言?欢迎[提交 issue](https://github.com/darkokoa/datetime-wheel-picker/issues) 或 pull request。

## 文档

| | |
|---|---|
| [API reference](docs/api.md) | 参数、默认值、回调和格式化器 |
| [Rows and barrel projection](docs/rows-and-barrel.md) | `WheelRows`、边缘角度和淡出 |
| [Sizing](docs/sizing.md) | `Modifier` 约束如何决定选择器尺寸 |
| [Recipes](docs/recipes.md) | 可直接使用的配置示例 |
| [Localization](docs/localization.md) | 语言、区域匹配、数字 |
| [Migration guide](MIGRATION.md) | 版本升级指南 |
| [Changelog](CHANGELOG.md) | 各版本的变更 |

`docs/` 下的页面目前只有英文版。

## 示例应用

`sample/` 模块是一个 Compose Multiplatform 应用,包含所有选择器的演示(含 barrel 的各种变体)。Wasm 版本托管在 https://darkokoa.github.io/datetime-wheel-picker/ ,每次推送到 `main` 都会自动重新部署(见 [`pages.yml`](.github/workflows/pages.yml))。

想自己运行的话,用 Android Studio 或 IntelliJ IDEA 打开项目后,可以运行 `sample:androidApp`、`sample/iosApp` 中的 iOS 应用,或桌面入口(`sample/composeApp/src/jvmMain` 下的 `main.kt`)。本地运行 Web 版本:

```shell
./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun
```

## 参与贡献

欢迎提交 issue 和 pull request。提交 pull request 前请先运行 `./gradlew check`。

## 许可证

基于 [Apache License, Version 2.0](LICENSE) 发布。

## 致谢

灵感来自 [WheelPickerCompose](https://github.com/commandiron/WheelPickerCompose)。
Barrel 投影基于 [@bnrdk](https://github.com/bnrdk) 的贡献([#150](https://github.com/darkokoa/datetime-wheel-picker/pull/150))。

[badge-android]: https://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-jvm]: https://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat
[badge-ios]: https://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: https://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-wasm]: https://img.shields.io/badge/platform-wasmJs-654FF0.svg?style=flat
