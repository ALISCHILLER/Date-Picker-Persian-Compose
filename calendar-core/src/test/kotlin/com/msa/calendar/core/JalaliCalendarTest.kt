package com.msa.calendar.core

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

public class JalaliCalendarTest {
    @Test
    public fun knownReferenceDatesConvertBothWays(): Unit {
        val references = listOf(
            JalaliDate(1403, 1, 1) to LocalDate.of(2024, 3, 20),
            JalaliDate(1404, 1, 1) to LocalDate.of(2025, 3, 21),
            JalaliDate(1399, 12, 30) to LocalDate.of(2021, 3, 20),
        )
        references.forEach { (jalali, gregorian) ->
            assertEquals(gregorian, jalali.toGregorian())
            assertEquals(jalali, JalaliDate.fromGregorian(gregorian))
        }
    }

    @Test
    public fun representativeRangeRoundTrips(): Unit {
        for (year in 1300..1500 step 5) {
            for (month in 1..12) {
                val lastDay = JalaliCalendar.monthLength(year, month)
                listOf(1, lastDay).forEach { day ->
                    val original = JalaliDate(year, month, day)
                    assertEquals(original, JalaliDate.fromGregorian(original.toGregorian()))
                }
            }
        }
    }

    @Test
    public fun everySupportedMonthEndpointRoundTrips(): Unit {
        for (year in JalaliCalendarLimits.supportedYears) {
            for (month in 1..12) {
                val lastDay = JalaliCalendar.monthLength(year, month)
                for (day in listOf(1, lastDay)) {
                    val original = JalaliDate(year, month, day)
                    assertEquals(original, JalaliDate.fromGregorian(original.toGregorian()))
                }
            }
        }
    }

    @Test
    public fun importantMonthAndYearTransitionsRemainContinuous(): Unit {
        val transitions = listOf(
            JalaliDate(1399, 12, 30) to JalaliDate(1400, 1, 1),
            JalaliDate(1400, 12, 29) to JalaliDate(1401, 1, 1),
            JalaliDate(1404, 6, 31) to JalaliDate(1404, 7, 1),
            JalaliDate(1404, 12, 29) to JalaliDate(1405, 1, 1),
        )
        transitions.forEach { (before, after) ->
            assertEquals(after, before.plusDays(1))
            assertEquals(before, after.minusDays(1))
            assertEquals(1L, before.daysUntil(after))
        }
    }

    @Test
    public fun leapYearControlsEsfandLength(): Unit {
        assertTrue(JalaliCalendar.isLeapYear(1399))
        assertEquals(30, JalaliCalendar.monthLength(1399, 12))
        assertFalse(JalaliCalendar.isLeapYear(1400))
        assertEquals(29, JalaliCalendar.monthLength(1400, 12))
    }

    @Test
    public fun rangeIsOrderedAndInclusive(): Unit {
        val range = JalaliDateRange.of(JalaliDate(1404, 1, 3), JalaliDate(1404, 1, 1))
        assertEquals(JalaliDate(1404, 1, 1), range.start)
        assertEquals(JalaliDate(1404, 1, 3), range.endInclusive)
        assertEquals(3L, range.lengthInDays)
        assertEquals(3, range.asSequence().count())
        assertEquals(
            range,
            JalaliDateRange.of(range.endInclusive, range.start),
        )
    }

    @Test
    public fun digitsNormalizePersianAndArabicInputs(): Unit {
        assertEquals("1404-01-09", CalendarDigits.toLatin("۱۴۰۴-۰۱-۰۹"))
        assertEquals("1404-01-09", CalendarDigits.toLatin("١٤٠٤-٠١-٠٩"))
        assertEquals("۱۴۰۴-۰۱-۰۹", CalendarDigits.toPersian("1404-01-09"))
        assertEquals(JalaliDate(1404, 1, 9), JalaliDate.parseOrNull("۱۴۰۴-۰۱-۰۹"))
    }

    @Test
    public fun injectedClockMakesTodayDeterministic(): Unit {
        val clock = Clock.fixed(Instant.parse("2025-03-21T10:15:30Z"), ZoneOffset.UTC)

        assertEquals(JalaliDate(1404, 1, 1), JalaliCalendar.today(clock))
        assertEquals(JalaliDate(1404, 1, 1), JalaliDate.today(clock))
    }

    @Test
    public fun parseReportsStableValidationErrors(): Unit {
        assertEquals(
            JalaliDateParseResult.Failure(JalaliDateParseError.EmptyInput),
            JalaliDate.parse("  "),
        )
        assertEquals(
            JalaliDateParseResult.Failure(JalaliDateParseError.InvalidFormat),
            JalaliDate.parse("1404/01/01"),
        )
        assertEquals(
            JalaliDateParseResult.Failure(JalaliDateParseError.InvalidYear),
            JalaliDate.parse("9999999999-01-01"),
        )
        assertEquals(
            JalaliDateParseResult.Failure(JalaliDateParseError.UnsupportedYear),
            JalaliDate.parse("4000-01-01"),
        )
        assertEquals(
            JalaliDateParseResult.Failure(JalaliDateParseError.InvalidMonth),
            JalaliDate.parse("1404-13-01"),
        )
        assertEquals(
            JalaliDateParseResult.Failure(JalaliDateParseError.InvalidDay),
            JalaliDate.parse("1404-12-30"),
        )
        assertEquals(
            JalaliDateParseResult.Success(JalaliDate(1404, 1, 9)),
            JalaliDate.parse("۱۴۰۴-۰۱-۰۹"),
        )
    }

    @Test
    public fun signedBoundaryYearRoundTripsThroughText(): Unit {
        val date = JalaliDate(JalaliCalendarLimits.MIN_YEAR, 1, 1)
        assertEquals(date, JalaliDate.parseOrNull(date.toString()))
    }
}
