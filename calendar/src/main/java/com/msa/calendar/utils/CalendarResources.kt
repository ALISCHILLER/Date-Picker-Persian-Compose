package com.msa.calendar.utils

import android.content.Context
import java.util.concurrent.atomic.AtomicReference

/**
 * Legacy global resource entry point for applications that prefer process-wide initialisation.
 *
 * New code should prefer passing a scoped [CalendarResourceProvider] through
 * `rememberLocalizedDatePickerConfig(...)` or the typed dialog wrappers. This object remains for
 * backward compatibility with earlier releases.
 */
object CalendarResources {
    private val applicationContextRef = AtomicReference<Context?>()
    private val providerRef = AtomicReference<CalendarResourceProvider?>()

    /**
     * Provides the calendar module with an application [Context] for resolving resources globally.
     */
    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        applicationContextRef.set(applicationContext)
        providerRef.set(AndroidCalendarResourceProvider(applicationContext))
    }

    /**
     * Provides a custom global resource provider. Prefer scoped providers for new code.
     */
    fun initialize(provider: CalendarResourceProvider) {
        applicationContextRef.set(null)
        providerRef.set(provider)
    }

    /** Clears the legacy global provider. Useful for tests and isolated previews. */
    fun clear() {
        applicationContextRef.set(null)
        providerRef.set(null)
    }

    internal fun contextOrNull(): Context? = applicationContextRef.get()

    internal fun providerOrNull(): CalendarResourceProvider? = providerRef.get()
}
