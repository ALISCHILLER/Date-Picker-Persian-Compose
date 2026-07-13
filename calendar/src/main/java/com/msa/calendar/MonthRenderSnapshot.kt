package com.msa.calendar

import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.utils.PersianCalendarEngine
import com.msa.calendar.utils.SoleimaniDate

/**
 * Compact render data for one visible Persian month.
 *
 * The picker renders at most 31 real day cells. Boolean/array lookups avoid allocating maps and
 * repeatedly hashing [SoleimaniDate] during recomposition while preserving the public callback API.
 */
internal class MonthRenderSnapshot private constructor(
    val year: Int,
    val month: Int,
    val monthLength: Int,
    private val enabledByDay: BooleanArray,
    private val eventsByDay: Array<CalendarEvent?>,
) {
    fun isEnabled(date: SoleimaniDate): Boolean =
        date.year == year &&
            date.month == month &&
            date.day in 1..monthLength &&
            enabledByDay[date.day]

    fun event(date: SoleimaniDate): CalendarEvent? =
        if (date.year == year && date.month == month && date.day in 1..monthLength) {
            eventsByDay[date.day]
        } else {
            null
        }

    fun isEnabled(day: Int): Boolean = day in 1..monthLength && enabledByDay[day]

    fun event(day: Int): CalendarEvent? =
        if (day in 1..monthLength) eventsByDay[day] else null

    companion object {
        fun single(
            year: Int,
            month: Int,
            constraints: DatePickerConstraints,
            eventIndicator: (SoleimaniDate) -> CalendarEvent?,
        ): MonthRenderSnapshot {
            val monthLength = PersianCalendarEngine.monthLength(year, month)
            val enabled = BooleanArray(monthLength + 1)
            val events = arrayOfNulls<CalendarEvent>(monthLength + 1)

            for (day in 1..monthLength) {
                val date = SoleimaniDate(year, month, day)
                enabled[day] = constraints.isDateSelectable(date)
                events[day] = eventIndicator(date)
            }

            return MonthRenderSnapshot(
                year = year,
                month = month,
                monthLength = monthLength,
                enabledByDay = enabled,
                eventsByDay = events,
            )
        }

        fun range(
            year: Int,
            month: Int,
            currentStart: SoleimaniDate?,
            currentEnd: SoleimaniDate?,
            constraints: DatePickerConstraints,
            eventIndicator: (SoleimaniDate) -> CalendarEvent?,
        ): MonthRenderSnapshot {
            val monthLength = PersianCalendarEngine.monthLength(year, month)
            val enabled = rangeCandidateAvailabilityArrayForMonth(
                year = year,
                month = month,
                currentStart = currentStart,
                currentEnd = currentEnd,
                constraints = constraints,
            )
            val events = arrayOfNulls<CalendarEvent>(monthLength + 1)

            for (day in 1..monthLength) {
                events[day] = eventIndicator(SoleimaniDate(year, month, day))
            }

            return MonthRenderSnapshot(
                year = year,
                month = month,
                monthLength = monthLength,
                enabledByDay = enabled,
                eventsByDay = events,
            )
        }
    }
}

/**
 * Array-backed equivalent of [rangeCandidateAvailabilityForMonth]. Index 0 is intentionally unused
 * so callers can address the array directly with a calendar day number.
 */
internal fun rangeCandidateAvailabilityArrayForMonth(
    year: Int,
    month: Int,
    currentStart: SoleimaniDate?,
    currentEnd: SoleimaniDate?,
    constraints: DatePickerConstraints,
): BooleanArray {
    val monthLength = PersianCalendarEngine.monthLength(year, month)
    val availability = BooleanArray(monthLength + 1)

    if (currentStart == null || currentEnd != null) {
        for (day in 1..monthLength) {
            availability[day] = constraints.isDateSelectable(SoleimaniDate(year, month, day))
        }
        return availability
    }

    val lastCandidate = SoleimaniDate(year, month, monthLength)
    val firstUnavailable = if (lastCandidate >= currentStart) {
        constraints.firstUnavailableDateInRange(currentStart, lastCandidate)
    } else {
        null
    }

    for (day in 1..monthLength) {
        val candidate = SoleimaniDate(year, month, day)
        availability[day] = when {
            !constraints.isDateSelectable(candidate) -> false
            candidate < currentStart -> true
            !constraints.isRangeWithinLimit(currentStart, candidate) -> false
            firstUnavailable != null && candidate >= firstUnavailable -> false
            else -> true
        }
    }

    return availability
}
