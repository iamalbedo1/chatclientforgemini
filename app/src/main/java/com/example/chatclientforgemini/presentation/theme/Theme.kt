package com.example.chatclientforgemini.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val DarkColors = Colors(
    primary        = Color.White,
    secondary      = Color.White,
    background     = Color.Black,
    surface        = Color.Black,
    onPrimary      = Color.Black,
    onSecondary    = Color.Black,
    onBackground   = Color.White,
    onSurface      = Color.White,
)

@Composable
fun ChatClientForGeminiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = DarkColors, content = content)
}
