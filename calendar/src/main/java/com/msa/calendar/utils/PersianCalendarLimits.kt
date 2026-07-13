package com.msa.calendar.utils

/**
 * Public limits of the deterministic Persian calendar engine used by this library.
 *
 * Keeping the limits in one place prevents the picker UI, date model and legacy calendar API
 * from accepting years that the conversion engine cannot represent.
 */
object PersianCalendarLimits {
    const val MIN_SUPPORTED_YEAR: Int = -61
    const val MAX_SUPPORTED_YEAR: Int = 3177

    val supportedYears: IntRange = MIN_SUPPORTED_YEAR..MAX_SUPPORTED_YEAR

    fun isSupportedYear(year: Int): Boolean = year in supportedYears

    fun requireSupportedYear(year: Int) {
        require(isSupportedYear(year)) {
            "Persian year must be in $MIN_SUPPORTED_YEAR..$MAX_SUPPORTED_YEAR but was $year"
        }
    }
}
