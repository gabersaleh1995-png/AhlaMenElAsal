package com.gaber.ahlamenelasal.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.data.model.ChatMessage
import com.gaber.ahlamenelasal.ui.viewmodel.AuthViewModel
import com.gaber.ahlamenelasal.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    chatId: String, 
    title: String, 
    onBack: () -> Unit = {},
    chatViewModel: ChatViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var textState by remember { mutableStateOf("") }
    val messages = chatViewModel.messages
    val isAdmin by authViewModel.isAdmin
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // مراقبة حالة الغرفة للطرد التلقائي
    LaunchedEffect(chatId, isAdmin) {
        if (!isAdmin) {
            // ننتظر قليلاً عند الدخول للتأكد من أن حالة السيرفر استقرت
            delay(1000) 
            chatViewModel.listenToChatStatus(chatId) { status, _ ->
                // يتم الطرد فقط إذا تغيرت الحالة إلى locked والمستخدم بالداخل بالفعل
                // أما عند الدخول الجديد، فشاشة ChatSelectionScreen تتكفل بالتحقق
            }
        }
    }
    
    // تسجيل الصوت
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "تم منح الإذن، اضغط مرة أخرى للبدء", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(chatId) {
        chatViewModel.listenToMessages(chatId)
        chatViewModel.markMessagesAsRead(chatId) // تحديث الرسائل كمقروءة عند الدخول
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    chatMessage = message, 
                    isAdmin = isAdmin,
                    onDeleteForMe = { chatViewModel.deleteMessageForMe(chatId, message.id) },
                    onDeleteForAll = { chatViewModel.deleteMessageForAll(chatId, message.id) }
                )
            }
        }

        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecording) {
                    Icon(
                        Icons.Default.Mic, 
                        contentDescription = null, 
                        tint = Color.Red,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "جاري التسجيل...",
                        modifier = Modifier.weight(1f),
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = {
                        try {
                            isRecording = false
                            mediaRecorder?.let { recorder ->
                                recorder.stop()
                                recorder.release()
                            }
                            mediaRecorder = null
                            
                            val finalFile = audioFile
                            if (finalFile != null && finalFile.exists() && finalFile.length() > 0) {
                                Toast.makeText(context, "جاري رفع الريكورد...", Toast.LENGTH_SHORT).show()
                                chatViewModel.sendVoiceMessage(chatId, finalFile.absolutePath) { uploading, error ->
                                    isUploading = uploading
                                    error?.let {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "التسجيل فارغ", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("ChatScreen", "Recording stop error: ${e.message}")
                            Toast.makeText(context, "حدث خطأ أثناء حفظ التسجيل", Toast.LENGTH_SHORT).show()
                            mediaRecorder?.release()
                            mediaRecorder = null
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "إرسال الصوت", tint = Color.Green)
                    }
                    IconButton(onClick = {
                        isRecording = false
                        try { mediaRecorder?.stop() } catch (e: Exception) {}
                        mediaRecorder?.release()
                        mediaRecorder = null
                        audioFile?.delete()
                        audioFile = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "إلغاء", tint = Color.Red)
                    }
                } else {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("اكتب رسالتك هنا...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    
                    if (textState.isNotBlank()) {
                        IconButton(onClick = {
                            chatViewModel.sendMessage(chatId, textState)
                            textState = ""
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    val voiceDir = File(context.filesDir, "voices")
                                    if (!voiceDir.exists()) voiceDir.mkdirs()
                                    
                                    val file = File(voiceDir, "voice_${System.currentTimeMillis()}.m4a")
                                    audioFile = file
                                    
                                    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        MediaRecorder(context)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        MediaRecorder()
                                    }
                                    
                                    mediaRecorder = recorder.apply {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                        setOutputFile(file.absolutePath)
                                        prepare()
                                        start()
                                    }
                                    isRecording = true
                                } catch (e: Exception) {
                                    Toast.makeText(context, "فشل بدء التسجيل", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "تسجيل", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    chatMessage: ChatMessage, 
    isAdmin: Boolean, 
    onDeleteForMe: () -> Unit,
    onDeleteForAll: () -> Unit
) {
    val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
    val timeString = chatMessage.timestamp?.let { sdf.format(it.toDate()) } ?: ""
    var showDeleteOptions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(isAdmin, chatMessage.isMe) {
                detectTapGestures(onLongPress = { showDeleteOptions = true })
            },
        horizontalAlignment = if (chatMessage.isMe) Alignment.End else Alignment.Start
    ) {
        if (!chatMessage.isMe) {
            Text(
                text = chatMessage.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
            )
        }
        
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (chatMessage.isMe) 16.dp else 0.dp,
                bottomEnd = if (chatMessage.isMe) 0.dp else 16.dp
            ),
            color = if (chatMessage.isMe) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) 
            else 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (chatMessage.type == "VOICE") {
                    VoicePlayer(chatMessage, chatMessage.isMe)
                } else {
                    Text(
                        text = chatMessage.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (chatMessage.isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (chatMessage.isMe) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                    
                    if (chatMessage.isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (chatMessage.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (chatMessage.isRead) Color(0xFF00B0FF) else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteOptions) {
        AlertDialog(
            onDismissRequest = { showDeleteOptions = false },
            title = { Text("حذف الرسالة") },
            text = { Text("اختر نوع الحذف:") },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { onDeleteForMe(); showDeleteOptions = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("حذف لدي فقط")
                    }
                    if (isAdmin || chatMessage.isMe) {
                        TextButton(onClick = { onDeleteForAll(); showDeleteOptions = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("حذف لدى الجميع", color = Color.Red)
                        }
                    }
                    TextButton(onClick = { showDeleteOptions = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("إلغاء")
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
fun VoicePlayer(chatMessage: ChatMessage, isMe: Boolean) {
    val context = LocalContext.current
    
    val voiceUrl = remember(chatMessage.voiceUrl) {
        val url = chatMessage.voiceUrl
        when {
            url.isBlank() || url == "null" -> ""
            url.startsWith("http://") -> url.replace("http://", "https://")
            else -> url
        }
    }
    
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    
    var totalDuration by remember(chatMessage.id) { 
        mutableIntStateOf(if (chatMessage.voiceDuration > 0) chatMessage.voiceDuration * 1000 else 0) 
    }
    
    DisposableEffect(chatMessage.id) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        currentPosition = it.currentPosition
                    } else {
                        isPlaying = false
                    }
                } catch (e: Exception) {
                    isPlaying = false
                }
            }
            delay(100)
        }
    }

    fun formatTime(ms: Int): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    Column(modifier = Modifier.width(200.dp).padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = if (isMe) Color.White else MaterialTheme.colorScheme.primary)
            } else {
                IconButton(
                    onClick = {
                        try {
                            if (isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else {
                                if (mediaPlayer == null) {
                                    if (voiceUrl.isBlank()) {
                                        Toast.makeText(context, "الرابط غير صالح أو محذوف", Toast.LENGTH_SHORT).show()
                                        return@IconButton
                                    }
                                    isLoading = true
                                    
                                    mediaPlayer = MediaPlayer().apply {
                                        setAudioAttributes(
                                            AudioAttributes.Builder()
                                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                                .build()
                                        )
                                        setDataSource(voiceUrl)
                                        
                                        setOnPreparedListener { 
                                            isLoading = false
                                            totalDuration = duration
                                            start()
                                            isPlaying = true
                                        }
                                        
                                        setOnCompletionListener { 
                                            isPlaying = false
                                            currentPosition = 0
                                        }
                                        
                                        setOnErrorListener { _, what, extra ->
                                            isLoading = false
                                            isPlaying = false
                                            Toast.makeText(context, "خطأ في تشغيل الملف", Toast.LENGTH_SHORT).show()
                                            true
                                        }
                                        
                                        prepareAsync()
                                    }
                                } else {
                                    mediaPlayer?.start()
                                    isPlaying = true
                                }
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            Toast.makeText(context, "فشل التشغيل", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { 
                    currentPosition = it.toInt()
                    mediaPlayer?.seekTo(it.toInt())
                },
                valueRange = 0f..totalDuration.coerceAtLeast(100).toFloat(),
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                    activeTrackColor = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray,
                fontSize = 10.sp
            )
            Text(
                text = formatTime(totalDuration),
                color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
