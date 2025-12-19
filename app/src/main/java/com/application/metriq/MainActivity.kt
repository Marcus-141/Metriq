package com.application.metriq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.application.metriq.ui.MainScreen
import com.application.metriq.ui.theme.MetriqTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetriqTheme {
                MainScreen()
            }
        }
    }
}
