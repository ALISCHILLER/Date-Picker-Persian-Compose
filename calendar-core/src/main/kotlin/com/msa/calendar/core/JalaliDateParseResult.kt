package com.msa.calendar.core

/** Detailed result returned by [JalaliDate.parse]. */
public sealed interface JalaliDateParseResult {
    /** Parsing succeeded and produced a validated Jalali date. */
    public data class Success(public val date: JalaliDate) : JalaliDateParseResult

    /** Parsing failed with a stable, machine-readable [error]. */
    public data class Failure(public val error: JalaliDateParseError) : JalaliDateParseResult
}

/** Stable parse errors for user input and validation feedback. */
public enum class JalaliDateParseError {
    EmptyInput,
    InvalidFormat,
    InvalidYear,
    UnsupportedYear,
    InvalidMonth,
    InvalidDay,
}
