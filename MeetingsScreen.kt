package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.data.model.Meeting
import com.gaber.ahlamenelasal.ui.viewmodel.MeetingsViewModel

@Composable
fun MeetingsScreen(meetingsViewModel: MeetingsViewModel = viewModel()) {
    val meetings = meetingsViewModel.meetings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "مواعيد الاجتماعات القادمة",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (meetings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد اجتماعات مجدولة حالياً", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(meetings) { meeting ->
                    MeetingCard(meeting)
                }
            }
        }
    }
}

@Composable
fun MeetingCard(meeting: Meeting) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${meeting.date} - ${meeting.time}", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = meeting.location, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = meeting.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { /* تفعيل التذكير */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("تذكيري")
            }
        }
    }
}
