package com.pay.eeaapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val EeaAmber     = Color(0xFFF4A261)   // accent / warning
val EeaRed       = Color(0xFFE63946)   // rejection / error
val EeaBlue      = Color(0xFF457B9D)   // info / under-review
val EeaSlate     = Color(0xFF3D405B)   // secondary text
val EeaSlate200  = Color(0xFFB0B3C6)

val StatusColors = mapOf(
    "DRAFT" to Color(0xFF90A4AE),
    "SUBMITTED" to Color(0xFF42A5F5),
    "UNDER_REVIEW" to Color(0xFFFFA726),
    "AMENDMENTS_REQUIRED" to Color(0xFFEF5350),
    "APPROVED" to Color(0xFF66BB6A),
    "REJECTED" to Color(0xFFB71C1C)
)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EEAAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

