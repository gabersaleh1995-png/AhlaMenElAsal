package com.gaber.ahlamenelasal.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay

data class AudioFile(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)

@Composable
fun AudioLibraryScreen() {
    val db = FirebaseFirestore.getInstance()
    var audioFiles by remember { mutableStateOf<List<AudioFile>>(emptyList()) }
    val context = LocalContext.current
    
    // إدارة المشغل بشكل مركزي
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("audios")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    audioFiles = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AudioFile::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    // تحديث شريط التقدم
    LaunchedEffect(isPlaying, currentlyPlayingUrl) {
        while (isPlaying && currentlyPlayingUrl != null) {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        currentPosition = it.currentPosition
                    }
                } catch (e: Exception) {
                    isPlaying = false
                }
            }
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "مكتبة التسجيلات الصوتية",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (audioFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد تسجيلات متاحة حالياً")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(audioFiles) { audio ->
                    val isThisPlaying = currentlyPlayingUrl == audio.url
                    
                    AudioItem(
                        audio = audio,
                        isPlaying = isThisPlaying && isPlaying,
                        isLoading = isLoading && isThisPlaying,
                        currentPosition = if (isThisPlaying) currentPosition else 0,
                        duration = if (isThisPlaying) duration else 0,
                        onSeek = { 
                            if (isThisPlaying) {
                                mediaPlayer?.seekTo(it.toInt())
                                currentPosition = it.toInt()
                            }
                        },
                        onPlayPause = {
                            if (currentlyPlayingUrl == audio.url) {
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                } else {
                                    mediaPlayer?.start()
                                    isPlaying = true
                                }
                            } else {
                                mediaPlayer?.release()
                                isPlaying = false
                                currentPosition = 0
                                duration = 0
                                isLoading = true
                                currentlyPlayingUrl = audio.url
                                
                                val playUrl = transformGoogleDriveUrl(audio.url)
                                
                                mediaPlayer = MediaPlayer().apply {
                                    setAudioAttributes(
                                        AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .build()
                                    )
                                    try {
                                        setDataSource(playUrl)
                                        prepareAsync()
                                        setOnPreparedListener { 
                                            start()
                                            duration = it.duration
                                            isLoading = false
                                            isPlaying = true
                                        }
                                        setOnCompletionListener {
                                            currentlyPlayingUrl = null
                                            isPlaying = false
                                            currentPosition = 0
                                        }
                                        setOnErrorListener { _, what, extra ->
                                            Toast.makeText(context, "خطأ في تشغيل الملف", Toast.LENGTH_SHORT).show()
                                            currentlyPlayingUrl = null
                                            isLoading = false
                                            isPlaying = false
                                            true
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "فشل تحميل الملف", Toast.LENGTH_SHORT).show()
                                        currentlyPlayingUrl = null
                                        isLoading = false
                                        isPlaying = false
                                    }
                                }
                            }
                        },
                        onDownload = {
                            downloadAudioFile(context, audio.url, audio.title)
                        }
                    )
                }
            }
        }
    }
}

private fun downloadAudioFile(context: Context, url: String, title: String) {
    try {
        val downloadUrl = transformGoogleDriveUrl(url)
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(title)
            .setDescription("جاري تحميل التسجيل الصوتي...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "$title.mp3")
        
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "بدأ تحميل الصوت...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "خطأ في التحميل: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun transformGoogleDriveUrl(url: String): String {
    if (!url.contains("drive.google.com") && !url.contains("docs.google.com")) return url
    return try {
        val fileId = when {
            url.contains("/file/d/") -> url.substringAfter("/file/d/").substringBefore("/").substringBefore("?")
            url.contains("id=") -> url.substringAfter("id=").substringBefore("&")
            url.contains("/d/") -> url.substringAfter("/d/").substringBefore("/")
            else -> null
        }
        if (fileId != null) "https://drive.google.com/uc?id=$fileId&export=download" else url
    } catch (e: Exception) { url }
}

@Composable
fun AudioItem(
    audio: AudioFile, 
    isPlaying: Boolean, 
    isLoading: Boolean, 
    currentPosition: Int,
    duration: Int,
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit, 
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = audio.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = "تحميل", tint = MaterialTheme.colorScheme.primary)
                }
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (duration > 0 || currentPosition > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = onSeek,
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(currentPosition), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = formatTime(duration), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
