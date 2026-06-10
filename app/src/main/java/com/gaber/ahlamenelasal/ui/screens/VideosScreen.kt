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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.gaber.ahlamenelasal.ui.theme.*
import com.gaber.ahlamenelasal.ui.viewmodel.VideosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(videosViewModel: VideosViewModel = viewModel()) {
    val videos = videosViewModel.videos
    val context = LocalContext.current
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    var selectedTitle by remember { mutableStateOf("") }

    if (selectedUrl != null) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            TopAppBar(
                title = { Text(selectedTitle, color = Color.White, style = MaterialTheme.typography.titleSmall) },
                navigationIcon = { IconButton(onClick = { selectedUrl = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                InternalVideoPlayer(selectedUrl!!)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text("مكتبة الفيديوهات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("فيديوهات تعليمية ودينية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

            if (videos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎬", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        CircularProgressIndicator(color = MidPurple)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(videos) { video ->
                        VideoCard(
                            video = video,
                            onPlay = {
                                if (video.url.contains("cloudinary.com") || video.url.endsWith(".mp4")) {
                                    selectedUrl = video.url; selectedTitle = video.title
                                } else {
                                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.url))) } catch (_: Exception) {}
                                }
                            },
                            onShare = {
                                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.url}") }, "مشاركة الفيديو"))
                            },
                            onDownload = { downloadFile(context, video.url, video.title, "mp4") }
                        )
                    }
                }
            }
        }
    }
}

private fun downloadFile(context: Context, url: String, title: String, ext: String) {
    try {
        val dlUrl = if (url.contains("drive.google.com")) transformGoogleDriveUrl(url) else url
        val req = DownloadManager.Request(Uri.parse(dlUrl)).setTitle(title).setDescription("جاري التحميل...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$title.$ext")
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
        Toast.makeText(context, "بدأ التحميل...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) { Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_LONG).show() }
}

@OptIn(UnstableApi::class)
@Composable
fun InternalVideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl)); prepare(); playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    AndroidView(
        factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = true; setBackgroundColor(android.graphics.Color.BLACK) } },
        modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f)
    )
}

@Composable
fun VideoCard(video: VideoItem, onPlay: () -> Unit, onShare: () -> Unit, onDownload: () -> Unit) {
    val isInternal = video.url.contains("cloudinary.com") || video.url.endsWith(".mp4")
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onPlay() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Thumbnail area
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1A1A22), Color(0xFF2D1B4E)))),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(50)).background(MidPurple.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                if (video.category.isNotBlank()) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                        color = HoneyGold, shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(video.category, color = DeepPurple, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                    color = if (isInternal) SuccessGreen.copy(alpha = 0.9f) else MidPurple.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isInternal) "داخلي" else "خارجي",
                        color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(video.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onDownload) { Icon(Icons.Default.Download, "تحميل", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onShare) { Icon(Icons.Default.Share, "مشاركة", tint = MaterialTheme.colorScheme.secondary) }
            }
        }
    }
}
