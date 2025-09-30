package com.msa.calendar.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.time.DayOfWeek
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

/**
 * Represents the locale configuration that the calendar should honour when rendering content.
 */
data class CalendarLocaleConfiguration(
    val locale: Locale,
    val calendarSystem: CalendarSystem,
    val weekStart: DayOfWeek,
    val weekendDays: Set<DayOfWeek>,
    val isRtl: Boolean,
) {
    companion object {
        fun persian(): CalendarLocaleConfiguration = CalendarLocaleConfiguration(
            locale = Locale("fa"),
            calendarSystem = CalendarSystem.Persian,
            weekStart = DayOfWeek.SATURDAY,
            weekendDays = setOf(DayOfWeek.FRIDAY),
            isRtl = true,
        )

        fun english(): CalendarLocaleConfiguration = CalendarLocaleConfiguration(
            locale = Locale("en"),
            calendarSystem = CalendarSystem.Gregorian,
            weekStart = DayOfWeek.MONDAY,
            weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            isRtl = false,
        )

        fun from(locale: Locale): CalendarLocaleConfiguration {
            val language = locale.language.lowercase(Locale.ROOT)
            return when (language) {
                "fa", "ar", "ckb", "ps" -> persian().copy(locale = locale)
                else -> english().copy(locale = locale)
            }
        }
    }
}

/**
 * Handles locale overrides and supplies correctly configured contexts for resolving resources.
 */
object CalendarLocalization {
    private val overrideRef = AtomicReference<CalendarLocaleConfiguration?>(null)

    fun override(configuration: CalendarLocaleConfiguration?) {
        overrideRef.set(configuration)
    }

    fun current(): CalendarLocaleConfiguration = overrideRef.get() ?: inferFromSystem()

    fun inferFromSystem(): CalendarLocaleConfiguration {
        val context = CalendarResources.contextOrNull()
        val locale = when {
            context != null -> context.resources.configuration.let { configuration ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    configuration.locale
                }
            }

            else -> Locale.getDefault()
        }
        return CalendarLocaleConfiguration.from(locale)
    }

    internal fun resolveContext(base: Context): Context {
        val configuration = Configuration(base.resources.configuration)
        val locale = current().locale
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return base.createConfigurationContext(configuration)
    }
}