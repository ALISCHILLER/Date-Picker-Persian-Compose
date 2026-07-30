#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK_DIR="${TMPDIR:-/tmp}/persian-calendar-core-verification"
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR/classes" "$WORK_DIR/smoke" "$WORK_DIR/java"

KOTLINC_VERSION="$(kotlinc -version 2>&1 | head -n 1)"
JAVAC_VERSION="$(javac -version 2>&1)"

mapfile -t SOURCES < <(find "$ROOT_DIR/calendar-core/src/main/kotlin" -name '*.kt' -type f | sort)
kotlinc -jvm-target 17 "${SOURCES[@]}" -d "$WORK_DIR/classes"

cat > "$WORK_DIR/CoreVerification.kt" <<'KOTLIN'
import com.msa.calendar.core.CalendarDigits
import com.msa.calendar.core.JalaliCalendar
import com.msa.calendar.core.JalaliDate
import com.msa.calendar.core.JalaliDateRange
import com.msa.calendar.core.JalaliCalendarLimits
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

fun main() {
    val references = listOf(
        JalaliDate(1403, 1, 1) to LocalDate.of(2024, 3, 20),
        JalaliDate(1404, 1, 1) to LocalDate.of(2025, 3, 21),
        JalaliDate(1399, 12, 30) to LocalDate.of(2021, 3, 20),
    )
    references.forEach { (jalali, gregorian) ->
        check(jalali.toGregorian() == gregorian)
        check(JalaliDate.fromGregorian(gregorian) == jalali)
    }

    var roundTrips = 0
    for (year in JalaliCalendarLimits.supportedYears) {
        for (month in 1..12) {
            val maximumDay = JalaliCalendar.monthLength(year, month)
            for (day in listOf(1, maximumDay)) {
                val original = JalaliDate(year, month, day)
                check(JalaliDate.fromGregorian(original.toGregorian()) == original)
                roundTrips += 1
            }
        }
    }

    val transitions = listOf(
        JalaliDate(1399, 12, 30) to JalaliDate(1400, 1, 1),
        JalaliDate(1400, 12, 29) to JalaliDate(1401, 1, 1),
        JalaliDate(1404, 6, 31) to JalaliDate(1404, 7, 1),
        JalaliDate(1404, 12, 29) to JalaliDate(1405, 1, 1),
    )
    transitions.forEach { (before, after) ->
        check(before.plusDays(1) == after)
        check(after.minusDays(1) == before)
        check(before.daysUntil(after) == 1L)
    }

    check(CalendarDigits.toLatin("۱۴۰۴-۰۱-۰۹") == "1404-01-09")
    check(CalendarDigits.toLatin("١٤٠٤-٠١-٠٩") == "1404-01-09")
    check(JalaliDate.parseOrNull("۱۴۰۴-۰۱-۰۹") == JalaliDate(1404, 1, 9))
    check(JalaliDate.parse("1404/01/09") is com.msa.calendar.core.JalaliDateParseResult.Failure)
    check(
        (JalaliDate.parse("9999999999-01-01") as com.msa.calendar.core.JalaliDateParseResult.Failure).error ==
            com.msa.calendar.core.JalaliDateParseError.InvalidYear,
    )
    check(
        (JalaliDate.parse("1404-13-01") as com.msa.calendar.core.JalaliDateParseResult.Failure).error ==
            com.msa.calendar.core.JalaliDateParseError.InvalidMonth,
    )
    val fixedClock = Clock.fixed(Instant.parse("2025-03-21T00:00:00Z"), ZoneOffset.UTC)
    check(JalaliDate.today(fixedClock) == JalaliDate(1404, 1, 1))
    val boundary = JalaliDate(JalaliCalendarLimits.MIN_YEAR, 1, 1)
    check(JalaliDate.parseOrNull(boundary.toString()) == boundary)
    check(
        JalaliDateRange.of(JalaliDate(1404, 1, 3), JalaliDate(1404, 1, 1))
            .asSequence()
            .count() == 3,
    )

    println("Standalone core verification passed: $roundTrips round trips")
}
KOTLIN

kotlinc -jvm-target 17 -cp "$WORK_DIR/classes" "$WORK_DIR/CoreVerification.kt" -d "$WORK_DIR/smoke"
kotlin -cp "$WORK_DIR/classes:$WORK_DIR/smoke" CoreVerificationKt


cat > "$WORK_DIR/JavaVerification.java" <<'JAVA'
import com.msa.calendar.core.CalendarDigits;
import com.msa.calendar.core.JalaliCalendar;
import com.msa.calendar.core.JalaliDate;
import com.msa.calendar.core.JalaliDateRange;
import com.msa.calendar.core.JalaliDateParseResult;
import java.time.LocalDate;

public final class JavaVerification {
    public static void main(String[] args) {
        JalaliDate date = new JalaliDate(1404, 1, 1);
        if (!JalaliCalendar.toGregorian(date).equals(LocalDate.of(2025, 3, 21))) {
            throw new AssertionError("Java Gregorian conversion failed");
        }
        if (!JalaliDate.fromGregorian(LocalDate.of(2025, 3, 21)).equals(date)) {
            throw new AssertionError("Java Jalali conversion failed");
        }
        JalaliDateRange range = JalaliDateRange.of(
            new JalaliDate(1404, 1, 5),
            new JalaliDate(1404, 1, 1)
        );
        if (range.getLengthInDays() != 5L) {
            throw new AssertionError("Java range API failed");
        }
        JalaliDateParseResult result = JalaliDate.parse("1404-01-01");
        if (!(result instanceof JalaliDateParseResult.Success)) {
            throw new AssertionError("Java detailed parse API failed");
        }
        if (!CalendarDigits.toLatin("۱۴۰۴").equals("1404")) {
            throw new AssertionError("Java digit normalization failed");
        }
        System.out.println("Standalone Java interop verification passed");
    }
}
JAVA

KOTLIN_BIN="$(command -v kotlinc)"
KOTLIN_HOME="$(cd "$(dirname "$KOTLIN_BIN")/.." && pwd)"
javac \
  --release 17 \
  -cp "$WORK_DIR/classes:$KOTLIN_HOME/lib/kotlin-stdlib.jar" \
  -d "$WORK_DIR/java" \
  "$WORK_DIR/JavaVerification.java"
kotlin -cp "$WORK_DIR/classes:$WORK_DIR/java" JavaVerification
printf 'Standalone toolchain: %s; %s; JVM target 17\n' "$KOTLINC_VERSION" "$JAVAC_VERSION"
