package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.R
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.utils.PersionCalendar

@Preview
@Composable
fun MonthViewPreview() {
    MonthView(
        selectedMonth = 3,
        displayedYear = 1402,
        digitMode = DigitMode.Persian,
        monthFormatter = MonthFormatter.Persian,
        colors = DatePickerDefaults.lightColors(),
        onMonthSelected = {},
    )
}

@Composable
fun MonthView(
    selectedMonth: Int,
    displayedYear: Int,
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    colors: DatePickerColors,
    onMonthSelected: (Int) -> Unit,
) {
    val monthNames = monthFormatter.labels(digitMode)

    val selectionModifier = Modifier
        .wrapContentHeight()
        .then(Modifier.padding(top = dimensionResource(R.dimen.scd_normal_150)))
    val baseViewModifier = Modifier
        .padding(top = dimensionResource(R.dimen.scd_normal_100))

    val todayCalendar = remember { PersionCalendar() }
    val currentMonthIndex = todayCalendar.getMonth() - 1
    val currentYear = todayCalendar.getYear()
    val isCurrentYear = displayedYear == currentYear

    val buttonShape = RoundedCornerShape(12.dp)
    val buttonPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)

    Column(
        modifier = selectionModifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "انتخاب ماه",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        LazyVerticalGrid(
            modifier = baseViewModifier,
            columns = GridCells.Fixed(3),
        ) {
            itemsIndexed(monthNames) { index, monthName ->
                val monthNumber = index + 1
                val selected = selectedMonth == monthNumber
                val isCurrentMonth = isCurrentYear && index == currentMonthIndex

                val itemModifier = Modifier
                    .wrapContentWidth()
                    .padding(dimensionResource(R.dimen.scd_small_50))

                val handleClick = { onMonthSelected(monthNumber) }

                val textContent: @Composable () -> Unit = {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                    )
                }

                when {
                    selected -> {
                        Button(
                            modifier = itemModifier,
                            onClick = handleClick,
                            shape = buttonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.confirmButtonBackground,
                                contentColor = colors.confirmButtonContent,
                                disabledContainerColor = colors.confirmButtonBackground.copy(alpha = 0.3f),
                                disabledContentColor = colors.confirmButtonContent.copy(alpha = 0.4f),
                            ),
                            contentPadding = buttonPadding,
                        ) { textContent() }
                    }

                    isCurrentMonth -> {
                        OutlinedButton(
                            modifier = itemModifier,
                            onClick = handleClick,
                            shape = buttonShape,
                            border = BorderStroke(1.25.dp, colors.todayOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = colors.todayOutline,
                            ),
                            contentPadding = buttonPadding,
                        ) { textContent() }
                    }

                    else -> {
                        TextButton(
                            modifier = itemModifier,
                            onClick = handleClick,
                            shape = buttonShape,
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            contentPadding = buttonPadding,
                        ) { textContent() }
                    }
                }
            }
        }
    }
}
