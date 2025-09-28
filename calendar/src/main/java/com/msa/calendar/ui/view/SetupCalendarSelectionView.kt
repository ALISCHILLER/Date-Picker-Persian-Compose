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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
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
import com.msa.calendar.ui.DatePickerColors
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

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
    title: String,
    subtitle: String,
    strings: DatePickerStrings,
    colors: DatePickerColors,
    quickActions: List<DatePickerQuickAction>,
    onQuickActionClick: (DatePickerQuickAction) -> Unit,
) {
    val gradientBrush = remember(colors.gradientStart, colors.gradientEnd) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return LinearGradientShader(
                    from = Offset.Zero,
                    to = Offset(size.width, size.height),
                    colors = listOf(colors.gradientStart, colors.gradientEnd),
                    colorStops = listOf(0f, 1f)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .background(gradientBrush)
            .animateContentSize()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .background(gradientBrush),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colors.titleTextColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                )


                Text(
                    text = subtitle,
                    color = colors.subtitleTextColor,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                )

            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(gradientBrush),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // KeyboardArrowLeft
            IconButton(
                onClick = {
                    decreaseMonth(
                        mMonth = mMonth,
                        mYear = mYear,
                        setMonth = setMonth,
                        setYear = setYear,
                    )
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowLeft),
                    contentDescription = "Previous Month",
                    tint = colors.controlIconColor
                )
            }

            TextButton(
                onClick = {
                    if (pickerType != PickerType.Year) pickerTypeChang(PickerType.Year)
                    else pickerTypeChang(PickerType.Day)
                }
            ) {
                Text(
                    text = mYear,
                    color = colors.subtitleTextColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    tint = colors.controlIconColor,
                )
            }


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
                    color = colors.subtitleTextColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    tint = colors.controlIconColor,
                )
            }

            IconButton(
                onClick = {
                    increaseMonth(
                        mMonth = mMonth,
                        mYear = mYear,
                        setMonth = setMonth,
                        setYear = setYear,
                    )

                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowRight),
                    contentDescription = "Next Month",
                    tint = colors.controlIconColor
                )

            }
        }

        if (quickActions.isNotEmpty()) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickActions.forEach { action ->
                        AssistChip(
                            onClick = { onQuickActionClick(action) },
                            label = { Text(text = action.label(strings)) },
                            shape = RoundedCornerShape(16.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.todayButtonBackground,
                                labelColor = colors.todayButtonContent,
                            ),
                        )
                    }
                }
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