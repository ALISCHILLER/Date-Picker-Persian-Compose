package com.msa.calendar.ui

import androidx.compose.ui.unit.LayoutDirection
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarSystem
import java.util.Locale

fun CalendarLocaleConfiguration.toWeekConfiguration(
    overrideFormatter: WeekdayFormatter? = null,
    overrideLayoutDirection: LayoutDirection? = null,
): WeekConfiguration {
    val formatter = overrideFormatter ?: when (calendarSystem) {
        CalendarSystem.Persian -> WeekdayFormatter.PersianShort
        CalendarSystem.Gregorian -> {
            if (locale.language.lowercase(Locale.ROOT) == "fa") {
                WeekdayFormatter.PersianGregorian
            } else {
                WeekdayFormatter.LatinShort
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
    if (calendarSystem == CalendarSystem.Persian) DigitMode.Persian else DigitMode.Latin