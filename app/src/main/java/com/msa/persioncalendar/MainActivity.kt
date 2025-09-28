package com.msa.persioncalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.msa.calendar.CalendarScreen
import com.msa.calendar.RangeCalendarScreen
import com.msa.calendar.ui.theme.PersionCalendarTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.dp
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.toSoleimaniDate
import com.msa.calendar.utils.toPersianNumber
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.plusDays
import java.util.Calendar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.msa.calendar.utils.SoleimaniDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersionCalendarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalendarShowcaseScreen()
                }
            }
        }
    }
}
private enum class SelectionType {
    Single,
    Range,
    QuickToday
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarShowcaseScreen(modifier: Modifier = Modifier) {
    var showSinglePicker by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }
    var selectedSingleDate by remember { mutableStateOf<SoleimaniDate?>(null) }
    var selectedRange by remember { mutableStateOf<SoleimaniRange?>(null) }
    var lastSelectionType by remember { mutableStateOf<SelectionType?>(null) }
    var useLatinDigits by remember { mutableStateOf(false) }
    var showTodayShortcut by remember { mutableStateOf(true) }
    var limitToNextMonth by remember { mutableStateOf(false) }
    var blockFridays by remember { mutableStateOf(false) }
    var blockThirteenth by remember { mutableStateOf(false) }

    val today = remember { PersionCalendar().toSoleimaniDate() }

    val constraintConfig = remember(limitToNextMonth, blockFridays, blockThirteenth, today) {
        val minDate = if (limitToNextMonth) today else null
        val computedMax = today.plusDays(30) ?: today
        val maxDate = if (limitToNextMonth) computedMax else null
        val disabledDates = if (blockThirteenth) {
            generateThirteenthBlackouts(
                start = today,
                monthsAhead = if (limitToNextMonth) 3 else 12,
                minDate = minDate,
                maxDate = maxDate,
            )
        } else {
            emptySet()
        }
        val validator = if (blockFridays) {
            { date: SoleimaniDate -> date.toCalendar().getDayOfWeek() != Calendar.FRIDAY }
        } else {
            DatePickerConstraints.AlwaysValid
        }
        DatePickerConstraints(
            minDate = minDate,
            maxDate = maxDate,
            disabledDates = disabledDates,
            dateValidator = validator,
        )
    }

    val dialogConfig = remember(useLatinDigits, showTodayShortcut, constraintConfig) {
        val strings = if (useLatinDigits) {
            DatePickerStrings(
                title = "Select date",
                confirm = "Confirm",
                cancel = "Cancel",
                today = "Today",
                rangeStartLabel = "Start date",
                rangeEndLabel = "End date",
            )
        } else {
            DatePickerStrings()
        }
        DatePickerConfig(
            strings = strings,
            digitMode = if (useLatinDigits) DigitMode.Latin else DigitMode.Persian,
            showTodayAction = showTodayShortcut,
            constraints = constraintConfig,
        )
    }

