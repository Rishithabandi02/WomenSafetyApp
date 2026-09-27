package com.example.safeher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.safeher.ui.theme.Shapes

private val LightColors = lightColorScheme(

    primary = LavenderPrimary,
    secondary = SoftPink,
    tertiary = LavenderAccent,

    background = BackgroundLight,
    surface = SurfaceLight,

    onPrimary = TextPrimaryDark,
    onSecondary = TextPrimaryLight,

    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,

    error = SOSRed
)

private val DarkColors = darkColorScheme(

    primary = NeonLavender,
    secondary = SoftPink,
    tertiary = NeonAccent,

    background = PurpleDarkBackground,
    surface = PurpleDarkSurface,

    onPrimary = TextPrimaryLight,
    onSecondary = TextPrimaryDark,

    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,

    error = SOSRed
)



@Composable
fun SafeHerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme = when {

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        shapes = Shapes
    )
}