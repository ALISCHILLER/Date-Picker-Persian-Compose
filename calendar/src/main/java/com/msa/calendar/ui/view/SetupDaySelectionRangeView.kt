package com.msa.calendar.ui.view

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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.theme.CalendarColorTokens
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
    highlightFill: Color,
    highlightedDate: SoleimaniDate?,
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
    val brandViolet = CalendarColorTokens.Violet
    val brandTeal = CalendarColorTokens.Teal

    Column {
        // Header: week day labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            val baseStyle = MaterialTheme.typography.labelLarge
            // امن در برابر isUnspecified: به Float تبدیل سپس sp
            val baseSizePx = if (baseStyle.fontSize.isUnspecified) 14f else baseStyle.fontSize.value

            orderedWeekDays.forEach { day ->
                val isWeekend = weekConfiguration.isWeekend(day)
                val labelSize = if (isWeekend) (baseSizePx + 1f).sp else baseSizePx.sp

                Text(
                    text = weekConfiguration.dayLabelFormatter.format(day).uppercase(),
                    color = if (isWeekend) weekendLabelColor else MaterialTheme.colorScheme.onSurface,
                    style = baseStyle.copy(
                        fontSize = labelSize,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp,
                    ),
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
                itemsIndexed(
                    items = monthCells,
                    key = { index, cell ->
                        cell.date?.let { date ->
                            "date-${date.year}-${date.month}-${date.day}"
                        } ?: "empty-$index"
                    }
                ) { _, cell ->
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

                    val isToday = highlightedDate != null && candidate == highlightedDate
                    val event = candidate?.let(eventIndicator)

                    val selectionBrush = remember(brandViolet, brandTeal) {
                        Brush.linearGradient(
                            listOf(
                                brandViolet.copy(alpha = 0.96f),
                                brandTeal.copy(alpha = 0.86f)
                            )
                        )
                    }
                    val rangeHighlight = brandTeal.copy(alpha = 0.18f)
                    val weekendBackground = weekendLabelColor.copy(alpha = 0.12f)
                    val restingTile = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    val disabledTile = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

                    val backgroundColor = when {
                        !isEnabled -> disabledTile
                        isWithinSelection -> rangeHighlight
                        isToday && isEnabled -> highlightFill
                        isWeekend -> weekendBackground
                        else -> restingTile
                    }

                    val contentColor = when {
                        isStart || isEnd || isPendingSelection -> MaterialTheme.colorScheme.onPrimary
                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        isToday && isEnabled -> highlightColor
                        isWeekend -> weekendLabelColor
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    val emphasizeSelection = isStart || isEnd || isPendingSelection
                    val shadowColor = when {
                        emphasizeSelection -> MaterialTheme.colorScheme.primary
                        isToday && isEnabled -> highlightColor
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val baseShape = RoundedCornerShape(14.dp)
                    val selectionBorder = remember(brandViolet, brandTeal) {
                        Brush.linearGradient(
                            listOf(
                                brandViolet.copy(alpha = 0.84f),
                                brandTeal.copy(alpha = 0.72f)
                            )
                        )
                    }
                    val restingBorder = SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    val todayBorder = SolidColor(highlightColor)

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f, true)
                            .padding(4.dp)
                            .shadow( // استفاده از shadow استاندارد Compose
                                elevation = 10.dp,
                                shape = RoundedCornerShape(JlResDimens.dp10),
                                clip = false
                            )
                            .border(
                                brush = when {
                                    emphasizeSelection -> selectionBorder
                                    isToday && isEnabled -> todayBorder
                                    else -> restingBorder
                                },
                                width = JlResDimens.dp1,
                                shape = RoundedCornerShape(JlResDimens.dp10),
                            )
                            .clip(baseShape)
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
                        color = Color.Transparent,
                    ) {
                        val backgroundBrush: Brush = when {
                            emphasizeSelection -> selectionBrush
                            isWithinSelection -> SolidColor(rangeHighlight)
                            !isEnabled -> SolidColor(disabledTile)
                            else -> SolidColor(backgroundColor)
                        }

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(backgroundBrush, baseShape)
                                .semantics {
                                    if (event?.label != null) {
                                        contentDescription = event.label
                                    }
                                },
                        ) {
                            val labelStyle = MaterialTheme.typography.bodyLarge

                            Text(
                                text = cell.dayOfMonth?.let { day ->
                                    when (digitMode) {
                                        DigitMode.Persian -> FormatHelper.toPersianNumber(day.toString())
                                        DigitMode.Latin -> day.toString()
                                    }
                                } ?: "",
                                style = labelStyle.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (isWeekend) 19.sp else 18.sp,
                                    letterSpacing = 0.2.sp,
                                ),
                                color = contentColor,
                                modifier = Modifier.align(Alignment.Center),
                            )
                            val ringSurface = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            if (event != null && candidate != null && isEnabled) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                        .size(8.dp)
                                        .drawBehind {
                                            val r = size.minDimension / 2f
                                            drawCircle(color = event.color, radius = r)

                                            val r1 = r - 1.dp.toPx()
                                            if (r1 > 0f) {
                                                drawCircle(
                                                    color = ringSurface,
                                                    radius = r1
                                                )
                                            }

                                            val r2 = r1 - 1.dp.toPx()
                                            if (r2 > 0f) {
                                                drawCircle(
                                                    color = event.color.copy(alpha = 0.7f),
                                                    radius = r2
                                                )
                                            }
                                        }
                                )
                            }

                            if (isStart || isEnd) {
                                Box(
                                    modifier = Modifier
                                        .align(if (isStart) Alignment.CenterStart else Alignment.CenterEnd)
                                        .fillMaxHeight()
                                        .width(6.dp)
                                        .background(highlightFill)
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
        highlightFill = colors.todayButtonBackground,
        highlightedDate = SoleimaniDate(1403, 5, 8),
        eventIndicator = { date ->
            if (date.day == 1) CalendarEvent(Color(0xFF10B981), "شروع ماه") else null
        },
        onDaySelected = {},
        setStartDate = {},
        setEndDate = {},
    )
}
