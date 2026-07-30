package com.example.coreconsumer;

import com.msa.calendar.core.JalaliCalendar;
import com.msa.calendar.core.JalaliDate;
import com.msa.calendar.core.JalaliDateParseResult;
import java.time.LocalDate;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        JalaliDate date = new JalaliDate(1404, 1, 1);
        LocalDate gregorian = JalaliCalendar.toGregorian(date);
        if (!gregorian.equals(LocalDate.of(2025, 3, 21))) {
            throw new IllegalStateException("Published Core conversion contract failed");
        }

        JalaliDateParseResult result = JalaliDate.parse("۱۴۰۴-۰۱-۰۱");
        if (!(result instanceof JalaliDateParseResult.Success)) {
            throw new IllegalStateException("Published Core parsing contract failed");
        }
    }
}
