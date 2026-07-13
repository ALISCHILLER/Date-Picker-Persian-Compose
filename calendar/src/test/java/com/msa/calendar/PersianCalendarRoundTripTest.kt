package com.msa.calendar

import com.msa.calendar.utils.PersianCalendarEngine
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PersianCalendarRoundTripTest {

    @Test
    fun representativeDatesRoundTripThroughGregorianCalendar() {
        val dates = buildList {
            for (year in 1398..1408) {
                for (month in 1..12) {
                    add(SoleimaniDate(year, month, 1))
                    add(SoleimaniDate(year, month, PersianCalendarEngine.monthLength(year, month)))
                }
            }
        }

        dates.forEach { original ->
            val gregorian = original.toGregorian()
            val (year, month, day) = PersianCalendarEngine.fromGregorian(gregorian)
            assertEquals("Round-trip failed for $original through $gregorian", original, SoleimaniDate(year, month, day))
        }
    }

    @Test
    fun invalidEsfandThirtyIsRejectedInCommonYears() {
        assertThrows(IllegalArgumentException::class.java) {
            SoleimaniDate(1404, 12, 30)
        }
    }

    @Test
    fun esfandThirtyIsAcceptedInLeapYears() {
        val date = SoleimaniDate(1403, 12, 30)

        assertEquals("2025-03-20", date.toGregorian().toString())
    }
}
