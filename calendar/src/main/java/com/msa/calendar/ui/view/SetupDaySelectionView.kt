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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.components.shadow
import com.msa.calendar.ui.theme.*
import com.msa.calendar.utils.JlResDimens
import com.msa.calendar.utils.monthsList
import com.msa.calendar.utils.toIntSafely
import com.msa.calendar.utils.SoleimaniDate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.theme.Purple40
import com.msa.calendar.ui.theme.PurpleGrey80
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.buildMonthCells
import com.msa.calendar.utils.toPersianNumber

@Composable
fun DayOfWeekView(
    mMonth: String,
    mDay: String,
    mYear: String,
    highlightedDate: SoleimaniDate?,
    highlightColor: Color,
    weekConfiguration: WeekConfiguration,
    digitMode: DigitMode,
    weekendLabelColor: Color,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
    setDay: (String) -> Unit,
    isDateEnabled: (SoleimaniDate) -> Boolean = { true },
    changeSelectedPart: (String) -> Unit = {},
) {

    val monthCells = remember (mMonth, mYear, weekConfiguration.startDay) {
        buildMonthCells(mMonth, mYear, weekConfiguration.startDay)
    }
    val selectedDayValue = remember(mDay) { mDay.toIntSafely() }
    val selectedMonthNumber = remember(mMonth) {
        monthsList.indexOf(mMonth).takeIf { it >= 0 }?.plus(1)
    }
    val orderedWeekDays = remember(weekConfiguration) { weekConfiguration.orderedDays }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            orderedWeekDays.forEach { day ->
                val isWeekend = day in weekConfiguration.weekendDays
                Text(
                    text = weekConfiguration.dayLabelFormatter.format(day),
                    color = if (isWeekend) weekendLabelColor else Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.Top,
                horizontalArrangement = Arrangement.Center,
            ) {
                items(monthCells) { cell ->
                    val candidateDate = cell.date
                    val isEnabled = candidateDate?.let(isDateEnabled) ?: false
                    val isSelected = isEnabled &&
                            candidateDate != null &&
                            selectedDayValue != null &&
                            candidateDate.day == selectedDayValue &&
                            selectedMonthNumber == candidateDate.month
                    val isToday = highlightedDate != null && candidateDate == highlightedDate
                    val isWeekend = candidateDate != null && cell.dayOfWeek in weekConfiguration.weekendDays
                    val event = candidateDate?.let(eventIndicator)

                    val shadowColor = when {
                        isSelected -> Purple40
                        isToday && isEnabled -> highlightColor
                        else -> PurpleGrey80
                    }

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f, true)
                            .padding(4.dp)
                            .shadow(
                                color = if (isEnabled) shadowColor else PurpleGrey80.copy(alpha = 0.3f),
                                borderRadius = 10.dp,
                                offsetX = 0.dp,
                                offsetY = 3.dp,
                                spread = 3.dp,
                                blurRadius = 10.dp,
                            )
                            .let { modifier ->
                                if (isToday && !isSelected && isEnabled) {
                                    modifier.border(
                                        BorderStroke(
                                            width = JlResDimens.dp1,
                                            brush = SolidColor(highlightColor),
                                        ),
                                        shape = RoundedCornerShape(JlResDimens.dp10),
                                    )
                                } else {
                                    modifier
                                }
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                enabled = isEnabled && candidateDate != null,
                                onClick = {
                                    val selected = candidateDate ?: return@clickable
                                    changeSelectedPart("main")
                                    val formattedDay = when (digitMode) {
                                        DigitMode.Persian -> selected.day.toPersianNumber()
                                        DigitMode.Latin -> selected.day.toString()
                                    }
                                    setDay(formattedDay)
                                },
                            ),
                        color = when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            else -> Color.White
                        },
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .semantics {
                                    if (event?.label != null) {
                                        contentDescription = event.label
                                    }
                                },
                        ) {
                            Text(
                                text = cell.dayOfMonth?.let { day ->
                                    when (digitMode) {
                                        DigitMode.Persian -> FormatHelper.toPersianNumber(day.toString())
                                        DigitMode.Latin -> day.toString()
                                    }
                                } ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    isToday && isEnabled -> highlightColor
                                    isWeekend -> weekendLabelColor
                                    else -> Color.Black
                                },
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Cursive,
                                modifier = Modifier.align(Alignment.Center),

                            )
                            if (event != null && candidateDate != null && isEnabled) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .border(
                                            BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                            CircleShape,
                                        )
                                        .background(event.color),
                                )
                            }
                        }

                    }
                }
            }
        }
    }


}


@Preview
@Composable
private fun DayOfWeekViewPreview() {
    DayOfWeekView(
        mMonth = monthsList[4],
        mDay = "10",
        mYear = "1403",
        highlightedDate = SoleimaniDate(1403, 5, 8),
        highlightColor = Color(0xFF3B82F6),
        weekConfiguration = WeekConfiguration(),
        digitMode = DigitMode.Persian,
        weekendLabelColor = Color(0xFFEF4444),
        eventIndicator = { date ->
            if (date.day == 1) CalendarEvent(Color(0xFF10B981), "شروع ماه") else null
        },
        setDay = {},
    )
}