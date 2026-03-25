package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChaptersScreen(bookName: String, chapterCount: Int, onChapterClick: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "سفر $bookName - الأصحاحات",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chapterCount) { index ->
                val chapterNumber = index + 1
                ChapterItem(chapterNumber, onChapterClick)
            }
        }
    }
}

@Composable
fun ChapterItem(number: Int, onChapterClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onChapterClick(number) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = "$number", fontWeight = FontWeight.Bold)
        }
    }
}
