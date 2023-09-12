package com.msa.persioncalendar.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.msa.persioncalendar.R
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.toPersianNumber

@Composable
fun MonthView(
    mMonth: String,
    onMonthClick: (String) -> Unit,
) {

    val monthsList = listOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند",
    )


    val selectionModifier = Modifier
        .wrapContentHeight()
        .then(Modifier.padding(top = dimensionResource(R.dimen.scd_normal_150)))
    val baseViewModifier = Modifier
        .padding(top = dimensionResource(R.dimen.scd_normal_100))

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
        ){
            items(monthsList){
                val selected = mMonth == it
                val thisMonth =  it == com.msa.calendar.utils.PersionCalendar().getMonth().toPersianNumber()
                val disabled = mMonth.contains(it)
                val textStyle =
                    when {
                        selected -> MaterialTheme.typography.bodySmall.copy(MaterialTheme.colorScheme.onPrimary)
                        thisMonth -> MaterialTheme.typography.titleSmall.copy(MaterialTheme.colorScheme.primary)
                        else -> MaterialTheme.typography.bodyMedium
                    }

                val baseModifier = Modifier
                    .wrapContentWidth()
                    .padding(dimensionResource(R.dimen.scd_small_50))
                    .clickable(!disabled) { onMonthClick(it) }

                val normalModifier = baseModifier
                    .clip(MaterialTheme.shapes.small)

                val selectedModifier = normalModifier
                    .background(MaterialTheme.colorScheme.primary)

                val textAlpha = when {
                    disabled -> Color.Blue
                    else -> Color.Black
                }


                Column(
                    modifier = when {
                        disabled -> normalModifier
                        selected -> selectedModifier
                        thisMonth -> baseModifier
                        else -> normalModifier
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(R.dimen.scd_small_150))
                            .padding(vertical = dimensionResource(R.dimen.scd_small_100)),
                        text =it ,
                        color=textAlpha,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                    )
                }

            }
        }
    }
}