package com.msa.calendar.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.msa.calendar.R
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.PersianCalendarLimits
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.daysUntil
import com.msa.calendar.utils.minusDays
import com.msa.calendar.utils.plusDays
import com.msa.calendar.utils.CalendarResourceProvider
import com.msa.calendar.utils.CalendarResourceResolver
import com.msa.calendar.utils.CalendarTextRepository
import com.msa.calendar.utils.FormatHelper
import java.time.DayOfWeek
import kotlin.math.abs

/**
 * Controls the overall behaviour and appearance of the Persian date picker dialogs.
 */
data class DatePickerConfig(
    val strings: DatePickerStrings = DatePickerStrings.localized(),
    val colors: DatePickerColors = DatePickerDefaults.colors(),
    val digitMode: DigitMode = DigitMode.Persian,
    val highlightToday: Boolean = true,
    val showTodayAction: Boolean = true,
    val weekConfiguration: WeekConfiguration = WeekConfiguration(),
    val quickActions: List<DatePickerQuickAction> = emptyList(),
    val containerShape: Shape = DatePickerDefaults.ContainerShape,
    val dateFormatter: DateFormatter = DateFormatter.Default,
    val constraints: DatePickerConstraints = DatePickerConstraints(),
    val monthFormatter: MonthFormatter = MonthFormatter.Persian,
    val yearFormatter: YearFormatter = YearFormatter.WithGregorianHint,
    val yearRange: IntRange = 1350..1450,
    val eventIndicator: (SoleimaniDate) -> CalendarEvent? = { null },
    val rangeFormatter: DateRangeFormatter = DateRangeFormatter.Default,
    val showGregorianDateHints: Boolean = true,
    val motionSpec: DatePickerMotionSpec = DatePickerMotionSpec(),
    val enableHaptics: Boolean = true,
) {
    init {
        require(!yearRange.isEmpty()) { "yearRange must not be empty" }
        require(yearRange.first >= PersianCalendarLimits.MIN_SUPPORTED_YEAR) {
            "yearRange starts before the supported Persian calendar limit: ${PersianCalendarLimits.MIN_SUPPORTED_YEAR}"
        }
        require(yearRange.last <= PersianCalendarLimits.MAX_SUPPORTED_YEAR) {
            "yearRange ends after the supported Persian calendar limit: ${PersianCalendarLimits.MAX_SUPPORTED_YEAR}"
        }
    }
}

/**
 * Screen-level motion only. Day cells intentionally remain animation-free so changing a month or
 * extending a range does not create dozens of concurrent animations.
 */
@Immutable
data class DatePickerMotionSpec(
    val enabled: Boolean = true,
    val modeTransitionMillis: Int = 120,
    val monthTransitionMillis: Int = 170,
    val fadeMillis: Int = 120,
) {
    init {
        require(modeTransitionMillis >= 0) { "modeTransitionMillis must be >= 0" }
        require(monthTransitionMillis >= 0) { "monthTransitionMillis must be >= 0" }
        require(fadeMillis >= 0) { "fadeMillis must be >= 0" }
    }

    internal fun resolvedModeDuration(): Int = if (enabled) modeTransitionMillis else 0
    internal fun resolvedMonthDuration(): Int = if (enabled) monthTransitionMillis else 0
    internal fun resolvedFadeDuration(): Int = if (enabled) fadeMillis else 0
}

/**
 * Allows customising the textual content of the dialog so it can easily be localised.
 */
