package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.CYB3R_1N1T_ZOLL
import dev.darkokoa.datetimewheelpicker.core.AdaptiveWheelDatePicker
import dev.darkokoa.datetimewheelpicker.core.EPOCH
import dev.darkokoa.datetimewheelpicker.core.SelectorProperties
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import dev.darkokoa.datetimewheelpicker.core.now
import dev.darkokoa.datetimewheelpicker.core.pickerDefaultSize
import dev.darkokoa.datetimewheelpicker.core.resolvePickerSize
import kotlinx.datetime.LocalDate

/**
 * A wheel date picker.
 *
 * Sizing is Modifier-driven: when [modifier] and the parent leave an axis unconstrained, the
 * picker uses its intrinsic default (256.dp wide, `rowCount` rows of ~42.7.dp — 128.dp tall at
 * the default `rowCount = 3`). Constraints from [modifier] or the parent layout (`Modifier.size`,
 * `width`, `height`, `fillMaxWidth`, `widthIn`, `weight`, a narrow container, ...) override or
 * clamp the default per standard Compose rules.
 *
 * The picker resolves its size via subcomposition and therefore does not support
 * intrinsic-measurement parents (`IntrinsicSize.Min`/`Max`); pass an explicit `width`/`height`
 * instead.
 */
@Composable
fun WheelDatePicker(
  modifier: Modifier = Modifier,
  startDate: LocalDate = LocalDate.now(),
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
  dateFormatter: DateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.FULL,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll
  ),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectedTextStyle: TextStyle = textStyle,
  selectedTextColor: Color = textColor,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedDateChanged: (snappedDate: LocalDate) -> Unit = {},
  onSnappedDate: (snappedDate: LocalDate) -> Unit = {},
) {
  BoxWithConstraints(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    val effectiveSize = with(LocalDensity.current) {
      resolvePickerSize(
        constraints = constraints,
        default = pickerDefaultSize(defaultWidth = 256.dp, rowCount = rowCount),
      )
    }

    AdaptiveWheelDatePicker(
      Modifier,
      startDate,
      minDate,
      maxDate,
      yearsRange,
      dateFormatter,
      effectiveSize,
      rowCount,
      textStyle,
      textColor,
      selectedTextStyle,
      selectedTextColor,
      selectorProperties,
      onSnappedDate = { snappedDate ->
        onSnappedDate(snappedDate.snappedLocalDate)
        snappedDate.snappedIndex
      },
      onSnappedDateChanged = { snappedDate ->
        onSnappedDateChanged(snappedDate.snappedLocalDate)
      }
    )
  }
}

// Binary-compatibility shim with the exact 1.3.x released signature (no selectedTextStyle /
// selectedTextColor — those were added after 1.3.3).
@Deprecated(
  message = "Sizing is Modifier-driven. Use Modifier.size/width/height instead of the size parameter.",
  level = DeprecationLevel.HIDDEN,
)
@Composable
fun WheelDatePicker(
  modifier: Modifier = Modifier,
  startDate: LocalDate = LocalDate.now(),
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
  dateFormatter: DateFormatter = dateFormatter(
    locale = Locale.current,
    monthDisplayStyle = MonthDisplayStyle.FULL,
    cjkSuffixConfig = CjkSuffixConfig.ShowAll
  ),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onSnappedDateChanged: (snappedDate: LocalDate) -> Unit = {},
  onSnappedDate: (snappedDate: LocalDate) -> Unit = {},
) = WheelDatePicker(
  modifier = modifier.size(size.width, size.height),
  startDate = startDate,
  minDate = minDate,
  maxDate = maxDate,
  yearsRange = yearsRange,
  dateFormatter = dateFormatter,
  rowCount = rowCount,
  textStyle = textStyle,
  textColor = textColor,
  selectorProperties = selectorProperties,
  onSnappedDateChanged = onSnappedDateChanged,
  onSnappedDate = onSnappedDate,
)
