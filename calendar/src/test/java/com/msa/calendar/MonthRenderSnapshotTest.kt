package com.msa.calendar

import androidx.compose.ui.graphics.Color
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthRenderSnapshotTest {

    @Test
    fun singleSnapshotUsesDirectDayLookupForConstraintsAndEvents() {
        val disabled = SoleimaniDate(1404, 1, 3)
        val eventDate = SoleimaniDate(1404, 1, 5)
        val snapshot = MonthRenderSnapshot.single(
            year = 1404,
            month = 1,
            constraints = DatePickerConstraints(disabledDates = setOf(disabled)),
            eventIndicator = { date ->
                if (date == eventDate) CalendarEvent(Color.Red, "Event") else null
            },
        )

        assertTrue(snapshot.isEnabled(1))
        assertFalse(snapshot.isEnabled(3))
        assertEquals("Event", snapshot.event(5)?.label)
        assertNull(snapshot.event(4))
    }

    @Test
    fun rangeSnapshotStopsAfterFirstUnavailableInteriorDate() {
        val constraints = DatePickerConstraints(
            disabledDates = setOf(SoleimaniDate(1404, 1, 3)),
        )
        val snapshot = MonthRenderSnapshot.range(
            year = 1404,
            month = 1,
            currentStart = SoleimaniDate(1404, 1, 1),
            currentEnd = null,
            constraints = constraints,
            eventIndicator = { null },
        )

        assertTrue(snapshot.isEnabled(2))
        assertFalse(snapshot.isEnabled(3))
        assertFalse(snapshot.isEnabled(4))
    }

    @Test
    fun lookupRejectsDatesFromAnotherVisibleMonth() {
        val snapshot = MonthRenderSnapshot.single(
            year = 1404,
            month = 1,
            constraints = DatePickerConstraints(),
            eventIndicator = { null },
        )

        assertFalse(snapshot.isEnabled(SoleimaniDate(1404, 2, 1)))
        assertNull(snapshot.event(SoleimaniDate(1404, 2, 1)))
    }
}