@Immutable
data class DatePickerStrings(
    val title: String,
    val confirm: String,
    val cancel: String,
    val today: String,
    val clearSelection: String,
    val rangeStartLabel: String,
    val rangeEndLabel: String,
    val rangeLimitMessage: String,
    val rangeSeparator: String = "to",
    val selectMonth: String = "Select month",
    val selectYear: String = "Select year",
    val previousMonth: String = "Previous month",
    val nextMonth: String = "Next month",
    val previousYearPage: String = "Previous years",
    val nextYearPage: String = "Next years",
    val emptyDay: String = "Empty day",
    val selectedState: String = "Selected",
    val rangeStartState: String = "Range start",
    val rangeEndState: String = "Range end",
    val insideSelectedRangeState: String = "In selected range",
    val todayState: String = "Today",
    val disabledState: String = "Disabled",
    val availableState: String = "Available",
    val eventPrefix: String = "Event",
    val rangeUnavailableMessage: String = "The selected range contains unavailable dates.",
    val gregorianCalendarLabel: String = "Gregorian",
) {
    companion object {
        fun localized(): DatePickerStrings = localized(CalendarResourceResolver.provider())

        fun localized(provider: CalendarResourceProvider): DatePickerStrings = DatePickerStrings(
            title = provider.string(
                R.string.calendar_picker_title,
                fallback = "Select date",
            ),
            confirm = provider.string(
                R.string.calendar_picker_confirm,
                fallback = "Confirm",
            ),
            cancel = provider.string(
                R.string.calendar_picker_cancel,
                fallback = "Cancel",
            ),
            today = provider.string(
                R.string.calendar_picker_today,
                fallback = "Today",
            ),
            clearSelection = provider.string(
                R.string.calendar_picker_clear,
                fallback = "Clear selection",
            ),
            rangeStartLabel = provider.string(
                R.string.calendar_picker_range_start,
                fallback = "Start date",
            ),
            rangeEndLabel = provider.string(
                R.string.calendar_picker_range_end,
                fallback = "End date",
            ),
            rangeLimitMessage = provider.string(
                R.string.calendar_picker_range_limit,
                fallback = "Maximum range is %1\$s days.",
            ),
            rangeSeparator = provider.string(
                R.string.calendar_picker_range_separator,
                fallback = "to",
            ),
            selectMonth = provider.string(
                R.string.calendar_picker_select_month,
                fallback = "Select month",
            ),
            selectYear = provider.string(
                R.string.calendar_picker_select_year,
                fallback = "Select year",
            ),
            previousMonth = provider.string(
                R.string.calendar_picker_previous_month,
                fallback = "Previous month",
            ),
            nextMonth = provider.string(
                R.string.calendar_picker_next_month,
                fallback = "Next month",
            ),
            previousYearPage = provider.string(
                R.string.calendar_picker_previous_year_page,
                fallback = "Previous years",
            ),
            nextYearPage = provider.string(
                R.string.calendar_picker_next_year_page,
                fallback = "Next years",
            ),
            emptyDay = provider.string(
                R.string.calendar_picker_empty_day,
                fallback = "Empty day",
            ),
            selectedState = provider.string(
                R.string.calendar_picker_state_selected,
                fallback = "Selected",
            ),
            rangeStartState = provider.string(
                R.string.calendar_picker_state_range_start,
                fallback = "Range start",
            ),
            rangeEndState = provider.string(
                R.string.calendar_picker_state_range_end,
                fallback = "Range end",
            ),
            insideSelectedRangeState = provider.string(
                R.string.calendar_picker_state_inside_range,
                fallback = "In selected range",
            ),
            todayState = provider.string(
                R.string.calendar_picker_state_today,
                fallback = "Today",
            ),
            disabledState = provider.string(
                R.string.calendar_picker_state_disabled,
                fallback = "Disabled",
            ),
            availableState = provider.string(
                R.string.calendar_picker_state_available,
                fallback = "Available",
            ),
            eventPrefix = provider.string(
                R.string.calendar_picker_state_event_prefix,
                fallback = "Event",
            ),
            rangeUnavailableMessage = provider.string(
                R.string.calendar_picker_range_unavailable,
                fallback = "The selected range contains unavailable dates.",
            ),
            gregorianCalendarLabel = provider.string(
                R.string.calendar_picker_gregorian_label,
                fallback = "Gregorian",
            ),
        )
    }
}

/**
 * Represents the colour palette that is used across the date picker dialog.
 */
