package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaber.ahlamenelasal.data.AgbeyaRepository

@Composable
fun AgbeyaContentScreen(initialPrayerName: String) {
    val prayers = listOf("باكر", "الثالثة", "السادسة", "التاسعة", "الغروب", "النوم", "الستار", "نصف الليل")
    val initialPage = prayers.indexOf(initialPrayerName).coerceAtLeast(0)
    val context = LocalContext.current
    
    // إعداد السحب الجانبي بين الصلوات
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { prayers.size })

    Column(modifier = Modifier.fillMaxSize()) {
        // العنوان العلوي يتغير مع السحب
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Text(
                text = "صلاة ${prayers[pagerState.currentPage]}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val prayerName = prayers[page]
            val prayerText = remember(prayerName) {
                AgbeyaRepository.getPrayerContent(context, prayerName)
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = prayerText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
