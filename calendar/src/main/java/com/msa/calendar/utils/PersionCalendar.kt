package com.msa.calendar.utils

import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class PersionCalendar {
   private  var year: Int = 0
   private  var month: Int = 0
    private var day: Int = 0

    /**
     * Today PersionCalendar Date
     */
    constructor() {
        fromGregorian(GregorianCalendar())
    }

    /**
     * Create a PersionCalendar object
     * @param year PersionCalendar Year
     * @param month PersionCalendar Month
     * @param day PersionCalendar Day
     */
    constructor(year: Int, month: Int, day: Int) {
        set(year, month, day)
    }

    /**
     * Create a ir.huri.jcal.PersionCalendar object from gregorian calendar
     * @param gc gregorian calendar object
     */
    constructor(gc: GregorianCalendar) {
        fromGregorian(gc)
    }

    /**
     * Create a PersionCalendar object from Localdate(java 8)
     * @param ld local date object
     */
    constructor(ld: LocalDate) {
        fromGregorian(GregorianCalendar.from(ld.atStartOfDay(ZoneId.systemDefault())))
    }

    /**
     * Create a PersionCalendar object from Date object
     * @param date Date object
     */
    constructor(date: Date) {
        val gc = GregorianCalendar()
        gc.time = date
        fromGregorian(gc)
    }

    /**
     * Convert current PersionCalendar date to gregorian date
     * @return date converted gregorianDate
     */
    fun toGregorian(): GregorianCalendar {
        val PersionDay = toPersionDay()
        return PersionDayToGregorianCalendar(PersionDay)
    }

    /**
     * set date from gregorian date
     * @param gc input gregorian calendar
     */
    fun fromGregorian(gc: GregorianCalendar) {
        val jd = gregorianToPersionDayNumber(gc)
        fromPersionDay(jd)
    }

    /**
     * @return yesterday date
     */
    fun getYesterday(): PersionCalendar {
        return getDateByDiff(-1)
    }

    /**
     * @return tomorrow date
     */
    fun getTomorrow(): PersionCalendar {
        return getDateByDiff(1)
    }

    /**
     * get Persion date by day Persion
     * @param diff number of day Persion
     * @return Persion calendar Persion
     */
    fun getDateByDiff(diff: Int): PersionCalendar {
        val gc = toGregorian()
        gc.add(Calendar.DAY_OF_MONTH, diff)
        return PersionCalendar(gc)
    }

    /**
     * @return day Of Week
     */
    fun getDayOfWeek(): Int {
        return toGregorian().get(Calendar.DAY_OF_WEEK)
    }

    /**
     * @return get first day of week
     */
    fun getFirstDayOfWeek(): Int {
        return toGregorian().firstDayOfWeek
    }
    fun dayOfWeek(): Int {
        return toGregorian().get(Calendar.DAY_OF_WEEK)
    }
    /**
     * @return day name
     */
    fun getDayOfWeekString(): String {
        when (getDayOfWeek()) {
            1 -> return "یک‌شنبه"
            2 -> return "دوشنبه"
            3 -> return "سه‌شنبه"
            4 -> return "چهارشنبه"
            5 -> return "پنجشنبه"
            6 -> return "جمعه"
            7 -> return "شنبه"
            else -> return "نامعلوم"
        }
    }

    /**
     * @return month name
     */
    fun getMonthString(): String {
        when (getMonth()) {
            1 -> return "فروردین"
            2 -> return "اردیبهشت"
            3 -> return "خرداد"
            4 -> return "تیر"
            5 -> return "مرداد"
            6 -> return "شهریور"
            7 -> return "مهر"
            8 -> return "آبان"
            9 -> return "آذر"
            10 -> return "دی"
            11 -> return "بهمن"
            12 -> return "اسفند"
            else -> return "نامعلوم"
        }
    }

    /**
     * get String with the following format :
     *  یکشنبه ۱۲ آبان
     * @return String format
     */
    fun getDayOfWeekDayMonthString(): String {
        return getDayOfWeekString() + " " + getDay() + " " + getMonthString()
    }

    /**
     * @return return whether this year is a jalali leap year
     */
    fun isLeap(): Boolean {
        return getLeapFactor(getYear()) == 0
    }

    fun getYearLength(): Int {
        return if (isLeap()) 366 else 365
    }

    fun getMonthLength(): Int {
        return if (getMonth() < 7) {
            31
        } else if (getMonth() < 12) {
            30
        } else if (getMonth() == 12) {
            if (isLeap()) 30 else 29
        } else {
            0
        }
    }

    fun getDay(): Int {
        return day
    }

    fun getMonth(): Int {
        return month
    }

    fun getYear(): Int {
        return year
    }

    fun setMonth(month: Int) {
        if (month in 1..12) {
            this.month = month
        } else {
            throw IllegalArgumentException("Month should be between 1 and 12 $month .")
        }
    }

    fun setYear(year: Int) {
        if (year >= 0) {
            this.year = year
        } else {
            throw IllegalArgumentException("Year should be a non-negative integer.")
        }
    }

    fun setDay(day: Int) {
        if (day in 1..31) { // تغییر این بخش به مطابق با تعداد روزهای معتبر در ماه‌های جلالی
            this.day = day
        } else {
            throw IllegalArgumentException("Day should be between 1 and 31.") // تغییر پیام خطا به مطابق با ماه‌های جلالی
        }
    }

    fun set(year: Int, month: Int, day: Int) {
        setYear(year)
        setMonth(month)
        setDay(day)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as PersionCalendar

        return year == that.year && month == that.month && day == that.day
    }

    private fun gregorianToPersionDayNumber(gc: GregorianCalendar): Int {
        val gregorianYear = gc.get(GregorianCalendar.YEAR)
        val gregorianMonth = gc.get(GregorianCalendar.MONTH) + 1
        val gregorianDay = gc.get(GregorianCalendar.DAY_OF_MONTH)

        return (((1461 * (gregorianYear + 4800 + (gregorianMonth - 14) / 12)) / 4
                + (367 * (gregorianMonth - 2 - 12 * ((gregorianMonth - 14) / 12))) / 12
                - (3 * ((gregorianYear + 4900 + (gregorianMonth - 14) / 12) / 100)) / 4 + gregorianDay
                - 32075) - (gregorianYear + 100100 + (gregorianMonth - 8) / 6) / 100 * 3 / 4 + 752)
    }

    private fun PersionToPersionDayNumber(jc: PersionCalendars): Int {
        val PersionYear = jc.year
        val PersionMonth = jc.month
        val PersionDay = jc.day

        return (1461 * (PersionYear + 4800 + (PersionMonth - 14) / 12) / 4
                + 367 * (PersionMonth - 2 - 12 * ((PersionMonth - 14) / 12)) / 12
                - 3 * ((PersionYear + 4900 + (PersionMonth - 14) / 12) / 100) / 4 + PersionDay
                - 32075)
    }

    private fun PersionDayToGregorianCalendar(PersionDayNumber: Int): GregorianCalendar {
        val j = 4 * PersionDayNumber + 139361631 + (4 * PersionDayNumber + 183187720) / 146097 * 3 / 4 * 4 - 3908
        val i = (j % 1461) / 4 * 5 + 308

        val gregorianDay = (i % 153) / 5 + 1
        val gregorianMonth = ((i / 153) % 12) + 1
        val gregorianYear = j / 1461 - 100100 + (8 - gregorianMonth) / 6

        return GregorianCalendar(gregorianYear, gregorianMonth - 1, gregorianDay)
    }

    private fun fromPersionDay(PersionDayNumber: Int) {
        val gc = PersionDayToGregorianCalendar(PersionDayNumber)
        val gregorianYear = gc.get(Calendar.YEAR)

        var jalaliYear: Int
        var jalaliMonth: Int
        var jalaliDay: Int

        jalaliYear = gregorianYear - 621

        val gregorianFirstFarvardin: GregorianCalendar =
            PersionCalendar(jalaliYear, 1, 1).getGregorianFirstFarvardin()
        val PersionDayFarvardinFirst = gregorianToPersionDayNumber(gregorianFirstFarvardin)
        var diffFromFarvardinFirst = PersionDayNumber - PersionDayFarvardinFirst

        if (diffFromFarvardinFirst >= 0) {
            if (diffFromFarvardinFirst <= 185) {
                jalaliMonth = 1 + diffFromFarvardinFirst / 31
                jalaliDay = (diffFromFarvardinFirst % 31) + 1
                set(jalaliYear, jalaliMonth, jalaliDay)
                return
            } else {
                diffFromFarvardinFirst = diffFromFarvardinFirst - 186
            }
        } else {
            diffFromFarvardinFirst = diffFromFarvardinFirst + 179
            if (getLeapFactor(jalaliYear) == 1)
                diffFromFarvardinFirst = diffFromFarvardinFirst + 1
            jalaliYear -= 1
        }

        jalaliMonth = 7 + diffFromFarvardinFirst / 30
        jalaliDay = (diffFromFarvardinFirst % 30) + 1
        set(jalaliYear, jalaliMonth, jalaliDay)
    }

    private fun toPersionDay(): Int {
        val jalaliMonth = getMonth()
        val jalaliDay = getDay()

        val gregorianFirstFarvardin = getGregorianFirstFarvardin()
        val gregorianYear = gregorianFirstFarvardin.get(Calendar.YEAR)
        val gregorianMonth = gregorianFirstFarvardin.get(Calendar.MONTH) + 1
        val gregorianDay = gregorianFirstFarvardin.get(Calendar.DAY_OF_MONTH)

        val PersionFirstFarvardin = PersionCalendars(gregorianYear, gregorianMonth, gregorianDay)

        var PersionDay = PersionToPersionDayNumber(PersionFirstFarvardin) + (jalaliMonth - 1) * 31 - jalaliMonth / 7 * (jalaliMonth - 7)
        + jalaliDay - 1

        return PersionDay
    }

    private fun getGregorianFirstFarvardin(): GregorianCalendar {
        var marchDay = 0
        val breaks = intArrayOf(-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178)

        val jalaliYear = getYear()
        val gregorianYear = jalaliYear + 621
        var jalaliLeap = -14
        var jp = breaks[0]

        var jump = 0
        for (j in 1..19) {
            val jm = breaks[j]
            jump = jm - jp
            if (jalaliYear < jm) {
                var N = jalaliYear - jp
                jalaliLeap = jalaliLeap + N / 33 * 8 + (N % 33 + 3) / 4

                if (jump % 33 == 4 && jump - N == 4)
                    jalaliLeap = jalaliLeap + 1

                val GregorianLeap = (gregorianYear / 4) - (gregorianYear / 100 + 1) * 3 / 4 - 150

                marchDay = 20 + (jalaliLeap - GregorianLeap)

                if (jump - N < 6)
                    N = N - jump + (jump + 4) / 33 * 33

                break
            }

            jalaliLeap = jalaliLeap + jump / 33 * 8 + (jump % 33) / 4
            jp = jm
        }

        return GregorianCalendar(gregorianYear, 2, marchDay)
    }

    private fun getLeapFactor(jalaliYear: Int): Int {
        var leap = 0
        val breaks = intArrayOf(-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178)

        var jp = breaks[0]

        var jump = 0
        for (j in 1..19) {
            val jm = breaks[j]
            jump = jm - jp
            if (jalaliYear < jm) {
                var N = jalaliYear - jp

                if (jump - N < 6)
                    N = N - jump + (jump + 4) / 33 * 33

                leap = ((((N + 1) % 33) - 1) % 4)

                if (leap == -1)
                    leap = 4

                break
            }

            jp = jm
        }

        return leap
    }

    override fun toString(): String {
        return String.format("%04d-%02d-%02d", getYear(), getMonth(), getDay())
    }

    private inner class PersionCalendars internal constructor(year: Int, month: Int, day: Int) {
        val year: Int = year
        val month: Int = month
        val day: Int = day
    }

    // داخل کلاس PersionCalendar
    fun isInRange(startDate: List<Int>, endDate: List<Int>): Boolean {
        require(startDate.size == 3 && endDate.size == 3) {
            "Dates must be [year, month, day]"
        }

        val ty = getYear(); val tm = getMonth(); val td = getDay()
        var sy = startDate[0]; var sm = startDate[1]; var sd = startDate[2]
        var ey = endDate[0];   var em = endDate[1];   var ed = endDate[2]

        fun cmp(y1: Int, m1: Int, d1: Int, y2: Int, m2: Int, d2: Int): Int =
            when {
                y1 != y2 -> y1 - y2
                m1 != m2 -> m1 - m2
                else     -> d1 - d2
            }

        // اگر تاریخ‌ها برعکس وارد شدند، جابجا کن
        if (cmp(sy, sm, sd, ey, em, ed) > 0) {
            val tY = sy; val tM = sm; val tD = sd
            sy = ey; sm = em; sd = ed
            ey = tY; em = tM; ed = tD
        }

        val afterOrEqStart = cmp(ty, tm, td, sy, sm, sd) >= 0
        val beforeOrEqEnd  = cmp(ty, tm, td, ey, em, ed) <= 0
        return afterOrEqStart && beforeOrEqEnd
    }
}
