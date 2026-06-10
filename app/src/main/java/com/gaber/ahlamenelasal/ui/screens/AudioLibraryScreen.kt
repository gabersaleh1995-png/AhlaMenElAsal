package com.gaber.ahlamenelasal.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaber.ahlamenelasal.ui.theme.*
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

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingAudio by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("audios").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null)
                    audioFiles = snap.documents.mapNotNull { it.toObject(AudioFile::class.java)?.copy(id = it.id) }
            }
    }

    LaunchedEffect(isPlaying, currentlyPlayingUrl) {
        while (isPlaying && currentlyPlayingUrl != null) {
            mediaPlayer?.let { try { if (it.isPlaying) currentPosition = it.currentPosition } catch (_: Exception) { isPlaying = false } }
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.stop(); mediaPlayer?.release() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("مكتبة التسجيلات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("استمع وحمّل التسجيلات الصوتية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

        if (audioFiles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎙️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("لا توجد تسجيلات متاحة حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(audioFiles) { audio ->
                    val isThis = currentlyPlayingUrl == audio.url
                    AudioItem(
                        audio = audio,
                        isPlaying = isThis && isPlaying,
                        isLoading = isLoadingAudio && isThis,
                        currentPosition = if (isThis) currentPosition else 0,
                        duration = if (isThis) duration else 0,
                        onSeek = { if (isThis) { mediaPlayer?.seekTo(it.toInt()); currentPosition = it.toInt() } },
                        onPlayPause = {
                            if (currentlyPlayingUrl == audio.url) {
                                if (isPlaying) { mediaPlayer?.pause(); isPlaying = false }
                                else { mediaPlayer?.start(); isPlaying = true }
                            } else {
                                mediaPlayer?.release(); isPlaying = false; currentPosition = 0; duration = 0
                                isLoadingAudio = true; currentlyPlayingUrl = audio.url
                                val playUrl = transformGoogleDriveUrl(audio.url)
                                mediaPlayer = MediaPlayer().apply {
                                    setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())
                                    try {
                                        setDataSource(playUrl); prepareAsync()
                                        setOnPreparedListener { start(); duration = it.duration; isLoadingAudio = false; isPlaying = true }
                                        setOnCompletionListener { currentlyPlayingUrl = null; isPlaying = false; currentPosition = 0 }
                                        setOnErrorListener { _, _, _ -> Toast.makeText(context, "خطأ في تشغيل الملف", Toast.LENGTH_SHORT).show(); currentlyPlayingUrl = null; isLoadingAudio = false; isPlaying = false; true }
                                    } catch (_: Exception) { Toast.makeText(context, "فشل تحميل الملف", Toast.LENGTH_SHORT).show(); currentlyPlayingUrl = null; isLoadingAudio = false; isPlaying = false }
                                }
                            }
                        },
                        onDownload = { downloadAudioFile(context, audio.url, audio.title) }
                    )
                }
            }
        }
    }
}

private fun downloadAudioFile(context: Context, url: String, title: String) {
    try {
        val req = DownloadManager.Request(Uri.parse(transformGoogleDriveUrl(url)))
            .setTitle(title).setDescription("جاري تحميل التسجيل...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "$title.mp3")
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
        Toast.makeText(context, "بدأ تحميل الصوت...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) { Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_LONG).show() }
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
    } catch (_: Exception) { url }
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
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GraphicEq, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(audio.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, "تحميل", tint = MaterialTheme.colorScheme.primary)
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    IconButton(onClick = onPlayPause) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
            }
            if (duration > 0 || currentPosition > 0) {
                Spacer(Modifier.height(6.dp))
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = onSeek,
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth().height(20.dp),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(currentPosition), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    Text(formatTime(duration), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val s = ms / 1000; return String.format("%02d:%02d", s / 60, s % 60)
}
