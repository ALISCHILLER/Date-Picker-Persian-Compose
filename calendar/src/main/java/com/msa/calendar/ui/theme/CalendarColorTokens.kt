package com.msa.calendar.ui.theme

import androidx.compose.ui.graphics.Color
import com.msa.calendar.R
import com.msa.calendar.utils.CalendarResourceResolver

object CalendarColorTokens {
    val gradientStart: Color
        get() = CalendarResourceResolver.color(R.color.ocean_blue, 0xFF4C1D95)

    val gradientEnd: Color
        get() = CalendarResourceResolver.color(R.color.sky_teal, 0xFF22D3EE)

    val accentOrange: Color
        get() = CalendarResourceResolver.color(R.color.sunset_orange, 0xFFFB7185)

    val accentGold: Color
        get() = CalendarResourceResolver.color(R.color.sand_gold, 0xFFFACC15)

    val baseDark: Color
        get() = CalendarResourceResolver.color(R.color.midnight, 0xFF020617)

    val baseLight: Color
        get() = CalendarResourceResolver.color(R.color.cloud, 0xFFF8FAFC)

    val textPrimary: Color
        get() = CalendarResourceResolver.color(R.color.slate, 0xFF101828)

    val textMuted: Color
        get() = CalendarResourceResolver.color(R.color.mist, 0xFF94A3B8)

    val success: Color
        get() = CalendarResourceResolver.color(R.color.fern, 0xFF16A34A)

    val danger: Color
        get() = CalendarResourceResolver.color(R.color.coral, 0xFFDC2626)
}