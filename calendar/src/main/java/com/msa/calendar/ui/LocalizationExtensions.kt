package com.msa.calendar.ui

import androidx.compose.ui.unit.LayoutDirection
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarResourceProvider
import com.msa.calendar.utils.CalendarResourceResolver
import com.msa.calendar.utils.CalendarSystem
import java.util.Locale

fun CalendarLocaleConfiguration.toWeekConfiguration(
    overrideFormatter: WeekdayFormatter? = null,
    overrideLayoutDirection: LayoutDirection? = null,
    provider: CalendarResourceProvider? = null,
): WeekConfiguration {
    val resolvedProvider = provider ?: CalendarResourceResolver.provider()
    val formatter = overrideFormatter ?: when (calendarSystem) {
        CalendarSystem.Persian -> {
            if (locale.language.lowercase(Locale.ROOT) == "fa") {
                WeekdayFormatter.persianShort(resolvedProvider)
            } else {
                WeekdayFormatter.latinShort(resolvedProvider)
            }
        }
        CalendarSystem.Gregorian -> {
            if (locale.language.lowercase(Locale.ROOT) == "fa") {
                WeekdayFormatter.persianGregorian(resolvedProvider)
            } else {
                WeekdayFormatter.latinShort(resolvedProvider)
            }
        }
    }
    val direction = overrideLayoutDirection ?: if (isRtl) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    return WeekConfiguration(
        startDay = weekStart,
        weekendDays = weekendDays,
        dayLabelFormatter = formatter,
        layoutDirection = direction,
    )
}

fun CalendarLocaleConfiguration.defaultDigitMode(): DigitMode =
    if (locale.language.lowercase(Locale.ROOT) == "fa") DigitMode.Persian else DigitMode.Latin
