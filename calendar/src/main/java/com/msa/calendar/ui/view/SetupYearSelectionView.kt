package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.YearFormatter
import com.msa.calendar.utils.PersionCalendar

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
        yearRange.map { value -> value to yearFormatter.format(value, digitMode) }
    }

    val selectedYearValue = selectedYear
    val yearListState = rememberLazyGridState()

    LaunchedEffect(selectedYearValue, digitMode) {
        val idx = years.indexOfFirst { it.first == selectedYearValue }
        if (idx >= 0) yearListState.scrollToItem(idx)
    }

    val buttonShape = RoundedCornerShape(12.dp)
    val buttonPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = yearListState,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(years) { (yearValue, yearLabel) ->
            val isSelected = selectedYearValue == yearValue
            val isCurrentYear = yearValue == currentYear

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
                    ) {
                        Text(yearLabel, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    }
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
                    ) {
                        Text(yearLabel, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    }
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
                    ) {
                        Text(yearLabel, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
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
