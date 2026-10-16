package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = JanaColors.Green,
    onPrimary = Color.White,
    secondary = JanaColors.Blue,
    onSecondary = Color.White,
    background = JanaColors.AuthBackground,
    onBackground = JanaColors.Text,
    surface = Color.White,
    onSurface = JanaColors.Text,
    surfaceVariant = JanaColors.Field,
    outline = JanaColors.Line,
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
