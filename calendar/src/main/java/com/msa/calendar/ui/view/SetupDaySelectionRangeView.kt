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
import com.msa.calendar.utils.JlResDimens
import com.msa.calendar.utils.getweekDay

@Composable
fun DayOfWeekRangeView(
    mMonth: String,
    mMonthint: String,
    mDay: String,
    mYear: String,
    startDate: List<String>,
    endDate: List<String>,
    setDay: (String) -> Unit,
    setStartDate: (List<String>) -> Unit,
    setEndDate: (List<String>) -> Unit,
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
            Text(text = "ج", color = Color.Black)
            Text(text = "پ", color = Color.Black)
            Text(text = "چ", color = Color.Black)
            Text(text = "س", color = Color.Black)
            Text(text = "د", color = Color.Black)
            Text(text = "ی", color = Color.Black)
            Text(text = "ش", color = Color.Black)
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
                items(daysList) {

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f, true)
                            .padding(4.dp)
                            .shadow(
                                color = if (mDay == it) Purple40 else PurpleGrey80,
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
                                setStartEndDates(day = it,
                                    myears = mYear,
                                    mMonth = mMonthint,
                                    mday = mDay,
                                    startDate = startDate,
                                    endDate = endDate,
                                    setstartDate = { setStartDate(it) },
                                    setendDate = { setEndDate(it) },
                                    setDay = { setDay(it) })
                            },

                        color = decideDayColor(
                            it, startDate, endDate, mYear, mMonthint, mDay
                        ),
//                    border = BorderStroke(1.dp, color = Color.White)
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = decideTextDayColor(
                                    it, startDate, endDate, mYear, mMonthint, mDay
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
    startDate: List<String>,
    endDate: List<String>,
    setstartDate: (List<String>) -> Unit,
    setendDate: (List<String>) -> Unit,
    setDay: (String) -> Unit

) {
    if (day != " ") {

        if (startDate.isEmpty()) {
            setstartDate(listOf(myears, mMonth, day))
            setDay(day)
        } else if (startDate.get(0).toInt() == myears.toInt() && startDate.get(1)
                .toInt() == mMonth.toInt() && startDate.get(2).toInt() > day.toInt()
        ) {
            setstartDate(listOf(myears, mMonth, day))
            setDay(day)
        } else if (startDate.get(0).toInt() > myears.toInt() || startDate.get(1)
                .toInt() > mMonth.toInt()
        ) {
            setstartDate(listOf(myears, mMonth, day))
            setDay(day)
        } else {
            setendDate(listOf(myears, mMonth, day))
        }
    }

}

@Composable
private fun decideTextDayColor(
    day: String,
    startDate: List<String>,
    endDate: List<String>,
    myears: String,
    mMonth: String,
    mday: String
): Color {
    if (day != " ") {
        val data = "$myears$mMonth$day"
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            if (startDate.get(0).toInt() <= myears.toInt() &&
                startDate.get(1).toInt() <= mMonth.toInt() &&
                (endDate.get(0).toInt() >= myears.toInt() &&
                        endDate.get(1).toInt() >= mMonth.toInt())
            ) {
                if (startDate.get(2).toInt() <= day.toInt() &&
                    endDate.get(2).toInt() >= day.toInt()
                )
                    return Color.White
            }
        } else if (day == mday) return Color.White
    }
    return Color.Black
}

@Composable
private fun decideDayColor(
    day: String,
    startDate: List<String>,
    endDate: List<String>,
    myears: String,
    mMonth: String,
    mday: String
): Color {
    if (day != " ") {
        val data = "$myears$mMonth$day"
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            if (startDate.get(0).toInt() < myears.toInt() &&
                startDate.get(1).toInt() < mMonth.toInt() &&
                endDate.get(0).toInt() > myears.toInt() &&
                endDate.get(1).toInt() > mMonth.toInt()
            ) {
                return Color.Blue
            } else if (
                startDate.get(0).toInt() == myears.toInt() &&
                startDate.get(1).toInt() == mMonth.toInt() &&
                endDate.get(0).toInt() == myears.toInt() &&
                endDate.get(1).toInt() == mMonth.toInt()
            ) {
                if (
                    startDate.get(2).toInt() <= day.toInt() &&
                    endDate.get(2).toInt() >= day.toInt()
                    )
                    return Color.Blue
            }
        } else if (day == mday)
            return Color.Blue
    }
    return Color.White
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