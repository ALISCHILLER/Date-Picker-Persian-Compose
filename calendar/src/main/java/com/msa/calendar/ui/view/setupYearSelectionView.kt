package com.msa.calendar.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.R
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.toPersianNumber
import kotlinx.coroutines.DelicateCoroutinesApi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class, DelicateCoroutinesApi::class)
@SuppressLint("CoroutineCreationDuringComposition", "SuspiciousIndentation")
@Composable
fun YearsView(
    mYear: String,
    onYearClick: (String) -> Unit,
) {

    val years = mutableListOf<String>()
    for (y in 1350..1450) {
        years.add(y.toPersianNumber())
    }
    val yearListState = rememberLazyListState()
    GlobalScope.launch(Dispatchers.Main) {
        yearListState.scrollToItem(index = 45)
    }

    val behavior = rememberSnapFlingBehavior(
        lazyListState = yearListState,
//        snapOffsetForItem = SnapOffsets.Center,
    )
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "انتخاب سال",
            style = MaterialTheme.typography.titleMedium,
        )

        LazyRow(
            state = yearListState,
            flingBehavior = behavior,
            contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.scd_large_100)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.scd_small_50)),
        ) {
            items(years) {
                val year =  it
                val selected = mYear == year
                val thisYear = year == PersionCalendar().getYear().toPersianNumber()

                val textStyle =
                    when {
                        selected -> MaterialTheme.typography.bodySmall.copy(MaterialTheme.colorScheme.onPrimary)
                        thisYear -> MaterialTheme.typography.titleSmall.copy(MaterialTheme.colorScheme.primary)
                        else -> MaterialTheme.typography.bodyMedium
                    }

                val baseModifier = Modifier
                    .wrapContentWidth()
                    .clip(MaterialTheme.shapes.small)
                    .padding(1.dp)
                    .clickable { onYearClick(year) }

                val selectedModifier = baseModifier
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onYearClick(year) }
                    .padding(4.dp)

                Column(
                    modifier = if (selected) selectedModifier else baseModifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(R.dimen.scd_small_150))
                            .padding(vertical = dimensionResource(R.dimen.scd_small_100)),
                        text = year.toString(),
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

    }

}