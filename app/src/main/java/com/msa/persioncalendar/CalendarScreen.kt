@file:OptIn(ExperimentalTextApi::class)

package com.msa.persioncalendar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.msa.persioncalendar.ui.theme.*
import com.msa.persioncalendar.utils.PersionCalendar
import com.msa.persioncalendar.utils.PickerType
import com.msa.persioncalendar.utils.getweekDay
import com.msa.persioncalendar.utils.toPersianNumber
import com.smarttoolfactory.animatedlist.ActiveColor
import com.smarttoolfactory.animatedlist.AnimatedInfiniteLazyRow
import com.smarttoolfactory.animatedlist.InactiveColor
import kotlinx.coroutines.launch
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
    var pickerType: PickerType by remember {
        mutableStateOf(PickerType.Year)
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

                    CalendarView(
                        mMonth = mMonth,
                        mDay = mDay,
                        mYear = mYear,
                        pickerTypeChang = { pickerType = it },
                        pickerType = pickerType
                    )

                    Crossfade(pickerType, label = "") {
                        when (it) {
                            PickerType.Day -> DayOfWeekView(
                                mMonth = mMonth,
                                mDay = mDay,
                                mYear = mYear,
                                setDay = { mDay = it },
                                {}
                            )

                            PickerType.Year -> YearsView(

                            )

                            PickerType.Month -> YearsView(

                            )

                            else -> DayOfWeekView(
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
    }

}

@Composable
fun CalendarView(
    mMonth: String,
    mDay: String,
    mYear: String,
    pickerTypeChang: (PickerType) -> Unit,
    pickerType: (PickerType),
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
                    .padding(5.dp)
                    .padding(4.dp)
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
                    text = "$mYear $mMonth  $mDay",
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
                onClick = {
                    if (pickerType != PickerType.Year)
                        pickerTypeChang(PickerType.Year)
                    else
                        pickerTypeChang(PickerType.Day)
                },
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
                onClick = {
                    if (pickerType != PickerType.Month)
                        pickerTypeChang(PickerType.Month)
                    else
                        pickerTypeChang(PickerType.Day)
                }
            )
            {
                Text(text = persion.getMonthString(), color = Color.White)
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White
                )
            }

            // KeyboardArrowRight
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
fun MonthView(

) {

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

@Composable
fun YearsView(

) {
    var years = mutableListOf<Int>()
    for (y in 1350 downTo 1450){
        years.add(y)
    }

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
        val listWidth = LocalDensity.current.run { 1000.toDp() }
        val spaceBetweenItems = LocalDensity.current.run { 30.toDp() }
        val initialVisibleItem = 0
        val visibleItemCount = 5
        val initialSelectedItem = 2

        var selectedItem by remember {
            mutableStateOf(initialSelectedItem)
        }



        AnimatedInfiniteLazyRow(
            modifier = Modifier.width(300.dp),
            items = years,
            visibleItemCount = 7,
            selectorIndex = 3,
            inactiveColor = InactiveColor,
            activeColor = ActiveColor,
            inactiveItemPercent = 70,
            itemContent = { animationProgress, index, item, size, lazyListState ->

                val color = animationProgress.color
                val scale = animationProgress.scale

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .background(color, CircleShape)
                        .size(size)
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                lazyListState.animateScrollBy(animationProgress.distanceToSelector)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        "$index",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )

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