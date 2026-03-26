package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.data.BibleVerseJson
import com.gaber.ahlamenelasal.ui.viewmodel.BibleViewModel

@Composable
fun VersesScreen(
    bookName: String, 
    chapterNumber: Int,
    bibleViewModel: BibleViewModel = viewModel()
) {
    val bibleData by bibleViewModel.bibleData
    
    // جلب بيانات السفر لمعرفة عدد الأصحاحات
    val bookData = remember(bookName, bibleData) {
        bibleData.find { it.book == bookName }
    }
    
    val totalChapters = remember(bookData) {
        val maxChapter = bookData?.verses?.maxOfOrNull { it.chapter } ?: chapterNumber
        maxOf(maxChapter, chapterNumber)
    }

    // إعداد الـ Pager للسحب الجانبي
    val pagerState = rememberPagerState(
        initialPage = (chapterNumber - 1).coerceIn(0, (totalChapters - 1).coerceAtLeast(0)),
        pageCount = { totalChapters }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // عنوان الأصحاح الحالي (يتغير عند السحب)
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$bookName - الأصحاح ${pagerState.currentPage + 1}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val currentChapter = page + 1
            val verses = remember(bookName, currentChapter, bibleData) {
                bibleViewModel.getVerses(bookName, currentChapter)
            }

            if (verses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "جاري تحميل الآيات...", color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(verses) { verse ->
                        VerseItem(verse)
                    }
                }
            }
        }
    }
}

@Composable
fun VerseItem(verse: BibleVerseJson) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${verse.verse}.",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = verse.text,
                fontSize = 18.sp,
                lineHeight = 28.sp
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
