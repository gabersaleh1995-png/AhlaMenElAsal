package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AgbeyaScreen(onPrayerClick: (String) -> Unit) {
    val prayers = listOf(
        "باكر",
        "الثالثة",
        "السادسة",
        "التاسعة",
        "الغروب",
        "النوم",
        "الستار (للرهبان)",
        "نصف الليل"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "صلوات الأجبية",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(prayers) { prayer ->
                PrayerItem(prayer, onPrayerClick)
            }
        }
    }
}

@Composable
fun PrayerItem(name: String, onPrayerClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPrayerClick(name) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(text = "صلاة $name", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}
