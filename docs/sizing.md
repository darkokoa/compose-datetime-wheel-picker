# Sizing

Picker size is controlled entirely through `Modifier` (since 1.4.0). When the caller does not
constrain an axis, the picker supplies its intrinsic default on that axis:

- **Width**: 256.dp for `WheelDatePicker` / `WheelDateTimePicker`, 128.dp for `WheelTimePicker` /
  `WheelTextPicker`.
- **Height**: follows [`rows`](rows-and-barrel.md#rows). `WheelRows.Count` takes ~42.7.dp per row
  (the default `Count(3)` is exactly 128.dp); `WheelRows.Height` takes seven rows. Larger counts
  grow the wheel instead of squeezing rows.

```kotlin
WheelDatePicker { }                                          // intrinsic 256 x 128.dp
WheelDatePicker(rows = WheelRows.Count(5)) { }               // intrinsic height ~213.dp
WheelDatePicker(rows = WheelRows.Height(32.dp)) { }          // intrinsic height 224.dp
WheelDatePicker(modifier = Modifier.fillMaxWidth()) { }      // parent width, intrinsic height
WheelDatePicker(modifier = Modifier.height(200.dp)) { }      // fixed height
WheelDatePicker(modifier = Modifier.size(300.dp, 160.dp)) { }// fixed size
WheelDatePicker(
  modifier = Modifier
    .widthIn(min = 240.dp, max = 400.dp)
    .heightIn(min = 128.dp),
) { }
```

Standard Compose constraint rules apply: fixed, min and max constraints from the `modifier` or the
parent override or clamp the intrinsic default, and pickers shrink to fit parents narrower than
their intrinsic width.

## Known limitation

The picker resolves its size via subcomposition and does not support intrinsic-measurement
parents (`IntrinsicSize.Min` / `IntrinsicSize.Max` will throw). Pass an explicit `width` / `height`
instead.

## Migrating from `size: DpSize`

See [MIGRATION.md](../MIGRATION.md#13x--140).
