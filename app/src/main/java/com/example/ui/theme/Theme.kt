package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = InkNavy,
    secondary = GoldAccent,
    background = Parchment,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = InkNavy,
    onSurface = InkNavy
)

private val DarkColors = darkColorScheme(
    primary = GoldAccent,
    secondary = InkNavy,
    background = Color(0xFF121212),
    surface = DarkParchment,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val SepiaColors = lightColorScheme(
    primary = SepiaText,
    secondary = GoldAccent,
    background = SepiaBg,
    surface = Color(0xFFEFE6D5),
    onPrimary = Color.White,
    onBackground = SepiaText,
    onSurface = SepiaText
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "LIGHT",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "DARK" -> DarkColors
        "SEPIA" -> SepiaColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
