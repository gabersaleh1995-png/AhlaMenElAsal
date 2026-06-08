package com.gaber.ahlamenelasal.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.gaber.ahlamenelasal.data.model.VideoItem
import com.gaber.ahlamenelasal.ui.viewmodel.VideosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(videosViewModel: VideosViewModel = viewModel()) {
    val videos = videosViewModel.videos
    val context = LocalContext.current
    
    // حالة لتتبع الفيديو المختار للتشغيل الداخلي
    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedVideoTitle by remember { mutableStateOf("") }

    if (selectedVideoUrl != null) {
        // مشغل الفيديو الداخلي
        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            TopAppBar(
                title = { Text(selectedVideoTitle, color = Color.White, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { selectedVideoUrl = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
            
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                InternalVideoPlayer(selectedVideoUrl!!)
            }
        }
    } else {
        // قائمة الفيديوهات
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "مكتبة الفيديوهات التعليمية",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (videos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(videos) { video ->
                        VideoCard(
                            video = video,
                            onPlay = {
                                if (video.url.contains("cloudinary.com") || video.url.endsWith(".mp4")) {
                                    // تشغيل داخلي للفيديوهات المرفوعة
                                    selectedVideoUrl = video.url
                                    selectedVideoTitle = video.title
                                } else {
                                    // فتح خارجي لليوتيوب وفيسبوك
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                            },
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.url}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الفيديو عبر:"))
                            },
                            onDownload = {
                                downloadFile(context, video.url, video.title, "mp4")
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun downloadFile(context: Context, url: String, title: String, extension: String) {
    try {
        val downloadUrl = if (url.contains("drive.google.com")) {
            transformGoogleDriveUrl(url)
        } else {
            url
        }
        
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(title)
            .setDescription("جاري تحميل الملف...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$title.$extension")
        
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "بدأ التحميل...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "خطأ في التحميل: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@OptIn(UnstableApi::class)
@Composable
fun InternalVideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    
    // إنشاء ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // التخلص من اللاعب عند الخروج من الشاشة
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        modifier = Modifier.fillMaxWidth().aspectRatio(16/9f)
    )
}

@Composable
fun VideoCard(video: VideoItem, onPlay: () -> Unit, onShare: () -> Unit, onDownload: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
                
                if (video.category.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = video.category,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val isInternal = video.url.contains("cloudinary.com") || video.url.endsWith(".mp4")
                    Text(
                        text = if (isInternal) "مشاهدة داخل التطبيق" else "مشاهدة عبر الرابط الخارجي",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isInternal) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }
                
                Row {
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "تحميل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
