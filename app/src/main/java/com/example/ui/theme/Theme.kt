package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeepObsidian = Color(0xFF070A0F)
private val ElevatedGlass = Color(0xFF0F141E)
private val ElevatedGlassVariant = Color(0xFF17202F)
private val NeonCyan = Color(0xFF00E5FF)
private val NeonAmber = Color(0xFFF59E0B)
private val ElectricBorder = Color(0xFF223046)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003844),
    onPrimaryContainer = NeonCyan,
    secondary = NeonAmber,
    onSecondary = Color.Black,
    background = DeepObsidian,
    onBackground = TextPrimary,
    surface = ElevatedGlass,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedGlassVariant,
    onSurfaceVariant = TextSecondary,
    outline = ElectricBorder,
    outlineVariant = Color(0xFF1B2433)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
