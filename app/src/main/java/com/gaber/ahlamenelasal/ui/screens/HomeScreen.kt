package com.gaber.ahlamenelasal.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaber.ahlamenelasal.navigation.Screen
import com.gaber.ahlamenelasal.ui.theme.*
import com.gaber.ahlamenelasal.ui.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore

data class HomeOption(
    val title: String,
    val icon: ImageVector,
    val gradient: List<Color>,
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
    var dailyReference by remember { mutableStateOf("مزمور ١١٩:١٠٣") }
    val isDark = MaterialTheme.colorScheme.background == DarkBg

    LaunchedEffect(Unit) {
        db.collection("app_data").document("daily_verse").addSnapshotListener { snap, _ ->
            if (snap != null && snap.exists()) {
                dailyVerse = snap.getString("text") ?: dailyVerse
                dailyReference = snap.getString("reference") ?: ""
            }
        }
    }

    val options = listOf(
        HomeOption("الموضوعات",       Icons.Default.Topic,        listOf(HoneyGold,    HoneyAmber),    Screen.Topics),
        HomeOption("المكتبة PDF",     Icons.Default.LibraryBooks, listOf(Color(0xFF8B5CF6), MidPurple), Screen.Library),
        HomeOption("تسجيلات صوتية",  Icons.Default.Mic,          listOf(Color(0xFF06B6D4), Color(0xFF0891B2)), Screen.AudioLibrary),
        HomeOption("معرض الصور",     Icons.Default.Image,        listOf(HoneyAmber,   Color(0xFFEA580C)), Screen.Gallery),
        HomeOption("المواعيد",       Icons.Default.Event,        listOf(SuccessGreen, Color(0xFF16A34A)), Screen.Meetings),
        HomeOption("سؤال الأسبوع",  Icons.Default.Quiz,         listOf(FuchsiaAccent, Color(0xFF9333EA)), Screen.WeeklyQuestion),
        HomeOption("الفيديوهات",     Icons.Default.PlayCircle,   listOf(ErrorRed,     Color(0xFFDC2626)), Screen.Videos),
        HomeOption("الدردشة",        Icons.Default.Chat,         listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)), Screen.Chat)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ─── Header ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .pointerInput(isAdmin) {
                    detectTapGestures(onLongPress = { if (isAdmin) onNavigate(Screen.Admin) })
                }
            ) {
                Text(
                    text = "أحلى من العسل 🍯",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "مرحباً بك في مجتمع الإيمان",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isAdmin) {
                IconButton(
                    onClick = { onNavigate(Screen.Admin) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = "لوحة التحكم",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Daily Verse Card ─────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(DeepPurple, Color(0xFF2D1B4E), Color(0xFF1A1040))
                        )
                    )
                    .padding(24.dp)
            ) {
                // Decorative glow
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-20).dp)
                        .background(
                            Brush.radialGradient(
                                listOf(HoneyGold.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✦  آية اليوم  ✦",
                        style = MaterialTheme.typography.labelMedium,
                        color = HoneyGold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = dailyVerse,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dailyReference.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = HoneyGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = dailyReference,
                                style = MaterialTheme.typography.labelMedium,
                                color = HoneyGold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "الأقسام",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ─── Grid ─────────────────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(options) { option ->
                HomeCard(option, onNavigate)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HomeCard(option: HomeOption, onNavigate: (Screen) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .clickable { onNavigate(option.screen) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(option.gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}
