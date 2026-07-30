package com.example.consumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.msa.calendar.PersianDatePickerDialog
import com.msa.calendar.rememberSingleDatePickerState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val pickerState = rememberSingleDatePickerState()
                var visible by remember { mutableStateOf(false) }

                Button(onClick = { visible = true }) {
                    Text(pickerState.selectedDate?.toString() ?: "Select date")
                }

                if (visible) {
                    PersianDatePickerDialog(
                        state = pickerState,
                        onClose = { visible = false },
                        onSelectionConfirmed = { visible = false },
                    )
                }
            }
        }
    }
}
