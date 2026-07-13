package com.msa.persioncalendar.showcase

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.msa.calendar.DateRangeSelection
import com.msa.calendar.SingleDateSelection
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DateRangeFormatter
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.WeekdayFormatter
import com.msa.calendar.ui.rememberCalendarResourceProvider
import com.msa.calendar.ui.YearFormatter
import com.msa.calendar.ui.defaultDigitMode
import com.msa.calendar.ui.toWeekConfiguration
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarLocalization
import com.msa.calendar.utils.CalendarResourceProvider
import com.msa.calendar.utils.CalendarSystem
import com.msa.calendar.utils.CalendarTextRepository
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersianCalendar
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.dayOfWeek
import com.msa.calendar.utils.plusDays
import com.msa.calendar.utils.toSoleimaniDate
import com.msa.persioncalendar.R
import java.time.DayOfWeek
import java.util.LinkedHashSet
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.ui.DatePickerColors

@Composable
fun CalendarShowcaseApp(modifier: Modifier = Modifier) {
    val state = rememberCalendarShowcaseState()
    val context = LocalContext.current
    val localeConfiguration = state.localeConfiguration
    val localizedContext = remember(context, localeConfiguration) {
        CalendarLocalization.localizedContext(context, localeConfiguration)
    }
    val layoutDirection = remember(localeConfiguration) {
        if (localeConfiguration.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides layoutDirection,
    ) {
        val uiState = rememberCalendarShowcaseUiState(state = state)

        CalendarShowcaseScreen(
            modifier = modifier,
            state = state,
            uiState = uiState,
        )
    }
}

enum class LocaleOption { System, Persian, English }

data class CalendarFormatting(
    val digitMode: DigitMode,
    val monthFormatter: MonthFormatter,
    val yearFormatter: YearFormatter,
    val rangeFormatter: RangeFormatter,
)

data class CalendarShowcaseUiState(
    val today: SoleimaniDate,
    val upcomingMilestone: SoleimaniDate,
    val constraints: DatePickerConstraints,
    val weekConfiguration: WeekConfiguration,
    val dialogConfig: DatePickerConfig,
    val formatting: CalendarFormatting,
)

private data class CalendarShowcaseSnapshot(
    val showSinglePicker: Boolean,
    val showRangePicker: Boolean,
    val selectedSingleDate: SoleimaniDate?,
    val selectedRange: SoleimaniRange?,
    val lastSelectionType: SelectionType?,
    val useLatinDigits: Boolean,
    val useTransliteratedMonthLabels: Boolean,
    val showGregorianYearHint: Boolean,
    val showTodayShortcut: Boolean,
    val limitToNextMonth: Boolean,
    val blockFridays: Boolean,
    val blockThirteenth: Boolean,
    val enableClearAction: Boolean,
    val useInternationalWeek: Boolean,
    val highlightEvents: Boolean,
    val limitRangeLength: Boolean,
    val localeOption: LocaleOption,
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
    var useTransliteratedMonthLabels by mutableStateOf(false)
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

    fun onSingleSelectionConfirmed(selection: SingleDateSelection) {
        selectedSingleDate = selection.date
        lastSelectionType = SelectionType.Single
    }

    fun onRangeSelectionConfirmed(selection: DateRangeSelection) {
        selectedRange = SoleimaniRange.of(selection.startDate, selection.endDate)
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
            LocaleOption.System -> localeResolver()
            LocaleOption.Persian -> CalendarLocaleConfiguration.persian()
            LocaleOption.English -> CalendarLocaleConfiguration.english()
        }
        localeConfiguration = config
        useLatinDigits = config.defaultDigitMode() == DigitMode.Latin
    }

    private fun snapshot(): CalendarShowcaseSnapshot = CalendarShowcaseSnapshot(
        showSinglePicker = showSinglePicker,
        showRangePicker = showRangePicker,
        selectedSingleDate = selectedSingleDate,
        selectedRange = selectedRange,
        lastSelectionType = lastSelectionType,
        useLatinDigits = useLatinDigits,
        useTransliteratedMonthLabels = useTransliteratedMonthLabels,
        showGregorianYearHint = showGregorianYearHint,
        showTodayShortcut = showTodayShortcut,
        limitToNextMonth = limitToNextMonth,
        blockFridays = blockFridays,
        blockThirteenth = blockThirteenth,
        enableClearAction = enableClearAction,
        useInternationalWeek = useInternationalWeek,
        highlightEvents = highlightEvents,
        limitRangeLength = limitRangeLength,
        localeOption = localeOption,
    )

    private fun restore(snapshot: CalendarShowcaseSnapshot) {
        applyLocale(snapshot.localeOption)
        showSinglePicker = snapshot.showSinglePicker
        showRangePicker = snapshot.showRangePicker
        selectedSingleDate = snapshot.selectedSingleDate
        selectedRange = snapshot.selectedRange
        lastSelectionType = snapshot.lastSelectionType
        useLatinDigits = snapshot.useLatinDigits
        useTransliteratedMonthLabels = snapshot.useTransliteratedMonthLabels
        showGregorianYearHint = snapshot.showGregorianYearHint
        showTodayShortcut = snapshot.showTodayShortcut
        limitToNextMonth = snapshot.limitToNextMonth
        blockFridays = snapshot.blockFridays
        blockThirteenth = snapshot.blockThirteenth
        enableClearAction = snapshot.enableClearAction
        useInternationalWeek = snapshot.useInternationalWeek
        highlightEvents = snapshot.highlightEvents
        limitRangeLength = snapshot.limitRangeLength
    }

    companion object {
        private const val MissingDatePart = Int.MIN_VALUE
        private const val SavedItemCount = 24

        internal fun saver(
            today: SoleimaniDate,
            localeResolver: () -> CalendarLocaleConfiguration,
        ): Saver<CalendarShowcaseState, Any> = listSaver(
            save = { state ->
                val snapshot = state.snapshot()
                listOf(
                    snapshot.showSinglePicker,
                    snapshot.showRangePicker,
                    snapshot.selectedSingleDate?.year ?: MissingDatePart,
                    snapshot.selectedSingleDate?.month ?: MissingDatePart,
                    snapshot.selectedSingleDate?.day ?: MissingDatePart,
                    snapshot.selectedRange?.start?.year ?: MissingDatePart,
                    snapshot.selectedRange?.start?.month ?: MissingDatePart,
                    snapshot.selectedRange?.start?.day ?: MissingDatePart,
                    snapshot.selectedRange?.end?.year ?: MissingDatePart,
                    snapshot.selectedRange?.end?.month ?: MissingDatePart,
                    snapshot.selectedRange?.end?.day ?: MissingDatePart,
                    snapshot.lastSelectionType?.name.orEmpty(),
                    snapshot.useLatinDigits,
                    snapshot.useTransliteratedMonthLabels,
                    snapshot.showGregorianYearHint,
                    snapshot.showTodayShortcut,
                    snapshot.limitToNextMonth,
                    snapshot.blockFridays,
                    snapshot.blockThirteenth,
                    snapshot.enableClearAction,
                    snapshot.useInternationalWeek,
                    snapshot.highlightEvents,
                    snapshot.limitRangeLength,
                    snapshot.localeOption.name,
                )
            },
            restore = restore@{ saved ->
                if (saved.size != SavedItemCount) return@restore null

                fun booleanAt(index: Int): Boolean? = saved.getOrNull(index) as? Boolean
                fun stringAt(index: Int): String? = saved.getOrNull(index) as? String
                fun intAt(index: Int): Int? = saved.getOrNull(index) as? Int
                fun dateAt(index: Int): SoleimaniDate? {
                    val year = intAt(index) ?: return null
                    if (year == MissingDatePart) return null
                    val month = intAt(index + 1) ?: return null
                    val day = intAt(index + 2) ?: return null
                    return runCatching { SoleimaniDate(year, month, day) }.getOrNull()
                }

                val start = dateAt(5)
                val end = dateAt(8)
                val snapshot = CalendarShowcaseSnapshot(
                    showSinglePicker = booleanAt(0) ?: return@restore null,
                    showRangePicker = booleanAt(1) ?: return@restore null,
                    selectedSingleDate = dateAt(2),
                    selectedRange = if (start != null && end != null) SoleimaniRange.of(start, end) else null,
                    lastSelectionType = stringAt(11)
                        ?.takeIf(String::isNotBlank)
                        ?.let { name -> SelectionType.entries.firstOrNull { it.name == name } },
                    useLatinDigits = booleanAt(12) ?: return@restore null,
                    useTransliteratedMonthLabels = booleanAt(13) ?: return@restore null,
                    showGregorianYearHint = booleanAt(14) ?: return@restore null,
                    showTodayShortcut = booleanAt(15) ?: return@restore null,
                    limitToNextMonth = booleanAt(16) ?: return@restore null,
                    blockFridays = booleanAt(17) ?: return@restore null,
                    blockThirteenth = booleanAt(18) ?: return@restore null,
                    enableClearAction = booleanAt(19) ?: return@restore null,
                    useInternationalWeek = booleanAt(20) ?: return@restore null,
                    highlightEvents = booleanAt(21) ?: return@restore null,
                    limitRangeLength = booleanAt(22) ?: return@restore null,
                    localeOption = stringAt(23)
                        ?.let { name -> LocaleOption.entries.firstOrNull { it.name == name } }
                        ?: LocaleOption.System,
                )
                CalendarShowcaseState(today = today, localeResolver = localeResolver).apply {
                    restore(snapshot)
                }
            },
        )
    }
}

