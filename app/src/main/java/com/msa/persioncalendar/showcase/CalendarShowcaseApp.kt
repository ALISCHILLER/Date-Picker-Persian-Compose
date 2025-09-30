package com.msa.persioncalendar.showcase

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.WeekdayFormatter
import com.msa.calendar.ui.YearFormatter
import com.msa.calendar.ui.defaultDigitMode
import com.msa.calendar.ui.toWeekConfiguration
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarLocalization
import com.msa.calendar.utils.CalendarSystem
import com.msa.calendar.utils.CalendarTextRepository
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.dayOfWeek
import com.msa.calendar.utils.plusDays
import com.msa.calendar.utils.toSoleimaniDate
import com.msa.persioncalendar.R
import java.time.DayOfWeek
import java.util.LinkedHashSet

@Composable
fun CalendarShowcaseApp(modifier: Modifier = Modifier) {
    val state = rememberCalendarShowcaseState()


    val uiState = rememberCalendarShowcaseUiState(state = state)

    CalendarShowcaseScreen(
        modifier = modifier,
        state = state,
        uiState = uiState,
    )
}

enum class LocaleOption { System, Persian, English }

@Stable
data class CalendarFormatting(
    val digitMode: DigitMode,
    val monthFormatter: MonthFormatter,
    val yearFormatter: YearFormatter,
    val rangeFormatter: RangeFormatter,
)

@Stable
data class CalendarShowcaseUiState(
    val today: SoleimaniDate,
    val upcomingMilestone: SoleimaniDate,
    val constraints: DatePickerConstraints,
    val weekConfiguration: WeekConfiguration,
    val dialogConfig: DatePickerConfig,
    val formatting: CalendarFormatting,
)

@Stable
class CalendarShowcaseState internal constructor(
    val today: SoleimaniDate,
    private val localeResolver: () -> CalendarLocaleConfiguration,
) {
    var showSinglePicker by mutableStateOf(false)
        private set
    var showRangePicker by mutableStateOf(false)
        private set
    var selectedSingleDate by mutableStateOf<SoleimaniDate?>(null)
        private set
    var selectedRange by mutableStateOf<SoleimaniRange?>(null)
        private set
    var lastSelectionType by mutableStateOf<SelectionType?>(null)
        private set

    var useLatinDigits by mutableStateOf(false)
    var useGregorianLabels by mutableStateOf(false)
    var showGregorianYearHint by mutableStateOf(false)
    var showTodayShortcut by mutableStateOf(true)
    var limitToNextMonth by mutableStateOf(false)
    var blockFridays by mutableStateOf(false)
    var blockThirteenth by mutableStateOf(false)
    var enableClearAction by mutableStateOf(true)
    var useInternationalWeek by mutableStateOf(false)
    var highlightEvents by mutableStateOf(true)
    var limitRangeLength by mutableStateOf(false)
    var localeOption by mutableStateOf(LocaleOption.System)
        private set
    var localeConfiguration by mutableStateOf(localeResolver())
        private set

    init {
        applyLocale(LocaleOption.System)
    }

    fun openSinglePicker() {
        showRangePicker = false
        showSinglePicker = true
    }

    fun openRangePicker() {
        showSinglePicker = false
        showRangePicker = true
    }

    fun dismissPickers() {
        showSinglePicker = false
        showRangePicker = false
    }

    fun onSingleDateSelected(date: SoleimaniDate) {
        selectedSingleDate = date
        lastSelectionType = SelectionType.Single
    }

    fun onRangeSelected(start: SoleimaniDate, end: SoleimaniDate) {
        selectedRange = SoleimaniRange.of(start, end)
        lastSelectionType = SelectionType.Range
    }

    fun onQuickTodaySelected(date: SoleimaniDate) {
        selectedSingleDate = date
        lastSelectionType = SelectionType.QuickToday
    }

    fun clearSelection() {
        selectedSingleDate = null
        selectedRange = null
        lastSelectionType = null
    }

    fun onLocaleOptionSelected(option: LocaleOption) {
        applyLocale(option)
    }

    private fun applyLocale(option: LocaleOption) {
        localeOption = option
        val config = when (option) {
            LocaleOption.System -> {
                CalendarLocalization.override(null)
                localeResolver()
            }
            LocaleOption.Persian -> CalendarLocaleConfiguration.persian()
            LocaleOption.English -> CalendarLocaleConfiguration.english()
        }
        localeConfiguration = config
        useLatinDigits = when (config.calendarSystem) {
            CalendarSystem.Persian -> false
            CalendarSystem.Gregorian -> true
        }
        if (option != LocaleOption.System) {
            CalendarLocalization.override(config)
        }
    }
}

