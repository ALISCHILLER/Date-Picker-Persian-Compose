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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.msa.calendar.ui.DatePickerColors
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.view.CalendarView
import com.msa.calendar.ui.view.DayOfWeekRangeView
import com.msa.calendar.ui.view.MonthView
import com.msa.calendar.ui.view.YearsView
import com.msa.calendar.utils.PersianCalendar
import com.msa.calendar.utils.PersianCalendarEngine
import com.msa.calendar.utils.PickerType
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.adjustDayIfOutOfBounds
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.utils.FormatHelper

@Composable
fun RangeCalendarScreen(
    onDismiss: (Boolean) -> Unit,
    setDate: (List<Map<String, String>>) -> Unit,
    modifier: Modifier = Modifier,
    initialStartDate: SoleimaniDate? = null,
    initialEndDate: SoleimaniDate? = null,
    config: DatePickerConfig = DatePickerConfig(),
    onRangeSelected: (SoleimaniDate, SoleimaniDate) -> Unit = { _, _ -> },
    onSelectionConfirmed: (DateRangeSelection) -> Unit = {},
    onClose: (DatePickerCloseReason) -> Unit = {},
) {
    val todayCalendar = remember { PersianCalendar() }
    val todayDay = remember { todayCalendar.getDay() }
    val todayMonth = remember { todayCalendar.getMonth() }
    val todayYear = remember { todayCalendar.getYear() }
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

    val effectiveYearRange = remember(config.yearRange, constraints) {
        effectivePickerYearRange(config.yearRange, constraints)
    }
    val initialDraft = remember(
        initialStartDate,
        initialEndDate,
        todayDate,
        constraints,
        effectiveYearRange,
    ) {
        resolveRangePickerDraft(
            initialStartDate = initialStartDate,
            initialEndDate = initialEndDate,
            todayDate = todayDate,
            constraints = constraints,
            yearRange = effectiveYearRange,
        )
    }

    var pickerType: PickerType by rememberSaveable(initialDraft, stateSaver = PickerTypeSaver) {
        mutableStateOf(PickerType.Day)
    }
    var startDate by rememberSaveable(initialDraft, stateSaver = NullableSoleimaniDateSaver) {
        mutableStateOf(initialDraft.startDate)
    }
    var endDate by rememberSaveable(initialDraft, stateSaver = NullableSoleimaniDateSaver) {
        mutableStateOf(initialDraft.endDate)
    }
    var visibleMonth by rememberSaveable(initialDraft) {
        mutableStateOf(initialDraft.visibleMonth.month)
    }
    var visibleYear by rememberSaveable(initialDraft) {
        mutableStateOf(initialDraft.visibleMonth.year)
    }
    var pendingDay by rememberSaveable(initialDraft) {
        mutableStateOf<Int?>(initialDraft.pendingDay)
    }

    LaunchedEffect(visibleMonth, visibleYear) {
        adjustDayIfOutOfBounds(
            dayValue = pendingDay,
            month = visibleMonth,
            year = visibleYear,
        )?.let { coerced ->
            pendingDay = coerced
        }
    }

    LaunchedEffect(
        constraints,
        effectiveYearRange,
        visibleYear,
        visibleMonth,
        startDate,
        endDate,
    ) {
        val boundedYear = visibleYear.coerceIn(effectiveYearRange)
        if (boundedYear != visibleYear) {
            visibleYear = boundedYear
            pendingDay = pendingDay?.coerceAtMost(
                PersianCalendarEngine.monthLength(boundedYear, visibleMonth),
            )
            return@LaunchedEffect
        }

        val start = startDate
        val end = endDate
        val startIsValid = start == null ||
            (start.year in effectiveYearRange && constraints.isDateSelectable(start))
        if (!startIsValid) {
            startDate = null
            endDate = null
            pendingDay = null
        } else if (
            start != null &&
            end != null &&
            (end.year !in effectiveYearRange || !constraints.isRangeSelectable(start, end))
        ) {
            endDate = null
            pendingDay = start.day
        }
    }

    val strings = config.strings
    val colors = config.colors
    val shape: Shape = config.containerShape

    val highlightableToday = remember(config.highlightToday, constraints) {
        if (!config.highlightToday) return@remember null
        if (todayDate.year !in effectiveYearRange) return@remember null
        if (!constraints.isDateSelectable(todayDate)) return@remember null
        todayDate
    }

    fun updateSelectionFromDate(target: SoleimaniDate) {
        visibleMonth = target.month.coerceIn(1, 12)
        visibleYear = target.year
        pendingDay = target.day
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
                        primaryAccentAlpha = 0.34f,
                        secondaryAccentAlpha = 0.32f,
                        primaryCenterYFraction = 0.12f,
                    ).testTag(DatePickerTestTags.RangeDialog),
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
                        val monthLabel = remember(visibleMonth, config.monthFormatter, config.digitMode) {
                            config.monthFormatter.format(visibleMonth, config.digitMode)
                        }
                        val yearLabel = remember(visibleYear, config.yearFormatter, config.digitMode) {
                            config.yearFormatter.format(visibleYear, config.digitMode)
                        }
                        val gregorianMonthHint = remember(
                            visibleYear,
                            visibleMonth,
                            config.digitMode,
                            config.showGregorianDateHints,
                            strings.gregorianCalendarLabel,
                        ) {
                            if (!config.showGregorianDateHints) null
                            else "${strings.gregorianCalendarLabel}: ${gregorianMonthSpanLabel(visibleYear, visibleMonth, config.digitMode)}"
                        }
                        val gregorianHintDirection = if (config.digitMode == DigitMode.Persian) {
                            LayoutDirection.Rtl
                        } else {
                            LayoutDirection.Ltr
                        }
                        CalendarView(
                            monthLabel = monthLabel,
                            yearLabel = yearLabel,
                            pickerTypeChang = { pickerType = it },
                            pickerType = pickerType,
                            onPreviousMonth = {
                                VisibleCalendarMonth(
                                    year = visibleYear,
                                    month = visibleMonth,
                                ).previousOrNull(effectiveYearRange)?.let { previous ->
                                    visibleYear = previous.year
                                    visibleMonth = previous.month
                                }
                            },
                            onNextMonth = {
                                VisibleCalendarMonth(
                                    year = visibleYear,
                                    month = visibleMonth,
                                ).nextOrNull(effectiveYearRange)?.let { next ->
                                    visibleYear = next.year
                                    visibleMonth = next.month
                                }
                            },
                            previousMonthEnabled = VisibleCalendarMonth(visibleYear, visibleMonth)
                                .canMovePrevious(effectiveYearRange),
                            nextMonthEnabled = VisibleCalendarMonth(visibleYear, visibleMonth)
                                .canMoveNext(effectiveYearRange),
                            title = strings.title,
                            subtitle = strings.title,
                            strings = strings,
                            colors = colors,
                            quickActions = if (dialogLayout.showQuickActions) quickActions else emptyList(),
                            onQuickActionClick = quick@{ action ->
                                when (action) {
                                    DatePickerQuickAction.Today -> {
                                        val resolvedToday = resolveSelectableInitialDateOrNull(
                                            initialDate = todayDate,
                                            todayDate = todayDate,
                                            constraints = constraints,
                                            yearRange = effectiveYearRange,
                                        ) ?: return@quick
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
                                        val resolved = resolveSelectableInitialDateOrNull(
                                            initialDate = target,
                                            todayDate = todayDate,
                                            constraints = constraints,
                                            yearRange = effectiveYearRange,
                                        ) ?: return@quick
                                        updateSelectionFromDate(resolved)
                                        startDate = resolved
                                        endDate = resolved
                                    }
                                }
                            },
                            layoutDirection = weekConfiguration.layoutDirection,
                            showHeaderDetails = false,
                            showHeaderBackground = true,
                            gregorianMonthLabel = gregorianMonthHint,
                            gregorianSelectionLabel = null,
                            gregorianHintDirection = gregorianHintDirection,
                            compact = dialogLayout.compactHeader,
                            minimal = dialogLayout.minimalHeader,
                            headerSupportingContent = {
                                RangeSelectionHeader(
                                    title = strings.title,
                                    startDate = startDate,
                                    endDate = endDate,
                                    strings = strings,
                                    digitMode = config.digitMode,
                                    colors = colors,
                                    layoutMode = dialogLayout.rangeHeaderMode,
                                    showGregorianHints = config.showGregorianDateHints,
                                )
                            },
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxWidth(),
                        ) {
                            Crossfade(
                                targetState = pickerType,
                                animationSpec = tween(durationMillis = config.motionSpec.resolvedModeDuration()),
                                label = "rangePicker",
                            ) { state ->
                                when (state) {
                                    PickerType.Day -> {
                                        val transitionDuration = config.motionSpec.resolvedMonthDuration()
                                        val fadeDuration = config.motionSpec.resolvedFadeDuration()

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
                                            val monthSnapshot = remember(
                                                year,
                                                month,
                                                startDate,
                                                endDate,
                                                constraints,
                                                config.eventIndicator,
                                            ) {
                                                MonthRenderSnapshot.range(
                                                    year = year,
                                                    month = month,
                                                    currentStart = startDate,
                                                    currentEnd = endDate,
                                                    constraints = constraints,
                                                    eventIndicator = config.eventIndicator,
                                                )
                                            }
                                            val eventLookup = remember(monthSnapshot) {
                                                { date: SoleimaniDate -> monthSnapshot.event(date) }
                                            }
                                            val enabledLookup = remember(monthSnapshot) {
                                                { date: SoleimaniDate -> monthSnapshot.isEnabled(date) }
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
                                                eventIndicator = eventLookup,
                                                strings = strings,
                                                onDaySelected = { pendingDay = it },
                                                setStartDate = { startDate = it },
                                                setEndDate = { endDate = it },
                                                isDateEnabled = enabledLookup,
                                                changeSelectedPart = {},
                                                compact = dialogLayout.compactCalendar,
                                                selectionStartColor = colors.brandViolet,
                                                selectionEndColor = colors.brandTeal,
                                                rangeFillColor = colors.rangeFillColor,
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
                                        selectedYear = visibleYear,
                                        digitMode = config.digitMode,
                                        yearFormatter = config.yearFormatter,
                                        yearRange = effectiveYearRange,
                                        colors = colors,
                                        title = strings.selectYear,
                                        previousPageDescription = strings.previousYearPage,
                                        nextPageDescription = strings.nextYearPage,
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
                                        title = strings.selectMonth,
                                        showGregorianHints = config.showGregorianDateHints,
                                        onMonthSelected = { selectedMonth ->
                                            visibleMonth = selectedMonth
                                            pickerType = PickerType.Day
                                        },
                                    )
                                }
                            }
                        }

                        val orderedRange = orderedRangeOrNull(startDate, endDate)
                        val isRangeWithinLimit = orderedRange?.let { (start, end) ->
                            constraints.isRangeWithinLimit(start, end)
                        } ?: true
                        val firstUnavailableDate = orderedRange?.let { (start, end) ->
                            if (isRangeWithinLimit) constraints.firstUnavailableDateInRange(start, end) else null
                        }
                        val isRangeComplete = isCompleteSelectableRange(
                            startDate = startDate,
                            endDate = endDate,
                            constraints = constraints,
                        )

                        if (!isRangeWithinLimit && constraints.maxRangeLength != null && startDate != null && endDate != null) {
                            val limitText = when (config.digitMode) {
                                DigitMode.Persian -> FormatHelper.toPersianNumber(constraints.maxRangeLength.toString())
                                DigitMode.Latin -> constraints.maxRangeLength.toString()
                            }
                            val message = strings.rangeLimitMessage.format(limitText)
                            DatePickerInlineMessage(
                                text = message,
                                colors = colors,
                                compact = dialogLayout.compactHeader,
                            )
                        } else if (firstUnavailableDate != null) {
                            DatePickerInlineMessage(
                                text = strings.rangeUnavailableMessage,
                                colors = colors,
                                compact = dialogLayout.compactHeader,
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dialogLayout.dividerTopPadding),
                            thickness = 1.dp,
                            color = colors.outlineColor
                        )

                        DatePickerActionBar(
                            cancelText = strings.cancel,
                            confirmText = strings.confirm,
                            confirmEnabled = isRangeComplete,
                            colors = colors,
                            compact = dialogLayout.compactHeader,
                            verticalPadding = dialogLayout.actionVerticalPadding,
                            onCancel = { closeDialog(DatePickerCloseReason.Canceled) },
                            onConfirm = confirm@{
                                val ordered = orderedRangeOrNull(startDate, endDate) ?: return@confirm
                                if (!constraints.isRangeSelectable(ordered.first, ordered.second)) return@confirm
                                val selection = toDateRangeSelection(
                                    firstDate = ordered.first,
                                    secondDate = ordered.second,
                                    config = config,
                                )
                                onSelectionConfirmed(selection)
                                onRangeSelected(selection.startDate, selection.endDate)
                                setDate(selection.legacyDateMaps)
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
private fun RangeSelectionHeader(
    title: String,
    startDate: SoleimaniDate?,
    endDate: SoleimaniDate?,
    strings: DatePickerStrings,
    digitMode: DigitMode,
    colors: DatePickerColors,
    layoutMode: RangeHeaderLayoutMode,
    showGregorianHints: Boolean,
) {
    val semanticLabel = buildRangeSubtitle(
        strings = strings,
        startDate = startDate,
        endDate = endDate,
        digitMode = digitMode,
    )
    val condensed = layoutMode == RangeHeaderLayoutMode.Condensed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(DatePickerTestTags.RangeHeader)
            .semantics { contentDescription = semanticLabel },
        verticalArrangement = Arrangement.spacedBy(if (condensed) 4.dp else 7.dp),
    ) {
        if (!condensed) {
            val activeStepLabel = when {
                startDate == null -> strings.rangeStartLabel
                endDate == null -> strings.rangeEndLabel
                else -> strings.selectedState
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    color = colors.subtitleTextColor.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.12f),
                    contentColor = colors.titleTextColor,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                ) {
                    Text(
                        text = activeStepLabel,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = colors.titleTextColor.copy(alpha = 0.90f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            RangeProgressTrack(
                startSelected = startDate != null,
                endSelected = endDate != null,
                colors = colors,
            )
        }

        when (layoutMode) {
            RangeHeaderLayoutMode.Condensed -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RangeEndpointCard(
                        modifier = Modifier.weight(1f),
                        label = strings.rangeStartLabel,
                        value = startDate?.format(digitMode),
                        secondaryValue = null,
                        digitMode = digitMode,
                        colors = colors,
                        compact = true,
                        active = startDate == null,
                        stepNumber = "1",
                    )
                    Text(
                        text = strings.rangeSeparator,
                        color = colors.subtitleTextColor.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    RangeEndpointCard(
                        modifier = Modifier.weight(1f),
                        label = strings.rangeEndLabel,
                        value = endDate?.format(digitMode),
                        secondaryValue = null,
                        digitMode = digitMode,
                        colors = colors,
                        compact = true,
                        active = startDate != null && endDate == null,
                        stepNumber = "2",
                    )
                }
            }

            RangeHeaderLayoutMode.Stacked -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    RangeEndpointCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = strings.rangeStartLabel,
                        value = startDate?.format(digitMode),
                        secondaryValue = startDate?.takeIf { showGregorianHints }?.let {
                            gregorianDateLabel(it, digitMode)
                        },
                        digitMode = digitMode,
                        colors = colors,
                        compact = true,
                        active = startDate == null,
                        stepNumber = "1",
                    )
                    Text(
                        text = strings.rangeSeparator,
                        color = colors.subtitleTextColor.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    RangeEndpointCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = strings.rangeEndLabel,
                        value = endDate?.format(digitMode),
                        secondaryValue = endDate?.takeIf { showGregorianHints }?.let {
                            gregorianDateLabel(it, digitMode)
                        },
                        digitMode = digitMode,
                        colors = colors,
                        compact = true,
                        active = startDate != null && endDate == null,
                        stepNumber = "2",
                    )
                }
            }

            RangeHeaderLayoutMode.Inline -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RangeEndpointCard(
                        modifier = Modifier.weight(1f),
                        label = strings.rangeStartLabel,
                        value = startDate?.format(digitMode),
                        secondaryValue = startDate?.takeIf { showGregorianHints }?.let {
                            gregorianDateLabel(it, digitMode)
                        },
                        digitMode = digitMode,
                        colors = colors,
                        compact = false,
                        active = startDate == null,
                        stepNumber = "1",
                    )
                    Text(
                        text = strings.rangeSeparator,
                        color = colors.subtitleTextColor.copy(alpha = 0.74f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    RangeEndpointCard(
                        modifier = Modifier.weight(1f),
                        label = strings.rangeEndLabel,
                        value = endDate?.format(digitMode),
                        secondaryValue = endDate?.takeIf { showGregorianHints }?.let {
                            gregorianDateLabel(it, digitMode)
                        },
                        digitMode = digitMode,
                        colors = colors,
                        compact = false,
                        active = startDate != null && endDate == null,
                        stepNumber = "2",
                    )
                }
            }
        }
    }
}

@Composable
private fun RangeProgressTrack(
    startSelected: Boolean,
    endSelected: Boolean,
    colors: DatePickerColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RangeProgressDot(
            completed = startSelected,
            active = !startSelected,
            colors = colors,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    color = if (startSelected) {
                        Color.White.copy(alpha = 0.76f)
                    } else {
                        Color.White.copy(alpha = 0.18f)
                    },
                    shape = RoundedCornerShape(50),
                ),
        )
        RangeProgressDot(
            completed = endSelected,
            active = startSelected && !endSelected,
            colors = colors,
        )
    }
}

