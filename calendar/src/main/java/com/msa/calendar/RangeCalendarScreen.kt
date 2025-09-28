package com.msa.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.view.CalendarView
import com.msa.calendar.ui.view.DayOfWeekRangeView
import com.msa.calendar.ui.view.MonthView
import com.msa.calendar.ui.view.YearsView
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.PickerType
import com.msa.calendar.utils.toPersianNumber
import com.msa.calendar.utils.*
import com.msa.calendar.utils.monthsList

@Composable
fun RangeCalendarScreen(
    onDismiss: (Boolean) -> Unit,
    setDate: (List<Map<String, String>>) -> Unit,
    modifier: Modifier = Modifier,
    initialStartDate: SoleimaniDate? = null,
    initialEndDate: SoleimaniDate? = null,
    config: DatePickerConfig = DatePickerConfig(),
    onRangeSelected: (SoleimaniDate, SoleimaniDate) -> Unit = { _, _ -> },
) {
    val todayCalendar = remember { PersionCalendar() }
    val todayDay = todayCalendar.getDay()
    val todayMonth = todayCalendar.getMonth()
    val todayYear = todayCalendar.getYear()
    val todayDate = remember { SoleimaniDate(todayYear, todayMonth, todayDay) }
    val initialMonthName = monthsList.getOrElse(todayMonth - 1) { monthsList.first() }

    val constraints = config.constraints

    val initialStart = remember(initialStartDate, constraints) {
        val desired = initialStartDate ?: todayDate
        constraints.nearestValidOrNull(desired)
            ?: constraints.minDate
            ?: constraints.maxDate
            ?: desired
    }
    val initialEnd = remember(initialEndDate, constraints) {
        initialEndDate?.let { candidate ->
            constraints.nearestValidOrNull(candidate)
        }
    }

    var pickerType: PickerType by remember { mutableStateOf(PickerType.Day) }

    var startDate by remember { mutableStateOf<SoleimaniDate?>(initialStart) }
    var endDate by remember { mutableStateOf<SoleimaniDate?>(initialEnd) }

    var mMonth by remember { mutableStateOf(monthsList.getOrElse(initialStart.month - 1) { initialMonthName }) }
    var mMonthin by remember { mutableStateOf(initialStart.month.toPersianNumber()) }
    var mYear by remember { mutableStateOf(initialStart.year.toPersianNumber()) }
    var mDay by remember { mutableStateOf(initialStart.day.toPersianNumber()) }

    val strings = config.strings
    val colors = config.colors
    val shape: Shape = config.containerShape

    // ✅ CLOSE THIS LAMBDA
    val updateMonthState: (String) -> Unit = { selectedMonth ->
        mMonth = selectedMonth
        val monthIndex = monthsList.indexOf(selectedMonth)
        if (monthIndex >= 0) {
            mMonthin = (monthIndex + 1).toPersianNumber()
        }
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

                    CalendarView(
                        mMonth = mMonth,
                        mDay = mDay,
                        mYear = mYear,
                        pickerTypeChang = { pickerType = it },
                        pickerType = pickerType,
                        setDay = { mDay = it },
                        setMonth = { monthName -> updateMonthState(monthName) },
                        setYear = { mYear = it },
                        title = strings.title,
                        subtitle = buildRangeSubtitle(strings, startDate, endDate, config.digitMode),
                        colors = colors,
                        showToday = config.showTodayAction,
                        todayLabel = strings.today,
                        onTodayClick = {
                            val resolvedToday = constraints.nearestValidOrNull(todayDate)
                            val reference = resolvedToday ?: constraints.minDate ?: startDate
                            if (reference != null) {
                                val resolved = resolvedToday ?: reference
                                val todayMonthName = monthsList.getOrElse(resolved.month - 1) { monthsList.first() }
                                mMonth = todayMonthName
                                mMonthin = resolved.month.toPersianNumber()
                                mYear = resolved.year.toPersianNumber()
                                mDay = resolved.day.toPersianNumber()
                                startDate = resolved
                                endDate = resolved
                                pickerType = PickerType.Day
                            }
                        }
                    )

                    Crossfade(targetState = pickerType, label = "") { state ->
                        when (state) {
                            PickerType.Day -> DayOfWeekRangeView(
                                mMonth = mMonth,
                                mMonthint = mMonthin, // it's already a String
                                mDay = mDay,
                                mYear = mYear,
                                startDate = startDate,
                                endDate = endDate,
                                setDay = { mDay = it },
                                setStartDate = { startDate = it },
                                setEndDate = { endDate = it },
                                isDateEnabled = { constraints.isDateSelectable(it) },
                            ) {}

                            PickerType.Year -> YearsView(
                                mYear = mYear,
                                onYearClick = { mYear = it },
                            )

                            PickerType.Month -> MonthView(
                                mMonth = mMonth,
                                onMonthClick = { updateMonthState(it) },
                                setMonth = { mMonthin = it }
                            )
                        }
                    }

                    val isRangeComplete = startDate != null && endDate != null &&
                            startDate?.let { constraints.isDateSelectable(it) } == true &&
                            endDate?.let { constraints.isDateSelectable(it) } == true


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
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
                            enabled = isRangeComplete,
                            onClick = {
                                val start = startDate ?: return@Button
                                val end = endDate ?: return@Button
                                val ordered = if (start <= end) start to end else end to start
                                if (!constraints.isDateSelectable(ordered.first) || !constraints.isDateSelectable(ordered.second)) {
                                    return@Button
                                }
                                onRangeSelected(ordered.first, ordered.second)
                                setDate(
                                    listOf(
                                        ordered.first.toMap(usePersianDigits = config.digitMode == DigitMode.Persian),
                                        ordered.second.toMap(usePersianDigits = config.digitMode == DigitMode.Persian)
                                    )
                                )
                                onDismiss(true)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.confirmButtonBackground,
                                contentColor = colors.confirmButtonContent,
                                disabledContainerColor = colors.confirmButtonBackground.copy(alpha = 0.3f),
                                disabledContentColor = colors.confirmButtonContent.copy(alpha = 0.4f)
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

@Preview(showBackground = true)
@Composable
fun RangeCalendarScreenPreview() {
    var hideDatePicker by remember { mutableStateOf(true) }
    RangeCalendarScreen(
        onDismiss = { hideDatePicker = true },
        setDate = { _ -> },
        config = DatePickerConfig(
            strings = DatePickerStrings(title = "Range Picker"),
            digitMode = DigitMode.Persian,
        )
    )
}

private fun buildRangeSubtitle(
    strings: DatePickerStrings,
    startDate: SoleimaniDate?,
    endDate: SoleimaniDate?,
    digitMode: DigitMode,
): String {
    val start = startDate?.format(digitMode)
    val end = endDate?.format(digitMode)
    return when {
        start == null && end == null -> strings.title
        start != null && end == null -> "${strings.rangeStartLabel}: $start"
        start == null && end != null -> "${strings.rangeEndLabel}: $end"
        start != null && end != null -> "$start - $end"
        else -> strings.title
    }
}

private fun SoleimaniDate.format(mode: DigitMode): String {
    val yearText = when (mode) {
        DigitMode.Persian -> year.toPersianNumber()
        DigitMode.Latin -> year.toString()
    }
    val monthText = when (mode) {
        DigitMode.Persian -> addLeadingZero(month).let { FormatHelper.toPersianNumber(it) }
        DigitMode.Latin -> addLeadingZero(month)
    }
    val dayText = when (mode) {
        DigitMode.Persian -> addLeadingZero(day).let { FormatHelper.toPersianNumber(it) }
        DigitMode.Latin -> addLeadingZero(day)
    }
    return "$yearText/$monthText/$dayText"
}