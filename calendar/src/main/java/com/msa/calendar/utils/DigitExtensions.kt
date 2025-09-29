package com.msa.calendar.utils

import com.msa.calendar.ui.DigitMode

/**
 * Formats an [Int] according to the selected [DigitMode].
 */
fun Int.toDigitString(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> toPersianNumber()
    DigitMode.Latin -> toString()
}

/**
 * Formats an [Int] with leading zeros based on the [DigitMode].
 */
fun Int.toPaddedDigitString(mode: DigitMode): String = when (mode) {
    DigitMode.Persian -> FormatHelper.toPersianNumber(addLeadingZero(this))
    DigitMode.Latin -> addLeadingZero(this)
}