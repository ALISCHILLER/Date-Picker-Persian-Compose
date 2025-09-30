package com.msa.calendar.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pure, timezone-independent utilities for working with the Persian (Soleimani) calendar.
 * The implementation follows the algorithms from "Calendrical Calculations" by Dershowitz and Reingold.
 */
internal object PersianCalendarEngine {
    /**
     * Julian day number for 1 Farvardin 0001 (March 19th 622 CE at midnight).
     *
     * Historical Persian calendars are defined relative to a noon-based epoch
     * (1948320.5). We operate strictly on midnight-based [LocalDate] instances,
     * therefore the integer epoch must be rounded up so that the Gregorian
     * mapping remains aligned with civil midnights. Using 1948321 ensures that
     * 1 Farvardin 1403 correctly corresponds to 20 March 2024 (Wednesday)
     * instead of drifting one day earlier.
     */
    private const val PERSIAN_EPOCH = 1948321

    fun toGregorian(year: Int, month: Int, day: Int): LocalDate {
        val jdn = toJulianDay(year, month, day)
        return julianDayToGregorian(jdn)
    }

    fun fromGregorian(date: LocalDate): Triple<Int, Int, Int> {
        val jdn = gregorianToJulianDay(date.year, date.monthValue, date.dayOfMonth)
        return julianDayToPersian(jdn)
    }

    fun dayOfWeek(year: Int, month: Int, day: Int): DayOfWeek {
        return toGregorian(year, month, day).dayOfWeek
    }

    fun monthLength(year: Int, month: Int): Int = when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isLeapYear(year)) 30 else 29
        else -> throw IllegalArgumentException("Month must be between 1 and 12 but was $month")
    }

    fun isLeapYear(year: Int): Boolean {
        val epBase = year - if (year >= 0) 474 else 473
        val epYear = 474 + mod(epBase, 2820)
        val leapCycleRemainder = mod(epYear * 682 - 110, 2816)
        return leapCycleRemainder < 682
    }

    fun today(zoneId: ZoneId = ZoneId.systemDefault()): Triple<Int, Int, Int> {
        val today = LocalDate.now(zoneId)
        return fromGregorian(today)
    }

    private fun toJulianDay(year: Int, month: Int, day: Int): Int {
        require(month in 1..12) { "Month must be between 1 and 12 but was $month" }
        require(day in 1..monthLength(year, month)) { "Invalid day $day for month $month of year $year" }
        val epBase = year - if (year >= 0) 474 else 473
        val epYear = 474 + mod(epBase, 2820)
        val monthDay = if (month <= 7) {
            (month - 1) * 31
        } else {
            (month - 1) * 30 + 6
        }
        val dayCount = day + monthDay
        val yearDays = (epYear - 1) * 365 + ((epYear * 682 - 110) / 2816)
        val cycleDays = (epBase / 2820) * 1029983
        return dayCount + yearDays + cycleDays + (PERSIAN_EPOCH - 1)
    }

    private fun julianDayToPersian(jdn: Int): Triple<Int, Int, Int> {
        val depoch = jdn - toJulianDay(475, 1, 1)
        val cycle = depoch / 1029983
        var cyear = depoch % 1029983
        val yCycle = if (cyear == 1029982) {
            2820
        } else {
            val aux1 = cyear / 366
            val aux2 = cyear % 366
            ((2134 * aux1 + 2816 * aux2 + 2815) / 1028522) + aux1 + 1
        }
        var year = yCycle + 2820 * cycle + 474
        if (year <= 0) {
            year -= 1
        }
        val startOfYear = toJulianDay(year, 1, 1)
        val dayOfYear = jdn - startOfYear + 1
        val month = if (dayOfYear <= 186) {
            ((dayOfYear - 1) / 31) + 1
        } else {
            ((dayOfYear - 187) / 30) + 7
        }
        val day = jdn - toJulianDay(year, month, 1) + 1
        return Triple(year, month, day)
    }

    private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + ((153 * m + 2) / 5) + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun julianDayToGregorian(jdn: Int): LocalDate {
        val epochDay = jdn - 2440588L
        return LocalDate.ofEpochDay(epochDay)
    }

    private fun mod(a: Int, b: Int): Int {
        val result = a % b
        return if (result >= 0) result else result + b
    }
}