@Immutable
data class DatePickerColors(
    val brandViolet: Color,
    val brandTeal: Color,
    val containerColor: Color,
    val titleTextColor: Color,
    val subtitleTextColor: Color,
    val controlIconColor: Color,
    val todayButtonBackground: Color,
    val todayButtonContent: Color,
    val confirmButtonBackground: Color,
    val confirmButtonContent: Color,
    val cancelButtonContent: Color,
    val todayOutline: Color,
    val weekendLabelColor: Color,
    /** Main calendar surface. Defaults preserve source compatibility for custom palettes. */
    val surfaceColor: Color = containerColor,
    /** Elevated controls and inactive calendar tiles. */
    val surfaceVariantColor: Color = brandViolet.copy(alpha = 0.06f),
    /** Subtle dividers and control outlines. */
    val outlineColor: Color = brandViolet.copy(alpha = 0.16f),
    /** Primary day label colour. */
    val dayTextColor: Color = todayButtonContent,
    /** Disabled day label colour. */
    val disabledDayTextColor: Color = todayButtonContent.copy(alpha = 0.34f),
    /** Connected fill behind dates inside a selected range. */
    val rangeFillColor: Color = brandTeal.copy(alpha = 0.14f),
    /** Scrim behind the dialog. */
    val scrimColor: Color = Color(0xFF020617).copy(alpha = 0.56f),
    /** Content drawn on selected dates and primary actions. */
    val selectionContentColor: Color = Color.White,
    /** Subtle surface used behind weekend dates. */
    val weekendSurfaceColor: Color = weekendLabelColor.copy(alpha = 0.055f),
    /** Inline validation banner background. */
    val errorContainerColor: Color = Color(0xFFFEF2F2),
    /** Inline validation banner content. */
    val errorContentColor: Color = Color(0xFFB91C1C),
    /** Soft highlight behind the current month/year choice. */
    val currentChoiceColor: Color = todayButtonBackground.copy(alpha = 0.68f),
)

/**
 * Controls whether a date range validates only its endpoints or every date inside the range.
 */
enum class RangeValidationMode {
    /** Backward-compatible mode: only the start and end dates must be selectable. */
    EndpointsOnly,

    /** Production-safe mode: every date from start through end must be selectable. */
    EntireRange,
}

/**
 * Defines rules that limit which dates can be picked by the user.
 */
data class DatePickerConstraints(
    val minDate: SoleimaniDate? = null,
    val maxDate: SoleimaniDate? = null,
    val disabledDates: Set<SoleimaniDate> = emptySet(),
    val dateValidator: (SoleimaniDate) -> Boolean = AlwaysValid,
    val maxRangeLength: Int? = null,
    val rangeValidationMode: RangeValidationMode = RangeValidationMode.EntireRange,
) {
    init {
        if (minDate != null && maxDate != null) {
            require(minDate <= maxDate) { "minDate must be before or equal to maxDate" }
        }
        if (maxRangeLength != null) {
            require(maxRangeLength > 0) { "maxRangeLength must be greater than zero" }
        }
    }

    fun isDateSelectable(date: SoleimaniDate): Boolean {
        if (minDate != null && date < minDate) return false
        if (maxDate != null && date > maxDate) return false
        if (date in disabledDates) return false
        return dateValidator(date)
    }

    fun clamp(date: SoleimaniDate): SoleimaniDate {
        val minClamped = minDate?.let { if (date < it) it else date } ?: date
        return maxDate?.let { if (minClamped > it) it else minClamped } ?: minClamped
    }

    fun isRangeWithinLimit(start: SoleimaniDate, end: SoleimaniDate): Boolean {
        val limit = maxRangeLength ?: return true
        val distance = abs(start.daysUntil(end)) + 1
        return distance <= limit
    }

    /** Returns the first unavailable date in an otherwise ordered range, or `null`. */
    fun firstUnavailableDateInRange(
        start: SoleimaniDate,
        end: SoleimaniDate,
    ): SoleimaniDate? {
        val orderedStart = minOf(start, end)
        val orderedEnd = maxOf(start, end)

        if (!isDateSelectable(orderedStart)) return orderedStart
        if (!isDateSelectable(orderedEnd)) return orderedEnd
        if (rangeValidationMode == RangeValidationMode.EndpointsOnly || orderedStart == orderedEnd) {
            return null
        }

        if (dateValidator === AlwaysValid) {
            return disabledDates
                .asSequence()
                .filter { it > orderedStart && it < orderedEnd }
                .minOrNull()
        }

        var current = orderedStart.plusDays(1)
        while (current != null && current < orderedEnd) {
            if (!isDateSelectable(current)) return current
            current = current.plusDays(1)
        }
        return null
    }

    /** True when both endpoints, length and (by default) every intermediate date are valid. */
    fun isRangeSelectable(start: SoleimaniDate, end: SoleimaniDate): Boolean {
        if (!isRangeWithinLimit(start, end)) return false
        return firstUnavailableDateInRange(start, end) == null
    }

    fun nearestValidOrNull(anchor: SoleimaniDate): SoleimaniDate? {
        val clamped = clamp(anchor)
        if (isDateSelectable(clamped)) return clamped

        var forward: SoleimaniDate? = clamped
        var backward: SoleimaniDate? = clamped
        val maxIterations = 4000 // Bounded to avoid blocking the UI for pathological validators.
        repeat(maxIterations) {
            val nextForward = forward?.plusDays(1)
            if (nextForward != null && (maxDate == null || nextForward <= maxDate)) {
                if (isDateSelectable(nextForward)) return nextForward
                forward = nextForward
            } else {
                forward = null
            }

            val nextBackward = backward?.minusDays(1)
            if (nextBackward != null && (minDate == null || nextBackward >= minDate)) {
                if (isDateSelectable(nextBackward)) return nextBackward
                backward = nextBackward
            } else {
                backward = null
            }

            if (forward == null && backward == null) return null
        }
        return null
    }

    companion object {
        val AlwaysValid: (SoleimaniDate) -> Boolean = { true }
    }
}

