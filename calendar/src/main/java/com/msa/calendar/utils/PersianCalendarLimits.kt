package com.msa.calendar.utils

import com.msa.calendar.core.JalaliCalendarLimits

/**
 * Source-compatible limits facade for the Compose artifact.
 *
 * New domain-only integrations can depend on `persian-calendar-core` and use
 * [JalaliCalendarLimits] directly.
 */
public object PersianCalendarLimits {
    public const val MIN_SUPPORTED_YEAR: Int = JalaliCalendarLimits.MIN_YEAR
    public const val MAX_SUPPORTED_YEAR: Int = JalaliCalendarLimits.MAX_YEAR

    public val supportedYears: IntRange = MIN_SUPPORTED_YEAR..MAX_SUPPORTED_YEAR

    public fun isSupportedYear(year: Int): Boolean = JalaliCalendarLimits.isSupported(year)

    public fun requireSupportedYear(year: Int): Unit = JalaliCalendarLimits.requireSupported(year)
}