@Composable
fun rememberCalendarShowcaseUiState(
    state: CalendarShowcaseState,
): CalendarShowcaseUiState {


    val isDarkTheme = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    val weekConfiguration = remember(state.localeConfiguration, state.useInternationalWeek) {
        if (state.useInternationalWeek) {
            WeekConfiguration(
                startDay = DayOfWeek.MONDAY,
                weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                dayLabelFormatter = WeekdayFormatter.LatinShort,
                layoutDirection = LayoutDirection.Ltr,
            )
        } else {
            state.localeConfiguration.toWeekConfiguration()
        }
    }

    val constraints = remember(
        state.limitToNextMonth,
        state.blockFridays,
        state.blockThirteenth,
        state.limitRangeLength,
        state.today,
        weekConfiguration,
    ) {
        buildConstraints(
            today = state.today,
            limitToNextMonth = state.limitToNextMonth,
            blockFridays = state.blockFridays,
            blockThirteenth = state.blockThirteenth,
            limitRangeLength = state.limitRangeLength,
            weekendDays = weekConfiguration.weekendDays,
        )
    }

    val upcomingMilestone = remember(state.today) {
        calculateUpcomingMilestone(state.today)
    }

    val milestoneLabel = stringResource(R.string.showcase_quick_action_next_milestone)
    val rangeFormatText = stringResource(R.string.showcase_summary_range_format)
    val quickActions = remember(
        state.showTodayShortcut,
        state.enableClearAction,
        milestoneLabel,
        upcomingMilestone,
    ) {
        buildQuickActions(
            showTodayShortcut = state.showTodayShortcut,
            enableClearAction = state.enableClearAction,
            milestoneLabel = milestoneLabel,
            upcomingMilestone = upcomingMilestone,
        )
    }

    val rangeFormatter = remember(rangeFormatText, state.localeConfiguration) {
        RangeFormatter { start, end ->
            String.format(state.localeConfiguration.locale, rangeFormatText, start, end)
        }
    }

    val eventDisabledLabel = stringResource(R.string.showcase_event_disabled)
    val eventMonthStartLabel = stringResource(R.string.showcase_event_month_start)
    val eventTodayLabel = stringResource(R.string.showcase_event_today)
    val eventIndicator = remember(
        state.highlightEvents,
        state.blockThirteenth,
        eventDisabledLabel,
        eventMonthStartLabel,
        eventTodayLabel,
        state.today,
    ) {
        buildEventIndicator(
            highlightEvents = state.highlightEvents,
            blockThirteenth = state.blockThirteenth,
            disabledLabel = eventDisabledLabel,
            monthStartLabel = eventMonthStartLabel,
            todayLabel = eventTodayLabel,
            today = state.today,
        )
    }

    val digitMode = remember(state.localeConfiguration, state.useLatinDigits) {
        if (state.useLatinDigits) {
            DigitMode.Latin
        } else {
            state.localeConfiguration.defaultDigitMode()
        }
    }

    val monthFormatter = remember(state.localeConfiguration, state.useGregorianLabels, state.useLatinDigits) {
        when {
            state.useGregorianLabels || state.localeConfiguration.calendarSystem == CalendarSystem.Gregorian ->
                MonthFormatter.Gregorian
            state.useLatinDigits -> MonthFormatter.PersianWithLatinTransliteration
            else -> MonthFormatter.Persian
        }
    }

    val yearFormatter = remember(state.showGregorianYearHint, state.localeConfiguration) {
        if (state.showGregorianYearHint && state.localeConfiguration.calendarSystem == CalendarSystem.Persian) {
            YearFormatter.WithGregorianHint
        } else {
            YearFormatter.Default
        }
    }

    val strings = remember(state.localeConfiguration) {
        DatePickerStrings.localized()
    }

    val colors = remember(isDarkTheme, colorScheme) {
        if (isDarkTheme) {
            DatePickerDefaults.darkColors(
                gradientStart = colorScheme.primary,
                gradientEnd = colorScheme.tertiary,
                containerColor = colorScheme.surface,
                titleTextColor = colorScheme.onPrimary,
                subtitleTextColor = colorScheme.onPrimary.copy(alpha = 0.88f),
                controlIconColor = colorScheme.onPrimary.copy(alpha = 0.85f),
                todayButtonBackground = colorScheme.primary.copy(alpha = 0.35f),
                todayButtonContent = colorScheme.onPrimary,
                confirmButtonBackground = colorScheme.primary,
                confirmButtonContent = colorScheme.onPrimary,
                cancelButtonContent = colorScheme.onPrimary.copy(alpha = 0.9f),
                todayOutline = colorScheme.primary.copy(alpha = 0.85f),
                weekendLabelColor = colorScheme.tertiary.copy(alpha = 0.9f),
            )
        } else {
            DatePickerDefaults.lightColors(
                gradientStart = colorScheme.primary,
                gradientEnd = colorScheme.tertiary,
                containerColor = colorScheme.surface,
                titleTextColor = colorScheme.onPrimary,
                subtitleTextColor = colorScheme.onPrimary.copy(alpha = 0.88f),
                controlIconColor = colorScheme.onPrimary.copy(alpha = 0.8f),
                todayButtonBackground = colorScheme.primary.copy(alpha = 0.22f),
                todayButtonContent = colorScheme.onPrimary,
                confirmButtonBackground = colorScheme.primary,
                confirmButtonContent = colorScheme.onPrimary,
                cancelButtonContent = colorScheme.primary.copy(alpha = 0.85f),
                todayOutline = colorScheme.primary.copy(alpha = 0.9f),
                weekendLabelColor = colorScheme.tertiary,
            )
        }
    }

    val dialogConfig = remember(
        strings,
        digitMode,
        state.showTodayShortcut,
        constraints,
        weekConfiguration,
        quickActions,
        eventIndicator,
        monthFormatter,
        yearFormatter,
        colors,
    ) {
        DatePickerConfig(
            strings = strings,
            digitMode = digitMode,
            showTodayAction = state.showTodayShortcut,
            constraints = constraints,
            weekConfiguration = weekConfiguration,
            quickActions = quickActions,
            eventIndicator = eventIndicator,
            monthFormatter = monthFormatter,
            yearFormatter = yearFormatter,
            colors = colors,
        )
    }

    val formatting = remember(digitMode, monthFormatter, yearFormatter, rangeFormatter) {
        CalendarFormatting(
            digitMode = digitMode,
            monthFormatter = monthFormatter,
            yearFormatter = yearFormatter,
            rangeFormatter = rangeFormatter,
        )
    }

    return CalendarShowcaseUiState(
        today = state.today,

        upcomingMilestone = upcomingMilestone,
        constraints = constraints,
        weekConfiguration = weekConfiguration,
        dialogConfig = dialogConfig,
        formatting = formatting,
    )
}




