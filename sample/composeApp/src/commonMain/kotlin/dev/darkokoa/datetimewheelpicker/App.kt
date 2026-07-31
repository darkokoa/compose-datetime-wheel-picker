package dev.darkokoa.datetimewheelpicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import dev.darkokoa.datetimewheelpicker.theme.AppTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal enum class DemoTab(
  val title: String,
  val icon: ImageVector,
) {
  TIME("Time", Icons.Outlined.Schedule),
  DATE("Date", Icons.Outlined.CalendarMonth),
  DATE_TIME("DateTime", Icons.Outlined.Event),
  SIZING("Sizing", Icons.Outlined.Straighten),
}

private interface DemoVariant {
  val title: String
}

private enum class TimeDemo(override val title: String) : DemoVariant {
  DEFAULT("Default"),
  AM_PM("AM/PM"),
  DIALOG("Dialog"),
}

private enum class DateDemo(override val title: String) : DemoVariant {
  DEFAULT("Default"),
  STYLED("Styled"),
  RANGE("Range"),
  DIALOG("Dialog"),
}

private enum class DateTimeDemo(override val title: String) : DemoVariant {
  DEFAULT("Default"),
  CUSTOM("Custom"),
}

private enum class SizingDemo(override val title: String) : DemoVariant {
  FILL_WIDTH("Fill width"),
  FIVE_ROWS("5 rows"),
  NARROW("Narrow"),
  TIME_FILL_WIDTH("Time width"),
}

private val DemoTabSaver = Saver<DemoTab, String>(
  save = { it.name },
  restore = { savedName -> DemoTab.entries.firstOrNull { it.name == savedName } ?: DemoTab.TIME },
)

private val TimeDemoSaver = Saver<TimeDemo, String>(
  save = { it.name },
  restore = { savedName -> TimeDemo.entries.firstOrNull { it.name == savedName } ?: TimeDemo.DEFAULT },
)

private val DateDemoSaver = Saver<DateDemo, String>(
  save = { it.name },
  restore = { savedName -> DateDemo.entries.firstOrNull { it.name == savedName } ?: DateDemo.DEFAULT },
)

private val DateTimeDemoSaver = Saver<DateTimeDemo, String>(
  save = { it.name },
  restore = { savedName -> DateTimeDemo.entries.firstOrNull { it.name == savedName } ?: DateTimeDemo.DEFAULT },
)

private val SizingDemoSaver = Saver<SizingDemo, String>(
  save = { it.name },
  restore = { savedName -> SizingDemo.entries.firstOrNull { it.name == savedName } ?: SizingDemo.FILL_WIDTH },
)

@Composable
fun App() = AppTheme {
  var selectedTab by rememberSaveable(stateSaver = DemoTabSaver) {
    mutableStateOf(DemoTab.TIME)
  }

  AppContent(
    selectedTab = selectedTab,
    onTabSelected = { selectedTab = it },
    nowProvider = ::currentDateTime,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
internal fun AppContent(
  selectedTab: DemoTab,
  onTabSelected: (DemoTab) -> Unit,
  nowProvider: () -> LocalDateTime,
  modifier: Modifier = Modifier,
) {
  val saveableStateHolder = rememberSaveableStateHolder()

  Scaffold(
    modifier = modifier,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      NavigationBar {
        DemoTab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { onTabSelected(tab) },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
              )
            },
            label = { Text(tab.title) },
          )
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      saveableStateHolder.SaveableStateProvider(selectedTab.name) {
        when (selectedTab) {
          DemoTab.TIME -> TimeDemos(
            nowProvider = nowProvider,
            modifier = Modifier.fillMaxSize(),
          )

          DemoTab.DATE -> DateDemos(
            nowProvider = nowProvider,
            modifier = Modifier.fillMaxSize(),
          )

          DemoTab.DATE_TIME -> DateTimeDemos(
            nowProvider = nowProvider,
            modifier = Modifier.fillMaxSize(),
          )

          DemoTab.SIZING -> SizingDemos(modifier = Modifier.fillMaxSize())
        }
      }
    }
  }
}

