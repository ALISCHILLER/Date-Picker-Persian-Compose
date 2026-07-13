package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.utils.PickerType
import java.util.Locale

/**
 * Premium calendar header with stable height and one screen-level drawing layer.
 *
 * All decorative gradients are cached. Quick actions scroll horizontally instead of wrapping and
 * pushing the calendar down, which keeps the month grid stable on compact phones.
 */
@Composable
internal fun CalendarView(
    monthLabel: String,
    yearLabel: String,
    pickerTypeChang: (PickerType) -> Unit,
    pickerType: PickerType,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    previousMonthEnabled: Boolean = true,
    nextMonthEnabled: Boolean = true,
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
    gregorianMonthLabel: String? = null,
    gregorianSelectionLabel: String? = null,
    gregorianHintDirection: LayoutDirection = LayoutDirection.Ltr,
    compact: Boolean = false,
    minimal: Boolean = false,
) {
    val headerShape = if (showHeaderBackground) {
        RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp,
        )
    } else {
        RoundedCornerShape(20.dp)
    }
    val headerPadding = when {
        minimal -> PaddingValues(horizontal = 10.dp, vertical = 7.dp)
        compact -> PaddingValues(horizontal = 14.dp, vertical = 11.dp)
        else -> PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    }
    val quickScrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(headerShape)
                    .then(
                        if (showHeaderBackground) {
                            Modifier.drawWithCache {
                                val base = Brush.linearGradient(
                                    colors = listOf(
                                        colors.brandViolet,
                                        colors.brandViolet.copy(alpha = 0.95f),
                                        colors.brandTeal.copy(alpha = 0.92f),
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, size.height),
                                )
                                val topGlow = Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.17f), Color.Transparent),
                                    center = Offset(size.width * 0.12f, 0f),
                                    radius = size.width * 0.72f,
                                )
                                val lowerGlow = Brush.radialGradient(
                                    colors = listOf(colors.brandTeal.copy(alpha = 0.24f), Color.Transparent),
                                    center = Offset(size.width, size.height),
                                    radius = size.width * 0.66f,
                                )
                                onDrawBehind {
                                    drawRect(base)
                                    drawRect(topGlow)
                                    drawRect(lowerGlow)
                                }
                            }
                        } else {
                            Modifier.background(colors.surfaceVariantColor, headerShape)
                        },
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(headerPadding),
                    verticalArrangement = Arrangement.spacedBy(
                        when {
                            minimal -> 5.dp
                            compact -> 8.dp
                            else -> 10.dp
                        },
                    ),
                ) {
                    if (showHeaderDetails) {
                        HeaderTitleBlock(
                            title = title,
                            subtitle = subtitle,
                            colors = colors,
                            onHeader = showHeaderBackground,
                            compact = compact,
                        )
                    }

                    gregorianSelectionLabel?.let { label ->
                        GregorianHintPill(
                            modifier = Modifier.align(Alignment.Start),
                            text = label,
                            colors = colors,
                            layoutDirection = gregorianHintDirection,
                            compact = compact || minimal,
                            onHeader = showHeaderBackground,
                        )
                    }

                    headerSupportingContent?.invoke()

                    CalendarNavigationBar(
                        monthLabel = monthLabel,
                        yearLabel = yearLabel,
                        pickerType = pickerType,
                        pickerTypeChang = pickerTypeChang,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        previousMonthEnabled = previousMonthEnabled,
                        nextMonthEnabled = nextMonthEnabled,
                        strings = strings,
                        colors = colors,
                        layoutDirection = layoutDirection,
                        compact = compact,
                        minimal = minimal,
                        onHeader = showHeaderBackground,
                    )

                    gregorianMonthLabel?.let { label ->
                        GregorianHintPill(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = label,
                            colors = colors,
                            layoutDirection = gregorianHintDirection,
                            compact = compact,
                            onHeader = showHeaderBackground,
                        )
                    }
                }
            }
        }

        if (quickActions.isNotEmpty()) {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(quickScrollState)
                        .padding(
                            horizontal = if (compact) 14.dp else 20.dp,
                            vertical = if (compact) 7.dp else 9.dp,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    quickActions.forEach { action ->
                        QuickActionPill(
                            text = action.label(strings),
                            icon = quickActionIcon(action),
                            colors = colors,
                            onClick = { onQuickActionClick(action) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderTitleBlock(
    title: String,
    subtitle: String,
    colors: DatePickerColors,
    onHeader: Boolean,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (onHeader) Color.White.copy(alpha = 0.13f) else colors.currentChoiceColor,
            contentColor = if (onHeader) colors.titleTextColor else colors.brandViolet,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(
                1.dp,
                if (onHeader) Color.White.copy(alpha = 0.20f) else colors.outlineColor,
            ),
        ) {
            Box(
                modifier = Modifier.size(if (compact) 40.dp else 44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 21.dp else 23.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title.uppercase(Locale.ROOT),
                color = if (onHeader) {
                    colors.subtitleTextColor.copy(alpha = 0.84f)
                } else {
                    colors.dayTextColor.copy(alpha = 0.62f)
                },
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 0.9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = if (onHeader) colors.titleTextColor else colors.dayTextColor,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compact) 27.sp else 31.sp,
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CalendarNavigationBar(
    monthLabel: String,
    yearLabel: String,
    pickerType: PickerType,
    pickerTypeChang: (PickerType) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    previousMonthEnabled: Boolean,
    nextMonthEnabled: Boolean,
    strings: DatePickerStrings,
    colors: DatePickerColors,
    layoutDirection: LayoutDirection,
    compact: Boolean,
    minimal: Boolean,
    onHeader: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarNavigationButton(
            onClick = onPreviousMonth,
            enabled = previousMonthEnabled,
            icon = if (layoutDirection == LayoutDirection.Rtl) {
                Icons.Filled.KeyboardArrowRight
            } else {
                Icons.Filled.KeyboardArrowLeft
            },
            contentDescription = strings.previousMonth,
            colors = colors,
            onHeader = onHeader,
        )

        Spacer(modifier = Modifier.width(if (minimal) 4.dp else if (compact) 7.dp else 9.dp))

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = if (onHeader) Color.White.copy(alpha = 0.09f) else colors.surfaceColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(
                1.dp,
                if (onHeader) Color.White.copy(alpha = 0.16f) else colors.outlineColor,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (minimal) 2.dp else 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalendarSelectorButton(
                    modifier = Modifier.weight(1f),
                    text = yearLabel,
                    contentDescription = "${strings.selectYear}: $yearLabel",
                    isActive = pickerType == PickerType.Year,
                    onClick = {
                        pickerTypeChang(if (pickerType == PickerType.Year) PickerType.Day else PickerType.Year)
                    },
                    colors = colors,
                    compact = compact,
                    onHeader = onHeader,
                )
                CalendarSelectorButton(
                    modifier = Modifier.weight(1f),
                    text = monthLabel,
                    contentDescription = "${strings.selectMonth}: $monthLabel",
                    isActive = pickerType == PickerType.Month,
                    onClick = {
                        pickerTypeChang(if (pickerType == PickerType.Month) PickerType.Day else PickerType.Month)
                    },
                    colors = colors,
                    compact = compact,
                    onHeader = onHeader,
                )
            }
        }

        Spacer(modifier = Modifier.width(if (minimal) 4.dp else if (compact) 7.dp else 9.dp))

        CalendarNavigationButton(
            onClick = onNextMonth,
            enabled = nextMonthEnabled,
            icon = if (layoutDirection == LayoutDirection.Rtl) {
                Icons.Filled.KeyboardArrowLeft
            } else {
                Icons.Filled.KeyboardArrowRight
            },
            contentDescription = strings.nextMonth,
            colors = colors,
            onHeader = onHeader,
        )
    }
}

@Composable
private fun GregorianHintPill(
    text: String,
    colors: DatePickerColors,
    layoutDirection: LayoutDirection,
    compact: Boolean,
    onHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    val background = if (onHeader) Color.White.copy(alpha = 0.10f) else colors.surfaceColor
    val border = if (onHeader) Color.White.copy(alpha = 0.17f) else colors.outlineColor
    val contentColor = if (onHeader) {
        colors.titleTextColor.copy(alpha = 0.90f)
    } else {
        colors.dayTextColor.copy(alpha = 0.74f)
    }

    Surface(
        modifier = modifier
            .widthIn(max = 360.dp)
            .semantics { contentDescription = text },
        shape = RoundedCornerShape(50),
        color = background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, border),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (compact) 10.dp else 12.dp,
                    vertical = if (compact) 5.dp else 6.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.78f),
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = text,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    text: String,
    icon: ImageVector,
    colors: DatePickerColors,
    onClick: () -> Unit,
) {
    val contentColor = remember(colors.containerColor, colors.brandViolet) {
        if (colors.containerColor.luminance() > 0.5f) colors.brandViolet else Color.White.copy(alpha = 0.92f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.semantics {
            role = Role.Button
            contentDescription = text
        },
        shape = RoundedCornerShape(50),
        color = colors.surfaceVariantColor.copy(alpha = 0.84f),
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, colors.outlineColor),
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 40.dp)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CalendarNavigationButton(
    icon: ImageVector,
    contentDescription: String,
    colors: DatePickerColors,
    enabled: Boolean,
    onHeader: Boolean,
    onClick: () -> Unit,
) {
    val background = if (onHeader) {
        Color.White.copy(alpha = if (enabled) 0.13f else 0.055f)
    } else {
        colors.surfaceVariantColor
    }
    val border = if (onHeader) {
        Color.White.copy(alpha = if (enabled) 0.22f else 0.08f)
    } else {
        colors.outlineColor
    }
    val tint = if (onHeader) colors.controlIconColor else colors.dayTextColor

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(44.dp)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
                if (!enabled) disabled()
            },
        shape = CircleShape,
        color = background,
        contentColor = tint,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, border),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint.copy(alpha = if (enabled) 1f else 0.32f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun CalendarSelectorButton(
    modifier: Modifier = Modifier,
    text: String,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    colors: DatePickerColors,
    compact: Boolean,
    onHeader: Boolean,
) {
    val background = when {
        onHeader && isActive -> Color.White.copy(alpha = 0.20f)
        onHeader -> Color.Transparent
        isActive -> colors.currentChoiceColor
        else -> Color.Transparent
    }
    val contentColor = if (onHeader) colors.titleTextColor else colors.dayTextColor

    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 40.dp)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
                if (isActive) selected = true
            },
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.82f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun quickActionIcon(action: DatePickerQuickAction): ImageVector = when (action) {
    DatePickerQuickAction.Today -> Icons.Filled.Today
    is DatePickerQuickAction.ClearSelection -> Icons.Filled.HighlightOff
    is DatePickerQuickAction.JumpToDate -> Icons.Filled.Event
}
