package com.msa.calendar

import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.FallbackCalendarResourceProvider
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GregorianDateLabelsTest {

    @Test
    fun persianMonthHintShowsBothOverlappingGregorianMonths() {
        assertEquals(
            "مارس – آوریل ۲۰۲۵",
            gregorianMonthSpanLabel(
                persianYear = 1404,
                persianMonth = 1,
                digitMode = DigitMode.Persian,
                provider = FallbackCalendarResourceProvider,
            ),
        )
    }

    @Test
    fun monthHintShowsBothGregorianYearsWhenPersianMonthCrossesNewYear() {
        assertEquals(
            "دسامبر ۲۰۲۵ – ژانویه ۲۰۲۶",
            gregorianMonthSpanLabel(
                persianYear = 1404,
                persianMonth = 10,
                digitMode = DigitMode.Persian,
                provider = FallbackCalendarResourceProvider,
            ),
        )
    }

    @Test
    fun monthPickerHintCanOmitYearForCompactCards() {
        assertEquals(
            "March – April",
            gregorianMonthSpanLabel(
                persianYear = 1404,
                persianMonth = 1,
                digitMode = DigitMode.Latin,
                provider = FallbackCalendarResourceProvider,
                includeYear = false,
            ),
        )
    }

    @Test
    fun exactGregorianDateUsesConfiguredDigitMode() {
        val date = SoleimaniDate(1404, 1, 1)

        assertEquals(
            "۲۱ مارس ۲۰۲۵",
            gregorianDateLabel(
                persianDate = date,
                digitMode = DigitMode.Persian,
                provider = FallbackCalendarResourceProvider,
            ),
        )
        assertEquals(
            "March 21, 2025",
            gregorianDateLabel(
                persianDate = date,
                digitMode = DigitMode.Latin,
                provider = FallbackCalendarResourceProvider,
            ),
        )
    }
}
