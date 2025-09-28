package com.msa.persioncalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msa.calendar.CalendarScreen
import com.msa.calendar.RangeCalendarScreen
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerQuickAction
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.WeekdayFormatter
import com.msa.calendar.ui.theme.PersionCalendarTheme
import com.msa.calendar.utils.FormatHelper
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.addLeadingZero
import com.msa.calendar.utils.toPersianNumber
import com.msa.calendar.utils.toSoleimaniDate
import java.time.DayOfWeek
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.plusDays
import java.util.Calendar


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersionCalendarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalendarShowcaseScreen()
                }
            }
        }
    }
}
private enum class SelectionType {
    Single,
    Range,
    QuickToday
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarShowcaseScreen(modifier: Modifier = Modifier) {
    var showSinglePicker by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }
    var selectedSingleDate by remember { mutableStateOf<SoleimaniDate?>(null) }
    var selectedRange by remember { mutableStateOf<SoleimaniRange?>(null) }
    var lastSelectionType by remember { mutableStateOf<SelectionType?>(null) }
    var useLatinDigits by remember { mutableStateOf(false) }
    var showTodayShortcut by remember { mutableStateOf(true) }
    var limitToNextMonth by remember { mutableStateOf(false) }
    var blockFridays by remember { mutableStateOf(false) }
    var blockThirteenth by remember { mutableStateOf(false) }
    var enableClearAction by remember { mutableStateOf(true) }
    var useInternationalWeek by remember { mutableStateOf(false) }
    var highlightEvents by remember { mutableStateOf(true) }
    var limitRangeLength by remember { mutableStateOf(false) }


    val today = remember { PersionCalendar().toSoleimaniDate() }

    val constraintConfig = remember(limitToNextMonth, blockFridays, blockThirteenth, today) {
        val minDate = if (limitToNextMonth) today else null
        val computedMax = today.plusDays(30) ?: today
        val maxDate = if (limitToNextMonth) computedMax else null
        val disabledDates = if (blockThirteenth) {
            generateThirteenthBlackouts(
                start = today,
                monthsAhead = if (limitToNextMonth) 3 else 12,
                minDate = minDate,
                maxDate = maxDate,
            )
        } else {
            emptySet()
        }
        val validator = if (blockFridays) {
            { date: SoleimaniDate -> date.toCalendar().getDayOfWeek() != Calendar.FRIDAY }
        } else {
            DatePickerConstraints.AlwaysValid
        }
        DatePickerConstraints(
            minDate = minDate,
            maxDate = maxDate,
            disabledDates = disabledDates,
            dateValidator = validator,
            maxRangeLength = if (limitRangeLength) 10 else null,
        )
    }

    val weekConfiguration = remember(useInternationalWeek) {
        if (useInternationalWeek) {
            WeekConfiguration(
                startDay = DayOfWeek.MONDAY,
                weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                dayLabelFormatter = WeekdayFormatter.LatinShort,
            )
        } else {
            WeekConfiguration()
        }
    }

    val nextPayday = remember(today, limitToNextMonth) {
        today.plusDays(if (limitToNextMonth) 10 else 20) ?: today
    }

    val quickActionSet = remember(showTodayShortcut, enableClearAction, useLatinDigits, nextPayday) {
        buildList {
            if (showTodayShortcut) add(DatePickerQuickAction.Today)
            if (enableClearAction) add(DatePickerQuickAction.ClearSelection())
            add(
                DatePickerQuickAction.JumpToDate(
                    actionLabel = if (useLatinDigits) "Next payday" else "حقوق بعدی",
                    targetDateProvider = { nextPayday }
                )
            )
        }
    }

    val eventIndicator = remember(highlightEvents, blockThirteenth, useLatinDigits, today) {
        if (!highlightEvents) {
            { _: SoleimaniDate -> null }
        } else {
            { date: SoleimaniDate ->
                when {
                    blockThirteenth && date.day == 13 -> CalendarEvent(
                        color = Color(0xFFEF4444),
                        label = if (useLatinDigits) "Disabled" else "تاریخ مسدود",
                    )

                    date.day == 1 -> CalendarEvent(
                        color = Color(0xFF10B981),
                        label = if (useLatinDigits) "Start of month" else "آغاز ماه",
                    )

                    date == today -> CalendarEvent(
                        color = Color(0xFF3B82F6),
                        label = if (useLatinDigits) "Today" else "امروز",
                    )

                    else -> null
                }
            }
        }
    }

