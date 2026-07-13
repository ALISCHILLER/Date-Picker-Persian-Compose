package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.buildDayCellContentDescription
import com.msa.calendar.buildDayCellStateDescription
import com.msa.calendar.formatDayNumber
import com.msa.calendar.resolveRangeSelection
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.utils.SoleimaniDate

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
    highlightFill: Color,
    highlightedDate: SoleimaniDate?,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
    strings: DatePickerStrings,
    onDaySelected: (Int?) -> Unit,
    setStartDate: (SoleimaniDate?) -> Unit,
    setEndDate: (SoleimaniDate?) -> Unit,
    isDateEnabled: (SoleimaniDate) -> Boolean = { true },
    changeSelectedPart: (String) -> Unit = {},
    compact: Boolean = false,
    selectionStartColor: Color = CalendarColorTokens.Violet,
    selectionEndColor: Color = CalendarColorTokens.Teal,
    rangeFillColor: Color = CalendarColorTokens.Teal.copy(alpha = 0.14f),
    dayTextColor: Color = Color.Unspecified,
    disabledDayTextColor: Color = Color.Unspecified,
    selectionContentColor: Color = Color.White,
    weekendSurfaceColor: Color = weekendLabelColor.copy(alpha = 0.055f),
    weekdayHeaderBackground: Color = Color.Transparent,
    weekdayHeaderOutline: Color = Color.Transparent,
    enableHaptics: Boolean = true,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val selectedDayValue = selectedDay
    val selectedBrush = remember(selectionStartColor, selectionEndColor) {
        Brush.linearGradient(colors = listOf(selectionStartColor, selectionEndColor))
    }
    val rangeTrackColor = rangeFillColor
    val fullTrackShape = remember { RoundedCornerShape(50) }
    val startTrackShape = remember {
        RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
    }
    val endTrackShape = remember {
        RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
    }
    val squareTrackShape = remember { RoundedCornerShape(0.dp) }
    val resolvedDayTextColor = if (dayTextColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        dayTextColor
    }
    val resolvedDisabledTextColor = if (disabledDayTextColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
    } else {
        disabledDayTextColor
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalendarWeekdayHeader(
            weekConfiguration = weekConfiguration,
            weekendLabelColor = weekendLabelColor,
            compact = compact,
            contentPadding = if (compact) 2.dp else 8.dp,
            backgroundColor = weekdayHeaderBackground,
            outlineColor = weekdayHeaderOutline,
        )

        CalendarMonthGrid(
            month = month,
            year = year,
            weekConfiguration = weekConfiguration,
            modifier = Modifier.weight(1f),
            contentPadding = if (compact) 2.dp else 8.dp,
        ) { cell ->
            val candidate = cell.date
            if (candidate == null) {
                EmptyCalendarDayCell(compact = compact)
                return@CalendarMonthGrid
            }

            val isEnabled = isDateEnabled(candidate)
            val isWithinSelection = startDate != null && endDate != null &&
                candidate >= startDate && candidate <= endDate
            val isStart = startDate != null && candidate == startDate
            val isEnd = endDate != null && candidate == endDate
            val isPendingSelection = endDate == null && selectedDayValue != null &&
                candidate.day == selectedDayValue && candidate.month == month
            val isWeekend = weekConfiguration.isWeekendIndex(cell.weekdayIndex)
            val isToday = highlightedDate != null && candidate == highlightedDate
            val event = eventIndicator(candidate)
            val emphasizeSelection = isStart || isEnd || isPendingSelection

            val cellDescription = remember(
                candidate,
                digitMode,
                strings,
                event?.label,
                isPendingSelection,
                isToday,
                isEnabled,
                isStart,
                isEnd,
                isWithinSelection,
            ) {
                buildDayCellContentDescription(
                    date = candidate,
                    digitMode = digitMode,
                    strings = strings,
                    eventLabel = event?.label,
                    isSelected = isPendingSelection,
                    isToday = isToday,
                    isEnabled = isEnabled,
                    isRangeStart = isStart,
                    isRangeEnd = isEnd,
                    isWithinRange = isWithinSelection,
                )
            }
            val cellStateDescription = remember(
                strings,
                isPendingSelection,
                isStart,
                isEnd,
                isWithinSelection,
                isToday,
                isEnabled,
            ) {
                buildDayCellStateDescription(
                    strings = strings,
                    isSelected = isPendingSelection,
                    isRangeStart = isStart,
                    isRangeEnd = isEnd,
                    isWithinRange = isWithinSelection,
                    isToday = isToday,
                    isEnabled = isEnabled,
                )
            }

            val contentColor = when {
                emphasizeSelection -> selectionContentColor
                !isEnabled -> resolvedDisabledTextColor
                isToday -> highlightColor
                isWeekend -> weekendLabelColor
                else -> resolvedDayTextColor
            }
            val border = when {
                emphasizeSelection -> BorderStroke(1.dp, selectionContentColor.copy(alpha = 0.24f))
                isToday && isEnabled -> BorderStroke(1.4.dp, highlightColor.copy(alpha = 0.90f))
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = if (compact) 3.dp else 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isWithinSelection && isEnabled) {
                    val trackShape = when {
                        isStart && isEnd -> fullTrackShape
                        isStart -> startTrackShape
                        isEnd -> endTrackShape
                        cell.weekdayIndex == 0 -> startTrackShape
                        cell.weekdayIndex == 6 -> endTrackShape
                        else -> squareTrackShape
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (compact) 32.dp else 38.dp)
                            .background(rangeTrackColor, trackShape),
                    )
                } else if (isWeekend && isEnabled) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = if (compact) 3.dp else 4.dp)
                            .fillMaxSize()
                            .background(weekendSurfaceColor, CircleShape),
                    )
                }

                Surface(
                    onClick = {
                        if (enableHaptics) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        changeSelectedPart("main")
                        resolveRangeSelection(
                            candidate = candidate,
                            currentStart = startDate,
                            currentEnd = endDate,
                        ).also { update ->
                            setStartDate(update.startDate)
                            setEndDate(update.endDate)
                            onDaySelected(update.selectedDay)
                        }
                    },
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (compact) 3.dp else 4.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = cellDescription
                            stateDescription = cellStateDescription
                            if (emphasizeSelection || isWithinSelection) selected = true
                            if (!isEnabled) disabled()
                        },
                    shape = CircleShape,
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = border,
                ) {
                    val backgroundModifier = when {
                        emphasizeSelection -> Modifier.background(selectedBrush, CircleShape)
                        isToday && isEnabled -> Modifier.background(highlightFill.copy(alpha = 0.68f), CircleShape)
                        else -> Modifier
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(backgroundModifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = formatDayNumber(candidate.day, digitMode),
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (compact) 14.5.sp else 16.sp,
                                fontWeight = if (emphasizeSelection || isToday) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                },
                                letterSpacing = 0.sp,
                            ),
                        )

                        if (event != null && isEnabled) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = if (compact) 3.dp else 5.dp)
                                    .size(if (compact) 4.dp else 5.dp)
                                    .background(
                                        color = if (emphasizeSelection) selectionContentColor else event.color,
                                        shape = CircleShape,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}
