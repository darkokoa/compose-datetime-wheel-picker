package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.format.CjkSuffixConfig
import dev.darkokoa.datetimewheelpicker.core.format.DateFormatter
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

@Composable
internal fun AdaptiveWheelDayMonthPicker(
    modifier: Modifier = Modifier,
    startMonth: Int, // 1-12
    startDayOfMonth: Int,
    dateFormatter: DateFormatter = dateFormatter(
        locale = Locale.current,
        monthDisplayStyle = MonthDisplayStyle.SHORT,
        cjkSuffixConfig = CjkSuffixConfig.HideAll
    ),
    size: DpSize = DpSize(256.dp, 128.dp),
    rowCount: Int = 3,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = LocalContentColor.current,
    selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
    onSnappedDateTime: (snappedDateTime: SnappedDateTime) -> Int? = { _ -> null }
) {

    var snappedDate by remember {
        mutableStateOf(
            LocalDate(2000, startMonth, startDayOfMonth)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (selectorProperties.enabled().value) {
            Surface(
                modifier = Modifier
                    .size(size.width, size.height / rowCount),
                shape = selectorProperties.shape().value,
                color = selectorProperties.color().value,
                border = selectorProperties.border().value
            ) {}
        }
        Row {
            // Month picker
            WheelTextPicker(
                size = DpSize(
                    width = size.width / 2,
                    height = size.height
                ),
                texts = rememberFormattedMonths(size.width, dateFormatter).map { it.text },
                rowCount = rowCount,
                style = textStyle,
                color = textColor,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    enabled = false
                ),
                startIndex = startMonth - 1,
                onScrollFinished = { snappedIndex ->
                    val newMonth = snappedIndex + 1
                    val newDate = snappedDate.withMonthNumber(newMonth)
                    snappedDate = newDate
                    
                    val snappedDateTime = LocalDateTime(snappedDate, kotlinx.datetime.LocalTime(0, 0))
                    onSnappedDateTime(
                        SnappedDateTime.Month(
                            snappedDateTime,
                            snappedIndex
                        )
                    )
                    snappedIndex
                }
            )
            
            // Day picker
            WheelTextPicker(
                size = DpSize(
                    width = size.width / 2,
                    height = size.height
                ),
                texts = rememberFormattedDayOfMonths(snappedDate.month.number, snappedDate.year, dateFormatter).map { it.text },
                rowCount = rowCount,
                style = textStyle,
                color = textColor,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    enabled = false
                ),
                startIndex = startDayOfMonth - 1,
                onScrollFinished = { snappedIndex ->
                    val newDay = snappedIndex + 1
                    val newDate = snappedDate.withDayOfMonth(newDay)
                    snappedDate = newDate
                    
                    val snappedDateTime = LocalDateTime(snappedDate, kotlinx.datetime.LocalTime(0, 0))
                    onSnappedDateTime(
                        SnappedDateTime.DayOfMonth(
                            snappedDateTime,
                            snappedIndex
                        )
                    )
                    snappedIndex
                }
            )
        }
    }
}
