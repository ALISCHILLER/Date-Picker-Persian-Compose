package com.msa.persioncalendar.showcase

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msa.calendar.PersianDatePickerDialog
import com.msa.calendar.PersianDateRangePickerDialog
import com.msa.calendar.ui.CalendarEvent
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.ui.theme.PersianCalendarTheme
import com.msa.calendar.utils.PersianCalendar
import com.msa.calendar.utils.SoleimaniDate
import com.msa.calendar.utils.toSoleimaniDate
import com.msa.persioncalendar.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarShowcaseScreen(
    modifier: Modifier = Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {

    val colorScheme = MaterialTheme.colorScheme
    val backgroundBrush = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.12f),
                colorScheme.surface,
                colorScheme.background,
            ),
        )
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        ShowcaseDialogs(
            state = state,
            dialogConfig = uiState.dialogConfig,
        )

        Scaffold(
            topBar = { ShowcaseTopAppBar() },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            ShowcaseContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = state,
                uiState = uiState,
            )
        }
    }
}

@Composable
private fun ShowcaseDialogs(
    state: CalendarShowcaseState,
    dialogConfig: DatePickerConfig,
) {

    if (state.showSinglePicker) {
        PersianDatePickerDialog(
            onClose = { state.dismissPickers() },
            config = dialogConfig,
            onSelectionConfirmed = state::onSingleSelectionConfirmed,
        )
    }

    if (state.showRangePicker) {
        PersianDateRangePickerDialog(
            onClose = { state.dismissPickers() },
            config = dialogConfig,
            onSelectionConfirmed = state::onRangeSelectionConfirmed,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowcaseTopAppBar() {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(R.string.showcase_appbar_title)) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}


private fun Modifier.showcaseContentWidth(): Modifier =
    this
        .fillMaxWidth()
        .widthIn(max = 920.dp)

@Composable
private fun ShowcaseContent(
    modifier: Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier) {
        val horizontalPadding = if (maxWidth < 360.dp) 16.dp else 24.dp
        val sectionSpacing = if (maxWidth < 360.dp) 16.dp else 20.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = 16.dp)
                .navigationBarsPadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            IntroCard(modifier = Modifier.showcaseContentWidth())

            Spacer(modifier = Modifier.height(sectionSpacing + 4.dp))

            SectionCard(
                titleRes = R.string.showcase_section_experience_title,
                subtitleRes = R.string.showcase_section_experience_subtitle,
                modifier = Modifier.showcaseContentWidth(),
            ) {
                PreferencesSection(state = state)
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            SectionCard(
                titleRes = R.string.showcase_section_quick_title,
                subtitleRes = R.string.showcase_section_quick_subtitle,
                modifier = Modifier.showcaseContentWidth(),
            ) {
                QuickActionsSection(
                    state = state,
                    today = uiState.today,
                )
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            SectionCard(
                titleRes = R.string.showcase_section_report_title,
                subtitleRes = R.string.showcase_section_report_subtitle,
                modifier = Modifier.showcaseContentWidth(),
            ) {
                SelectionSummaryCard(
                    state = state,
                    uiState = uiState,
                )
            }

            Spacer(modifier = Modifier.height(sectionSpacing + 4.dp))

            HighlightLegend(
                modifier = Modifier.showcaseContentWidth(),
                formatting = uiState.formatting,
                eventIndicator = uiState.dialogConfig.eventIndicator,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntroCard(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val heroShape = RoundedCornerShape(32.dp)
    val heroBrush = remember(colorScheme) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.96f),
                colorScheme.secondary.copy(alpha = 0.88f),
            )
        )
    }

    Surface(
        modifier = modifier.clip(heroShape),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .background(heroBrush, heroShape)
                .heightIn(min = 214.dp),
        ) {
            val compact = maxWidth < 380.dp
            val heroPadding = if (compact) 18.dp else 24.dp
            val badgeSpacing = if (compact) 6.dp else 8.dp

            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.16f),
                    radius = size.maxDimension * 0.34f,
                    center = Offset(size.width * 0.12f, size.height * 0.08f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.maxDimension * 0.42f,
                    center = Offset(size.width * 0.92f, size.height * 0.95f),
                )
            }

            Column(
                modifier = Modifier.padding(heroPadding),
                verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp),
            ) {
                Text(
                    text = stringResource(R.string.showcase_hero_eyebrow).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.showcase_hero_title),
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (compact) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.showcase_intro_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = if (compact) 4 else 3,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(badgeSpacing),
                    verticalArrangement = Arrangement.spacedBy(badgeSpacing),
                ) {
                    HeroBadge(text = stringResource(R.string.showcase_hero_badge_typed_api))
                    HeroBadge(text = stringResource(R.string.showcase_hero_badge_rtl))
                    HeroBadge(text = stringResource(R.string.showcase_hero_badge_constraints))
                }
            }
        }
    }
}

