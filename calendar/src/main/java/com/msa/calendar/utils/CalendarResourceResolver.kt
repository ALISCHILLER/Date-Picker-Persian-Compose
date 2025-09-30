package com.msa.calendar.utils

import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

internal object CalendarResourceResolver {

    fun color(@ColorRes id: Int, fallback: Long): Color {
        val context = CalendarResources.contextOrNull()?.let(CalendarLocalization::resolveContext)
        return if (context != null) {
            Color(ContextCompat.getColor(context, id))
        } else {
            Color(fallback)
        }
    }

    fun stringArray(@ArrayRes id: Int, fallback: List<String>): List<String> {
        val context = CalendarResources.contextOrNull()?.let(CalendarLocalization::resolveContext)
        return if (context != null) {
            context.resources.getStringArray(id).toList()
        } else {
            fallback
        }
    }

    fun string(@StringRes id: Int, fallback: String): String {
        val context = CalendarResources.contextOrNull()?.let(CalendarLocalization::resolveContext)
        return if (context != null) {
            context.getString(id)
        } else {
            fallback
        }
    }
}