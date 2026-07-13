package com.msa.calendar

import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersianCalendarEngine
import com.msa.calendar.utils.PersianCalendarLimits
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero

/**
 * Small presentation-level helpers that keep Compose state transitions deterministic and testable.
 */
internal data class VisibleCalendarMonth(
    val year: Int,
    val month: Int,
) {
    init {
        PersianCalendarLimits.requireSupportedYear(year)
        require(month in 1..12) { "month must be in 1..12 but was $month" }
    }

    fun canMovePrevious(yearRange: IntRange): Boolean =
        year > yearRange.first || (year == yearRange.first && month > 1)

    fun canMoveNext(yearRange: IntRange): Boolean =
        year < yearRange.last || (year == yearRange.last && month < 12)

    fun previousOrNull(yearRange: IntRange): VisibleCalendarMonth? {
        if (!canMovePrevious(yearRange)) return null
        return if (month == 1) copy(year = year - 1, month = 12) else copy(month = month - 1)
    }

    fun nextOrNull(yearRange: IntRange): VisibleCalendarMonth? {
        if (!canMoveNext(yearRange)) return null
        return if (month == 12) copy(year = year + 1, month = 1) else copy(month = month + 1)
    }

    /** Legacy internal helper, now safely bounded by the calendar engine. */
    fun previous(): VisibleCalendarMonth =
        previousOrNull(PersianCalendarLimits.supportedYears) ?: this

    /** Legacy internal helper, now safely bounded by the calendar engine. */
    fun next(): VisibleCalendarMonth =
        nextOrNull(PersianCalendarLimits.supportedYears) ?: this
}

internal data class RangePickerDraft(
    val startDate: SoleimaniDate?,
    val endDate: SoleimaniDate?,
    val visibleMonth: VisibleCalendarMonth,
    val pendingDay: Int?,
)

internal fun effectivePickerYearRange(
    configuredRange: IntRange,
    constraints: DatePickerConstraints,
): IntRange {
    require(!configuredRange.isEmpty()) { "yearRange must not be empty" }
    val first = maxOf(
        configuredRange.first,
        PersianCalendarLimits.MIN_SUPPORTED_YEAR,
        constraints.minDate?.year ?: PersianCalendarLimits.MIN_SUPPORTED_YEAR,
    )
    val last = minOf(
        configuredRange.last,
        PersianCalendarLimits.MAX_SUPPORTED_YEAR,
        constraints.maxDate?.year ?: PersianCalendarLimits.MAX_SUPPORTED_YEAR,
    )
    require(first <= last) {
        "yearRange and date constraints do not contain any common selectable year"
    }
    return first..last
}

private fun DatePickerConstraints.restrictedTo(yearRange: IntRange): DatePickerConstraints {
    val firstDate = SoleimaniDate(yearRange.first, 1, 1)
    val lastDate = SoleimaniDate(
        year = yearRange.last,
        month = 12,
        day = PersianCalendarEngine.monthLength(yearRange.last, 12),
    )
    return copy(
        minDate = minDate?.let { maxOf(it, firstDate) } ?: firstDate,
        maxDate = maxDate?.let { minOf(it, lastDate) } ?: lastDate,
    )
}

private fun SoleimaniDate.coerceToYearRange(yearRange: IntRange): SoleimaniDate {
    val coercedYear = year.coerceIn(yearRange)
    if (coercedYear == year) return this
    val maxDay = PersianCalendarEngine.monthLength(coercedYear, month)
    return SoleimaniDate(coercedYear, month, day.coerceAtMost(maxDay))
}

internal fun resolveSelectableInitialDateOrNull(
    initialDate: SoleimaniDate?,
    todayDate: SoleimaniDate,
    constraints: DatePickerConstraints,
    yearRange: IntRange = PersianCalendarLimits.supportedYears,
): SoleimaniDate? {
    val effectiveRange = effectivePickerYearRange(yearRange, constraints)
    val boundedConstraints = constraints.restrictedTo(effectiveRange)
    val fallback = (initialDate ?: todayDate).coerceToYearRange(effectiveRange)
    return boundedConstraints.nearestValidOrNull(fallback)
}

