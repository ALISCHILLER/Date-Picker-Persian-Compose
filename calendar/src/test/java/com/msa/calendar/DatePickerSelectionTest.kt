package com.msa.calendar

import com.msa.calendar.ui.DateFormatter
import com.msa.calendar.ui.DateRangeFormatter
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DatePickerSelectionTest {

    @Test
    fun singleSelectionContainsTypedAndLegacyValues() {
        val selection = SingleDateSelection.create(
            date = SoleimaniDate(1404, 1, 1),
            dateFormatter = DateFormatter.Default,
            digitMode = DigitMode.Latin,
        )

        assertEquals(SoleimaniDate(1404, 1, 1), selection.date)
        assertEquals("2025-03-21", selection.gregorianDate.toString())
        assertEquals("1404 / 01 / 01", selection.formattedDate)
        assertEquals(
            mapOf("day" to "1", "month" to "1", "year" to "1404"),
            selection.legacyDateMap,
        )
    }

    @Test
    fun singleSelectionCanKeepPersianDigitLegacyContract() {
        val selection = SingleDateSelection.create(
            date = SoleimaniDate(1404, 12, 29),
            dateFormatter = DateFormatter.Default,
            digitMode = DigitMode.Persian,
        )

        assertEquals("۱۴۰۴ / ۱۲ / ۲۹", selection.formattedDate)
        assertEquals(
            mapOf("day" to "۲۹", "month" to "۱۲", "year" to "۱۴۰۴"),
            selection.legacyDateMap,
        )
    }

    @Test
    fun rangeSelectionOrdersReverseInputAndKeepsBothFormats() {
        val selection = DateRangeSelection.create(
            firstDate = SoleimaniDate(1404, 1, 1),
            secondDate = SoleimaniDate(1403, 12, 30),
            dateFormatter = DateFormatter.Default,
            rangeFormatter = DateRangeFormatter.Default,
            digitMode = DigitMode.Latin,
        )

        assertEquals(SoleimaniDate(1403, 12, 30), selection.startDate)
        assertEquals(SoleimaniDate(1404, 1, 1), selection.endDate)
        assertEquals("2025-03-20", selection.startGregorianDate.toString())
        assertEquals("2025-03-21", selection.endGregorianDate.toString())
        assertEquals(2, selection.daysInclusive)
        assertEquals("1403 / 12 / 30 - 1404 / 01 / 01", selection.formattedRange)
        assertEquals(
            listOf(
                mapOf("day" to "30", "month" to "12", "year" to "1403"),
                mapOf("day" to "1", "month" to "1", "year" to "1404"),
            ),
            selection.legacyDateMaps,
        )
    }

    @Test
    fun rangeSelectionSupportsCustomRangeFormatter() {
        val formatter = DateRangeFormatter { start, end -> "$start تا $end" }

        val selection = DateRangeSelection.create(
            firstDate = SoleimaniDate(1404, 2, 10),
            secondDate = SoleimaniDate(1404, 2, 12),
            dateFormatter = DateFormatter.Default,
            rangeFormatter = formatter,
            digitMode = DigitMode.Latin,
        )

        assertEquals("1404 / 02 / 10 تا 1404 / 02 / 12", selection.formattedRange)
        assertEquals(3, selection.daysInclusive)
    }
}
