package com.msa.calendar

import com.msa.calendar.ui.DateFormatter
import com.msa.calendar.ui.DateRangeFormatter
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.WeekdayFormatter
import com.msa.calendar.ui.YearFormatter
import com.msa.calendar.utils.CalendarResourceProvider
import com.msa.calendar.utils.CalendarResources
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class DatePickerFormattingTest {

    @Test
    fun defaultDateFormatterSupportsLatinAndPersianDigits() {
        val date = SoleimaniDate(1404, 2, 9)

        assertEquals("1404 / 02 / 09", DateFormatter.Default.format(date, DigitMode.Latin))
        assertEquals("۱۴۰۴ / ۰۲ / ۰۹", DateFormatter.Default.format(date, DigitMode.Persian))
    }

    @Test
    fun defaultRangeFormatterDoesNotModifyEndpointFormatting() {
        assertEquals(
            "1404 / 01 / 01 - 1404 / 01 / 10",
            DateRangeFormatter.Default.format("1404 / 01 / 01", "1404 / 01 / 10"),
        )
    }

    @Test
    fun yearFormatterCanShowGregorianHintInBothDigitModes() {
        assertEquals("1404", YearFormatter.Default.format(1404, DigitMode.Latin))
        assertEquals("۱۴۰۴", YearFormatter.Default.format(1404, DigitMode.Persian))
        assertEquals("1404 (2025)", YearFormatter.WithGregorianHint.format(1404, DigitMode.Latin))
        assertEquals("۱۴۰۴ (۲۰۲۵)", YearFormatter.WithGregorianHint.format(1404, DigitMode.Persian))
    }

    @Test
    fun monthFormatterRejectsInvalidMonthAndInvalidProviderSize() {
        val formatter = MonthFormatter.persian(FakeCalendarResourceProvider())

        assertThrows(IllegalArgumentException::class.java) {
            formatter.format(month = 0, digitMode = DigitMode.Latin)
        }

        val brokenCustomFormatter = MonthFormatter { listOf("Only one month") }
        assertThrows(IllegalArgumentException::class.java) {
            brokenCustomFormatter.labels(DigitMode.Latin)
        }
    }

    @Test
    fun weekConfigurationOrdersDaysFromCustomStartAndWrapsIndexes() {
        val configuration = WeekConfiguration(
            startDay = DayOfWeek.THURSDAY,
            weekendDays = setOf(DayOfWeek.FRIDAY),
            dayLabelFormatter = WeekdayFormatter.LatinShort,
        )

        assertEquals(
            listOf(
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
            ),
            configuration.orderedDays,
        )
        assertEquals(DayOfWeek.WEDNESDAY, configuration.dayAt(-1))
        assertEquals(DayOfWeek.THURSDAY, configuration.dayAt(7))
        assertTrue(configuration.isWeekendIndex(1))
    }

    @Test
    fun weekConfigurationRejectsEmptyWeekendSet() {
        assertThrows(IllegalArgumentException::class.java) {
            WeekConfiguration(weekendDays = emptySet())
        }
    }


    @Test
    fun defaultFormatterSingletonsResolveGlobalProviderLazily() {
        val monthFormatter = MonthFormatter.Persian
        val weekdayFormatter = WeekdayFormatter.PersianShort
        try {
            CalendarResources.clear()
            CalendarResources.initialize(
                FakeCalendarResourceProvider(
                    arrays = mapOf(
                        R.array.persian_months to List(12) { index -> "P${index + 1}" },
                        R.array.persian_weekdays_short to listOf("Sat*", "Sun*", "Mon*", "Tue*", "Wed*", "Thu*", "Fri*"),
                    ),
                )
            )

            assertEquals("P2", monthFormatter.format(month = 2, digitMode = DigitMode.Latin))
            assertEquals("Sat*", weekdayFormatter.format(DayOfWeek.SATURDAY))
        } finally {
            CalendarResources.clear()
        }
    }

    @Test
    fun customFormatterConstructorsArePublicApiReady() {
        val monthFormatter = MonthFormatter { digitMode ->
            List(12) { index -> "${digitMode.name}-${index + 1}" }
        }
        val yearFormatter = YearFormatter { year, digitMode -> "${digitMode.name}:$year" }
        val weekdayFormatter = WeekdayFormatter { day -> day.name.take(2) }

        assertEquals("Latin-3", monthFormatter.format(month = 3, digitMode = DigitMode.Latin))
        assertEquals("Persian:1404", yearFormatter.format(1404, DigitMode.Persian))
        assertEquals("MO", weekdayFormatter.format(DayOfWeek.MONDAY))
    }

    private class FakeCalendarResourceProvider(
        private val arrays: Map<Int, List<String>> = emptyMap(),
    ) : CalendarResourceProvider {
        override fun color(id: Int, fallback: Long) = androidx.compose.ui.graphics.Color(fallback)
        override fun string(id: Int, fallback: String): String = fallback
        override fun stringArray(id: Int, fallback: List<String>): List<String> = arrays[id] ?: fallback
    }
}
