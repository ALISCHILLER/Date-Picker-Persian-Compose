package com.msa.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.msa.calendar.utils.AndroidCalendarResourceProvider
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarLocalization
import com.msa.calendar.utils.CalendarResourceProvider
import com.msa.calendar.utils.CalendarSystem

/**
 * Builds a resource provider scoped to the current composition and optional locale configuration.
 */
@Composable
fun rememberCalendarResourceProvider(
    localeConfiguration: CalendarLocaleConfiguration? = null,
): CalendarResourceProvider {
    val context = LocalContext.current
    val inferredConfiguration = remember(context) { CalendarLocalization.inferFrom(context) }
    val resolvedConfiguration = localeConfiguration ?: inferredConfiguration
    return remember(context, resolvedConfiguration) {
        AndroidCalendarResourceProvider(context, resolvedConfiguration)
    }
}

/**
 * Creates a fully localized date picker config without using process-wide resource overrides.
 */
fun localizedDatePickerConfig(
    resourceProvider: CalendarResourceProvider,
    localeConfiguration: CalendarLocaleConfiguration,
): DatePickerConfig {
    val digitMode = localeConfiguration.defaultDigitMode()
    val strings = DatePickerStrings.localized(resourceProvider)
    val monthFormatter = when (localeConfiguration.calendarSystem) {
        CalendarSystem.Persian -> when (digitMode) {
            DigitMode.Persian -> MonthFormatter.persian(resourceProvider)
            DigitMode.Latin -> MonthFormatter.persianWithLatinTransliteration(resourceProvider)
        }
        CalendarSystem.Gregorian -> MonthFormatter.gregorian(resourceProvider)
    }
    return DatePickerConfig(
        strings = strings,
        colors = DatePickerDefaults.colors(resourceProvider),
        digitMode = digitMode,
        weekConfiguration = localeConfiguration.toWeekConfiguration(provider = resourceProvider),
        monthFormatter = monthFormatter,
        yearFormatter = if (localeConfiguration.calendarSystem == CalendarSystem.Persian) {
            YearFormatter.WithGregorianHint
        } else {
            YearFormatter.Default
        },
        rangeFormatter = DateRangeFormatter { start, end ->
            "$start ${strings.rangeSeparator} $end"
        },
    )
}

/**
 * Remembers a localized default config and optionally lets callers make final adjustments.
 */
@Composable
fun rememberLocalizedDatePickerConfig(
    localeConfiguration: CalendarLocaleConfiguration? = null,
    customize: (DatePickerConfig) -> DatePickerConfig = { it },
): DatePickerConfig {
    val context = LocalContext.current
    val inferredConfiguration = remember(context) { CalendarLocalization.inferFrom(context) }
    val resolvedConfiguration = localeConfiguration ?: inferredConfiguration
    val provider = rememberCalendarResourceProvider(resolvedConfiguration)
    return remember(provider, resolvedConfiguration, customize) {
        customize(
            localizedDatePickerConfig(
                resourceProvider = provider,
                localeConfiguration = resolvedConfiguration,
            )
        )
    }
}
