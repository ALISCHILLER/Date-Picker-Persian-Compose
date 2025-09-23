package com.msa.persioncalendar.ui.view

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.msa.calendar.utils.getWeekDays

@Composable
fun DayOfWeekView(
    mMonth: String,
    mDay: String,
    mYear: String,
    setDay: (String) -> Unit,
    changeSelectedPart: (String) -> Unit
) {
    val daysList = getWeekDays(mMonth, mYear)

    Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Text(text = "ج", color = Color.Black)
        Text(text = "پ", color = Color.Black)
        Text(text = "چ", color = Color.Black)
        Text(text = "س", color = Color.Black)
        Text(text = "د", color = Color.Black)
        Text(text = "ی", color = Color.Black)
        Text(text = "ش", color = Color.Black)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            items(daysList) {
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f, true)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (it != " ") {
                                changeSelectedPart("main")
                                setDay(it)
                            }
                        },
                    color = if (mDay == it) Color.Blue else Color.White,
                    border = BorderStroke(1.dp, color = Color.White)
                ) {
                    Row(
                        Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = it, style = MaterialTheme.typography.bodyLarge,
                            color = if (mDay == it) Color.White
                            else Color.Black
                        )
                    }
                }
            }

        }
    }
    }


}