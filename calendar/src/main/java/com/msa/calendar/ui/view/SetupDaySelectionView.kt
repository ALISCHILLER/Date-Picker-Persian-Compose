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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.components.shadow
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
    onDaySelected: (Int) -> Unit,
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
        // هدر روزهای هفته
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            val baseStyle = MaterialTheme.typography.labelLarge
            val resolvedBaseSize = if (baseStyle.fontSize != TextUnit.Unspecified) {
                baseStyle.fontSize
            } else {
                14.sp
            }
            val weekendSize = (resolvedBaseSize.value + 1f).sp

            orderedWeekDays.forEach { day ->
                val isWeekend = weekConfiguration.isWeekend(day)
                Text(
                    text = weekConfiguration.dayLabelFormatter.format(day).uppercase(),
                    color = if (isWeekend) weekendLabelColor else MaterialTheme.colorScheme.onSurface,
                    style = baseStyle.copy(
                        fontSize = if (isWeekend) weekendSize else resolvedBaseSize,
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
                    val candidateDate = cell.date
                    val isEnabled = candidateDate?.let(isDateEnabled) ?: false

                    val isSelected = isEnabled &&
                            candidateDate != null &&
                            selectedDayValue != null &&
                            candidateDate.day == selectedDayValue &&
                            candidateDate.month == month

                    val isToday = highlightedDate != null && candidateDate == highlightedDate
                    val isWeekend = candidateDate != null && weekConfiguration.isWeekendIndex(cell.weekdayIndex)
                    val event = candidateDate?.let(eventIndicator)

                    val shadowColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday && isEnabled -> highlightColor
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val baseShape = RoundedCornerShape(14.dp)

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
                            .let { m ->
                                if (isToday && !isSelected && isEnabled) {
                                    m.border(
                                        BorderStroke(
                                            width = JlResDimens.dp1,
                                            brush = SolidColor(highlightColor),
                                        ),
                                        shape = RoundedCornerShape(JlResDimens.dp10),
                                    )
                                } else m
                            }
                            .clip(baseShape)
                            .clickable(
                                enabled = isEnabled && candidateDate != null,
                                onClick = {
                                    val selected = candidateDate ?: return@clickable
                                    changeSelectedPart("main")
                                    onDaySelected(selected.day)
                                },
                            ),
                        color = Color.Transparent,
                    ) {
                        val selectedBrush = remember(brandViolet, brandTeal) {
                            Brush.linearGradient(
                                listOf(
                                    brandViolet.copy(alpha = 0.96f),
                                    brandTeal.copy(alpha = 0.86f)
                                )
                            )
                        }
                        val weekendBackground = weekendLabelColor.copy(alpha = 0.12f)
                        val todayBackground = highlightFill
                        val defaultBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        val tileColor = when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            isSelected -> Color.Transparent
                            isToday && isEnabled -> todayBackground
                            isWeekend -> weekendBackground
                            else -> defaultBackground
                        }
                        val backgroundBrush: Brush = if (isSelected) selectedBrush else SolidColor(tileColor)

                        // رنگی که به CompositionLocal نیاز دارد را بیرون از drawBehind بگیرید
                        val ringSurface = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    brush = backgroundBrush,
                                    shape = baseShape
                                )
                                .semantics {
                                    if (event?.label != null) contentDescription = event.label
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
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    isToday && isEnabled -> highlightColor
                                    isWeekend -> weekendLabelColor
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.align(Alignment.Center),
                            )

                            if (event != null && candidateDate != null && isEnabled) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                        .size(8.dp)
                                        .drawBehind {
                                            val radius = size.minDimension / 2f
                                            drawCircle(color = event.color, radius = radius)

                                            val firstRing = radius - 1.dp.toPx()
                                            if (firstRing > 0f) {
                                                drawCircle(
                                                    color = ringSurface,
                                                    radius = firstRing
                                                )
                                            }

                                            val coreRadius = firstRing - 1.dp.toPx()
                                            if (coreRadius > 0f) {
                                                drawCircle(
                                                    color = event.color.copy(alpha = 0.7f),
                                                    radius = coreRadius
                                                )
                                            }
                                        }
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
        highlightFill = colors.todayButtonBackground,
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
