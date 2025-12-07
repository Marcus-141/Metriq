package com.application.metriq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.application.metriq.ui.AppNavHost
import com.application.metriq.ui.theme.MetriqTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetriqTheme {
                AppNavHost()
            }
        }
    }
}
