package com.msa.calendar.core

/** Ordered inclusive Jalali date range. */
public data class JalaliDateRange private constructor(
    public val start: JalaliDate,
    public val endInclusive: JalaliDate,
) {
    init {
        require(start <= endInclusive) { "start must be before or equal to endInclusive" }
    }

    public val lengthInDays: Long
        get() = start.daysUntil(endInclusive) + 1L

    public operator fun contains(date: JalaliDate): Boolean = date in start..endInclusive

    public fun asSequence(): Sequence<JalaliDate> = sequence {
        var current = start
        while (true) {
            yield(current)
            if (current == endInclusive) break
            current = current.plusDays(1)
        }
    }

    public companion object {
        @JvmStatic
        public fun of(first: JalaliDate, second: JalaliDate): JalaliDateRange =
            if (first <= second) JalaliDateRange(first, second) else JalaliDateRange(second, first)
    }
}
