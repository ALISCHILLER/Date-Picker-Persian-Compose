package com.msa.calendar.core;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import org.junit.Test;

public final class JalaliCalendarJavaInteropTest {
    @Test
    public void staticFactoriesAreUsableFromJava() {
        JalaliDate date = new JalaliDate(1404, 1, 1);

        assertEquals(LocalDate.of(2025, 3, 21), JalaliCalendar.toGregorian(date));
        assertEquals(date, JalaliDate.fromGregorian(LocalDate.of(2025, 3, 21)));
        assertEquals("1404-01-01", CalendarDigits.toLatin("۱۴۰۴-۰۱-۰۱"));
    }

    @Test
    public void rangeFactoryKeepsEndpointsOrdered() {
        JalaliDateRange range = JalaliDateRange.of(
            new JalaliDate(1404, 1, 5),
            new JalaliDate(1404, 1, 1)
        );

        assertEquals(new JalaliDate(1404, 1, 1), range.getStart());
        assertEquals(new JalaliDate(1404, 1, 5), range.getEndInclusive());
        assertEquals(5L, range.getLengthInDays());
    }
}
