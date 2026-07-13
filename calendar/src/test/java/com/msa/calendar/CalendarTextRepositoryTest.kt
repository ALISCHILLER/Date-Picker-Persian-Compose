package com.msa.calendar

import androidx.compose.ui.graphics.Color
import com.msa.calendar.utils.CalendarResourceProvider
import com.msa.calendar.utils.CalendarTextRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class CalendarTextRepositoryTest {

    @Test
    fun repositoryUsesFallbackLabelsWhenProviderHasNoOverrides() {
        val provider = FakeCalendarResourceProvider()

        assertEquals("فروردین", CalendarTextRepository.persianMonthName(1, provider))
        assertEquals("اسفند", CalendarTextRepository.persianMonthName(12, provider))
        assertEquals("ش", CalendarTextRepository.persianWeekdayShort(DayOfWeek.SATURDAY, provider))
        assertEquals("ج", CalendarTextRepository.persianWeekdayShort(DayOfWeek.FRIDAY, provider))
        assertEquals("Mo", CalendarTextRepository.latinWeekdayShort(DayOfWeek.MONDAY, provider))
        assertEquals("Su", CalendarTextRepository.latinWeekdayShort(DayOfWeek.SUNDAY, provider))
    }

    @Test
    fun repositoryUsesScopedProviderArraysWhenAvailable() {
        val provider = FakeCalendarResourceProvider(
            arrays = mapOf(
                R.array.persian_months to List(12) { index -> "P${index + 1}" },
                R.array.gregorian_months_en to List(12) { index -> "G${index + 1}" },
                R.array.latin_weekdays_short to listOf("M*", "T*", "W*", "R*", "F*", "S*", "U*"),
            )
        )

        assertEquals("P7", CalendarTextRepository.persianMonthName(7, provider))
        assertEquals("G12", CalendarTextRepository.gregorianMonthNamesEn(provider)[11])
        assertEquals("M*", CalendarTextRepository.latinWeekdayShort(DayOfWeek.MONDAY, provider))
        assertEquals("U*", CalendarTextRepository.latinWeekdayShort(DayOfWeek.SUNDAY, provider))
    }

    @Test
    fun repositoryFallsBackWhenScopedProviderReturnsInvalidArraySizes() {
        val provider = FakeCalendarResourceProvider(
            arrays = mapOf(
                R.array.persian_months to listOf("Broken"),
                R.array.persian_weekdays_short to listOf("X"),
                R.array.latin_weekdays_short to listOf("Y"),
            )
        )

        assertEquals("فروردین", CalendarTextRepository.persianMonthName(1, provider))
        assertEquals("ش", CalendarTextRepository.persianWeekdayShort(DayOfWeek.SATURDAY, provider))
        assertEquals("Mo", CalendarTextRepository.latinWeekdayShort(DayOfWeek.MONDAY, provider))
    }

    private class FakeCalendarResourceProvider(
        private val arrays: Map<Int, List<String>> = emptyMap(),
    ) : CalendarResourceProvider {
        override fun color(id: Int, fallback: Long): Color = Color(fallback)
        override fun string(id: Int, fallback: String): String = fallback
        override fun stringArray(id: Int, fallback: List<String>): List<String> = arrays[id] ?: fallback
    }
}