@Composable
fun rememberCalendarShowcaseState(
    todayProvider: () -> SoleimaniDate = { PersionCalendar().toSoleimaniDate() },
    localeResolver: () -> CalendarLocaleConfiguration = { CalendarLocalization.inferFromSystem() },
): CalendarShowcaseState {
    val today = remember { todayProvider() }
    return remember { CalendarShowcaseState(today = today, localeResolver = localeResolver) }
}

private fun buildConstraints(
    today: SoleimaniDate,
    limitToNextMonth: Boolean,
    blockFridays: Boolean,
    blockThirteenth: Boolean,
    limitRangeLength: Boolean,
    weekendDays: Set<DayOfWeek>,
): DatePickerConstraints {
    val minDate = if (limitToNextMonth) today else null
    val computedMax = today.plusDays(30) ?: today
    val maxDate = if (limitToNextMonth) computedMax else null
    val disabledDates = if (blockThirteenth) {
        generateThirteenthBlackouts(
            start = today,
            monthsAhead = if (limitToNextMonth) 3 else 12,
            minDate = minDate,
            maxDate = maxDate,
        )
    } else {
        emptySet()
    }
    val validator = if (blockFridays) {
        { date: SoleimaniDate -> date.dayOfWeek() !in weekendDays }
    } else {
        DatePickerConstraints.AlwaysValid
    }
    return DatePickerConstraints(
        minDate = minDate,
        maxDate = maxDate,
        disabledDates = disabledDates,
        dateValidator = validator,
        maxRangeLength = if (limitRangeLength) 10 else null,
    )
}

