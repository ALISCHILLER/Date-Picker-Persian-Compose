package com.msa.calendar.ui.theme

import androidx.compose.ui.graphics.Color
import com.msa.calendar.R
import com.msa.calendar.utils.CalendarResourceResolver

object CalendarColorTokens {
    val gradientStart: Color
        get() = CalendarResourceResolver.color(R.color.ocean_blue, 0xFF0F4C81)

    val gradientEnd: Color
        get() = CalendarResourceResolver.color(R.color.sky_teal, 0xFF38BDF8)

    val accentOrange: Color
        get() = CalendarResourceResolver.color(R.color.sunset_orange, 0xFFF97316)

    val accentGold: Color
        get() = CalendarResourceResolver.color(R.color.sand_gold, 0xFFFACC15)

    val baseDark: Color
        get() = CalendarResourceResolver.color(R.color.midnight, 0xFF0F172A)

    val baseLight: Color
        get() = CalendarResourceResolver.color(R.color.cloud, 0xFFF8FAFC)

    val textPrimary: Color
        get() = CalendarResourceResolver.color(R.color.slate, 0xFF1E293B)

    val textMuted: Color
        get() = CalendarResourceResolver.color(R.color.mist, 0xFFE2E8F0)

    val success: Color
        get() = CalendarResourceResolver.color(R.color.fern, 0xFF22C55E)

    val danger: Color
        get() = CalendarResourceResolver.color(R.color.coral, 0xFFFF6B6B)
}