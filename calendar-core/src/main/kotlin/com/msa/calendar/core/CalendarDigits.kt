package com.msa.calendar.core

/** Locale-independent digit conversion helpers for Persian calendar input and output. */
public object CalendarDigits {
    private const val PERSIAN_ZERO: Char = '۰'
    private const val ARABIC_ZERO: Char = '٠'

    @JvmStatic
    public fun toPersian(value: CharSequence): String = buildString(value.length) {
        value.forEach { character ->
            append(
                when (character) {
                    in '0'..'9' -> (PERSIAN_ZERO.code + character.digitToInt()).toChar()
                    else -> character
                },
            )
        }
    }

    @JvmStatic
    public fun toLatin(value: CharSequence): String = buildString(value.length) {
        value.forEach { character ->
            append(
                when (character) {
                    in '۰'..'۹' -> ('0'.code + character.code - PERSIAN_ZERO.code).toChar()
                    in '٠'..'٩' -> ('0'.code + character.code - ARABIC_ZERO.code).toChar()
                    else -> character
                },
            )
        }
    }
}
