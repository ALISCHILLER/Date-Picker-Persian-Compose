package com.msa.calendar.ui.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.msa.calendar.R
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.toPersianNumber
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import com.msa.calendar.utils.monthsList

@Preview
@Composable
fun MonthViewPreview() {
    MonthView(
        "3",
        {},
        {}
    )
}

@Composable
fun MonthView(
    mMonth: String,
    onMonthClick: (String) -> Unit,
    setMonth: (String) ->Unit
) {

    val monthNames = monthsList
    val selectionModifier = Modifier
        .wrapContentHeight()
        .then(Modifier.padding(top = dimensionResource(R.dimen.scd_normal_150)))
    val baseViewModifier = Modifier
        .padding(top = dimensionResource(R.dimen.scd_normal_100))

    val todayCalendar = remember { PersionCalendar() }
    val currentMonthIndex = todayCalendar.getMonth() - 1

    Column(
        modifier = selectionModifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "انتخاب ماه",
            style = MaterialTheme.typography.titleMedium,
        )
        LazyVerticalGrid(
            modifier = baseViewModifier,
            columns = GridCells.Fixed(3),
        ) {
            itemsIndexed(monthNames) { index, monthName ->
                val selected = mMonth == monthName
                val isCurrentMonth = index == currentMonthIndex
                val monthNumber = index + 1

                val itemModifier = Modifier
                    .wrapContentWidth()
                    .padding(dimensionResource(R.dimen.scd_small_50))
                val onMonthSelected = {
                    onMonthClick(monthName)
                    setMonth(monthNumber.toPersianNumber())
                }
                val textColor = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    isCurrentMonth -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }


                val textContent: @Composable () -> Unit = {
                    Text(
                        text = monthName,
                        color = textColor,
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
                            onClick = onMonthSelected
                        ) {
                            textContent()
                        }
                    }

                    isCurrentMonth -> {
                        OutlinedButton(
                            modifier = itemModifier,
                            onClick = onMonthSelected
                        ) {
                            textContent()
                        }
                    }

                    else -> {
                        TextButton(
                            modifier = itemModifier,
                            onClick = onMonthSelected
                        ) {
                            textContent()
                        }
                    }
                }
            }
        }
    }
}