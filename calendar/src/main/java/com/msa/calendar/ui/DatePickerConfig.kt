package com.msa.calendar.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.daysUntil
import com.msa.calendar.utils.minusDays
import com.msa.calendar.utils.plusDays
import com.msa.calendar.utils.toPersianNumber
import java.time.DayOfWeek
import kotlin.math.abs
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.toDigitString
/**
 * Controls the overall behaviour and appearance of the Persian date picker dialogs.
 */
@Stable
data class DatePickerConfig(
    val strings: DatePickerStrings = DatePickerStrings(),
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
    val yearFormatter: YearFormatter = YearFormatter.Default,
    val yearRange: IntRange = 1350..1450,
    val eventIndicator: (SoleimaniDate) -> CalendarEvent? = { null },
)

/**
 * Allows customising the textual content of the dialog so it can easily be localised.
 */
@Immutable
data class DatePickerStrings(
    val title: String = "انتخاب تاریخ",
    val confirm: String = "تایید",
    val cancel: String = "انصراف",
    val today: String = "امروز",
    val clearSelection: String = "پاک کردن انتخاب",
    val rangeStartLabel: String = "تاریخ شروع",
    val rangeEndLabel: String = "تاریخ پایان",
)

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
    private val persianLabels: List<String>,
    private val latinLabels: List<String> = persianLabels,
) {
    init {
        require(persianLabels.size == 12) { "persianLabels must contain 12 month names" }
        require(latinLabels.size == 12) { "latinLabels must contain 12 month names" }
    }

    fun format(month: Int, digitMode: DigitMode): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        val index = month - 1
        return when (digitMode) {
            DigitMode.Persian -> persianLabels[index]
            DigitMode.Latin -> latinLabels[index]
        }
    }

    fun labels(digitMode: DigitMode): List<String> = when (digitMode) {
        DigitMode.Persian -> persianLabels
        DigitMode.Latin -> latinLabels
    }

    companion object {
        val Persian = MonthFormatter(
            persianLabels = persianMonthNames,
        )

        val PersianWithLatinTransliteration = MonthFormatter(
            persianLabels = persianMonthNames,
            latinLabels = persianMonthNamesLatin,
        )

        val Gregorian = MonthFormatter(
            persianLabels = gregorianMonthNamesFa,
            latinLabels = gregorianMonthNamesEn,
        )
    }
}

