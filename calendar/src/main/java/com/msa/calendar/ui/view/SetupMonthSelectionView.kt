package com.msa.calendar.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.gregorianMonthSpanLabel
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersianCalendar

@Preview
@Composable
fun MonthViewPreview() {
    MonthView(
        selectedMonth = 3,
        displayedYear = 1402,
        digitMode = DigitMode.Persian,
        monthFormatter = MonthFormatter.Persian,
        colors = DatePickerDefaults.lightColors(),
        title = "Select month",
        onMonthSelected = {},
    )
}

/** Static 12-item month grid. Avoids lazy-layout overhead and keeps row geometry deterministic. */
@Composable
fun MonthView(
    selectedMonth: Int,
    displayedYear: Int,
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    colors: DatePickerColors,
    title: String,
    onMonthSelected: (Int) -> Unit,
    showGregorianHints: Boolean = true,
) {
    val monthNames = remember(monthFormatter, digitMode) { monthFormatter.labels(digitMode) }
    val gregorianMonthHints = remember(displayedYear, digitMode, showGregorianHints) {
        if (!showGregorianHints) {
            emptyList()
        } else {
            (1..12).map { month ->
                gregorianMonthSpanLabel(
                    persianYear = displayedYear,
                    persianMonth = month,
                    digitMode = digitMode,
                    includeYear = false,
                )
            }
        }
    }
    val todayCalendar = remember { PersianCalendar() }
    val currentMonthIndex = remember { todayCalendar.getMonth() - 1 }
    val currentYear = remember { todayCalendar.getYear() }
    val isCurrentYear = displayedYear == currentYear
    val displayedYearLabel = remember(displayedYear, digitMode) {
        when (digitMode) {
            DigitMode.Persian -> FormatHelper.toPersianNumber(displayedYear.toString())
            DigitMode.Latin -> displayedYear.toString()
        }
    }
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 500.dp -> 4
            maxWidth >= 380.dp -> 3
            else -> 2
        }
        val rows = (monthNames.size + columns - 1) / columns

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.dayTextColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.currentChoiceColor,
                    contentColor = colors.todayOutline,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = displayedYearLabel,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            repeat(rows) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(columns) { columnIndex ->
                        val index = rowIndex * columns + columnIndex
                        if (index >= monthNames.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val monthNumber = index + 1
                            val selected = selectedMonth == monthNumber
                            val gregorianHint = gregorianMonthHints.getOrNull(index)
                            val isCurrentMonth = isCurrentYear && index == currentMonthIndex
                            val contentColor = when {
                                selected -> colors.selectionContentColor
                                isCurrentMonth -> colors.todayOutline
                                else -> colors.dayTextColor
                            }

                            PickerChoiceCard(
                                modifier = Modifier.weight(1f),
                                selected = selected,
                                highlighted = isCurrentMonth,
                                colors = colors,
                                contentDescription = listOfNotNull(
                                    monthNames[index],
                                    gregorianHint,
                                ).joinToString(", "),
                                onClick = { onMonthSelected(monthNumber) },
                                contentPadding = PaddingValues(
                                    horizontal = 10.dp,
                                    vertical = if (showGregorianHints) 10.dp else 13.dp,
                                ),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = monthNames[index],
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = contentColor,
                                    )
                                    gregorianHint?.let { hint ->
                                        Text(
                                            text = hint,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = contentColor.copy(alpha = 0.76f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
