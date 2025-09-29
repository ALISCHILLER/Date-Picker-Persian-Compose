package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.components.shadow
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.theme.Purple40
import com.msa.calendar.ui.theme.PurpleGrey80
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.JlResDimens
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.buildMonthCells

@Composable
fun DayOfWeekView(
    month: Int,
    selectedDay: Int?,
    year: Int,
    highlightedDate: SoleimaniDate?,
    highlightColor: Color,
    weekConfiguration: WeekConfiguration,
    digitMode: DigitMode,
    weekendLabelColor: Color,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
    onDaySelected: (Int) -> Unit,
    isDateEnabled: (SoleimaniDate) -> Boolean = { true },
    changeSelectedPart: (String) -> Unit = {},
) {
    val monthCells = remember(month, year, weekConfiguration.startDay) {
        buildMonthCells(month, year, weekConfiguration.startDay)
    }
    val selectedDayValue = selectedDay
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
                    color = if (isWeekend) weekendLabelColor else MaterialTheme.colorScheme.onSurface,
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
                            candidateDate.month == month
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
                                    onDaySelected(selected.day)
                                },
                            ),
                        color = when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.primary
                            isWeekend -> weekendLabelColor.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .semantics {
                                    if (event?.label != null) contentDescription = event.label
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
                                    else -> MaterialTheme.colorScheme.onSurface
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
    val colors = DatePickerDefaults.lightColors()
    DayOfWeekView(
        month = 5,
        selectedDay = 10,
        year = 1403,
        highlightedDate = SoleimaniDate(1403, 5, 8),
        highlightColor = colors.todayOutline,
        weekConfiguration = WeekConfiguration(),
        digitMode = DigitMode.Persian,
        weekendLabelColor = colors.weekendLabelColor,
        eventIndicator = { date ->
            if (date.day == 1) CalendarEvent(Color(0xFF10B981), "شروع ماه") else null
        },
        onDaySelected = {},
        isDateEnabled = { true },
    )
}