@Composable
private fun RangeProgressDot(
    completed: Boolean,
    active: Boolean,
    colors: DatePickerColors,
) {
    val dotColor = when {
        completed -> Color.White
        active -> colors.brandTeal
        else -> Color.White.copy(alpha = 0.24f)
    }
    Box(
        modifier = Modifier
            .size(if (active) 11.dp else 9.dp)
            .background(dotColor, CircleShape),
    )
}

@Composable
private fun RangeEndpointCard(
    modifier: Modifier,
    label: String,
    value: String?,
    secondaryValue: String?,
    digitMode: DigitMode,
    colors: DatePickerColors,
    compact: Boolean,
    active: Boolean,
    stepNumber: String,
) {
    val shape = RoundedCornerShape(if (compact) 13.dp else 15.dp)
    val isFilled = value != null
    val borderColor = when {
        active -> Color.White.copy(alpha = 0.46f)
        isFilled -> Color.White.copy(alpha = 0.26f)
        else -> Color.White.copy(alpha = 0.14f)
    }
    val backgroundColor = when {
        active -> Color.White.copy(alpha = 0.17f)
        isFilled -> Color.White.copy(alpha = 0.11f)
        else -> Color.White.copy(alpha = 0.07f)
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 8.dp else 9.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = if (active || isFilled) {
                    Color.White.copy(alpha = 0.92f)
                } else {
                    Color.White.copy(alpha = 0.16f)
                },
                contentColor = if (active || isFilled) colors.brandViolet else colors.subtitleTextColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(
                    modifier = Modifier.size(if (compact) 26.dp else 28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stepNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = label,
                    color = colors.subtitleTextColor.copy(alpha = if (active) 1f else 0.82f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = value ?: "— / — / —",
                        color = if (isFilled) {
                            colors.titleTextColor
                        } else {
                            colors.subtitleTextColor.copy(alpha = 0.62f)
                        },
                        style = if (compact) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                    )
                }
                secondaryValue?.let { gregorianValue ->
                    CompositionLocalProvider(
                        LocalLayoutDirection provides if (digitMode == DigitMode.Persian) {
                            LayoutDirection.Rtl
                        } else {
                            LayoutDirection.Ltr
                        }
                    ) {
                        Text(
                            text = gregorianValue,
                            color = colors.subtitleTextColor.copy(alpha = 0.76f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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

private fun SoleimaniDate.format(mode: DigitMode): String = formatDateSlash(this, mode)
