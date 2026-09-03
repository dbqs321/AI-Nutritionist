package com.swimnutrition.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0066CC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D3F),
    secondary = Color(0xFF4A90E2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E4FF),
    onSecondaryContainer = Color(0xFF001D3F),
    tertiary = Color(0xFF00A8CC),
    onTertiary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4A90E2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004A99),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF6BB3FF),
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF004A99),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color(0xFF003742),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFCF6679),
    onError = Color(0xFF1A000A)
)

@Composable
fun SwimNutritionAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}