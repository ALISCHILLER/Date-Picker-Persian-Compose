package com.msa.calendar

import com.msa.calendar.ui.DatePickerMotionSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DatePickerMotionSpecTest {

    @Test
    fun disabledMotionResolvesEveryDurationToZero() {
        val spec = DatePickerMotionSpec(
            enabled = false,
            modeTransitionMillis = 120,
            monthTransitionMillis = 170,
            fadeMillis = 90,
        )

        assertEquals(0, spec.resolvedModeDuration())
        assertEquals(0, spec.resolvedMonthDuration())
        assertEquals(0, spec.resolvedFadeDuration())
    }

    @Test
    fun negativeDurationsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            DatePickerMotionSpec(modeTransitionMillis = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DatePickerMotionSpec(monthTransitionMillis = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DatePickerMotionSpec(fadeMillis = -1)
        }
    }
}
