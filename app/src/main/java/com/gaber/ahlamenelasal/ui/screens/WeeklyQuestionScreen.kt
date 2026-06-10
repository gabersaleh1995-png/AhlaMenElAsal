package com.gaber.ahlamenelasal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaber.ahlamenelasal.ui.theme.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun WeeklyQuestionScreen() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var questionText by remember { mutableStateOf("جاري التحميل...") }
    var questionId by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var existingAnswerId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("questions").orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { snap, _ ->
                if (snap != null && !snap.isEmpty) {
                    val doc = snap.documents[0]
                    questionText = doc.getString("text") ?: ""
                    questionId = doc.id
                    auth.currentUser?.let { user ->
                        db.collection("questions").document(questionId)
                            .collection("answers").whereEqualTo("userId", user.uid).get()
                            .addOnSuccessListener { ansSnap ->
                                if (!ansSnap.isEmpty) {
                                    val ansDoc = ansSnap.documents[0]
                                    answerText = ansDoc.getString("text") ?: ""
                                    existingAnswerId = ansDoc.id
                                    isSubmitted = true
                                }
                            }
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Hero header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(DeepPurple, Color(0xFF2D1B4E)))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Text("❓", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "سؤال الأسبوع",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HoneyGold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isSubmitted && !isEditing) {
            // Submitted state
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("إجابتك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(answerText, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = {
                                if (existingAnswerId.isNotEmpty()) {
                                    db.collection("questions").document(questionId)
                                        .collection("answers").document(existingAnswerId).delete()
                                        .addOnSuccessListener {
                                            answerText = ""; existingAnswerId = ""; isSubmitted = false
                                            Toast.makeText(context, "تم حذف الإجابة", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp)); Text("حذف")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { isEditing = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MidPurple)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp)); Text("تعديل")
                        }
                    }
                }
            }
        } else {
            // Answer input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (isEditing) "تعديل إجابتك" else "أرسل إجابتك",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        placeholder = { Text("اكتب إجابتك هنا...") },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MidPurple,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (answerText.isNotBlank()) {
                                val user = auth.currentUser
                                val data = hashMapOf(
                                    "userId" to (user?.uid ?: ""),
                                    "userName" to (user?.displayName ?: "عضو"),
                                    "text" to answerText,
                                    "timestamp" to Timestamp.now()
                                )
                                if (existingAnswerId.isNotEmpty()) {
                                    db.collection("questions").document(questionId)
                                        .collection("answers").document(existingAnswerId).set(data)
                                } else {
                                    db.collection("questions").document(questionId)
                                        .collection("answers").add(data)
                                        .addOnSuccessListener { existingAnswerId = it.id }
                                }
                                isSubmitted = true; isEditing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MidPurple)
                    ) {
                        Text(if (isEditing) "حفظ التعديلات" else "إرسال الإجابة", fontWeight = FontWeight.Bold)
                    }
                    if (isEditing) {
                        TextButton(onClick = { isEditing = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
