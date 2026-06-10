package com.gaber.ahlamenelasal.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaber.ahlamenelasal.ui.theme.*
import com.gaber.ahlamenelasal.ui.viewmodel.AppFont
import com.gaber.ahlamenelasal.ui.viewmodel.AuthViewModel
import com.gaber.ahlamenelasal.ui.viewmodel.SettingsViewModel
import org.json.JSONObject

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val fontSizeMultiplier by settingsViewModel.fontSizeMultiplier
    val isDarkMode by settingsViewModel.isDarkMode
    val useSystemTheme by settingsViewModel.useSystemTheme
    val primaryColorIndex by settingsViewModel.primaryColorIndex
    val selectedFont by settingsViewModel.selectedFont
    val context = LocalContext.current

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportAppData(context, it, settingsViewModel) }
    }
    val loadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importAppData(context, it, settingsViewModel) }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("الإعدادات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("تخصيص التطبيق حسب تفضيلاتك", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        // ─── Appearance ───
        SettingsSection("المظهر", Icons.Default.Palette) {
            SettingsToggleRow("استخدام سمة النظام", useSystemTheme) { settingsViewModel.setUseSystemTheme(it) }
            if (!useSystemTheme) {
                Spacer(Modifier.height(4.dp))
                SettingsToggleRow("الوضع الليلي", isDarkMode) { settingsViewModel.toggleDarkMode(it) }
            }
            Spacer(Modifier.height(16.dp))
            Text("لون التطبيق", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                settingsViewModel.availableColors.forEachIndexed { i, color ->
                    Box(
                        modifier = Modifier
                            .size(44.dp).clip(CircleShape).background(color)
                            .border(if (primaryColorIndex == i) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else BorderStroke(0.dp, Color.Transparent), CircleShape)
                            .clickable { settingsViewModel.updateColor(i) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ─── Typography ───
        SettingsSection("الخط والقراءة", Icons.Default.TextFormat) {
            Text("حجم الخط: ${(fontSizeMultiplier * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Slider(
                value = fontSizeMultiplier,
                onValueChange = { settingsViewModel.updateFontSize(it) },
                valueRange = 0.8f..1.5f, steps = 6,
                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.height(12.dp))
            Text("نوع الخط", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            AppFont.values().forEach { font ->
                Surface(
                    onClick = { settingsViewModel.updateFont(font) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFont == font) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedFont == font, onClick = { settingsViewModel.updateFont(font) })
                        Text(font.displayName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ─── Backup ───
        SettingsSection("النسخ الاحتياطي", Icons.Default.Backup) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { saveLauncher.launch("ahlamenelasal_backup.json") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MidPurple)
                ) {
                    Icon(Icons.Default.Backup, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text("حفظ", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { loadLauncher.launch("application/json") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyAmber)
                ) {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text("استعادة", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { authViewModel.logout(); onLogout() },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(10.dp))
            Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MidPurple))
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Icon(icon, null, tint = MidPurple, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            content()
        }
    }
}

private fun exportAppData(context: Context, uri: Uri, vm: SettingsViewModel) {
    try {
        val json = JSONObject().apply {
            put("fontSizeMultiplier", vm.fontSizeMultiplier.value)
            put("isDarkMode", vm.isDarkMode.value)
            put("useSystemTheme", vm.useSystemTheme.value)
            put("primaryColorIndex", vm.primaryColorIndex.value)
            put("selectedFont", vm.selectedFont.value.name)
        }
        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toString(4).toByteArray()) }
        Toast.makeText(context, "تم الحفظ بنجاح ✅", Toast.LENGTH_LONG).show()
    } catch (_: Exception) { Toast.makeText(context, "فشل الحفظ", Toast.LENGTH_LONG).show() }
}

private fun importAppData(context: Context, uri: Uri, vm: SettingsViewModel) {
    try {
        context.contentResolver.openInputStream(uri)?.use {
            val json = JSONObject(it.bufferedReader().readText())
            vm.updateFontSize(json.getDouble("fontSizeMultiplier").toFloat())
            vm.toggleDarkMode(json.getBoolean("isDarkMode"))
            vm.setUseSystemTheme(json.getBoolean("useSystemTheme"))
            vm.updateColor(json.getInt("primaryColorIndex"))
            vm.updateFont(AppFont.valueOf(json.getString("selectedFont")))
            Toast.makeText(context, "تمت الاستعادة بنجاح ✅", Toast.LENGTH_LONG).show()
        }
    } catch (_: Exception) { Toast.makeText(context, "فشل الاستعادة", Toast.LENGTH_LONG).show() }
}
