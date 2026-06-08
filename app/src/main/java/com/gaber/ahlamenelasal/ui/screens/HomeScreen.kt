package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.navigation.Screen
import com.gaber.ahlamenelasal.ui.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore

data class HomeOption(
    val title: String,
    val icon: ImageVector,
    val colors: List<Color>,
    val screen: Screen
)

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val isAdmin by authViewModel.isAdmin
    val db = FirebaseFirestore.getInstance()
    var dailyVerse by remember { mutableStateOf("كلامك أحلى من العسل في فمي") }
    var dailyReference by remember { mutableStateOf("مزمور 119:103") }

    LaunchedEffect(Unit) {
        db.collection("app_data").document("daily_verse").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                dailyVerse = snapshot.getString("text") ?: dailyVerse
                dailyReference = snapshot.getString("reference") ?: ""
            }
        }
    }

    val options = listOf(
        HomeOption("الموضوعات", Icons.Default.Topic, listOf(Color(0xFFFFA000), Color(0xFFFFC107)), Screen.Topics),
        HomeOption("المكتبة PDF", Icons.Default.LibraryBooks, listOf(Color(0xFF795548), Color(0xFFA1887F)), Screen.Library),
        HomeOption("تسجيلات صوتية", Icons.Default.Mic, listOf(Color(0xFF8D6E63), Color(0xFFBCAAA4)), Screen.AudioLibrary),
        HomeOption("معرض الصور", Icons.Default.Image, listOf(Color(0xFFFFB300), Color(0xFFFFD54F)), Screen.Gallery),
        HomeOption("المواعيد", Icons.Default.Event, listOf(Color(0xFFE65100), Color(0xFFFF9800)), Screen.Meetings),
        HomeOption("سؤال الاجتماع", Icons.Default.Quiz, listOf(Color(0xFFFFD600), Color(0xFFFFFF00)), Screen.WeeklyQuestion),
        HomeOption("الفيديوهات", Icons.Default.PlayCircle, listOf(Color(0xFFBF360C), Color(0xFFF4511E)), Screen.Videos),
        HomeOption("الدردشة", Icons.Default.Chat, listOf(Color(0xFFFF6F00), Color(0xFFFFB300)), Screen.Chat)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "فإن الروحي أحلي من العسل ,وميراثي الذ من شهد العسل",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.pointerInput(isAdmin) {
                            detectTapGestures(onLongPress = { if (isAdmin) onNavigate(Screen.Admin) })
                        }
                    )
                    Text(
                        text = "(سي 27:24)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (isAdmin) {
                    IconButton(
                        onClick = { onNavigate(Screen.Admin) },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Daily Verse
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(
                            MaterialTheme.colorScheme.primary, 
                            MaterialTheme.colorScheme.secondary
                        )))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dailyVerse,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (dailyReference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dailyReference, 
                                style = MaterialTheme.typography.labelMedium, 
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Grid Section
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(options) { option ->
                    HomeCard(option, onNavigate)
                }
            }
        }
    }
}

@Composable
fun HomeCard(option: HomeOption, onNavigate: (Screen) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clickable { onNavigate(option.screen) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(option.colors))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = option.icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
