package com.msa.persiancalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.msa.calendar.ui.theme.PersianCalendarTheme
import com.msa.persiancalendar.showcase.CalendarShowcaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersianCalendarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalendarShowcaseApp()
                }
            }
        }
    }
}


