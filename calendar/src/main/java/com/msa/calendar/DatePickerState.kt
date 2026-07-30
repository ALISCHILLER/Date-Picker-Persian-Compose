package com.msa.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.msa.calendar.core.JalaliDate
import com.msa.calendar.core.JalaliDateRange
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.utils.SoleimaniDate

/** Outcome of a single-date state mutation. */
public enum class SingleDatePickerUpdate {
    Applied,
    RejectedByConstraints,
    Cleared,
}

/** Outcome of a date-range state mutation. */
public enum class DateRangePickerUpdate {
    Started,
    Completed,
    Restarted,
    RejectedByConstraints,
    Cleared,
}

/** Events accepted by [SingleDatePickerState]. */
public sealed interface SingleDatePickerEvent {
    public data class Select(public val date: SoleimaniDate) : SingleDatePickerEvent
    public data object Clear : SingleDatePickerEvent
}

/**
 * Hoisted state holder for a single-date picker.
 *
 * State moves down to the UI and user events move back through [dispatch]. The class deliberately
 * has no ViewModel or lifecycle dependency, which keeps it suitable for reusable UI libraries.
 * [constraints] is the single source of truth used by both restoration and event validation.
 */
@Stable
public class SingleDatePickerState @JvmOverloads constructor(
    initialSelection: SoleimaniDate? = null,
    public val constraints: DatePickerConstraints = DatePickerConstraints(),
) {
    public var selectedDate: SoleimaniDate? by mutableStateOf(
        initialSelection?.takeIf(constraints::isDateSelectable),
    )
        private set

    /** Android-free representation exposed by the `persian-calendar-core` artifact. */
    public val selectedJalaliDate: JalaliDate?
        get() = selectedDate?.toJalaliDate()

    public val hasSelection: Boolean
        get() = selectedDate != null

    /** Applies [event]. Use [tryDispatch] when the host needs an explicit validation outcome. */
    public fun dispatch(event: SingleDatePickerEvent): Unit {
        tryDispatch(event)
    }

    /** Applies [event] and returns a stable, machine-readable result. */
    public fun tryDispatch(event: SingleDatePickerEvent): SingleDatePickerUpdate = when (event) {
        is SingleDatePickerEvent.Select -> trySelect(event.date)
        SingleDatePickerEvent.Clear -> {
            selectedDate = null
            SingleDatePickerUpdate.Cleared
        }
    }

    /** Selects an Android-free core date without exposing internal UI models to domain code. */
    public fun select(date: JalaliDate): Unit {
        trySelect(SoleimaniDate.from(date))
    }

    /** Attempts to select [date] and reports whether constraints accepted it. */
    public fun trySelect(date: JalaliDate): SingleDatePickerUpdate =
        trySelect(SoleimaniDate.from(date))

    private fun trySelect(date: SoleimaniDate): SingleDatePickerUpdate {
        if (!constraints.isDateSelectable(date)) return SingleDatePickerUpdate.RejectedByConstraints
        selectedDate = date
        return SingleDatePickerUpdate.Applied
    }

    internal companion object {
        private const val MissingDatePart: Int = Int.MIN_VALUE

        fun saver(constraints: DatePickerConstraints): Saver<SingleDatePickerState, Any> = listSaver(
            save = { state ->
                val date = state.selectedDate
                listOf(
                    date?.year ?: MissingDatePart,
                    date?.month ?: MissingDatePart,
                    date?.day ?: MissingDatePart,
                )
            },
            restore = { saved ->
                val date = saved.toNullableDate(MissingDatePart)
                SingleDatePickerState(date, constraints)
            },
        )
    }
}

/** Events accepted by [DateRangePickerState]. */
public sealed interface DateRangePickerEvent {
    public data class Select(public val date: SoleimaniDate) : DateRangePickerEvent
    public data class Replace(
        public val start: SoleimaniDate,
        public val end: SoleimaniDate,
    ) : DateRangePickerEvent
    public data object Clear : DateRangePickerEvent
}

