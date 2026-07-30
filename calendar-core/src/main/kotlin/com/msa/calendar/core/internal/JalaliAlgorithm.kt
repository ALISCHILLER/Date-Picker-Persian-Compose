package com.msa.calendar.core.internal

import com.msa.calendar.core.JalaliCalendarLimits
import java.time.LocalDate

internal object JalaliAlgorithm {
    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )

    fun toGregorian(year: Int, month: Int, day: Int): LocalDate {
        validateYear(year)
        require(month in 1..12) { "Month must be between 1 and 12 but was $month" }
        require(day in 1..monthLength(year, month)) {
            "Invalid day $day for month $month of year $year"
        }
        val parts = dayNumberToGregorian(jalaliToDayNumber(year, month, day))
        return LocalDate.of(parts.year, parts.month, parts.day)
    }

    fun fromGregorian(date: LocalDate): Triple<Int, Int, Int> {
        val result = dayNumberToJalali(
            gregorianToDayNumber(date.year, date.monthValue, date.dayOfMonth),
        )
        validateYear(result.first)
        return result
    }

    fun monthLength(year: Int, month: Int): Int {
        validateYear(year)
        return when (month) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> if (isLeapYear(year)) 30 else 29
            else -> throw IllegalArgumentException("Month must be between 1 and 12 but was $month")
        }
    }

    fun isLeapYear(year: Int): Boolean {
        validateYear(year)
        return calculateJalaliCalendar(year).leap == 0
    }

    private fun validateYear(year: Int): Unit = JalaliCalendarLimits.requireSupported(year)

    private fun jalaliToDayNumber(year: Int, month: Int, day: Int): Long {
        val calendar = calculateJalaliCalendar(year, includeLeap = false)
        return gregorianToDayNumber(calendar.gregorianYear, 3, calendar.marchDay) +
            ((month - 1) * 31L) -
            ((month / 7).toLong() * (month - 7).toLong()) +
            day - 1L
    }

    private fun dayNumberToJalali(dayNumber: Long): Triple<Int, Int, Int> {
        val gregorianYear = dayNumberToGregorian(dayNumber).year
        var jalaliYear = (gregorianYear - 621).coerceAtMost(JalaliCalendarLimits.MAX_YEAR)
        validateYear(jalaliYear)
        val calendar = calculateJalaliCalendar(jalaliYear)
        val firstFarvardin = gregorianToDayNumber(
            calendar.gregorianYear,
            3,
            calendar.marchDay,
        )
        var dayOffset = dayNumber - firstFarvardin

        if (dayOffset >= 0) {
            if (dayOffset <= 185) {
                return Triple(
                    jalaliYear,
                    1 + (dayOffset / 31).toInt(),
                    (dayOffset % 31).toInt() + 1,
                )
            }
            dayOffset -= 186
        } else {
            jalaliYear -= 1
            dayOffset += 179
            if (calendar.leap == 1) dayOffset += 1
        }

        return Triple(
            jalaliYear,
            7 + (dayOffset / 30).toInt(),
            (dayOffset % 30).toInt() + 1,
        )
    }

    private fun calculateJalaliCalendar(
        year: Int,
        includeLeap: Boolean = true,
    ): JalaliCalendarCalculation {
        validateYear(year)
        val gregorianYear = year + 621
        var leapJalali = -14
        var previousBreak = breaks.first()
        var jump = 0

        for (index in 1 until breaks.size) {
            val nextBreak = breaks[index]
            jump = nextBreak - previousBreak
            if (year < nextBreak) break
            leapJalali += (jump / 33) * 8 + ((jump % 33) / 4)
            previousBreak = nextBreak
        }

        var yearsSinceBreak = year - previousBreak
        leapJalali += (yearsSinceBreak / 33) * 8 + (((yearsSinceBreak % 33) + 3) / 4)
        if (jump % 33 == 4 && jump - yearsSinceBreak == 4) leapJalali += 1

        val leapGregorian = gregorianYear / 4 - (((gregorianYear / 100) + 1) * 3) / 4 - 150
        val marchDay = 20 + leapJalali - leapGregorian
        if (!includeLeap) return JalaliCalendarCalculation(0, gregorianYear, marchDay)

        if (jump - yearsSinceBreak < 6) {
            yearsSinceBreak = yearsSinceBreak - jump + ((jump + 4) / 33) * 33
        }
        var leap = (((yearsSinceBreak + 1) % 33) - 1) % 4
        if (leap == -1) leap = 4
        return JalaliCalendarCalculation(leap, gregorianYear, marchDay)
    }

    private fun gregorianToDayNumber(year: Int, month: Int, day: Int): Long {
        val monthShift = (month - 8) / 6
        var result = ((year + monthShift + 100100).toLong() * 1461) / 4
        result += (153L * positiveModulo(month + 9, 12) + 2) / 5
        result += day.toLong() - 34_840_408L
        result -= (((year + 100100 + monthShift) / 100).toLong() * 3) / 4
        result += 752
        return result
    }

    private fun dayNumberToGregorian(dayNumber: Long): GregorianParts {
        var calculation = 4L * dayNumber + 139_361_631L
        calculation += (((4L * dayNumber + 183_187_720L) / 146_097L) * 3L / 4L) * 4L - 3908L
        val intermediate = ((calculation % 1461L) / 4L) * 5L + 308L
        val day = ((intermediate % 153L) / 5L).toInt() + 1
        val month = ((intermediate / 153L) % 12L).toInt() + 1
        val year = (calculation / 1461L).toInt() - 100100 + (8 - month) / 6
        return GregorianParts(year, month, day)
    }

    private fun positiveModulo(value: Int, modulus: Int): Int {
        val result = value % modulus
        return if (result >= 0) result else result + modulus
    }

    private data class JalaliCalendarCalculation(
        val leap: Int,
        val gregorianYear: Int,
        val marchDay: Int,
    )

    private data class GregorianParts(
        val year: Int,
        val month: Int,
        val day: Int,
    )
}
