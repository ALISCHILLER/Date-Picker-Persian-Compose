package com.msa.calendar.utils

import java.time.DateTimeException
import java.time.LocalDate
import java.time.chrono.Chronology
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQueries
import java.time.temporal.TemporalQuery
import java.time.temporal.UnsupportedTemporalTypeException
import java.time.temporal.ValueRange
import java.util.Locale


public enum class PersianMonth : TemporalAccessor, TemporalAdjuster {
    FARVARDIN, ORDIBEHESHT, KHORDAD, TIR, MORDAD, SHAHRIVAR, MEHR, ABAN, AZAR, DEY, BAHMAN, ESFAND;

    val value: Int
        get() = ordinal + 1

    fun getDisplayName(style: TextStyle?, locale: Locale?): String {
        return DateTimeFormatterBuilder().appendText(ChronoField.MONTH_OF_YEAR, style)
            .toFormatter(locale).format(this)
    }

    override fun isSupported(field: TemporalField): Boolean {
        return if (field is ChronoField) {
            field === ChronoField.MONTH_OF_YEAR
        } else {
            field.isSupportedBy(this)
        }
    }

    override fun range(field: TemporalField): ValueRange {
        return if (field === ChronoField.MONTH_OF_YEAR) {
            field.range()
        } else super<TemporalAccessor>.range(field)
    }

    override fun get(field: TemporalField): Int {
        return if (field === ChronoField.MONTH_OF_YEAR) {
            value
        } else super<TemporalAccessor>.get(field)
    }

    override fun getLong(field: TemporalField): Long {
        if (field === ChronoField.MONTH_OF_YEAR) {
            return value.toLong()
        } else if (field is ChronoField) {
            throw UnsupportedTemporalTypeException("Unsupported field: $field")
        }
        return field.getFrom(this)
    }
    fun length(leapYear: Boolean): Int {
        return when (this) {
            FARVARDIN, ORDIBEHESHT, KHORDAD, TIR, MORDAD, SHAHRIVAR -> 31
            MEHR, ABAN, AZAR, DEY, BAHMAN -> 30
            ESFAND -> if (leapYear) 30 else 29
        }
    }
    override fun adjustInto(temporal: Temporal): Temporal {
        val persianChrono = Chronology.ofLocale(Locale.forLanguageTag("fa-IR"))
        if (persianChrono != Chronology.from(temporal)) {
            throw DateTimeException("Adjustment only supported on Persian (Shamsi) date-time")
        }
        return temporal.with(ChronoField.MONTH_OF_YEAR, value.toLong())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R> query(query: TemporalQuery<R>): R {
        if (query === TemporalQueries.chronology()) {
            return Chronology.ofLocale(Locale.forLanguageTag("fa-IR")) as R
        } else if (query === TemporalQueries.precision()) {
            return ChronoUnit.MONTHS as R
        }
        return super<TemporalAccessor>.query(query)
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private val ENUMS = values()
        fun of(month: Int): PersianMonth {
            if (month < 1 || month > 12) {
                throw DateTimeException("Invalid value for PersianMonth: $month")
            }
            return ENUMS[month - 1]
        }

        fun from(temporal: TemporalAccessor): PersianMonth {
            var resolvedTemporal = temporal
            if (resolvedTemporal is PersianMonth) {
                return resolvedTemporal
            }
            try {
                val persianChrono = Chronology.ofLocale(Locale.forLanguageTag("fa-IR"))
                if (persianChrono != Chronology.from(resolvedTemporal)) {
                    resolvedTemporal = LocalDate.from(resolvedTemporal).with(persianChrono.date(1, 1, 1))
                }
                return of(resolvedTemporal[ChronoField.MONTH_OF_YEAR])
            } catch (ex: DateTimeException) {
                throw DateTimeException(
                    "Unable to obtain PersianMonth from TemporalAccessor: " +
                        resolvedTemporal + " of type " + resolvedTemporal.javaClass.name,
                    ex,
                )
            }
        }
    }
}
