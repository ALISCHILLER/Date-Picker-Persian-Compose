package com.msa.calendar

import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateRangeSelectionStateTest {

    @Test
    fun firstSelectionStartsANewOpenRange() {
        val candidate = SoleimaniDate(1403, 2, 10)

        val update = resolveRangeSelection(
            candidate = candidate,
            currentStart = null,
            currentEnd = null,
        )

        assertEquals(candidate, update.startDate)
        assertNull(update.endDate)
        assertEquals(10, update.selectedDay)
    }

    @Test
    fun selectingEarlierDateWhileRangeIsOpenMovesTheStart() {
        val currentStart = SoleimaniDate(1403, 2, 10)
        val candidate = SoleimaniDate(1403, 2, 5)

        val update = resolveRangeSelection(
            candidate = candidate,
            currentStart = currentStart,
            currentEnd = null,
        )

        assertEquals(candidate, update.startDate)
        assertNull(update.endDate)
        assertEquals(5, update.selectedDay)
    }

    @Test
    fun selectingSameOrLaterDateWhileRangeIsOpenCompletesTheRange() {
        val currentStart = SoleimaniDate(1403, 2, 10)
        val candidate = SoleimaniDate(1403, 2, 12)

        val update = resolveRangeSelection(
            candidate = candidate,
            currentStart = currentStart,
            currentEnd = null,
        )

        assertEquals(currentStart, update.startDate)
        assertEquals(candidate, update.endDate)
        assertEquals(12, update.selectedDay)
    }

    @Test
    fun selectingAfterACompletedRangeStartsANewRange() {
        val candidate = SoleimaniDate(1403, 3, 1)

        val update = resolveRangeSelection(
            candidate = candidate,
            currentStart = SoleimaniDate(1403, 2, 10),
            currentEnd = SoleimaniDate(1403, 2, 12),
        )

        assertEquals(candidate, update.startDate)
        assertNull(update.endDate)
        assertEquals(1, update.selectedDay)
    }
}
