package com.msa.calendar.utils

import com.msa.calendar.core.JalaliCalendar
import com.msa.calendar.core.JalaliDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * Compatibility facade used by the Compose module.
 *
 * All conversion and calendar arithmetic is delegated to the pure `:calendar-core` module. Keeping
 * the facade internal prevents the UI implementation from duplicating or owning calendar rules.
 */
internal object PersianCalendarEngine {
    fun toGregorian(year: Int, month: Int, day: Int): LocalDate =
        JalaliCalendar.toGregorian(year, month, day)

    fun fromGregorian(date: LocalDate): Triple<Int, Int, Int> {
        val result = JalaliCalendar.fromGregorian(date)
        return Triple(result.year, result.month, result.day)
    }

    fun dayOfWeek(year: Int, month: Int, day: Int): DayOfWeek =
        JalaliCalendar.toGregorian(year, month, day).dayOfWeek

    fun monthLength(year: Int, month: Int): Int = JalaliCalendar.monthLength(year, month)

    fun isLeapYear(year: Int): Boolean = JalaliCalendar.isLeapYear(year)

    fun today(zoneId: ZoneId = ZoneId.systemDefault()): Triple<Int, Int, Int> {
        val result: JalaliDate = JalaliCalendar.today(zoneId)
        return Triple(result.year, result.month, result.day)
    }
}
