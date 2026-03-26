package com.gaber.ahlamenelasal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.gaber.ahlamenelasal.R
import com.gaber.ahlamenelasal.ui.viewmodel.AppFont

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun getFontFamily(appFont: AppFont): FontFamily {
    return when (appFont) {
        AppFont.Cairo -> FontFamily(Font(googleFont = GoogleFont("Cairo"), fontProvider = provider))
        AppFont.Amiri -> FontFamily(Font(googleFont = GoogleFont("Amiri"), fontProvider = provider))
        AppFont.Lateef -> FontFamily(Font(googleFont = GoogleFont("Lateef"), fontProvider = provider))
        else -> FontFamily.Default
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun AhlaMenElAsalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    customPrimaryColor: Color? = null,
    fontSizeMultiplier: Float = 1.0f,
    appFont: AppFont = AppFont.Default,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && customPrimaryColor == null -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            if (customPrimaryColor != null) DarkColorScheme.copy(primary = customPrimaryColor)
            else DarkColorScheme
        }
        else -> {
            if (customPrimaryColor != null) LightColorScheme.copy(primary = customPrimaryColor)
            else LightColorScheme
        }
    }

    val fontFamily = getFontFamily(appFont)
    val baseTypography = Typography
    val scaledTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * fontSizeMultiplier, fontFamily = fontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * fontSizeMultiplier, fontFamily = fontFamily)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
