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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    setDate: (List<Map<String, String>>) -> Unit
) {
    val todayCalendar = remember { PersionCalendar() }
    val today = todayCalendar.getDay()
    val month = todayCalendar.getMonth()
    val year = todayCalendar.getYear()
    val initialMonthName = monthsList.getOrElse(month - 1) { monthsList.first() }

    var mMonth by remember { mutableStateOf(initialMonthName) }
    var mMonthin by remember { mutableStateOf(month.toPersianNumber()) }
    var mYear by remember { mutableStateOf(year.toPersianNumber()) }
    var mDay by remember { mutableStateOf(today.toPersianNumber()) }

    var pickerType: PickerType by remember { mutableStateOf(PickerType.Day) }

    var startDate by remember { mutableStateOf<JalaliDate?>(null) }
    var endDate by remember { mutableStateOf<JalaliDate?>(null) }

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
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
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
                        setYear = { mYear = it }
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
                                setEndDate = { endDate = it }
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

                    val isRangeComplete = startDate != null && endDate != null

                    Row {
                        TextButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            enabled = isRangeComplete,
                            onClick = {
                                if (!isRangeComplete) return@TextButton
                                setDate(
                                    listOf(
                                        startDate!!.toMap(usePersianDigits = false),
                                        endDate!!.toMap(usePersianDigits = false)
                                    )
                                )
                                onDismiss(true)
                            }
                        ) {
                            Text(text = "تایید")
                        }

                        TextButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            onClick = { onDismiss(true) }
                        ) {
                            Text(text = "انصراف")
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
        setDate = { _ -> } // ✅ accept the parameter
    )
}
