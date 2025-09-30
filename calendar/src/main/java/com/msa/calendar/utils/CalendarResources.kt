package com.msa.calendar.utils

import android.content.Context
import java.util.concurrent.atomic.AtomicReference

/**
 * Stores an application [Context] so calendar components can access resources in a testable way.
 */
object CalendarResources {
    private val applicationContextRef = AtomicReference<Context?>()

    /**
     * Provides the calendar module with an application [Context] for resolving resources.
     */
    fun initialize(context: Context) {
        applicationContextRef.set(context.applicationContext)
    }

    internal fun contextOrNull(): Context? = applicationContextRef.get()
}