/**
 * Resolves the date used to open the picker. It prefers a selectable date, but still returns a
 * valid visible anchor when the current constraints intentionally make every nearby date disabled.
 * Call [resolveSelectableInitialDateOrNull] when an actual selection is required.
 */
internal fun resolveSingleInitialDate(
    initialDate: SoleimaniDate?,
    todayDate: SoleimaniDate,
    constraints: DatePickerConstraints,
    yearRange: IntRange = PersianCalendarLimits.supportedYears,
): SoleimaniDate {
    resolveSelectableInitialDateOrNull(
        initialDate = initialDate,
        todayDate = todayDate,
        constraints = constraints,
        yearRange = yearRange,
    )?.let { return it }

    val effectiveRange = effectivePickerYearRange(yearRange, constraints)
    val boundedConstraints = constraints.restrictedTo(effectiveRange)
    val fallback = (initialDate ?: todayDate).coerceToYearRange(effectiveRange)
    return boundedConstraints.clamp(fallback)
}

internal fun resolveRangePickerDraft(
    initialStartDate: SoleimaniDate?,
    initialEndDate: SoleimaniDate?,
    todayDate: SoleimaniDate,
    constraints: DatePickerConstraints,
    yearRange: IntRange = PersianCalendarLimits.supportedYears,
): RangePickerDraft {
    val effectiveRange = effectivePickerYearRange(yearRange, constraints)
    val boundedConstraints = constraints.restrictedTo(effectiveRange)
    val visibleAnchor = resolveSingleInitialDate(
        initialDate = initialStartDate,
        todayDate = todayDate,
        constraints = boundedConstraints,
        yearRange = effectiveRange,
    )
    val resolvedStart = resolveSelectableInitialDateOrNull(
        initialDate = initialStartDate,
        todayDate = todayDate,
        constraints = boundedConstraints,
        yearRange = effectiveRange,
    )
    val resolvedEnd = if (resolvedStart == null) {
        null
    } else {
        initialEndDate
            ?.coerceToYearRange(effectiveRange)
            ?.let(boundedConstraints::nearestValidOrNull)
            ?.takeIf { boundedConstraints.isRangeSelectable(resolvedStart, it) }
    }
    val ordered = orderedRangeOrNull(resolvedStart, resolvedEnd)

    val start = ordered?.first ?: resolvedStart
    val end = ordered?.second
    val displayedDate = start ?: visibleAnchor
    return RangePickerDraft(
        startDate = start,
        endDate = end,
        visibleMonth = VisibleCalendarMonth(
            year = displayedDate.year,
            month = displayedDate.month,
        ),
        pendingDay = start?.day,
    )
}

internal fun orderedRangeOrNull(
    startDate: SoleimaniDate?,
    endDate: SoleimaniDate?,
): Pair<SoleimaniDate, SoleimaniDate>? {
    val start = startDate ?: return null
    val end = endDate ?: return null
    return if (start <= end) start to end else end to start
}

internal fun isCompleteSelectableRange(
    startDate: SoleimaniDate?,
    endDate: SoleimaniDate?,
    constraints: DatePickerConstraints,
): Boolean {
    val ordered = orderedRangeOrNull(startDate, endDate) ?: return false
    return constraints.isRangeSelectable(ordered.first, ordered.second)
}

internal fun canSelectRangeCandidate(
    candidate: SoleimaniDate,
    currentStart: SoleimaniDate?,
    currentEnd: SoleimaniDate?,
    constraints: DatePickerConstraints,
): Boolean {
    if (!constraints.isDateSelectable(candidate)) return false
    if (currentStart == null || currentEnd != null || candidate < currentStart) return true
    return constraints.isRangeSelectable(currentStart, candidate)
}

/**
 * Computes the enabled state of all dates in a visible month with at most one forward range scan.
 * This avoids repeating the same custom-validator work for every one of the 42 calendar cells.
 */
