package com.msa.calendar.ui.view

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.utils.PickerType
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.draw.clip

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
    showHeaderDetails: Boolean = true,
    showHeaderBackground: Boolean = true,
    headerSupportingContent: (@Composable () -> Unit)? = null,
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

    val headerShape = if (showHeaderBackground) {
        RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    } else {
        RoundedCornerShape(0.dp)
    }
    val headerPadding = if (showHeaderBackground) {
        PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    } else {
        PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    }
    val navigationPadding = if (showHeaderBackground) 0.dp else 8.dp
    val quickActionsPadding = if (showHeaderBackground) {
        PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    } else {
        PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    }

    Column(modifier = Modifier.animateContentSize()) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(headerShape)
            ) {
                if (showHeaderBackground) {
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
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(headerPadding),
                    verticalArrangement = Arrangement.spacedBy(
                        if (showHeaderDetails || headerSupportingContent != null) 16.dp else 12.dp
                    )
                ) {
                    if (showHeaderDetails) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    }

                    headerSupportingContent?.let { it() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = navigationPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalendarNavigationButton(
                            onClick = onPreviousMonth,
                            icon = Icons.Filled.KeyboardArrowLeft,
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
                                    if (pickerType != PickerType.Month) pickerTypeChang(PickerType.Month)
                                    else pickerTypeChang(PickerType.Day)
                                },
                                colors = colors,
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        CalendarNavigationButton(
                            onClick = onNextMonth,
                            icon = Icons.Filled.KeyboardArrowRight,
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
                        .padding(quickActionsPadding),
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
                    val quickActionContentColor = remember(colors.containerColor, colors.brandViolet) {
                        val baseLuminance = colors.containerColor.luminance()
                        if (baseLuminance > 0.5f) {
                            colors.brandViolet.copy(alpha = 0.9f)
                        } else {
                            Color.White.copy(alpha = 0.94f)
                        }
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
                                labelColor = quickActionContentColor,
                                leadingIconContentColor = quickActionContentColor,
                            ),
                            border = BorderStroke(1.dp, chipBorder)
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
        modifier = Modifier.size(44.dp),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush, shape)
                .fillMaxSize()
                .padding(10.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
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
        modifier = Modifier.height(44.dp),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderBrush),
    ) {
        Row(
            modifier = Modifier
                .background(backgroundBrush, shape)
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
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
