package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun WheelTextPicker(
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  size: DpSize = DpSize(128.dp, 128.dp),
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
  WheelPicker(
    modifier = modifier,
    startIndex = startIndex,
    size = size,
    count = texts.size,
    rowCount = rowCount,
    selectorProperties = selectorProperties,
    onScrollFinished = onScrollFinished,
    onScrollChanged = onScrollChanged
  ) { index, isSelected ->
    Text(
      text = texts[index],
      style = if (isSelected) selectedTextStyle else textStyle,
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
  size: DpSize = DpSize(128.dp, 128.dp),
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
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  val suffixWidth = remember(suffix, suffixTextStyle, density, textMeasurer) {
    if (suffix.isNotEmpty()) {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(suffix),
        style = suffixTextStyle,
        maxLines = 1
      )
      with(density) { textLayoutResult.size.width.toDp() }
    } else {
      0.dp
    }
  }

  val textWidth = remember(texts, textStyle, selectedTextStyle, density, textMeasurer) {
    if (texts.isEmpty()) {
      0.dp
    } else {
      with(density) {
        listOf(textStyle, selectedTextStyle).distinct().maxOf { style ->
          texts.maxOf { text ->
            textMeasurer.measure(
              text = AnnotatedString(text),
              style = style,
              maxLines = 1
            ).size.width
          }
        }.toDp()
      }
    }
  }

  Box(modifier = modifier) {
    WheelPicker(
      startIndex = startIndex,
      size = size,
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
          style = if (isSelected) selectedTextStyle else textStyle,
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
        style = suffixTextStyle,
        color = suffixTextColor,
        maxLines = 1
      )
    }
  }
}