/** Hoisted, saveable state holder for an ordered date range. */
@Stable
public class DateRangePickerState @JvmOverloads constructor(
    initialStart: SoleimaniDate? = null,
    initialEnd: SoleimaniDate? = null,
    public val constraints: DatePickerConstraints = DatePickerConstraints(),
) {
    public var startDate: SoleimaniDate? by mutableStateOf<SoleimaniDate?>(null)
        private set
    public var endDate: SoleimaniDate? by mutableStateOf<SoleimaniDate?>(null)
        private set

    init {
        if (initialStart != null && initialEnd != null) {
            tryReplace(initialStart, initialEnd)
        } else if (initialStart != null && constraints.isDateSelectable(initialStart)) {
            startDate = initialStart
        }
    }

    /** Android-free ordered range when both endpoints are selected. */
    public val selectedJalaliRange: JalaliDateRange?
        get() {
            val start = startDate ?: return null
            val end = endDate ?: return null
            return JalaliDateRange.of(start.toJalaliDate(), end.toJalaliDate())
        }

    public val isComplete: Boolean
        get() = startDate != null && endDate != null

    /** Applies [event]. Use [tryDispatch] when the host needs an explicit validation outcome. */
    public fun dispatch(event: DateRangePickerEvent): Unit {
        tryDispatch(event)
    }

    /** Applies [event] and returns a stable, machine-readable state transition. */
    public fun tryDispatch(event: DateRangePickerEvent): DateRangePickerUpdate = when (event) {
        is DateRangePickerEvent.Select -> trySelect(event.date)
        is DateRangePickerEvent.Replace -> tryReplace(event.start, event.end)
        DateRangePickerEvent.Clear -> clearAndReport()
    }

    /** Applies the next selection event using an Android-free core date. */
    public fun select(date: JalaliDate): Unit {
        trySelect(SoleimaniDate.from(date))
    }

    /** Applies the next core-date selection and reports the transition. */
    public fun trySelect(date: JalaliDate): DateRangePickerUpdate =
        trySelect(SoleimaniDate.from(date))

    /** Replaces the current range using Android-free core dates. */
    public fun replace(start: JalaliDate, end: JalaliDate): Unit {
        tryReplace(SoleimaniDate.from(start), SoleimaniDate.from(end))
    }

    /** Replaces the current range and reports whether constraints accepted it. */
    public fun tryReplace(start: JalaliDate, end: JalaliDate): DateRangePickerUpdate =
        tryReplace(SoleimaniDate.from(start), SoleimaniDate.from(end))

    private fun trySelect(date: SoleimaniDate): DateRangePickerUpdate {
        if (!constraints.isDateSelectable(date)) return DateRangePickerUpdate.RejectedByConstraints
        val currentStart = startDate
        val currentEnd = endDate
        return when {
            currentStart == null -> {
                startDate = date
                endDate = null
                DateRangePickerUpdate.Started
            }
            currentEnd != null -> {
                startDate = date
                endDate = null
                DateRangePickerUpdate.Restarted
            }
            date < currentStart -> {
                startDate = date
                endDate = null
                DateRangePickerUpdate.Restarted
            }
            constraints.isRangeSelectable(currentStart, date) -> {
                endDate = date
                DateRangePickerUpdate.Completed
            }
            else -> DateRangePickerUpdate.RejectedByConstraints
        }
    }

    private fun tryReplace(first: SoleimaniDate, second: SoleimaniDate): DateRangePickerUpdate {
        val orderedStart = minOf(first, second)
        val orderedEnd = maxOf(first, second)
        if (!constraints.isRangeSelectable(orderedStart, orderedEnd)) {
            return DateRangePickerUpdate.RejectedByConstraints
        }
        startDate = orderedStart
        endDate = orderedEnd
        return DateRangePickerUpdate.Completed
    }

    private fun clearAndReport(): DateRangePickerUpdate {
        startDate = null
        endDate = null
        return DateRangePickerUpdate.Cleared
    }

    internal companion object {
        private const val MissingDatePart: Int = Int.MIN_VALUE

        fun saver(constraints: DatePickerConstraints): Saver<DateRangePickerState, Any> = listSaver(
            save = { state ->
                listOf(
                    state.startDate?.year ?: MissingDatePart,
                    state.startDate?.month ?: MissingDatePart,
                    state.startDate?.day ?: MissingDatePart,
                    state.endDate?.year ?: MissingDatePart,
                    state.endDate?.month ?: MissingDatePart,
                    state.endDate?.day ?: MissingDatePart,
                )
            },
            restore = { saved ->
                val start = saved.take(3).toNullableDate(MissingDatePart)
                val end = saved.drop(3).take(3).toNullableDate(MissingDatePart)
                DateRangePickerState(start, end, constraints)
            },
        )
    }
}

/** Creates and saves a [SingleDatePickerState] for the current composition. */
@Composable
public fun rememberSingleDatePickerState(
    initialSelection: SoleimaniDate? = null,
    constraints: DatePickerConstraints = DatePickerConstraints(),
): SingleDatePickerState = rememberSaveable(
    constraints,
    saver = SingleDatePickerState.saver(constraints),
) {
    SingleDatePickerState(initialSelection, constraints)
}

/** Creates and saves a [DateRangePickerState] for the current composition. */
@Composable
public fun rememberDateRangePickerState(
    initialStart: SoleimaniDate? = null,
    initialEnd: SoleimaniDate? = null,
    constraints: DatePickerConstraints = DatePickerConstraints(),
): DateRangePickerState = rememberSaveable(
    constraints,
    saver = DateRangePickerState.saver(constraints),
) {
    DateRangePickerState(initialStart, initialEnd, constraints)
}

private fun List<Any?>.toNullableDate(missingDatePart: Int): SoleimaniDate? {
    if (size < 3) return null
    val year = this[0] as? Int ?: return null
    if (year == missingDatePart) return null
    val month = this[1] as? Int ?: return null
    val day = this[2] as? Int ?: return null
    return runCatching { SoleimaniDate(year, month, day) }.getOrNull()
}
