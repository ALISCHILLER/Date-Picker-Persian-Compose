package com.msa.persioncalendar.showcase

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msa.calendar.CalendarScreen
import com.msa.calendar.RangeCalendarScreen
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DatePickerDefaults
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.ui.WeekConfiguration
import com.msa.calendar.ui.YearFormatter
import com.msa.calendar.ui.theme.PersionCalendarTheme
import com.msa.calendar.utils.PersionCalendar
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.toSoleimaniDate
import com.msa.persioncalendar.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarShowcaseScreen(
    modifier: Modifier = Modifier,
    state: CalendarShowcaseState,
    dialogConfig: DatePickerConfig,
    today: SoleimaniDate,
    constraintConfig: DatePickerConstraints,
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    yearFormatter: YearFormatter,
    weekConfiguration: WeekConfiguration,
    upcomingMilestone: SoleimaniDate,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
    rangeFormatter: RangeFormatter,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
    ) {
        ShowcaseDialogs(
            state = state,
            dialogConfig = dialogConfig,
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = stringResource(R.string.showcase_appbar_title)) },
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
                IntroCard()

                Spacer(modifier = Modifier.height(24.dp))

                SectionCard(
                    titleRes = R.string.showcase_section_experience_title,
                    subtitleRes = R.string.showcase_section_experience_subtitle,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PreferencesSection(
                        state = state,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionCard(
                    titleRes = R.string.showcase_section_quick_title,
                    subtitleRes = R.string.showcase_section_quick_subtitle,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    QuickActionsSection(
                        state = state,
                        today = today,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionCard(
                    titleRes = R.string.showcase_section_report_title,
                    subtitleRes = R.string.showcase_section_report_subtitle,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SelectionSummaryCard(
                        state = state,
                        constraintConfig = constraintConfig,
                        limitToNextMonth = state.limitToNextMonth,
                        blockFridays = state.blockFridays,
                        blockThirteenth = state.blockThirteenth,
                        limitRangeLength = state.limitRangeLength,
                        digitMode = digitMode,
                        monthFormatter = monthFormatter,
                        yearFormatter = yearFormatter,
                        weekConfiguration = weekConfiguration,
                        upcomingMilestone = upcomingMilestone,
                        rangeFormatter = rangeFormatter,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                HighlightLegend(
                    digitMode = digitMode,
                    eventIndicator = eventIndicator,
                )
            }
        }
    }
}

@Composable
private fun ShowcaseDialogs(
    state: CalendarShowcaseState,
    dialogConfig: DatePickerConfig,
) {
    if (state.showSinglePicker) {
        CalendarScreen(
            onDismiss = state::dismissPickers,
            onConfirm = state::dismissPickers,
            config = dialogConfig,
            onDateSelected = state::onSingleDateSelected,
        )
    }

    if (state.showRangePicker) {
        RangeCalendarScreen(
            onDismiss = state::dismissPickers,
            setDate = { _ -> },
            config = dialogConfig,
            onRangeSelected = state::onRangeSelected,
        )
    }
}

@Composable
private fun IntroCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.85f),
        tonalElevation = 3.dp,
    ) {
        Text(
            text = stringResource(R.string.showcase_intro_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun PreferencesSection(state: CalendarShowcaseState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PreferenceCategoryLabel(R.string.showcase_category_display)
        LanguageSelector(state)
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_latin_digits_title,
            subtitleRes = R.string.showcase_toggle_latin_digits_subtitle,
            checked = state.useLatinDigits,
            onCheckedChange = { state.useLatinDigits = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_gregorian_labels_title,
            subtitleRes = R.string.showcase_toggle_gregorian_labels_subtitle,
            checked = state.useGregorianLabels,
            onCheckedChange = { state.useGregorianLabels = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_gregorian_year_title,
            subtitleRes = R.string.showcase_toggle_gregorian_year_subtitle,
            checked = state.showGregorianYearHint,
            onCheckedChange = { state.showGregorianYearHint = it },
        )

        Divider()

        PreferenceCategoryLabel(R.string.showcase_category_behavior)
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_today_shortcut_title,
            subtitleRes = R.string.showcase_toggle_today_shortcut_subtitle,
            checked = state.showTodayShortcut,
            onCheckedChange = { state.showTodayShortcut = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_limit_30_title,
            subtitleRes = R.string.showcase_toggle_limit_30_subtitle,
            checked = state.limitToNextMonth,
            onCheckedChange = { state.limitToNextMonth = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_disable_weekend_title,
            subtitleRes = R.string.showcase_toggle_disable_weekend_subtitle,
            checked = state.blockFridays,
            onCheckedChange = { state.blockFridays = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_block_13_title,
            subtitleRes = R.string.showcase_toggle_block_13_subtitle,
            checked = state.blockThirteenth,
            onCheckedChange = { state.blockThirteenth = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_enable_clear_title,
            subtitleRes = R.string.showcase_toggle_enable_clear_subtitle,
            checked = state.enableClearAction,
            onCheckedChange = { state.enableClearAction = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_international_week_title,
            subtitleRes = R.string.showcase_toggle_international_week_subtitle,
            checked = state.useInternationalWeek,
            onCheckedChange = { state.useInternationalWeek = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_highlight_events_title,
            subtitleRes = R.string.showcase_toggle_highlight_events_subtitle,
            checked = state.highlightEvents,
            onCheckedChange = { state.highlightEvents = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_limit_range_title,
            subtitleRes = R.string.showcase_toggle_limit_range_subtitle,
            checked = state.limitRangeLength,
            onCheckedChange = { state.limitRangeLength = it },
        )
    }
}

@Composable
private fun LanguageSelector(state: CalendarShowcaseState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.showcase_language_selector_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val options = listOf(
                LocaleOption.System to R.string.showcase_locale_system,
                LocaleOption.Persian to R.string.showcase_locale_persian,
                LocaleOption.English to R.string.showcase_locale_english,
            )
            options.forEach { (option, labelRes) ->
                val selected = state.localeOption == option
                FilterChip(
                    selected = selected,
                    onClick = { state.onLocaleOptionSelected(option) },
                    label = { Text(text = stringResource(labelRes)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        selected = selected,
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun PreferenceToggle(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val title = stringResource(titleRes)
    val subtitle = stringResource(subtitleRes)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
private fun QuickActionsSection(
    state: CalendarShowcaseState,
    today: SoleimaniDate,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilledTonalButton(
            onClick = state::openSinglePicker,
            modifier = Modifier.widthIn(min = 0.dp),
        ) {
            Text(text = stringResource(R.string.showcase_action_single_picker))
        }

        FilledTonalButton(
            onClick = state::openRangePicker,
            modifier = Modifier.widthIn(min = 0.dp),
        ) {
            Text(text = stringResource(R.string.showcase_action_range_picker))
        }

        FilledTonalButton(
            onClick = { state.onQuickTodaySelected(today) },
            modifier = Modifier.widthIn(min = 0.dp),
        ) {
            Text(text = stringResource(R.string.showcase_action_today))
        }

        OutlinedButton(
            onClick = state::clearSelection,
            modifier = Modifier.widthIn(min = 0.dp),
        ) {
            Text(text = stringResource(R.string.showcase_action_clear))
        }

        OutlinedButton(
            enabled = state.lastSelectionType != null,
            onClick = {
                when (state.lastSelectionType) {
                    SelectionType.Single -> state.openSinglePicker()
                    SelectionType.Range -> state.openRangePicker()
                    SelectionType.QuickToday -> state.onQuickTodaySelected(today)
                    null -> Unit
                }
            },
            modifier = Modifier.widthIn(min = 0.dp),
        ) {
            Text(text = stringResource(R.string.showcase_action_repeat_last))
        }
    }
}

@Composable
private fun SelectionSummaryCard(
    state: CalendarShowcaseState,
    constraintConfig: DatePickerConstraints,
    limitToNextMonth: Boolean,
    blockFridays: Boolean,
    blockThirteenth: Boolean,
    limitRangeLength: Boolean,
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    yearFormatter: YearFormatter,
    weekConfiguration: WeekConfiguration,
    upcomingMilestone: SoleimaniDate,
    rangeFormatter: RangeFormatter,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val singleTitle = stringResource(R.string.showcase_summary_single_title)
        val rangeTitle = stringResource(R.string.showcase_summary_range_title)
        val noSingleValue = stringResource(R.string.showcase_summary_none_single)
        val noRangeValue = stringResource(R.string.showcase_summary_none_range)
        val milestoneTitle = stringResource(R.string.showcase_summary_milestone_title)

        SelectionSummaryRow(
            title = singleTitle,
            value = state.selectedSingleDate?.toDisplayString(digitMode, monthFormatter, yearFormatter) ?: noSingleValue,
        )
        SelectionSummaryRow(
            title = rangeTitle,
            value = state.selectedRange
                ?.toDisplayString(digitMode, monthFormatter, yearFormatter, rangeFormatter)
                ?: noRangeValue,
        )
        SelectionSummaryRow(
            title = milestoneTitle,
            value = upcomingMilestone.toDisplayString(digitMode, monthFormatter, yearFormatter),
        )

        AnimatedVisibility(
            visible = state.lastSelectionType != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            val (label, color) = when (state.lastSelectionType) {
                SelectionType.Single -> stringResource(R.string.showcase_summary_last_single) to MaterialTheme.colorScheme.primary
                SelectionType.Range -> stringResource(R.string.showcase_summary_last_range) to MaterialTheme.colorScheme.secondary
                SelectionType.QuickToday -> stringResource(R.string.showcase_summary_last_quick) to MaterialTheme.colorScheme.tertiary
                null -> "" to MaterialTheme.colorScheme.primary
            }
            StatusPill(text = label, color = color)
        }

        Divider()

        ConstraintSummary(
            constraints = constraintConfig,
            limitToNextMonth = limitToNextMonth,
            blockFridays = blockFridays,
            blockThirteenth = blockThirteenth,
            limitRangeLength = limitRangeLength,
            digitMode = digitMode,
            monthFormatter = monthFormatter,
            yearFormatter = yearFormatter,
            weekConfiguration = weekConfiguration,
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
    digitMode: DigitMode,
    monthFormatter: MonthFormatter,
    yearFormatter: YearFormatter,
    weekConfiguration: WeekConfiguration,
) {
    val min = constraints.minDate
    val max = constraints.maxDate
    val disabled = constraints.disabledDates
    val maxRange = constraints.maxRangeLength
    val noLimitMessage = stringResource(R.string.showcase_summary_limit_between_none)

    val minText = min?.toDisplayString(digitMode, monthFormatter, yearFormatter)
    val maxText = max?.toDisplayString(digitMode, monthFormatter, yearFormatter)

    val useLatin = digitMode == DigitMode.Latin
    val weekendList = weekConfiguration.weekendDays
        .sortedBy { it.value }
        .joinToString(separator = if (useLatin) ", " else "، ") { it.toDisplayName(useLatin) }
    val overview = listOf(
        stringResource(R.string.showcase_summary_week_start, weekConfiguration.startDay.toDisplayName(useLatin)),
        stringResource(R.string.showcase_summary_weekend_days, weekendList),
    )
    val rules = buildList {
        if (limitToNextMonth && min != null && max != null) {
            add(stringResource(R.string.showcase_summary_limit_between, minText.orEmpty(), maxText.orEmpty()))
        } else if (min == null && max == null) {
            add(noLimitMessage)
        }

        if (blockFridays) {
            add(stringResource(R.string.showcase_summary_weekend_blocked, weekendList))
        }

        if (blockThirteenth) {
            val blockedCount = disabled.size
            val countText = blockedCount.toDigitString(digitMode)
            val suffix = if (blockedCount > 0) {
                stringResource(R.string.showcase_summary_block_13_suffix, countText)
            } else {
                ""
            }
            add(stringResource(R.string.showcase_summary_block_13, suffix))
        }

        if (limitRangeLength && maxRange != null) {
            val limitText = maxRange.toDigitString(digitMode)
            add(stringResource(R.string.showcase_summary_max_range, limitText))
        }

        if (!limitToNextMonth && min != null && max != null) {
            add(stringResource(R.string.showcase_summary_active_window, minText.orEmpty(), maxText.orEmpty()))
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            overview.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.showcase_summary_active_rules_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (rules.isEmpty()) {
                Text(
                    text = stringResource(R.string.showcase_summary_no_rules),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                rules.forEach { rule ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "\u2022",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = rule,
                            modifier = Modifier.weight(1f, fill = true),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceCategoryLabel(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SectionCard(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val title = stringResource(titleRes)
    val subtitle = subtitleRes?.let { stringResource(it) }
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.92f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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

@Composable
private fun HighlightLegend(
    digitMode: DigitMode,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
) {
    val caption = stringResource(R.string.showcase_legend_title)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = caption,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val samples = listOf(
                SoleimaniDate(1403, 1, 13),
                SoleimaniDate(1403, 5, 1),
                SoleimaniDate(1403, 7, 15),
            )
            val events = samples.mapNotNull(eventIndicator)
            events.forEach { event -> EventLegendRow(event = event) }
            if (events.isEmpty()) {
                val message = stringResource(R.string.showcase_legend_disabled)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EventLegendRow(event: CalendarEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .height(28.dp)
                .widthIn(min = 28.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(event.color.copy(alpha = 0.2f)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = event.label ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarShowcasePreview() {
    val state = rememberCalendarShowcaseState(todayProvider = { PersionCalendar().toSoleimaniDate() })
    val config = DatePickerConfig(colors = DatePickerDefaults.colors())
    PersionCalendarTheme {
        CalendarShowcaseScreen(
            state = state,
            dialogConfig = config,
            today = state.today,
            constraintConfig = DatePickerConstraints(),
            digitMode = DigitMode.Persian,
            monthFormatter = MonthFormatter.Persian,
            yearFormatter = YearFormatter.Default,
            weekConfiguration = WeekConfiguration(),
            upcomingMilestone = state.today,
            eventIndicator = { null },
            rangeFormatter = RangeFormatter { a, b -> "$a → $b" },
        )
    }
}
