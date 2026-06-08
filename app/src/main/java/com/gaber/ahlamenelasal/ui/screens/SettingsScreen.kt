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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportAppData(context, it, settingsViewModel) }
    }

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
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
        )

        SettingsSection(title = "المظهر", icon = Icons.Default.Palette) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("استخدام سمة النظام", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = useSystemTheme,
                    onCheckedChange = { settingsViewModel.setUseSystemTheme(it) }
                )
            }

            if (!useSystemTheme) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الوضع الليلي", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.toggleDarkMode(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("لون التطبيق الأساسي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                settingsViewModel.availableColors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                if (primaryColorIndex == index) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                                else BorderStroke(0.dp, Color.Transparent),
                                CircleShape
                            )
                            .clickable { settingsViewModel.updateColor(index) }
                    )
                }
            }
        }

        SettingsSection(title = "الخط والقراءة", icon = Icons.Default.TextFormat) {
            Text(
                text = "حجم الخط: ${(fontSizeMultiplier * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = fontSizeMultiplier,
                onValueChange = { settingsViewModel.updateFontSize(it) },
                valueRange = 0.8f..1.5f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("نوع الخط", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            AppFont.values().forEach { font ->
                Surface(
                    onClick = { settingsViewModel.updateFont(font) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFont == font) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedFont == font),
                            onClick = { settingsViewModel.updateFont(font) }
                        )
                        Text(
                            text = font.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        SettingsSection(title = "البيانات والنسخ الاحتياطي", icon = Icons.Default.Backup) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { createDocumentLauncher.launch("ahlamenelasal_backup.json") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("نسخ احتياطي", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { openDocumentLauncher.launch("application/json") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استعادة", fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "احفظ إعداداتك المفضلة لاستعادتها لاحقاً.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("تسجيل الخروج", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
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
        Toast.makeText(context, "فشل حفظ النسخة الاحتياطية", Toast.LENGTH_LONG).show()
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
            viewModel.updateFont(AppFont.valueOf(fontName))
            Toast.makeText(context, "تمت استعادة الإعدادات بنجاح", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "فشل استعادة البيانات", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
