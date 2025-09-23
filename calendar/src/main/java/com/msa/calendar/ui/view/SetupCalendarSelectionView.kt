package com.msa.calendar.ui.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.utils.PickerType
import com.msa.calendar.utils.monthsList
import com.msa.calendar.utils.toIntSafely
import com.msa.calendar.utils.toPersianNumber

@Composable
fun CalendarView(
    mMonth: String,
    mDay: String,
    mYear: String,
    pickerTypeChang: (PickerType) -> Unit,
    pickerType: (PickerType),
    setDay: (String) -> Unit,
    setMonth: (String) -> Unit,
    setYear: (String) -> Unit,
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
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Serif,
                )


                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "$mYear $mMonth $mDay",
                    color = Color.White,
                    style = TextStyle(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    fontFamily = FontFamily.Serif,
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
                onClick = {
                    decreaseMonth(
                        mMonth,
                        mYear,
                        setMonth = { setMonth(it) },
                        setYear = { setYear(it) })
                },
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
                    text = mYear,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White,
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
                Text(
                    text = mMonth,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White
                )
            }

            // KeyboardArrowRight
            IconButton(
                onClick = {
                    increaseMonth(
                        mMonth,
                        mYear,
                        setMonth = { setMonth(it) },
                        setYear = { setYear(it) }
                    )

                },
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

private fun increaseMonth(
    mMonth: String,
    mYear: String,
    setMonth: (String) -> Unit,
    setYear: (String) -> Unit
) {
    val currentIndex = monthsList.indexOf(mMonth)
    if (currentIndex in 0 until monthsList.lastIndex) {
        setMonth(monthsList[currentIndex + 1])
    } else if (currentIndex == monthsList.lastIndex) {
        setMonth(monthsList.first())
        val nextYear = mYear.toIntSafely()?.plus(1)
        if (nextYear != null) {
            setYear(nextYear.toPersianNumber())
        }
    }
}

private fun decreaseMonth(
    mMonth: String,
    mYear: String,
    setMonth: (String) -> Unit,
    setYear: (String) -> Unit
) {
    val currentIndex = monthsList.indexOf(mMonth)
    if (currentIndex > 0) {
        setMonth(monthsList[currentIndex - 1])
    } else if (currentIndex == 0) {
        setMonth(monthsList.last())
        val previousYear = mYear.toIntSafely()?.minus(1)
        if (previousYear != null) {
            setYear(previousYear.toPersianNumber())
        }
    }
}