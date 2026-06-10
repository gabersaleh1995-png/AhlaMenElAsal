package com.gaber.ahlamenelasal.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.navigation.Screen
import com.gaber.ahlamenelasal.ui.viewmodel.AdminViewModel
import com.gaber.ahlamenelasal.util.NotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = cursor.getString(index)
            }
        } finally { cursor?.close() }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) result = result?.substring(cut + 1)
    }
    return result?.substringBeforeLast(".") ?: "ملف جديد"
}

@Composable
fun AdminScreen(
    onNavigate: (String) -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

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

        // قسم إرسال إشعار مخصص للجميع
        var customTitle by remember { mutableStateOf("") }
        var customBody by remember { mutableStateOf("") }

        AdminSection(title = "إرسال إشعار عام (تنبيه)", icon = Icons.Default.NotificationsActive) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("عنوان التنبيه (مثال: تنبيه هام)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customBody,
                    onValueChange = { customBody = it },
                    label = { Text("نص الرسالة") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Button(
                    onClick = { 
                        if (customTitle.isNotBlank() && customBody.isNotBlank()) {
                            NotificationHelper.notifyAll(customTitle, customBody)
                            Toast.makeText(context, "تم إرسال التنبيه للجميع بنجاح", Toast.LENGTH_LONG).show()
                            customTitle = ""
                            customBody = ""
                        } else {
                            Toast.makeText(context, "برجاء كتابة العنوان والرسالة أولاً", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("إرسال الآن")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (adminViewModel.isUploading.value) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("جاري الرفع الآن...", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { adminViewModel.uploadProgress.value },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                    Text("${(adminViewModel.uploadProgress.value * 100).toInt()}%")
                }
            }
        }

        AdminSection(title = "آية اليوم", icon = Icons.Default.FormatQuote) { DailyVerseForm() }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "الموضوعات المصورة والفيديو", icon = Icons.Default.Topic) { TopicUploader(adminViewModel) }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "المواعيد", icon = Icons.Default.Event) { MeetingManagerSection() }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "معرض الصور (رفع أو رابط)", icon = Icons.Default.Image) { ImageUploader(adminViewModel) }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "نشر فيديوهات (رفع أو رابط)", icon = Icons.Default.PlayCircle) { AddVideoForm(adminViewModel) }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "رفع الكتب والملفات PDF", icon = Icons.Default.PictureAsPdf) { UniversalFileUploader(adminViewModel) }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "رفع تسجيل صوتي", icon = Icons.Default.Mic) { AudioUploader(adminViewModel) }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "إدارة الأعضاء", icon = Icons.Default.People) { ManageUsersSection() }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "سؤال الأسبوع", icon = Icons.Default.Help) { AddWeeklyQuestionForm(onNavigate) }
        Spacer(modifier = Modifier.height(24.dp))
        AdminSection(title = "التحكم في غرف الشات", icon = Icons.Default.Lock) { ChatLockSection(adminViewModel) }
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
fun TopicUploader(adminViewModel: AdminViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var directLink by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("IMAGE") } // IMAGE or VIDEO
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adminViewModel.uploadFileToCloudinary(context, it, "TOPIC_IMAGE", title.ifBlank { getFileNameFromUri(context, it) }, description = description) }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adminViewModel.uploadFileToCloudinary(context, it, "TOPIC_VIDEO", title.ifBlank { getFileNameFromUri(context, it) }, description = description) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الموضوع") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("نص الموضوع (الوصف)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = mediaType == "IMAGE", onClick = { mediaType = "IMAGE" })
            Text("صورة", modifier = Modifier.clickable { mediaType = "IMAGE" })
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = mediaType == "VIDEO", onClick = { mediaType = "VIDEO" })
            Text("فيديو", modifier = Modifier.clickable { mediaType = "VIDEO" })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { if (mediaType == "IMAGE") imageLauncher.launch("image/*") else videoLauncher.launch("video/*") },
                modifier = Modifier.weight(1f),
                enabled = !adminViewModel.isUploading.value
            ) {
                Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("رفع من الهاتف")
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("أو استخدم رابطاً خارجياً:", style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(value = directLink, onValueChange = { directLink = it }, label = { Text("رابط الميديا (صورة أو فيديو)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                if (title.isNotBlank() && directLink.isNotBlank()) {
                    val type = if (mediaType == "IMAGE") "TOPIC_IMAGE" else "TOPIC_VIDEO"
                    adminViewModel.saveDirectLink(context, title, directLink, type, description = description)
                    title = ""; description = ""; directLink = ""; Toast.makeText(context, "تم النشر", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "يرجى إدخال العنوان والرابط", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("نشر باستخدام الرابط")
        }
        DeleteManagerSection("topics", "title")
    }
}

@Composable
fun ImageUploader(adminViewModel: AdminViewModel) {
    var directLink by remember { mutableStateOf("") }
    var titleForLink by remember { mutableStateOf("") }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adminViewModel.uploadFileToCloudinary(context, it, "IMAGE", getFileNameFromUri(context, it)) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), enabled = !adminViewModel.isUploading.value) {
            Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("رفع صورة من الهاتف")
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        OutlinedTextField(value = titleForLink, onValueChange = { titleForLink = it }, label = { Text("عنوان الصورة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = directLink, onValueChange = { directLink = it }, label = { Text("رابط صورة خارجي") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (titleForLink.isNotBlank() && directLink.isNotBlank()) {
                adminViewModel.saveDirectLink(context, titleForLink, directLink, "IMAGE")
                titleForLink = ""; directLink = ""; Toast.makeText(context, "تم النشر", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("نشر رابط صورة")
        }
        DeleteManagerSection("images", "title")
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

    val calendar = Calendar.getInstance()

    // تهيئة DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // تهيئة TimePickerDialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            time = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // نظام 24 ساعة
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الاجتماع") }, modifier = Modifier.fillMaxWidth())
        
        // اختيار التاريخ عبر التقويم
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = date,
                onValueChange = { },
                label = { Text("التاريخ") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
            // طبقة غير مرئية فوق الحقل لاستقبال الضغطات وفتح التقويم
            Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
        }

        // اختيار الوقت عبر منقي الوقت
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = time,
                onValueChange = { },
                label = { Text("الوقت") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
            )
            Box(modifier = Modifier.matchParentSize().clickable { timePickerDialog.show() })
        }

        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("المكان") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())
        
        Button(onClick = {
            if (title.isNotBlank() && date.isNotBlank() && time.isNotBlank()) {
                val meetingData = hashMapOf(
                    "title" to title,
                    "date" to date,
                    "time" to time,
                    "location" to location,
                    "description" to description,
                    "timestamp" to Timestamp.now()
                )
                db.collection("meetings").add(meetingData)
                    .addOnSuccessListener {
                        // جدولة الإشعار
                        val scheduleTime = "${date.trim()} ${time.trim()}:00 GMT+0200"
                        NotificationHelper.notifyAllScheduled(
                            title = "تذكير بموعد اجتماع 📅",
                            body = "لديك اجتماع الآن بعنوان: $title",
                            scheduleTime = scheduleTime
                        )
                        
                        title = ""; date = ""; time = ""; location = ""; description = ""
                        Toast.makeText(context, "تم الحفظ والجدولة", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "يرجى ملء العنوان والتاريخ والوقت", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("إضافة الاجتماع وجدولته") }

        DeleteManagerSection("meetings", "title")
    }
}

@Composable
fun UniversalFileUploader(adminViewModel: AdminViewModel) {
    val categories = listOf("المكتبة العامة")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var folderName by remember { mutableStateOf("") }
    var subFolderName by remember { mutableStateOf("") }
    var directLink by remember { mutableStateOf("") }
    var titleForLink by remember { mutableStateOf("") }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adminViewModel.uploadFileToCloudinary(context, it, "PDF", getFileNameFromUri(context, it), selectedCategory, folder = folderName, subFolder = subFolderName) }
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { 
            adminViewModel.uploadFolderContent(context, it, selectedCategory)
            Toast.makeText(context, "بدأ فحص ورفع محتويات المجلد..", Toast.LENGTH_SHORT).show()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedCategory); Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false }) }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = folderName, onValueChange = { folderName = it }, label = { Text("اسم المجلد الرئيسي") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = subFolderName, onValueChange = { subFolderName = it }, label = { Text("المجلد الفرعي") }, modifier = Modifier.weight(1f))
        }

        Text("الخيار 1: رفع ملفات من الهاتف", style = MaterialTheme.typography.labelSmall)
        Button(
            onClick = { pdfLauncher.launch("application/pdf") }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = !adminViewModel.isUploading.value
        ) {
            Icon(Icons.Default.PictureAsPdf, null); Spacer(Modifier.width(8.dp)); Text("رفع ملف PDF")
        }

        Button(
            onClick = { folderLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !adminViewModel.isUploading.value
        ) {
            Icon(Icons.Default.CreateNewFolder, null)
            Spacer(Modifier.width(8.dp))
            Text("رفع مجلد PDF كامل")
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("الخيار 2: وضع رابط مباشر (مثل جوجل درايف)", style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(value = titleForLink, onValueChange = { titleForLink = it }, label = { Text("عنوان الكتاب") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = directLink, onValueChange = { directLink = it }, label = { Text("رابط الملف") }, modifier = Modifier.fillMaxWidth())
        
        Button(onClick = {
            if (titleForLink.isNotBlank() && directLink.isNotBlank()) {
                adminViewModel.saveDirectLink(context, titleForLink, directLink, "PDF", selectedCategory, folder = folderName, subFolder = subFolderName)
                titleForLink = ""; directLink = "";
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("نشر رابط PDF")
        }
        DeleteManagerSection("bible_pdfs", "title")
    }
}

@Composable
fun AudioUploader(adminViewModel: AdminViewModel) {
    var directLink by remember { mutableStateOf("") }
    var titleForLink by remember { mutableStateOf("") }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adminViewModel.uploadFileToCloudinary(context, it, "AUDIO", getFileNameFromUri(context, it)) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { launcher.launch("audio/*") }, modifier = Modifier.fillMaxWidth(), enabled = !adminViewModel.isUploading.value) {
            Icon(Icons.Default.Mic, null); Spacer(Modifier.width(8.dp)); Text("رفع تسجيل من الهاتف")
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        OutlinedTextField(value = titleForLink, onValueChange = { titleForLink = it }, label = { Text("عنوان التسجيل") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = directLink, onValueChange = { directLink = it }, label = { Text("رابط تسجيل (Drive/Direct)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (titleForLink.isNotBlank() && directLink.isNotBlank()) {
                adminViewModel.saveDirectLink(context, titleForLink, directLink, "AUDIO")
                titleForLink = ""; directLink = ""; Toast.makeText(context, "تم النشر", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("نشر رابط صوتي")
        }
        DeleteManagerSection("audios", "title")
    }
}

@Composable
fun AddVideoForm(adminViewModel: AdminViewModel) {
    var title by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adminViewModel.uploadFileToCloudinary(context, it, "VIDEO", getFileNameFromUri(context, it)) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الفيديو") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it }, label = { Text("رابط YouTube/Drive") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (title.isNotBlank() && videoUrl.isNotBlank()) {
                adminViewModel.saveDirectLink(context, title, videoUrl, "VIDEO")
                title = ""; videoUrl = ""; Toast.makeText(context, "تم النشر", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("نشر رابط فيديو") }
        
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Button(onClick = { launcher.launch("video/*") }, modifier = Modifier.fillMaxWidth(), enabled = !adminViewModel.isUploading.value) {
            Icon(Icons.Default.Movie, null); Spacer(Modifier.width(8.dp)); Text("رفع فيديو من الهاتف")
        }
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
                        NotificationHelper.notifyAll("آية اليوم الجديدة ✨", "$verseText ($reference)")
                        Toast.makeText(context, "تم النشر وإرسال الإشعار", Toast.LENGTH_SHORT).show() 
                    }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("نشر آية اليوم") }
    }
}

@Composable
fun ManageUsersSection() {
    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var userToDelete by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            users = snapshot?.documents?.map { Triple(it.id, it.getString("name") ?: "بدون اسم", it.getString("email") ?: "") } ?: emptyList()
        }
    }

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

    users.forEach { (id, name, email) ->
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) { 
                    Text(name, fontWeight = FontWeight.Bold)
                    Text(email, style = MaterialTheme.typography.bodySmall, color = Color.Gray) 
                }
                IconButton(onClick = { userToDelete = Triple(id, name, email) }) { 
                    Icon(Icons.Default.PersonRemove, null, tint = Color.Red.copy(alpha = 0.7f)) 
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
    var questionToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        db.collection("questions").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshot, _ ->
            questions = snapshot?.documents?.map { it.id to (it.getString("text") ?: "") } ?: emptyList()
        }
    }

    if (questionToDelete != null) {
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("حذف السؤال") },
            text = { Text("هل تريد حذف هذا السؤال وجميع إجاباته؟") },
            confirmButton = {
                TextButton(onClick = {
                    questionToDelete?.let { q ->
                        db.collection("questions").document(q.first).delete()
                            .addOnSuccessListener { Toast.makeText(context, "تم الحذف", Toast.LENGTH_SHORT).show() }
                    }
                    questionToDelete = null
                }) { Text("حذف", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { questionToDelete = null }) { Text("إلغاء") } }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("سؤال جديد") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { 
            if (text.isNotBlank()) {
                db.collection("questions").add(hashMapOf("text" to text, "timestamp" to Timestamp.now()))
                    .addOnSuccessListener { 
                        NotificationHelper.notifyAll("سؤال الأسبوع الجديد ❓", text)
                        text = ""; Toast.makeText(context, "تم النشر وإرسال الإشعار", Toast.LENGTH_SHORT).show() 
                    } 
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("نشر السؤال") }
        
        questions.forEach { (id, qText) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(qText, modifier = Modifier.weight(1f), maxLines = 1)
                    Row {
                        IconButton(onClick = { onNavigate(Screen.AdminAnswers.createRoute(id)) }) { 
                            Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary) 
                        }
                        IconButton(onClick = { questionToDelete = id to qText }) { 
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f)) 
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
    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection(collection).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshot, _ ->
            items = snapshot?.documents?.map { it.id to (it.getString(field) ?: "بدون عنوان") } ?: emptyList()
        }
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف '${itemToDelete?.second}'؟") },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.let { item ->
                        db.collection(collection).document(item.first).delete()
                            .addOnSuccessListener { Toast.makeText(context, "تم الحذف", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { Toast.makeText(context, "فشل الحذف", Toast.LENGTH_SHORT).show() }
                    }
                    itemToDelete = null
                }) { Text("حذف", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    items.forEach { (id, title) ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1)
            IconButton(onClick = { itemToDelete = id to title }) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatLockSection(adminViewModel: AdminViewModel) {
    val currentStatus by adminViewModel.chatStatus
    val currentPasscode by adminViewModel.chatPasscode
    
    var passcode by remember(currentPasscode) { mutableStateOf(currentPasscode) }
    var isLocked by remember(currentStatus) { mutableStateOf(currentStatus == "locked") }
    val context = LocalContext.current

    val chatId = "group_all"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "الحالة الحالية: ${if (currentStatus == "locked") "🔒 مقفولة" else "🔓 مفتوحة"}",
            color = if (currentStatus == "locked") Color.Red else Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("قفل الدردشة الجماعية")
            Switch(
                checked = isLocked,
                onCheckedChange = { isLocked = it }
            )
        }

        if (isLocked) {
            OutlinedTextField(
                value = passcode,
                onValueChange = { passcode = it },
                label = { Text("كود الدخول") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Button(
            onClick = {
                adminViewModel.lockChat(chatId, passcode, isLocked)
                val msg = if (isLocked) "تم قفل الغرفة" else "تم فتح الغرفة"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ التغييرات")
        }
    }
}

@Composable
fun AdminSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text(title, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp)); content()
        }
    }
}
