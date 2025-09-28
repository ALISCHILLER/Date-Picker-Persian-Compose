package com.msa.calendar.ui.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.toPersianNumber
import com.msa.calendar.utils.toIntSafely
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.msa.calendar.ui.DigitMode


@Composable
fun YearsView(
    mYear: String,
    digitMode: DigitMode,
    onYearClick: (String) -> Unit,
) {

    val currentYear = remember { PersionCalendar().getYear() }
    val years = remember(digitMode) {
        (1350..1450).map { value ->
            value to value.toDigitString(digitMode)
        }
    }
    val selectedYearValue = remember(mYear) { mYear.toIntSafely() }
    val yearListState = rememberLazyGridState()
    LaunchedEffect(selectedYearValue, digitMode) {
        val targetIndex = years.indexOfFirst { it.first == selectedYearValue }
        if (targetIndex >= 0) {
            yearListState.scrollToItem(index = targetIndex)
        }
    }



    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = yearListState,
        horizontalArrangement = Arrangement.spacedBy(
            space = 4.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(years) { (yearValue, yearLabel) ->
            val isSelected = selectedYearValue == yearValue
            val isCurrentYear = yearValue == currentYear
            when {
                isSelected -> {
                    Button(onClick = { onYearClick(yearLabel) }) {
                        Text(yearLabel, maxLines = 1)
                    }
                }
                isCurrentYear -> {
                    OutlinedButton(onClick = { onYearClick(yearLabel) }) {
                        Text(yearLabel, maxLines = 1)
                    }
                }
                else -> {
                    TextButton(onClick = { onYearClick(yearLabel) }) {
                        Text(yearLabel, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun YearsViewPreview() {
    MaterialTheme {
        YearsView(
            mYear = "1402",
            digitMode = DigitMode.Persian,
            onYearClick = {},
        )
    }
}
private fun Int.toDigitString(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> toPersianNumber()
    DigitMode.Latin -> toString()
}