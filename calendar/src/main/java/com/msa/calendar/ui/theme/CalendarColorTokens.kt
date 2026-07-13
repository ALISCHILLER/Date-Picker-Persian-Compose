package com.msa.calendar.ui.theme

import androidx.compose.ui.graphics.Color
import com.msa.calendar.R
import com.msa.calendar.utils.CalendarResourceResolver

object CalendarColorTokens {
    val Violet: Color
        get() = CalendarResourceResolver.color(R.color.ocean_blue, 0xFF3F5BF6)

    val Teal: Color
        get() = CalendarResourceResolver.color(R.color.sky_teal, 0xFF0EA5A4)

    val MidSheen: Color = Color(0xFF8BE9FD)

    val BaseDark: Color
        get() = CalendarResourceResolver.color(R.color.midnight, 0xFF0A1020)

    val BaseLight: Color
        get() = CalendarResourceResolver.color(R.color.cloud, 0xFFF7F9FC)

    val TextPrimary: Color
        get() = CalendarResourceResolver.color(R.color.slate, 0xFF172033)

    val TextMuted: Color
        get() = CalendarResourceResolver.color(R.color.mist, 0xFF667085)

    val Weekend: Color
        get() = CalendarResourceResolver.color(R.color.sunset_orange, 0xFFE5484D)

    val Gold: Color
        get() = CalendarResourceResolver.color(R.color.sand_gold, 0xFFF2B93B)

    val Success: Color
        get() = CalendarResourceResolver.color(R.color.fern, 0xFF17A673)

    val Danger: Color
        get() = CalendarResourceResolver.color(R.color.coral, 0xFFD92D20)
}
