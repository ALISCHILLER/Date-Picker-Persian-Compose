package com.msa.calendar.utils

/**
 * Represents a single date in the Jalali (Persian) calendar.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int,
) : Comparable<JalaliDate> {

    init {
        require(month in 1..12) { "Month must be in 1..12 but was $month" }
        val maxDayInMonth = monthLength(year, month)
        require(day in 1..maxDayInMonth) {
            "Day must be in 1..$maxDayInMonth for month $month of year $year but was $day"
        }
    }

    override fun compareTo(other: JalaliDate): Int = compareValuesBy(this, other, JalaliDate::year, JalaliDate::month, JalaliDate::day)

    /**
     * Converts this date to a [PersionCalendar] instance.
     */
    fun toCalendar(): PersionCalendar = PersionCalendar(year, month, day)

    /**
     * Creates a map that mirrors the previous public contract of the range picker.
     * @param usePersianDigits whether to format numbers using [toPersianNumber].
     */
    fun toMap(usePersianDigits: Boolean = true): Map<String, String> {
        fun Int.format() = if (usePersianDigits) toPersianNumber() else toString()
        return mapOf(
            "day" to day.format(),
            "month" to month.format(),
            "year" to year.format(),
        )
    }

    companion object {
        /**
         * Parses a [JalaliDate] from three localized strings. The input strings may contain Persian digits.
         */
        fun fromLocalizedStrings(year: String, month: String, day: String): JalaliDate? {
            val yearValue = year.toIntSafely() ?: return null
            val monthValue = month.toIntSafely() ?: return null
            val dayValue = day.toIntSafely() ?: return null
            return runCatching { JalaliDate(yearValue, monthValue, dayValue) }.getOrNull()
        }

        private fun monthLength(year: Int, month: Int): Int = PersionCalendar(year, month, 1).getMonthLength()
    }
}

/**
 * Converts the current [PersionCalendar] date into a strongly typed [JalaliDate].
 */
fun PersionCalendar.toJalaliDate(): JalaliDate = JalaliDate(getYear(), getMonth(), getDay())