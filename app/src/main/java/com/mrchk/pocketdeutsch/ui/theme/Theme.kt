package com.mrchk.pocketdeutsch.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext

data class PocketColors(
    val ink: Color,
    val paper: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val gray50: Color,
    val gray200: Color,
    val gray400: Color,
    val gray500: Color,
    val isLight: Boolean
)

val LocalPocketColors = staticCompositionLocalOf {
    PocketColors(
        ink = Color.Unspecified,
        paper = Color.Unspecified,
        surface = Color.Unspecified,
        primary = Color.Unspecified,
        secondary = Color.Unspecified,
        tertiary = Color.Unspecified,
        success = Color.Unspecified,
        warning = Color.Unspecified,
        error = Color.Unspecified,
        gray50 = Color.Unspecified,
        gray200 = Color.Unspecified,
        gray400 = Color.Unspecified,
        gray500 = Color.Unspecified,
        isLight = true
    )
}

object PocketTheme {
    val colors: PocketColors
        @Composable
        get() = LocalPocketColors.current

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}

@Composable
fun PocketDeutschTheme(
    content: @Composable () -> Unit
) {

    val pocketColors = PocketColors(
        ink = Ink,
        paper = Paper,
        surface = Surface,
        primary = Primary,
        secondary = Secondary,
        tertiary = BlueSoft,
        success = Success,
        warning = Warning,
        error = Error,
        gray50 = Gray50,
        gray200 = Gray200,
        gray400 = Gray400,
        gray500 = Gray500,
        isLight = true
    )

    CompositionLocalProvider(LocalPocketColors provides pocketColors) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Primary,
                secondary = Secondary,
                tertiary = BlueSoft,
                background = Paper,
                surface = Surface,
                error = Error,
                onPrimary = Ink,
                onSecondary = Ink,
                onTertiary = Ink,
                onBackground = Ink,
                onSurface = Ink,
                onError = White
            ),
            typography = Typography,
            content = content
        )
    }
}