/**
 * Decides which digit set should be used for textual results.
 */
enum class DigitMode {
    Persian,
    Latin
}

@Immutable
class MonthFormatter(
    private val provider: (DigitMode) -> List<String>,
) {
    fun format(month: Int, digitMode: DigitMode): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        val labels = provider(digitMode)
        require(labels.size == 12) { "Month label provider must return 12 entries" }
        return labels[month - 1]
    }

    fun labels(digitMode: DigitMode): List<String> {
        val labels = provider(digitMode)
        require(labels.size == 12) { "Month label provider must return 12 entries" }
        return labels
    }

    companion object {
        val Persian = persian()

        val PersianWithLatinTransliteration = persianWithLatinTransliteration()

        val Gregorian = gregorian()

        fun persian(provider: CalendarResourceProvider? = null): MonthFormatter = MonthFormatter {
            CalendarTextRepository.persianMonthNames(provider)
        }

        fun persianWithLatinTransliteration(
            provider: CalendarResourceProvider? = null,
        ): MonthFormatter {
            return MonthFormatter { digitMode ->
                when (digitMode) {
                    DigitMode.Persian -> CalendarTextRepository.persianMonthNames(provider)
                    DigitMode.Latin -> CalendarTextRepository.persianMonthLatinNames(provider)
                }
            }
        }

        fun gregorian(provider: CalendarResourceProvider? = null): MonthFormatter {
            return MonthFormatter { digitMode ->
                when (digitMode) {
                    DigitMode.Persian -> CalendarTextRepository.gregorianMonthNamesFa(provider)
                    DigitMode.Latin -> CalendarTextRepository.gregorianMonthNamesEn(provider)
                }
            }
        }
    }
}

@Immutable
class YearFormatter(
    private val formatter: (Int, DigitMode) -> String,
) {
    fun format(year: Int, digitMode: DigitMode): String = formatter(year, digitMode)

    companion object {
        val Default = YearFormatter { year, mode -> year.toDigits(mode) }

        val WithGregorianHint = YearFormatter { year, mode ->
            val primary = year.toDigits(mode)
            val gregorianYear = year + 621
            val secondary = when (mode) {
                DigitMode.Persian -> FormatHelper.toPersianNumber(gregorianYear.toString())
                DigitMode.Latin -> gregorianYear.toString()
            }
            "$primary ($secondary)"
        }
    }
}

/**
 * Encapsulates the logic that produces the final string passed to the consumer when a date is confirmed.
 */
