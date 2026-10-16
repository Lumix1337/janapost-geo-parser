package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.ui.nav.JanaNavHost
import com.example.myapplication.ui.theme.JanaColors
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.light(JanaColors.AuthBackground.toArgb(), Color.Transparent.toArgb()),
        )
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = JanaColors.AuthBackground) {
                    JanaNavHost()
                }
            }
        }
    }
}
