package com.msa.calendar.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

/**
 * Legacy-compatible wrapper around [PersianCalendarEngine] that keeps the original API surface
 * while delegating every calculation to the pure engine.
 */
open class PersionCalendar {

    // Safe defaults prevent access before the calendar is initialised.
    private var year: Int = 0
    private var month: Int = 1
    private var day: Int = 1

    /** Creates an instance representing "today" in the supplied [zoneId]. */
    constructor(zoneId: ZoneId = ZoneId.systemDefault()) {
        val (y, m, d) = PersianCalendarEngine.today(zoneId)
        year = y
        month = m
        day = d
    }

    constructor(year: Int, month: Int, day: Int) {
        set(year, month, day)
    }

    constructor(gc: GregorianCalendar) {
        fromGregorian(gc)
    }

    constructor(ld: LocalDate) {
        setFromLocalDate(ld)
    }

    constructor(date: Date) {
        val instant = date.toInstant()
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        setFromLocalDate(localDate)
    }

    fun toGregorian(): GregorianCalendar {
        val local = PersianCalendarEngine.toGregorian(year, month, day)
        return GregorianCalendar.from(local.atStartOfDay(ZoneId.of("UTC")))
    }

    fun fromGregorian(gc: GregorianCalendar) {
        val localDate = LocalDate.of(
            gc.get(GregorianCalendar.YEAR),
            gc.get(GregorianCalendar.MONTH) + 1,
            gc.get(GregorianCalendar.DAY_OF_MONTH),
        )
        setFromLocalDate(localDate)
    }

    fun getYesterday(): PersionCalendar = getDateByDiff(-1)

    fun getTomorrow(): PersionCalendar = getDateByDiff(1)

    /**
     * Returns the shifted date, or the current date when the requested value is outside the
     * supported Persian-calendar range. Use [getDateByDiffOrNull] when the boundary must be
     * observable by the caller.
     */
    fun getDateByDiff(diff: Int): PersionCalendar {
        return getDateByDiffOrNull(diff) ?: PersionCalendar(year, month, day)
    }

    /** Returns `null` when shifting would leave [PersianCalendarLimits.supportedYears]. */
    fun getDateByDiffOrNull(diff: Int): PersionCalendar? = runCatching {
        val local = PersianCalendarEngine.toGregorian(year, month, day).plusDays(diff.toLong())
        val (y, m, d) = PersianCalendarEngine.fromGregorian(local)
        PersionCalendar(y, m, d)
    }.getOrNull()

    fun getDayOfWeek(): Int = dayOfWeek()

    fun getFirstDayOfWeek(): Int = 0

    fun dayOfWeek(): Int {
        val dow = PersianCalendarEngine.dayOfWeek(year, month, day)
        return dow.indexRelativeTo(DayOfWeek.SATURDAY)
    }

    fun getDayOfWeekString(): String {
        val dayOfWeek = PersianCalendarEngine.dayOfWeek(year, month, day)
        return CalendarTextRepository.persianWeekdayFull(dayOfWeek)
    }

    fun getMonthString(): String = CalendarTextRepository.persianMonthName(month)

    fun getDayOfWeekDayMonthString(): String {
        return "${getDayOfWeekString()} $day ${getMonthString()}"
    }

    fun isLeap(): Boolean = PersianCalendarEngine.isLeapYear(year)

    fun getYearLength(): Int = if (isLeap()) 366 else 365

    fun getMonthLength(): Int = PersianCalendarEngine.monthLength(year, month)

    fun getDay(): Int = day

    fun getMonth(): Int = month

    fun getYear(): Int = year

    fun setMonth(month: Int) {
        require(month in 1..12) { "Month should be between 1 and 12 $month ." }
        this.month = month
        val maxDay = PersianCalendarEngine.monthLength(year, month)
        if (day > maxDay) {
            day = maxDay
        }
    }

    fun setYear(year: Int) {
        PersianCalendarLimits.requireSupportedYear(year)
        this.year = year
        val maxDay = PersianCalendarEngine.monthLength(year, month)
        if (day > maxDay) {
            day = maxDay
        }
    }

    fun setDay(day: Int) {
        val maxDay = PersianCalendarEngine.monthLength(year, month)
        require(day in 1..maxDay) { "Day should be between 1 and $maxDay." }
        this.day = day
    }

    fun set(year: Int, month: Int, day: Int) {
        PersianCalendarLimits.requireSupportedYear(year)
        require(month in 1..12) { "Month should be between 1 and 12 but was $month." }
        val maxDay = PersianCalendarEngine.monthLength(year, month)
        require(day in 1..maxDay) { "Day should be between 1 and $maxDay." }

        // Mutate only after all arguments have been validated so a failed call cannot corrupt the
        // previously valid instance.
        this.year = year
        this.month = month
        this.day = day
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersionCalendar) return false
        return year == other.year && month == other.month && day == other.day
    }

    override fun hashCode(): Int = 31 * (31 * year + month) + day

    override fun toString(): String = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month, day)

    private fun setFromLocalDate(localDate: LocalDate) {
        val (y, m, d) = PersianCalendarEngine.fromGregorian(localDate)
        year = y
        month = m
        day = d
    }

}