@Immutable
class DateFormatter(
    private val formatter: (SoleimaniDate, DigitMode) -> String,
) {
    fun format(date: SoleimaniDate, digitMode: DigitMode): String = formatter(date, digitMode)

    companion object {
        /** Formats the date as `YYYY / MM / DD` using the selected [DigitMode]. */
        val Default = DateFormatter { date, digitMode ->
            val year = date.year.toDigits(digitMode)
            val month = addLeadingZero(date.month).toDigits(digitMode)
            val day = addLeadingZero(date.day).toDigits(digitMode)
            "$year / $month / $day"
        }
    }
}


/**
 * Formats the visible string returned by range selections after each endpoint has already been
 * formatted with [DateFormatter].
 */
@Immutable
class DateRangeFormatter(
    private val formatter: (String, String) -> String,
) {
    fun format(start: String, end: String): String = formatter(start, end)

    companion object {
        val Default = DateRangeFormatter { start, end -> "$start - $end" }
    }
}

@Immutable
data class CalendarEvent(
    val color: Color,
    val label: String? = null,
)

/**
 * Week configuration and weekday text formatting
 */
@Immutable
data class WeekConfiguration(
    val startDay: DayOfWeek = DayOfWeek.SATURDAY,
    val weekendDays: Set<DayOfWeek> = setOf(DayOfWeek.FRIDAY),
    val dayLabelFormatter: WeekdayFormatter = WeekdayFormatter.PersianShort,
    val layoutDirection: LayoutDirection = LayoutDirection.Rtl,
) {
    init {
        require(weekendDays.isNotEmpty()) { "weekendDays must contain at least one day" }
    }

    private val orderedDaysCache: List<DayOfWeek> = List(7) { startDay.shift(it) }
    private val weekendIndexCache: Set<Int> = weekendDays.mapTo(mutableSetOf()) { indexOf(it) }

    val orderedDays: List<DayOfWeek>
        get() = orderedDaysCache

    fun indexOf(day: DayOfWeek): Int = day.indexRelativeTo(startDay)

    fun dayAt(index: Int): DayOfWeek = orderedDaysCache[floorModInt(index, 7)]

    fun isWeekend(day: DayOfWeek): Boolean = day in weekendDays

    fun isWeekendIndex(index: Int): Boolean = floorModInt(index, 7) in weekendIndexCache

    companion object {
        fun persian(): WeekConfiguration = WeekConfiguration()

        fun gregorian(): WeekConfiguration = WeekConfiguration(
            startDay = DayOfWeek.MONDAY,
            weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            dayLabelFormatter = WeekdayFormatter.PersianGregorian,
            layoutDirection = LayoutDirection.Ltr,
        )
    }
}

@Immutable
class WeekdayFormatter(
    private val formatter: (DayOfWeek) -> String,
) {
    fun format(day: DayOfWeek): String = formatter(day)

    companion object {
        val PersianShort = persianShort()

        val LatinShort = latinShort()

        val PersianGregorian = persianGregorian()

        fun persianShort(
            provider: CalendarResourceProvider? = null,
        ): WeekdayFormatter {
            return WeekdayFormatter { day ->
                CalendarTextRepository.persianWeekdayShort(day, provider)
            }
        }

        fun latinShort(
            provider: CalendarResourceProvider? = null,
        ): WeekdayFormatter {
            return WeekdayFormatter { day ->
                CalendarTextRepository.latinWeekdayShort(day, provider)
            }
        }

        fun persianGregorian(
            provider: CalendarResourceProvider? = null,
        ): WeekdayFormatter {
            return WeekdayFormatter { day ->
                CalendarTextRepository.gregorianWeekdayShortFa(day, provider)
            }
        }
    }
}

@Immutable
sealed interface DatePickerQuickAction {
    fun label(strings: DatePickerStrings): String

    object Today : DatePickerQuickAction {
        override fun label(strings: DatePickerStrings): String = strings.today
    }

    data class ClearSelection(private val customLabel: String? = null) : DatePickerQuickAction {
        override fun label(strings: DatePickerStrings): String = customLabel ?: strings.clearSelection
    }

    data class JumpToDate(
        private val actionLabel: String,
        val targetDateProvider: () -> SoleimaniDate?,
    ) : DatePickerQuickAction {
        override fun label(strings: DatePickerStrings): String = actionLabel
    }
}

