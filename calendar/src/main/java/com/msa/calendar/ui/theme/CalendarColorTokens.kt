package com.msa.calendar.ui.theme

import androidx.compose.ui.graphics.Color
import com.msa.calendar.R
import com.msa.calendar.utils.CalendarResourceResolver

object CalendarColorTokens {
    val Violet: Color
        get() = CalendarResourceResolver.color(R.color.ocean_blue, 0xFF4C1D95)

    val Teal: Color
        get() = CalendarResourceResolver.color(R.color.sky_teal, 0xFF0EA5E9)

    val MidSheen: Color = Color(0xFFA5E3FF)

    val BaseDark: Color
        get() = CalendarResourceResolver.color(R.color.midnight, 0xFF020617)

    val BaseLight: Color
        get() = CalendarResourceResolver.color(R.color.cloud, 0xFFF8FAFC)

    val TextPrimary: Color
        get() = CalendarResourceResolver.color(R.color.slate, 0xFF101828)

    val TextMuted: Color
        get() = CalendarResourceResolver.color(R.color.mist, 0xFF94A3B8)

    val Weekend: Color
        get() = CalendarResourceResolver.color(R.color.sunset_orange, 0xFFFB7185)

    val Gold: Color
        get() = CalendarResourceResolver.color(R.color.sand_gold, 0xFFFACC15)

    val Success: Color
        get() = CalendarResourceResolver.color(R.color.fern, 0xFF16A34A)

    val Danger: Color
        get() = CalendarResourceResolver.color(R.color.coral, 0xFFDC2626)
}