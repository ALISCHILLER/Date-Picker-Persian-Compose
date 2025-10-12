package com.msa.calendar.ui.view

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.utils.PickerType
import com.msa.calendar.ui.DatePickerColors
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Today
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState


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
    val gradientHighlight = remember { CalendarColorTokens.MidSheen }
    val gradientBrush = remember(colors.brandViolet, colors.brandTeal, gradientHighlight) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return LinearGradientShader(
                    from = Offset.Zero,
                    to = Offset(size.width, size.height),
                    colors = listOf(colors.brandViolet, gradientHighlight, colors.brandTeal),
                    colorStops = listOf(0f, 0.55f, 1f)
                )
            }
        }
    }
    val headerShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    Column(
        modifier = Modifier
            .animateContentSize()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(headerShape)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(gradientBrush)
                )
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.brandViolet.copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(size.width * 0.18f, size.height * 0.9f),
                            radius = size.maxDimension * 0.85f,
                        )
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.brandTeal.copy(alpha = 0.38f), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.2f),
                            radius = size.maxDimension * 0.8f,
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title.uppercase(),
                            color = colors.subtitleTextColor,
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.SemiBold,
                        )


                        Text(
                            text = subtitle,
                            color = colors.titleTextColor,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 30.sp,
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
                    val chipShape = RoundedCornerShape(16.dp)
                    val chipBorder = remember(colors.brandViolet, colors.brandTeal) {
                        Brush.linearGradient(
                            listOf(
                                colors.brandViolet.copy(alpha = 0.65f),
                                colors.brandTeal.copy(alpha = 0.55f)
                            )
                        )
                    }
                    val chipGlass = remember(colors.brandViolet, colors.brandTeal) {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                    quickActions.forEach { action ->
                        AssistChip(
                            modifier = Modifier
                                .clip(chipShape)
                                .background(chipGlass),
                            onClick = { onQuickActionClick(action) },
                            label = { Text(text = action.label(strings)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = quickActionIcon(action),
                                    contentDescription = null,
                                )
                            },
                            shape = chipShape,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.Transparent,
                                labelColor = Color.White.copy(alpha = 0.94f),
                                leadingIconContentColor = Color.White.copy(alpha = 0.94f),
                            ),
                            border = BorderStroke(
                                1.dp,
                                chipBorder
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
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 18.dp,
        animationSpec = tween(durationMillis = 160),
        label = "navElevation"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.45f else 0.72f,
        animationSpec = tween(durationMillis = 160),
        label = "navBorder"
    )
    val highlight by animateFloatAsState(
        targetValue = if (isPressed) 0.72f else 0.48f,
        animationSpec = tween(durationMillis = 160),
        label = "navHighlight"
    )
    val backgroundBrush = remember(highlight) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.26f + highlight * 0.08f),
                Color.White.copy(alpha = 0.18f + highlight * 0.06f)
            )
        )
    }
    val borderColor = colors.todayOutline.copy(alpha = borderAlpha)
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush, shape)
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = colors.controlIconColor,
            )
        }
    }
}

@Composable
private fun CalendarSelectorButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    colors: DatePickerColors,
) {
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val emphasisTarget = when {
        isActive -> 0.82f
        isPressed -> 0.58f
        else -> 0.4f
    }
    val emphasis by animateFloatAsState(
        targetValue = emphasisTarget,
        animationSpec = tween(durationMillis = 180),
        label = "selectorEmphasis"
    )
    val elevation by animateDpAsState(
        targetValue = when {
            isActive -> 20.dp
            isPressed -> 10.dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 200),
        label = "selectorElevation"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = when {
            isActive -> 0.85f
            isPressed -> 0.6f
            else -> 0.4f
        },
        animationSpec = tween(durationMillis = 200),
        label = "selectorBorder"
    )
    val backgroundBrush = remember(emphasis) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f + emphasis * 0.28f),
                colors.brandTeal.copy(alpha = 0.24f + emphasis * 0.32f)
            )
        )
    }
    val borderBrush = remember(colors.brandViolet, colors.brandTeal, borderAlpha) {
        Brush.linearGradient(
            colors = listOf(
                colors.brandViolet.copy(alpha = 0.55f * borderAlpha + 0.2f),
                colors.brandTeal.copy(alpha = 0.5f * borderAlpha + 0.18f)
            )
        )
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderBrush),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .background(backgroundBrush, shape)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.94f),
            )
        }
    }
}

private fun quickActionIcon(action: DatePickerQuickAction) = when (action) {
    DatePickerQuickAction.Today -> Icons.Filled.Today
    is DatePickerQuickAction.ClearSelection -> Icons.Filled.HighlightOff
    is DatePickerQuickAction.JumpToDate -> Icons.Filled.Event

}