@Composable
private fun <T : DemoVariant> DemoPage(
  variants: List<T>,
  selectedVariant: T,
  onVariantSelected: (T) -> Unit,
  stateKey: (T) -> String,
  modifier: Modifier = Modifier,
  content: @Composable (T) -> Unit,
) {
  val saveableStateHolder = rememberSaveableStateHolder()

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        variants.forEach { variant ->
          FilterChip(
            selected = selectedVariant == variant,
            onClick = { onVariantSelected(variant) },
            label = { Text(variant.title) },
          )
        }
      }
    }
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      contentAlignment = Alignment.Center,
    ) {
      saveableStateHolder.SaveableStateProvider(stateKey(selectedVariant)) {
        content(selectedVariant)
      }
    }
  }
}

@Composable
private fun DemoSection(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    DemoLabel(title)
    content()
  }
}

@Composable
private fun PickerDemoSection(
  title: String,
  callbackName: String,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.(
    onSnapped: (String) -> Unit,
    onSnappedChanged: (String) -> Unit,
  ) -> Unit,
) {
  var snappedValue by rememberSaveable { mutableStateOf<String?>(null) }
  var snappedChangedValue by rememberSaveable { mutableStateOf<String?>(null) }

  DemoSection(
    title = title,
    modifier = modifier.fillMaxWidth(),
  ) {
    content(
      { value -> snappedValue = value },
      { value -> snappedChangedValue = value },
    )
    CallbackValuesCard(
      callbackName = callbackName,
      snappedValue = snappedValue,
      snappedChangedValue = snappedChangedValue,
    )
  }
}

@Composable
private fun CallbackValuesCard(
  callbackName: String,
  snappedValue: String?,
  snappedChangedValue: String?,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      CallbackValueRow(name = callbackName, value = snappedValue)
      HorizontalDivider()
      CallbackValueRow(name = "${callbackName}Changed", value = snappedChangedValue)
    }
  }
}

