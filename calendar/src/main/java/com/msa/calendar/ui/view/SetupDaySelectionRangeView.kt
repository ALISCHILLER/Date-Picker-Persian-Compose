package com.msa.calendar.ui.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.components.shadow
import com.msa.calendar.ui.theme.Purple40
import com.msa.calendar.ui.theme.PurpleGrey80
import com.msa.calendar.utils.*

@Composable
fun DayOfWeekRangeView(
    mMonth: String,
    mMonthint: String,
    mDay: String,
    mYear: String,
    startDate: List<Int>,
    endDate: List<Int>,
    setDay: (String) -> Unit,
    setStartDate: (List<Int>) -> Unit,
    setEndDate: (List<Int>) -> Unit,
    changeSelectedPart: (String) -> Unit
) {
    val daysList = getweekDay(mMonth, mYear)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("ج", "پ", "چ", "س", "د", "ی", "ش").forEach { day ->
                Text(text = day, color = Color.Black)
            }
        }

        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                items(daysList) { day ->

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f, true)
                            .padding(4.dp)
                            .shadow(
                                color = if (mDay == day) Purple40 else PurpleGrey80,
                                borderRadius = 10.dp,
                                offsetX = 0.0.dp,
                                offsetY = 3.dp,
                                spread = 3.dp,
                                blurRadius = 10.0.dp
                            )
                            .border(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    )
                                ),
                                width = JlResDimens.dp1,
                                shape = RoundedCornerShape(JlResDimens.dp10)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                setStartEndDates(
                                    day,
                                    mYear,
                                    mMonthint,
                                    mDay,
                                    startDate,
                                    endDate,
                                    setStartDate,
                                    setEndDate,
                                    setDay
                                )
                            },

                        color = decideDayColor(day, startDate, endDate, mYear, mMonthint, mDay),
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodyLarge,
                                color = decideTextDayColor(
                                    day,
                                    startDate,
                                    endDate,
                                    mYear,
                                    mMonthint,
                                    mDay
                                ),
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Cursive
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun setStartEndDates(
    day: String,
    myears: String,
    mMonth: String,
    mday: String,
    startDate: List<Int>,
    endDate: List<Int>,
    setstartDate: (List<Int>) -> Unit,
    setendDate: (List<Int>) -> Unit,
    setDay: (String) -> Unit
) {
    if (day.isNotEmpty()) {
        val targetDateList = listOf(myears.toInt(), mMonth.toInt(), day.toInt())


        fun isDateBefore() = startDate.isEmpty() || startDate.compareTo(targetDateList) > 0
        fun isDateAfter() = endDate.isEmpty() || endDate.compareTo(targetDateList) < 0

        if (isDateBefore()) {
            setstartDate(targetDateList)
            setDay(day)
        } else if (isDateAfter()) {
                setendDate(targetDateList)
        }
    }
}

private fun decideTextDayColor(
    day: String,
    startDate: List<Int>,
    endDate: List<Int>,
    myears: String,
    mMonth: String,
    mday: String
): Color {
    if (day != " ") {
        val targetDate = PersionCalendar(myears.toInt(), mMonth.toInt(), day.toInt())
        if (startDate.isNotEmpty() && endDate.isNotEmpty() && isDateInRange(
                targetDate,
                startDate,
                endDate
            )
        ) {
            return Color.White
        } else if (day == mday && endDate.isEmpty()) {
            return Color.White
        }
    }
    return Color.Black
}

private fun decideDayColor(
    day: String,
    startDate: List<Int>,
    endDate: List<Int>,
    myears: String,
    mMonth: String,
    mday: String
): Color {
    if (day != " ") {
        val targetDate = PersionCalendar(myears.toInt(), mMonth.toInt(), day.toInt())
        if (startDate.isNotEmpty() && endDate.isNotEmpty() && isDateInRange(
                targetDate,
                startDate,
                endDate
            )
        ) {
            println("تاریخ مشخص شده در بازه تاریخ‌ها قرار دارد.")
            return Color.Blue
        } else if (day == mday && endDate.isEmpty()) {
            return Color.Blue
        } else {
            println("تاریخ مشخص شده خارج از بازه تاریخ‌ها است.")
        }
    }
    return Color.White
}

fun isDateInRange(targetDate: PersionCalendar, startDate: List<Int>, endDate: List<Int>): Boolean {
    return targetDate.isInRange(startDate, endDate)
}

fun PersionCalendar.isInRange(start: List<Int>, end: List<Int>): Boolean {
    val targetYear = this.getYear()
    val targetMonth = this.getMonth()
    val targetDay = this.getDay()

    val startYear = start[0]
    val startMonth = start[1]
    val startDay = start[2]

    val endYear = end[0]
    val endMonth = end[1]
    val endDay = end[2]

    return when {
        targetYear < startYear || targetYear > endYear -> false
        targetYear == startYear && targetMonth < startMonth -> false
        targetYear == startYear && targetMonth == startMonth && targetDay < startDay -> false
        targetYear == endYear && targetMonth > endMonth -> false
        targetYear == endYear && targetMonth == endMonth && targetDay > endDay -> false
        else -> true
    }
}


@Preview
@Composable
fun DayOfWeekRangeViewPreview() {
    DayOfWeekRangeView(mMonth = "5",
        mMonthint = "5",
        mDay = "10",
        mYear = "2024",
        startDate = listOf(),
        endDate = listOf(),
        setDay = {},
        setStartDate = {},
        setEndDate = {}) {}
}