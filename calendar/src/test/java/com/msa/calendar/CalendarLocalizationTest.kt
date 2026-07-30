package com.msa.calendar

import androidx.compose.ui.unit.LayoutDirection
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.defaultDigitMode
import com.msa.calendar.ui.toWeekConfiguration
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.util.Locale

class CalendarLocalizationTest {

    @Test
    fun persianLocaleUsesPersianCalendarAndRtlLayout() {
        val config = CalendarLocaleConfiguration.from(Locale("fa", "IR"))

        assertEquals("fa", config.locale.language)
        assertEquals(CalendarSystem.Persian, config.calendarSystem)
        assertTrue(config.isRtl)
    }

    @Test
    fun nonPersianLocalesUseEnglishLanguageForPersianCalendarByDefault() {
        val config = CalendarLocaleConfiguration.from(Locale("ar"))

        assertEquals("en", config.locale.language)
        assertEquals(CalendarSystem.Persian, config.calendarSystem)
        assertEquals(DayOfWeek.SATURDAY, config.weekStart)
        assertEquals(setOf(DayOfWeek.FRIDAY), config.weekendDays)
        assertFalse(config.isRtl)
    }

    @Test
    fun englishLocaleUsesLatinDigitsLtrLayoutAndEnglishWeekdayLabels() {
        val config = CalendarLocaleConfiguration.english()
        val weekConfiguration = config.toWeekConfiguration()

        assertEquals(CalendarSystem.Persian, config.calendarSystem)
        assertEquals(DigitMode.Latin, config.defaultDigitMode())
        assertEquals(LayoutDirection.Ltr, weekConfiguration.layoutDirection)
        assertEquals("Sa", weekConfiguration.dayLabelFormatter.format(DayOfWeek.SATURDAY))
    }
}
