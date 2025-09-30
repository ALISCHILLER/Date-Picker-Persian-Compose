package com.msa.calendar.utils

/**
 * Ensures that the provided [dayText] represents a day that exists in the given month/year.
 * Returns an updated value if coercion is required, otherwise `null`.
 */
fun adjustDayIfOutOfBounds(
    dayValue: Int?,
    month: Int,
    year: Int,
): Int? {
    if (dayValue == null) return null
    if (month !in 1..12) return null
    val monthLength = PersianCalendarEngine.monthLength(year, month)
    if (monthLength <= 0) return null
    val coercedDay = dayValue.coerceIn(1, monthLength)
    return if (coercedDay != dayValue) coercedDay else null
}