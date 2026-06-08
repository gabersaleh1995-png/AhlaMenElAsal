package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.gaber.ahlamenelasal.ui.viewmodel.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast

@Composable
fun ChatSelectionScreen(
    onNavigateToGroup: () -> Unit, 
    onNavigateToAdmin: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var inputPasscode by remember { mutableStateOf("") }
    var correctPasscode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "اختر غرفة الدردشة",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ChatOptionCard(
            title = "الدردشة الجماعية",
            subtitle = "تواصل مع كل أعضاء الاجتماع",
            icon = Icons.Default.Group,
            onClick = {
                chatViewModel.listenToChatStatus("group_all") { status, passcode ->
                    if (status == "open") {
                        onNavigateToGroup()
                    } else {
                        correctPasscode = passcode
                        showPasscodeDialog = true
                    }
                }
            }
        )

        if (showPasscodeDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPasscodeDialog = false
                    inputPasscode = "" 
                },
                title = { Text("الغرفة مقفولة بكود 🔑") },
                text = {
                    Column {
                        Text("برجاء إدخال الكود الذي أرسله الأدمن للدخول")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputPasscode,
                            onValueChange = { 
                                inputPasscode = it
                                // التحقق التلقائي بمجرد الكتابة
                                if (it.trim() == correctPasscode.trim() && it.isNotEmpty()) {
                                    showPasscodeDialog = false
                                    inputPasscode = "" // مسح الكود فوراً
                                    onNavigateToGroup()
                                }
                            },
                            label = { Text("كود الدخول") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (inputPasscode == correctPasscode) {
                            showPasscodeDialog = false
                            inputPasscode = ""
                            onNavigateToGroup()
                        } else {
                            Toast.makeText(context, "الكود غير صحيح", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("دخول") }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showPasscodeDialog = false
                        inputPasscode = ""
                    }) { Text("إلغاء") }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChatOptionCard(
            title = "دردشة مع الأدمن",
            subtitle = "تواصل خاص مع خادم الاجتماع",
            icon = Icons.Default.SupportAgent,
            onClick = onNavigateToAdmin
        )
    }
}

@Composable
fun ChatOptionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