@Immutable
class YearFormatter internal constructor(
    private val formatter: (Int, DigitMode) -> String,
) {
    fun format(year: Int, digitMode: DigitMode): String = formatter(year, digitMode)

    companion object {
        val Default = YearFormatter { year, mode -> year.toDigitString(mode) }

        val WithGregorianHint = YearFormatter { year, mode ->
            val primary = year.toDigitString(mode)
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


private val persianMonthNames = listOf(
    "فروردین",
    "اردیبهشت",
    "خرداد",
    "تیر",
    "مرداد",
    "شهریور",
    "مهر",
    "آبان",
    "آذر",
    "دی",
    "بهمن",
    "اسفند",
)

private val persianMonthNamesLatin = listOf(
    "Farvardin",
    "Ordibehesht",
    "Khordad",
    "Tir",
    "Mordad",
    "Shahrivar",
    "Mehr",
    "Aban",
    "Azar",
    "Dey",
    "Bahman",
    "Esfand",
)

private val gregorianMonthNamesFa = listOf(
    "ژانویه",
    "فوریه",
    "مارس",
    "آوریل",
    "مه",
    "ژوئن",
    "ژوئیه",
    "اوت",
    "سپتامبر",
    "اکتبر",
    "نوامبر",
    "دسامبر",
)

private val gregorianMonthNamesEn = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
)

@Immutable
data class CalendarEvent(
    val color: Color,
    val label: String? = null,
)

@Immutable
data class WeekConfiguration(
    val startDay: DayOfWeek = DayOfWeek.SATURDAY,
    val weekendDays: Set<DayOfWeek> = setOf(DayOfWeek.FRIDAY),
    val dayLabelFormatter: WeekdayFormatter = WeekdayFormatter.PersianShort,
) {
    init {
        require(weekendDays.isNotEmpty()) { "weekendDays must contain at least one day" }
    }

    val orderedDays: List<DayOfWeek>
        get() = List(7) { startDay.shift(it) }
}

@Immutable
class WeekdayFormatter internal constructor(
    private val formatter: (DayOfWeek) -> String,
) {
    fun format(day: DayOfWeek): String = formatter(day)

    companion object {
        val PersianShort = WeekdayFormatter { day ->
            when (day) {
                DayOfWeek.SATURDAY -> "ش"
                DayOfWeek.SUNDAY -> "ی"
                DayOfWeek.MONDAY -> "د"
                DayOfWeek.TUESDAY -> "س"
                DayOfWeek.WEDNESDAY -> "چ"
                DayOfWeek.THURSDAY -> "پ"
                DayOfWeek.FRIDAY -> "ج"
            }
        }

        val LatinShort = WeekdayFormatter { day ->
            when (day) {
                DayOfWeek.MONDAY -> "Mo"
                DayOfWeek.TUESDAY -> "Tu"
                DayOfWeek.WEDNESDAY -> "We"
                DayOfWeek.THURSDAY -> "Th"
                DayOfWeek.FRIDAY -> "Fr"
                DayOfWeek.SATURDAY -> "Sa"
                DayOfWeek.SUNDAY -> "Su"
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
    val ContainerShape: Shape = RoundedCornerShape(28.dp)

    fun colors(
        gradientStart: Color = Color(0xFF0EA5E9),
        gradientEnd: Color = Color(0xFF6366F1),
        containerColor: Color = Color(0xFFF8FAFC),
        titleTextColor: Color = Color(0xFFFFFFFF),
        subtitleTextColor: Color = Color(0xFFF1F5F9),
        controlIconColor: Color = Color(0xFFE0F2FE),
        todayButtonBackground: Color = Color(0xFF2563EB).copy(alpha = 0.28f),
        todayButtonContent: Color = Color(0xFFF8FAFC),
        confirmButtonBackground: Color = Color(0xFF2563EB),
        confirmButtonContent: Color = Color.White,
        cancelButtonContent: Color = Color(0xFF1E3A8A),
        todayOutline: Color = Color(0xFF38BDF8),
        weekendLabelColor: Color = Color(0xFFF97316),
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
        gradientStart: Color = Color(0xFF0EA5E9),
        gradientEnd: Color = Color(0xFF6366F1),
        containerColor: Color = Color(0xFFF8FAFC),
        titleTextColor: Color = Color(0xFFFFFFFF),
        subtitleTextColor: Color = Color(0xFFF1F5F9),
        controlIconColor: Color = Color(0xFFE0F2FE),
        todayButtonBackground: Color = Color(0xFF2563EB).copy(alpha = 0.28f),
        todayButtonContent: Color = Color(0xFFF8FAFC),
        confirmButtonBackground: Color = Color(0xFF2563EB),
        confirmButtonContent: Color = Color.White,
        cancelButtonContent: Color = Color(0xFF1E3A8A),
        todayOutline: Color = Color(0xFF38BDF8),
        weekendLabelColor: Color = Color(0xFFF97316),
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
        gradientStart: Color = Color(0xFF1D4ED8),
        gradientEnd: Color = Color(0xFF312E81),
        containerColor: Color = Color(0xFF0F172A),
        titleTextColor: Color = Color(0xFFBFDBFE),
        subtitleTextColor: Color = Color(0xFFE0F2FE),
        controlIconColor: Color = Color(0xFFBAE6FD),
        todayButtonBackground: Color = Color(0xFF1D4ED8).copy(alpha = 0.35f),
        todayButtonContent: Color = Color(0xFFEFF6FF),
        confirmButtonBackground: Color = Color(0xFF2563EB),
        confirmButtonContent: Color = Color.White,
        cancelButtonContent: Color = Color(0xFFBFDBFE),
        todayOutline: Color = Color(0xFF38BDF8),
        weekendLabelColor: Color = Color(0xFFFACC15),
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

private fun Int.toDigits(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> toPersianNumber()
    DigitMode.Latin -> toString()
}

private fun String.toDigits(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> this.toPersianNumber()
    DigitMode.Latin -> this
}

private fun String.toPersianNumber(): String = buildString(length) {
    for (character in this@toPersianNumber) {
        append(
            when (character) {
                '0' -> '۰'
                '1' -> '۱'
                '2' -> '۲'
                '3' -> '۳'
                '4' -> '۴'
                '5' -> '۵'
                '6' -> '۶'
                '7' -> '۷'
                '8' -> '۸'
                '9' -> '۹'
                else -> character
            }
        )
    }
}

internal fun DayOfWeek.shift(days: Int): DayOfWeek {
    val normalized = ((value - 1 + days) % 7 + 7) % 7
    return DayOfWeek.of(normalized + 1)
}