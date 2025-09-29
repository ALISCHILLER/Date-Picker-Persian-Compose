package com.msa.calendar


import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.calendar.ui.view.CalendarView
import com.msa.calendar.ui.view.DayOfWeekView
import com.msa.calendar.ui.view.MonthView
import com.msa.calendar.ui.view.YearsView
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.PickerType
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.ui.DatePickerQuickAction

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.LaunchedEffect
import com.msa.calendar.utils.adjustDayIfOutOfBounds

@Composable
fun CalendarScreen(
    onDismiss: (Boolean) -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: SoleimaniDate? = null,
    config: DatePickerConfig = DatePickerConfig(),
    onDateSelected: (SoleimaniDate) -> Unit = {},
) {
    val strings = config.strings
    val colors = config.colors
    val constraints = config.constraints
    val weekConfiguration = config.weekConfiguration
    val quickActions = remember(config.quickActions, config.showTodayAction) {
        when {
            config.quickActions.isNotEmpty() -> config.quickActions
            config.showTodayAction -> listOf(DatePickerQuickAction.Today)
            else -> emptyList()
        }
    }
    val shape: Shape = config.containerShape
    val todayCalendar = remember { PersionCalendar() }

    val todayDate = remember { todayCalendar.getDay() }
    val todayMonth = remember { todayCalendar.getMonth() }
    val todayYear = remember { todayCalendar.getYear() }
    val todaySoleimani = remember { SoleimaniDate(todayYear, todayMonth, todayDate) }

    val baseDate = remember(initialDate, constraints) {
        val fallback = initialDate ?: todaySoleimani
        constraints.nearestValidOrNull(fallback)
            ?: constraints.minDate
            ?: constraints.maxDate
            ?: fallback
    }

    var pickerType: PickerType by remember { mutableStateOf(PickerType.Day) }

    var selectedYear by remember { mutableStateOf(baseDate.year) }
    var selectedMonth by remember { mutableStateOf(baseDate.month.coerceIn(1, 12)) }
    var selectedDay by remember { mutableStateOf<Int?>(baseDate.day) }

    LaunchedEffect(selectedMonth, selectedYear) {
        adjustDayIfOutOfBounds(
            dayValue = selectedDay,
            month = selectedMonth,
            year = selectedYear,
        )?.let { coerced ->
            selectedDay = coerced
        }
    }

    fun updateSelectionFromDate(target: SoleimaniDate) {
        selectedYear = target.year
        selectedMonth = target.month.coerceIn(1, 12)
        selectedDay = target.day
        pickerType = PickerType.Day
    }

    Dialog(onDismissRequest = { onDismiss(true) }) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onDismiss(true) }
            )
            Surface(
                modifier = modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .widthIn(min = 280.dp, max = 420.dp),
                shape = shape,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                shadowElevation = 24.dp,
                color = colors.containerColor,
            ) {

                Column(
                    modifier = Modifier
                        .animateContentSize()
                ) {

                    val monthLabel = remember(selectedMonth, config.monthFormatter, config.digitMode) {
                        config.monthFormatter.format(selectedMonth, config.digitMode)
                    }

                    val yearLabel = remember(selectedYear, config.yearFormatter, config.digitMode) {
                        config.yearFormatter.format(selectedYear, config.digitMode)
                    }

                    val selectedDate = remember(selectedYear, selectedMonth, selectedDay) {
                        selectedDay?.let { day ->
                            runCatching { SoleimaniDate(selectedYear, selectedMonth, day) }.getOrNull()
                        }
                    }
                    val headerSubtitle = remember(selectedDate, monthLabel, yearLabel, config.digitMode, strings) {
                        selectedDate?.let { date ->
                            val dayText = when (config.digitMode) {
                                DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(date.day))
                                DigitMode.Latin -> addLeadingZero(date.day)
                            }
                            "$dayText $monthLabel $yearLabel"
                        } ?: strings.title
                    }
                    val highlightToday = remember(config.highlightToday, selectedMonth, selectedYear, constraints) {
                        if (!config.highlightToday) return@remember null
                        if (!constraints.isDateSelectable(todaySoleimani)) return@remember null
                        if (todaySoleimani.month == selectedMonth && todaySoleimani.year == selectedYear) {
                            todaySoleimani
                        } else null
                    }
                    val isSelectionEnabled = remember(selectedDate, constraints) {
                        selectedDate?.let(constraints::isDateSelectable) == true
                    }
                    val effectiveYearRange = remember(config.yearRange, selectedYear, todayYear, constraints) {
                        val candidates = mutableListOf(
                            config.yearRange.first,
                            config.yearRange.last,
                            selectedYear,
                            todayYear,
                        )
                        constraints.minDate?.let { candidates += it.year }
                        constraints.maxDate?.let { candidates += it.year }
                        val minYear = candidates.minOrNull() ?: selectedYear
                        val maxYear = candidates.maxOrNull() ?: selectedYear
                        minYear..maxYear
                    }
                    CalendarView(
                        monthLabel = monthLabel,
                        yearLabel = yearLabel,
                        pickerTypeChang = { pickerType = it },
                        pickerType = pickerType,
                        onPreviousMonth = {
                            if (selectedMonth == 1) {
                                selectedMonth = 12
                                selectedYear -= 1
                            } else {
                                selectedMonth -= 1
                            }
                        },
                        onNextMonth = {
                            if (selectedMonth == 12) {
                                selectedMonth = 1
                                selectedYear += 1
                            } else {
                                selectedMonth += 1
                            }
                        },
                        title = strings.title,
                        subtitle = headerSubtitle,
                        strings = strings,
                        colors = colors,
                        quickActions = quickActions,
                        onQuickActionClick = quick@  { action ->
                            when (action) {
                                DatePickerQuickAction.Today -> {
                                    val resolvedToday = constraints.nearestValidOrNull(todaySoleimani) ?: todaySoleimani
                                    updateSelectionFromDate(resolvedToday)
                                }

                                is DatePickerQuickAction.ClearSelection -> {
                                    selectedDay = null
                                    pickerType = PickerType.Day
                                }

                                is DatePickerQuickAction.JumpToDate -> {
                                    val target = action.targetDateProvider() ?: return@quick
                                    val resolved = constraints.nearestValidOrNull(target) ?: target
                                    updateSelectionFromDate(resolved)
                                }
                            }
                        },
                    )

                    Crossfade(targetState = pickerType, label = "picker") { type ->
                        when (type) {
                            PickerType.Day -> DayOfWeekView(
                                month = selectedMonth,
                                selectedDay = selectedDay,
                                year = selectedYear,
                                highlightedDate = highlightToday,
                                highlightColor = colors.todayOutline,
                                weekConfiguration = weekConfiguration,
                                digitMode = config.digitMode,
                                weekendLabelColor = colors.weekendLabelColor,
                                eventIndicator = config.eventIndicator,
                                onDaySelected = { day -> selectedDay = day },
                                isDateEnabled = { constraints.isDateSelectable(it) },
                                changeSelectedPart = {}
                            )

                            PickerType.Year -> YearsView(
                                selectedYear = selectedYear,
                                digitMode = config.digitMode,
                                yearFormatter = config.yearFormatter,
                                yearRange = effectiveYearRange,
                                colors = colors,
                                onYearClick = { yearValue ->
                                    selectedYear = yearValue
                                },
                            )

                            PickerType.Month -> MonthView(
                                selectedMonth = selectedMonth,
                                displayedYear = selectedYear,
                                digitMode = config.digitMode,
                                monthFormatter = config.monthFormatter,
                                colors = colors,
                                onMonthSelected = { monthValue ->
                                    selectedMonth = monthValue
                                    pickerType = PickerType.Day
                                },
                            )

                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        thickness = 1.dp,
                        color = colors.cancelButtonContent.copy(alpha = 0.12f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onDismiss(true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = colors.todayButtonBackground,
                                contentColor = colors.cancelButtonContent
                            )
                        ) {
                            Text(text = strings.cancel)
                        }

                        Button(
                            enabled = isSelectionEnabled,
                            onClick = {
                                val confirmed = selectedDate ?: return@Button
                                if (!constraints.isDateSelectable(confirmed)) {
                                    return@Button
                                }
                                onDateSelected(confirmed)
                                onConfirm(config.dateFormatter.format(confirmed, config.digitMode))
                                onDismiss(true)
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.confirmButtonBackground,
                                contentColor = colors.confirmButtonContent,
                            )
                        ) {
                            Text(text = strings.confirm)
                        }
                    }
                }
            }
        }
    }

}


@Composable
@Preview(showBackground = true)
fun CalendarScreenPreview() {
    var hideDatePicker by remember {
        mutableStateOf(true)
    }
    CalendarScreen(
        onDismiss = { hideDatePicker = true },
        onConfirm = {},
        config = DatePickerConfig(
            strings = DatePickerStrings(title = "Test Title"),
            digitMode = DigitMode.Persian,
            showTodayAction = true,
            highlightToday = true,
        )
    )
}
