package com.msa.calendar


import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.toIntSafely


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
    var mYear by remember { mutableStateOf(baseDate.year.toPersianNumber()) }
    var mDay by remember { mutableStateOf(baseDate.day.toPersianNumber()) }


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
                    val todayMonthName = monthsList.getOrElse(todayMonth - 1) { monthsList.first() }
                    val highlightToday = remember(config.highlightToday, mMonth, mYear, constraints) {
                        if (!config.highlightToday) return@remember null
                        if (!constraints.isDateSelectable(todaySoleimani)) return@remember null
                        if (todayMonthName == mMonth && todayYear.toPersianNumber() == mYear) {
                            todayDate.toPersianNumber()
                        } else null
                    }

                    val monthIndex = remember(mMonth) { monthsList.indexOf(mMonth) }
                    val selectedDate = remember(mYear, mDay, monthIndex) {
                        val yearValue = mYear.toIntSafely() ?: todayYear
                        val monthValue = if (monthIndex >= 0) monthIndex + 1 else todayMonth
                        val dayValue = mDay.toIntSafely() ?: todayDate
                        runCatching { SoleimaniDate(yearValue, monthValue, dayValue) }
                            .getOrElse {
                                SoleimaniDate(yearValue, monthValue.coerceIn(1, 12), dayValue.coerceAtLeast(1))
                            }
                    }

                    val headerSubtitle = remember(selectedDate, mMonth, config.digitMode) {
                        val dayText = when (config.digitMode) {
                            DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(selectedDate.day))
                            DigitMode.Latin -> addLeadingZero(selectedDate.day)
                        }
                        val yearText = when (config.digitMode) {
                            DigitMode.Persian -> selectedDate.year.toPersianNumber()
                            DigitMode.Latin -> selectedDate.year.toString()
                        }
                        "$dayText $mMonth $yearText"
                    }

                    val isSelectionEnabled = remember(selectedDate, constraints) {
                        constraints.isDateSelectable(selectedDate)
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
                        colors = colors,
                        showToday = config.showTodayAction,
                        todayLabel = strings.today,
                        onTodayClick = {
                            val resolvedToday = constraints.nearestValidOrNull(todaySoleimani) ?: todaySoleimani
                            val resolvedMonthName = monthsList.getOrElse(resolvedToday.month - 1) { todayMonthName }
                            mMonth = resolvedMonthName
                            mYear = resolvedToday.year.toPersianNumber()
                            mDay = resolvedToday.day.toPersianNumber()
                            pickerType = PickerType.Day
                        }
                    )

                    Crossfade(targetState = pickerType, label = "picker") { type ->
                        when (type) {
                            PickerType.Day -> DayOfWeekView(
                                mMonth = mMonth,
                                mDay = mDay,
                                mYear = mYear,
                                highlightedDay = highlightToday,
                                highlightColor = colors.todayOutline,
                                setDay = { mDay = it },
                                isDateEnabled = { constraints.isDateSelectable(it) },
                                {}
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
                        pacer(modifier = Modifier.width(12.dp))

                        Button(
                            enabled = isSelectionEnabled,
                            onClick = {
                                if (!constraints.isDateSelectable(selectedDate)) {
                                    return@Button
                                }
                                onDateSelected(selectedDate)
                                onConfirm(config.dateFormatter.format(selectedDate, config.digitMode))
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
