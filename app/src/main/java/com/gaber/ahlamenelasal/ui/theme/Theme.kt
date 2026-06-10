package com.gaber.ahlamenelasal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.core.view.WindowCompat
import com.gaber.ahlamenelasal.R
import com.gaber.ahlamenelasal.ui.viewmodel.AppFont

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

fun getFontFamily(appFont: AppFont): FontFamily {
    return when (appFont) {
        AppFont.Cairo  -> FontFamily(Font(googleFont = GoogleFont("Cairo"),  fontProvider = provider))
        AppFont.Amiri  -> FontFamily(Font(googleFont = GoogleFont("Amiri"),  fontProvider = provider))
        AppFont.Lateef -> FontFamily(Font(googleFont = GoogleFont("Lateef"), fontProvider = provider))
        else           -> FontFamily.Default
    }
}

// ─── Dark Color Scheme ────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = HoneyGold,
    onPrimary          = DeepPurple,
    primaryContainer   = Color(0xFF2D1B4E),
    onPrimaryContainer = HoneyLight,
    secondary          = HoneyAmber,
    onSecondary        = DeepPurple,
    secondaryContainer = DarkCard,
    onSecondaryContainer = Color(0xFFE0D5FF),
    tertiary           = FuchsiaAccent,
    onTertiary         = Color.White,
    background         = DarkBg,
    onBackground       = Color(0xFFF5F0FF),
    surface            = DarkSurface,
    onSurface          = Color(0xFFF5F0FF),
    surfaceVariant     = DarkCard,
    onSurfaceVariant   = Color(0xFFCDC4E8),
    outline            = BorderDark,
    error              = ErrorRed,
    onError            = Color.White
)

// ─── Light Color Scheme ───────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = MidPurple,
    onPrimary          = Color.White,
    primaryContainer   = LightCard,
    onPrimaryContainer = DeepPurple,
    secondary          = HoneyAmber,
    onSecondary        = Color.White,
    secondaryContainer = HoneyLight,
    onSecondaryContainer = Color(0xFF92400E),
    tertiary           = FuchsiaAccent,
    onTertiary         = Color.White,
    background         = LavenderBg,
    onBackground       = TextPrimary,
    surface            = LightSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = LightCard,
    onSurfaceVariant   = TextSecondary,
    outline            = BorderLight,
    error              = ErrorRed,
    onError            = Color.White
)

@Composable
fun AhlaMenElAsalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // أوقفنا dynamic color للحفاظ على الهوية البصرية
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
            if (customPrimaryColor != null)
                DarkColorScheme.copy(primary = customPrimaryColor, onPrimary = Color.White)
            else DarkColorScheme
        }
        else -> {
            if (customPrimaryColor != null)
                LightColorScheme.copy(primary = customPrimaryColor, onPrimary = Color.White)
            else LightColorScheme
        }
    }

    // Status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val fontFamily = getFontFamily(appFont)
    val base = Typography
    val scaledTypography = Typography(
        displayLarge    = base.displayLarge.copy(fontSize    = base.displayLarge.fontSize    * fontSizeMultiplier, fontFamily = fontFamily),
        displayMedium   = base.displayMedium.copy(fontSize   = base.displayMedium.fontSize   * fontSizeMultiplier, fontFamily = fontFamily),
        displaySmall    = base.displaySmall.copy(fontSize    = base.displaySmall.fontSize    * fontSizeMultiplier, fontFamily = fontFamily),
        headlineLarge   = base.headlineLarge.copy(fontSize   = base.headlineLarge.fontSize   * fontSizeMultiplier, fontFamily = fontFamily),
        headlineMedium  = base.headlineMedium.copy(fontSize  = base.headlineMedium.fontSize  * fontSizeMultiplier, fontFamily = fontFamily),
        headlineSmall   = base.headlineSmall.copy(fontSize   = base.headlineSmall.fontSize   * fontSizeMultiplier, fontFamily = fontFamily),
        titleLarge      = base.titleLarge.copy(fontSize      = base.titleLarge.fontSize      * fontSizeMultiplier, fontFamily = fontFamily),
        titleMedium     = base.titleMedium.copy(fontSize     = base.titleMedium.fontSize     * fontSizeMultiplier, fontFamily = fontFamily),
        titleSmall      = base.titleSmall.copy(fontSize      = base.titleSmall.fontSize      * fontSizeMultiplier, fontFamily = fontFamily),
        bodyLarge       = base.bodyLarge.copy(fontSize       = base.bodyLarge.fontSize       * fontSizeMultiplier, fontFamily = fontFamily),
        bodyMedium      = base.bodyMedium.copy(fontSize      = base.bodyMedium.fontSize      * fontSizeMultiplier, fontFamily = fontFamily),
        bodySmall       = base.bodySmall.copy(fontSize       = base.bodySmall.fontSize       * fontSizeMultiplier, fontFamily = fontFamily),
        labelLarge      = base.labelLarge.copy(fontSize      = base.labelLarge.fontSize      * fontSizeMultiplier, fontFamily = fontFamily),
        labelMedium     = base.labelMedium.copy(fontSize     = base.labelMedium.fontSize     * fontSizeMultiplier, fontFamily = fontFamily),
        labelSmall      = base.labelSmall.copy(fontSize      = base.labelSmall.fontSize      * fontSizeMultiplier, fontFamily = fontFamily)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = scaledTypography,
        content     = content
    )
}
