package com.msa.calendar.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.msa.calendar.R
import com.msa.calendar.ui.theme.CalendarColorTokens
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.daysUntil
import com.msa.calendar.utils.minusDays
import com.msa.calendar.utils.plusDays
import com.msa.calendar.utils.CalendarResourceResolver
import com.msa.calendar.utils.CalendarTextRepository
import com.msa.calendar.utils.FormatHelper
import java.time.DayOfWeek
import kotlin.math.abs

/**
 * Controls the overall behaviour and appearance of the Persian date picker dialogs.
 */
@Stable
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
)

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
) {
    companion object {
        fun localized(): DatePickerStrings = DatePickerStrings(
            title = CalendarResourceResolver.string(
                R.string.calendar_picker_title,
                fallback = "Select date",
            ),
            confirm = CalendarResourceResolver.string(
                R.string.calendar_picker_confirm,
                fallback = "Confirm",
            ),
            cancel = CalendarResourceResolver.string(
                R.string.calendar_picker_cancel,
                fallback = "Cancel",
            ),
            today = CalendarResourceResolver.string(
                R.string.calendar_picker_today,
                fallback = "Today",
            ),
            clearSelection = CalendarResourceResolver.string(
                R.string.calendar_picker_clear,
                fallback = "Clear selection",
            ),
            rangeStartLabel = CalendarResourceResolver.string(
                R.string.calendar_picker_range_start,
                fallback = "Start date",
            ),
            rangeEndLabel = CalendarResourceResolver.string(
                R.string.calendar_picker_range_end,
                fallback = "End date",
            ),
            rangeLimitMessage = CalendarResourceResolver.string(
                R.string.calendar_picker_range_limit,
                fallback = "Maximum range is %1\$s days.",
            ),
        )
    }
}

/**
 * Represents the colour palette that is used across the date picker dialog.
 */
@Immutable
data class DatePickerColors(
    val gradientStart: Color,
    val gradientEnd: Color,
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
)

/**
 * Defines rules that limit which dates can be picked by the user.
 */
@Stable
data class DatePickerConstraints(
    val minDate: SoleimaniDate? = null,
    val maxDate: SoleimaniDate? = null,
    val disabledDates: Set<SoleimaniDate> = emptySet(),
    val dateValidator: (SoleimaniDate) -> Boolean = AlwaysValid,
    val maxRangeLength: Int? = null,
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

    fun nearestValidOrNull(anchor: SoleimaniDate): SoleimaniDate? {
        val clamped = clamp(anchor)
        if (isDateSelectable(clamped)) return clamped

        var forward: SoleimaniDate? = clamped
        var backward: SoleimaniDate? = clamped
        val maxIterations = 4000 // ~11 years which is beyond typical dialog usage.
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
class MonthFormatter internal constructor(
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
        val Persian = MonthFormatter { CalendarTextRepository.persianMonthNames() }

        val PersianWithLatinTransliteration = MonthFormatter { digitMode ->
            when (digitMode) {
                DigitMode.Persian -> CalendarTextRepository.persianMonthNames()
                DigitMode.Latin -> CalendarTextRepository.persianMonthLatinNames()
            }
        }

        val Gregorian = MonthFormatter { digitMode ->
            when (digitMode) {
                DigitMode.Persian -> CalendarTextRepository.gregorianMonthNamesFa()
                DigitMode.Latin -> CalendarTextRepository.gregorianMonthNamesEn()
            }
        }
    }
}

@Immutable
class YearFormatter internal constructor(
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
class DateFormatter internal constructor(
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
class WeekdayFormatter internal constructor(
    private val formatter: (DayOfWeek) -> String,
) {
    fun format(day: DayOfWeek): String = formatter(day)

    companion object {
        val PersianShort = WeekdayFormatter { day ->
            CalendarTextRepository.persianWeekdayShort(day)
        }

        val LatinShort = WeekdayFormatter { day ->
            CalendarTextRepository.latinWeekdayShort(day)
        }

        val PersianGregorian = WeekdayFormatter { day ->
            CalendarTextRepository.gregorianWeekdayShortFa(day)
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
    val ContainerShape: Shape = RoundedCornerShape(28.dp)

    fun colors(
        gradientStart: Color = CalendarColorTokens.gradientStart,
        gradientEnd: Color = CalendarColorTokens.gradientEnd,
        containerColor: Color = CalendarColorTokens.baseLight,
        titleTextColor: Color = Color.White,
        subtitleTextColor: Color = CalendarColorTokens.textPrimary.copy(alpha = 0.72f),
        controlIconColor: Color = Color.White,
        todayButtonBackground: Color = CalendarColorTokens.gradientEnd.copy(alpha = 0.28f),
        todayButtonContent: Color = Color.White,
        confirmButtonBackground: Color = CalendarColorTokens.gradientStart,
        confirmButtonContent: Color = Color.White,
        cancelButtonContent: Color = CalendarColorTokens.textPrimary.copy(alpha = 0.75f),
        todayOutline: Color = CalendarColorTokens.gradientEnd,
        weekendLabelColor: Color = CalendarColorTokens.accentOrange,
    ): DatePickerColors = lightColors(
        gradientStart = gradientStart,
        gradientEnd = gradientEnd,
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
    )

    fun lightColors(
        gradientStart: Color = CalendarColorTokens.gradientStart,
        gradientEnd: Color = CalendarColorTokens.gradientEnd,
        containerColor: Color = CalendarColorTokens.baseLight,
        titleTextColor: Color = Color.White,
        subtitleTextColor: Color = CalendarColorTokens.textPrimary.copy(alpha = 0.72f),
        controlIconColor: Color = Color.White,
        todayButtonBackground: Color = CalendarColorTokens.gradientEnd.copy(alpha = 0.28f),
        todayButtonContent: Color = Color.White,
        confirmButtonBackground: Color = CalendarColorTokens.gradientStart,
        confirmButtonContent: Color = Color.White,
        cancelButtonContent: Color = CalendarColorTokens.textPrimary.copy(alpha = 0.75f),
        todayOutline: Color = CalendarColorTokens.gradientEnd,
        weekendLabelColor: Color = CalendarColorTokens.accentOrange,
    ): DatePickerColors = DatePickerColors(
        gradientStart = gradientStart,
        gradientEnd = gradientEnd,
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
    )

    fun darkColors(
        gradientStart: Color = CalendarColorTokens.gradientStart,
        gradientEnd: Color = CalendarColorTokens.gradientEnd,
        containerColor: Color = CalendarColorTokens.baseDark,
        titleTextColor: Color = Color.White,
        subtitleTextColor: Color = Color.White.copy(alpha = 0.82f),
        controlIconColor: Color = Color.White,
        todayButtonBackground: Color = CalendarColorTokens.gradientEnd.copy(alpha = 0.35f),
        todayButtonContent: Color = Color.White,
        confirmButtonBackground: Color = CalendarColorTokens.gradientStart,
        confirmButtonContent: Color = Color.White,
        cancelButtonContent: Color = CalendarColorTokens.textMuted,
        todayOutline: Color = CalendarColorTokens.gradientEnd,
        weekendLabelColor: Color = CalendarColorTokens.accentGold,
    ): DatePickerColors = DatePickerColors(
        gradientStart = gradientStart,
        gradientEnd = gradientEnd,
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
