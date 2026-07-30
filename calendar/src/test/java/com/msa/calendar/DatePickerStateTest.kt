package com.msa.calendar

import com.msa.calendar.core.JalaliDate
import com.msa.calendar.core.JalaliDateRange
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DatePickerStateTest {
    @Test
    fun singleStateRejectsUnavailableDates(): Unit {
        val blocked = SoleimaniDate(1404, 1, 13)
        val state = SingleDatePickerState(
            initialSelection = null,
            constraints = DatePickerConstraints(disabledDates = setOf(blocked)),
        )

        assertEquals(
            SingleDatePickerUpdate.RejectedByConstraints,
            state.tryDispatch(SingleDatePickerEvent.Select(blocked)),
        )
        assertNull(state.selectedDate)

        val allowed = SoleimaniDate(1404, 1, 14)
        assertEquals(
            SingleDatePickerUpdate.Applied,
            state.tryDispatch(SingleDatePickerEvent.Select(allowed)),
        )
        assertEquals(allowed, state.selectedDate)
        assertEquals(JalaliDate(1404, 1, 14), state.selectedJalaliDate)
    }

    @Test
    fun singleStateAcceptsCoreDate(): Unit {
        val state = SingleDatePickerState(null, DatePickerConstraints())

        state.select(JalaliDate(1404, 2, 3))

        assertEquals(SoleimaniDate(1404, 2, 3), state.selectedDate)
    }

    @Test
    fun rangeStateOrdersAndClearsSelection(): Unit {
        val state = DateRangePickerState(null, null, DatePickerConstraints())
        state.dispatch(
            DateRangePickerEvent.Replace(
                SoleimaniDate(1404, 1, 5),
                SoleimaniDate(1404, 1, 1),
            ),
        )

        assertEquals(SoleimaniDate(1404, 1, 1), state.startDate)
        assertEquals(SoleimaniDate(1404, 1, 5), state.endDate)
        assertEquals(
            JalaliDateRange.of(JalaliDate(1404, 1, 1), JalaliDate(1404, 1, 5)),
            state.selectedJalaliRange,
        )
        assertTrue(state.isComplete)

        state.dispatch(DateRangePickerEvent.Clear)
        assertNull(state.startDate)
        assertNull(state.endDate)
        assertFalse(state.isComplete)
    }

    @Test
    fun rangeStateDoesNotCrossDisabledDate(): Unit {
        val blocked = SoleimaniDate(1404, 1, 3)
        val state = DateRangePickerState(
            null,
            null,
            DatePickerConstraints(disabledDates = setOf(blocked)),
        )

        assertEquals(
            DateRangePickerUpdate.Started,
            state.tryDispatch(DateRangePickerEvent.Select(SoleimaniDate(1404, 1, 1))),
        )
        assertEquals(
            DateRangePickerUpdate.RejectedByConstraints,
            state.tryDispatch(DateRangePickerEvent.Select(SoleimaniDate(1404, 1, 5))),
        )

        assertEquals(SoleimaniDate(1404, 1, 1), state.startDate)
        assertNull(state.endDate)
    }

    @Test
    fun singleStateReportsClearTransition(): Unit {
        val state = SingleDatePickerState(
            SoleimaniDate(1404, 2, 3),
            DatePickerConstraints(),
        )

        assertEquals(
            SingleDatePickerUpdate.Cleared,
            state.tryDispatch(SingleDatePickerEvent.Clear),
        )
        assertNull(state.selectedDate)
    }

    @Test
    fun rangeStateReportsCompleteRestartAndClearTransitions(): Unit {
        val state = DateRangePickerState(null, null, DatePickerConstraints())

        assertEquals(
            DateRangePickerUpdate.Started,
            state.tryDispatch(DateRangePickerEvent.Select(SoleimaniDate(1404, 1, 1))),
        )
        assertEquals(
            DateRangePickerUpdate.Completed,
            state.tryDispatch(DateRangePickerEvent.Select(SoleimaniDate(1404, 1, 5))),
        )
        assertEquals(
            DateRangePickerUpdate.Restarted,
            state.tryDispatch(DateRangePickerEvent.Select(SoleimaniDate(1404, 2, 1))),
        )
        assertEquals(SoleimaniDate(1404, 2, 1), state.startDate)
        assertNull(state.endDate)
        assertEquals(
            DateRangePickerUpdate.Cleared,
            state.tryDispatch(DateRangePickerEvent.Clear),
        )
        assertNull(state.startDate)
    }

    @Test
    fun invalidInitialSelectionsAreNotCommitted(): Unit {
        val blocked = SoleimaniDate(1404, 1, 3)
        val constraints = DatePickerConstraints(disabledDates = setOf(blocked))

        val single = SingleDatePickerState(blocked, constraints)
        val range = DateRangePickerState(
            SoleimaniDate(1404, 1, 1),
            SoleimaniDate(1404, 1, 5),
            constraints,
        )

        assertNull(single.selectedDate)
        assertNull(range.startDate)
        assertNull(range.endDate)
    }

    @Test
    fun selectingEarlierSecondEndpointRestartsRange(): Unit {
        val state = DateRangePickerState(null, null, DatePickerConstraints())
        state.trySelect(JalaliDate(1404, 2, 10))

        assertEquals(
            DateRangePickerUpdate.Restarted,
            state.trySelect(JalaliDate(1404, 2, 5)),
        )
        assertEquals(SoleimaniDate(1404, 2, 5), state.startDate)
        assertNull(state.endDate)
    }
}
