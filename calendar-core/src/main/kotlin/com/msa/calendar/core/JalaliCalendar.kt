package com.msa.calendar.core

import com.msa.calendar.core.internal.JalaliAlgorithm
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/** Stateless entry point for Jalali calendar conversion and calendar arithmetic. */
public object JalaliCalendar {
    @JvmStatic
    public fun toGregorian(date: JalaliDate): LocalDate =
        JalaliAlgorithm.toGregorian(date.year, date.month, date.day)

    @JvmStatic
    public fun toGregorian(year: Int, month: Int, day: Int): LocalDate =
        JalaliAlgorithm.toGregorian(year, month, day)

    @JvmStatic
    public fun fromGregorian(date: LocalDate): JalaliDate {
        val result = JalaliAlgorithm.fromGregorian(date)
        return JalaliDate(result.first, result.second, result.third)
    }

    @JvmStatic
    public fun isLeapYear(year: Int): Boolean = JalaliAlgorithm.isLeapYear(year)

    @JvmStatic
    public fun monthLength(year: Int, month: Int): Int = JalaliAlgorithm.monthLength(year, month)

    /** Returns today using an injectable clock for deterministic tests and host-controlled time. */
    @JvmStatic
    public fun today(clock: Clock): JalaliDate = fromGregorian(LocalDate.now(clock))

    /** Returns today in [zoneId]. Prefer [today] with a [Clock] in testable domain code. */
    @JvmStatic
    @JvmOverloads
    public fun today(zoneId: ZoneId = ZoneId.systemDefault()): JalaliDate =
        today(Clock.system(zoneId))
}
