package com.msa.calendar

import com.msa.calendar.utils.SoleimaniDate

internal data class RangeSelectionUpdate(
    val startDate: SoleimaniDate?,
    val endDate: SoleimaniDate?,
    val selectedDay: Int?,
)

internal fun resolveRangeSelection(
    candidate: SoleimaniDate,
    currentStart: SoleimaniDate?,
    currentEnd: SoleimaniDate?,
): RangeSelectionUpdate = when {
    currentStart == null || currentEnd != null -> RangeSelectionUpdate(
        startDate = candidate,
        endDate = null,
        selectedDay = candidate.day,
    )
    candidate < currentStart -> RangeSelectionUpdate(
        startDate = candidate,
        endDate = null,
        selectedDay = candidate.day,
    )
    else -> RangeSelectionUpdate(
        startDate = currentStart,
        endDate = candidate,
        selectedDay = candidate.day,
    )
}
