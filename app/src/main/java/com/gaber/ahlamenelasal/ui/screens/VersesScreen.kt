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
import androidx.compose.ui.graphics.Color
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
    // مراقبة حالة جاهزية البيانات
    val isDataReady by bibleViewModel.isDataReady.collectAsState()

    // جلب عدد الأصحاحات من الـ ViewModel مباشرة
    val totalChapters = if (isDataReady) {
        bibleViewModel.getChapterCount(bookName)
    } else {
        chapterNumber // استخدام الرقم الممرر كحد أدنى مؤقتاً
    }

    // إعداد الـ Pager للسحب الجانبي
    val pagerState = rememberPagerState(
        initialPage = (chapterNumber - 1).coerceIn(0, (totalChapters - 1).coerceAtLeast(0)),
        pageCount = { totalChapters.coerceAtLeast(1) }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f), // جعل العنوان شبه شفاف
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

        if (!isDataReady) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "جاري تحميل الكتاب المقدس...", color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val currentChapter = page + 1
                // جلب الآيات باستخدام الطريقة السريعة O(1)
                val verses = remember(bookName, currentChapter, isDataReady) {
                    bibleViewModel.getVerses(bookName, currentChapter)
                }

                if (verses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد آيات لهذا الأصحاح", color = MaterialTheme.colorScheme.error)
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
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface // التأكد من لون النص ليظهر فوق الخلفية
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}