internal fun rangeCandidateAvailabilityForMonth(
    year: Int,
    month: Int,
    currentStart: SoleimaniDate?,
    currentEnd: SoleimaniDate?,
    constraints: DatePickerConstraints,
): Map<SoleimaniDate, Boolean> {
    val candidates = (1..PersianCalendarEngine.monthLength(year, month))
        .map { day -> SoleimaniDate(year, month, day) }

    if (currentStart == null || currentEnd != null) {
        return candidates.associateWith(constraints::isDateSelectable)
    }

    val latestForwardCandidate = candidates.lastOrNull { it >= currentStart }
    val firstUnavailable = latestForwardCandidate?.let { lastCandidate ->
        constraints.firstUnavailableDateInRange(currentStart, lastCandidate)
    }

    return candidates.associateWith { candidate ->
        when {
            !constraints.isDateSelectable(candidate) -> false
            candidate < currentStart -> true
            !constraints.isRangeWithinLimit(currentStart, candidate) -> false
            firstUnavailable != null && candidate >= firstUnavailable -> false
            else -> true
        }
    }
}
internal fun formatDayNumber(day: Int, digitMode: DigitMode): String = when (digitMode) {
    DigitMode.Persian -> FormatHelper.toPersianNumber(day.toString())
    DigitMode.Latin -> day.toString()
}

internal fun formatDateSlash(date: SoleimaniDate, digitMode: DigitMode): String {
    val yearText = when (digitMode) {
        DigitMode.Persian -> FormatHelper.toPersianNumber(date.year.toString())
        DigitMode.Latin -> date.year.toString()
    }
    val monthText = when (digitMode) {
        DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(date.month))
        DigitMode.Latin -> addLeadingZero(date.month)
    }
    val dayText = when (digitMode) {
        DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(date.day))
        DigitMode.Latin -> addLeadingZero(date.day)
    }
    return "$yearText/$monthText/$dayText"
}

internal fun buildSingleSelectionLabel(
    date: SoleimaniDate?,
    monthLabel: String,
    yearLabel: String,
    digitMode: DigitMode,
): String? = date?.let {
    "${formatDayNumber(it.day, digitMode)} $monthLabel $yearLabel"
}

internal fun buildDayCellContentDescription(
    date: SoleimaniDate?,
    digitMode: DigitMode,
    strings: DatePickerStrings,
    eventLabel: String? = null,
    isSelected: Boolean = false,
    isToday: Boolean = false,
    isEnabled: Boolean = true,
    isRangeStart: Boolean = false,
    isRangeEnd: Boolean = false,
    isWithinRange: Boolean = false,
): String {
    if (date == null) return strings.emptyDay
    val states = buildList {
        if (isSelected) add(strings.selectedState.lowercase())
        if (isRangeStart) add(strings.rangeStartState.lowercase())
        if (isRangeEnd) add(strings.rangeEndState.lowercase())
        if (isWithinRange && !isRangeStart && !isRangeEnd) add(strings.insideSelectedRangeState.lowercase())
        if (isToday) add(strings.todayState.lowercase())
        if (!isEnabled) add(strings.disabledState.lowercase())
        eventLabel?.takeIf(String::isNotBlank)?.let { label ->
            add("${strings.eventPrefix}: $label")
        }
    }
    return buildString {
        append(formatDateSlash(date, digitMode))
        if (states.isNotEmpty()) {
            append(", ")
            append(states.joinToString())
        }
    }
}

internal fun buildDayCellStateDescription(
    strings: DatePickerStrings,
    isSelected: Boolean = false,
    isRangeStart: Boolean = false,
    isRangeEnd: Boolean = false,
    isWithinRange: Boolean = false,
    isToday: Boolean = false,
    isEnabled: Boolean = true,
): String = buildList {
    if (isSelected) add(strings.selectedState)
    if (isRangeStart) add(strings.rangeStartState)
    if (isRangeEnd) add(strings.rangeEndState)
    if (isWithinRange && !isRangeStart && !isRangeEnd) add(strings.insideSelectedRangeState)
    if (isToday) add(strings.todayState)
    if (!isEnabled) add(strings.disabledState)
}.joinToString().ifBlank { strings.availableState }
