package com.msa.calendar.utils

/** Converts ASCII digits in an integer to Persian digits. */
fun Int.toPersianNumber(): String = FormatHelper.toPersianNumber(toString())

/** Shared digit formatter used by public formatters and UI labels. */
object FormatHelper {
    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianNumber(text: String): String {
        if (text.isEmpty()) return ""

        return buildString(capacity = text.length) {
            text.forEach { char ->
                when (char) {
                    in '0'..'9' -> append(persianDigits[char.digitToInt()])
                    '٫' -> append('،')
                    else -> append(char)
                }
            }
        }
    }
}
