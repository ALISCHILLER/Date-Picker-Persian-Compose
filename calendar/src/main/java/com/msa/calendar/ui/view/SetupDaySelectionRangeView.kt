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
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.JlResDimens
import com.msa.calendar.utils.getWeekDays
import com.msa.calendar.utils.toPersianNumber
@Composable
fun DayOfWeekRangeView(
    mMonth: String,
    mMonthint: String,
    mDay: String,
    mYear: String,
    startDate: SoleimaniDate?,
    endDate: SoleimaniDate?,
    setDay: (String) -> Unit,
    setStartDate: (SoleimaniDate?) -> Unit,
    setEndDate: (SoleimaniDate?) -> Unit,
    isDateEnabled: (SoleimaniDate) -> Boolean = { true },
    changeSelectedPart: (String) -> Unit = {}
) {
    val daysList = getWeekDays(mMonth, mYear)

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
                    val candidate = SoleimaniDate.fromLocalizedStrings(mYear, mMonthint, day)
                    val isEnabled = candidate?.let(isDateEnabled) ?: false
                    val isRangeHighlighted = candidate != null && isEnabled && startDate != null && endDate != null && candidate.isWithin(startDate, endDate)
                    val isPendingStart = candidate != null && isEnabled && startDate != null && endDate == null && candidate == startDate
                    val isPendingSelection = day == mDay && endDate == null && isEnabled
                    val isHighlighted = isRangeHighlighted || isPendingStart || isPendingSelection
                    val backgroundColor = when {
                        isHighlighted -> MaterialTheme.colorScheme.primary
                        !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        else -> Color.White
                    }
                    val contentColor = when {
                        isHighlighted -> MaterialTheme.colorScheme.onPrimary
                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else -> Color.Black
                    }
                    val emphasizeSelection = candidate != null && isEnabled && (
                            (startDate != null && candidate == startDate) ||
                                    (endDate != null && candidate == endDate) ||
                                    (endDate == null && day == mDay)
                            )
                    val shadowColor = if (emphasizeSelection) Purple40 else PurpleGrey80
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f, true)
                            .padding(4.dp)
                            .shadow(
                                color = if (isEnabled) shadowColor else PurpleGrey80.copy(alpha = 0.3f),
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
                            .clickable(
                                enabled = isEnabled && candidate != null,
                                onClick = {
                                    val selectedDate = candidate ?: return@clickable
                                    changeSelectedPart("main")
                                    handleRangeSelection(
                                        candidate = selectedDate,
                                        currentStart = startDate,
                                        currentEnd = endDate,
                                        onStartChange = setStartDate,
                                        onEndChange = setEndDate,
                                        onDaySelected = setDay,
                                    )
                                }
                            ),

                        color = backgroundColor,
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodyLarge,
                                color = contentColor,
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

private fun handleRangeSelection(
    candidate: SoleimaniDate,
    currentStart: SoleimaniDate?,
    currentEnd: SoleimaniDate?,
    onStartChange: (SoleimaniDate?) -> Unit,
    onEndChange: (SoleimaniDate?) -> Unit,
    onDaySelected: (String) -> Unit,
) {
    when {
        currentStart == null || (currentStart != null && currentEnd != null) -> {
            onStartChange(candidate)
            onEndChange(null)
            onDaySelected(candidate.day.toPersianNumber())
        }

        currentEnd == null && candidate < currentStart -> {
            onStartChange(candidate)
            onDaySelected(candidate.day.toPersianNumber())
        }
        currentEnd == null -> {
            onEndChange(candidate)
        }

        candidate <= currentStart -> {
            onStartChange(candidate)
            onEndChange(null)
            onDaySelected(candidate.day.toPersianNumber())
        }


        else -> {
            onEndChange(candidate)
        }
    }
}

fun isDateInRange(targetDate: PersionCalendar, startDate: List<Int>, endDate: List<Int>): Boolean {
    return targetDate.isInRange(startDate, endDate)
}

private fun SoleimaniDate.isWithin(start: SoleimaniDate, end: SoleimaniDate): Boolean = this >= start && this <= end


@Preview
@Composable
fun DayOfWeekRangeViewPreview() {
    DayOfWeekRangeView(
        mMonth = "5",
        mMonthint = "5",
        mDay = "10",
        mYear = "2024",
        startDate = null,
        endDate = null,
        setDay = {},
        setStartDate = { _ -> },
        setEndDate = { _ -> },
    ) {}
}