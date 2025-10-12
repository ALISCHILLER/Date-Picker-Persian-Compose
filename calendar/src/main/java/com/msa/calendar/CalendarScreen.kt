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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.msa.calendar.ui.view.CalendarView
import com.msa.calendar.ui.view.DayOfWeekView
import com.msa.calendar.ui.view.MonthView
import com.msa.calendar.ui.view.YearsView
import com.msa.calendar.utils.*
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path


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
        )?.let { coerced -> selectedDay = coerced }
    }

    fun updateSelectionFromDate(target: SoleimaniDate) {
        selectedYear = target.year
        selectedMonth = target.month.coerceIn(1, 12)
        selectedDay = target.day
        pickerType = PickerType.Day
    }

    Dialog(onDismissRequest = { onDismiss(true) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Backdrop
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
                        colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset.Zero,
                    )
                }

                // Background effects
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val outline = shape.createOutline(size, layoutDirection, this)
                            val drawWithBrush = { brush: Brush ->
                                when (outline) {
                                    is Outline.Rounded -> {
                                        val roundRect = outline.roundRect
                                        drawPath(Path().apply { addRoundRect(roundRect) }, brush = brush)
                                    }
                                    else -> drawRect(brush = brush)
                                }
                            }
                            val primaryRadius = size.width * 0.95f
                            drawWithBrush(
                                Brush.radialGradient(
                                    colors = listOf(
                                        colors.brandViolet.copy(alpha = 0.38f),
                                        Color.Transparent,
                                    ),
                                    center = Offset(0f, size.height * 0.15f),
                                    radius = primaryRadius,
                                )
                            )
                            drawWithBrush(
                                Brush.radialGradient(
                                    colors = listOf(
                                        colors.brandTeal.copy(alpha = 0.3f),
                                        Color.Transparent,
                                    ),
                                    center = Offset(size.width, size.height),
                                    radius = primaryRadius * 0.9f,
                                )
                            )
                        }
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val outline = shape.createOutline(size, layoutDirection, this)
                            when (outline) {
                                is Outline.Rounded -> {
                                    val roundRect = outline.roundRect
                                    drawPath(Path().apply { addRoundRect(roundRect) }, brush = containerSheen, alpha = 1f)
                                }
                                else -> drawRect(brush = containerSheen, alpha = 1f)
                            }
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
                        Column {
                            val monthLabel = remember(selectedMonth, config.monthFormatter, config.digitMode) {
                                config.monthFormatter.format(selectedMonth, config.digitMode)
                            }
                            val yearLabel = remember(selectedYear, config.yearFormatter, config.digitMode) {
                                config.yearFormatter.format(selectedYear, config.digitMode)
                            }
                            val selectedDate = remember(selectedYear, selectedMonth, selectedDay) {
                                selectedDay?.let { day ->
                                    runCatching { SoleimaniDate(selectedYear, selectedMonth, day) }.getOrNull()
                                }
                            }

                            // برچسب انتخاب (برای ساب‌تایتل)
                            val selectionLabel: String? = remember(
                                selectedDate, monthLabel, yearLabel, config.digitMode
                            ) {
                                selectedDate?.let { date ->
                                    val dayText = when (config.digitMode) {
                                        DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(date.day))
                                        DigitMode.Latin -> addLeadingZero(date.day)
                                    }
                                    "$dayText $monthLabel $yearLabel"
                                }
                            }
                            val headerSubtitle = selectionLabel ?: strings.title

                            val highlightableToday = remember(config.highlightToday, constraints) {
                                if (!config.highlightToday) null
                                else if (!constraints.isDateSelectable(todaySoleimani)) null
                                else todaySoleimani
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

                            // تنها هدر: CalendarView با هدر کامل و کنترل‌های داخل هدر
                            CalendarView(
                                monthLabel = monthLabel,
                                yearLabel = yearLabel,
                                pickerTypeChang = { pickerType = it },
                                pickerType = pickerType,
                                onPreviousMonth = {
                                    if (selectedMonth == 1) {
                                        selectedMonth = 12
                                        selectedYear -= 1
                                    } else selectedMonth -= 1
                                },
                                onNextMonth = {
                                    if (selectedMonth == 12) {
                                        selectedMonth = 1
                                        selectedYear += 1
                                    } else selectedMonth += 1
                                },
                                title = strings.title,
                                subtitle = headerSubtitle,
                                strings = strings,
                                colors = colors,
                                quickActions = quickActions,
                                onQuickActionClick = quick@{ action ->
                                    when (action) {
                                        DatePickerQuickAction.Today -> {
                                            val resolvedToday = constraints.nearestValidOrNull(todaySoleimani)
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
                                showHeaderDetails = true,        // عنوان + زیرعنوان در خود هدر
                                showHeaderBackground = true,     // گرادیان هدر فعال
                                headerSupportingContent = {      // قرص تاریخ انتخاب‌شده، داخل هدر
                                    AnimatedVisibility(
                                        visible = selectionLabel != null,
                                        enter = fadeIn(tween(220)) + expandVertically(expandFrom = Alignment.Top),
                                        exit = fadeOut(tween(200)) + shrinkVertically(shrinkTowards = Alignment.Top),
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            SelectedDatePill(
                                                formattedDate = selectionLabel.orEmpty(),
                                                colors = colors,
                                            )
                                        }
                                    }
                                }
                            )

                            Crossfade(targetState = pickerType, label = "picker") { type ->
                                when (type) {
                                    PickerType.Day -> {
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
                                        onYearClick = { yearValue ->
                                            selectedYear = yearValue
                                            pickerType = PickerType.Month
                                        },
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
                                    onClick = { onDismiss(true) }
                                )
                                ConfirmActionButton(
                                    text = strings.confirm,
                                    enabled = isSelectionEnabled,
                                    colors = colors,
                                    onClick = {
                                        val confirmed = selectedDate ?: return@ConfirmActionButton
                                        if (!constraints.isDateSelectable(confirmed)) return@ConfirmActionButton
                                        onDateSelected(confirmed)
                                        onConfirm(config.dateFormatter.format(confirmed, config.digitMode))
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
                    colors = listOf(colors.brandTeal.copy(alpha = 0.4f), Color.Transparent)
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
