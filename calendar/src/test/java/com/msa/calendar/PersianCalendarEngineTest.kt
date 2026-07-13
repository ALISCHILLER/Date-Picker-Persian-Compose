package com.msa.calendar

import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.PersianCalendar
import com.msa.calendar.utils.PersianCalendarEngine
import com.msa.calendar.utils.PersianCalendarLimits
import com.msa.calendar.utils.PersianMonth
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.buildMonthCells
import com.msa.calendar.utils.dayOfWeek
import com.msa.calendar.utils.daysUntil
import com.msa.calendar.utils.minusDays
import com.msa.calendar.utils.plusDays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.GregorianCalendar
import java.util.TimeZone

class PersianCalendarEngineTest {

    @Test
    fun knownNowruzConversionsAreStable() {
        assertEquals(LocalDate.of(2020, 3, 20), SoleimaniDate(1399, 1, 1).toGregorian())
        assertEquals(LocalDate.of(2021, 3, 21), SoleimaniDate(1400, 1, 1).toGregorian())
        assertEquals(LocalDate.of(2022, 3, 21), SoleimaniDate(1401, 1, 1).toGregorian())
        assertEquals(LocalDate.of(2023, 3, 21), SoleimaniDate(1402, 1, 1).toGregorian())
        assertEquals(LocalDate.of(2024, 3, 20), SoleimaniDate(1403, 1, 1).toGregorian())
        assertEquals(LocalDate.of(2025, 3, 21), SoleimaniDate(1404, 1, 1).toGregorian())
        assertEquals(LocalDate.of(2026, 3, 21), SoleimaniDate(1405, 1, 1).toGregorian())
    }

    @Test
    fun knownGregorianDatesConvertBackToJalali() {
        assertEquals(SoleimaniDate(1403, 1, 1), PersionCalendar(LocalDate.of(2024, 3, 20)).toSoleimaniDateForTest())
        assertEquals(SoleimaniDate(1403, 12, 30), PersionCalendar(LocalDate.of(2025, 3, 20)).toSoleimaniDateForTest())
        assertEquals(SoleimaniDate(1404, 1, 1), PersionCalendar(LocalDate.of(2025, 3, 21)).toSoleimaniDateForTest())
        assertEquals(SoleimaniDate(1404, 12, 29), PersionCalendar(LocalDate.of(2026, 3, 20)).toSoleimaniDateForTest())
        assertEquals(SoleimaniDate(1405, 4, 9), PersionCalendar(LocalDate.of(2026, 6, 30)).toSoleimaniDateForTest())
    }

    @Test
    fun persianMonthEnumUsesCorrectCivilMonthLengths() {
        assertEquals(31, PersianMonth.FARVARDIN.length(leapYear = false))
        assertEquals(31, PersianMonth.SHAHRIVAR.length(leapYear = false))
        assertEquals(30, PersianMonth.MEHR.length(leapYear = false))
        assertEquals(30, PersianMonth.BAHMAN.length(leapYear = false))
        assertEquals(29, PersianMonth.ESFAND.length(leapYear = false))
        assertEquals(30, PersianMonth.ESFAND.length(leapYear = true))
    }

    @Test
    fun esfandLengthMatchesLeapYears() {
        assertEquals(30, PersionCalendar(1399, 12, 1).getMonthLength())
        assertEquals(29, PersionCalendar(1400, 12, 1).getMonthLength())
        assertEquals(29, PersionCalendar(1401, 12, 1).getMonthLength())
        assertEquals(29, PersionCalendar(1402, 12, 1).getMonthLength())
        assertEquals(30, PersionCalendar(1403, 12, 1).getMonthLength())
        assertEquals(29, PersionCalendar(1404, 12, 1).getMonthLength())
    }

    @Test
    fun invalidEsfandDayIsRejectedForNonLeapYear() {
        assertThrows(IllegalArgumentException::class.java) {
            SoleimaniDate(1404, 12, 30)
        }
    }

    @Test
    fun validEsfandDayIsAcceptedForLeapYear() {
        assertEquals(LocalDate.of(2025, 3, 20), SoleimaniDate(1403, 12, 30).toGregorian())
    }

    @Test
    fun legacyGregorianCalendarConstructorUsesCivilDateFields() {
        val gregorianCalendar = GregorianCalendar(TimeZone.getTimeZone("Asia/Tehran")).apply {
            clear()
            set(2025, GregorianCalendar.MARCH, 21, 18, 45, 0)
        }

        val persian = PersionCalendar(gregorianCalendar)

        assertEquals(1404, persian.getYear())
        assertEquals(1, persian.getMonth())
        assertEquals(1, persian.getDay())
    }

    @Test
    fun plusAndMinusDaysCrossYearBoundarySafely() {
        val endOfLeapYear = SoleimaniDate(1403, 12, 30)

        assertEquals(SoleimaniDate(1404, 1, 1), endOfLeapYear.plusDays(1))
        assertEquals(SoleimaniDate(1403, 12, 29), endOfLeapYear.minusDays(1))
        assertEquals(1, SoleimaniDate(1403, 12, 30).daysUntil(SoleimaniDate(1404, 1, 1)))
    }

    @Test
    fun dayOfWeekIsCalculatedFromGregorianEquivalent() {
        assertEquals(DayOfWeek.WEDNESDAY, SoleimaniDate(1403, 1, 1).dayOfWeek())
        assertEquals(4, PersionCalendar(1403, 1, 1).getDayOfWeek())
    }