    val dialogConfig = remember(
        useLatinDigits,
        showTodayShortcut,
        constraintConfig,
        weekConfiguration,
        quickActionSet,
        eventIndicator,
    ) {
        val strings = if (useLatinDigits) {
            DatePickerStrings(
                title = "Select date",
                confirm = "Confirm",
                cancel = "Cancel",
                today = "Today",
                clearSelection = "Clear",
                rangeStartLabel = "Start date",
                rangeEndLabel = "End date",
            )
        } else {
            DatePickerStrings()
        }
        DatePickerConfig(
            strings = strings,
            digitMode = if (useLatinDigits) DigitMode.Latin else DigitMode.Persian,
            showTodayAction = showTodayShortcut,
            constraints = constraintConfig,
            weekConfiguration = weekConfiguration,
            quickActions = quickActionSet,
            eventIndicator = eventIndicator,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )

    ) {
        if (showSinglePicker) {
            CalendarScreen(
                onDismiss = { showSinglePicker = false },
                onConfirm = { showSinglePicker = false },
                config = dialogConfig,
                onDateSelected = { date ->
                    selectedSingleDate = date
                    lastSelectionType = SelectionType.Single
                }
            )



        }
        if (showRangePicker) {
            RangeCalendarScreen(
                onDismiss = { showRangePicker = false },
                setDate = { _ -> },
                config = dialogConfig,
                onRangeSelected = { start, end ->
                    val (first, second) = if (start <= end) start to end else end to start
                    selectedRange = SoleimaniRange(first, second)
                    lastSelectionType = SelectionType.Range
                }
            )
        }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "نمونه انتخاب تاریخ فارسی") },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.85f),
                    tonalElevation = 3.dp,
                ) {
                    Text(
                        text = "این صفحه سناریوهای متداول استفاده از کتابخانه را با ظاهری نو و منسجم نمایش می‌دهد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionCard(
                    title = "تنظیمات تجربه",
                    subtitle = "می‌توانید رفتار و محدودیت‌های تقویم را از این بخش شخصی‌سازی کنید.",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PreferencesSection(
                        useLatinDigits = useLatinDigits,
                        onDigitsModeChanged = { useLatinDigits = it },
                        showTodayShortcut = showTodayShortcut,
                        onToggleTodayShortcut = { showTodayShortcut = it },
                        limitToNextMonth = limitToNextMonth,
                        onToggleLimitToNextMonth = { limitToNextMonth = it },
                        blockFridays = blockFridays,
                        onToggleBlockFridays = { blockFridays = it },
                        blockThirteenth = blockThirteenth,
                        onToggleBlockThirteenth = { blockThirteenth = it },
                        enableClearAction = enableClearAction,
                        onToggleClearAction = { enableClearAction = it },
                        useInternationalWeek = useInternationalWeek,
                        onToggleInternationalWeek = { useInternationalWeek = it },
                        highlightEvents = highlightEvents,
                        onToggleHighlightEvents = { highlightEvents = it },
                        limitRangeLength = limitRangeLength,
                        onToggleLimitRangeLength = { limitRangeLength = it },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionCard(
                    title = "انتخاب‌های سریع",
                    subtitle = "میانبرهای کاربردی برای تست و نمایش حالات مختلف انتخاب تاریخ.",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { showSinglePicker = true },
                            modifier = Modifier.widthIn(min = 0.dp)
                        ) {
                            Text(text = "انتخاب تاریخ تکی")
                        }

                        FilledTonalButton(
                            onClick = { showRangePicker = true },
                            modifier = Modifier.widthIn(min = 0.dp)
                        ) {
                            Text(text = "انتخاب بازه تاریخ")
                        }

                        FilledTonalButton(
                            onClick = {
                                selectedSingleDate = today
                                lastSelectionType = SelectionType.QuickToday
                            },
                            modifier = Modifier.widthIn(min = 0.dp)
                        ) {
                            Text(text = "ثبت سریع امروز")
                        }

                        OutlinedButton(
                            onClick = {
                                selectedSingleDate = null
                                selectedRange = null
                                lastSelectionType = null
                            },
                            modifier = Modifier.widthIn(min = 0.dp)
                        ) {
                            Text(text = "پاک کردن انتخاب‌ها")
                        }

                        OutlinedButton(
                            enabled = lastSelectionType != null,
                            onClick = {
                                when (lastSelectionType) {
                                    SelectionType.Single -> showSinglePicker = true
                                    SelectionType.Range -> showRangePicker = true
                                    SelectionType.QuickToday -> {
                                        selectedSingleDate = today
                                    }
                                    null -> Unit
                                }
                            },
                            modifier = Modifier.widthIn(min = 0.dp)
                        ) {
                            Text(text = "تکرار آخرین حالت")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionCard(
                    title = "گزارش انتخاب‌ها",
                    subtitle = "جزئیات آخرین تعامل و محدودیت‌های فعال روی تقویم را بررسی کنید.",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionSummaryCard(
                        selectedSingleDate = selectedSingleDate,
                        selectedRange = selectedRange,
                        lastSelectionType = lastSelectionType,
                        constraints = constraintConfig,
                        limitToNextMonth = limitToNextMonth,
                        blockFridays = blockFridays,
                        blockThirteenth = blockThirteenth,
                        limitRangeLength = limitRangeLength,
                        useLatinDigits = useLatinDigits,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferencesSection(
    useLatinDigits: Boolean,
    onDigitsModeChanged: (Boolean) -> Unit,
    showTodayShortcut: Boolean,
    onToggleTodayShortcut: (Boolean) -> Unit,
    limitToNextMonth: Boolean,
    onToggleLimitToNextMonth: (Boolean) -> Unit,
    blockFridays: Boolean,
    onToggleBlockFridays: (Boolean) -> Unit,
    blockThirteenth: Boolean,
    onToggleBlockThirteenth: (Boolean) -> Unit,
    enableClearAction: Boolean,
    onToggleClearAction: (Boolean) -> Unit,
    useInternationalWeek: Boolean,
    onToggleInternationalWeek: (Boolean) -> Unit,
    highlightEvents: Boolean,
    onToggleHighlightEvents: (Boolean) -> Unit,
    limitRangeLength: Boolean,
    onToggleLimitRangeLength: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PreferenceCategoryLabel(text = "نمایش و زبان")

        PreferenceSwitchRow(
            label = "نمایش دکمه امروز",
            description = "میانبر همیشه دردسترس برای بازگشت به امروز",
            checked = showTodayShortcut,
            onCheckedChange = onToggleTodayShortcut,
        )

        PreferenceSwitchRow(
            label = "استفاده از اعداد لاتین",
            description = "نمایش اعداد با فرمت بین‌المللی",
            checked = useLatinDigits,
            onCheckedChange = onDigitsModeChanged,
        )
        PreferenceSwitchRow(
            label = "نمایش دکمه پاک کردن انتخاب",
            description = "به کاربر اجازه می‌دهد همه انتخاب‌ها را ریست کند",
            checked = enableClearAction,
            onCheckedChange = onToggleClearAction,
        )

        PreferenceCategoryLabel(text = "محدودیت‌های انتخاب")
        PreferenceSwitchRow(
            label = "محدود به ۳۰ روز آینده",
            checked = limitToNextMonth,
            description = "فقط تاریخ‌های نزدیک قابل انتخاب باشند",
            onCheckedChange = onToggleLimitToNextMonth,
        )

        PreferenceSwitchRow(
            label = "مسدود کردن جمعه‌ها",
            description = "برای جلوگیری از انتخاب روزهای تعطیل آخر هفته",
            checked = blockFridays,
            onCheckedChange = onToggleBlockFridays,
        )

        PreferenceSwitchRow(
            label = "حذف روز سیزدهم",
            checked = blockThirteenth,
            description = "روزهای خاص یا نامطلوب را غیرفعال کنید",
            onCheckedChange = onToggleBlockThirteenth,
        )
        PreferenceSwitchRow(
            label = "محدودیت بازه به ۱۰ روز",
            description = "برای جلوگیری از انتخاب بازه‌های بسیار طولانی",
            checked = limitRangeLength,
            onCheckedChange = onToggleLimitRangeLength,
        )
        PreferenceCategoryLabel(text = "ساختار هفته و رویدادها")
        PreferenceSwitchRow(
            label = "شروع هفته از دوشنبه",
            description = "چیدمان بین‌المللی روزهای هفته",
            checked = useInternationalWeek,
            onCheckedChange = onToggleInternationalWeek,
        )

        PreferenceSwitchRow(
            label = "نمایش رویدادهای شاخص",
            description = "رویدادهای بصری برای تاریخ‌های مهم",
            checked = highlightEvents,
            onCheckedChange = onToggleHighlightEvents,
        )



    }
}

@Composable
private fun PreferenceSwitchRow(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}

@Composable
private fun SelectionSummaryCard(
    selectedSingleDate: SoleimaniDate?,
    selectedRange: SoleimaniRange?,
    lastSelectionType: SelectionType?,
    constraints: DatePickerConstraints,
    limitToNextMonth: Boolean,
    blockFridays: Boolean,
    blockThirteenth: Boolean,
    limitRangeLength: Boolean,
    useLatinDigits: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectionSummaryRow(
            title = "تاریخ تکی",
            value = selectedSingleDate?.toDisplayString()
                ?: "هنوز تاریخی انتخاب نشده است.",
        )

        SelectionSummaryRow(
            title = "بازه تاریخی",
            value = selectedRange?.toDisplayString()
                ?: "بازه‌ای انتخاب نشده است.",
        )

        AnimatedVisibility(
            visible = lastSelectionType != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            val (label, color) = when (lastSelectionType) {
                SelectionType.Single -> "آخرین عمل: انتخاب تاریخ تکی" to MaterialTheme.colorScheme.primary
                SelectionType.Range -> "آخرین عمل: انتخاب بازه" to MaterialTheme.colorScheme.secondary
                SelectionType.QuickToday -> "آخرین عمل: ثبت سریع امروز" to MaterialTheme.colorScheme.tertiary
                null -> "" to MaterialTheme.colorScheme.primary
            }
            StatusPill(text = label, color = color)
        }
        Divider()

        ConstraintSummary(
            constraints = constraints,
            limitToNextMonth = limitToNextMonth,
            blockFridays = blockFridays,
            blockThirteenth = blockThirteenth,
            limitRangeLength = limitRangeLength,
            useLatinDigits = useLatinDigits,
        )
    }
}

@Composable
private fun SelectionSummaryRow(
    title: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ConstraintSummary(
    constraints: DatePickerConstraints,
    limitToNextMonth: Boolean,
    blockFridays: Boolean,
    blockThirteenth: Boolean,
    limitRangeLength: Boolean,
    useLatinDigits: Boolean,
) {
    // Snapshot to local vals to enable smart cast
    val min = constraints.minDate
    val max = constraints.maxDate
    val disabled = constraints.disabledDates
    val maxRange = constraints.maxRangeLength  // <— این خط را اضافه کن

    val rules = buildList {
        if (limitToNextMonth && min != null && max != null) {
            add("انتخاب تاریخ تنها بین ${min.toDisplayString()} تا ${max.toDisplayString()} امکان‌پذیر است.")
        }
        if (blockFridays) add("روزهای جمعه برای انتخاب غیرفعال شده‌اند.")

        if (blockThirteenth) {
            val blockedCount = disabled.size
            val suffix = if (blockedCount > 0) " ($blockedCount تاریخ)" else ""
            add("روز سیزدهم هر ماه مسدود است$suffix.")
        }

        // به‌جای دسترسی مستقیم به constraints.maxRangeLength از maxRange استفاده کن
        if (limitRangeLength && maxRange != null) {
            val limitText = if (useLatinDigits) {
                addLeadingZero(maxRange) // یا فقط maxRange.toString()
            } else {
                FormatHelper.toPersianNumber(addLeadingZero(maxRange))
            }
            add("حداکثر طول بازه $limitText روز تعریف شده است.")
        }

        if (!limitToNextMonth && min != null && max != null) {
            add("محدوده فعال از ${min.toDisplayString()} تا ${max.toDisplayString()} تعیین شده است.")
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "محدودیت‌های فعال",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (rules.isEmpty()) {
                Text(
                    text = "هیچ محدودیتی فعال نیست.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                rules.forEach { rule ->
                    Text(
                        text = "• $rule",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceCategoryLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.92f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

private data class SoleimaniRange(
    val start: SoleimaniDate,
    val end: SoleimaniDate,
)

private fun SoleimaniRange.toDisplayString(): String {
    return "${start.toDisplayString()} تا ${end.toDisplayString()}"
}



private fun SoleimaniDate.toDisplayString(): String {
    val yearText = year.toPersianNumber()
    val monthText = FormatHelper.toPersianNumber(addLeadingZero(month))
    val dayText = FormatHelper.toPersianNumber(addLeadingZero(day))
    return "$yearText/$monthText/$dayText"
}

private fun generateThirteenthBlackouts(
    start: SoleimaniDate,
    monthsAhead: Int,
    minDate: SoleimaniDate?,
    maxDate: SoleimaniDate?,
): Set<SoleimaniDate> {
    if (monthsAhead <= 0) return emptySet()
    val blockedDates = mutableSetOf<SoleimaniDate>()
    var cursor = SoleimaniDate(start.year, start.month, 13)
    repeat(monthsAhead) {
        if ((minDate == null || cursor >= minDate) && (maxDate == null || cursor <= maxDate)) {
            blockedDates.add(cursor)
        }
        val calendar = cursor.toCalendar()
        val nextMonth = calendar.getDateByDiff(calendar.getMonthLength())
        cursor = SoleimaniDate(nextMonth.getYear(), nextMonth.getMonth(), 13)
    }
    return blockedDates
}

@Preview(showBackground = true)
@Composable
private fun CalendarShowcaseScreenPreview() {
    PersionCalendarTheme {
        CalendarShowcaseScreen()
    }
}