package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BibleBook(val name: String, val chapters: Int)

@Composable
fun BibleScreen(onBookClick: (BibleBook) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("العهد القديم", "العهد الجديد")
    
    // حالة البحث
    var searchQuery by remember { mutableStateOf("") }

    val oldTestament = listOf(
        BibleBook("التكوين", 50), BibleBook("الخروج", 40), BibleBook("اللاويين", 27),
        BibleBook("العدد", 36), BibleBook("التثنية", 34), BibleBook("يشوع", 24),
        BibleBook("القضاة", 21), BibleBook("راعوث", 4), BibleBook("صموئيل الأول", 31),
        BibleBook("صموئيل الثاني", 24), BibleBook("الملوك الأول", 22), BibleBook("الملوك الثاني", 25),
        BibleBook("أخبار الأيام الأول", 29), BibleBook("أخبار الأيام الثاني", 36), BibleBook("عزرا", 10),
        BibleBook("نحميا", 13), BibleBook("أستير", 10), BibleBook("أيوب", 42),
        BibleBook("المزامير", 150), BibleBook("الأمثال", 31), BibleBook("الجامعة", 12),
        BibleBook("نشيد الأنشاد", 8), BibleBook("إشعياء", 66), BibleBook("إرميا", 52),
        BibleBook("مراثي إرميا", 5), BibleBook("حزقيال", 48), BibleBook("دانيال", 12),
        BibleBook("هوشع", 14), BibleBook("يوئيل", 3), BibleBook("عاموس", 9),
        BibleBook("عوبديا", 1), BibleBook("يونان", 4), BibleBook("ميخا", 7),
        BibleBook("ناحوم", 3), BibleBook("حبقوق", 3), BibleBook("صفنيا", 3),
        BibleBook("حجي", 2), BibleBook("زكريا", 14), BibleBook("ملاخي", 4)
    )

    val newTestament = listOf(
        BibleBook("متى", 28), BibleBook("مرقس", 16), BibleBook("لوقا", 24),
        BibleBook("يوحنا", 21), BibleBook("أعمال الرسل", 28), BibleBook("رومية", 16),
        BibleBook("كورنثوس الأولى", 16), BibleBook("كورنثوس الثانية", 13), BibleBook("غلاطية", 6),
        BibleBook("أفسس", 6), BibleBook("فيلبي", 4), BibleBook("كولوسي", 4),
        BibleBook("تسالونيكي الأولى", 5), BibleBook("تسالونيكي الثانية", 3), BibleBook("تيموثاوس الأولى", 6),
        BibleBook("تيموثاوس الثانية", 4), BibleBook("تيطس", 3), BibleBook("فليمون", 1),
        BibleBook("العبرانيين", 13), BibleBook("يعقوب", 5), BibleBook("بطرس الأولى", 5),
        BibleBook("بطرس الثانية", 3), BibleBook("يوحنا الأولى", 5), BibleBook("يوحنا الثانية", 1),
        BibleBook("يوحنا الثالثة", 1), BibleBook("يهوذا", 1), BibleBook("رؤيا يوحنا", 22)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { 
                        selectedTab = index
                        searchQuery = "" 
                    },
                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            placeholder = { Text("ابحث عن سفر...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "مسح")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        val fullList = if (selectedTab == 0) oldTestament else newTestament
        val filteredBooks = fullList.filter { it.name.contains(searchQuery, ignoreCase = true) }

        if (filteredBooks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد أسفار بهذا الاسم")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredBooks) { book ->
                    BookItem(book, onBookClick)
                }
            }
        }
    }
}

@Composable
fun BookItem(book: BibleBook, onBookClick: (BibleBook) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookClick(book) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = book.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = "${book.chapters} أصحاح", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
