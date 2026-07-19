package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * A wheel picker over arbitrary [texts].
 *
 * Sizing is Modifier-driven: when [modifier] and the parent leave an axis unconstrained, the
 * picker uses its intrinsic default (128.dp wide, `rowCount` rows of ~42.7.dp). Constraints from
 * [modifier] or the parent layout (`Modifier.size`, `width`, `height`, `fillMaxWidth`, `widthIn`,
 * `weight`, a narrow container, ...) override or clamp the default per standard Compose rules.
 *
 * The picker resolves its size via subcomposition and therefore does not support
 * intrinsic-measurement parents (`IntrinsicSize.Min`/`Max`); pass an explicit `width`/`height`
 * instead.
 */
@Composable
fun WheelTextPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  texts: List<String>,
  rowCount: Int,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectedTextStyle: TextStyle = textStyle,
  selectedTextColor: Color = textColor,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollChanged: (snappedIndex: Int) -> Unit = {},
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
) {
  BoxWithConstraints(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    val effectiveSize = with(LocalDensity.current) {
      resolvePickerSize(
        constraints = constraints,
        default = pickerDefaultSize(defaultWidth = 128.dp, rowCount = rowCount),
      )
    }

    FixedSizeWheelTextPicker(
      modifier = Modifier,
      startIndex = startIndex,
      viewportSize = effectiveSize,
      texts = texts,
      rowCount = rowCount,
      textStyle = textStyle,
      textColor = textColor,
      selectedTextStyle = selectedTextStyle,
      selectedTextColor = selectedTextColor,
      selectorProperties = selectorProperties,
      onScrollChanged = onScrollChanged,
      onScrollFinished = onScrollFinished,
    )
  }
}

// Binary-compatibility shim with the exact 1.3.x released signature (style/color naming; no
// selectedTextStyle / selectedTextColor — those were added after 1.3.3).
@Deprecated(
  message = "Sizing is Modifier-driven. Use Modifier.size/width/height instead of the size parameter.",
  level = DeprecationLevel.HIDDEN,
)
@Composable
fun WheelTextPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  size: DpSize = DpSize(128.dp, 128.dp),
  texts: List<String>,
  rowCount: Int,
  style: TextStyle = MaterialTheme.typography.titleMedium,
  color: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollChanged: (snappedIndex: Int) -> Unit = {},
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
) = WheelTextPicker(
  modifier = modifier.size(size.width, size.height),
  startIndex = startIndex,
  texts = texts,
  rowCount = rowCount,
  textStyle = style,
  textColor = color,
  selectorProperties = selectorProperties,
  onScrollChanged = onScrollChanged,
  onScrollFinished = onScrollFinished,
)

@Composable
internal fun FixedSizeWheelTextPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  viewportSize: DpSize,
  texts: List<String>,
  rowCount: Int,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectedTextStyle: TextStyle = textStyle,
  selectedTextColor: Color = textColor,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollChanged: (snappedIndex: Int) -> Unit = {},
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
) {
  val defaultTextStyle = MaterialTheme.typography.titleMedium
  val resolvedTextStyle = remember(defaultTextStyle, textStyle) {
    defaultTextStyle.merge(textStyle)
  }
  val resolvedSelectedTextStyle = remember(resolvedTextStyle, selectedTextStyle) {
    resolvedTextStyle.merge(selectedTextStyle)
  }

  WheelPicker(
    modifier = modifier,
    startIndex = startIndex,
    size = viewportSize,
    count = texts.size,
    rowCount = rowCount,
    selectorProperties = selectorProperties,
    onScrollFinished = onScrollFinished,
    onScrollChanged = onScrollChanged
  ) { index, isSelected ->
    Text(
      text = texts[index],
      style = if (isSelected) resolvedSelectedTextStyle else resolvedTextStyle,
      color = if (isSelected) selectedTextColor else textColor,
      maxLines = 1,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
internal fun WheelTextPickerWithSuffix(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  viewportSize: DpSize,
  texts: List<String>,
  rowCount: Int,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectedTextStyle: TextStyle = textStyle,
  selectedTextColor: Color = textColor,
  suffix: String = "",
  suffixTextStyle: TextStyle = selectedTextStyle,
  suffixTextColor: Color = selectedTextColor,
  textToSuffixSpacing: Dp = 8.dp,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollChanged: (snappedIndex: Int) -> Unit = {},
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
) {
  val defaultTextStyle = MaterialTheme.typography.titleMedium
  val resolvedTextStyle = remember(defaultTextStyle, textStyle) {
    defaultTextStyle.merge(textStyle)
  }
  val resolvedSelectedTextStyle = remember(resolvedTextStyle, selectedTextStyle) {
    resolvedTextStyle.merge(selectedTextStyle)
  }
  val resolvedSuffixTextStyle = remember(resolvedTextStyle, suffixTextStyle) {
    resolvedTextStyle.merge(suffixTextStyle)
  }
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  val suffixWidth = remember(suffix, resolvedSuffixTextStyle, density, textMeasurer) {
    if (suffix.isNotEmpty()) {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(suffix),
        style = resolvedSuffixTextStyle,
        maxLines = 1
      )
      with(density) { textLayoutResult.size.width.toDp() }
    } else {
      0.dp
    }
  }

  val hasSuffix = suffix.isNotEmpty()
  val widthReferenceText = remember(texts, hasSuffix) {
    if (hasSuffix) texts.maxByOrNull { it.length } else null
  }

  val textWidth = remember(widthReferenceText, resolvedSelectedTextStyle, density, textMeasurer) {
    if (widthReferenceText == null) {
      0.dp
    } else {
      with(density) {
        textMeasurer.measure(
          text = AnnotatedString(widthReferenceText),
          style = resolvedSelectedTextStyle,
          maxLines = 1
        ).size.width.toDp()
      }
    }
  }

  Box(modifier = modifier) {
    WheelPicker(
      startIndex = startIndex,
      size = viewportSize,
      count = texts.size,
      rowCount = rowCount,
      selectorProperties = selectorProperties,
      onScrollFinished = onScrollFinished,
      onScrollChanged = onScrollChanged
    ) { index, isSelected ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = texts[index],
          style = if (isSelected) resolvedSelectedTextStyle else resolvedTextStyle,
          color = if (isSelected) selectedTextColor else textColor,
          maxLines = 1,
          textAlign = TextAlign.Center
        )
        if (suffix.isNotEmpty()) {
          Spacer(modifier = Modifier.width(suffixWidth + textToSuffixSpacing))
        }
      }
    }

    if (suffix.isNotEmpty()) {
      Text(
        text = suffix,
        modifier = Modifier
          .align(Alignment.Center)
          .padding(start = textWidth + textToSuffixSpacing),
        style = resolvedSuffixTextStyle,
        color = suffixTextColor,
        maxLines = 1
      )
    }
  }
}
