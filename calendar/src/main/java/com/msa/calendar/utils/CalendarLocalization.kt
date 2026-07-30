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
            calendarSystem = CalendarSystem.Persian,
            weekStart = DayOfWeek.SATURDAY,
            weekendDays = setOf(DayOfWeek.FRIDAY),
            isRtl = false,
        )

        fun from(locale: Locale): CalendarLocaleConfiguration {
            val language = locale.language.lowercase(Locale.ROOT)
            return when (language) {
                "fa" -> persian()
                else -> english()
            }
        }
    }
}

/**
 * Handles legacy locale overrides and supplies correctly configured contexts for resolving
 * resources.
 *
 * Prefer passing [CalendarLocaleConfiguration] to scoped configuration helpers for new code. The
 * override API is intentionally kept for existing integrations.
 */
object CalendarLocalization {
    private val overrideRef = AtomicReference<CalendarLocaleConfiguration?>(null)

    fun override(configuration: CalendarLocaleConfiguration?) {
        overrideRef.set(configuration)
    }

    fun clearOverride() {
        overrideRef.set(null)
    }

    fun current(): CalendarLocaleConfiguration = overrideRef.get() ?: inferFromSystem()

    fun inferFromSystem(): CalendarLocaleConfiguration {
        val context = CalendarResources.contextOrNull()
        return if (context != null) {
            inferFrom(context)
        } else {
            CalendarLocaleConfiguration.from(Locale.getDefault())
        }
    }

    fun inferFrom(context: Context): CalendarLocaleConfiguration {
        val locale = context.resources.configuration.primaryLocale()
        return CalendarLocaleConfiguration.from(locale)
    }

    /**
     * Returns a context whose resources and layout direction match the supplied calendar locale.
     * Use this when app/sample UI must switch language together with the picker.
     */
    fun localizedContext(
        base: Context,
        configuration: CalendarLocaleConfiguration? = null,
    ): Context = resolveContext(base, configuration)

    internal fun resolveContext(
        base: Context,
        configuration: CalendarLocaleConfiguration? = null,
    ): Context {
        val localizedConfiguration = configuration
            ?: overrideRef.get()
            ?: inferFrom(base)
        val androidConfiguration = Configuration(base.resources.configuration)
        val locale = localizedConfiguration.locale
        androidConfiguration.setLocale(locale)
        androidConfiguration.setLayoutDirection(locale)
        return base.createConfigurationContext(androidConfiguration)
    }
}

private fun Configuration.primaryLocale(): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    locales.get(0)
} else {
    @Suppress("DEPRECATION")
    locale
}
