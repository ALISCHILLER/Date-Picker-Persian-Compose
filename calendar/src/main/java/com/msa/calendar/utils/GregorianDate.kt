package com.msa.calendar.utils

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Immutable representation of a Gregorian date that exposes calendar-agnostic helpers.
 */
data class GregorianDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    init {
        require(month in 1..12) { "Month must be in 1..12 but was $month" }
        val maxDay = LocalDate.of(year, month, 1).lengthOfMonth()
        require(day in 1..maxDay) { "Day must be in 1..$maxDay for month $month of year $year but was $day" }
    }

    fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)

    fun dayOfWeek(): DayOfWeek = toLocalDate().dayOfWeek
}