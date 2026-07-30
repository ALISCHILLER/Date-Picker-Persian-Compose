package com.msa.calendar.core

/**
 * Supported year limits of the deterministic Jalali conversion algorithm.
 *
 * The range intentionally mirrors the break-year table used by the implementation. Callers must
 * not assume that dates outside this range can be represented.
 */
public object JalaliCalendarLimits {
    public const val MIN_YEAR: Int = -61
    public const val MAX_YEAR: Int = 3177

    public val supportedYears: IntRange = MIN_YEAR..MAX_YEAR

    public fun isSupported(year: Int): Boolean = year in supportedYears

    public fun requireSupported(year: Int): Unit = require(isSupported(year)) {
        "Jalali year must be in $MIN_YEAR..$MAX_YEAR but was $year"
    }
}
