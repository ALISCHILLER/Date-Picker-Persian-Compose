package com.msa.calendar.utils

import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

internal object CalendarResourceResolver {

    fun provider(): CalendarResourceProvider {
        return CalendarResources.providerOrNull()
            ?: CalendarResources.contextOrNull()?.let { AndroidCalendarResourceProvider(it) }
            ?: FallbackCalendarResourceProvider
    }

    fun color(@ColorRes id: Int, fallback: Long): Color = provider().color(id, fallback)

    fun stringArray(@ArrayRes id: Int, fallback: List<String>): List<String> =
        provider().stringArray(id, fallback)

    fun string(@StringRes id: Int, fallback: String): String = provider().string(id, fallback)
}
