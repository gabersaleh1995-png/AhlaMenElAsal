package com.gaber.ahlamenelasal.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    chatId: String, 
    title: String, 
    chatViewModel: ChatViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var textState by remember { mutableStateOf("") }
    val messages = chatViewModel.messages
    val isAdmin by authViewModel.isAdmin
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
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
        } else {
            Toast.makeText(context, "تحتاج إلى إذن الميكروفون للتسجيل", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(chatId) {
        chatViewModel.listenToMessages(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.primaryContainer
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
            color = MaterialTheme.colorScheme.surface
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
                                chatViewModel.sendVoiceMessage(chatId, Uri.fromFile(finalFile)) { uploading ->
                                    isUploading = uploading
                                }
                            } else {
                                Toast.makeText(context, "التسجيل فارغ أو لم يكتمل", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("ChatScreen", "Recording stop error: ${e.message}")
                            Toast.makeText(context, "التسجيل قصير جداً أو حدث خطأ", Toast.LENGTH_SHORT).show()
                            mediaRecorder?.release()
                            mediaRecorder = null
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "إرسال الصوت", tint = Color.Green)
                    }
                    IconButton(onClick = {
                        isRecording = false
                        try {
                            mediaRecorder?.stop()
                        } catch (e: Exception) {}
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
                                    Log.e("ChatScreen", "Recording start error: ${e.message}")
                                    Toast.makeText(context, "فشل بدء التسجيل: ${e.message}", Toast.LENGTH_SHORT).show()
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
                detectTapGestures(
                    onLongPress = {
                        showDeleteOptions = true
                    }
                )
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
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (chatMessage.isMe) 16.dp else 0.dp,
                bottomEnd = if (chatMessage.isMe) 0.dp else 16.dp
            ),
            color = if (chatMessage.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // التحقق من النوع كسلسلة نصية
                if (chatMessage.type == "VOICE") {
                    VoicePlayer(chatMessage.voiceUrl, chatMessage.isMe)
                } else {
                    Text(
                        text = chatMessage.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (chatMessage.isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (chatMessage.isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
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
                    TextButton(
                        onClick = {
                            onDeleteForMe()
                            showDeleteOptions = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حذف لدي فقط")
                    }
                    
                    if (isAdmin || chatMessage.isMe) {
                        TextButton(
                            onClick = {
                                onDeleteForAll()
                                showDeleteOptions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حذف لدى الجميع", color = Color.Red)
                        }
                    }
                    
                    TextButton(
                        onClick = { showDeleteOptions = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إلغاء")
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
fun VoicePlayer(voiceUrl: String, isMe: Boolean) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    DisposableEffect(voiceUrl) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            IconButton(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                        isPlaying = false
                    } else {
                        if (mediaPlayer == null) {
                            if (voiceUrl.isBlank()) {
                                Toast.makeText(context, "رابط الصوت غير صالح", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            isLoading = true
                            mediaPlayer = MediaPlayer().apply {
                                try {
                                    setDataSource(voiceUrl)
                                    prepareAsync()
                                    setOnPreparedListener { 
                                        isLoading = false
                                        start()
                                        isPlaying = true
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                    }
                                    setOnErrorListener { _, what, extra ->
                                        Log.e("VoicePlayer", "MediaPlayer Error: $what, $extra")
                                        isLoading = false
                                        Toast.makeText(context, "خطأ في تشغيل الصوت", Toast.LENGTH_SHORT).show()
                                        isPlaying = false
                                        true
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    Log.e("VoicePlayer", "Exception: ${e.message}")
                                    Toast.makeText(context, "فشل تحميل ملف الصوت", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            mediaPlayer?.start()
                            isPlaying = true
                        }
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
        Text(
            text = "رسالة صوتية",
            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}
