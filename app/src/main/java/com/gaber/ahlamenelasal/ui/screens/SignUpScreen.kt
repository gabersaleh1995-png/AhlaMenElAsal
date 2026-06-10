package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.ui.theme.*
import com.gaber.ahlamenelasal.ui.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var adminCode by remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.errorMessage

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(DeepPurple, Color(0xFF2D1B4E), Color(0xFF1A1040))))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                .padding(horizontal = 28.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(HoneyGold, HoneyAmber))),
                contentAlignment = Alignment.Center
            ) { Text("🍯", fontSize = 36.sp) }

            Spacer(Modifier.height(14.dp))
            Text("إنشاء حساب جديد", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text("انضم إلى مجتمع أحلى من العسل", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.5f), modifier = Modifier.padding(top = 6.dp, bottom = 28.dp))

            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    listOf(
                        Triple("الاسم بالكامل", name, Icons.Default.Person) to { v: String -> name = v },
                        Triple("البريد الإلكتروني", email, Icons.Default.Email) to { v: String -> email = v },
                    ).forEach { (meta, setter) ->
                        val (label, value, icon) = meta
                        OutlinedTextField(
                            value = value, onValueChange = setter,
                            label = { Text(label) },
                            leadingIcon = { Icon(icon, null, tint = MidPurple) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MidPurple, unfocusedBorderColor = BorderLight)
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MidPurple) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MidPurple, unfocusedBorderColor = BorderLight)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminCode, onValueChange = { adminCode = it },
                        label = { Text("كود الأدمن (اختياري)") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = HoneyAmber) },
                        supportingText = { Text("اتركه فارغاً إذا كنت مستخدماً عادياً", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoneyAmber, unfocusedBorderColor = BorderLight)
                    )

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(Modifier.height(20.dp))
                    if (isLoading) {
                        CircularProgressIndicator(color = MidPurple, modifier = Modifier.padding(8.dp))
                    } else {
                        Button(
                            onClick = { authViewModel.signUp(name, email, password, adminCode, onSignUpSuccess) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MidPurple)
                        ) { Text("إنشاء الحساب", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }

                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text("لديك حساب؟ ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("سجل دخولك", modifier = Modifier.clickable { onNavigateToLogin() }, color = MidPurple, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