    if (showSinglePicker) {
        CalendarScreen(
            onDismiss = { showSinglePicker = false },
            onConfirm = { showSinglePicker = false },
            config = dialogConfig,
            onDateSelected = { date ->
                selectedSingleDate = date
                lastSelectionType = SelectionType.Single
            }
        )
    }
    if (showRangePicker) {
        RangeCalendarScreen(
            onDismiss = { showRangePicker = false },
            setDate = { _ -> },
            config = dialogConfig,
            onRangeSelected = { start, end ->
                val (first, second) = if (start <= end) start to end else end to start
                selectedRange = SoleimaniRange(first, second)
                lastSelectionType = SelectionType.Range
            }
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "نمونه انتخاب تاریخ فارسی") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = "این صفحه چند سناریو متداول استفاده از کتابخانه را نمایش می‌دهد.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))
            PreferencesSection(
                useLatinDigits = useLatinDigits,
                onDigitsModeChanged = { useLatinDigits = it },
                showTodayShortcut = showTodayShortcut,
                onToggleTodayShortcut = { showTodayShortcut = it },
                limitToNextMonth = limitToNextMonth,
                onToggleLimitToNextMonth = { limitToNextMonth = it },
                blockFridays = blockFridays,
                onToggleBlockFridays = { blockFridays = it },
                blockThirteenth = blockThirteenth,
                onToggleBlockThirteenth = { blockThirteenth = it },
            )



            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(onClick = { showSinglePicker = true }) {
                    Text(text = "انتخاب تاریخ تکی")
                }

                FilledTonalButton(onClick = { showRangePicker = true }) {
                    Text(text = "انتخاب بازه تاریخ")
                }

                FilledTonalButton(onClick = {
                    selectedSingleDate = today
                    lastSelectionType = SelectionType.QuickToday
                }) {
                    Text(text = "ثبت سریع امروز")
                }

                OutlinedButton(
                    onClick = {
                        selectedSingleDate = null
                        selectedRange = null
                        lastSelectionType = null
                    },
                ) {
                    Text(text = "پاک کردن انتخاب‌ها")
                }

                OutlinedButton(
                    enabled = lastSelectionType != null,
                    onClick = {
                        when (lastSelectionType) {
                            SelectionType.Single -> showSinglePicker = true
                            SelectionType.Range -> showRangePicker = true
                            SelectionType.QuickToday -> {
                                selectedSingleDate = today
                            }
                            null -> Unit
                        }
                    }
                ) {
                    Text(text = "تکرار آخرین حالت")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SelectionSummaryCard(
                selectedSingleDate = selectedSingleDate,
                selectedRange = selectedRange,
                lastSelectionType = lastSelectionType,
                constraints = constraintConfig,
                limitToNextMonth = limitToNextMonth,
                blockFridays = blockFridays,
                blockThirteenth = blockThirteenth,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PreferencesSection(
    useLatinDigits: Boolean,
    onDigitsModeChanged: (Boolean) -> Unit,
    showTodayShortcut: Boolean,
    onToggleTodayShortcut: (Boolean) -> Unit,
    limitToNextMonth: Boolean,
    onToggleLimitToNextMonth: (Boolean) -> Unit,
    blockFridays: Boolean,
    onToggleBlockFridays: (Boolean) -> Unit,
    blockThirteenth: Boolean,
    onToggleBlockThirteenth: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "تنظیمات پیشرفته",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        PreferenceSwitchRow(
            label = "نمایش دکمه امروز",
            checked = showTodayShortcut,
            onCheckedChange = onToggleTodayShortcut,
        )

        PreferenceSwitchRow(
            label = "استفاده از اعداد لاتین",
            checked = useLatinDigits,
            onCheckedChange = onDigitsModeChanged,
        )

        PreferenceSwitchRow(
            label = "محدود به ۳۰ روز آینده",
            checked = limitToNextMonth,
            onCheckedChange = onToggleLimitToNextMonth,
        )

        PreferenceSwitchRow(
            label = "مسدود کردن جمعه‌ها",
            checked = blockFridays,
            onCheckedChange = onToggleBlockFridays,
        )

        PreferenceSwitchRow(
            label = "حذف روز سیزدهم",
            checked = blockThirteenth,
            onCheckedChange = onToggleBlockThirteenth,
        )
    }
}

@Composable
private fun PreferenceSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            )
        )
    }
}

