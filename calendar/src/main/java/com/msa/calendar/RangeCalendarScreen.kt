package com.msa.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.view.CalendarView
import com.msa.calendar.ui.view.DayOfWeekRangeView
import com.msa.calendar.ui.view.MonthView
import com.msa.calendar.ui.view.YearsView
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.PickerType
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.adjustDayIfOutOfBounds
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.toPersianNumber
import kotlin.math.roundToInt

@Composable
fun RangeCalendarScreen(
    onDismiss: (Boolean) -> Unit,
    setDate: (List<Map<String, String>>) -> Unit,
    modifier: Modifier = Modifier,
    initialStartDate: SoleimaniDate? = null,
    initialEndDate: SoleimaniDate? = null,
    config: DatePickerConfig = DatePickerConfig(),
    onRangeSelected: (SoleimaniDate, SoleimaniDate) -> Unit = { _, _ -> },
) {
    val todayCalendar = remember { PersionCalendar() }
    val todayDay = todayCalendar.getDay()
    val todayMonth = todayCalendar.getMonth()
    val todayYear = todayCalendar.getYear()
    val todayDate = remember { SoleimaniDate(todayYear, todayMonth, todayDay) }

    val constraints = config.constraints
    val weekConfiguration = config.weekConfiguration
    val quickActions = remember(config.quickActions, config.showTodayAction) {
        when {
            config.quickActions.isNotEmpty() -> config.quickActions
            config.showTodayAction -> listOf(DatePickerQuickAction.Today)
            else -> emptyList()
        }
    }

    val initialStart = remember(initialStartDate, constraints) {
        val desired = initialStartDate ?: todayDate
        constraints.nearestValidOrNull(desired)
            ?: constraints.minDate
            ?: constraints.maxDate
            ?: desired
    }
    val initialEnd = remember(initialEndDate, constraints) {
        initialEndDate?.let { candidate ->
            constraints.nearestValidOrNull(candidate)
        }
    }

    var pickerType: PickerType by remember { mutableStateOf(PickerType.Day) }

    var startDate by remember { mutableStateOf<SoleimaniDate?>(initialStart) }
    var endDate by remember { mutableStateOf<SoleimaniDate?>(initialEnd) }

    var visibleMonth by remember { mutableStateOf(initialStart.month.coerceIn(1, 12)) }
    var visibleYear by remember { mutableStateOf(initialStart.year) }
    var pendingDay by remember { mutableStateOf<Int?>(initialStart.day) }

    LaunchedEffect(visibleMonth, visibleYear) {
        adjustDayIfOutOfBounds(
            dayValue = pendingDay,
            month = visibleMonth,
            year = visibleYear,
        )?.let { coerced ->
            pendingDay = coerced
        }
    }

    val strings = config.strings
    val colors = config.colors
    val shape: Shape = config.containerShape

    val highlightableToday = remember(config.highlightToday, constraints) {
        if (!config.highlightToday) return@remember null
        if (!constraints.isDateSelectable(todayDate)) return@remember null
        todayDate
    }

    fun updateSelectionFromDate(target: SoleimaniDate) {
        visibleMonth = target.month.coerceIn(1, 12)
        visibleYear = target.year
        pendingDay = target.day
        pickerType = PickerType.Day
    }

    Dialog(onDismissRequest = { onDismiss(true) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            // بک‌دراپ کلیکی برای بستن
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onDismiss(true) }
            )

            Surface(
                modifier = modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .widthIn(min = 280.dp, max = 420.dp),
                shape = shape,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                shadowElevation = 24.dp,
                color = colors.containerColor,
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides weekConfiguration.layoutDirection,
                ) {
                    Column(
                        modifier = Modifier.animateContentSize()
                    ) {
                        val monthLabel = remember(visibleMonth, config.monthFormatter, config.digitMode) {
                            config.monthFormatter.format(visibleMonth, config.digitMode)
                        }
                        val yearLabel = remember(visibleYear, config.yearFormatter, config.digitMode) {
                            config.yearFormatter.format(visibleYear, config.digitMode)
                        }
                        val effectiveYearRange = remember(
                            config.yearRange,
                            visibleYear,
                            todayYear,
                            startDate,
                            endDate,
                            constraints,
                        ) {
                            val candidates = mutableListOf(
                                config.yearRange.first,
                                config.yearRange.last,
                                visibleYear,
                                todayYear,
                            )
                            startDate?.let { candidates += it.year }
                            endDate?.let { candidates += it.year }
                            constraints.minDate?.let { candidates += it.year }
                            constraints.maxDate?.let { candidates += it.year }
                            val minYear = candidates.minOrNull() ?: visibleYear
                            val maxYear = candidates.maxOrNull() ?: visibleYear
                            minYear..maxYear
                        }

                        CalendarView(
                            monthLabel = monthLabel,
                            yearLabel = yearLabel,
                            pickerTypeChang = { pickerType = it },
                            pickerType = pickerType,
                            onPreviousMonth = {
                                if (visibleMonth == 1) {
                                    visibleMonth = 12
                                    visibleYear -= 1
                                } else {
                                    visibleMonth -= 1
                                }
                            },
                            onNextMonth = {
                                if (visibleMonth == 12) {
                                    visibleMonth = 1
                                    visibleYear += 1
                                } else {
                                    visibleMonth += 1
                                }
                            },
                            title = strings.title,
                            subtitle = buildRangeSubtitle(strings, startDate, endDate, config.digitMode),
                            strings = strings,
                            colors = colors,
                            quickActions = quickActions,
                            onQuickActionClick = quick@{ action ->
                                when (action) {
                                    DatePickerQuickAction.Today -> {
                                        val resolvedToday = constraints.nearestValidOrNull(todayDate) ?: todayDate
                                        updateSelectionFromDate(resolvedToday)
                                        startDate = resolvedToday
                                        endDate = resolvedToday
                                    }
                                    is DatePickerQuickAction.ClearSelection -> {
                                        startDate = null
                                        endDate = null
                                        pendingDay = null
                                        pickerType = PickerType.Day
                                    }
                                    is DatePickerQuickAction.JumpToDate -> {
                                        val target = action.targetDateProvider() ?: return@quick
                                        val resolved = constraints.nearestValidOrNull(target) ?: target
                                        updateSelectionFromDate(resolved)
                                        startDate = resolved
                                        endDate = if (constraints.isDateSelectable(resolved)) resolved else null
                                    }
                                }
                            },
                            layoutDirection = weekConfiguration.layoutDirection,
                        )

                        Crossfade(targetState = pickerType, label = "rangePicker") { state ->
                            when (state) {
                                PickerType.Day -> {
                                    // نسخه بدون LocalMotionDurationScale
                                    val transitionDuration = 220
                                    val fadeDuration = 180

                                    AnimatedContent(
                                        targetState = visibleYear to visibleMonth,
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
                                        label = "rangeMonthContent"
                                    ) { (year, month) ->
                                        val highlightForMonth = highlightableToday?.takeIf {
                                            it.year == year && it.month == month
                                        }
                                        DayOfWeekRangeView(
                                            month = month,
                                            selectedDay = pendingDay,
                                            year = year,
                                            startDate = startDate,
                                            endDate = endDate,
                                            weekConfiguration = weekConfiguration,
                                            digitMode = config.digitMode,
                                            weekendLabelColor = colors.weekendLabelColor,
                                            highlightColor = colors.todayOutline,
                                            highlightFill = colors.todayButtonBackground,
                                            highlightedDate = highlightForMonth,
                                            eventIndicator = config.eventIndicator,
                                            onDaySelected = { pendingDay = it },
                                            setStartDate = { startDate = it },
                                            setEndDate = { endDate = it },
                                            isDateEnabled = { constraints.isDateSelectable(it) },
                                            changeSelectedPart = {}
                                        )
                                    }
                                }

                                PickerType.Year -> YearsView(
                                    selectedYear = visibleYear,
                                    digitMode = config.digitMode,
                                    yearFormatter = config.yearFormatter,
                                    yearRange = effectiveYearRange,
                                    colors = colors,
                                    onYearClick = { selected ->
                                        visibleYear = selected
                                        pickerType = PickerType.Month
                                    }
                                )

                                PickerType.Month -> MonthView(
                                    selectedMonth = visibleMonth,
                                    displayedYear = visibleYear,
                                    digitMode = config.digitMode,
                                    monthFormatter = config.monthFormatter,
                                    colors = colors,
                                    onMonthSelected = { selectedMonth ->
                                        visibleMonth = selectedMonth
                                        pickerType = PickerType.Day
                                    },
                                )
                            }
                        }

                        val isRangeSelectable = startDate != null && endDate != null &&
                                startDate?.let { constraints.isDateSelectable(it) } == true &&
                                endDate?.let { constraints.isDateSelectable(it) } == true

                        val isRangeWithinLimit = if (startDate != null && endDate != null) {
                            constraints.isRangeWithinLimit(startDate!!, endDate!!)
                        } else {
                            true
                        }
                        val isRangeComplete = isRangeSelectable && isRangeWithinLimit

                        if (!isRangeWithinLimit && constraints.maxRangeLength != null && startDate != null && endDate != null) {
                            val limitText = when (config.digitMode) {
                                DigitMode.Persian -> FormatHelper.toPersianNumber(constraints.maxRangeLength.toString())
                                DigitMode.Latin -> constraints.maxRangeLength.toString()
                            }
                            val message = strings.rangeLimitMessage.format(limitText)
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp),
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            thickness = 1.dp,
                            color = colors.cancelButtonContent.copy(alpha = 0.12f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CancelActionButton(
                                text = strings.cancel,
                                colors = colors,
                                onClick = { onDismiss(true) } // ✅ onClick مشخص
                            )

                            ConfirmActionButton(
                                text = strings.confirm,
                                enabled = isRangeComplete,
                                colors = colors,
                                onClick = { // ✅ onClick مشخص
                                    val start = startDate ?: return@ConfirmActionButton
                                    val end = endDate ?: return@ConfirmActionButton
                                    val ordered = if (start <= end) start to end else end to start
                                    if (!constraints.isDateSelectable(ordered.first) || !constraints.isDateSelectable(ordered.second)) {
                                        return@ConfirmActionButton
                                    }
                                    onRangeSelected(ordered.first, ordered.second)
                                    setDate(
                                        listOf(
                                            ordered.first.toMap(usePersianDigits = config.digitMode == DigitMode.Persian),
                                            ordered.second.toMap(usePersianDigits = config.digitMode == DigitMode.Persian)
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

@Preview(showBackground = true)
@Composable
fun RangeCalendarScreenPreview() {
    var hideDatePicker by remember { mutableStateOf(true) }
    RangeCalendarScreen(
        onDismiss = { hideDatePicker = true },
        setDate = { _ -> },
        config = DatePickerConfig(
            strings = DatePickerStrings.localized().copy(title = "Range Picker"),
            digitMode = DigitMode.Persian,
        )
    )
}

private fun buildRangeSubtitle(
    strings: DatePickerStrings,
    startDate: SoleimaniDate?,
    endDate: SoleimaniDate?,
    digitMode: DigitMode,
): String {
    val start = startDate?.format(digitMode)
    val end = endDate?.format(digitMode)
    return when {
        start == null && end == null -> strings.title
        start != null && end == null -> "${strings.rangeStartLabel}: $start"
        start == null && end != null -> "${strings.rangeEndLabel}: $end"
        start != null && end != null -> "$start - $end"
        else -> strings.title
    }
}

private fun SoleimaniDate.format(mode: DigitMode): String {
    val yearText = when (mode) {
        DigitMode.Persian -> year.toPersianNumber()
        DigitMode.Latin -> year.toString()
    }
    val monthText = when (mode) {
        DigitMode.Persian -> addLeadingZero(month).let { FormatHelper.toPersianNumber(it) }
        DigitMode.Latin -> addLeadingZero(month)
    }
    val dayText = when (mode) {
        DigitMode.Persian -> addLeadingZero(day).let { FormatHelper.toPersianNumber(it) }
        DigitMode.Latin -> addLeadingZero(day)
    }
    return "$yearText/$monthText/$dayText"
}
