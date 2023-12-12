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
import androidx.compose.runtime.mutableStateListOf
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




@Composable
fun RangeCalendarScreen(
    onDismiss: (Boolean) -> Unit,
    setDate : (List<Map<String, String>>) -> Unit
) {
    val today = PersionCalendar().getDay()
    val month = PersionCalendar().getMonth()
    val year = PersionCalendar().getYear()
    var monthh:String
    val monthsList = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
        "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )
    var mMonth by remember {
        mutableStateOf(monthsList[month - 1])
    }

    var mYear by remember {
        mutableStateOf(year.toPersianNumber())
    }

    var mDay by remember {
        mutableStateOf(today.toPersianNumber())
    }
    var pickerType: PickerType by remember {
        mutableStateOf(PickerType.Day)
    }

    var startDate  by remember {
        mutableStateOf(mutableListOf<String>())
    }
    var endDate by remember {
        mutableStateOf(mutableListOf<String>())
    }
    Dialog(
        onDismissRequest = { onDismiss(true) },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    // same action as in onDismissRequest
                    onDismiss(true)
                }
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
                        setDay = {mDay = it},
                        setMonth = {mMonth = it},
                        setYear = {mYear = it}
                    )

                    Crossfade(pickerType, label = "") { it ->
                        when (it) {
                            PickerType.Day -> DayOfWeekRangeView(
                                mMonth = mMonth,
                                mDay = mDay,
                                mYear = mYear,
                                startDate = startDate,
                                endDate =  endDate,
                                setStartDate = {startDate= it.toMutableList() },
                                setEndDate = {endDate= it.toMutableList() }
                            ) {}

                            PickerType.Year -> YearsView(
                                mYear = mYear,
                                onYearClick = { mYear = it },
                            )

                            PickerType.Month -> MonthView(
                                mMonth = mMonth,
                                onMonthClick = { mMonth = it }
                            )

                        }
                    }
                    Row() {
                        TextButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            onClick = {
                                monthh  = (monthsList.indexOf(mMonth) + 1).toPersianNumber()
                               // onConfirm("$mYear / $monthh / $mDay")
                                onDismiss(true)
                            }) {
                            Text(text = "تایید")
                        }
                        TextButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            onClick = {  onDismiss(true) }) {
                            Text(text = "انصراف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun RangeCalendarScreenPreview() {
    var hideDatePicker by remember {
        mutableStateOf(true)
    }
    RangeCalendarScreen(
        onDismiss = { hideDatePicker = true },
        {}
    )
}