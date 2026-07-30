package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.utils.MonthDayCell
import com.msa.calendar.utils.buildMonthCells
import java.util.Locale

private const val CalendarColumns = 7
private const val CalendarRows = 6
private val MaximumDayCellSize = 58.dp

@Composable
internal fun CalendarWeekdayHeader(
    weekConfiguration: WeekConfiguration,
    weekendLabelColor: Color,
    compact: Boolean = false,
    contentPadding: Dp = 0.dp,
    backgroundColor: Color = Color.Transparent,
    outlineColor: Color = Color.Transparent,
) {
    val orderedWeekDays = remember(weekConfiguration) { weekConfiguration.orderedDays }
    val baseStyle = MaterialTheme.typography.labelMedium
    val resolvedBaseSize = if (baseStyle.fontSize != TextUnit.Unspecified) baseStyle.fontSize else 12.sp
    val shape = RoundedCornerShape(if (compact) 13.dp else 15.dp)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gridWidth = resolveCalendarGridWidth(maxWidth, contentPadding)
        Surface(
            modifier = Modifier
                .width(gridWidth)
                .align(Alignment.Center),
            shape = shape,
            color = backgroundColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = if (outlineColor != Color.Transparent) BorderStroke(1.dp, outlineColor) else null,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (compact) 3.dp else 5.dp,
                        vertical = if (compact) 6.dp else 8.dp,
                    ),
                horizontalArrangement = Arrangement.Center,
            ) {
                orderedWeekDays.forEach { day ->
                    val isWeekend = weekConfiguration.isWeekend(day)
                    Text(
                        modifier = Modifier.weight(1f),
                        text = weekConfiguration.dayLabelFormatter.format(day).uppercase(Locale.ROOT),
                        color = if (isWeekend) weekendLabelColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = baseStyle.copy(
                            fontSize = resolvedBaseSize,
                            fontWeight = if (isWeekend) FontWeight.Bold else FontWeight.SemiBold,
                            letterSpacing = 0.2.sp,
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Fixed 7x6 calendar grid with a capped cell size.
 *
 * Phones still use every available pixel, while tablets stop growing after 58dp per day. That keeps
 * scan distance comfortable, avoids oversized touch targets, and reduces the amount of drawing on
 * large windows. Scrolling is created only when the available height cannot contain the grid.
 */
@Composable
internal fun CalendarMonthGrid(
    month: Int,
    year: Int,
    weekConfiguration: WeekConfiguration,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 0.dp,
    dayContent: @Composable (MonthDayCell) -> Unit,
) {
    val monthCells = remember(month, year, weekConfiguration.startDay) {
        buildMonthCells(month, year, weekConfiguration.startDay)
    }
    val scrollState = rememberScrollState()

    CompositionLocalProvider(LocalLayoutDirection provides weekConfiguration.layoutDirection) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .testTag(com.msa.calendar.DatePickerTestTags.MonthGrid),
        ) {
            val gridWidth = resolveCalendarGridWidth(maxWidth, contentPadding)
            val cellSize = gridWidth / CalendarColumns.toFloat()
            val gridHeight = cellSize * CalendarRows.toFloat()
            val needsVerticalScroll = gridHeight > maxHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (needsVerticalScroll) Modifier.verticalScroll(scrollState) else Modifier),
                contentAlignment = if (needsVerticalScroll) Alignment.TopCenter else Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .width(gridWidth)
                        .height(gridHeight),
                ) {
                    repeat(CalendarRows) { rowIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(cellSize),
                        ) {
                            repeat(CalendarColumns) { columnIndex ->
                                val index = rowIndex * CalendarColumns + columnIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                ) {
                                    dayContent(monthCells[index])
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resolveCalendarGridWidth(maxWidth: Dp, contentPadding: Dp): Dp {
    val usableWidth = (maxWidth - contentPadding * 2).coerceAtLeast(1.dp)
    val naturalCellSize = usableWidth / CalendarColumns.toFloat()
    return naturalCellSize.coerceAtMost(MaximumDayCellSize) * CalendarColumns.toFloat()
}

@Composable
internal fun EmptyCalendarDayCell(compact: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (compact) 2.dp else 4.dp),
    )
}
