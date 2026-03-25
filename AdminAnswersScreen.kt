package com.gaber.ahlamenelasal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

data class UserAnswer(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val isCorrect: Boolean = false
)

@Composable
fun AdminAnswersScreen(questionId: String, onContactUser: (String, String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var answers by remember { mutableStateOf<List<UserAnswer>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(questionId) {
        db.collection("questions").document(questionId).collection("answers")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    answers = snapshot.documents.map { doc ->
                        UserAnswer(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "عضو مجهول",
                            text = doc.getString("text") ?: "",
                            isCorrect = doc.getBoolean("isCorrect") ?: false
                        )
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "إجابات الأعضاء",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (answers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد إجابات حتى الآن")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(answers) { answer ->
                    AnswerCard(
                        answer = answer,
                        onDelete = {
                            db.collection("questions").document(questionId)
                                .collection("answers").document(answer.id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "تم حذف الإجابة", Toast.LENGTH_SHORT).show()
                                }
                        },
                        onToggleCorrect = {
                            db.collection("questions").document(questionId)
                                .collection("answers").document(answer.id)
                                .update("isCorrect", !answer.isCorrect)
                        },
                        onContact = { onContactUser(answer.userId, answer.userName) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerCard(
    answer: UserAnswer,
    onDelete: () -> Unit,
    onToggleCorrect: () -> Unit,
    onContact: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (answer.isCorrect) {
            CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = answer.userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (answer.isCorrect) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "إجابة صحيحة", tint = Color(0xFF4CAF50))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = answer.text, style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onToggleCorrect) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "تصحيح",
                        tint = if (answer.isCorrect) Color(0xFF4CAF50) else Color.Gray
                    )
                }
                IconButton(onClick = onContact) {
                    Icon(Icons.Default.Chat, contentDescription = "تواصل", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
