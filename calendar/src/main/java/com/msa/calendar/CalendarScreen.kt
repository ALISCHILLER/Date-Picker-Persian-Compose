package com.msa.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.view.CalendarView
import com.msa.calendar.ui.view.DayOfWeekView
import com.msa.calendar.ui.view.MonthView
import com.msa.calendar.ui.view.YearsView
import com.msa.calendar.utils.PersianCalendar
import com.msa.calendar.utils.PersianCalendarEngine
import com.msa.calendar.utils.PickerType
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.adjustDayIfOutOfBounds


@Deprecated(
    message = "Use PersianDatePickerDialog for the stable typed API.",
    replaceWith = ReplaceWith("PersianDatePickerDialog(onClose, onSelectionConfirmed)"),
)
@Composable
public fun CalendarScreen(
    onDismiss: (Boolean) -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: SoleimaniDate? = null,
    config: DatePickerConfig = DatePickerConfig(),
    onDateSelected: (SoleimaniDate) -> Unit = {},
    onSelectionConfirmed: (SingleDateSelection) -> Unit = {},
    onClose: (DatePickerCloseReason) -> Unit = {},
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
    val todayCalendar = remember { PersianCalendar() }

    val todayDate = remember { todayCalendar.getDay() }
    val todayMonth = remember { todayCalendar.getMonth() }
    val todayYear = remember { todayCalendar.getYear() }
    val todaySoleimani = remember { SoleimaniDate(todayYear, todayMonth, todayDate) }
    val effectiveYearRange = remember(config.yearRange, constraints) {
        effectivePickerYearRange(config.yearRange, constraints)
    }
    val initialSelection = remember(initialDate, todaySoleimani, constraints, effectiveYearRange) {
        resolveSelectableInitialDateOrNull(
            initialDate = initialDate,
            todayDate = todaySoleimani,
            constraints = constraints,
            yearRange = effectiveYearRange,
        )
    }
    val baseDate = remember(initialDate, todaySoleimani, constraints, effectiveYearRange, initialSelection) {
        initialSelection ?: resolveSingleInitialDate(
            initialDate = initialDate,
            todayDate = todaySoleimani,
            constraints = constraints,
            yearRange = effectiveYearRange,
        )
    }

    var pickerType: PickerType by rememberSaveable(baseDate, stateSaver = PickerTypeSaver) {
        mutableStateOf(PickerType.Day)
    }
    var selectedYear by rememberSaveable(baseDate) { mutableIntStateOf(baseDate.year) }
    var selectedMonth by rememberSaveable(baseDate) { mutableIntStateOf(baseDate.month) }
    var selectedDay by rememberSaveable(baseDate) { mutableStateOf<Int?>(initialSelection?.day) }

    LaunchedEffect(selectedMonth, selectedYear) {
        adjustDayIfOutOfBounds(
            dayValue = selectedDay,
            month = selectedMonth,
            year = selectedYear,
        )?.let { coerced -> selectedDay = coerced }
    }

    LaunchedEffect(constraints, effectiveYearRange, selectedYear, selectedMonth, selectedDay) {
        val boundedYear = selectedYear.coerceIn(effectiveYearRange)
        if (boundedYear != selectedYear) {
            selectedYear = boundedYear
            selectedDay = selectedDay?.coerceAtMost(
                PersianCalendarEngine.monthLength(boundedYear, selectedMonth),
            )
            return@LaunchedEffect
        }

        val currentSelection = selectedDay?.let { day ->
            runCatching { SoleimaniDate(selectedYear, selectedMonth, day) }.getOrNull()
        }
        if (currentSelection != null && !constraints.isDateSelectable(currentSelection)) {
            selectedDay = null
        }
    }

    fun updateSelectionFromDate(target: SoleimaniDate) {
        selectedYear = target.year
        selectedMonth = target.month.coerceIn(1, 12)
        selectedDay = target.day
        pickerType = PickerType.Day
    }

    fun closeDialog(reason: DatePickerCloseReason) {
        onClose(reason)
        onDismiss(true)
    }

    Dialog(
        onDismissRequest = { closeDialog(DatePickerCloseReason.Dismissed) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.scrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { closeDialog(DatePickerCloseReason.Dismissed) }
                    .clearAndSetSemantics { }
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding(),
            ) {
                val fontScale = LocalDensity.current.fontScale
                val dialogLayout = remember(maxWidth, maxHeight, fontScale) {
                    DatePickerDialogMetrics.resolveAvailableSpace(
                        availableWidthDp = maxWidth.value,
                        availableHeightDp = maxHeight.value,
                        fontScale = fontScale,
                    )
                }

                Box(
                    modifier = modifier
                        .align(Alignment.Center)
                        .padding(
                            horizontal = dialogLayout.horizontalPadding,
                            vertical = dialogLayout.verticalPadding,
                        )
                        .width(dialogLayout.maxWidth)
                        .height(dialogLayout.maxHeight),
                ) {
                val containerBorder = remember(colors.brandViolet, colors.brandTeal) {
                    Brush.linearGradient(
                        colors = listOf(
                            colors.brandViolet.copy(alpha = 0.34f),
                            colors.brandTeal.copy(alpha = 0.34f),
                        )
                    )
                }

                Surface(
                    modifier = Modifier.datePickerDialogChrome(
                        shape = shape,
                        colors = colors,
                        title = strings.title,
                        primaryAccentAlpha = 0.38f,
                        secondaryAccentAlpha = 0.3f,
                        primaryCenterYFraction = 0.15f,
                    ).testTag(DatePickerTestTags.SingleDialog),
                    shape = shape,
                    tonalElevation = 0.dp,
                    shadowElevation = 18.dp,
                    border = BorderStroke(1.dp, containerBorder),
                    color = Color.Transparent,
                ) {
                    CompositionLocalProvider(
                        LocalLayoutDirection provides weekConfiguration.layoutDirection,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            val monthLabel = remember(selectedMonth, config.monthFormatter, config.digitMode) {
                                config.monthFormatter.format(selectedMonth, config.digitMode)
                            }
                            val yearLabel = remember(selectedYear, config.yearFormatter, config.digitMode) {
                                config.yearFormatter.format(selectedYear, config.digitMode)
                            }
                            val gregorianMonthHint = remember(
                                selectedYear,
                                selectedMonth,
                                config.digitMode,
                                config.showGregorianDateHints,
                                strings.gregorianCalendarLabel,
                            ) {
                                if (!config.showGregorianDateHints) null
                                else "${strings.gregorianCalendarLabel}: ${gregorianMonthSpanLabel(selectedYear, selectedMonth, config.digitMode)}"
                            }
                            val selectedDate = remember(selectedYear, selectedMonth, selectedDay) {
                                selectedDay?.let { day ->
                                    runCatching { SoleimaniDate(selectedYear, selectedMonth, day) }.getOrNull()
                                }
                            }

                            val selectionLabel: String? = remember(
                                selectedDate,
                                monthLabel,
                                yearLabel,
                                config.digitMode,
                            ) {
                                buildSingleSelectionLabel(
                                    date = selectedDate,
                                    monthLabel = monthLabel,
                                    yearLabel = yearLabel,
                                    digitMode = config.digitMode,
                                )
                            }
                            val headerSubtitle = selectionLabel ?: strings.title
                            val gregorianSelectionHint = remember(
                                selectedDate,
                                config.digitMode,
                                config.showGregorianDateHints,
                                strings.gregorianCalendarLabel,
                            ) {
                                if (!config.showGregorianDateHints || selectedDate == null) null
                                else "${strings.gregorianCalendarLabel}: ${gregorianDateLabel(selectedDate, config.digitMode)}"
                            }
                            val gregorianHintDirection = if (config.digitMode == DigitMode.Persian) {
                                LayoutDirection.Rtl
                            } else {
                                LayoutDirection.Ltr
                            }

                            val highlightableToday = remember(config.highlightToday, constraints) {
                                if (!config.highlightToday) null
                                else if (todaySoleimani.year !in effectiveYearRange) null
                                else if (!constraints.isDateSelectable(todaySoleimani)) null
                                else todaySoleimani
                            }

                            val isSelectionEnabled = remember(selectedDate, constraints) {
                                selectedDate?.let(constraints::isDateSelectable) == true
                            }

                            CalendarView(
                                monthLabel = monthLabel,
                                yearLabel = yearLabel,
                                pickerTypeChang = { pickerType = it },
                                pickerType = pickerType,
                                onPreviousMonth = {
                                    VisibleCalendarMonth(
                                        year = selectedYear,
                                        month = selectedMonth,
                                    ).previousOrNull(effectiveYearRange)?.let { previous ->
                                        selectedYear = previous.year
                                        selectedMonth = previous.month
                                    }
                                },
                                onNextMonth = {
                                    VisibleCalendarMonth(
                                        year = selectedYear,
                                        month = selectedMonth,
                                    ).nextOrNull(effectiveYearRange)?.let { next ->
                                        selectedYear = next.year
                                        selectedMonth = next.month
                                    }
                                },
                                previousMonthEnabled = VisibleCalendarMonth(selectedYear, selectedMonth)
                                    .canMovePrevious(effectiveYearRange),
                                nextMonthEnabled = VisibleCalendarMonth(selectedYear, selectedMonth)
                                    .canMoveNext(effectiveYearRange),
                                title = strings.title,
                                subtitle = headerSubtitle,
                                strings = strings,
                                colors = colors,
                                quickActions = if (dialogLayout.showQuickActions) quickActions else emptyList(),
                                onQuickActionClick = quick@{ action ->
                                    when (action) {
                                        DatePickerQuickAction.Today -> {
                                            val resolvedToday = resolveSelectableInitialDateOrNull(
                                                initialDate = todaySoleimani,
                                                todayDate = todaySoleimani,
                                                constraints = constraints,
                                                yearRange = effectiveYearRange,
                                            ) ?: return@quick
                                            updateSelectionFromDate(resolvedToday)
                                        }
                                        is DatePickerQuickAction.ClearSelection -> {
                                            selectedDay = null
                                            pickerType = PickerType.Day
                                        }
                                        is DatePickerQuickAction.JumpToDate -> {
                                            val target = action.targetDateProvider() ?: return@quick
                                            val resolved = resolveSelectableInitialDateOrNull(
                                                initialDate = target,
                                                todayDate = todaySoleimani,
                                                constraints = constraints,
                                                yearRange = effectiveYearRange,
                                            ) ?: return@quick
                                            updateSelectionFromDate(resolved)
                                        }
                                    }
                                },
                                layoutDirection = weekConfiguration.layoutDirection,
                                showHeaderDetails = !dialogLayout.minimalHeader,
                                showHeaderBackground = true,
                                gregorianMonthLabel = if (gregorianSelectionHint == null) gregorianMonthHint else null,
                                gregorianSelectionLabel = gregorianSelectionHint,
                                gregorianHintDirection = gregorianHintDirection,
                                compact = dialogLayout.compactHeader,
                                minimal = dialogLayout.minimalHeader,
                                headerSupportingContent = null,
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f, fill = true)
                                    .fillMaxWidth(),
                            ) {
                                Crossfade(
                                    targetState = pickerType,
                                    animationSpec = tween(durationMillis = config.motionSpec.resolvedModeDuration()),
                                    label = "picker",
                                ) { type ->
                                    when (type) {
                                        PickerType.Day -> {
                                            val transitionDuration = config.motionSpec.resolvedMonthDuration()
                                            val fadeDuration = config.motionSpec.resolvedFadeDuration()
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
                                                val monthSnapshot = remember(
                                                    year,
                                                    month,
                                                    constraints,
                                                    config.eventIndicator,
                                                ) {
                                                    MonthRenderSnapshot.single(
                                                        year = year,
                                                        month = month,
                                                        constraints = constraints,
                                                        eventIndicator = config.eventIndicator,
                                                    )
                                                }
                                                val enabledLookup = remember(monthSnapshot) {
                                                    { date: SoleimaniDate -> monthSnapshot.isEnabled(date) }
                                                }
                                                val eventLookup = remember(monthSnapshot) {
                                                    { date: SoleimaniDate -> monthSnapshot.event(date) }
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
                                                    eventIndicator = eventLookup,
                                                    strings = strings,
                                                    onDaySelected = { day -> selectedDay = day },
                                                    isDateEnabled = enabledLookup,
                                                    changeSelectedPart = {},
                                                    compact = dialogLayout.compactCalendar,
                                                    selectionStartColor = colors.brandViolet,
                                                    selectionEndColor = colors.brandTeal,
                                                    dayTextColor = colors.dayTextColor,
                                                    disabledDayTextColor = colors.disabledDayTextColor,
                                                    selectionContentColor = colors.selectionContentColor,
                                                    weekendSurfaceColor = colors.weekendSurfaceColor,
                                                    weekdayHeaderBackground = colors.surfaceVariantColor.copy(alpha = 0.72f),
                                                    weekdayHeaderOutline = colors.outlineColor,
                                                    enableHaptics = config.enableHaptics,
                                                )
                                            }
                                        }
                                        PickerType.Year -> YearsView(
                                            selectedYear = selectedYear,
                                            digitMode = config.digitMode,
                                            yearFormatter = config.yearFormatter,
                                            yearRange = effectiveYearRange,
                                            colors = colors,
                                            title = strings.selectYear,
                                            previousPageDescription = strings.previousYearPage,
                                            nextPageDescription = strings.nextYearPage,
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
                                            title = strings.selectMonth,
                                            showGregorianHints = config.showGregorianDateHints,
                                            onMonthSelected = { monthValue ->
                                                selectedMonth = monthValue
                                                pickerType = PickerType.Day
                                            },
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = dialogLayout.dividerTopPadding),
                                thickness = 1.dp,
                                color = colors.outlineColor,
                            )

                            DatePickerActionBar(
                                cancelText = strings.cancel,
                                confirmText = strings.confirm,
                                confirmEnabled = isSelectionEnabled,
                                colors = colors,
                                compact = dialogLayout.compactHeader,
                                verticalPadding = dialogLayout.actionVerticalPadding,
                                onCancel = { closeDialog(DatePickerCloseReason.Canceled) },
                                onConfirm = confirm@{
                                    val confirmed = selectedDate ?: return@confirm
                                    if (!constraints.isDateSelectable(confirmed)) return@confirm
                                    val selection = confirmed.toSingleDateSelection(config)
                                    onSelectionConfirmed(selection)
                                    onDateSelected(selection.date)
                                    onConfirm(selection.formattedDate)
                                    closeDialog(DatePickerCloseReason.Confirmed)
                                },
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
@Preview(showBackground = true)
private fun CalendarScreenPreview() {
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
