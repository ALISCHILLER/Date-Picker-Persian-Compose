package com.msa.persioncalendar.showcase

import com.msa.calendar.DateRangeSelection
import com.msa.calendar.SingleDateSelection
import com.msa.calendar.ui.DateFormatter
import com.msa.calendar.ui.DateRangeFormatter
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarSystem
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarShowcaseStateTest {

    @Test
    fun openingOnePickerClosesTheOtherPicker() {
        val state = state()

        state.openSinglePicker()
        assertTrue(state.showSinglePicker)
        assertFalse(state.showRangePicker)

        state.openRangePicker()
        assertFalse(state.showSinglePicker)
        assertTrue(state.showRangePicker)
    }

    @Test
    fun confirmedSelectionsUpdateTypedStateAndLastSelectionType() {
        val state = state()
        val singleSelection = SingleDateSelection.create(
            date = SoleimaniDate(1404, 1, 1),
            dateFormatter = DateFormatter.Default,
            digitMode = DigitMode.Latin,
        )
        val rangeSelection = DateRangeSelection.create(
            firstDate = SoleimaniDate(1404, 1, 10),
            secondDate = SoleimaniDate(1404, 1, 5),
            dateFormatter = DateFormatter.Default,
            rangeFormatter = DateRangeFormatter.Default,
            digitMode = DigitMode.Latin,
        )

        state.onSingleSelectionConfirmed(singleSelection)
        assertEquals(SoleimaniDate(1404, 1, 1), state.selectedSingleDate)
        assertEquals(SelectionType.Single, state.lastSelectionType)

        state.onRangeSelectionConfirmed(rangeSelection)
        assertEquals(SoleimaniRange.of(SoleimaniDate(1404, 1, 5), SoleimaniDate(1404, 1, 10)), state.selectedRange)
        assertEquals(SelectionType.Range, state.lastSelectionType)
    }

    @Test
    fun clearSelectionRemovesAllSelectionState() {
        val state = state()
        state.onQuickTodaySelected(SoleimaniDate(1404, 1, 1))

        state.clearSelection()

        assertNull(state.selectedSingleDate)
        assertNull(state.selectedRange)
        assertNull(state.lastSelectionType)
    }

    @Test
    fun localeSelectionKeepsPersianCalendarAndUpdatesDigitPreference() {
        val state = state()

        state.onLocaleOptionSelected(LocaleOption.English)
        assertEquals(CalendarSystem.Persian, state.localeConfiguration.calendarSystem)
        assertFalse(state.localeConfiguration.isRtl)
        assertTrue(state.useLatinDigits)

        state.onLocaleOptionSelected(LocaleOption.Persian)
        assertEquals(CalendarSystem.Persian, state.localeConfiguration.calendarSystem)
        assertTrue(state.localeConfiguration.isRtl)
        assertFalse(state.useLatinDigits)
    }

    @Test
    fun englishLocaleUsesPersianMonthTransliterationNotGregorianLabels() {
        val state = state()

        state.onLocaleOptionSelected(LocaleOption.English)

        assertTrue(state.shouldUsePersianMonthTransliteration())
        assertEquals(CalendarSystem.Persian, state.localeConfiguration.calendarSystem)
    }

    @Test
    fun persianLocaleKeepsPersianMonthLabelsUnlessExplicitlyRequested() {
        val state = state()

        state.onLocaleOptionSelected(LocaleOption.Persian)
        assertFalse(state.shouldUsePersianMonthTransliteration())

        state.useTransliteratedMonthLabels = true
        assertTrue(state.shouldUsePersianMonthTransliteration())
    }

    private fun state(): CalendarShowcaseState = CalendarShowcaseState(
        today = SoleimaniDate(1404, 1, 1),
        localeResolver = { CalendarLocaleConfiguration.persian() },
    )
}