@Composable
private fun CallbackValueRow(
  name: String,
  value: String?,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Text(
      text = name,
      modifier = Modifier.fillMaxWidth(),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = value ?: "—",
      modifier = Modifier.fillMaxWidth(),
      style = MaterialTheme.typography.bodyMedium,
      fontFamily = FontFamily.Monospace,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun DemoLabel(
  text: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    modifier = modifier,
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.secondary,
  )
}

@Composable
private fun TimeDemos(
  nowProvider: () -> LocalDateTime,
  modifier: Modifier = Modifier,
) {
  val initialTime = remember(nowProvider) { nowProvider().atMinutePrecision().time }
  var selectedDemo by rememberSaveable(stateSaver = TimeDemoSaver) {
    mutableStateOf(TimeDemo.DEFAULT)
  }

  DemoPage(
    variants = TimeDemo.entries,
    selectedVariant = selectedDemo,
    onVariantSelected = { selectedDemo = it },
    stateKey = { it.name },
    modifier = modifier,
  ) { demo ->
    when (demo) {
      TimeDemo.DEFAULT -> PickerDemoSection(
        title = "Default time picker",
        callbackName = "onSnappedTime",
      ) { onSnapped, onSnappedChanged ->
        WheelTimePicker(
          startTime = initialTime,
          onSnappedTime = { time ->
            logPickerCallback("Default time picker", "onSnappedTime", time)
            onSnapped(time.toString())
          },
          onSnappedTimeChanged = { time ->
            logPickerCallback("Default time picker", "onSnappedTimeChanged", time)
            onSnappedChanged(time.toString())
          },
        )
      }

      TimeDemo.AM_PM -> PickerDemoSection(
        title = "AM/PM format",
        callbackName = "onSnappedTime",
      ) { onSnapped, onSnappedChanged ->
        WheelTimePicker(
          startTime = initialTime,
          timeFormatter = timeFormatter(timeFormat = TimeFormat.AM_PM),
          onSnappedTime = { time ->
            logPickerCallback("AM/PM time picker", "onSnappedTime", time)
            onSnapped(time.toString())
          },
          onSnappedTimeChanged = { time ->
            logPickerCallback("AM/PM time picker", "onSnappedTimeChanged", time)
            onSnappedChanged(time.toString())
          },
        )
      }

      TimeDemo.DIALOG -> DemoSection("In a dialog") {
        TimePickerChip(nowProvider = nowProvider)
      }
    }
  }
}

@Composable
private fun DateDemos(
  nowProvider: () -> LocalDateTime,
  modifier: Modifier = Modifier,
) {
  val initialDate = remember(nowProvider) { nowProvider().date }
  val maxDate = remember(initialDate) { initialDate.plus(1, DateTimeUnit.YEAR) }
  var selectedDemo by rememberSaveable(stateSaver = DateDemoSaver) {
    mutableStateOf(DateDemo.DEFAULT)
  }

  DemoPage(
    variants = DateDemo.entries,
    selectedVariant = selectedDemo,
    onVariantSelected = { selectedDemo = it },
    stateKey = { it.name },
    modifier = modifier,
  ) { demo ->
    when (demo) {
      DateDemo.DEFAULT -> PickerDemoSection(
        title = "Default date picker",
        callbackName = "onSnappedDate",
      ) { onSnapped, onSnappedChanged ->
        WheelDatePicker(
          startDate = initialDate,
          onSnappedDate = { date ->
            logPickerCallback("Default date picker", "onSnappedDate", date)
            onSnapped(date.toString())
          },
          onSnappedDateChanged = { date ->
            logPickerCallback("Default date picker", "onSnappedDateChanged", date)
            onSnappedChanged(date.toString())
          },
        )
      }

      DateDemo.STYLED -> PickerDemoSection(
        title = "Selected text styling",
        callbackName = "onSnappedDate",
      ) { onSnapped, onSnappedChanged ->
        WheelDatePicker(
          startDate = initialDate,
          textStyle = MaterialTheme.typography.titleMedium,
          textColor = LocalContentColor.current,
          selectedTextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          selectedTextColor = MaterialTheme.colorScheme.primary,
          onSnappedDate = { date ->
            logPickerCallback("Styled date picker", "onSnappedDate", date)
            onSnapped(date.toString())
          },
          onSnappedDateChanged = { date ->
            logPickerCallback("Styled date picker", "onSnappedDateChanged", date)
            onSnappedChanged(date.toString())
          },
        )
      }

      DateDemo.RANGE -> PickerDemoSection(
        title = "One-year min/max range",
        callbackName = "onSnappedDate",
      ) { onSnapped, onSnappedChanged ->
        WheelDatePicker(
          startDate = initialDate,
          minDate = initialDate,
          maxDate = maxDate,
          onSnappedDate = { date ->
            logPickerCallback("Ranged date picker", "onSnappedDate", date)
            onSnapped(date.toString())
          },
          onSnappedDateChanged = { date ->
            logPickerCallback("Ranged date picker", "onSnappedDateChanged", date)
            onSnappedChanged(date.toString())
          },
        )
      }

      DateDemo.DIALOG -> DemoSection("In a dialog") {
        DatePickerChip(nowProvider = nowProvider)
      }
    }
  }
}

@Composable
private fun DateTimeDemos(
  nowProvider: () -> LocalDateTime,
  modifier: Modifier = Modifier,
) {
  val initialDateTime = remember(nowProvider) { nowProvider().atMinutePrecision() }
  val maxDateTime = remember(initialDateTime) {
    LocalDateTime(
      date = initialDateTime.date.plus(1, DateTimeUnit.YEAR),
      time = initialDateTime.time,
    )
  }
  var selectedDemo by rememberSaveable(stateSaver = DateTimeDemoSaver) {
    mutableStateOf(DateTimeDemo.DEFAULT)
  }

  DemoPage(
    variants = DateTimeDemo.entries,
    selectedVariant = selectedDemo,
    onVariantSelected = { selectedDemo = it },
    stateKey = { it.name },
    modifier = modifier,
  ) { demo ->
    when (demo) {
      DateTimeDemo.DEFAULT -> PickerDemoSection(
        title = "Default date time picker",
        callbackName = "onSnappedDateTime",
      ) { onSnapped, onSnappedChanged ->
        WheelDateTimePicker(
          startDateTime = initialDateTime,
          onSnappedDateTime = { dateTime ->
            logPickerCallback("Default date time picker", "onSnappedDateTime", dateTime)
            onSnapped(dateTime.toString())
          },
          onSnappedDateTimeChanged = { dateTime ->
            logPickerCallback("Default date time picker", "onSnappedDateTimeChanged", dateTime)
            onSnappedChanged(dateTime.toString())
          },
        )
      }

      DateTimeDemo.CUSTOM -> PickerDemoSection(
        title = "AM/PM, one-year range, custom selector",
        callbackName = "onSnappedDateTime",
      ) { onSnapped, onSnappedChanged ->
        WheelDateTimePicker(
          startDateTime = initialDateTime,
          minDateTime = initialDateTime,
          maxDateTime = maxDateTime,
          timeFormatter = timeFormatter(timeFormat = TimeFormat.AM_PM),
          modifier = Modifier.size(200.dp, 100.dp),
          rowCount = 5,
          textStyle = MaterialTheme.typography.titleSmall,
          textColor = Color(0xFFFFC300),
          selectorProperties = WheelPickerDefaults.selectorProperties(
            enabled = true,
            shape = RoundedCornerShape(0.dp),
            color = Color(0xFFF1FAEE).copy(alpha = 0.2f),
            border = BorderStroke(2.dp, Color(0xFFF1FAEE)),
          ),
          onSnappedDateTime = { dateTime ->
            logPickerCallback("Custom date time picker", "onSnappedDateTime", dateTime)
            onSnapped(dateTime.toString())
          },
          onSnappedDateTimeChanged = { dateTime ->
            logPickerCallback("Custom date time picker", "onSnappedDateTimeChanged", dateTime)
            onSnappedChanged(dateTime.toString())
          },
        )
      }
    }
  }
}

@Composable
private fun SizingDemos(modifier: Modifier = Modifier) {
  var selectedDemo by rememberSaveable(stateSaver = SizingDemoSaver) {
    mutableStateOf(SizingDemo.FILL_WIDTH)
  }

  DemoPage(
    variants = SizingDemo.entries,
    selectedVariant = selectedDemo,
    onVariantSelected = { selectedDemo = it },
    stateKey = { it.name },
    modifier = modifier,
  ) { demo ->
    when (demo) {
      SizingDemo.FILL_WIDTH -> DemoSection(
        title = "fillMaxWidth(), intrinsic height",
        modifier = Modifier.fillMaxWidth(),
      ) {
        WheelDatePicker(
          modifier = Modifier.fillMaxWidth(),
          onSnappedDate = { date ->
            logPickerCallback("Sizing fillMaxWidth", "onSnappedDate", date)
          },
          onSnappedDateChanged = { date ->
            logPickerCallback("Sizing fillMaxWidth", "onSnappedDateChanged", date)
          },
        )
      }

      SizingDemo.FIVE_ROWS -> DemoSection("rowCount = 5, intrinsic height") {
        WheelDatePicker(
          rowCount = 5,
          onSnappedDate = { date ->
            logPickerCallback("Sizing rowCount = 5", "onSnappedDate", date)
          },
          onSnappedDateChanged = { date ->
            logPickerCallback("Sizing rowCount = 5", "onSnappedDateChanged", date)
          },
        )
      }

      SizingDemo.NARROW -> DemoSection("Narrow 200.dp parent") {
        Box(modifier = Modifier.width(200.dp)) {
          WheelDatePicker(
            modifier = Modifier.fillMaxWidth(),
            onSnappedDate = { date ->
              logPickerCallback("Sizing narrow parent", "onSnappedDate", date)
            },
            onSnappedDateChanged = { date ->
              logPickerCallback("Sizing narrow parent", "onSnappedDateChanged", date)
            },
          )
        }
      }

      SizingDemo.TIME_FILL_WIDTH -> DemoSection(
        title = "WheelTimePicker, fillMaxWidth()",
        modifier = Modifier.fillMaxWidth(),
      ) {
        WheelTimePicker(
          modifier = Modifier.fillMaxWidth(),
          onSnappedTime = { time ->
            logPickerCallback("Sizing time fillMaxWidth", "onSnappedTime", time)
          },
          onSnappedTimeChanged = { time ->
            logPickerCallback("Sizing time fillMaxWidth", "onSnappedTimeChanged", time)
          },
        )
      }
    }
  }
}

@Composable
private fun TimePickerChip(
  nowProvider: () -> LocalDateTime,
  modifier: Modifier = Modifier,
) {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  var selectedTimeStr by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedTime = selectedTimeStr?.let(LocalTime::parse)

  Button(
    onClick = { showDialog = true },
    modifier = modifier,
  ) {
    Text(selectedTime?.let { "Selected time: $it" } ?: "Select time")
  }

  if (showDialog) {
    val dialogInitialTime = remember(selectedTime, nowProvider) {
      selectedTime ?: nowProvider().atMinutePrecision().time
    }
    var pendingTime by remember(dialogInitialTime) { mutableStateOf(dialogInitialTime) }

    PickerDialog(
      title = "Select time",
      onConfirm = {
        selectedTimeStr = pendingTime.toString()
        showDialog = false
      },
      onDismiss = { showDialog = false },
    ) {
      WheelTimePicker(
        startTime = dialogInitialTime,
        onSnappedTime = { time ->
          logPickerCallback("Time dialog", "onSnappedTime", time)
        },
        onSnappedTimeChanged = { time ->
          logPickerCallback("Time dialog", "onSnappedTimeChanged", time)
          pendingTime = time
        },
      )
    }
  }
}

@Composable
private fun DatePickerChip(
  nowProvider: () -> LocalDateTime,
  modifier: Modifier = Modifier,
) {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  var selectedDateStr by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedDate = selectedDateStr?.let(LocalDate::parse)

  Button(
    onClick = { showDialog = true },
    modifier = modifier,
  ) {
    Text(selectedDate?.let { "Selected date: $it" } ?: "Select date")
  }

  if (showDialog) {
    val dialogInitialDate = remember(selectedDate, nowProvider) {
      selectedDate ?: nowProvider().date
    }
    var pendingDate by remember(dialogInitialDate) { mutableStateOf(dialogInitialDate) }

    PickerDialog(
      title = "Select date",
      onConfirm = {
        selectedDateStr = pendingDate.toString()
        showDialog = false
      },
      onDismiss = { showDialog = false },
    ) {
      WheelDatePicker(
        startDate = dialogInitialDate,
        onSnappedDate = { date ->
          logPickerCallback("Date dialog", "onSnappedDate", date)
        },
        onSnappedDateChanged = { date ->
          logPickerCallback("Date dialog", "onSnappedDateChanged", date)
          pendingDate = date
        },
      )
    }
  }
}

@Composable
private fun PickerDialog(
  title: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier,
    title = { Text(title) },
    text = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
        content = content,
      )
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text("OK")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}

private fun currentDateTime(): LocalDateTime =
  Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

private fun LocalDateTime.atMinutePrecision(): LocalDateTime =
  LocalDateTime(date = date, time = LocalTime(hour = hour, minute = minute))
