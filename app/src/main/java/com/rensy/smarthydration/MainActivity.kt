package com.rensy.smarthydration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rensy.smarthydration.navigation.AppNavigation
import com.rensy.smarthydration.ui.theme.SmarthydrationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmarthydrationTheme {
                AppNavigation()
            }
        }
    }
}