@Composable
fun rememberCalendarShowcaseUiState(
    state: CalendarShowcaseState,
): CalendarShowcaseUiState {


    val isDarkTheme = isSystemInDarkTheme()
    val resourceProvider = rememberCalendarResourceProvider(state.localeConfiguration)
    val weekConfiguration = rememberWeekConfiguration(state, resourceProvider)
    val constraints = rememberConstraints(state, weekConfiguration)
    val upcomingMilestone = rememberUpcomingMilestone(state.today)

    val milestoneLabel = stringResource(R.string.showcase_quick_action_next_milestone)
    val quickActions = rememberQuickActions(state, milestoneLabel, upcomingMilestone)

    val rangeFormatText = stringResource(R.string.showcase_summary_range_format)
    val rangeFormatter = rememberRangeFormatter(state, rangeFormatText)

    val eventIndicator = rememberEventIndicator(state)
    val digitMode = rememberDigitMode(state)
    val monthFormatter = rememberMonthFormatter(state, resourceProvider)
    val yearFormatter = rememberYearFormatter(state)
    val strings = rememberDatePickerStrings(resourceProvider)
    val colors = rememberDatePickerColors(isDarkTheme)
    val dialogConfig = rememberDialogConfig(
        state = state,
        strings = strings,
        digitMode = digitMode,
        constraints = constraints,
        weekConfiguration = weekConfiguration,
        quickActions = quickActions,
        eventIndicator = eventIndicator,
        monthFormatter = monthFormatter,
        yearFormatter = yearFormatter,
        colors = colors,
    )
    val formatting = rememberCalendarFormatting(
        digitMode = digitMode,
        monthFormatter = monthFormatter,
        yearFormatter = yearFormatter,
        rangeFormatter = rangeFormatter,
    )

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
    todayProvider: () -> SoleimaniDate = { PersianCalendar().toSoleimaniDate() },
    localeResolver: () -> CalendarLocaleConfiguration = { CalendarLocalization.inferFromSystem() },
): CalendarShowcaseState {
    val today = remember { todayProvider() }
    val saver = remember(today, localeResolver) {
        CalendarShowcaseState.saver(today = today, localeResolver = localeResolver)
    }
    return rememberSaveable(today, saver = saver) {
        CalendarShowcaseState(today = today, localeResolver = localeResolver)
    }
}

