package com.msa.calendar.core

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Immutable date in the civil Jalali calendar.
 *
 * The type contains no Android or Compose dependency and is safe to use in domain and data layers.
 */
public data class JalaliDate(
    public val year: Int,
    public val month: Int,
    public val day: Int,
) : Comparable<JalaliDate> {

    init {
        JalaliCalendarLimits.requireSupported(year)
        require(month in 1..12) { "Month must be in 1..12 but was $month" }
        val maximumDay = JalaliCalendar.monthLength(year, month)
        require(day in 1..maximumDay) {
            "Day must be in 1..$maximumDay for $year-$month but was $day"
        }
    }

    public val dayOfWeek: DayOfWeek
        get() = toGregorian().dayOfWeek

    public fun toGregorian(): LocalDate = JalaliCalendar.toGregorian(this)

    public fun plusDays(days: Long): JalaliDate =
        JalaliCalendar.fromGregorian(toGregorian().plusDays(days))

    public fun minusDays(days: Long): JalaliDate = plusDays(-days)

    public fun daysUntil(other: JalaliDate): Long =
        other.toGregorian().toEpochDay() - toGregorian().toEpochDay()

    public override fun compareTo(other: JalaliDate): Int = compareValuesBy(
        this,
        other,
        JalaliDate::year,
        JalaliDate::month,
        JalaliDate::day,
    )

    public override fun toString(): String = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month, day)

    public companion object {
        @JvmStatic
        public fun fromGregorian(date: LocalDate): JalaliDate = JalaliCalendar.fromGregorian(date)

        /** Returns today using an injectable clock for deterministic tests. */
        @JvmStatic
        public fun today(clock: Clock): JalaliDate = JalaliCalendar.today(clock)

        @JvmStatic
        @JvmOverloads
        public fun today(zoneId: ZoneId = ZoneId.systemDefault()): JalaliDate =
            JalaliCalendar.today(zoneId)

        /**
         * Parses an ISO-like Jalali date in `yyyy-MM-dd` form and reports a precise failure reason.
         *
         * Persian and Arabic-Indic digits are accepted. A signed year is supported so the complete
         * algorithm range can round-trip through [toString].
         */
        @JvmStatic
        public fun parse(value: String): JalaliDateParseResult {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) {
                return JalaliDateParseResult.Failure(JalaliDateParseError.EmptyInput)
            }

            val normalized = CalendarDigits.toLatin(trimmed)
            val match = DatePattern.matchEntire(normalized)
                ?: return JalaliDateParseResult.Failure(JalaliDateParseError.InvalidFormat)
            val year = match.groupValues[1].toIntOrNull()
                ?: return JalaliDateParseResult.Failure(JalaliDateParseError.InvalidYear)
            val month = match.groupValues[2].toIntOrNull()
                ?: return JalaliDateParseResult.Failure(JalaliDateParseError.InvalidMonth)
            val day = match.groupValues[3].toIntOrNull()
                ?: return JalaliDateParseResult.Failure(JalaliDateParseError.InvalidDay)

            if (!JalaliCalendarLimits.isSupported(year)) {
                return JalaliDateParseResult.Failure(JalaliDateParseError.UnsupportedYear)
            }
            if (month !in 1..12) {
                return JalaliDateParseResult.Failure(JalaliDateParseError.InvalidMonth)
            }
            if (day !in 1..JalaliCalendar.monthLength(year, month)) {
                return JalaliDateParseResult.Failure(JalaliDateParseError.InvalidDay)
            }
            return JalaliDateParseResult.Success(JalaliDate(year, month, day))
        }

        /** Nullable compatibility helper. Prefer [parse] when validation feedback is needed. */
        @JvmStatic
        public fun parseOrNull(value: String): JalaliDate? = when (val result = parse(value)) {
            is JalaliDateParseResult.Success -> result.date
            is JalaliDateParseResult.Failure -> null
        }

        private val DatePattern: Regex = Regex("^([+-]?[0-9]{1,10})-([0-9]{1,2})-([0-9]{1,2})$")
    }
}
