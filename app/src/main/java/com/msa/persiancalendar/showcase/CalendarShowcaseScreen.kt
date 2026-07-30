package com.msa.persiancalendar.showcase

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.msa.persiancalendar.R

private val ShowcaseMaxWidth = 1080.dp
private val ShowcaseCardShape = RoundedCornerShape(28.dp)
private val ShowcaseTileShape = RoundedCornerShape(22.dp)

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
                colorScheme.primary.copy(alpha = 0.11f),
                colorScheme.background,
                colorScheme.surface,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        AmbientBackground()
        ShowcaseDialogs(state = state, dialogConfig = uiState.dialogConfig)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                ShowcaseTopAppBar(
                    onRepeatLast = {
                        when (state.lastSelectionType) {
                            SelectionType.Single -> state.openSinglePicker()
                            SelectionType.Range -> state.openRangePicker()
                            SelectionType.QuickToday -> state.onQuickTodaySelected(uiState.today)
                            null -> Unit
                        }
                    },
                    repeatEnabled = state.lastSelectionType != null,
                )
            },
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
private fun AmbientBackground() {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = primary.copy(alpha = 0.055f),
            radius = size.maxDimension * 0.35f,
            center = Offset(size.width * 0.04f, size.height * 0.16f),
        )
        drawCircle(
            color = secondary.copy(alpha = 0.05f),
            radius = size.maxDimension * 0.28f,
            center = Offset(size.width * 0.96f, size.height * 0.78f),
        )
    }
}

