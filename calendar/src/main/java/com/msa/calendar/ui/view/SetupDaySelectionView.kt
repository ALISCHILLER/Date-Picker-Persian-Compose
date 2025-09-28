package com.msa.calendar.ui.view

import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.components.shadow
import com.msa.calendar.ui.theme.*
import com.msa.calendar.utils.JlResDimens
import com.msa.calendar.utils.getWeekDays
import com.msa.calendar.utils.monthsList
import com.msa.calendar.utils.toIntSafely
import com.msa.calendar.utils.SoleimaniDate
import androidx.compose.ui.graphics.SolidColor

@Composable
fun DayOfWeekView(
    mMonth: String,
    mDay: String,
    mYear: String,
    highlightedDay: String?,
    highlightColor: Color,
    setDay: (String) -> Unit,
    isDateEnabled: (SoleimaniDate) -> Boolean = { true },
    changeSelectedPart: (String) -> Unit = {}
) {
    val daysList = getWeekDays(mMonth, mYear)
    val selectedDayValue = mDay.toIntSafely()
    val monthNumber = monthsList.indexOf(mMonth).takeIf { it >= 0 }?.plus(1)

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                items(daysList) { dayValue ->
                    val candidateDate = if (monthNumber != null && dayValue.isNotBlank()) {
                        SoleimaniDate.fromLocalizedStrings(mYear, monthNumber.toString(), dayValue)
                    } else null
                    val isEnabled = candidateDate?.let(isDateEnabled) ?: false
                    val isSelected = isEnabled && selectedDayValue != null && candidateDate?.day == selectedDayValue
                    val isToday = highlightedDay != null && highlightedDay == dayValue
                    val baseShadowColor = when {
                        isSelected -> Purple40
                        isToday && isEnabled -> highlightColor
                        else -> PurpleGrey80
                    }
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f, true)
                            .padding(4.dp)
                            .shadow(
                                color = if (isEnabled) baseShadowColor else PurpleGrey80.copy(alpha = 0.3f),
                                borderRadius = 10.dp,
                                offsetX = 0.0.dp,
                                offsetY = 3.dp,
                                spread = 3.dp,
                                blurRadius = 10.0.dp
                            )
                            .let { modifier ->
                                if (isToday && !isSelected && isEnabled) {
                                    modifier.border(
                                        width = JlResDimens.dp1,
                                        shape = RoundedCornerShape(JlResDimens.dp10),
                                        brush = SolidColor(highlightColor)
                                    )
                                } else {
                                    modifier
                                }
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                enabled = isEnabled && dayValue.isNotBlank(),
                                onClick = {
                                    changeSelectedPart("main")
                                    setDay(dayValue)
                                }
                            ),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> Color.White
                        },
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayValue,
                                style = MaterialTheme.typography.bodyLarge,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday && isEnabled -> highlightColor
                                    isEnabled -> Color.Black
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                },
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Cursive

                            )
                        }

                    }
                }
            }
        }
    }


}


@Preview
@Composable
fun DayOfWeekViewPreview() {
    DayOfWeekView(
        mMonth = "5",
        mDay = "10",
        mYear = "2024",
        highlightedDay = "8",
        highlightColor = Color(0xFF3B82F6),
        setDay = { },
    )
}