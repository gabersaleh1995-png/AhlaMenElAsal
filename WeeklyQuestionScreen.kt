package com.gaber.ahlamenelasal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        db.collection("questions").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    questionText = doc.getString("text") ?: ""
                    questionId = doc.id
                    
                    // Check if user already answered
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        db.collection("questions").document(questionId).collection("answers")
                            .whereEqualTo("userId", currentUser.uid)
                            .get()
                            .addOnSuccessListener { answerSnapshot ->
                                if (!answerSnapshot.isEmpty) {
                                    val answerDoc = answerSnapshot.documents[0]
                                    answerText = answerDoc.getString("text") ?: ""
                                    existingAnswerId = answerDoc.id
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "سؤال الأسبوع", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = questionText,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSubmitted && !isEditing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "إجابتك السابقة:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = answerText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                if (existingAnswerId.isNotEmpty()) {
                                    db.collection("questions").document(questionId)
                                        .collection("answers").document(existingAnswerId)
                                        .delete()
                                        .addOnSuccessListener {
                                            answerText = ""
                                            existingAnswerId = ""
                                            isSubmitted = false
                                            Toast.makeText(context, "تم حذف الإجابة", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حذف")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { isEditing = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تعديل الإجابة")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "تم إرسال إجابتك بنجاح!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        } else {
            OutlinedTextField(
                value = answerText,
                onValueChange = { answerText = it },
                label = { Text("اكتب إجابتك هنا...") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (answerText.isNotBlank()) {
                        val currentUser = auth.currentUser
                        val answerData = hashMapOf(
                            "userId" to (currentUser?.uid ?: ""),
                            "userName" to (currentUser?.displayName ?: "عضو"),
                            "text" to answerText,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        
                        if (existingAnswerId.isNotEmpty()) {
                            // Update existing answer
                            db.collection("questions").document(questionId)
                                .collection("answers").document(existingAnswerId)
                                .set(answerData)
                        } else {
                            // Add new answer
                            db.collection("questions").document(questionId)
                                .collection("answers").add(answerData)
                                .addOnSuccessListener { docRef ->
                                    existingAnswerId = docRef.id
                                }
                        }
                        isSubmitted = true
                        isEditing = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "حفظ التعديلات" else "إرسال الإجابة")
            }
            
            if (isEditing) {
                TextButton(onClick = { isEditing = false }) {
                    Text("إلغاء")
                }
            }
        }
    }
}
