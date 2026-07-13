package com.msa.calendar.utils

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

/**
 * Resolves Android resources for the calendar module.
 *
 * The interface lets consumers provide resources per picker/configuration instead of relying on a
 * single process-wide [CalendarResources] context or [CalendarLocalization] override.
 */
interface CalendarResourceProvider {
    fun color(@ColorRes id: Int, fallback: Long): Color

    fun string(@StringRes id: Int, fallback: String): String

    fun stringArray(@ArrayRes id: Int, fallback: List<String>): List<String>
}

/**
 * Safe provider used by previews, JVM tests and integrations that do not initialise Android
 * resources. It always returns the supplied fallbacks.
 */
object FallbackCalendarResourceProvider : CalendarResourceProvider {
    override fun color(id: Int, fallback: Long): Color = Color(fallback)

    override fun string(id: Int, fallback: String): String = fallback

    override fun stringArray(id: Int, fallback: List<String>): List<String> = fallback
}

/**
 * Android backed implementation that resolves resources from a localized configuration context.
 */
class AndroidCalendarResourceProvider(
    context: Context,
    localeConfiguration: CalendarLocaleConfiguration? = null,
) : CalendarResourceProvider {
    private val applicationContext: Context = context.applicationContext
    private val resolvedLocaleConfiguration: CalendarLocaleConfiguration =
        localeConfiguration ?: CalendarLocalization.inferFrom(context)

    private val localizedContext: Context
        get() = CalendarLocalization.resolveContext(applicationContext, resolvedLocaleConfiguration)

    override fun color(id: Int, fallback: Long): Color = runCatching {
        Color(ContextCompat.getColor(localizedContext, id))
    }.getOrElse {
        Color(fallback)
    }

    override fun string(id: Int, fallback: String): String = runCatching {
        localizedContext.getString(id)
    }.getOrElse {
        fallback
    }

    override fun stringArray(id: Int, fallback: List<String>): List<String> = runCatching {
        localizedContext.resources.getStringArray(id).toList()
    }.getOrElse {
        fallback
    }
}