    @Test
    fun monthCellsRespectConfiguredWeekStartAndMonthLength() {
        val cells = buildMonthCells(month = 12, year = 1403, startDay = DayOfWeek.SATURDAY)
        val dateCells = cells.filter { it.date != null }

        assertEquals(42, cells.size)
        assertEquals(30, dateCells.size)
        assertEquals(SoleimaniDate(1403, 12, 1), dateCells.first().date)
        assertEquals(SoleimaniDate(1403, 12, 30), dateCells.last().date)
        assertTrue(cells.first().date == null || cells.first().date == SoleimaniDate(1403, 12, 1))
    }

    @Test
    fun everySupportedMonthBuildsAStableSevenBySixGrid() {
        for (year in PersianCalendarLimits.supportedYears) {
            for (month in 1..12) {
                val cells = buildMonthCells(
                    month = month,
                    year = year,
                    startDay = DayOfWeek.SATURDAY,
                )
                val realDates = cells.mapNotNull { it.date }

                assertEquals(42, cells.size)
                assertEquals(PersianCalendarEngine.monthLength(year, month), realDates.size)
                assertEquals(SoleimaniDate(year, month, 1), realDates.first())
                assertEquals(
                    SoleimaniDate(year, month, PersianCalendarEngine.monthLength(year, month)),
                    realDates.last(),
                )
                cells.forEachIndexed { index, cell ->
                    assertEquals(index % 7, cell.weekdayIndex)
                }
            }
        }
    }

    @Test
    fun legacyLeapApiMatchesDateValidation() {
        assertTrue(PersionCalendar(1403, 1, 1).isLeap())
        assertFalse(PersionCalendar(1404, 1, 1).isLeap())
        assertEquals(366, PersionCalendar(1403, 1, 1).getYearLength())
        assertEquals(365, PersionCalendar(1404, 1, 1).getYearLength())
    }


    @Test
    fun everySupportedPersianDateRoundTripsThroughGregorian() {
        var checkedDates = 0L
        for (year in PersianCalendarLimits.supportedYears) {
            for (month in 1..12) {
                for (day in 1..PersianCalendarEngine.monthLength(year, month)) {
                    val source = SoleimaniDate(year, month, day)
                    val converted = PersianCalendarEngine.fromGregorian(source.toGregorian())
                    assertEquals(source, SoleimaniDate(converted.first, converted.second, converted.third))
                    checkedDates++
                }
            }
        }

        assertEquals(1_183_020L, checkedDates)
    }

    @Test
    fun gregorianDatesOutsideSupportedPersianRangeAreRejected() {
        val first = SoleimaniDate(PersianCalendarLimits.MIN_SUPPORTED_YEAR, 1, 1)
        val last = SoleimaniDate(
            PersianCalendarLimits.MAX_SUPPORTED_YEAR,
            12,
            PersianCalendarEngine.monthLength(PersianCalendarLimits.MAX_SUPPORTED_YEAR, 12),
        )

        assertThrows(IllegalArgumentException::class.java) {
            PersianCalendarEngine.fromGregorian(first.toGregorian().minusDays(1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            PersianCalendarEngine.fromGregorian(last.toGregorian().plusDays(1))
        }
    }

    @Test
    fun publicCalendarLimitsMatchEngineBoundaries() {
        assertEquals(-61, PersianCalendarLimits.MIN_SUPPORTED_YEAR)
        assertEquals(3177, PersianCalendarLimits.MAX_SUPPORTED_YEAR)
        assertThrows(IllegalArgumentException::class.java) {
            SoleimaniDate(PersianCalendarLimits.MIN_SUPPORTED_YEAR - 1, 1, 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PersianCalendar().setYear(PersianCalendarLimits.MAX_SUPPORTED_YEAR + 1)
        }
    }


    @Test
    fun dateArithmeticReturnsNullOutsideSupportedEngineBounds() {
        val firstDate = SoleimaniDate(PersianCalendarLimits.MIN_SUPPORTED_YEAR, 1, 1)
        val lastYear = PersianCalendarLimits.MAX_SUPPORTED_YEAR
        val lastDay = runCatching { SoleimaniDate(lastYear, 12, 30) }
            .getOrElse { SoleimaniDate(lastYear, 12, 29) }

        assertEquals(null, firstDate.minusDays(1))
        assertEquals(null, lastDay.plusDays(1))
    }


    @Test
    fun legacyCalendarMutationIsAtomicAndBoundarySafe() {
        val calendar = PersionCalendar(1404, 1, 1)

        assertThrows(IllegalArgumentException::class.java) {
            calendar.set(PersianCalendarLimits.MAX_SUPPORTED_YEAR + 1, 1, 1)
        }
        assertEquals(1404, calendar.getYear())
        assertEquals(1, calendar.getMonth())
        assertEquals(1, calendar.getDay())

        val first = PersionCalendar(PersianCalendarLimits.MIN_SUPPORTED_YEAR, 1, 1)
        assertEquals(null, first.getDateByDiffOrNull(-1))
        assertEquals(first, first.getYesterday())
    }

    @Test
    fun correctlySpelledCalendarAliasKeepsLegacyBehaviour() {
        val calendar = PersianCalendar(1404, 1, 1)

        assertEquals(1404, calendar.getYear())
        assertEquals(1, calendar.getMonth())
        assertEquals(1, calendar.getDay())
    }

}

private fun PersionCalendar.toSoleimaniDateForTest(): SoleimaniDate {
    return SoleimaniDate(getYear(), getMonth(), getDay())
}
