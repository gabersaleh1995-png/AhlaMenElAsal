package com.gaber.ahlamenelasal.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

enum class AppFont(val displayName: String) {
    Default("الافتراضي"),
    Cairo("Cairo (عصري)"),
    Amiri("Amiri (كلاسيكي)"),
    Lateef("Lateef (ناعم)")
}

class SettingsViewModel : ViewModel() {
    var fontSizeMultiplier = mutableStateOf(1.0f)
    var isDarkMode = mutableStateOf(false)
    var useSystemTheme = mutableStateOf(true)
    var primaryColorIndex = mutableStateOf(0)
    var selectedFont = mutableStateOf(AppFont.Default)
    
    val availableColors = listOf(
        androidx.compose.ui.graphics.Color(0xFF6650a4), // Purple
        androidx.compose.ui.graphics.Color(0xFF1B5E20), // Green
        androidx.compose.ui.graphics.Color(0xFFB71C1C), // Red
        androidx.compose.ui.graphics.Color(0xFF0D47A1)  // Blue
    )

    fun updateFontSize(newMultiplier: Float) {
        fontSizeMultiplier.value = newMultiplier
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        useSystemTheme.value = false
    }
    
    fun setUseSystemTheme(enabled: Boolean) {
        useSystemTheme.value = enabled
    }

    fun updateColor(index: Int) {
        primaryColorIndex.value = index
    }

    fun updateFont(font: AppFont) {
        selectedFont.value = font
    }
}
