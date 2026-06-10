package com.gaber.ahlamenelasal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.ui.theme.*
import com.gaber.ahlamenelasal.ui.viewmodel.ChatViewModel

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

    if (showPasscodeDialog) {
        AlertDialog(
            onDismissRequest = { showPasscodeDialog = false; inputPasscode = "" },
            title = { Text("🔑 الغرفة مقفولة", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل الكود الذي أرسله الأدمن للدخول")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputPasscode,
                        onValueChange = {
                            inputPasscode = it
                            if (it.trim() == correctPasscode.trim() && it.isNotEmpty()) {
                                showPasscodeDialog = false
                                inputPasscode = ""
                                onNavigateToGroup()
                            }
                        },
                        label = { Text("كود الدخول") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (inputPasscode == correctPasscode) {
                        showPasscodeDialog = false; inputPasscode = ""; onNavigateToGroup()
                    } else {
                        Toast.makeText(context, "الكود غير صحيح", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("دخول") }
            },
            dismissButton = {
                TextButton(onClick = { showPasscodeDialog = false; inputPasscode = "" }) { Text("إلغاء") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)))),
            contentAlignment = Alignment.Center
        ) {
            Text("💬", fontSize = 36.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("الدردشة", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("اختر غرفة المحادثة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))

        ChatOptionCard(
            title = "الدردشة الجماعية",
            subtitle = "تواصل مع كل أعضاء الاجتماع",
            emoji = "👥",
            gradient = listOf(MidPurple, Color(0xFF6D28D9)),
            onClick = {
                chatViewModel.listenToChatStatus("group_all") { status, passcode ->
                    if (status == "open") onNavigateToGroup()
                    else { correctPasscode = passcode; showPasscodeDialog = true }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ChatOptionCard(
            title = "دردشة مع الأدمن",
            subtitle = "تواصل خاص مع خادم الاجتماع",
            emoji = "🛡️",
            gradient = listOf(HoneyAmber, HoneyGold),
            onClick = onNavigateToAdmin
        )
    }
}

@Composable
fun ChatOptionCard(
    title: String,
    subtitle: String,
    emoji: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
