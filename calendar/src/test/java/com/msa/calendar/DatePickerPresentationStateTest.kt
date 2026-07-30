package com.msa.calendar

import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatePickerPresentationStateTest {

    @Test
    fun visibleMonthNavigationCrossesYearBoundariesSafely() {
        assertEquals(
            VisibleCalendarMonth(year = 1403, month = 12),
            VisibleCalendarMonth(year = 1404, month = 1).previous(),
        )
        assertEquals(
            VisibleCalendarMonth(year = 1405, month = 1),
            VisibleCalendarMonth(year = 1404, month = 12).next(),
        )
    }

    @Test
    fun singleInitialDateUsesNearestSelectableDate() {
        val constraints = DatePickerConstraints(
            minDate = SoleimaniDate(1404, 1, 10),
            disabledDates = setOf(SoleimaniDate(1404, 1, 10)),
        )

        val resolved = resolveSingleInitialDate(
            initialDate = SoleimaniDate(1404, 1, 1),
            todayDate = SoleimaniDate(1404, 1, 2),
            constraints = constraints,
        )

        assertEquals(SoleimaniDate(1404, 1, 11), resolved)
    }

    @Test
    fun rangeDraftOrdersReverseInitialDates() {
        val draft = resolveRangePickerDraft(
            initialStartDate = SoleimaniDate(1404, 1, 10),
            initialEndDate = SoleimaniDate(1404, 1, 5),
            todayDate = SoleimaniDate(1404, 1, 1),
            constraints = DatePickerConstraints(),
        )

        assertEquals(SoleimaniDate(1404, 1, 5), draft.startDate)
        assertEquals(SoleimaniDate(1404, 1, 10), draft.endDate)
        assertEquals(VisibleCalendarMonth(year = 1404, month = 1), draft.visibleMonth)
        assertEquals(5, draft.pendingDay)
    }

    @Test
    fun rangeCompletenessChecksSelectabilityAndMaximumLength() {
        val constraints = DatePickerConstraints(maxRangeLength = 3)

        assertTrue(
            isCompleteSelectableRange(
                startDate = SoleimaniDate(1404, 1, 1),
                endDate = SoleimaniDate(1404, 1, 3),
                constraints = constraints,
            )
        )
        assertFalse(
            isCompleteSelectableRange(
                startDate = SoleimaniDate(1404, 1, 1),
                endDate = SoleimaniDate(1404, 1, 4),
                constraints = constraints,
            )
        )
    }

    @Test
    fun dayCellContentDescriptionIncludesStateAndEvent() {
        val description = buildDayCellContentDescription(
            date = SoleimaniDate(1404, 1, 1),
            digitMode = DigitMode.Latin,
            strings = englishStrings(),
            eventLabel = "Holiday",
            isSelected = true,
            isToday = true,
            isEnabled = true,
        )

        assertEquals("1404/01/01, selected, today, Event: Holiday", description)
    }

    @Test
    fun dayCellContentDescriptionUsesLocalizedPersianStateLabels() {
        val description = buildDayCellContentDescription(
            date = SoleimaniDate(1404, 1, 1),
            digitMode = DigitMode.Persian,
            strings = persianStrings(),
            eventLabel = "تعطیل",
            isRangeStart = true,
            isEnabled = false,
        )

        assertEquals("۱۴۰۴/۰۱/۰۱, شروع بازه, غیرفعال, رویداد: تعطیل", description)
    }

    private fun englishStrings(): DatePickerStrings = DatePickerStrings.localized()

    private fun persianStrings(): DatePickerStrings = DatePickerStrings.localized().copy(
        emptyDay = "روز خالی",
        selectedState = "انتخاب‌شده",
        rangeStartState = "شروع بازه",
        rangeEndState = "پایان بازه",
        insideSelectedRangeState = "داخل بازه انتخاب‌شده",
        todayState = "امروز",
        disabledState = "غیرفعال",
        availableState = "قابل انتخاب",
        eventPrefix = "رویداد",
    )
    @Test
    fun navigationStopsAtConfiguredYearRangeBoundaries() {
        val range = 1400..1400
        val firstMonth = VisibleCalendarMonth(1400, 1)
        val lastMonth = VisibleCalendarMonth(1400, 12)

        assertFalse(firstMonth.canMovePrevious(range))
        assertEquals(null, firstMonth.previousOrNull(range))
        assertFalse(lastMonth.canMoveNext(range))
        assertEquals(null, lastMonth.nextOrNull(range))
    }

    @Test
    fun initialDateIsClampedToConfiguredYearRange() {
        val resolved = resolveSingleInitialDate(
            initialDate = SoleimaniDate(1390, 12, 29),
            todayDate = SoleimaniDate(1404, 1, 1),
            constraints = DatePickerConstraints(),
            yearRange = 1400..1410,
        )

        assertEquals(SoleimaniDate(1400, 12, 29), resolved)
    }


    @Test
    fun unavailableConfigurationOpensWithoutCreatingAnInvalidSelection() {
        val constraints = DatePickerConstraints(
            minDate = SoleimaniDate(1404, 1, 1),
            maxDate = SoleimaniDate(1404, 1, 3),
            dateValidator = { false },
        )

        assertEquals(
            null,
            resolveSelectableInitialDateOrNull(
                initialDate = SoleimaniDate(1404, 1, 2),
                todayDate = SoleimaniDate(1404, 1, 2),
                constraints = constraints,
                yearRange = 1404..1404,
            ),
        )

        val draft = resolveRangePickerDraft(
            initialStartDate = SoleimaniDate(1404, 1, 2),
            initialEndDate = null,
            todayDate = SoleimaniDate(1404, 1, 2),
            constraints = constraints,
            yearRange = 1404..1404,
        )
        assertEquals(null, draft.startDate)
        assertEquals(null, draft.endDate)
        assertEquals(VisibleCalendarMonth(1404, 1), draft.visibleMonth)
    }

    @Test
    fun rangeCandidateCannotCrossUnavailableInteriorDate() {
        val constraints = DatePickerConstraints(
            disabledDates = setOf(SoleimaniDate(1404, 1, 2)),
        )

        assertFalse(
            canSelectRangeCandidate(
                candidate = SoleimaniDate(1404, 1, 3),
                currentStart = SoleimaniDate(1404, 1, 1),
                currentEnd = null,
                constraints = constraints,
            ),
        )
    }

    @Test
    fun monthlyRangeAvailabilityStopsAtFirstUnavailableDate() {
        val constraints = DatePickerConstraints(
            disabledDates = setOf(SoleimaniDate(1404, 1, 3)),
        )

        val availability = rangeCandidateAvailabilityForMonth(
            year = 1404,
            month = 1,
            currentStart = SoleimaniDate(1404, 1, 1),
            currentEnd = null,
            constraints = constraints,
        )

        assertTrue(availability.getValue(SoleimaniDate(1404, 1, 2)))
        assertFalse(availability.getValue(SoleimaniDate(1404, 1, 3)))
        assertFalse(availability.getValue(SoleimaniDate(1404, 1, 4)))
    }

}
