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
 * This wrapper keeps callers away from the legacy string callback and exposes one strongly typed
 * result object instead. When [config] is not supplied, resources and locale are resolved from the
 * current composition instead of a process-wide global resource override.
 */
@Composable
fun PersianDatePickerDialog(
    onClose: (DatePickerCloseReason) -> Unit,
    onSelectionConfirmed: (SingleDateSelection) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: SoleimaniDate? = null,
    config: DatePickerConfig? = null,
    localeConfiguration: CalendarLocaleConfiguration? = null,
) {
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
 * Preferred typed API for showing a Persian date range picker dialog.
 *
 * The emitted [DateRangeSelection] is always ordered from earlier to later and also contains the
 * legacy map payload for integrations that still need it during migration.
 */
@Composable
fun PersianDateRangePickerDialog(
    onClose: (DatePickerCloseReason) -> Unit,
    onSelectionConfirmed: (DateRangeSelection) -> Unit,
    modifier: Modifier = Modifier,
    initialStartDate: SoleimaniDate? = null,
    initialEndDate: SoleimaniDate? = null,
    config: DatePickerConfig? = null,
    localeConfiguration: CalendarLocaleConfiguration? = null,
) {
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
