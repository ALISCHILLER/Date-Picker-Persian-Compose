package com.msa.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.ui.view.CalendarView
import com.msa.calendar.ui.view.DayOfWeekView
import com.msa.calendar.ui.view.MonthView
import com.msa.calendar.ui.view.YearsView
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.PickerType
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.adjustDayIfOutOfBounds

@Composable
fun CalendarScreen(
    onDismiss: (Boolean) -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: SoleimaniDate? = null,
    config: DatePickerConfig = DatePickerConfig(),
    onDateSelected: (SoleimaniDate) -> Unit = {},
) {
    val strings = config.strings
    val colors = config.colors
    val constraints = config.constraints
    val weekConfiguration = config.weekConfiguration

    val quickActions = remember(config.quickActions, config.showTodayAction) {
        when {
            config.quickActions.isNotEmpty() -> config.quickActions
            config.showTodayAction -> listOf(DatePickerQuickAction.Today)
            else -> emptyList()
        }
    }

    val shape: Shape = config.containerShape
    val todayCalendar = remember { PersionCalendar() }

    val todayDate = remember { todayCalendar.getDay() }
    val todayMonth = remember { todayCalendar.getMonth() }
    val todayYear = remember { todayCalendar.getYear() }
    val todaySoleimani = remember { SoleimaniDate(todayYear, todayMonth, todayDate) }

    val baseDate = remember(initialDate, constraints) {
        val fallback = initialDate ?: todaySoleimani
        constraints.nearestValidOrNull(fallback)
            ?: constraints.minDate
            ?: constraints.maxDate
            ?: fallback
    }

    var pickerType: PickerType by remember { mutableStateOf(PickerType.Day) }

    var selectedYear by remember { mutableStateOf(baseDate.year) }
    var selectedMonth by remember { mutableStateOf(baseDate.month.coerceIn(1, 12)) }
    var selectedDay by remember { mutableStateOf<Int?>(baseDate.day) }

    LaunchedEffect(selectedMonth, selectedYear) {
        adjustDayIfOutOfBounds(
            dayValue = selectedDay,
            month = selectedMonth,
            year = selectedYear,
        )?.let { coerced ->
            selectedDay = coerced
        }
    }

    fun updateSelectionFromDate(target: SoleimaniDate) {
        selectedYear = target.year
        selectedMonth = target.month.coerceIn(1, 12)
        selectedDay = target.day
        pickerType = PickerType.Day
    }

    Dialog(onDismissRequest = { onDismiss(true) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            // بک‌دراپ کلیکی برای dismiss
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onDismiss(true) }
            )

            Box(
                modifier = modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .widthIn(min = 280.dp, max = 420.dp)
            ) {
                val containerBorder = remember(colors.brandViolet, colors.brandTeal) {
                    Brush.linearGradient(
                        colors = listOf(
                            colors.brandViolet.copy(alpha = 0.6f),
                            colors.brandTeal.copy(alpha = 0.6f),
                        )
                    )
                }
                val containerSheen = remember(colors.brandTeal) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset.Zero,
                    )
                }

                // افکت‌های پس‌زمینه
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val primaryRadius = size.width * 0.95f
                            val cornerRadius = androidx.compose.ui.geometry.CornerRadius(48f, 48f)
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors.brandViolet.copy(alpha = 0.38f),
                                        Color.Transparent,
                                    ),
                                    center = Offset(0f, size.height * 0.15f),
                                    radius = primaryRadius,
                                ),
                                cornerRadius = cornerRadius,
                            )
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors.brandTeal.copy(alpha = 0.3f),
                                        Color.Transparent,
                                    ),
                                    center = Offset(size.width, size.height),
                                    radius = primaryRadius * 0.9f,
                                ),
                                cornerRadius = cornerRadius,
                            )
                        }
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val cornerRadius = androidx.compose.ui.geometry.CornerRadius(48f, 48f)
                            drawRoundRect(
                                brush = containerSheen,
                                cornerRadius = cornerRadius,
                                alpha = 1f
                            )
                        },
                    shape = shape,
                    tonalElevation = 0.dp,
                    shadowElevation = 42.dp,
                    border = BorderStroke(1.25.dp, containerBorder),
                    color = colors.containerColor.copy(alpha = 0.9f),
                ) {
                    CompositionLocalProvider(
                        LocalLayoutDirection provides weekConfiguration.layoutDirection,
                    ) {
                        Column(modifier = Modifier) {
                            val monthLabel = remember(selectedMonth, config.monthFormatter, config.digitMode) {
                                config.monthFormatter.format(selectedMonth, config.digitMode)
                            }

                            val yearLabel = remember(selectedYear, config.yearFormatter, config.digitMode) {
                                config.yearFormatter.format(selectedYear, config.digitMode)
                            }

                            val selectedDate = remember(selectedYear, selectedMonth, selectedDay) {
                                selectedDay?.let { day ->
                                    runCatching {
                                        SoleimaniDate(selectedYear, selectedMonth, day)
                                    }.getOrNull()
                                }
                            }

                            val headerSubtitle = remember(
                                selectedDate, monthLabel, yearLabel, config.digitMode, strings
                            ) {
                                selectedDate?.let { date ->
                                    val dayText = when (config.digitMode) {
                                        DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(date.day))
                                        DigitMode.Latin -> addLeadingZero(date.day)
                                    }
                                    "$dayText $monthLabel $yearLabel"
                                } ?: strings.title
                            }

                            CalendarDialogHeader(
                                title = strings.title,
                                subtitle = headerSubtitle,
                                monthLabel = monthLabel,
                                yearLabel = yearLabel,
                                colors = colors,
                                hasSelection = selectedDate != null,
                            )

                            val highlightableToday = remember(config.highlightToday, constraints) {
                                if (!config.highlightToday) return@remember null
                                if (!constraints.isDateSelectable(todaySoleimani)) return@remember null
                                todaySoleimani
                            }

                            val isSelectionEnabled = remember(selectedDate, constraints) {
                                selectedDate?.let(constraints::isDateSelectable) == true
                            }

                            val effectiveYearRange = remember(
                                config.yearRange, selectedYear, todayYear, constraints
                            ) {
                                val candidates = mutableListOf(
                                    config.yearRange.first,
                                    config.yearRange.last,
                                    selectedYear,
                                    todayYear,
                                )
                                constraints.minDate?.let { candidates += it.year }
                                constraints.maxDate?.let { candidates += it.year }
                                val minYear = candidates.minOrNull() ?: selectedYear
                                val maxYear = candidates.maxOrNull() ?: selectedYear
                                minYear..maxYear
                            }

                            AnimatedVisibility(
                                visible = selectedDate != null,
                                enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                                        expandVertically(expandFrom = Alignment.Top),
                                exit = fadeOut(animationSpec = tween(durationMillis = 200)) +
                                        shrinkVertically(shrinkTowards = Alignment.Top),
                            ) {
                                Column {
                                    SelectedDatePill(
                                        formattedDate = headerSubtitle,
                                        colors = colors,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            CalendarView(
                                monthLabel = monthLabel,
                                yearLabel = yearLabel,
                                pickerTypeChang = { pickerType = it },
                                pickerType = pickerType,
                                onPreviousMonth = {
                                    if (selectedMonth == 1) {
                                        selectedMonth = 12
                                        selectedYear -= 1
                                    } else {
                                        selectedMonth -= 1
                                    }
                                },
                                onNextMonth = {
                                    if (selectedMonth == 12) {
                                        selectedMonth = 1
                                        selectedYear += 1
                                    } else {
                                        selectedMonth += 1
                                    }
                                },
                                title = strings.title,
                                subtitle = headerSubtitle,
                                strings = strings,
                                colors = colors,
                                quickActions = quickActions,
                                onQuickActionClick = quick@{ action ->
                                    when (action) {
                                        DatePickerQuickAction.Today -> {
                                            val resolvedToday =
                                                constraints.nearestValidOrNull(todaySoleimani)
                                                    ?: todaySoleimani
                                            updateSelectionFromDate(resolvedToday)
                                        }
                                        is DatePickerQuickAction.ClearSelection -> {
                                            selectedDay = null
                                            pickerType = PickerType.Day
                                        }
                                        is DatePickerQuickAction.JumpToDate -> {
                                            val target = action.targetDateProvider() ?: return@quick
                                            val resolved = constraints.nearestValidOrNull(target) ?: target
                                            updateSelectionFromDate(resolved)
                                        }
                                    }
                                },
                                layoutDirection = weekConfiguration.layoutDirection,
                            )

                            Crossfade(targetState = pickerType, label = "picker") { type ->
                                when (type) {
                                    PickerType.Day -> {
                                        // نسخه ساده‌شده بدون LocalMotionDurationScale
                                        val transitionDuration = 220
                                        val fadeDuration = 180

                                        AnimatedContent(
                                            targetState = selectedYear to selectedMonth,
                                            transitionSpec = {
                                                val direction = when {
                                                    targetState.first > initialState.first -> 1
                                                    targetState.first < initialState.first -> -1
                                                    targetState.second > initialState.second -> 1
                                                    targetState.second < initialState.second -> -1
                                                    else -> 1
                                                }
                                                (slideInVertically(
                                                    animationSpec = tween(durationMillis = transitionDuration)
                                                ) { height -> direction * (height / 5) } +
                                                        fadeIn(animationSpec = tween(durationMillis = fadeDuration))) togetherWith
                                                        (slideOutVertically(
                                                            animationSpec = tween(durationMillis = transitionDuration)
                                                        ) { height -> -direction * (height / 5) } +
                                                                fadeOut(animationSpec = tween(durationMillis = fadeDuration)))
                                            },
                                            label = "monthContent",
                                        ) { (year, month) ->
                                            val highlightForMonth = highlightableToday?.takeIf {
                                                it.month == month && it.year == year
                                            }
                                            DayOfWeekView(
                                                month = month,
                                                selectedDay = selectedDay,
                                                year = year,
                                                highlightedDate = highlightForMonth,
                                                highlightColor = colors.todayOutline,
                                                highlightFill = colors.todayButtonBackground,
                                                weekConfiguration = weekConfiguration,
                                                digitMode = config.digitMode,
                                                weekendLabelColor = colors.weekendLabelColor,
                                                eventIndicator = config.eventIndicator,
                                                onDaySelected = { day -> selectedDay = day },
                                                isDateEnabled = { constraints.isDateSelectable(it) },
                                                changeSelectedPart = {},
                                            )
                                        }
                                    }

                                    PickerType.Year -> YearsView(
                                        selectedYear = selectedYear,
                                        digitMode = config.digitMode,
                                        yearFormatter = config.yearFormatter,
                                        yearRange = effectiveYearRange,
                                        colors = colors,
                                        onYearClick = { yearValue -> selectedYear = yearValue },
                                    )

                                    PickerType.Month -> MonthView(
                                        selectedMonth = selectedMonth,
                                        displayedYear = selectedYear,
                                        digitMode = config.digitMode,
                                        monthFormatter = config.monthFormatter,
                                        colors = colors,
                                        onMonthSelected = { monthValue ->
                                            selectedMonth = monthValue
                                            pickerType = PickerType.Day
                                        },
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                thickness = 1.dp,
                                color = colors.cancelButtonContent.copy(alpha = 0.1f),
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CancelActionButton(
                                    text = strings.cancel,
                                    colors = colors,
                                    onClick = { onDismiss(true) } // ✅ onClick صریح
                                )

                                ConfirmActionButton(
                                    text = strings.confirm,
                                    enabled = isSelectionEnabled,
                                    colors = colors,
                                    onClick = { // ✅ onClick صریح
                                        val confirmed = selectedDate ?: return@ConfirmActionButton
                                        if (!constraints.isDateSelectable(confirmed)) return@ConfirmActionButton

                                        onDateSelected(confirmed)
                                        onConfirm(
                                            config.dateFormatter.format(
                                                confirmed,
                                                config.digitMode
                                            )
                                        )
                                        onDismiss(true)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDialogHeader(
    title: String,
    subtitle: String,
    monthLabel: String,
    yearLabel: String,
    colors: DatePickerColors,
    hasSelection: Boolean,
) {
    val accentMid = remember(colors.brandViolet, colors.brandTeal) { CalendarColorTokens.MidSheen }
    val headerGradient = remember(colors.brandViolet, accentMid, colors.brandTeal) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0f to colors.brandViolet,
                0.55f to accentMid,
                1f to colors.brandTeal,
            ),
            start = Offset.Zero,
            end = Offset(860f, 540f),
        )
    }
    val headerBorder = remember(colors.brandViolet, colors.brandTeal) {
        Brush.linearGradient(
            colors = listOf(
                colors.brandViolet.copy(alpha = 0.55f),
                colors.brandTeal.copy(alpha = 0.48f),
            )
        )
    }
    val iconFrame = remember(colors.brandViolet, colors.brandTeal) {
        Brush.linearGradient(
            colors = listOf(
                colors.brandViolet.copy(alpha = 0.8f),
                colors.brandTeal.copy(alpha = 0.62f),
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
            .background(headerGradient)
            .drawBehind {
                val glowRadius = size.maxDimension * 0.72f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.brandTeal.copy(alpha = 0.42f), Color.Transparent),
                        center = Offset(size.width * 0.75f, size.height * 0.08f),
                        radius = glowRadius,
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.brandViolet.copy(alpha = 0.38f), Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.92f),
                        radius = glowRadius,
                    )
                )
            }
            .padding(horizontal = 28.dp, vertical = 30.dp)
            .border(
                BorderStroke(1.dp, headerBorder),
                RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconBackground = remember(colors.brandViolet, colors.brandTeal) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.08f),
                        )
                    )
                }
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    border = BorderStroke(1.dp, iconFrame),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier
                            .background(iconBackground, RoundedCornerShape(22.dp))
                            .padding(14.dp)
                            .size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.subtitleTextColor,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.4.sp,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.titleTextColor,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                    )
                }
            }

            val emphasisPrimary = if (hasSelection) 1f else 0.65f
            val emphasisSecondary = if (hasSelection) 0.75f else 0.55f

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderGlassChip(
                    label = monthLabel,
                    colors = colors,
                    emphasis = emphasisPrimary,
                )
                HeaderGlassChip(
                    label = yearLabel,
                    colors = colors,
                    emphasis = emphasisSecondary,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun HeaderGlassChip(
    label: String,
    colors: DatePickerColors,
    emphasis: Float,
) {
    val chipShape = RoundedCornerShape(22.dp)
    val borderBrush = remember(colors.brandViolet, colors.brandTeal, emphasis) {
        Brush.linearGradient(
            colors = listOf(
                colors.brandViolet.copy(alpha = 0.6f * emphasis + 0.22f),
                colors.brandTeal.copy(alpha = 0.52f * emphasis + 0.18f),
            )
        )
    }
    val surfaceBrush = remember(colors.brandViolet, colors.brandTeal, emphasis) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f + 0.12f * emphasis),
                Color.White.copy(alpha = 0.02f + 0.06f * emphasis),
            )
        )
    }

    Surface(
        shape = chipShape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, borderBrush),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(surfaceBrush, chipShape)
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.92f),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SelectedDatePill(
    formattedDate: String,
    colors: DatePickerColors,
) {
    val chipShape = RoundedCornerShape(24.dp)
    val pillBrush = remember(colors.brandViolet, colors.brandTeal) {
        Brush.linearGradient(
            colors = listOf(
                colors.brandViolet.copy(alpha = 0.95f),
                colors.brandTeal.copy(alpha = 0.85f),
            )
        )
    }
    val pillBackground = remember(colors.todayButtonBackground) {
        Brush.linearGradient(
            colors = listOf(
                colors.todayButtonBackground.copy(alpha = 0.34f),
                colors.todayButtonBackground.copy(alpha = 0.16f),
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = chipShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, pillBrush),
    ) {
        Row(
            modifier = Modifier
                .background(pillBackground, chipShape)
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconBackground = remember(colors.brandViolet, colors.brandTeal) {
                Brush.radialGradient(
                    colors = listOf(
                        colors.brandTeal.copy(alpha = 0.4f),
                        Color.Transparent,
                    )
                )
            }
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = colors.confirmButtonBackground,
                modifier = Modifier
                    .size(26.dp)
                    .background(iconBackground, RoundedCornerShape(percent = 50))
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                color = colors.titleTextColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CalendarScreenPreview() {
    var hideDatePicker by remember { mutableStateOf(true) }
    CalendarScreen(
        onDismiss = { hideDatePicker = true },
        onConfirm = {},
        config = DatePickerConfig(
            strings = DatePickerStrings.localized().copy(title = "Test Title"),
            digitMode = DigitMode.Persian,
            showTodayAction = true,
            highlightToday = true,
        )
    )
}
