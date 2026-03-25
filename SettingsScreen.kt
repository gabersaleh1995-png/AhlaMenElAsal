package com.gaber.ahlamenelasal.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Launcher لحفظ الملف (النسخ الاحتياطي)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportAppData(context, it, settingsViewModel) }
    }

    // Launcher لاختيار ملف (استعادة النسخة)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { importAppData(context, it, settingsViewModel) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "الإعدادات",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsSection(title = "المظهر") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("استخدام سمة النظام")
                Switch(
                    checked = useSystemTheme,
                    onCheckedChange = { settingsViewModel.setUseSystemTheme(it) }
                )
            }

            if (!useSystemTheme) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الوضع الليلي")
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.toggleDarkMode(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("لون التطبيق الأساسي")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                settingsViewModel.availableColors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { settingsViewModel.updateColor(index) }
                            .padding(4.dp)
                    ) {
                        if (primaryColorIndex == index) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        SettingsSection(title = "الخط") {
            Text("حجم الخط: ${(fontSizeMultiplier * 100).toInt()}%")
            Slider(
                value = fontSizeMultiplier,
                onValueChange = { settingsViewModel.updateFontSize(it) },
                valueRange = 0.8f..1.5f,
                steps = 6
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("نوع الخط")
            AppFont.values().forEach { font ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { settingsViewModel.updateFont(font) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedFont == font),
                        onClick = { settingsViewModel.updateFont(font) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = font.displayName)
                }
            }
        }

        SettingsSection(title = "البيانات والنسخ الاحتياطي") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { createDocumentLauncher.launch("ahlamenelasal_backup.json") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نسخ احتياطي", fontSize = 12.sp)
                }

                Button(
                    onClick = { openDocumentLauncher.launch("application/json") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استعادة", fontSize = 12.sp)
                }
            }
            Text(
                text = "يمكنك حفظ إعداداتك الحالية في ملف واستعادتها في أي وقت.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("تسجيل الخروج")
        }
    }
}

private fun exportAppData(context: Context, uri: Uri, viewModel: SettingsViewModel) {
    try {
        val backupData = JSONObject().apply {
            put("fontSizeMultiplier", viewModel.fontSizeMultiplier.value)
            put("isDarkMode", viewModel.isDarkMode.value)
            put("useSystemTheme", viewModel.useSystemTheme.value)
            put("primaryColorIndex", viewModel.primaryColorIndex.value)
            put("selectedFont", viewModel.selectedFont.value.name)
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(backupData.toString(4).toByteArray())
        }
        Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "فشل حفظ النسخة الاحتياطية: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun importAppData(context: Context, uri: Uri, viewModel: SettingsViewModel) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val content = inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(content)
            
            viewModel.updateFontSize(json.getDouble("fontSizeMultiplier").toFloat())
            viewModel.toggleDarkMode(json.getBoolean("isDarkMode"))
            viewModel.setUseSystemTheme(json.getBoolean("useSystemTheme"))
            viewModel.updateColor(json.getInt("primaryColorIndex"))
            
            val fontName = json.getString("selectedFont")
            val font = AppFont.valueOf(fontName)
            viewModel.updateFont(font)

            Toast.makeText(context, "تمت استعادة الإعدادات بنجاح", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "فشل استعادة البيانات: ملف غير صالح أو تالف", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    }
}
