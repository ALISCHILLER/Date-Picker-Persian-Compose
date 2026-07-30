package com.msa.calendar.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.YearFormatter
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersianCalendar
import com.msa.calendar.utils.SoleimaniDate
import kotlinx.coroutines.launch

@Composable
internal fun YearsView(
    selectedYear: Int,
    digitMode: DigitMode,
    yearFormatter: YearFormatter,
    yearRange: IntRange,
    colors: DatePickerColors,
    title: String,
    previousPageDescription: String,
    nextPageDescription: String,
    onYearClick: (Int) -> Unit,
) {
    val currentYear = remember { PersianCalendar().getYear() }
    val years = remember(digitMode, yearFormatter, yearRange) {
        yearRange.map { value ->
            val formattedLabel = yearFormatter.format(value, digitMode)
            val (primaryLabel, embeddedSecondary) = extractYearLabels(formattedLabel)

            val computedGregorian = runCatching {
                SoleimaniDate(value, 1, 1).toGregorian().year
            }.getOrElse { value + 621 }
            val secondaryLabel = embeddedSecondary ?: when (digitMode) {
                DigitMode.Persian -> FormatHelper.toPersianNumber(computedGregorian.toString())
                DigitMode.Latin -> computedGregorian.toString()
            }

            YearDisplay(
                value = value,
                primary = primaryLabel.ifEmpty { formattedLabel },
                secondary = secondaryLabel,
            )
        }
    }

    val selectedYearValue = selectedYear
    val yearListState = rememberLazyGridState()

    val coroutineScope = rememberCoroutineScope()
    val pageJump = 12

    val canScrollBackward by remember(yearListState, years) {
        derivedStateOf { years.isNotEmpty() && yearListState.firstVisibleItemIndex > 0 }
    }
    val canScrollForward by remember(yearListState, years) {
        derivedStateOf {
            if (years.isEmpty()) {
                false
            } else {
                val lastVisible = yearListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible < years.lastIndex
            }
        }
    }

    LaunchedEffect(selectedYearValue, years) {
        val idx = years.indexOfFirst { it.value == selectedYearValue }
        if (idx >= 0) {
            val visibleIndices = yearListState.layoutInfo.visibleItemsInfo.map { it.index }
            val isVisible = visibleIndices.any { it == idx }
            if (!isVisible || visibleIndices.isEmpty()) {
                val targetIndex = (idx - 3).coerceAtLeast(0)
                // Initial positioning should be immediate; animated startup scrolling delays first use.
                yearListState.scrollToItem(targetIndex)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YearPageButton(
                enabled = canScrollBackward,
                contentDescription = previousPageDescription,
                colors = colors,
                upward = true,
                onClick = {
                    coroutineScope.launch {
                        val target = (yearListState.firstVisibleItemIndex - pageJump).coerceAtLeast(0)
                        yearListState.animateScrollToItem(target)
                    }
                },
            )

            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = colors.dayTextColor,
            )

            YearPageButton(
                enabled = canScrollForward,
                contentDescription = nextPageDescription,
                colors = colors,
                upward = false,
                onClick = {
                    coroutineScope.launch {
                        if (years.isNotEmpty()) {
                            val target = (yearListState.firstVisibleItemIndex + pageJump)
                                .coerceAtMost(years.lastIndex)
                            yearListState.animateScrollToItem(target)
                        }
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Adaptive(minSize = 96.dp),
            state = yearListState,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            items(years, key = { it.value }) { yearDisplay ->
                val yearValue = yearDisplay.value
                val isSelected = selectedYearValue == yearValue
                val isCurrentYear = yearValue == currentYear
                val contentColor = when {
                    isSelected -> colors.selectionContentColor
                    isCurrentYear -> colors.todayOutline
                    else -> colors.dayTextColor
                }

                PickerChoiceCard(
                    selected = isSelected,
                    highlighted = isCurrentYear,
                    colors = colors,
                    contentDescription = yearDisplay.primary,
                    onClick = { onYearClick(yearValue) },
                ) {
                    YearLabelContent(
                        primary = yearDisplay.primary,
                        secondary = yearDisplay.secondary,
                        color = contentColor,
                        selected = isSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun YearPageButton(
    enabled: Boolean,
    contentDescription: String,
    colors: DatePickerColors,
    upward: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.semantics {
            role = Role.Button
            this.contentDescription = contentDescription
            if (!enabled) disabled()
        },
        shape = shape,
        color = if (enabled) colors.currentChoiceColor else colors.surfaceVariantColor.copy(alpha = 0.42f),
        contentColor = colors.dayTextColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineColor),
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .padding(horizontal = 9.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (upward) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = if (enabled) {
                    colors.dayTextColor
                } else {
                    colors.disabledDayTextColor
                },
            )
        }
    }
}

private data class YearDisplay(
    val value: Int,
    val primary: String,
    val secondary: String,
)

private fun extractYearLabels(label: String): Pair<String, String?> {
    val start = label.indexOf('(')
    val end = label.indexOf(')', startIndex = start + 1)
    return if (start in 1 until end) {
        val primary = label.substring(0, start).trim()
        val secondary = label.substring(start + 1, end).trim()
        primary to secondary.ifEmpty { null }
    } else {
        label to null
    }
}

@Composable
private fun YearLabelContent(
    primary: String,
    secondary: String,
    color: Color,
    selected: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center,
        )
        Text(
            text = secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = if (selected) 0.86f else 0.68f),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun YearsViewPreview() {
    YearsView(
        selectedYear = 1402,
        digitMode = DigitMode.Persian,
        yearFormatter = YearFormatter.WithGregorianHint,
        yearRange = 1350..1450,
        colors = DatePickerDefaults.lightColors(),
        title = "Select year",
        previousPageDescription = "Previous years",
        nextPageDescription = "Next years",
        onYearClick = {},
    )
}
