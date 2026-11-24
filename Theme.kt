package com.akiba.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AkibaGreen = Color(0xFF1B7F5C)
val AkibaGold = Color(0xFFD99B26)
val AkibaBackground = Color(0xFFF6F8F7)

private val LightColors = lightColorScheme(
    primary = AkibaGreen,
    secondary = AkibaGold,
    background = AkibaBackground,
    surface = Color.White
)

@Composable
fun AkibaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
