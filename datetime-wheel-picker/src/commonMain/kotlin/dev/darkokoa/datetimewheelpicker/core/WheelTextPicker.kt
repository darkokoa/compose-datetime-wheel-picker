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
  style: TextStyle = MaterialTheme.typography.titleMedium,
  color: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
) {
  WheelPicker(
    modifier = modifier,
    startIndex = startIndex,
    size = size,
    count = texts.size,
    rowCount = rowCount,
    selectorProperties = selectorProperties,
    onScrollFinished = onScrollFinished
  ) { index ->
    Text(
      text = texts[index],
      style = style,
      color = color,
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
  style: TextStyle = MaterialTheme.typography.titleMedium,
  color: Color = LocalContentColor.current,
  suffix: String = "",
  suffixStyle: TextStyle = style,
  suffixColor: Color = color,
  textToSuffixSpacing: Dp = 8.dp,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  onScrollFinished: (snappedIndex: Int) -> Int? = { null },
) {
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  val suffixWidth = remember(suffix, suffixStyle) {
    if (suffix.isNotEmpty()) {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(suffix),
        style = suffixStyle
      )
      with(density) { textLayoutResult.size.width.toDp() }
    } else {
      0.dp
    }
  }

  val textWidth = remember(style) {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(texts.last()),
        style = style
      )
      with(density) { textLayoutResult.size.width.toDp() }
  }

  Box(modifier = modifier) {
    WheelPicker(
      startIndex = startIndex,
      size = size,
      count = texts.size,
      rowCount = rowCount,
      selectorProperties = selectorProperties,
      onScrollFinished = onScrollFinished
    ) { index ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = texts[index],
          style = style,
          color = color,
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
        style = suffixStyle,
        color = suffixColor,
        maxLines = 1
      )
    }
  }
}