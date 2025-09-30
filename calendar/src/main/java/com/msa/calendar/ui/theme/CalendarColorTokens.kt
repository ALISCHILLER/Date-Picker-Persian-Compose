package com.msa.calendar.ui.theme

import androidx.compose.ui.graphics.Color
import com.msa.calendar.R
import com.msa.calendar.utils.CalendarResourceResolver

object CalendarColorTokens {
    val gradientStart: Color
        get() = CalendarResourceResolver.color(R.color.ocean_blue, 0xFF7C3AED)

    val gradientEnd: Color
        get() = CalendarResourceResolver.color(R.color.sky_teal, 0xFF0EA5E9)

    val accentOrange: Color
        get() = CalendarResourceResolver.color(R.color.sunset_orange, 0xFFF59E0B)

    val accentGold: Color
        get() = CalendarResourceResolver.color(R.color.sand_gold, 0xFFFBBF24)

    val baseDark: Color
        get() = CalendarResourceResolver.color(R.color.midnight, 0xFF0B1120)

    val baseLight: Color
        get() = CalendarResourceResolver.color(R.color.cloud, 0xFFF9FAFB)

    val textPrimary: Color
        get() = CalendarResourceResolver.color(R.color.slate, 0xFF0F172A)

    val textMuted: Color
        get() = CalendarResourceResolver.color(R.color.mist, 0xFFE2E8F0)

    val success: Color
        get() = CalendarResourceResolver.color(R.color.fern, 0xFF22C55E)

    val danger: Color
        get() = CalendarResourceResolver.color(R.color.coral, 0xFFEF4444)
}