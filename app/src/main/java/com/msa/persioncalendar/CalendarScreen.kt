@file:OptIn(ExperimentalTextApi::class)

package com.msa.persioncalendar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.persioncalendar.ui.theme.*
import com.msa.persioncalendar.utils.PersionCalendar
import com.msa.persioncalendar.utils.getweekDay
import com.msa.persioncalendar.utils.toPersianNumber
import java.time.DayOfWeek

@Composable
fun CalendarScreen(
    onDismiss: (Boolean) -> Unit,
) {
    val today = PersionCalendar().getDay()
    val month = PersionCalendar().getMonth()
    val year = PersionCalendar().getYear()

    val monthsList = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
        "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )
    val mMonth by remember {
        mutableStateOf(monthsList[month - 1])
    }

    val mYear by remember {
        mutableStateOf(year.toPersianNumber())
    }

    var mDay by remember {
        mutableStateOf(today.toPersianNumber())
    }
    var selectedDay by remember {
        mutableStateOf("main")
    }

    Dialog(
        onDismissRequest = { onDismiss(true) },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    // same action as in onDismissRequest
                    onDismiss(true)
                }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {

                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .animateContentSize()
                ) {
                    CalendarView( mMonth = mMonth,
                        mDay = mDay,
                        mYear = mYear,)
                    DayOfWeekView(
                        mMonth = mMonth,
                        mDay = mDay,
                        mYear = mYear,
                        setDay = { mDay = it },
                        {}
                    )

                }
            }
        }
    }

}

@Composable
fun CalendarView(
    mMonth: String,
    mDay: String,
    mYear: String,
) {
    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = listOf(Color(0xFF2be4dc), Color(0xFF243484)),
                center = size.center,
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }
    Column(
        modifier = Modifier
            .background(
                largeRadialGradient
            )
            .animateContentSize()
    ) {
        val persion by remember {
            mutableStateOf(PersionCalendar())
            //   mutableStateOf(initialDate ?: PersionCalendar())
        }
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .background(largeRadialGradient),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "انتخاب تاریخ",
                    color = Color.White
                )


                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "$mYear $mMonth  $mDay" ,
                    color = Color.White,
                    style = TextStyle(),
                    fontWeight = FontWeight.Bold
                )

            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(largeRadialGradient),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // KeyboardArrowLeft
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(43.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowLeft),
                    contentDescription = "arrow",
                    tint = Color.White
                )
            }


            //Years
            TextButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.padding(0.dp)
            ) {
                Text(
                    text = persion.getYear().toPersianNumber(),
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White
                )
            }

            ///Month
            TextButton(
                onClick = { /*TODO*/ }
            )
            {
                Text(text = persion.getMonthString(), color = Color.White)
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(46.dp)
            )
            {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowRight),
                    contentDescription = "Right",
                    tint = Color.White
                )

            }
        }

    }
}

@Composable
fun DayOfWeekView(
    mMonth: String,
    mDay: String,
    mYear: String,
    setDay: (String) -> Unit,
    changeSelectedPart: (String) -> Unit
) {
    val daysList = getweekDay(mMonth, mDay, mYear)
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
                        .clip(MaterialTheme.shapes.extraSmall)
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


@Composable
@Preview(showBackground = true)
fun CalendarScreenPreview() {
    var hideDatePicker by remember {
        mutableStateOf(true)
    }
    CalendarScreen(
        onDismiss = { hideDatePicker = true }
    )
}