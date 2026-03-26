package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaber.ahlamenelasal.data.BibleCommentaryData

data class CommentaryItem(
    val id: String = "",
    val reference: String = "",
    val summary: String = "",
    val fullText: String = "",
    val hasVideo: Boolean = false,
    val isOldTestament: Boolean = true
)

@Composable
fun CommentaryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("العهد القديم", "العهد الجديد")
    
    val oldTestament = BibleCommentaryData.oldTestamentCommentaries
    val newTestament = BibleCommentaryData.newTestamentCommentaries

    val currentList = if (selectedTab == 0) oldTestament else newTestament
    val filteredCommentaries = currentList.filter { 
        it.reference.contains(searchQuery) || it.summary.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "تفسير ${tabs[selectedTab]}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("ابحث عن سفر...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )

            if (filteredCommentaries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "لا توجد نتائج")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCommentaries) { item ->
                        CommentaryCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentaryCard(item: CommentaryItem) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.reference,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("اقرأ التفسير الكامل")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(item.reference, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = item.fullText, style = MaterialTheme.typography.bodyLarge)
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
