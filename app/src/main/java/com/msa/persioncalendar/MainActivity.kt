package com.msa.persioncalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.msa.calendar.ui.theme.PersionCalendarTheme
import com.msa.calendar.utils.CalendarResources
import com.msa.persioncalendar.showcase.CalendarShowcaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalendarResources.initialize(applicationContext)
        setContent {
            PersionCalendarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalendarShowcaseApp()
                }
            }
        }
    }
}


