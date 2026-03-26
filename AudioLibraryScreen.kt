package com.gaber.ahlamenelasal.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
    var isLoading by remember { mutableStateOf(false) }

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
                    AudioItem(
                        audio = audio,
                        isPlaying = currentlyPlayingUrl == audio.url,
                        isLoading = isLoading && currentlyPlayingUrl == audio.url,
                        onPlayPause = {
                            if (currentlyPlayingUrl == audio.url) {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                                currentlyPlayingUrl = null
                                isLoading = false
                            } else {
                                mediaPlayer?.release()
                                isLoading = true
                                currentlyPlayingUrl = audio.url
                                
                                val playUrl = transformGoogleDriveUrl(audio.url)
                                Log.d("AudioPlayer", "Attempting to play: $playUrl")
                                
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
                                            isLoading = false
                                        }
                                        setOnCompletionListener {
                                            currentlyPlayingUrl = null
                                            isLoading = false
                                        }
                                        setOnErrorListener { _, what, extra ->
                                            Log.e("AudioPlayer", "Error: $what, $extra")
                                            val message = when(what) {
                                                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "انقطع الاتصال بالخادم"
                                                else -> "تأكد من أن الملف عام (Public) وصيغته مدعومة"
                                            }
                                            Toast.makeText(context, "خطأ: $message", Toast.LENGTH_LONG).show()
                                            currentlyPlayingUrl = null
                                            isLoading = false
                                            true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AudioPlayer", "Exception: ${e.message}")
                                        Toast.makeText(context, "فشل تحميل الملف: تأكد من الرابط", Toast.LENGTH_SHORT).show()
                                        currentlyPlayingUrl = null
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

fun transformGoogleDriveUrl(url: String): String {
    if (!url.contains("drive.google.com") && !url.contains("docs.google.com")) return url
    
    return try {
        val fileId = when {
            url.contains("/file/d/") -> {
                url.substringAfter("/file/d/").substringBefore("/").substringBefore("?")
            }
            url.contains("id=") -> {
                url.substringAfter("id=").substringBefore("&")
            }
            url.contains("/d/") -> {
                url.substringAfter("/d/").substringBefore("/")
            }
            else -> null
        }
        
        if (fileId != null) {
            // الرابط المباشر للتحميل/التشغيل
            "https://drive.google.com/uc?id=$fileId&export=download"
        } else {
            url
        }
    } catch (e: Exception) {
        url
    }
}

@Composable
fun AudioItem(audio: AudioFile, isPlaying: Boolean, isLoading: Boolean, onPlayPause: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = audio.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            
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
    }
}