object DatePickerDefaults {
    val ContainerShape: Shape = RoundedCornerShape(32.dp)

    fun colors(
        brandViolet: Color = CalendarColorTokens.Violet,
        brandTeal: Color = CalendarColorTokens.Teal,
        containerColor: Color = CalendarColorTokens.BaseLight,
        titleTextColor: Color = Color.White.copy(alpha = 0.95f),
        subtitleTextColor: Color = Color.White.copy(alpha = 0.92f),
        controlIconColor: Color = Color.White.copy(alpha = 0.94f),
        todayButtonBackground: Color = CalendarColorTokens.Teal.copy(alpha = 0.24f),
        todayButtonContent: Color = CalendarColorTokens.TextPrimary,
        confirmButtonBackground: Color = CalendarColorTokens.Violet.copy(alpha = 0.95f),
        confirmButtonContent: Color = Color.White,
        cancelButtonContent: Color = CalendarColorTokens.Violet.copy(alpha = 0.90f),
        todayOutline: Color = CalendarColorTokens.Violet.copy(alpha = 0.78f),
        weekendLabelColor: Color = CalendarColorTokens.Weekend,
        surfaceColor: Color = containerColor,
        surfaceVariantColor: Color = brandViolet.copy(alpha = 0.06f),
        outlineColor: Color = brandViolet.copy(alpha = 0.16f),
        dayTextColor: Color = todayButtonContent,
        disabledDayTextColor: Color = todayButtonContent.copy(alpha = 0.34f),
        rangeFillColor: Color = brandTeal.copy(alpha = 0.14f),
        scrimColor: Color = Color(0xFF020617).copy(alpha = 0.56f),
        selectionContentColor: Color = Color.White,
        weekendSurfaceColor: Color = weekendLabelColor.copy(alpha = 0.055f),
        errorContainerColor: Color = Color(0xFFFEF2F2),
        errorContentColor: Color = Color(0xFFB91C1C),
        currentChoiceColor: Color = todayButtonBackground.copy(alpha = 0.68f),
    ): DatePickerColors = DatePickerColors(
        brandViolet = brandViolet,
        brandTeal = brandTeal,
        containerColor = containerColor,
        titleTextColor = titleTextColor,
        subtitleTextColor = subtitleTextColor,
        controlIconColor = controlIconColor,
        todayButtonBackground = todayButtonBackground,
        todayButtonContent = todayButtonContent,
        confirmButtonBackground = confirmButtonBackground,
        confirmButtonContent = confirmButtonContent,
        cancelButtonContent = cancelButtonContent,
        todayOutline = todayOutline,
        weekendLabelColor = weekendLabelColor,
        surfaceColor = surfaceColor,
        surfaceVariantColor = surfaceVariantColor,
        outlineColor = outlineColor,
        dayTextColor = dayTextColor,
        disabledDayTextColor = disabledDayTextColor,
        rangeFillColor = rangeFillColor,
        scrimColor = scrimColor,
        selectionContentColor = selectionContentColor,
        weekendSurfaceColor = weekendSurfaceColor,
        errorContainerColor = errorContainerColor,
        errorContentColor = errorContentColor,
        currentChoiceColor = currentChoiceColor,
    )

    fun colors(provider: CalendarResourceProvider): DatePickerColors = colors(
        brandViolet = provider.color(R.color.ocean_blue, 0xFF3F5BF6),
        brandTeal = provider.color(R.color.sky_teal, 0xFF0EA5A4),
        containerColor = provider.color(R.color.cloud, 0xFFF7F9FC),
        titleTextColor = Color.White.copy(alpha = 0.95f),
        subtitleTextColor = Color.White.copy(alpha = 0.92f),
        controlIconColor = Color.White.copy(alpha = 0.94f),
        todayButtonBackground = provider.color(R.color.sky_teal, 0xFF0EA5A4).copy(alpha = 0.24f),
        todayButtonContent = provider.color(R.color.slate, 0xFF172033),
        confirmButtonBackground = provider.color(R.color.ocean_blue, 0xFF3F5BF6).copy(alpha = 0.95f),
        confirmButtonContent = Color.White,
        cancelButtonContent = provider.color(R.color.ocean_blue, 0xFF3F5BF6).copy(alpha = 0.90f),
        todayOutline = provider.color(R.color.ocean_blue, 0xFF3F5BF6).copy(alpha = 0.78f),
        weekendLabelColor = provider.color(R.color.sunset_orange, 0xFFE5484D),
    )

