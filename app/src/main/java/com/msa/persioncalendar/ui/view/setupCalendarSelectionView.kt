package com.msa.persioncalendar.ui.view

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.PickerType

@Composable
fun CalendarView(
    mMonth: String,
    mDay: String,
    mYear: String,
    pickerTypeChang: (com.msa.calendar.utils.PickerType) -> Unit,
    pickerType: (com.msa.calendar.utils.PickerType),
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
                    text = "$mYear $mMonth  $mDay",
                    color = Color.White,
                    style = TextStyle(),
                    fontWeight = FontWeight.Bold ,
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
                    if (pickerType != com.msa.calendar.utils.PickerType.Year)
                        pickerTypeChang(com.msa.calendar.utils.PickerType.Year)
                    else
                        pickerTypeChang(com.msa.calendar.utils.PickerType.Day)
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
                    if (pickerType != com.msa.calendar.utils.PickerType.Month)
                        pickerTypeChang(com.msa.calendar.utils.PickerType.Month)
                    else
                        pickerTypeChang(com.msa.calendar.utils.PickerType.Day)
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