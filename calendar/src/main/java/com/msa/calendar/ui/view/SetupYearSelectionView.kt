package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.YearFormatter
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.SoleimaniDate
import kotlinx.coroutines.launch

@Composable
fun YearsView(
    selectedYear: Int,
    digitMode: DigitMode,
    yearFormatter: YearFormatter,
    yearRange: IntRange,
    colors: DatePickerColors,
    onYearClick: (Int) -> Unit,
) {
    val currentYear = remember { PersionCalendar().getYear() }
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
                if (yearListState.layoutInfo.totalItemsCount == 0) {
                    yearListState.scrollToItem(targetIndex)
                } else {
                    yearListState.animateScrollToItem(targetIndex)
                }
            }
        }
    }

    val buttonShape = RoundedCornerShape(12.dp)
    val buttonPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val target = (yearListState.firstVisibleItemIndex - pageJump).coerceAtLeast(0)
                        yearListState.animateScrollToItem(target)
                    }
                },
                enabled = canScrollBackward,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Previous years",
                )
            }

            Text(
                text = "انتخاب سال",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        if (years.isNotEmpty()) {
                            val target = (yearListState.firstVisibleItemIndex + pageJump)
                                .coerceAtMost(years.lastIndex)
                            yearListState.animateScrollToItem(target)
                        }
                    }
                },
                enabled = canScrollForward,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Next years",
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = yearListState,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        ) {
            items(years, key = { it.value }) { yearDisplay ->
                val yearValue = yearDisplay.value
                val isSelected = selectedYearValue == yearValue
                val isCurrentYear = yearValue == currentYear

                val content: @Composable () -> Unit = {
                    YearLabelContent(
                        primary = yearDisplay.primary,
                        secondary = yearDisplay.secondary,
                    )

                }

                when {
                    isSelected -> {
                        Button(
                            onClick = { onYearClick(yearValue) },
                            shape = buttonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.confirmButtonBackground,
                                contentColor = colors.confirmButtonContent,
                                disabledContainerColor = colors.confirmButtonBackground.copy(alpha = 0.3f),
                                disabledContentColor = colors.confirmButtonContent.copy(alpha = 0.4f),
                            ),
                            contentPadding = buttonPadding,
                        ) { content() }
                    }

                    isCurrentYear -> {
                        OutlinedButton(
                            onClick = { onYearClick(yearValue) },
                            shape = buttonShape,
                            border = BorderStroke(1.25.dp, colors.todayOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = colors.todayOutline,
                            ),
                            contentPadding = buttonPadding,
                        ) { content() }
                    }

                    else -> {
                        TextButton(
                            onClick = { onYearClick(yearValue) },
                            shape = buttonShape,
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            contentPadding = buttonPadding,
                        ) { content() }
                    }
                }
            }
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
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = primary,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = secondary,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}


@Preview
@Composable
fun YearsViewPreview() {
    MaterialTheme {
        YearsView(
            selectedYear = 1402,
            digitMode = DigitMode.Persian,
            yearFormatter = YearFormatter.Default,
            yearRange = 1350..1450,
            colors = DatePickerDefaults.lightColors(),
            onYearClick = {},
        )
    }
}