@Composable
private fun rememberWeekConfiguration(
    state: CalendarShowcaseState,
    resourceProvider: CalendarResourceProvider,
): WeekConfiguration {
    return remember(state.localeConfiguration, state.useInternationalWeek, resourceProvider) {
        if (state.useInternationalWeek) {
            WeekConfiguration(
                startDay = DayOfWeek.MONDAY,
                weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                dayLabelFormatter = WeekdayFormatter.latinShort(resourceProvider),
                layoutDirection = LayoutDirection.Ltr,
            )
        } else {
            state.localeConfiguration.toWeekConfiguration(provider = resourceProvider)
        }
    }
}
@Composable
private fun rememberConstraints(
    state: CalendarShowcaseState,
    weekConfiguration: WeekConfiguration,
): DatePickerConstraints {
    return remember(
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
}
@Composable
private fun rememberUpcomingMilestone(today: SoleimaniDate): SoleimaniDate {
    return remember(today) { calculateUpcomingMilestone(today) }
}

@Composable
private fun rememberQuickActions(
    state: CalendarShowcaseState,
    milestoneLabel: String,
    upcomingMilestone: SoleimaniDate,
): List<DatePickerQuickAction> {
    return remember(
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
}
@Composable
private fun rememberRangeFormatter(
    state: CalendarShowcaseState,
    rangeFormatText: String,
): RangeFormatter {
    return remember(rangeFormatText, state.localeConfiguration) {
        RangeFormatter { start, end ->
            String.format(state.localeConfiguration.locale, rangeFormatText, start, end)
        }
    }
}
@Composable
private fun rememberEventIndicator(state: CalendarShowcaseState): CalendarEventIndicator {
    val eventDisabledLabel = stringResource(R.string.showcase_event_disabled)
    val eventMonthStartLabel = stringResource(R.string.showcase_event_month_start)
    val eventTodayLabel = stringResource(R.string.showcase_event_today)
    return remember(
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
}

@Composable
private fun rememberDigitMode(state: CalendarShowcaseState): DigitMode {
    return remember(state.localeConfiguration, state.useLatinDigits) {
        if (state.useLatinDigits) {
            DigitMode.Latin
        } else {
            state.localeConfiguration.defaultDigitMode()
        }
    }
}
@Composable
private fun rememberMonthFormatter(
    state: CalendarShowcaseState,
    resourceProvider: CalendarResourceProvider,
): MonthFormatter {
    return remember(
        state.localeConfiguration,
        state.useTransliteratedMonthLabels,
        resourceProvider,
    ) {
        when {
            state.localeConfiguration.calendarSystem == CalendarSystem.Gregorian ->
                MonthFormatter.gregorian(resourceProvider)
            state.shouldUsePersianMonthTransliteration() ->
                MonthFormatter.persianWithLatinTransliteration(resourceProvider)
            else -> MonthFormatter.persian(resourceProvider)
        }
    }
}

internal fun CalendarShowcaseState.shouldUsePersianMonthTransliteration(): Boolean {
    val language = localeConfiguration.locale.language
    return useTransliteratedMonthLabels || !language.equals("fa", ignoreCase = true)
}
@Composable
private fun rememberYearFormatter(state: CalendarShowcaseState): YearFormatter {
    return remember(state.showGregorianYearHint, state.localeConfiguration) {
        if (state.showGregorianYearHint && state.localeConfiguration.calendarSystem == CalendarSystem.Persian) {
            YearFormatter.WithGregorianHint
        } else {
            YearFormatter.Default
        }
    }
}
@Composable
private fun rememberDatePickerStrings(
    resourceProvider: CalendarResourceProvider,
): DatePickerStrings {
    return remember(resourceProvider) { DatePickerStrings.localized(resourceProvider) }
}

@Composable
private fun rememberDatePickerColors(
    isDarkTheme: Boolean,
): DatePickerColors = remember(isDarkTheme) {
    if (isDarkTheme) DatePickerDefaults.darkColors() else DatePickerDefaults.lightColors()
}

@Composable
private fun rememberDialogConfig(
    state: CalendarShowcaseState,
    strings: DatePickerStrings,
    digitMode: DigitMode,
    constraints: DatePickerConstraints,
    weekConfiguration: WeekConfiguration,
    quickActions: List<DatePickerQuickAction>,
    eventIndicator: CalendarEventIndicator,
    monthFormatter: MonthFormatter,
    yearFormatter: YearFormatter,
    colors: DatePickerColors,
): DatePickerConfig {
    return remember(
        state.showTodayShortcut,
        strings,
        digitMode,
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
            rangeFormatter = DateRangeFormatter { start, end ->
                "$start ${strings.rangeSeparator} $end"
            },
            colors = colors,
        )
    }
}
@Composable
private fun rememberCalendarFormatting(
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    yearFormatter: YearFormatter,
    rangeFormatter: RangeFormatter,
): CalendarFormatting {
    return remember(digitMode, monthFormatter, yearFormatter, rangeFormatter) {
        CalendarFormatting(
            digitMode = digitMode,
            monthFormatter = monthFormatter,
            yearFormatter = yearFormatter,
            rangeFormatter = rangeFormatter,
        )
    }
}

private typealias CalendarEventIndicator = (SoleimaniDate) -> CalendarEvent?

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