    fun lightColors(): DatePickerColors = colors(
        containerColor = CalendarColorTokens.BaseLight,
        todayButtonBackground = CalendarColorTokens.Teal.copy(alpha = 0.24f),
        todayButtonContent = CalendarColorTokens.TextPrimary,
        cancelButtonContent = CalendarColorTokens.Violet.copy(alpha = 0.90f),
        todayOutline = CalendarColorTokens.Violet.copy(alpha = 0.78f),
        weekendLabelColor = CalendarColorTokens.Weekend,
        surfaceColor = Color.White,
        surfaceVariantColor = Color(0xFFF1F5F9),
        outlineColor = Color(0xFFE2E8F0),
        dayTextColor = CalendarColorTokens.TextPrimary,
        disabledDayTextColor = CalendarColorTokens.TextMuted.copy(alpha = 0.52f),
        rangeFillColor = CalendarColorTokens.Teal.copy(alpha = 0.15f),
        selectionContentColor = Color.White,
        weekendSurfaceColor = CalendarColorTokens.Weekend.copy(alpha = 0.05f),
        errorContainerColor = Color(0xFFFFF1F2),
        errorContentColor = Color(0xFFBE123C),
        currentChoiceColor = CalendarColorTokens.Violet.copy(alpha = 0.10f),
    )

    fun darkColors(): DatePickerColors = colors(
        containerColor = CalendarColorTokens.BaseDark,
        titleTextColor = Color.White.copy(alpha = 0.95f),
        subtitleTextColor = Color.White.copy(alpha = 0.92f),
        controlIconColor = Color.White.copy(alpha = 0.94f),
        todayButtonBackground = CalendarColorTokens.Teal.copy(alpha = 0.32f),
        todayButtonContent = Color.White.copy(alpha = 0.94f),
        cancelButtonContent = CalendarColorTokens.Teal.copy(alpha = 0.92f),
        todayOutline = CalendarColorTokens.Violet.copy(alpha = 0.82f),
        weekendLabelColor = CalendarColorTokens.Weekend,
        surfaceColor = Color(0xFF111827),
        surfaceVariantColor = Color(0xFF1E293B),
        outlineColor = Color(0xFF334155),
        dayTextColor = Color(0xFFE2E8F0),
        disabledDayTextColor = Color(0xFF667085),
        rangeFillColor = CalendarColorTokens.Teal.copy(alpha = 0.20f),
        scrimColor = Color(0xFF020617).copy(alpha = 0.74f),
        selectionContentColor = Color.White,
        weekendSurfaceColor = CalendarColorTokens.Weekend.copy(alpha = 0.09f),
        errorContainerColor = Color(0xFF3F1722),
        errorContentColor = Color(0xFFFFB4C2),
        currentChoiceColor = CalendarColorTokens.Teal.copy(alpha = 0.14f),
    )
}

/* ---------------------- Local helpers (no external deps) ---------------------- */

private fun Int.toDigits(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> FormatHelper.toPersianNumber(toString())
    DigitMode.Latin -> toString()
}

private fun String.toDigits(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> FormatHelper.toPersianNumber(this)
    DigitMode.Latin -> this
}

/** Safe floor-mod for Int (works for negative indices too). */
private fun floorModInt(a: Int, b: Int): Int = ((a % b) + b) % b

/** Relative index of [this] with respect to [start], in range 0..6 */
private fun DayOfWeek.indexRelativeTo(start: DayOfWeek): Int =
    floorModInt(this.value - start.value, 7)

/** Shift [this] forward by [days] days (can be negative), wrapping inside 7-day cycle. */
internal fun DayOfWeek.shift(days: Int): DayOfWeek {
    val normalized = floorModInt((value - 1) + days, 7)
    return DayOfWeek.of(normalized + 1)
}
