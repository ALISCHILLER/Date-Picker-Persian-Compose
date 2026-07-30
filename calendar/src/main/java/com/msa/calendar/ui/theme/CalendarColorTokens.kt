package com.msa.calendar.ui.theme

import androidx.compose.ui.graphics.Color
import com.msa.calendar.R
import com.msa.calendar.utils.CalendarResourceResolver

object CalendarColorTokens {
    val Violet: Color
        get() = CalendarResourceResolver.color(R.color.ocean_blue, 0xFF6D5EF5)

    val Teal: Color
        get() = CalendarResourceResolver.color(R.color.sky_teal, 0xFF14B8A6)

    val MidSheen: Color = Color(0xFF8BE9FD)

    val BaseDark: Color
        get() = CalendarResourceResolver.color(R.color.midnight, 0xFF090B16)

    val BaseLight: Color
        get() = CalendarResourceResolver.color(R.color.cloud, 0xFFF7F7FC)

    val TextPrimary: Color
        get() = CalendarResourceResolver.color(R.color.slate, 0xFF191B2B)

    val TextMuted: Color
        get() = CalendarResourceResolver.color(R.color.mist, 0xFF667085)

    val Weekend: Color
        get() = CalendarResourceResolver.color(R.color.sunset_orange, 0xFFF04438)

    val Gold: Color
        get() = CalendarResourceResolver.color(R.color.sand_gold, 0xFFF4B740)

    val Success: Color
        get() = CalendarResourceResolver.color(R.color.fern, 0xFF12B76A)

    val Danger: Color
        get() = CalendarResourceResolver.color(R.color.coral, 0xFFD92D20)
}