@Composable
private fun SelectionSummaryCard(
    selectedSingleDate: SoleimaniDate?,
    selectedRange: SoleimaniRange?,
    lastSelectionType: SelectionType?,
    constraints: DatePickerConstraints,
    limitToNextMonth: Boolean,
    blockFridays: Boolean,
    blockThirteenth: Boolean,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "نتیجه انتخاب‌ها",
                style = MaterialTheme.typography.titleMedium,
            )

            Divider()

            SelectionSummaryRow(
                title = "تاریخ تکی",
                value = selectedSingleDate?.toDisplayString()
                    ?: "هنوز تاریخی انتخاب نشده است.",
            )

            SelectionSummaryRow(
                title = "بازه تاریخی",
                value = selectedRange?.toDisplayString()
                    ?: "بازه‌ای انتخاب نشده است.",
            )

            AnimatedVisibility(
                visible = lastSelectionType != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val label = when (lastSelectionType) {
                    SelectionType.Single -> "آخرین عمل: انتخاب تاریخ تکی"
                    SelectionType.Range -> "آخرین عمل: انتخاب بازه"
                    SelectionType.QuickToday -> "آخرین عمل: ثبت سریع امروز"
                    null -> ""
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Divider()

            ConstraintSummary(
                constraints = constraints,
                limitToNextMonth = limitToNextMonth,
                blockFridays = blockFridays,
                blockThirteenth = blockThirteenth,
            )
        }
    }
}
@Composable
private fun SelectionSummaryRow(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
@Composable
private fun ConstraintSummary(
    constraints: DatePickerConstraints,
    limitToNextMonth: Boolean,
    blockFridays: Boolean,
    blockThirteenth: Boolean,
) {
    // Snapshot to local vals to enable smart cast
    val min = constraints.minDate
    val max = constraints.maxDate
    val disabled = constraints.disabledDates

    val rules = buildList {
        if (limitToNextMonth && min != null && max != null) {
            add("انتخاب تاریخ تنها بین ${min.toDisplayString()} تا ${max.toDisplayString()} امکان‌پذیر است.")
        }
        if (blockFridays) {
            add("روزهای جمعه برای انتخاب غیرفعال شده‌اند.")
        }
        if (blockThirteenth) {
            val blockedCount = disabled.size
            val suffix = if (blockedCount > 0) " ($blockedCount تاریخ)" else ""
            add("روز سیزدهم هر ماه مسدود است$suffix.")
        }
        if (!limitToNextMonth && min != null && max != null) {
            add("محدوده فعال از ${min.toDisplayString()} تا ${max.toDisplayString()} تعیین شده است.")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "محدودیت‌های فعال",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (rules.isEmpty()) {
            Text(
                text = "هیچ محدودیتی فعال نیست.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            rules.forEach { rule ->
                Text(
                    text = "• $rule",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class SoleimaniRange(
    val start: SoleimaniDate,
    val end: SoleimaniDate,
)

private fun SoleimaniRange.toDisplayString(): String {
    return "${start.toDisplayString()} تا ${end.toDisplayString()}"
}



private fun SoleimaniDate.toDisplayString(): String {
    val yearText = year.toPersianNumber()
    val monthText = FormatHelper.toPersianNumber(addLeadingZero(month))
    val dayText = FormatHelper.toPersianNumber(addLeadingZero(day))
    return "$yearText/$monthText/$dayText"
}

private fun generateThirteenthBlackouts(
    start: SoleimaniDate,
    monthsAhead: Int,
    minDate: SoleimaniDate?,
    maxDate: SoleimaniDate?,
): Set<SoleimaniDate> {
    if (monthsAhead <= 0) return emptySet()
    val blockedDates = mutableSetOf<SoleimaniDate>()
    var cursor = SoleimaniDate(start.year, start.month, 13)
    repeat(monthsAhead) {
        if ((minDate == null || cursor >= minDate) && (maxDate == null || cursor <= maxDate)) {
            blockedDates.add(cursor)
        }
        val calendar = cursor.toCalendar()
        val nextMonth = calendar.getDateByDiff(calendar.getMonthLength())
        cursor = SoleimaniDate(nextMonth.getYear(), nextMonth.getMonth(), 13)
    }
    return blockedDates
}

@Preview(showBackground = true)
@Composable
private fun CalendarShowcaseScreenPreview() {
    PersionCalendarTheme {
        CalendarShowcaseScreen()
    }
}