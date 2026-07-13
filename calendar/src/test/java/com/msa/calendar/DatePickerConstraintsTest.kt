package com.msa.calendar

import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DatePickerConstraintsTest {

    @Test
    fun constructorRejectsInvalidBoundsAndRangeLength() {
        assertThrows(IllegalArgumentException::class.java) {
            DatePickerConstraints(
                minDate = SoleimaniDate(1404, 2, 1),
                maxDate = SoleimaniDate(1404, 1, 1),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            DatePickerConstraints(maxRangeLength = 0)
        }
    }

    @Test
    fun clampKeepsDateInsideBounds() {
        val constraints = DatePickerConstraints(
            minDate = SoleimaniDate(1404, 1, 10),
            maxDate = SoleimaniDate(1404, 1, 20),
        )

        assertEquals(SoleimaniDate(1404, 1, 10), constraints.clamp(SoleimaniDate(1404, 1, 1)))
        assertEquals(SoleimaniDate(1404, 1, 15), constraints.clamp(SoleimaniDate(1404, 1, 15)))
        assertEquals(SoleimaniDate(1404, 1, 20), constraints.clamp(SoleimaniDate(1404, 2, 1)))
    }

    @Test
    fun selectabilityChecksBoundsDisabledDatesAndCustomValidator() {
        val constraints = DatePickerConstraints(
            minDate = SoleimaniDate(1404, 1, 10),
            maxDate = SoleimaniDate(1404, 1, 20),
            disabledDates = setOf(SoleimaniDate(1404, 1, 13)),
            dateValidator = { date -> date.day % 2 == 0 },
        )

        assertFalse(constraints.isDateSelectable(SoleimaniDate(1404, 1, 9)))
        assertFalse(constraints.isDateSelectable(SoleimaniDate(1404, 1, 21)))
        assertFalse(constraints.isDateSelectable(SoleimaniDate(1404, 1, 13)))
        assertFalse(constraints.isDateSelectable(SoleimaniDate(1404, 1, 15)))
        assertTrue(constraints.isDateSelectable(SoleimaniDate(1404, 1, 16)))
    }

    @Test
    fun nearestValidFindsForwardCandidateBeforeBackwardCandidateWhenEquallyClose() {
        val constraints = DatePickerConstraints(
            minDate = SoleimaniDate(1404, 1, 9),
            maxDate = SoleimaniDate(1404, 1, 11),
            disabledDates = setOf(SoleimaniDate(1404, 1, 10)),
        )

        assertEquals(
            SoleimaniDate(1404, 1, 11),
            constraints.nearestValidOrNull(SoleimaniDate(1404, 1, 10)),
        )
    }

    @Test
    fun nearestValidReturnsNullWhenNoCandidateExistsInsideBounds() {
        val constraints = DatePickerConstraints(
            minDate = SoleimaniDate(1404, 1, 1),
            maxDate = SoleimaniDate(1404, 1, 3),
            dateValidator = { false },
        )

        assertNull(constraints.nearestValidOrNull(SoleimaniDate(1404, 1, 2)))
    }

    @Test
    fun rangeLimitIsInclusiveAndIndependentFromSelectionOrder() {
        val constraints = DatePickerConstraints(maxRangeLength = 3)

        assertTrue(
            constraints.isRangeWithinLimit(
                SoleimaniDate(1404, 1, 1),
                SoleimaniDate(1404, 1, 3),
            )
        )
        assertTrue(
            constraints.isRangeWithinLimit(
                SoleimaniDate(1404, 1, 3),
                SoleimaniDate(1404, 1, 1),
            )
        )
        assertFalse(
            constraints.isRangeWithinLimit(
                SoleimaniDate(1404, 1, 1),
                SoleimaniDate(1404, 1, 4),
            )
        )
    }
    @Test
    fun entireRangeValidationRejectsDisabledInteriorDate() {
        val unavailable = SoleimaniDate(1404, 1, 2)
        val constraints = DatePickerConstraints(
            disabledDates = setOf(unavailable),
        )

        assertEquals(
            unavailable,
            constraints.firstUnavailableDateInRange(
                SoleimaniDate(1404, 1, 1),
                SoleimaniDate(1404, 1, 3),
            ),
        )
        assertFalse(
            constraints.isRangeSelectable(
                SoleimaniDate(1404, 1, 1),
                SoleimaniDate(1404, 1, 3),
            ),
        )
    }

    @Test
    fun customValidatorIsAppliedToInteriorRangeDates() {
        val constraints = DatePickerConstraints(
            dateValidator = { date -> date.day != 2 },
        )

        assertFalse(
            constraints.isRangeSelectable(
                SoleimaniDate(1404, 1, 1),
                SoleimaniDate(1404, 1, 3),
            ),
        )
    }

    @Test
    fun firstUnavailableDateUsesChronologicalOrderAcrossAllRules() {
        val constraints = DatePickerConstraints(
            disabledDates = setOf(SoleimaniDate(1404, 1, 4)),
            dateValidator = { date -> date != SoleimaniDate(1404, 1, 2) },
        )

        assertEquals(
            SoleimaniDate(1404, 1, 2),
            constraints.firstUnavailableDateInRange(
                SoleimaniDate(1404, 1, 1),
                SoleimaniDate(1404, 1, 5),
            ),
        )
    }

    @Test
    fun endpointsOnlyModePreservesLegacyRangeBehaviour() {
        val constraints = DatePickerConstraints(
            disabledDates = setOf(SoleimaniDate(1404, 1, 2)),
            rangeValidationMode = com.msa.calendar.ui.RangeValidationMode.EndpointsOnly,
        )

        assertTrue(
            constraints.isRangeSelectable(
                SoleimaniDate(1404, 1, 1),
                SoleimaniDate(1404, 1, 3),
            ),
        )
    }

}
