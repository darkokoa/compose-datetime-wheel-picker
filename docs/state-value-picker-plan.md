# Picker state-based / value-based API 重构计划

## 目标

`datetime-wheel-picker` 的公开 picker API 只保留两种入口：

1. state-based：调用方创建 `Wheel...PickerState`，再传给 `Wheel...Picker(state = state, ...)`。
2. value-based：调用方传入外部权威值和提交事件，例如 `selectedDate` / `onDateChange`。

旧的 `startDate`、`startTime`、`startDateTime`、`onSnapped*Changed`、`onSnapped*` 公开入口直接移除，不再提供兼容 wrapper、deprecated overload 或 trailing-lambda API。

## 背景

旧 API 把初始值和滚动事件混在同一层：

- `start*` 只能稳定表达首次 composition 的初始值，不能作为外部状态源。
- `onSnapped*Changed` 暴露滚动中的临时值，容易被当成提交事件使用。
- `onSnapped*` 是提交事件，但命名和 `onSnapped*Changed` 过于接近。
- DateTime picker 的合法性必须以完整 `LocalDateTime` 判断，分散回调会放大中间非法状态的风险。

新的实现把滚动协调和选择值放进 plain state holder。value-based overload 只是受控组件入口，内部复用 state holder，不再把旧事件模型暴露给用户。

## 公开 API

### Date

```kotlin
@Composable
fun WheelDatePicker(
  state: WheelDatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DateFormatter = dateFormatter(...),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
)

@Composable
fun WheelDatePicker(
  selectedDate: LocalDate,
  onDateChange: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
  dateFormatter: DateFormatter = dateFormatter(...),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
)
```

### Time

```kotlin
@Composable
fun WheelTimePicker(
  state: WheelTimePickerState,
  modifier: Modifier = Modifier,
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  size: DpSize = DpSize(128.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
)

@Composable
fun WheelTimePicker(
  selectedTime: LocalTime,
  onTimeChange: (LocalTime) -> Unit,
  modifier: Modifier = Modifier,
  minTime: LocalTime = LocalTime.MIN,
  maxTime: LocalTime = LocalTime.MAX,
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  size: DpSize = DpSize(128.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
)
```

### DateTime

```kotlin
@Composable
fun WheelDateTimePicker(
  state: WheelDateTimePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DateFormatter = dateFormatter(...),
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
)

@Composable
fun WheelDateTimePicker(
  selectedDateTime: LocalDateTime,
  onDateTimeChange: (LocalDateTime) -> Unit,
  modifier: Modifier = Modifier,
  minDateTime: LocalDateTime = LocalDateTime.EPOCH,
  maxDateTime: LocalDateTime = LocalDateTime.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDateTime.year, maxDateTime.year),
  dateFormatter: DateFormatter = dateFormatter(...),
  timeFormatter: TimeFormatter = timeFormatter(Locale.current),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
)
```

## State holder 语义

`WheelDatePickerState`：

- `displayedDate` 表示滚动中的当前 snapped date。
- `selectedDate` 表示滚动停止后提交的 date。
- `scrollToDate` / `animateScrollToDate` 会经过 `minDate`、`maxDate`、`yearsRange` 和月份天数裁剪，并返回实际落点。

`WheelTimePickerState`：

- `displayedTime` 表示滚动中的当前 snapped time。
- `selectedTime` 表示滚动停止后提交的 time。
- `scrollToTime` / `animateScrollToTime` 会同步 hour、minute、period wheel，并返回实际落点。

`WheelDateTimePickerState`：

- 以完整 `LocalDateTime` 作为单一事实来源。
- `displayedDateTime` 和 `selectedDateTime` 都必须经过完整 `minDateTime` / `maxDateTime` 裁剪。
- 程序化滚动按事务处理：先计算合法目标，再更新 date/time 子 wheel，避免发布中间非法 datetime。

## 内部实现

- `WheelPickerState` 只负责单列 wheel 的 index 和 `LazyListState`。
- `WheelDatePickerState`、`WheelTimePickerState`、`WheelDateTimePickerState` 是 composition-owned plain state holder，不保存 coroutine scope、formatter 或业务层对象。
- Date、Time、DateTime 的规则放在纯 Kotlin helper 中，便于 commonTest 覆盖。
- 内部 event sink 只用于 value-based overload 的 settled 事件和 DateTime 子 picker 的 displayed state 同步，不作为公开 API。

## 实施清单

1. 新增并接入 `WheelPickerState`，让单列 wheel 支持 state 驱动。
2. 新增 Date/Time/DateTime rules，覆盖裁剪、index 转换、AM/PM、闰年、月份天数和 datetime 边界。
3. 新增 `WheelDatePickerState`、`WheelTimePickerState`、`WheelDateTimePickerState` 和对应 `remember...State`。
4. 将 `AdaptiveWheelDatePicker`、`StandardWheelDatePicker`、`CJKWheelDatePicker`、`StandardWheelTimePicker`、`AdaptiveWheelDateTimePicker` 改为接收 state。
5. 添加 public state-based overload。
6. 添加 public value-based overload，并用 effect 将外部值同步到内部 state。
7. 删除所有 public legacy overload 和旧兼容测试。
8. 更新 sample 和 README，只展示 state-based / value-based 用法。
9. 运行 `./gradlew check`，至少确认 common tests 与 sample 编译不因 API 删除失败。

## 验收标准

- `WheelDatePicker`、`WheelTimePicker`、`WheelDateTimePicker` 的公开入口只有 state-based 和 value-based。
- 仓库内没有 `startDate`、`startTime`、`startDateTime`、`onSnapped*Changed`、`onSnapped*` 的公开用法或兼容测试。
- state-based overload 不带外部 change handler。
- value-based overload 只在 settle 后调用 `onDateChange`、`onTimeChange`、`onDateTimeChange`。
- `README.MD` 和 sample 使用新 API。
- 规则测试和 API 编译测试覆盖新模型。