private fun buildQuickActions(
    showTodayShortcut: Boolean,
    enableClearAction: Boolean,
    milestoneLabel: String,
    upcomingMilestone: SoleimaniDate,
): List<DatePickerQuickAction> = buildList {
    if (showTodayShortcut) add(DatePickerQuickAction.Today)
    if (enableClearAction) add(DatePickerQuickAction.ClearSelection())
    add(
        DatePickerQuickAction.JumpToDate(
            actionLabel = milestoneLabel,
            targetDateProvider = { upcomingMilestone }
        )
    )
}

private fun buildEventIndicator(
    highlightEvents: Boolean,
    blockThirteenth: Boolean,
    disabledLabel: String,
    monthStartLabel: String,
    todayLabel: String,
    today: SoleimaniDate,
): (SoleimaniDate) -> CalendarEvent? {
    if (!highlightEvents) {
        return { null }
    }
    return { date ->
        when {
            blockThirteenth && date.day == 13 -> CalendarEvent(
                color = Color(0xFFEF4444),
                label = disabledLabel,
            )
            date.day == 1 -> CalendarEvent(
                color = Color(0xFF10B981),
                label = monthStartLabel,
            )
            date == today -> CalendarEvent(
                color = Color(0xFF3B82F6),
                label = todayLabel,
            )
            else -> null
        }
    }
}

private fun calculateUpcomingMilestone(today: SoleimaniDate): SoleimaniDate {
    val calendar = today.toCalendar()
    val daysRemainingInMonth = calendar.getMonthLength() - calendar.getDay() + 1
    return today.plusDays(daysRemainingInMonth)?.copy(day = 1) ?: today
}

private fun generateThirteenthBlackouts(
    start: SoleimaniDate,
    monthsAhead: Int,
    minDate: SoleimaniDate?,
    maxDate: SoleimaniDate?,
): Set<SoleimaniDate> {
    if (monthsAhead <= 0) return emptySet()
    val blockedDates = LinkedHashSet<SoleimaniDate>()
    var cursor = SoleimaniDate(start.year, start.month, 13)
    repeat(monthsAhead) {
        if ((minDate == null || cursor >= minDate) && (maxDate == null || cursor <= maxDate)) {
            blockedDates.add(cursor)
        }
        val calendar = cursor.toCalendar()
        val nextMonth = calendar.getDateByDiff(calendar.getMonthLength())
        cursor = SoleimaniDate(nextMonth.getYear(), nextMonth.getMonth(), 13)
    }
    return blockedDates
}

internal fun Int.toDigitString(digitMode: DigitMode, padWithZero: Boolean = false): String {
    val raw = if (padWithZero) addLeadingZero(this) else toString()
    return when (digitMode) {
        DigitMode.Persian -> FormatHelper.toPersianNumber(raw)
        DigitMode.Latin -> raw
    }
}

internal fun DayOfWeek.toDisplayName(useLatinDigits: Boolean): String =
    if (useLatinDigits) {
        CalendarTextRepository.latinWeekdayShort(this)
    } else {
        CalendarTextRepository.persianWeekdayShort(this)
    }

internal fun SoleimaniDate.toDisplayString(
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    yearFormatter: YearFormatter,
): String {
    val dayText = day.toDigitString(digitMode, padWithZero = true)
    val monthText = monthFormatter.format(month, digitMode)
    val yearText = yearFormatter.format(year, digitMode)
    return "$dayText $monthText $yearText"
}

data class SoleimaniRange private constructor(
    val start: SoleimaniDate,
    val end: SoleimaniDate,
) {
    companion object {
        fun of(first: SoleimaniDate, second: SoleimaniDate): SoleimaniRange {
            return if (first <= second) {
                SoleimaniRange(first, second)
            } else {
                SoleimaniRange(second, first)
            }
        }
    }
}

internal fun SoleimaniRange.toDisplayString(
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    yearFormatter: YearFormatter,
    rangeFormatter: RangeFormatter,
): String {
    val startText = start.toDisplayString(digitMode, monthFormatter, yearFormatter)
    val endText = end.toDisplayString(digitMode, monthFormatter, yearFormatter)
    return rangeFormatter.format(startText, endText)
}

fun interface RangeFormatter {
    fun format(start: String, end: String): String
}

enum class SelectionType {
    Single,
    Range,
    QuickToday
}