@Composable
private fun HeroBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.94f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
            titleRes = R.string.showcase_toggle_transliterated_months_title,
            subtitleRes = R.string.showcase_toggle_transliterated_months_subtitle,
            checked = state.useTransliteratedMonthLabels,
            onCheckedChange = { state.useTransliteratedMonthLabels = it },
        )
        PreferenceToggle(
            titleRes = R.string.showcase_toggle_gregorian_year_title,
            subtitleRes = R.string.showcase_toggle_gregorian_year_subtitle,
            checked = state.showGregorianYearHint,
            onCheckedChange = { state.showGregorianYearHint = it },
        )

        HorizontalDivider()

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

@OptIn(ExperimentalLayoutApi::class)
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
                val label = stringResource(labelRes)
                LocaleChoiceChip(
                    text = label,
                    selected = state.localeOption == option,
                    onClick = { state.onLocaleOptionSelected(option) },
                )
            }
        }
    }
}

@Composable
private fun LocaleChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val selectedBrush = remember(colorScheme) {
        Brush.horizontalGradient(
            listOf(colorScheme.primary, colorScheme.secondary)
        )
    }
    val backgroundColor = if (selected) Color.Transparent else colorScheme.surfaceVariant
    val contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 44.dp)
            .semantics {
                role = Role.Button
                contentDescription = text
                if (selected) this.selected = true
            },
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = if (selected) 3.dp else 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                colorScheme.secondary.copy(alpha = 0.42f)
            } else {
                colorScheme.outlineVariant
            },
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .then(if (selected) Modifier.background(selectedBrush, shape) else Modifier)
                .padding(horizontal = 15.dp, vertical = 9.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
    val shape = RoundedCornerShape(22.dp)
    val colorScheme = MaterialTheme.colorScheme
    val backgroundBrush = remember(checked, colorScheme) {
        Brush.linearGradient(
            colors = if (checked) {
                listOf(
                    colorScheme.primary.copy(alpha = 0.18f),
                    colorScheme.secondary.copy(alpha = 0.12f),
                )
            } else {
                listOf(
                    colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.96f),
                    colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.9f),
                )
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color.Transparent,
        tonalElevation = if (checked) 3.dp else 1.dp,
        shadowElevation = if (checked) 3.dp else 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (checked) {
                colorScheme.primary.copy(alpha = 0.28f)
            } else {
                colorScheme.outline.copy(alpha = 0.14f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .background(backgroundBrush, shape)
                .toggleable(
                    value = checked,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.onPrimary,
                    checkedTrackColor = colorScheme.primary,
                    uncheckedTrackColor = colorScheme.surfaceVariant,
                ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        ShowcaseActionButton(
            text = stringResource(R.string.showcase_action_single_picker),
            primary = true,
            onClick = state::openSinglePicker,
        )

        ShowcaseActionButton(
            text = stringResource(R.string.showcase_action_range_picker),
            primary = true,
            onClick = state::openRangePicker,
        )

        ShowcaseActionButton(
            text = stringResource(R.string.showcase_action_today),
            primary = false,
            onClick = { state.onQuickTodaySelected(today) },
        )

        ShowcaseActionButton(
            text = stringResource(R.string.showcase_action_clear),
            primary = false,
            onClick = state::clearSelection,
        )

        ShowcaseActionButton(
            text = stringResource(R.string.showcase_action_repeat_last),
            primary = false,
            enabled = state.lastSelectionType != null,
            onClick = {
                when (state.lastSelectionType) {
                    SelectionType.Single -> state.openSinglePicker()
                    SelectionType.Range -> state.openRangePicker()
                    SelectionType.QuickToday -> state.onQuickTodaySelected(today)
                    null -> Unit
                }
            },
        )
    }
}

@Composable
private fun ShowcaseActionButton(
    text: String,
    primary: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val activeBrush = remember(colorScheme) {
        Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary))
    }
    val background = when {
        primary && enabled -> Color.Transparent
        enabled -> colorScheme.surfaceVariant
        else -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = when {
        primary && enabled -> colorScheme.onPrimary
        enabled -> colorScheme.onSurface
        else -> colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .widthIn(min = 132.dp)
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Button
                contentDescription = text
                if (!enabled) disabled()
            },
        shape = shape,
        color = background,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = if (primary && enabled) 4.dp else 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (primary && enabled) {
                colorScheme.secondary.copy(alpha = 0.4f)
            } else {
                colorScheme.outlineVariant
            },
        ),
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (primary && enabled) Modifier.background(activeBrush, shape) else Modifier
                )
                .padding(horizontal = 18.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SelectionSummaryCard(
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val singleTitle = stringResource(R.string.showcase_summary_single_title)
        val rangeTitle = stringResource(R.string.showcase_summary_range_title)
        val noSingleValue = stringResource(R.string.showcase_summary_none_single)
        val noRangeValue = stringResource(R.string.showcase_summary_none_range)
        val milestoneTitle = stringResource(R.string.showcase_summary_milestone_title)
        val formatting = uiState.formatting
        val digitMode = formatting.digitMode
        val monthFormatter = formatting.monthFormatter
        val yearFormatter = formatting.yearFormatter


        SelectionSummaryRow(
            title = singleTitle,
            value = state.selectedSingleDate
                ?.toDisplayString(digitMode, monthFormatter, yearFormatter)
                ?: noSingleValue,
        )
        SelectionSummaryRow(
            title = rangeTitle,
            value = state.selectedRange
                ?.toDisplayString(digitMode, monthFormatter, yearFormatter, formatting.rangeFormatter)
                ?: noRangeValue,
        )
        SelectionSummaryRow(
            title = milestoneTitle,
            value = uiState.upcomingMilestone.toDisplayString(digitMode, monthFormatter, yearFormatter),
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

        HorizontalDivider()

        ConstraintSummary(
            uiState = uiState,
            limitToNextMonth = state.limitToNextMonth,
            blockFridays = state.blockFridays,
            blockThirteenth = state.blockThirteenth,
            limitRangeLength = state.limitRangeLength,
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ConstraintSummary(
    uiState: CalendarShowcaseUiState,
    limitToNextMonth: Boolean,
    blockFridays: Boolean,
    blockThirteenth: Boolean,
    limitRangeLength: Boolean,
) {
    val constraints = uiState.constraints
    val formatting = uiState.formatting
    val digitMode = formatting.digitMode
    val monthFormatter = formatting.monthFormatter
    val yearFormatter = formatting.yearFormatter
    val weekConfiguration = uiState.weekConfiguration
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
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    val cardBrush = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.surfaceColorAtElevation(5.dp).copy(alpha = 0.96f),
                colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.90f),
            )
        )
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush, shape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun HighlightLegend(
    modifier: Modifier = Modifier,
    formatting: CalendarFormatting,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
) {
    val caption = stringResource(R.string.showcase_legend_title)
    Surface(
        modifier = modifier,
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
    val state = rememberCalendarShowcaseState(todayProvider = { PersianCalendar().toSoleimaniDate() })
    PersianCalendarTheme {
        val uiState = rememberCalendarShowcaseUiState(state)
        CalendarShowcaseScreen(
            state = state,
            uiState = uiState,
        )
    }
}
