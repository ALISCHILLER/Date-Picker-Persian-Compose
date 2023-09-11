@file:OptIn(ExperimentalTextApi::class)

package com.msa.persioncalendar

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.persioncalendar.ui.theme.*
import com.msa.persioncalendar.ui.view.DayOfWeekView
import com.msa.persioncalendar.ui.view.YearsView
import com.msa.persioncalendar.utils.Constants
import com.msa.persioncalendar.utils.PersionCalendar
import com.msa.persioncalendar.utils.PickerType
import com.msa.persioncalendar.utils.toPersianNumber

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun CalendarScreen(
    onDismiss: (Boolean) -> Unit,
) {
    val today = PersionCalendar().getDay()
    val month = PersionCalendar().getMonth()
    val year = PersionCalendar().getYear()

    val monthsList = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
        "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )
    var mMonth by remember {
        mutableStateOf(monthsList[month - 1])
    }

    var mYear by remember {
        mutableStateOf(year.toPersianNumber())
    }

    var mDay by remember {
        mutableStateOf(today.toPersianNumber())
    }
    var pickerType: PickerType by remember {
        mutableStateOf(PickerType.Day)
    }
    val yearListState = rememberLazyListState()
    GlobalScope.launch(Dispatchers.Main) {
        yearListState.scrollToItem(index = 45)
    }
    Dialog(
        onDismissRequest = { onDismiss(true) },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    // same action as in onDismissRequest
                    onDismiss(true)
                }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {

                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .animateContentSize()
                ) {

                    CalendarView(
                        mMonth = mMonth,
                        mDay = mDay,
                        mYear = mYear,
                        pickerTypeChang = { pickerType = it },
                        pickerType = pickerType
                    )

                    Crossfade(pickerType, label = "") {
                        when (it) {
                            PickerType.Day -> DayOfWeekView(
                                mMonth = mMonth,
                                mDay = mDay,
                                mYear = mYear,
                                setDay = { mDay = it },
                                {}
                            )

                            PickerType.Year -> YearsView(
                                mYear = mYear,
                                onYearClick={mYear= it},
                                yearListState= yearListState
                            )

                            PickerType.Month -> MonthView(
                                mMonth= mMonth,
                                onMonthClick={mMonth=it}
                            )

                            else -> DayOfWeekView(
                                mMonth = mMonth,
                                mDay = mDay,
                                mYear = mYear,
                                setDay = { mDay = it },
                                {}
                            )
                        }
                    }


                }
            }
        }
    }

}

@Composable
fun CalendarView(
    mMonth: String,
    mDay: String,
    mYear: String,
    pickerTypeChang: (PickerType) -> Unit,
    pickerType: (PickerType),
) {
    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = listOf(Color(0xFF2be4dc), Color(0xFF243484)),
                center = size.center,
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }
    Column(
        modifier = Modifier
            .background(
                largeRadialGradient
            )
            .animateContentSize()
    ) {
        val persion by remember {
            mutableStateOf(PersionCalendar())
            //   mutableStateOf(initialDate ?: PersionCalendar())
        }
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .padding(4.dp)
                    .background(largeRadialGradient),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "انتخاب تاریخ",
                    color = Color.White
                )


                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "$mYear $mMonth  $mDay",
                    color = Color.White,
                    style = TextStyle(),
                    fontWeight = FontWeight.Bold
                )

            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(largeRadialGradient),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // KeyboardArrowLeft
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(43.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowLeft),
                    contentDescription = "arrow",
                    tint = Color.White
                )
            }


            //Years
            TextButton(
                onClick = {
                    if (pickerType != PickerType.Year)
                        pickerTypeChang(PickerType.Year)
                    else
                        pickerTypeChang(PickerType.Day)
                },
                modifier = Modifier.padding(0.dp)
            ) {
                Text(
                    text = mYear,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White
                )
            }

            ///Month
            TextButton(
                onClick = {
                    if (pickerType != PickerType.Month)
                        pickerTypeChang(PickerType.Month)
                    else
                        pickerTypeChang(PickerType.Day)
                }
            )
            {
                Text(text = mMonth, color = Color.White)
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White
                )
            }

            // KeyboardArrowRight
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(46.dp)
            )
            {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowRight),
                    contentDescription = "Right",
                    tint = Color.White
                )

            }
        }

    }
}


@Composable
fun MonthView(
    mMonth: String,
    onMonthClick: (String) -> Unit,
) {

    val monthsList = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند",)


    val selectionModifier = Modifier
        .wrapContentHeight()
        .then(Modifier.padding(top = dimensionResource(R.dimen.scd_normal_150)))
    val baseViewModifier = Modifier
        .padding(top = dimensionResource(R.dimen.scd_normal_100))

    Column(
        modifier = selectionModifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "انتخاب ماه",
            style = MaterialTheme.typography.titleMedium,
        )
        LazyVerticalGrid(
            modifier = baseViewModifier,
            columns = GridCells.Fixed(3),
        ){
            items(monthsList){
                val selected = mMonth == it
                val thisMonth =  it ==PersionCalendar().getMonth().toPersianNumber()
                val disabled = mMonth.contains(it)
                val textStyle =
                    when {
                        selected -> MaterialTheme.typography.bodySmall.copy(MaterialTheme.colorScheme.onPrimary)
                        thisMonth -> MaterialTheme.typography.titleSmall.copy(MaterialTheme.colorScheme.primary)
                        else -> MaterialTheme.typography.bodyMedium
                    }

                val baseModifier = Modifier
                    .wrapContentWidth()
                    .padding(dimensionResource(R.dimen.scd_small_50))
                    .clickable(!disabled) { onMonthClick(it) }

                val normalModifier = baseModifier
                    .clip(MaterialTheme.shapes.small)

                val selectedModifier = normalModifier
                    .background(MaterialTheme.colorScheme.primary)

                val textAlpha = when {
                    disabled -> Color.Blue
                    else -> Color.Black
                }


                Column(
                    modifier = when {
                        disabled -> normalModifier
                        selected -> selectedModifier
                        thisMonth -> baseModifier
                        else -> normalModifier
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(R.dimen.scd_small_150))
                            .padding(vertical = dimensionResource(R.dimen.scd_small_100)),
                        text =it ,
                        color=textAlpha,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }

            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CalendarScreenPreview() {
    var hideDatePicker by remember {
        mutableStateOf(true)
    }
    CalendarScreen(
        onDismiss = { hideDatePicker = true }
    )
}

