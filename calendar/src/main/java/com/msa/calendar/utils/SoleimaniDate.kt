package com.msa.calendar.utils

import androidx.compose.runtime.Immutable
import com.msa.calendar.core.JalaliDate
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Represents a single date in the Soleimani (Persian) calendar.
 */
@Immutable
data class SoleimaniDate(
    val year: Int,
    val month: Int,
    val day: Int,
) : Comparable<SoleimaniDate> {

    init {
        require(month in 1..12) { "Month must be in 1..12 but was $month" }
        val maxDayInMonth = PersianCalendarEngine.monthLength(year, month)
        require(day in 1..maxDayInMonth) {
            "Day must be in 1..$maxDayInMonth for month $month of year $year but was $day"
        }
    }

    override fun compareTo(other: SoleimaniDate): Int = compareValuesBy(this, other, SoleimaniDate::year, SoleimaniDate::month, SoleimaniDate::day)

    /**
     * Converts this date to a [PersianCalendar] instance.
     */
    fun toCalendar(): PersianCalendar = PersianCalendar(year, month, day)

    /**
     * Converts this date to the equivalent [LocalDate] in the Gregorian calendar.
     */
    fun toGregorian(): LocalDate = PersianCalendarEngine.toGregorian(year, month, day)

    /** Converts to the Android-free model from the `persian-calendar-core` artifact. */
    fun toJalaliDate(): JalaliDate = JalaliDate(year, month, day)

    /**
     * Creates a map that mirrors the previous public contract of the range picker.
     * @param usePersianDigits whether to format numbers using [toPersianNumber].
     */
    fun toMap(usePersianDigits: Boolean = true): Map<String, String> {
        fun Int.format() = if (usePersianDigits) toPersianNumber() else toString()
        return mapOf(
            "day" to day.format(),
            "month" to month.format(),
            "year" to year.format(),
        )
    }

    companion object {
        /** Creates the Compose-facing model from the Android-free core model. */
        @JvmStatic
        fun from(date: JalaliDate): SoleimaniDate = SoleimaniDate(date.year, date.month, date.day)

        /**
         * Parses a [SoleimaniDate] from three localized strings. The input strings may contain Persian digits.
         */
        fun fromLocalizedStrings(year: String, month: String, day: String): SoleimaniDate? {
            val yearValue = year.toIntSafely() ?: return null
            val monthValue = month.toIntSafely() ?: return null
            val dayValue = day.toIntSafely() ?: return null
            return runCatching { SoleimaniDate(yearValue, monthValue, dayValue) }.getOrNull()
        }

    }
}

/** Moves the date by [days] while keeping it within the Persian calendar system. */
fun SoleimaniDate.plusDays(days: Int): SoleimaniDate? {
    if (days == 0) return this
    return runCatching {
        val targetGregorian = toGregorian().plusDays(days.toLong())
        val (year, month, day) = PersianCalendarEngine.fromGregorian(targetGregorian)
        SoleimaniDate(year, month, day)
    }.getOrNull()
}

/** Convenience wrapper around [plusDays] for subtracting days. */
fun SoleimaniDate.minusDays(days: Int): SoleimaniDate? = plusDays(-days)


/**
 * Converts the current [PersianCalendar] date into a strongly typed [SoleimaniDate].
 */
fun PersionCalendar.toSoleimaniDate(): SoleimaniDate = SoleimaniDate(getYear(), getMonth(), getDay())

/** Returns the [DayOfWeek] represented by this date. */
fun SoleimaniDate.dayOfWeek(): DayOfWeek = PersianCalendarEngine.dayOfWeek(year, month, day)

/** Calculates the signed number of days between this date and [other]. */
fun SoleimaniDate.daysUntil(other: SoleimaniDate): Int {
    val thisEpoch = toGregorian().toEpochDay()
    val otherEpoch = other.toGregorian().toEpochDay()
    return (otherEpoch - thisEpoch).toInt()
}
