package com.msa.persioncalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.msa.calendar.CalendarScreen
import com.msa.calendar.RangeCalendarScreen
import com.msa.calendar.ui.theme.PersionCalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersionCalendarTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                    ,
                    color = MaterialTheme.colorScheme.background,
                ) {

                    var hideDatePicker by remember {
                        mutableStateOf(true)
                    }
                    var setDate by remember {
                        mutableStateOf("")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ){


                        Button(
                            onClick = { hideDatePicker = false }
                        ) {
                            Text("انتخاب تاریخ")
                        }
                        if (!hideDatePicker) {
                            // *************************************************
//                            CalendarScreen(
//                                onDismiss = { hideDatePicker = true },
//                                onConfirm = { setDate = it }
//                            )
                            RangeCalendarScreen(
                                onDismiss = { hideDatePicker = true },
                                setDate = { list ->
                                    var startDateMap = list[0] //mapOf("day" to day, "month" to month, "year" to year)
                                    var endDateMap = list[1] //mapOf("day" to day, "month" to month, "year" to year)
                                    setDate =(startDateMap + endDateMap).toString()
                                }
                            )
                            // *************************************************
                        }
                        Text(text = setDate)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PersionCalendarTheme {
        var hideDatePicker by remember {
            mutableStateOf(true)
        }
        com.msa.calendar.CalendarScreen(
            onDismiss = { hideDatePicker = true },
            {}
        )
    }
}