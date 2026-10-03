# Migration guide

- **1.3.x → 1.4.0** is **source-breaking** only. Binaries compiled against 1.3.x keep linking
  through hidden compatibility overloads that restore the exact 1.3.x signatures; those overloads
  will be removed in the next major release.
- **1.4.x → 1.5.0** is source-breaking **and binary-breaking**: the 1.4.x signatures of
  `WheelDatePicker`, `WheelTimePicker`, `WheelDateTimePicker` and `WheelTextPicker` (the ones with
  `rowCount`) no longer exist in the binary. Anything compiled against 1.4.x, including a library
  that depends on this one, must be recompiled against 1.5.0. Binaries compiled against 1.3.x
  still link.

See [CHANGELOG.md](CHANGELOG.md) for the full list of changes.

## 1.4.x → 1.5.0

### `rowCount: Int` is replaced by `rows: WheelRows`

Applies to `WheelDatePicker`, `WheelTimePicker`, `WheelDateTimePicker` and `WheelTextPicker`.

```kotlin
// Before
WheelDatePicker(rowCount = 5) { }
// After
WheelDatePicker(rows = WheelRows.Count(5)) { }
```

Behavior changes that come with it:

- **Even counts**: the drum always holds an odd number of rows so the selected row is centered.
  `Count(4)` now shows the same five rows as `Count(5)`; before, an even `rowCount` rendered half a
  row cut off at each rim. `Count(4).count` still returns `4`.
- `WheelTextPicker.rows` now defaults to `Count(3)`, like the other pickers.
- New: `WheelRows.Height(h)` fixes the row height instead of the row count. See
  [Rows](docs/rows-and-barrel.md#rows).

### Barrel projection replaces the per-row tilt and fade

Rows are now laid out on a cylinder in all pickers. For odd row counts (including the default 3)
picker sizes are unchanged, but the look is different: rows span the drum from rim to rim and the
centered row is slightly taller than `height / count`. Rows more than four positions from the
center no longer render mirrored.

- Configure it with the new `barrelProperties` parameter. See
  [Barrel projection](docs/rows-and-barrel.md#barrel-projection).
- For a flatter wheel, use a small `rimAngle`, or `0f` for no projection at all:

  ```kotlin
  WheelDatePicker(
    barrelProperties = WheelPickerDefaults.barrelProperties(rimAngle = 0f),
  ) { }
  ```
- For the iOS look, use `rimAngle = 90f` together with `rows = WheelRows.Height(32.dp)`.
- Rows now fade by their on-screen distance from the center, which also applies to a flat wheel.
  Use `fadeStrength = 0f` to keep every row opaque.

## 1.3.x → 1.4.0

### The `size: DpSize` parameter is removed

Picker size is controlled through `Modifier`. This applies to `WheelDatePicker`, `WheelTimePicker`,
`WheelDateTimePicker` and `WheelTextPicker`. `BoxWithConstraints` workarounds for responsive sizing
can be removed. See [Sizing](docs/sizing.md).

```kotlin
// Before
WheelDatePicker(size = DpSize(300.dp, 160.dp)) { }
// After
WheelDatePicker(modifier = Modifier.size(300.dp, 160.dp)) { }

// Before: workaround for responsive width
BoxWithConstraints(Modifier.fillMaxWidth()) {
  WheelDatePicker(size = DpSize(maxWidth, 200.dp)) { }
}
// After
WheelDatePicker(modifier = Modifier.fillMaxWidth().height(200.dp)) { }
```

With no height constraint, the default height now scales with the row count (~42.7.dp per row;
three rows stay exactly 128.dp). Callers that depended on the old squeezed 128.dp total height for
more than three rows should state it with `Modifier.height(128.dp)`.

### `WheelTextPicker` renames

`style` becomes `textStyle` and `color` becomes `textColor`. `selectedTextStyle` and
`selectedTextColor` are new on all pickers.

```kotlin
// Before
WheelTextPicker(
  texts = values,
  rowCount = 3,
  size = DpSize(96.dp, 128.dp),
  style = MaterialTheme.typography.titleMedium,
  color = LocalContentColor.current,
)

// After (1.5.0 syntax; on 1.4.x keep rowCount = 3)
WheelTextPicker(
  modifier = Modifier.size(96.dp, 128.dp),
  texts = values,
  rows = WheelRows.Count(3),
  textStyle = MaterialTheme.typography.titleMedium,
  textColor = LocalContentColor.current,
  selectedTextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
  selectedTextColor = MaterialTheme.colorScheme.primary,
)
```

### Known limitation

Pickers resolve their size via subcomposition and no longer support intrinsic-measurement parents
(`IntrinsicSize.Min` / `IntrinsicSize.Max` will throw). Pass an explicit `width` / `height`
instead.
