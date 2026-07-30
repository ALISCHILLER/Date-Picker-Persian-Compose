package com.msa.calendar

import androidx.compose.ui.graphics.Color
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.ui.WeekdayFormatter
import com.msa.calendar.utils.CalendarResourceProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class CalendarResourceProviderTest {

    @Test
    fun datePickerStringsCanResolveFromScopedProvider() {
        val provider = FakeCalendarResourceProvider(
            strings = mapOf(
                R.string.calendar_picker_title to "Scoped title",
                R.string.calendar_picker_confirm to "Apply",
                R.string.calendar_picker_cancel to "Close",
                R.string.calendar_picker_today to "Now",
                R.string.calendar_picker_clear to "Reset",
                R.string.calendar_picker_range_start to "From",
                R.string.calendar_picker_range_end to "To",
                R.string.calendar_picker_range_limit to "Only %1\$s days",
                R.string.calendar_picker_range_separator to "through",
                R.string.calendar_picker_select_month to "Choose month",
                R.string.calendar_picker_select_year to "Choose year",
                R.string.calendar_picker_previous_month to "Previous month",
                R.string.calendar_picker_next_month to "Next month",
                R.string.calendar_picker_previous_year_page to "Previous years",
                R.string.calendar_picker_next_year_page to "Next years",
                R.string.calendar_picker_empty_day to "Empty",
                R.string.calendar_picker_state_selected to "Chosen",
                R.string.calendar_picker_state_range_start to "From point",
                R.string.calendar_picker_state_range_end to "To point",
                R.string.calendar_picker_state_inside_range to "Between",
                R.string.calendar_picker_state_today to "Current day",
                R.string.calendar_picker_state_disabled to "Unavailable",
                R.string.calendar_picker_state_available to "Available",
                R.string.calendar_picker_state_event_prefix to "Marker",
                R.string.calendar_picker_gregorian_label to "Gregorian calendar",
            )
        )

        val strings = DatePickerStrings.localized(provider)

        assertEquals("Scoped title", strings.title)
        assertEquals("Apply", strings.confirm)
        assertEquals("Close", strings.cancel)
        assertEquals("Now", strings.today)
        assertEquals("Reset", strings.clearSelection)
        assertEquals("From", strings.rangeStartLabel)
        assertEquals("To", strings.rangeEndLabel)
        assertEquals("Only %1\$s days", strings.rangeLimitMessage)
        assertEquals("through", strings.rangeSeparator)
        assertEquals("Choose month", strings.selectMonth)
        assertEquals("Choose year", strings.selectYear)
        assertEquals("Previous month", strings.previousMonth)
        assertEquals("Next month", strings.nextMonth)
        assertEquals("Previous years", strings.previousYearPage)
        assertEquals("Next years", strings.nextYearPage)
        assertEquals("Empty", strings.emptyDay)
        assertEquals("Chosen", strings.selectedState)
        assertEquals("From point", strings.rangeStartState)
        assertEquals("To point", strings.rangeEndState)
        assertEquals("Between", strings.insideSelectedRangeState)
        assertEquals("Current day", strings.todayState)
        assertEquals("Unavailable", strings.disabledState)
        assertEquals("Available", strings.availableState)
        assertEquals("Marker", strings.eventPrefix)
        assertEquals("Gregorian calendar", strings.gregorianCalendarLabel)
    }

    @Test
    fun monthFormatterCanUseScopedProviderWithoutGlobalResources() {
        val provider = FakeCalendarResourceProvider(
            arrays = mapOf(
                R.array.persian_months to List(12) { index -> "M${index + 1}" },
            )
        )

        val formatter = MonthFormatter.persian(provider)

        assertEquals("M1", formatter.format(month = 1, digitMode = com.msa.calendar.ui.DigitMode.Latin))
        assertEquals("M12", formatter.format(month = 12, digitMode = com.msa.calendar.ui.DigitMode.Latin))
    }

    @Test
    fun weekdayFormatterCanUseScopedProviderWithoutLocaleOverride() {
        val provider = FakeCalendarResourceProvider(
            arrays = mapOf(
                R.array.persian_weekdays_short to listOf("Sat*", "Sun*", "Mon*", "Tue*", "Wed*", "Thu*", "Fri*"),
            )
        )

        val formatter = WeekdayFormatter.persianShort(provider)

        assertEquals("Sat*", formatter.format(DayOfWeek.SATURDAY))
        assertEquals("Fri*", formatter.format(DayOfWeek.FRIDAY))
    }

    private class FakeCalendarResourceProvider(
        private val strings: Map<Int, String> = emptyMap(),
        private val arrays: Map<Int, List<String>> = emptyMap(),
    ) : CalendarResourceProvider {
        override fun color(id: Int, fallback: Long): Color = Color(fallback)

        override fun string(id: Int, fallback: String): String = strings[id] ?: fallback

        override fun stringArray(id: Int, fallback: List<String>): List<String> = arrays[id] ?: fallback
    }
}
