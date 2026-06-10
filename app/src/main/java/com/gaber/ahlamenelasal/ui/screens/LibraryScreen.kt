package com.gaber.ahlamenelasal.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.gaber.ahlamenelasal.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class LibraryItem(val id: String, val title: String, val url: String, val folder: String = "", val subFolder: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    val db = FirebaseFirestore.getInstance()
    var items by remember { mutableStateOf<List<LibraryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var search by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<LibraryItem?>(null) }
    var currentFolder by remember { mutableStateOf<String?>(null) }
    var currentSubFolder by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun processUrl(url: String): String {
        val clean = url.trim()
        if (clean.contains("drive.google.com")) {
            val id = when {
                clean.contains("/d/") -> clean.split("/d/").getOrNull(1)?.split("/")?.getOrNull(0)
                clean.contains("id=") -> clean.split("id=").getOrNull(1)?.split("&")?.getOrNull(0)
                else -> null
            }
            if (id != null) return "https://drive.google.com/uc?export=download&id=$id"
        }
        return if (!clean.lowercase().endsWith(".pdf")) "$clean.pdf" else clean
    }

    if (selectedItem != null) {
        val finalUrl = processUrl(selectedItem!!.url)
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(selectedItem!!.title, maxLines = 1) },
                navigationIcon = { IconButton(onClick = { selectedItem = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } },
                actions = {
                    IconButton(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))) } catch (_: Exception) { Toast.makeText(context, "لا يمكن فتح الرابط", Toast.LENGTH_SHORT).show() } }) { Icon(Icons.Default.OpenInNew, "فتح") }
                    IconButton(onClick = {
                        try {
                            val req = DownloadManager.Request(Uri.parse(finalUrl)).setTitle(selectedItem!!.title)
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${selectedItem!!.title}.pdf")
                            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
                            Toast.makeText(context, "بدأ التحميل", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) { Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_LONG).show() }
                    }) { Icon(Icons.Default.Download, "تحميل") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
            InternalFileViewer(finalUrl)
        }
        return
    }

    LaunchedEffect(Unit) {
        db.collection("bible_pdfs").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { Toast.makeText(context, "فشل تحميل البيانات", Toast.LENGTH_LONG).show(); isLoading = false; return@addSnapshotListener }
                if (snap != null) items = snap.documents.map { LibraryItem(it.id, it.getString("title") ?: "بدون عنوان", it.getString("url") ?: "", it.getString("folder") ?: "", it.getString("subFolder") ?: "") }
                isLoading = false
            }
    }

    val filtered = items.filter { it.title.contains(search, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (currentFolder != null) {
                IconButton(onClick = { if (currentSubFolder != null) currentSubFolder = null else currentFolder = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Column {
                Text(when { currentSubFolder != null -> currentSubFolder!!; currentFolder != null -> currentFolder!!; else -> "المكتبة الإلكترونية" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("كتب ومراجع PDF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = search, onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(), placeholder = { Text("ابحث عن كتاب...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MidPurple) },
            trailingIcon = { if (search.isNotEmpty()) IconButton(onClick = { search = "" }) { Icon(Icons.Default.Close, null) } },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MidPurple, unfocusedBorderColor = BorderLight, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
        )
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MidPurple) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (search.isEmpty()) {
                    when {
                        currentFolder == null -> {
                            val folders = filtered.map { it.folder }.distinct().filter { it.isNotEmpty() }
                            items(folders) { FolderCard(it) { currentFolder = it } }
                            items(filtered.filter { it.folder.isEmpty() }) { FileCard(it) { selectedItem = it } }
                        }
                        currentSubFolder == null -> {
                            val subs = filtered.filter { it.folder == currentFolder }.map { it.subFolder }.distinct().filter { it.isNotEmpty() }
                            items(subs) { FolderCard(it) { currentSubFolder = it } }
                            items(filtered.filter { it.folder == currentFolder && it.subFolder.isEmpty() }) { FileCard(it) { selectedItem = it } }
                        }
                        else -> items(filtered.filter { it.folder == currentFolder && it.subFolder == currentSubFolder }) { FileCard(it) { selectedItem = it } }
                    }
                } else {
                    items(filtered) { FileCard(it) { selectedItem = it } }
                }
            }
        }
    }
}

@Composable
fun FolderCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(HoneyGold, HoneyAmber))), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Folder, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FileCard(item: LibraryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFEEEE)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PictureAsPdf, null, tint = ErrorRed, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("PDF Document", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun InternalFileViewer(fileUrl: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                loadUrl("https://docs.google.com/gview?embedded=true&url=$fileUrl")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
