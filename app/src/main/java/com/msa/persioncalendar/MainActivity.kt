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
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.JalaliDate
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.toJalaliDate
import com.msa.calendar.utils.toPersianNumber

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
    var selectedSingleDate by remember { mutableStateOf<JalaliDate?>(null) }
    var selectedRange by remember { mutableStateOf<JalaliRange?>(null) }
    var lastSelectionType by remember { mutableStateOf<SelectionType?>(null) }

    val today = remember { PersionCalendar().toJalaliDate() }

    if (showSinglePicker) {
        CalendarScreen(
            onDismiss = { showSinglePicker = false },
            onConfirm = { rawDate ->
                val parsedDate = rawDate
                    .split("/")
                    .map { it.trim() }
                    .takeIf { it.size == 3 }
                    ?.let { (year, month, day) ->
                        JalaliDate.fromLocalizedStrings(year, month, day)
                    }
                selectedSingleDate = parsedDate
                if (parsedDate != null) {
                    lastSelectionType = SelectionType.Single
                }
                showSinglePicker = false
            }
        )
    }
    if (showRangePicker) {
        RangeCalendarScreen(
            onDismiss = { showRangePicker = false },
            setDate = { dateMaps ->
                val start = dateMaps.getOrNull(0)?.toJalaliDateOrNull()
                val end = dateMaps.getOrNull(1)?.toJalaliDateOrNull()
                if (start != null && end != null) {
                    val (first, second) = if (start <= end) start to end else end to start
                    selectedRange = JalaliRange(first, second)
                    lastSelectionType = SelectionType.Range
                }
                showRangePicker = false
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
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
@Composable
private fun SelectionSummaryCard(
    selectedSingleDate: JalaliDate?,
    selectedRange: JalaliRange?,
    lastSelectionType: SelectionType?,
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

private data class JalaliRange(
    val start: JalaliDate,
    val end: JalaliDate,
)

private fun JalaliRange.toDisplayString(): String {
    return "${start.toDisplayString()} تا ${end.toDisplayString()}"
}

private fun Map<String, String>.toJalaliDateOrNull(): JalaliDate? {
    val year = this["year"] ?: return null
    val month = this["month"] ?: return null
    val day = this["day"] ?: return null
    return JalaliDate.fromLocalizedStrings(year, month, day)
}

private fun JalaliDate.toDisplayString(): String {
    val yearText = year.toPersianNumber()
    val monthText = FormatHelper.toPersianNumber(addLeadingZero(month))
    val dayText = FormatHelper.toPersianNumber(addLeadingZero(day))
    return "$yearText/$monthText/$dayText"
}

@Preview(showBackground = true)
@Composable
private fun CalendarShowcaseScreenPreview() {
    PersionCalendarTheme {
        CalendarShowcaseScreen()
    }
}