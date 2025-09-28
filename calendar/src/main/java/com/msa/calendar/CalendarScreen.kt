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
import com.msa.calendar.utils.toPersianNumber
import com.msa.calendar.utils.monthsList
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.toIntSafely
import com.msa.calendar.ui.DatePickerQuickAction

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

    var mMonth by remember {
        mutableStateOf(monthsList.getOrElse(baseDate.month - 1) { monthsList.first() })
    }
    var mYear by remember { mutableStateOf(baseDate.year.toDigitString(config.digitMode)) }
    var mDay by remember { mutableStateOf(baseDate.day.toDigitString(config.digitMode)) }

    fun updateSelectionFromDate(target: SoleimaniDate) {
        val monthName = monthsList.getOrElse(target.month - 1) { monthsList.first() }
        mMonth = monthName
        mYear = target.year.toDigitString(config.digitMode)
        mDay = target.day.toDigitString(config.digitMode)
        pickerType = PickerType.Day
    }

    Dialog(onDismissRequest = { onDismiss(true) }) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onDismiss(true) }
        ) {
            Surface(
                modifier = modifier,
                shape = shape,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                color = colors.containerColor,
            ) {

                Column(
                    modifier = Modifier
                        .animateContentSize()
                ) {

                    val highlightToday = remember(config.highlightToday, mMonth, mYear, constraints) {
                        if (!config.highlightToday) return@remember null
                        if (!constraints.isDateSelectable(todaySoleimani)) return@remember null
                        val monthIndex = monthsList.indexOf(mMonth)
                        val yearValue = mYear.toIntSafely()
                        if (monthIndex >= 0 && yearValue != null &&
                            todaySoleimani.month == monthIndex + 1 &&
                            todaySoleimani.year == yearValue
                        ) {
                            todaySoleimani
                        } else null
                    }

                    val monthIndex = remember(mMonth) { monthsList.indexOf(mMonth) }
                    val selectedDate = remember(mYear, mDay, monthIndex) {
                        val yearValue = mYear.toIntSafely()
                        val monthValue = if (monthIndex >= 0) monthIndex + 1 else null
                        val dayValue = mDay.toIntSafely()
                        if (yearValue != null && monthValue != null && dayValue != null) {
                            runCatching { SoleimaniDate(yearValue, monthValue, dayValue) }.getOrNull()
                        } else null
                    }

                    val headerSubtitle = remember(selectedDate, mMonth, config.digitMode, strings) {
                        selectedDate?.let { date ->
                            val dayText = when (config.digitMode) {
                                DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(date.day))
                                DigitMode.Latin -> addLeadingZero(date.day)
                            }
                            val yearText = when (config.digitMode) {
                                DigitMode.Persian -> date.year.toPersianNumber()
                                DigitMode.Latin -> date.year.toString()
                            }
                            "$dayText $mMonth $yearText"
                        } ?: strings.title
                    }

                    val isSelectionEnabled = remember(selectedDate, constraints) {
                        selectedDate?.let(constraints::isDateSelectable) == true
                    }

                    CalendarView(
                        mMonth = mMonth,
                        mDay = mDay,
                        mYear = mYear,
                        pickerTypeChang = { pickerType = it },
                        pickerType = pickerType,
                        setDay = { mDay = it },
                        setMonth = { mMonth = it },
                        setYear = { mYear = it },
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
                                    mDay = ""
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
                                mMonth = mMonth,
                                mDay = mDay,
                                mYear = mYear,
                                highlightedDate = highlightToday,
                                highlightColor = colors.todayOutline,
                                weekConfiguration = weekConfiguration,
                                digitMode = config.digitMode,
                                weekendLabelColor = colors.weekendLabelColor,
                                eventIndicator = config.eventIndicator,
                                setDay = { mDay = it },
                                isDateEnabled = { constraints.isDateSelectable(it) },
                                changeSelectedPart = {}
                            )

                            PickerType.Year -> YearsView(
                                mYear = mYear,
                                onYearClick = { mYear = it },
                            )

                            PickerType.Month -> MonthView(
                                mMonth = mMonth,
                                onMonthClick = { mMonth = it },
                                {}
                            )

                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = { onDismiss(true) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colors.cancelButtonContent
                            )
                        ) {
                            Text(text = strings.cancel)
                        }
                        Spacer(modifier = Modifier.width(12.dp))

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

private fun Int.toDigitString(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> toPersianNumber()
    DigitMode.Latin -> toString()
}