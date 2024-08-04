# datetime-wheel-picker (work-in-progress 👷🔧️👷‍♀️⛏)

[![badge-version-main]](https://search.maven.org/search?q=g:io.github.darkokoa%20a:datetime-wheel-picker*)
![badge-android][badge-android]
![badge-jvm][badge-jvm]
![badge-ios][badge-ios]
![badge-js][badge-js]

Compose Multiplatform implementation of [WheelPickerCompose](https://github.com/commandiron/WheelPickerCompose).

| Picker                                                                                                                                     | Usage                                                                   |
|--------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------|
| <img src="https://user-images.githubusercontent.com/50905347/201921058-82c7813d-b9c4-448c-a296-62465845152d.gif" width="256" height="256"> | ```WheelDateTimePicker { snappedDateTime -> }```                        |
| <img src="https://user-images.githubusercontent.com/50905347/201921069-14a8410b-5952-4130-80b0-71f9ca286a93.gif" width="256" height="256"> | ```WheelDatePicker { snappedDate -> }```                                |
| <img src="https://user-images.githubusercontent.com/50905347/201921066-b94b9fcd-c447-4b01-833f-03600e20ed44.gif" width="256" height="256"> | ```WheelTimePicker { snappedTime -> }```                                |
| <img src="https://user-images.githubusercontent.com/50905347/205661315-2eac971a-2dd9-41dc-93e7-de2be0514a9e.gif" width="256" height="256"> | ```WheelTimePicker(timeFormat = TimeFormat.AM_PM) { snappedTime -> }``` |

## Features

<table>
<tr>
<td>

```kotlin  
WheelDateTimePicker(
  startDateTime = LocalDateTime(
    year = 2025,
    monthNumber = 10,
    dayOfMonth = 20,
    hour = 5,
    minute = 30
  ),
  minDateTime = Clock.System
    .now()
    .toLocalDateTime(TimeZone.currentSystemDefault()),
  maxDateTime = LocalDateTime(
    year = 2025,
    monthNumber = 10,
    dayOfMonth = 20,
    hour = 5,
    minute = 30
  ),
  timeFormat = TimeFormat.AM_PM,
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

## Setup
datetime-wheel-picker has multiple active versions. The higher one uses the latest versions for Compose, while the others use only stable versions. Choose the one that matches your Compose version, considering this table:

| Compose Version   |                                                         datetime-wheel-picker Version                                                          |
|-------------------|:----------------------------------------------------------------------------------------------------------------------------------------------:|
| Compose 1.3 - 1.6 |                 [![badge-version-main]](https://central.sonatype.com/artifact/io.github.darkokoa/datetime-wheel-picker/1.0.0)                  |
| Compose 1.7       | [![badge-version-compose1.7-alpha01]](https://central.sonatype.com/artifact/io.github.darkokoa/datetime-wheel-picker/1.0.0-compose1.7-alpha01) |

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
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
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
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
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
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
```

## License

Released under the [Apache License, Version 2.0](https://opensource.org/license/apache-2-0).

## Thx

[WheelPickerCompose](https://github.com/commandiron/WheelPickerCompose)

[badge-version-main]: https://img.shields.io/maven-central/v/io.github.darkokoa/datetime-wheel-picker?versionSuffix=1.0.0&style=flat

[badge-version-compose1.7-alpha01]: https://img.shields.io/maven-central/v/io.github.darkokoa/datetime-wheel-picker?versionSuffix=compose1.7-alpha01&style=flat

[badge-android]: https://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat

[badge-jvm]: https://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat

[badge-ios]: https://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat

[badge-js]: https://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
