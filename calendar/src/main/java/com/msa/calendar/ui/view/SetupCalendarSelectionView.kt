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
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.utils.PickerType
import com.msa.calendar.ui.DatePickerColors
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.clip
import kotlin.math.max
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarView(
    monthLabel: String,
    yearLabel: String,
    pickerTypeChang: (PickerType) -> Unit,
    pickerType: PickerType,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    title: String,
    subtitle: String,
    strings: DatePickerStrings,
    colors: DatePickerColors,
    quickActions: List<DatePickerQuickAction>,
    onQuickActionClick: (DatePickerQuickAction) -> Unit,
    layoutDirection: LayoutDirection,
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
            .animateContentSize()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(gradientBrush)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CalendarNavigationButton(
                        onClick = onPreviousMonth,
                        icon = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        colors = colors,
                    )


                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalendarSelectorButton(
                            text = yearLabel,
                            isActive = pickerType == PickerType.Year,
                            onClick = {
                                if (pickerType != PickerType.Year) pickerTypeChang(PickerType.Year)
                                else pickerTypeChang(PickerType.Day)
                            },
                            colors = colors,
                        )
                        CalendarSelectorButton(
                            text = monthLabel,
                            isActive = pickerType == PickerType.Month,
                            onClick = {
                                if (pickerType != PickerType.Month)
                                    pickerTypeChang(PickerType.Month)
                                else
                                    pickerTypeChang(PickerType.Day)
                            },
                            colors = colors,
                        )
                    }


                    Spacer(modifier = Modifier.width(12.dp))

                    CalendarNavigationButton(
                        onClick = onNextMonth,
                        icon = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        colors = colors,
                    )
                }

            }
        }

        if (quickActions.isNotEmpty()) {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickActions.forEach { action ->
                        AssistChip(
                            onClick = { onQuickActionClick(action) },
                            label = { Text(text = action.label(strings)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = quickActionIcon(action),
                                    contentDescription = null,
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.todayButtonBackground.copy(alpha = 0.35f),
                                labelColor = colors.cancelButtonContent,
                                leadingIconContentColor = colors.cancelButtonContent,
                            ),
                            border = BorderStroke(
                                1.dp,
                                colors.todayButtonBackground.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }

    }
}
@Composable
private fun CalendarNavigationButton(
    icon: ImageVector,
    contentDescription: String,
    colors: DatePickerColors,
    onClick: () -> Unit,
) {
    val containerAlpha = max(colors.todayButtonBackground.alpha, 0.35f)
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = colors.todayButtonBackground.copy(alpha = containerAlpha),
            contentColor = colors.controlIconColor,
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun CalendarSelectorButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    colors: DatePickerColors,
) {
    val baseBackground = colors.todayButtonBackground
    val containerAlpha = if (isActive) max(baseBackground.alpha, 0.45f) else max(baseBackground.alpha, 0.25f)
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = baseBackground.copy(alpha = containerAlpha),
            contentColor = colors.todayButtonContent,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            color = colors.todayButtonContent,
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Outlined.ArrowDropDown,
            contentDescription = null,
            tint = colors.todayButtonContent,
        )
    }
}

private fun quickActionIcon(action: DatePickerQuickAction) = when (action) {
    DatePickerQuickAction.Today -> Icons.Filled.Today
    is DatePickerQuickAction.ClearSelection -> Icons.Filled.HighlightOff
    is DatePickerQuickAction.JumpToDate -> Icons.Filled.Event

}