@Composable
private fun ShowcaseDialogs(
    state: CalendarShowcaseState,
    dialogConfig: DatePickerConfig,
) {
    if (state.showSinglePicker) {
        PersianDatePickerDialog(
            initialDate = state.selectedSingleDate,
            onClose = { state.dismissPickers() },
            config = dialogConfig,
            onSelectionConfirmed = state::onSingleSelectionConfirmed,
        )
    }

    if (state.showRangePicker) {
        PersianDateRangePickerDialog(
            initialStartDate = state.selectedRange?.start,
            initialEndDate = state.selectedRange?.end,
            onClose = { state.dismissPickers() },
            config = dialogConfig,
            onSelectionConfirmed = state::onRangeSelectionConfirmed,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowcaseTopAppBar(
    repeatEnabled: Boolean,
    onRepeatLast: () -> Unit,
) {
    val repeatLabel = stringResource(R.string.showcase_action_repeat_last)
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(8.dp).size(20.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.showcase_appbar_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        actions = {
            IconButton(
                onClick = onRepeatLast,
                enabled = repeatEnabled,
                modifier = Modifier.semantics {
                    contentDescription = repeatLabel
                    role = Role.Button
                    if (!repeatEnabled) disabled()
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun ShowcaseContent(
    modifier: Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {
    BoxWithConstraints(modifier = modifier) {
        val compact = maxWidth < 380.dp
        val horizontalPadding = when {
            maxWidth >= 900.dp -> 32.dp
            compact -> 14.dp
            else -> 20.dp
        }
        val verticalSpacing = if (compact) 14.dp else 18.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 16.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                HeroCard(
                    modifier = Modifier.showcaseContentWidth(),
                    state = state,
                    uiState = uiState,
                )
            }

            item {
                PickerWorkspace(
                    modifier = Modifier.showcaseContentWidth(),
                    state = state,
                    uiState = uiState,
                )
            }

            item {
                AdaptiveDashboardRow(
                    modifier = Modifier.showcaseContentWidth(),
                    state = state,
                    uiState = uiState,
                    wide = maxWidth >= 820.dp,
                )
            }

            item {
                CustomizationCard(
                    modifier = Modifier.showcaseContentWidth(),
                    state = state,
                )
            }

            item {
                EventLegendCard(
                    modifier = Modifier.showcaseContentWidth(),
                    eventIndicator = uiState.dialogConfig.eventIndicator,
                )
            }

            item { Spacer(modifier = Modifier.navigationBarsPadding()) }
        }
    }
}

private fun Modifier.showcaseContentWidth(): Modifier =
    fillMaxWidth().widthIn(max = ShowcaseMaxWidth)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroCard(
    modifier: Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {
    val colors = MaterialTheme.colorScheme
    val heroBrush = remember(colors) {
        Brush.linearGradient(
            colors = listOf(
                colors.primary,
                colors.primary.copy(alpha = 0.9f),
                colors.secondary.copy(alpha = 0.92f),
            ),
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.17f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroBrush)
                .padding(24.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = size.maxDimension * 0.34f,
                    center = Offset(size.width * 0.02f, size.height * 0.05f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.maxDimension * 0.3f,
                    center = Offset(size.width * 0.97f, size.height * 0.95f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(13.dp).size(30.dp),
                        )
                    }
                    StatusBadge(
                        text = stringResource(R.string.showcase_hero_badge_typed_api),
                        icon = Icons.Rounded.Check,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        text = stringResource(R.string.showcase_hero_eyebrow),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.showcase_hero_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.showcase_intro_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.86f),
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeroBadge(stringResource(R.string.showcase_hero_badge_rtl))
                    HeroBadge(stringResource(R.string.showcase_hero_badge_constraints))
                    HeroBadge(
                        uiState.today.toDisplayString(
                            uiState.formatting.digitMode,
                            uiState.formatting.monthFormatter,
                            uiState.formatting.yearFormatter,
                        ),
                    )
                }

                AnimatedVisibility(
                    visible = state.selectedSingleDate != null || state.selectedRange != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    ) {
                        Text(
                            text = selectedSummaryText(state, uiState),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, icon: ImageVector) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun HeroBadge(text: String) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = Color.White.copy(alpha = 0.94f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PickerWorkspace(
    modifier: Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {
    ShowcaseSectionCard(
        modifier = modifier,
        title = stringResource(R.string.showcase_section_quick_title),
        subtitle = stringResource(R.string.showcase_section_quick_subtitle),
        icon = Icons.Rounded.CalendarMonth,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val wide = maxWidth >= 620.dp
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PickerModeCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.showcase_action_single_picker),
                        subtitle = state.selectedSingleDate?.toDisplayString(
                            uiState.formatting.digitMode,
                            uiState.formatting.monthFormatter,
                            uiState.formatting.yearFormatter,
                        ) ?: stringResource(R.string.showcase_summary_none_single),
                        icon = Icons.Rounded.Today,
                        primary = true,
                        onClick = state::openSinglePicker,
                    )
                    PickerModeCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.showcase_action_range_picker),
                        subtitle = state.selectedRange?.toDisplayString(
                            uiState.formatting.digitMode,
                            uiState.formatting.monthFormatter,
                            uiState.formatting.yearFormatter,
                            uiState.formatting.rangeFormatter,
                        ) ?: stringResource(R.string.showcase_summary_none_range),
                        icon = Icons.Rounded.DateRange,
                        primary = false,
                        onClick = state::openRangePicker,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerModeCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.showcase_action_single_picker),
                        subtitle = state.selectedSingleDate?.toDisplayString(
                            uiState.formatting.digitMode,
                            uiState.formatting.monthFormatter,
                            uiState.formatting.yearFormatter,
                        ) ?: stringResource(R.string.showcase_summary_none_single),
                        icon = Icons.Rounded.Today,
                        primary = true,
                        onClick = state::openSinglePicker,
                    )
                    PickerModeCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.showcase_action_range_picker),
                        subtitle = state.selectedRange?.toDisplayString(
                            uiState.formatting.digitMode,
                            uiState.formatting.monthFormatter,
                            uiState.formatting.yearFormatter,
                            uiState.formatting.rangeFormatter,
                        ) ?: stringResource(R.string.showcase_summary_none_range),
                        icon = Icons.Rounded.DateRange,
                        primary = false,
                        onClick = state::openRangePicker,
                    )
                }
            }
        }

        QuickActionRow(state = state, today = uiState.today)
    }
}

@Composable
private fun PickerModeCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val brush = remember(colors, primary) {
        if (primary) {
            Brush.linearGradient(listOf(colors.primary, colors.secondary))
        } else {
            Brush.linearGradient(
                listOf(
                    colors.secondaryContainer,
                    colors.primaryContainer.copy(alpha = 0.7f),
                ),
            )
        }
    }
    val contentColor = if (primary) colors.onPrimary else colors.onSurface
    val secondaryColor = if (primary) colors.onPrimary.copy(alpha = 0.78f) else colors.onSurfaceVariant

    Card(
        modifier = modifier
            .heightIn(min = 150.dp)
            .semantics {
                role = Role.Button
                contentDescription = title
            },
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (primary) 8.dp else 2.dp),
        border = BorderStroke(
            1.dp,
            if (primary) Color.White.copy(alpha = 0.16f) else colors.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = if (primary) Color.White.copy(alpha = 0.16f) else colors.surface.copy(alpha = 0.72f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.padding(10.dp).size(23.dp),
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = title,
                    color = contentColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    color = secondaryColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun QuickActionRow(state: CalendarShowcaseState, today: SoleimaniDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalButton(
            onClick = { state.onQuickTodaySelected(today) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Icon(Icons.Rounded.Today, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text(stringResource(R.string.showcase_action_today), maxLines = 1)
        }
        OutlinedButton(
            onClick = state::clearSelection,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text(stringResource(R.string.showcase_action_clear), maxLines = 1)
        }
    }
}

@Composable
private fun AdaptiveDashboardRow(
    modifier: Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
    wide: Boolean,
) {
    if (wide) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SelectionSummaryCard(
                modifier = Modifier.weight(1.05f),
                state = state,
                uiState = uiState,
            )
            ActiveRulesCard(
                modifier = Modifier.weight(0.95f),
                state = state,
                uiState = uiState,
            )
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SelectionSummaryCard(Modifier.fillMaxWidth(), state, uiState)
            ActiveRulesCard(Modifier.fillMaxWidth(), state, uiState)
        }
    }
}

@Composable
private fun SelectionSummaryCard(
    modifier: Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {
    ShowcaseSectionCard(
        modifier = modifier,
        title = stringResource(R.string.showcase_section_report_title),
        subtitle = stringResource(R.string.showcase_section_report_subtitle),
        icon = Icons.Rounded.Check,
    ) {
        SummaryValue(
            icon = Icons.Rounded.Today,
            label = stringResource(R.string.showcase_summary_single_title),
            value = state.selectedSingleDate?.toDisplayString(
                uiState.formatting.digitMode,
                uiState.formatting.monthFormatter,
                uiState.formatting.yearFormatter,
            ) ?: stringResource(R.string.showcase_summary_none_single),
            highlighted = state.selectedSingleDate != null,
        )
        SummaryValue(
            icon = Icons.Rounded.DateRange,
            label = stringResource(R.string.showcase_summary_range_title),
            value = state.selectedRange?.toDisplayString(
                uiState.formatting.digitMode,
                uiState.formatting.monthFormatter,
                uiState.formatting.yearFormatter,
                uiState.formatting.rangeFormatter,
            ) ?: stringResource(R.string.showcase_summary_none_range),
            highlighted = state.selectedRange != null,
        )
        SummaryValue(
            icon = Icons.Rounded.Event,
            label = stringResource(R.string.showcase_summary_milestone_title),
            value = uiState.upcomingMilestone.toDisplayString(
                uiState.formatting.digitMode,
                uiState.formatting.monthFormatter,
                uiState.formatting.yearFormatter,
            ),
            highlighted = false,
        )
    }
}

@Composable
private fun SummaryValue(
    icon: ImageVector,
    label: String,
    value: String,
    highlighted: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val container = if (highlighted) colors.primaryContainer.copy(alpha = 0.7f) else colors.surfaceVariant.copy(alpha = 0.58f)
    Surface(
        shape = ShowcaseTileShape,
        color = container,
        border = BorderStroke(
            1.dp,
            if (highlighted) colors.primary.copy(alpha = 0.22f) else colors.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (highlighted) colors.primary else colors.surface,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlighted) colors.onPrimary else colors.primary,
                    modifier = Modifier.padding(9.dp).size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                AnimatedContent(
                    targetState = value,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "summary-value",
                ) { animatedValue ->
                    Text(
                        text = animatedValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveRulesCard(
    modifier: Modifier,
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
) {
    val limitWindowLabel = stringResource(R.string.showcase_toggle_limit_30_title)
    val weekendLabel = stringResource(R.string.showcase_toggle_disable_weekend_title)
    val thirteenthLabel = stringResource(R.string.showcase_toggle_block_13_title)
    val rangeLimitLabel = stringResource(R.string.showcase_toggle_limit_range_title)
    val internationalWeekLabel = stringResource(R.string.showcase_toggle_international_week_title)
    val eventsLabel = stringResource(R.string.showcase_toggle_highlight_events_title)
    val activeRules = buildList {
        if (state.limitToNextMonth) add(limitWindowLabel)
        if (state.blockFridays) add(weekendLabel)
        if (state.blockThirteenth) add(thirteenthLabel)
        if (state.limitRangeLength) add(rangeLimitLabel)
        if (state.useInternationalWeek) add(internationalWeekLabel)
        if (state.highlightEvents) add(eventsLabel)
    }

    ShowcaseSectionCard(
        modifier = modifier,
        title = stringResource(R.string.showcase_summary_active_rules_title),
        subtitle = stringResource(R.string.showcase_summary_week_start, uiState.weekConfiguration.startDay.toDisplayName(state.useLatinDigits)),
        icon = Icons.Rounded.Settings,
    ) {
        if (activeRules.isEmpty()) {
            EmptyStateLine(stringResource(R.string.showcase_summary_no_rules))
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                activeRules.forEach { rule -> ActiveRuleChip(rule) }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            text = constraintWindowText(state, uiState),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyStateLine(text: String) {
    Surface(
        shape = ShowcaseTileShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActiveRuleChip(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun CustomizationCard(
    modifier: Modifier,
    state: CalendarShowcaseState,
) {
    ShowcaseSectionCard(
        modifier = modifier,
        title = stringResource(R.string.showcase_section_experience_title),
        subtitle = stringResource(R.string.showcase_section_experience_subtitle),
        icon = Icons.Rounded.Settings,
    ) {
        LanguageSelector(state)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SettingsGroup(
            title = stringResource(R.string.showcase_category_display),
            settings = listOf(
                SettingModel(
                    R.string.showcase_toggle_latin_digits_title,
                    R.string.showcase_toggle_latin_digits_subtitle,
                    state.useLatinDigits,
                    { state.useLatinDigits = it },
                ),
                SettingModel(
                    R.string.showcase_toggle_transliterated_months_title,
                    R.string.showcase_toggle_transliterated_months_subtitle,
                    state.useTransliteratedMonthLabels,
                    { state.useTransliteratedMonthLabels = it },
                ),
                SettingModel(
                    R.string.showcase_toggle_gregorian_year_title,
                    R.string.showcase_toggle_gregorian_year_subtitle,
                    state.showGregorianYearHint,
                    { state.showGregorianYearHint = it },
                ),
            ),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SettingsGroup(
            title = stringResource(R.string.showcase_category_behavior),
            settings = listOf(
                SettingModel(R.string.showcase_toggle_today_shortcut_title, R.string.showcase_toggle_today_shortcut_subtitle, state.showTodayShortcut) { state.showTodayShortcut = it },
                SettingModel(R.string.showcase_toggle_limit_30_title, R.string.showcase_toggle_limit_30_subtitle, state.limitToNextMonth) { state.limitToNextMonth = it },
                SettingModel(R.string.showcase_toggle_disable_weekend_title, R.string.showcase_toggle_disable_weekend_subtitle, state.blockFridays) { state.blockFridays = it },
                SettingModel(R.string.showcase_toggle_block_13_title, R.string.showcase_toggle_block_13_subtitle, state.blockThirteenth) { state.blockThirteenth = it },
                SettingModel(R.string.showcase_toggle_enable_clear_title, R.string.showcase_toggle_enable_clear_subtitle, state.enableClearAction) { state.enableClearAction = it },
                SettingModel(R.string.showcase_toggle_international_week_title, R.string.showcase_toggle_international_week_subtitle, state.useInternationalWeek) { state.useInternationalWeek = it },
                SettingModel(R.string.showcase_toggle_highlight_events_title, R.string.showcase_toggle_highlight_events_subtitle, state.highlightEvents) { state.highlightEvents = it },
                SettingModel(R.string.showcase_toggle_limit_range_title, R.string.showcase_toggle_limit_range_subtitle, state.limitRangeLength) { state.limitRangeLength = it },
            ),
        )
    }
}

private data class SettingModel(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
)

@Composable
private fun SettingsGroup(title: String, settings: List<SettingModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        settings.forEach { setting ->
            SettingRow(
                title = stringResource(setting.title),
                subtitle = stringResource(setting.subtitle),
                checked = setting.checked,
                onCheckedChange = setting.onCheckedChange,
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (checked) colors.primaryContainer.copy(alpha = 0.55f) else colors.surfaceVariant.copy(alpha = 0.46f),
        border = BorderStroke(
            1.dp,
            if (checked) colors.primary.copy(alpha = 0.22f) else colors.outlineVariant,
        ),
        onClick = { onCheckedChange(!checked) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onPrimary,
                    checkedTrackColor = colors.primary,
                    uncheckedThumbColor = colors.onSurfaceVariant,
                    uncheckedTrackColor = colors.surfaceVariant,
                ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageSelector(state: CalendarShowcaseState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.showcase_language_selector_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LocaleChip(LocaleOption.System, R.string.showcase_locale_system, state)
            LocaleChip(LocaleOption.Persian, R.string.showcase_locale_persian, state)
            LocaleChip(LocaleOption.English, R.string.showcase_locale_english, state)
        }
    }
}

@Composable
private fun LocaleChip(
    option: LocaleOption,
    @StringRes labelRes: Int,
    state: CalendarShowcaseState,
) {
    val selected = state.localeOption == option
    val label = stringResource(labelRes)
    FilterChip(
        selected = selected,
        onClick = { state.onLocaleOptionSelected(option) },
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        leadingIcon = if (selected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(17.dp)) }
        } else null,
        modifier = Modifier.semantics {
            contentDescription = label
            role = Role.Button
            if (selected) this.selected = true
        },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun EventLegendCard(
    modifier: Modifier,
    eventIndicator: (SoleimaniDate) -> CalendarEvent?,
) {
    val samples = listOf(
        SoleimaniDate(1403, 1, 13),
        SoleimaniDate(1403, 5, 1),
        SoleimaniDate(1403, 7, 15),
    )
    val events = samples.mapNotNull(eventIndicator)

    ShowcaseSectionCard(
        modifier = modifier,
        title = stringResource(R.string.showcase_legend_title),
        subtitle = if (events.isEmpty()) stringResource(R.string.showcase_legend_disabled) else null,
        icon = Icons.Rounded.Event,
    ) {
        if (events.isEmpty()) {
            EmptyStateLine(stringResource(R.string.showcase_legend_disabled))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                events.take(3).forEach { event ->
                    EventLegendItem(event = event, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EventLegendItem(event: CalendarEvent, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = event.color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, event.color.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(event.color),
            )
            Text(
                text = event.label.orEmpty(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ShowcaseSectionCard(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = ShowcaseCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.96f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun selectedSummaryText(
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
): String {
    val formatting = uiState.formatting
    return when {
        state.selectedRange != null -> state.selectedRange!!.toDisplayString(
            formatting.digitMode,
            formatting.monthFormatter,
            formatting.yearFormatter,
            formatting.rangeFormatter,
        )
        state.selectedSingleDate != null -> state.selectedSingleDate!!.toDisplayString(
            formatting.digitMode,
            formatting.monthFormatter,
            formatting.yearFormatter,
        )
        else -> stringResource(R.string.showcase_summary_no_rules)
    }
}

@Composable
private fun constraintWindowText(
    state: CalendarShowcaseState,
    uiState: CalendarShowcaseUiState,
): String {
    val constraints = uiState.constraints
    val formatting = uiState.formatting
    val min = constraints.minDate
    val max = constraints.maxDate
    return if (state.limitToNextMonth && min != null && max != null) {
        stringResource(
            R.string.showcase_summary_limit_between,
            min.toDisplayString(formatting.digitMode, formatting.monthFormatter, formatting.yearFormatter),
            max.toDisplayString(formatting.digitMode, formatting.monthFormatter, formatting.yearFormatter),
        )
    } else {
        stringResource(R.string.showcase_summary_limit_between_none)
    }
}

@Preview(showBackground = true, widthDp = 430, heightDp = 1100)
@Composable
private fun CalendarShowcasePreview() {
    PersianCalendarTheme {
        val state = rememberCalendarShowcaseState(
            todayProvider = { PersianCalendar().toSoleimaniDate() },
        )
        val uiState = rememberCalendarShowcaseUiState(state)
        CalendarShowcaseScreen(state = state, uiState = uiState)
    }
}
