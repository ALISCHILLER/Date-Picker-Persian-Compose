package com.msa.calendar

import androidx.compose.runtime.Immutable
import com.msa.calendar.core.JalaliDate
import com.msa.calendar.core.JalaliDateRange
import com.msa.calendar.ui.DateFormatter
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DateRangeFormatter
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.daysUntil
import java.time.LocalDate
import kotlin.math.abs

/**
 * Describes why the picker dialog was closed.
 *
 * The existing Boolean dismissal callback is kept for source compatibility, but this enum should be
 * preferred by new integrations because it makes analytics, state restoration and form handling less
 * ambiguous.
 */
@Immutable
enum class DatePickerCloseReason {
    /** The system/backdrop dismiss request closed the dialog without confirming a selection. */
    Dismissed,

    /** The explicit cancel action closed the dialog. */
    Canceled,

    /** A valid selection was confirmed before the dialog closed. */
    Confirmed,
}

/**
 * Strongly typed result for the single-date picker.
 */
@Immutable
data class SingleDateSelection(
    val date: SoleimaniDate,
    val gregorianDate: LocalDate,
    val formattedDate: String,
    val legacyDateMap: Map<String, String>,
) {
    /** Android-free representation suitable for domain and data layers. */
    val jalaliDate: JalaliDate
        get() = date.toJalaliDate()

    companion object {
        fun create(
            date: SoleimaniDate,
            dateFormatter: DateFormatter = DateFormatter.Default,
            digitMode: DigitMode = DigitMode.Persian,
        ): SingleDateSelection = SingleDateSelection(
            date = date,
            gregorianDate = date.toGregorian(),
            formattedDate = dateFormatter.format(date, digitMode),
            legacyDateMap = date.toLegacyMap(digitMode),
        )
    }
}

/**
 * Strongly typed result for the range picker.
 *
 * [startDate] and [endDate] are always ordered from earlier to later, even if the user selected them
 * in reverse order.
 */
@Immutable
data class DateRangeSelection(
    val startDate: SoleimaniDate,
    val endDate: SoleimaniDate,
    val startGregorianDate: LocalDate,
    val endGregorianDate: LocalDate,
    val formattedStartDate: String,
    val formattedEndDate: String,
    val formattedRange: String,
    val legacyDateMaps: List<Map<String, String>>,
) {
    /** Android-free ordered range suitable for domain and data layers. */
    val jalaliRange: JalaliDateRange
        get() = JalaliDateRange.of(startDate.toJalaliDate(), endDate.toJalaliDate())

    init {
        require(startDate <= endDate) { "startDate must be before or equal to endDate" }
        require(legacyDateMaps.size == 2) { "legacyDateMaps must contain start and end entries" }
    }

    val daysInclusive: Int = abs(startDate.daysUntil(endDate)) + 1

    companion object {
        fun create(
            firstDate: SoleimaniDate,
            secondDate: SoleimaniDate,
            dateFormatter: DateFormatter = DateFormatter.Default,
            rangeFormatter: DateRangeFormatter = DateRangeFormatter.Default,
            digitMode: DigitMode = DigitMode.Persian,
        ): DateRangeSelection {
            val (start, end) = if (firstDate <= secondDate) {
                firstDate to secondDate
            } else {
                secondDate to firstDate
            }
            val formattedStart = dateFormatter.format(start, digitMode)
            val formattedEnd = dateFormatter.format(end, digitMode)
            return DateRangeSelection(
                startDate = start,
                endDate = end,
                startGregorianDate = start.toGregorian(),
                endGregorianDate = end.toGregorian(),
                formattedStartDate = formattedStart,
                formattedEndDate = formattedEnd,
                formattedRange = rangeFormatter.format(formattedStart, formattedEnd),
                legacyDateMaps = listOf(
                    start.toLegacyMap(digitMode),
                    end.toLegacyMap(digitMode),
                ),
            )
        }
    }
}

internal fun SoleimaniDate.toLegacyMap(digitMode: DigitMode): Map<String, String> =
    toMap(usePersianDigits = digitMode == DigitMode.Persian)

internal fun SoleimaniDate.toSingleDateSelection(config: DatePickerConfig): SingleDateSelection =
    SingleDateSelection.create(
        date = this,
        dateFormatter = config.dateFormatter,
        digitMode = config.digitMode,
    )

internal fun toDateRangeSelection(
    firstDate: SoleimaniDate,
    secondDate: SoleimaniDate,
    config: DatePickerConfig,
): DateRangeSelection = DateRangeSelection.create(
    firstDate = firstDate,
    secondDate = secondDate,
    dateFormatter = config.dateFormatter,
    rangeFormatter = config.rangeFormatter,
    digitMode = config.digitMode,
)
