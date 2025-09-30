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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.msa.calendar.components.shadow
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.JlResDimens
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.buildMonthCells

@Composable
fun DayOfWeekRangeView(
    month: Int,
    selectedDay: Int?,
    year: Int,
    startDate: SoleimaniDate?,
    endDate: SoleimaniDate?,
    weekConfiguration: WeekConfiguration,
    digitMode: DigitMode,
    weekendLabelColor: Color,
    highlightColor: Color,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
    onDaySelected: (Int?) -> Unit,
    setStartDate: (SoleimaniDate?) -> Unit,
    setEndDate: (SoleimaniDate?) -> Unit,
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
                val isWeekend = weekConfiguration.isWeekend(day)
                Text(
                    text = weekConfiguration.dayLabelFormatter.format(day),
                    color = if (isWeekend) weekendLabelColor else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides weekConfiguration.layoutDirection) {
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
                    val candidate = cell.date
                    val isEnabled = candidate?.let(isDateEnabled) ?: false
                    val isWithinSelection =
                        candidate != null && startDate != null && endDate != null &&
                                candidate.isWithin(startDate, endDate)
                    val isStart = candidate != null && startDate != null && candidate == startDate
                    val isEnd = candidate != null && endDate != null && candidate == endDate
                    val isPendingSelection =
                        endDate == null && selectedDayValue != null &&
                                candidate?.day == selectedDayValue && candidate?.month == month
                    val isWeekend =
                        candidate != null && weekConfiguration.isWeekendIndex(cell.weekdayIndex)
                    val event = candidate?.let(eventIndicator)

                    val backgroundColor = when {
                        isStart || isEnd || isPendingSelection -> MaterialTheme.colorScheme.primary
                        isWithinSelection -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        isWeekend -> weekendLabelColor.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val contentColor = when {
                        isStart || isEnd || isPendingSelection -> MaterialTheme.colorScheme.onPrimary
                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        isWeekend -> weekendLabelColor
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    val emphasizeSelection = isStart || isEnd || isPendingSelection
                    val shadowColor = if (emphasizeSelection) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f, true)
                            .padding(4.dp)
                            .shadow(
                                color = if (isEnabled) shadowColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                borderRadius = 10.dp,
                                offsetX = 0.dp,
                                offsetY = 3.dp,
                                spread = 3.dp,
                                blurRadius = 10.dp,
                            )
                            .border(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    )
                                ),
                                width = JlResDimens.dp1,
                                shape = RoundedCornerShape(JlResDimens.dp10),
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                enabled = isEnabled && candidate != null,
                                onClick = {
                                    val resolved = candidate ?: return@clickable
                                    changeSelectedPart("main")
                                    handleRangeSelection(
                                        candidate = resolved,
                                        currentStart = startDate,
                                        currentEnd = endDate,
                                        onStartChange = setStartDate,
                                        onEndChange = setEndDate,
                                        onDaySelected = onDaySelected,
                                    )
                                }
                            ),
                        color = backgroundColor,
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
                                color = contentColor,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Cursive,
                                modifier = Modifier.align(Alignment.Center),
                            )

                            if (event != null && candidate != null && isEnabled) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            ),
                                            CircleShape,
                                        )
                                        .background(event.color),
                                )
                            }

                            if (isStart || isEnd) {
                                Box(
                                    modifier = Modifier
                                        .align(if (isStart) Alignment.CenterStart else Alignment.CenterEnd)
                                        .fillMaxHeight()
                                        .width(6.dp)
                                        .background(highlightColor.copy(alpha = 0.28f))
                                        .zIndex(-1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleRangeSelection(
    candidate: SoleimaniDate,
    currentStart: SoleimaniDate?,
    currentEnd: SoleimaniDate?,
    onStartChange: (SoleimaniDate?) -> Unit,
    onEndChange: (SoleimaniDate?) -> Unit,
    onDaySelected: (Int?) -> Unit,
) {
    when {
        currentStart == null || currentEnd != null -> {
            onStartChange(candidate)
            onEndChange(null)
            onDaySelected(candidate.day)
        }
        currentEnd == null && candidate < currentStart -> {
            onStartChange(candidate)
            onDaySelected(candidate.day)
        }
        currentEnd == null -> {
            onEndChange(candidate)
            onDaySelected(candidate.day)
        }
        candidate <= currentStart -> {
            onStartChange(candidate)
            onEndChange(null)
            onDaySelected(candidate.day)
        }
        else -> {
            onEndChange(candidate)
            onDaySelected(candidate.day)
        }
    }
}

private fun SoleimaniDate.isWithin(start: SoleimaniDate, end: SoleimaniDate): Boolean {
    val (first, second) = if (start <= end) start to end else end to start
    return this >= first && this <= second
}

@Preview
@Composable
private fun DayOfWeekRangeViewPreview() {
    val colors = DatePickerDefaults.lightColors()
    DayOfWeekRangeView(
        month = 5,
        selectedDay = null,
        year = 1403,
        startDate = null,
        endDate = null,
        weekConfiguration = WeekConfiguration(),
        digitMode = DigitMode.Persian,
        weekendLabelColor = colors.weekendLabelColor,
        highlightColor = colors.todayOutline,
        eventIndicator = { date ->
            if (date.day == 1) CalendarEvent(Color(0xFF10B981), "شروع ماه") else null
        },
        onDaySelected = {},
        setStartDate = {},
        setEndDate = {},
    )
}
