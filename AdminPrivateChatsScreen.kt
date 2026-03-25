package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.ui.viewmodel.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore

data class PrivateChatInfo(
    val userId: String, 
    val name: String = "", 
    val lastMessage: String = "",
    val lastUpdate: com.google.firebase.Timestamp? = null
)

@Composable
fun AdminPrivateChatsScreen(
    onChatClick: (String, String) -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val db = FirebaseFirestore.getInstance()
    var chatRooms by remember { mutableStateOf<List<PrivateChatInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("chats")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val rooms = snapshot.documents
                        .filter { it.id.startsWith("admin_") }
                        .map { doc ->
                            PrivateChatInfo(
                                userId = doc.id.removePrefix("admin_"),
                                name = doc.getString("senderName") ?: "مستخدم غير معروف",
                                lastMessage = doc.getString("lastMessage") ?: "لا توجد رسائل",
                                lastUpdate = doc.getTimestamp("lastUpdate")
                            )
                        }
                        .sortedByDescending { it.lastUpdate }
                    
                    chatRooms = rooms
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "الرسائل الخاصة الواردة",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (chatRooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد رسائل خاصة حالياً")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chatRooms) { room ->
                    ChatRoomItem(
                        room = room, 
                        onChatClick = { onChatClick(room.userId, room.name) },
                        onDelete = { chatViewModel.deleteChat("admin_${room.userId}") }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(room: PrivateChatInfo, onChatClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person, 
                contentDescription = null, 
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = room.lastMessage, 
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "حذف المحادثة", tint = Color.Red)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف المحادثة") },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذه المحادثة بالكامل من قائمتك؟") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("حذف", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
