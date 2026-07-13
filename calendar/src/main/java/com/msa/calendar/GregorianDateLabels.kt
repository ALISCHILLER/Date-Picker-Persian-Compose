package com.msa.calendar

import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.CalendarResourceProvider
import com.msa.calendar.utils.CalendarTextRepository
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersianCalendarEngine
import com.msa.calendar.utils.SoleimaniDate
import java.time.LocalDate

/**
 * Produces compact Gregorian hints for a visible Persian month or an exact Persian date.
 *
 * A Persian month normally overlaps two Gregorian months, so the visible-month label is derived
 * from both the first and last day instead of relying on an approximate year offset.
 */
internal fun gregorianMonthSpanLabel(
    persianYear: Int,
    persianMonth: Int,
    digitMode: DigitMode,
    provider: CalendarResourceProvider? = null,
    includeYear: Boolean = true,
): String {
    val firstGregorianDay = PersianCalendarEngine.toGregorian(
        year = persianYear,
        month = persianMonth,
        day = 1,
    )
    val lastGregorianDay = PersianCalendarEngine.toGregorian(
        year = persianYear,
        month = persianMonth,
        day = PersianCalendarEngine.monthLength(persianYear, persianMonth),
    )
    return formatGregorianMonthSpan(
        first = firstGregorianDay,
        last = lastGregorianDay,
        digitMode = digitMode,
        provider = provider,
        includeYear = includeYear,
    )
}

internal fun gregorianDateLabel(
    persianDate: SoleimaniDate,
    digitMode: DigitMode,
    provider: CalendarResourceProvider? = null,
): String {
    val gregorianDate = PersianCalendarEngine.toGregorian(
        year = persianDate.year,
        month = persianDate.month,
        day = persianDate.day,
    )
    val monthNames = gregorianMonthNames(digitMode, provider)
    val monthName = monthNames[gregorianDate.monthValue - 1]
    val day = gregorianDate.dayOfMonth.toDigitMode(digitMode)
    val year = gregorianDate.year.toDigitMode(digitMode)

    return when (digitMode) {
        DigitMode.Persian -> "$day $monthName $year"
        DigitMode.Latin -> "$monthName $day, $year"
    }
}

private fun formatGregorianMonthSpan(
    first: LocalDate,
    last: LocalDate,
    digitMode: DigitMode,
    provider: CalendarResourceProvider?,
    includeYear: Boolean,
): String {
    val monthNames = gregorianMonthNames(digitMode, provider)
    val firstMonth = monthNames[first.monthValue - 1]
    val lastMonth = monthNames[last.monthValue - 1]
    val firstYear = first.year.toDigitMode(digitMode)
    val lastYear = last.year.toDigitMode(digitMode)

    if (!includeYear) {
        return if (first.monthValue == last.monthValue && first.year == last.year) {
            firstMonth
        } else {
            "$firstMonth – $lastMonth"
        }
    }

    return when {
        first.monthValue == last.monthValue && first.year == last.year -> {
            "$firstMonth $firstYear"
        }
        first.year == last.year -> {
            "$firstMonth – $lastMonth $firstYear"
        }
        else -> {
            "$firstMonth $firstYear – $lastMonth $lastYear"
        }
    }
}

private fun gregorianMonthNames(
    digitMode: DigitMode,
    provider: CalendarResourceProvider?,
): List<String> = when (digitMode) {
    DigitMode.Persian -> CalendarTextRepository.gregorianMonthNamesFa(provider)
    DigitMode.Latin -> CalendarTextRepository.gregorianMonthNamesEn(provider)
}

private fun Int.toDigitMode(digitMode: DigitMode): String = when (digitMode) {
    DigitMode.Persian -> FormatHelper.toPersianNumber(toString())
    DigitMode.Latin -> toString()
}
