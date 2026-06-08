package com.gaber.ahlamenelasal.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class LibraryItem(
    val id: String, 
    val title: String, 
    val url: String, 
    val folder: String = "",
    val subFolder: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    val db = FirebaseFirestore.getInstance()
    var libraryItems by remember { mutableStateOf<List<LibraryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedItem by remember { mutableStateOf<LibraryItem?>(null) }
    
    // حالات المجلدات المفتوحة
    var currentFolder by remember { mutableStateOf<String?>(null) }
    var currentSubFolder by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    fun getProcessedUrl(url: String): String {
        val cleanUrl = url.trim()
        if (cleanUrl.contains("drive.google.com")) {
            val fileId = when {
                cleanUrl.contains("/d/") -> cleanUrl.split("/d/").getOrNull(1)?.split("/")?.getOrNull(0)
                cleanUrl.contains("id=") -> cleanUrl.split("id=").getOrNull(1)?.split("&")?.getOrNull(0)
                else -> null
            }
            if (fileId != null) return "https://drive.google.com/uc?export=download&id=$fileId"
        }
        if (!cleanUrl.lowercase().endsWith(".pdf")) return "$cleanUrl.pdf"
        return cleanUrl
    }

    if (selectedItem != null) {
        val finalFileUrl = getProcessedUrl(selectedItem!!.url)
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(selectedItem!!.title, maxLines = 1) },
                navigationIcon = { IconButton(onClick = { selectedItem = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } },
                actions = {
                    IconButton(onClick = {
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalFileUrl))) }
                        catch (e: Exception) { Toast.makeText(context, "لا يمكن فتح الرابط", Toast.LENGTH_SHORT).show() }
                    }) { Icon(Icons.Default.OpenInNew, "فتح") }
                    IconButton(onClick = {
                        try {
                            val cleanTitle = selectedItem!!.title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                            val request = DownloadManager.Request(Uri.parse(finalFileUrl))
                                .setTitle(selectedItem!!.title)
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$cleanTitle.pdf")
                            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                            Toast.makeText(context, "بدأ التحميل..", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) { Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_LONG).show() }
                    }) { Icon(Icons.Default.Download, "تحميل") }
                }
            )
            InternalFileViewer(finalFileUrl)
        }
    } else {
        LaunchedEffect(Unit) {
            db.collection("bible_pdfs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(context, "فشل تحميل البيانات: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        libraryItems = snapshot.documents.map { doc ->
                            LibraryItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "بدون عنوان",
                                url = doc.getString("url") ?: "",
                                folder = doc.getString("folder") ?: "",
                                subFolder = doc.getString("subFolder") ?: ""
                            )
                        }
                    }
                    isLoading = false
                }
        }

        val filteredItems = libraryItems.filter { it.title.contains(searchQuery, ignoreCase = true) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentFolder != null) {
                    IconButton(onClick = {
                        if (currentSubFolder != null) currentSubFolder = null
                        else currentFolder = null
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
                Text(
                    text = when {
                        currentSubFolder != null -> currentSubFolder!!
                        currentFolder != null -> currentFolder!!
                        else -> "المكتبة الإلكترونية (PDF)"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث عن كتاب...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) } },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (searchQuery.isEmpty()) {
                        when {
                            currentFolder == null -> {
                                val folders = filteredItems.map { it.folder }.distinct().filter { it.isNotEmpty() }
                                items(folders) { folder ->
                                    FolderCard(title = folder, onClick = { currentFolder = folder })
                                }
                                items(filteredItems.filter { it.folder.isEmpty() }) { item ->
                                    FileCard(item, onClick = { selectedItem = item })
                                }
                            }
                            currentSubFolder == null -> {
                                val subFolders = filteredItems.filter { it.folder == currentFolder }
                                    .map { it.subFolder }.distinct().filter { it.isNotEmpty() }
                                items(subFolders) { sub ->
                                    FolderCard(title = sub, onClick = { currentSubFolder = sub })
                                }
                                items(filteredItems.filter { it.folder == currentFolder && it.subFolder.isEmpty() }) { item ->
                                    FileCard(item, onClick = { selectedItem = item })
                                }
                            }
                            else -> {
                                items(filteredItems.filter { it.folder == currentFolder && it.subFolder == currentSubFolder }) { item ->
                                    FileCard(item, onClick = { selectedItem = item })
                                }
                            }
                        }
                    } else {
                        items(filteredItems) { item -> FileCard(item, onClick = { selectedItem = item }) }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun FileCard(item: LibraryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(item.title, fontWeight = FontWeight.Bold)
                Text(
                    text = "PDF Document",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun InternalFileViewer(fileUrl: String) {
    val viewerUrl = "https://docs.google.com/gview?embedded=true&url=$fileUrl"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                loadUrl(viewerUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
