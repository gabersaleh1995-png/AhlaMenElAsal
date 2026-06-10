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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.gaber.ahlamenelasal.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class ImageItem(val id: String, val title: String, val url: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var images by remember { mutableStateOf<List<ImageItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<ImageItem?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("images").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null)
                    images = snap.documents.map { ImageItem(it.id, it.getString("title") ?: "", it.getString("url") ?: "") }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معرض الصور", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MidPurple) }
            images.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🖼️", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                    Text("لا توجد صور حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(images) { img ->
                    Card(
                        modifier = Modifier.aspectRatio(1f).clickable { selected = img },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        AsyncImage(model = img.url, contentDescription = img.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
            }
        }
    }

    selected?.let { img ->
        Dialog(onDismissRequest = { selected = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(model = img.url, contentDescription = img.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopEnd).padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        listOf(
                            Triple(Icons.Default.OpenInNew, "فتح") {
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(img.url))) }
                                catch (_: Exception) { Toast.makeText(context, "لا يمكن فتح الرابط", Toast.LENGTH_SHORT).show() }
                            },
                            Triple(Icons.Default.Download, "تحميل") {
                                try {
                                    val req = DownloadManager.Request(Uri.parse(img.url)).setTitle(img.title)
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "${img.title}.jpg")
                                    (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
                                    Toast.makeText(context, "بدأ التحميل...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) { Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_LONG).show() }
                            },
                            Triple(Icons.Default.Close, "إغلاق") { selected = null }
                        ).forEach { (icon, desc, action) ->
                            IconButton(
                                onClick = action,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.6f))
                            ) { Icon(icon, desc, tint = Color.White, modifier = Modifier.size(20.dp)) }
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    if (img.title.isNotBlank()) {
                        Text(
                            img.title,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
                                .clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(0.6f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
