package com.gaber.ahlamenelasal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMembersScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var userToDelete by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            users = snapshot?.documents?.map { Triple(it.id, it.getString("name") ?: "بدون اسم", it.getString("email") ?: "") } ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الأعضاء") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (userToDelete != null) {
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    title = { Text("حذف مستخدم") },
                    text = { Text("هل أنت متأكد من حذف المستخدم '${userToDelete?.second}'؟ هذا الإجراء لا يمكن التراجع عنه.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                userToDelete?.let { user ->
                                    db.collection("users").document(user.first).delete()
                                        .addOnSuccessListener { Toast.makeText(context, "تم حذف المستخدم", Toast.LENGTH_SHORT).show() }
                                }
                                userToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("حذف نهائي") }
                    },
                    dismissButton = { TextButton(onClick = { userToDelete = null }) { Text("إلغاء") } }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(users) { (id, name, email) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { userToDelete = Triple(id, name, email) }) {
                                Icon(Icons.Default.PersonRemove, null, tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}
