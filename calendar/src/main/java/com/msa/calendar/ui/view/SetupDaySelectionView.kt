package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.buildDayCellContentDescription
import com.msa.calendar.buildDayCellStateDescription
import com.msa.calendar.formatDayNumber
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.utils.SoleimaniDate

@Composable
fun DayOfWeekView(
    month: Int,
    selectedDay: Int?,
    year: Int,
    highlightedDate: SoleimaniDate?,
    highlightColor: Color,
    highlightFill: Color,
    weekConfiguration: WeekConfiguration,
    digitMode: DigitMode,
    weekendLabelColor: Color,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
    strings: DatePickerStrings,
    onDaySelected: (Int) -> Unit,
    isDateEnabled: (SoleimaniDate) -> Boolean = { true },
    changeSelectedPart: (String) -> Unit = {},
    compact: Boolean = false,
    selectionStartColor: Color = CalendarColorTokens.Violet,
    selectionEndColor: Color = CalendarColorTokens.Teal,
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
            val candidateDate = cell.date
            if (candidateDate == null) {
                EmptyCalendarDayCell(compact = compact)
                return@CalendarMonthGrid
            }

            val isEnabled = isDateEnabled(candidateDate)
            val isSelected = isEnabled &&
                selectedDayValue != null &&
                candidateDate.day == selectedDayValue &&
                candidateDate.month == month
            val isToday = highlightedDate != null && candidateDate == highlightedDate
            val isWeekend = weekConfiguration.isWeekendIndex(cell.weekdayIndex)
            val event = eventIndicator(candidateDate)
            val cellDescription = remember(
                candidateDate,
                digitMode,
                strings,
                event?.label,
                isSelected,
                isToday,
                isEnabled,
            ) {
                buildDayCellContentDescription(
                    date = candidateDate,
                    digitMode = digitMode,
                    strings = strings,
                    eventLabel = event?.label,
                    isSelected = isSelected,
                    isToday = isToday,
                    isEnabled = isEnabled,
                )
            }
            val cellStateDescription = remember(strings, isSelected, isToday, isEnabled) {
                buildDayCellStateDescription(
                    strings = strings,
                    isSelected = isSelected,
                    isToday = isToday,
                    isEnabled = isEnabled,
                )
            }

            val contentColor = when {
                isSelected -> selectionContentColor
                !isEnabled -> resolvedDisabledTextColor
                isToday -> highlightColor
                isWeekend -> weekendLabelColor
                else -> resolvedDayTextColor
            }
            val tileColor = when {
                !isEnabled -> Color.Transparent
                isToday && !isSelected -> highlightFill.copy(alpha = 0.68f)
                isWeekend -> weekendSurfaceColor
                else -> Color.Transparent
            }
            val border = when {
                isSelected -> BorderStroke(1.dp, selectionContentColor.copy(alpha = 0.24f))
                isToday && isEnabled -> BorderStroke(1.4.dp, highlightColor.copy(alpha = 0.90f))
                else -> null
            }

            Surface(
                onClick = {
                    if (enableHaptics) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    changeSelectedPart("main")
                    onDaySelected(candidateDate.day)
                },
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (compact) 3.dp else 4.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = cellDescription
                        stateDescription = cellStateDescription
                        if (isSelected) selected = true
                        if (!isEnabled) disabled()
                    },
                shape = CircleShape,
                color = Color.Transparent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = border,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isSelected) {
                                Modifier.background(selectedBrush, CircleShape)
                            } else {
                                Modifier.background(tileColor, CircleShape)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = formatDayNumber(candidateDate.day, digitMode),
                        color = contentColor,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = if (compact) 14.5.sp else 16.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
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
                                    color = if (isSelected) selectionContentColor else event.color,
                                    shape = CircleShape,
                                ),
                        )
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
        highlightFill = colors.todayButtonBackground,
        weekConfiguration = WeekConfiguration(),
        digitMode = DigitMode.Persian,
        weekendLabelColor = colors.weekendLabelColor,
        eventIndicator = { date ->
            if (date.day == 1) CalendarEvent(Color(0xFF10B981), "Month start") else null
        },
        strings = DatePickerStrings.localized(),
        onDaySelected = {},
        isDateEnabled = { true },
        selectionContentColor = colors.selectionContentColor,
        weekendSurfaceColor = colors.weekendSurfaceColor,
        weekdayHeaderBackground = colors.surfaceVariantColor,
        weekdayHeaderOutline = colors.outlineColor,
    )
}
