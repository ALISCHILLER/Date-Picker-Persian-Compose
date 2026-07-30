package com.msa.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.rememberLocalizedDatePickerConfig
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.SoleimaniDate

/**
 * Preferred typed API for showing a single Persian date picker dialog.
 *
 * When [config] is omitted, resources and locale are resolved from the current composition instead
 * of a process-wide global resource override.
 */
@Composable
public fun PersianDatePickerDialog(
    onClose: (DatePickerCloseReason) -> Unit,
    onSelectionConfirmed: (SingleDateSelection) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: SoleimaniDate? = null,
    config: DatePickerConfig? = null,
    localeConfiguration: CalendarLocaleConfiguration? = null,
): Unit {
    val effectiveConfig = config ?: rememberLocalizedDatePickerConfig(
        localeConfiguration = localeConfiguration,
    )
    CalendarScreen(
        onDismiss = {},
        onConfirm = {},
        modifier = modifier,
        initialDate = initialDate,
        config = effectiveConfig,
        onSelectionConfirmed = onSelectionConfirmed,
        onClose = onClose,
    )
}

/**
 * Stateful overload that exposes a hoisted [SingleDatePickerState].
 *
 * Create the state with [rememberSingleDatePickerState]. Its constraints become the effective validation rules for the dialog.
 */
@Composable
public fun PersianDatePickerDialog(
    state: SingleDatePickerState,
    onClose: (DatePickerCloseReason) -> Unit,
    onSelectionConfirmed: (SingleDateSelection) -> Unit,
    modifier: Modifier = Modifier,
    config: DatePickerConfig? = null,
    localeConfiguration: CalendarLocaleConfiguration? = null,
): Unit {
    val localizedConfig = config ?: rememberLocalizedDatePickerConfig(
        localeConfiguration = localeConfiguration,
    )
    val effectiveConfig = localizedConfig.copy(constraints = state.constraints)
    CalendarScreen(
        onDismiss = {},
        onConfirm = {},
        modifier = modifier,
        initialDate = state.selectedDate,
        config = effectiveConfig,
        onSelectionConfirmed = { selection ->
            state.dispatch(SingleDatePickerEvent.Select(selection.date))
            onSelectionConfirmed(selection)
        },
        onClose = onClose,
    )
}

/**
 * Preferred typed API for showing a Persian date range picker dialog.
 *
 * The emitted [DateRangeSelection] is always ordered from earlier to later and also contains the
 * legacy map payload for integrations that still need it during migration.
 */
@Composable
public fun PersianDateRangePickerDialog(
    onClose: (DatePickerCloseReason) -> Unit,
    onSelectionConfirmed: (DateRangeSelection) -> Unit,
    modifier: Modifier = Modifier,
    initialStartDate: SoleimaniDate? = null,
    initialEndDate: SoleimaniDate? = null,
    config: DatePickerConfig? = null,
    localeConfiguration: CalendarLocaleConfiguration? = null,
): Unit {
    val effectiveConfig = config ?: rememberLocalizedDatePickerConfig(
        localeConfiguration = localeConfiguration,
    )
    RangeCalendarScreen(
        onDismiss = {},
        setDate = {},
        modifier = modifier,
        initialStartDate = initialStartDate,
        initialEndDate = initialEndDate,
        config = effectiveConfig,
        onSelectionConfirmed = onSelectionConfirmed,
        onClose = onClose,
    )
}

/** Stateful range overload using unidirectional events and a saveable state holder. */
@Composable
public fun PersianDateRangePickerDialog(
    state: DateRangePickerState,
    onClose: (DatePickerCloseReason) -> Unit,
    onSelectionConfirmed: (DateRangeSelection) -> Unit,
    modifier: Modifier = Modifier,
    config: DatePickerConfig? = null,
    localeConfiguration: CalendarLocaleConfiguration? = null,
): Unit {
    val localizedConfig = config ?: rememberLocalizedDatePickerConfig(
        localeConfiguration = localeConfiguration,
    )
    val effectiveConfig = localizedConfig.copy(constraints = state.constraints)
    RangeCalendarScreen(
        onDismiss = {},
        setDate = {},
        modifier = modifier,
        initialStartDate = state.startDate,
        initialEndDate = state.endDate,
        config = effectiveConfig,
        onSelectionConfirmed = { selection ->
            state.dispatch(DateRangePickerEvent.Replace(selection.startDate, selection.endDate))
            onSelectionConfirmed(selection)
        },
        onClose = onClose,
    )
}
