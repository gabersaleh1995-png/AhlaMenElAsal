package com.gaber.ahlamenelasal.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaber.ahlamenelasal.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

@Composable
fun AdminScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "لوحة تحكم الأدمن",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AdminSection(
            title = "آية اليوم",
            icon = Icons.Default.FormatQuote,
            content = { DailyVerseForm() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AdminSection(
            title = "إدارة مواعيد الاجتماعات",
            icon = Icons.Default.Event,
            content = { MeetingManagerSection() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AdminSection(
            title = "نشر فيديوهات (YouTube / Facebook)",
            icon = Icons.Default.PlayCircle,
            content = { AddVideoForm() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AdminSection(
            title = "رفع الكتب والملفات (PDF / Drive)",
            icon = Icons.Default.PictureAsPdf,
            content = { UniversalFileUploader() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AdminSection(
            title = "رفع تسجيل صوتي (Drive / Storage)",
            icon = Icons.Default.Mic,
            content = { AudioUploader() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AdminSection(
            title = "إدارة الأعضاء",
            icon = Icons.Default.People,
            content = { ManageUsersSection() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AdminSection(
            title = "سؤال الأسبوع والإجابات",
            icon = Icons.Default.Help,
            content = { AddWeeklyQuestionForm(onNavigate) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onNavigate(Screen.AdminPrivateChats.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Chat, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("الرسائل الخاصة الواردة")
        }
    }
}

@Composable
fun MeetingManagerSection() {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الاجتماع") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("التاريخ (مثلاً: 15 أكتوبر)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("الوقت (مثلاً: 7:00 م)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("المكان") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("وصف قصير") }, modifier = Modifier.fillMaxWidth())
        
        Button(
            onClick = {
                if (title.isNotBlank() && date.isNotBlank()) {
                    val meeting = hashMapOf(
                        "title" to title,
                        "date" to date,
                        "time" to time,
                        "location" to location,
                        "description" to description,
                        "timestamp" to Timestamp.now()
                    )
                    db.collection("meetings").add(meeting).addOnSuccessListener {
                        sendPushNotification(context, "اجتماع جديد", "تمت إضافة اجتماع: $title")
                        Toast.makeText(context, "تمت إضافة الاجتماع", Toast.LENGTH_SHORT).show()
                        title = ""; date = ""; time = ""; location = ""; description = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إضافة الاجتماع")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("الاجتماعات المجدولة:", style = MaterialTheme.typography.titleSmall)
        DeleteManagerSection("meetings", "title")
    }
}

@Composable
fun UniversalFileUploader() {
    val categories = listOf("المكتبة العامة", "الكتاب المقدس PDF", "الأجبية PDF")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var title by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { pdfUri = it }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedCategory)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false })
                }
            }
        }

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("اسم الكتاب/الملف") }, modifier = Modifier.fillMaxWidth())
        
        OutlinedTextField(
            value = linkUrl, 
            onValueChange = { linkUrl = it }, 
            label = { Text("رابط من جوجل درايف (اختياري)") }, 
            modifier = Modifier.fillMaxWidth()
        )

        Text("أو اختر ملفاً من هاتفك:", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { launcher.launch("application/pdf") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (pdfUri == null) "اختر PDF من الهاتف" else "تم اختيار: ${pdfUri?.lastPathSegment}")
        }

        if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (title.isNotBlank() && (linkUrl.isNotBlank() || pdfUri != null)) {
                    isUploading = true
                    if (pdfUri != null) {
                        val ref = storage.reference.child("pdfs/${UUID.randomUUID()}.pdf")
                        ref.putFile(pdfUri!!).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                saveToFirestore(title, uri.toString(), selectedCategory, db, context) {
                                    isUploading = false; title = ""; linkUrl = ""; pdfUri = null
                                }
                            }
                        }
                    } else {
                        saveToFirestore(title, linkUrl, selectedCategory, db, context) {
                            isUploading = false; title = ""
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) { Text("نشر الآن") }
        
        DeleteManagerSection("bible_pdfs", "title")
    }
}

@Composable
fun AudioUploader() {
    var title by remember { mutableStateOf("") }
    var audioUrl by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { audioUri = it }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان التسجيل الصوتي") }, modifier = Modifier.fillMaxWidth())
        
        OutlinedTextField(
            value = audioUrl, 
            onValueChange = { audioUrl = it }, 
            label = { Text("رابط التسجيل من Google Drive (اختياري)") }, 
            modifier = Modifier.fillMaxWidth()
        )

        Text("أو اختر ملفاً من هاتفك:", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { launcher.launch("audio/*") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (audioUri == null) "اختر ملف صوتي من الهاتف" else "تم اختيار ملف")
        }

        if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (title.isNotBlank() && (audioUrl.isNotBlank() || audioUri != null)) {
                    isUploading = true
                    if (audioUri != null) {
                        val ref = storage.reference.child("audios/${UUID.randomUUID()}.mp3")
                        ref.putFile(audioUri!!).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                saveAudioToFirestore(title, uri.toString(), db, context) {
                                    isUploading = false; title = ""; audioUrl = ""; audioUri = null
                                }
                            }
                        }.addOnFailureListener {
                            isUploading = false
                            Toast.makeText(context, "فشل الرفع", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        saveAudioToFirestore(title, audioUrl, db, context) {
                            isUploading = false; title = ""; audioUrl = ""
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) { Text("نشر التسجيل") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("التسجيلات المرفوعة:", style = MaterialTheme.typography.titleSmall)
        DeleteManagerSection("audios", "title")
    }
}

fun saveAudioToFirestore(title: String, url: String, db: FirebaseFirestore, context: android.content.Context, onComplete: () -> Unit) {
    val data = hashMapOf(
        "title" to title,
        "url" to url,
        "timestamp" to Timestamp.now()
    )
    db.collection("audios").add(data).addOnSuccessListener {
        sendPushNotification(context, "تسجيل صوتي جديد", "تم رفع تسجيل: $title")
        Toast.makeText(context, "تم رفع التسجيل بنجاح", Toast.LENGTH_SHORT).show()
        onComplete()
    }
}

fun saveToFirestore(title: String, url: String, category: String, db: FirebaseFirestore, context: android.content.Context, onComplete: () -> Unit) {
    val data = hashMapOf(
        "title" to title,
        "url" to url,
        "category" to category,
        "timestamp" to Timestamp.now()
    )
    db.collection("bible_pdfs").add(data).addOnSuccessListener {
        sendPushNotification(context, "ملف PDF جديد", "تم رفع: $title في $category")
        Toast.makeText(context, "تم النشر بنجاح", Toast.LENGTH_SHORT).show()
        onComplete()
    }
}

@Composable
fun AddVideoForm() {
    var title by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الفيديو") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = videoUrl, 
            onValueChange = { videoUrl = it }, 
            label = { Text("رابط الفيديو (YouTube / Facebook)") }, 
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("أدخل رابط الفيديو هنا") }
        )
        
        if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (title.isNotBlank() && videoUrl.isNotBlank()) {
                    isUploading = true
                    val data = hashMapOf("title" to title, "url" to videoUrl, "timestamp" to Timestamp.now())
                    db.collection("videos").add(data).addOnSuccessListener {
                        sendPushNotification(context, "فيديو جديد", "تمت إضافة فيديو: $title")
                        isUploading = false
                        title = ""
                        videoUrl = ""
                        Toast.makeText(context, "تم النشر بنجاح", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        isUploading = false
                        Toast.makeText(context, "حدث خطأ أثناء النشر", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "يرجى إدخال العنوان والرابط", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) { Text("نشر الفيديو") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("الفيديوهات المنشورة حالياً:", style = MaterialTheme.typography.titleSmall)
        DeleteManagerSection("videos", "title")
    }
}

@Composable
fun DailyVerseForm() {
    var verseText by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = verseText, onValueChange = { verseText = it }, label = { Text("نص الآية") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("الشاهد") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (verseText.isNotBlank()) {
                db.collection("app_data").document("daily_verse").set(hashMapOf("text" to verseText, "reference" to reference))
                    .addOnSuccessListener { 
                        sendPushNotification(context, "آية اليوم", verseText)
                        Toast.makeText(context, "تم النشر", Toast.LENGTH_SHORT).show() 
                    }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("نشر آية اليوم") }
    }
}

@Composable
fun ManageUsersSection() {
    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            users = snapshot?.documents?.map { Triple(it.id, it.getString("name") ?: "", it.getString("email") ?: "") } ?: emptyList()
        }
    }
    users.forEach { (id, name, email) ->
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(name); Text(email, style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = { db.collection("users").document(id).delete() }) {
                    Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AddWeeklyQuestionForm(onNavigate: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        db.collection("questions").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshot, _ ->
            questions = snapshot?.documents?.map { it.id to (it.getString("text") ?: "") } ?: emptyList()
        }
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text, 
            onValueChange = { text = it }, 
            label = { Text("سؤال جديد") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { 
                if (text.isNotBlank()) {
                    db.collection("questions").add(hashMapOf("text" to text, "timestamp" to Timestamp.now())) 
                        .addOnSuccessListener { 
                            sendPushNotification(context, "سؤال الأسبوع", text)
                            text = ""
                            Toast.makeText(context, "تم نشر السؤال", Toast.LENGTH_SHORT).show()
                        }
                }
            }, 
            modifier = Modifier.fillMaxWidth()
        ) { Text("نشر السؤال") }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("الأسئلة السابقة (اضغط لمشاهدة الإجابات أو الحذف):", style = MaterialTheme.typography.titleSmall)
        
        questions.forEach { (id, qText) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.padding(8.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(qText, modifier = Modifier.weight(1f), maxLines = 1)
                    Row {
                        IconButton(onClick = { onNavigate(Screen.AdminAnswers.createRoute(id)) }) { 
                            Icon(Icons.Default.Visibility, contentDescription = "مشاهدة الإجابات", tint = MaterialTheme.colorScheme.primary) 
                        }
                        IconButton(onClick = { 
                            db.collection("questions").document(id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "تم حذف السؤال", Toast.LENGTH_SHORT).show()
                                }
                        }) { 
                            Icon(Icons.Default.Delete, contentDescription = "حذف السؤال", tint = Color.Red) 
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteManagerSection(collection: String, field: String) {
    val db = FirebaseFirestore.getInstance()
    var items by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(Unit) {
        db.collection(collection).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshot, _ ->
            items = snapshot?.documents?.map { it.id to (it.getString(field) ?: "") } ?: emptyList()
        }
    }
    items.forEach { (id, title) ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = { db.collection(collection).document(id).delete() }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        }
    }
}

@Composable
fun AdminSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold) 
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

/**
 * وظيفة لإرسال إشعار. 
 * ملاحظة: في التطبيقات الحقيقية يتم إرسال الإشعارات عبر Firebase Cloud Functions 
 * أو عبر Console، ولكن هنا نضع دالة يمكن توسيعها لإرسال طلب إلى API الإشعارات.
 */
fun sendPushNotification(context: android.content.Context, title: String, body: String) {
    // هنا يمكن إضافة كود إرسال إشعار عبر API أو Firebase
    // حالياً نقوم فقط بإظهار توست لإعلام الأدمن بأن الإشعار سيصل للمستخدمين
    Toast.makeText(context, "سيتم إرسال إشعار للمستخدمين: $title", Toast.LENGTH_LONG).show()
}
