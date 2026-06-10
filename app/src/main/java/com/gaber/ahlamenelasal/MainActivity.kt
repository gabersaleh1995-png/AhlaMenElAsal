package com.gaber.ahlamenelasal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gaber.ahlamenelasal.navigation.Screen
import com.gaber.ahlamenelasal.ui.screens.*
import com.gaber.ahlamenelasal.ui.theme.AhlaMenElAsalTheme
import com.gaber.ahlamenelasal.ui.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // طلب إذن الإشعارات
        CoroutineScope(Dispatchers.Main).launch {
            OneSignal.Notifications.requestPermission(true)
        }

        // إضافة مراقب لتغييرات الاشتراك (لضمان حفظ الـ ID فور تولده)
        OneSignal.User.pushSubscription.addObserver(object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                saveOneSignalIdToFirestore()
            }
        })

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()
            
            val fontSizeMultiplier by settingsViewModel.fontSizeMultiplier
            val isDarkMode by settingsViewModel.isDarkMode
            val useSystemTheme by settingsViewModel.useSystemTheme
            val primaryColorIndex by settingsViewModel.primaryColorIndex
            val selectedFont by settingsViewModel.selectedFont
            val customColor = settingsViewModel.availableColors[primaryColorIndex]

            val context = LocalContext.current
            
            LaunchedEffect(Unit) {
                FirebaseMessaging.getInstance().subscribeToTopic("all")
                saveOneSignalIdToFirestore()
            }

            AhlaMenElAsalTheme(
                darkTheme = if (useSystemTheme) androidx.compose.foundation.isSystemInDarkTheme() else isDarkMode,
                customPrimaryColor = customColor,
                fontSizeMultiplier = fontSizeMultiplier,
                appFont = selectedFont
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainScreen(authViewModel, settingsViewModel)
                }
            }
        }
    }

    private fun saveOneSignalIdToFirestore() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val oneSignalId = OneSignal.User.pushSubscription.id
        
        if (!oneSignalId.isNullOrBlank()) {
            val data = mapOf("oneSignalId" to oneSignalId)
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUid)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener { 
                    Log.d("OneSignal_Dev", "OneSignal ID successfully saved: $oneSignalId") 
                }
                .addOnFailureListener { e ->
                    Log.e("OneSignal_Dev", "Failed to save ID: ${e.message}")
                }
        }
    }
}

@Composable
fun MainScreen(authViewModel: AuthViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser
    
    val startDestination = if (currentUser == null) Screen.Login.route else Screen.Home.route

    val items = listOf(
        Screen.Home,
        Screen.Chat,
        Screen.Meetings,
        Screen.Videos,
        Screen.Settings
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val showBottomBar = currentUser != null && items.any { it.route == currentDestination?.route }

                if (showBottomBar) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)) {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        when (screen) {
                                            Screen.Home -> Icons.Filled.Home
                                            Screen.Chat -> Icons.AutoMirrored.Filled.Chat
                                            Screen.Meetings -> Icons.Filled.DateRange
                                            Screen.Videos -> Icons.Filled.PlayArrow
                                            Screen.Settings -> Icons.Filled.Settings
                                            else -> Icons.Filled.Home
                                        },
                                        null
                                    )
                                },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = startDestination,
                Modifier.padding(innerPadding)
            ) {
                composable(Screen.Login.route) { LoginScreen(onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } }, onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }) }
                composable(Screen.SignUp.route) { SignUpScreen(onSignUpSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.SignUp.route) { inclusive = true } } }, onNavigateToLogin = { navController.navigate(Screen.Login.route) }) }
                composable(Screen.Home.route) { HomeScreen(onNavigate = { screen -> navController.navigate(screen.route) }) }
                
                composable(Screen.Library.route) { LibraryScreen() }
                composable(Screen.AudioLibrary.route) { AudioLibraryScreen() }
                composable(Screen.Gallery.route) { GalleryScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Topics.route) { TopicsScreen(onBack = { navController.popBackStack() }) }

                composable(Screen.Chat.route) { ChatSelectionScreen(onNavigateToGroup = { navController.navigate(Screen.GroupChat.route) }, onNavigateToAdmin = { navController.navigate(Screen.AdminChat.createRoute(currentUser?.uid ?: "unknown", currentUser?.displayName ?: "أنا")) }) }
                composable(Screen.GroupChat.route) { ChatScreen(chatId = "group_all", title = "دردشة الجماعة", onBack = { navController.popBackStack() }) }
                composable(route = Screen.AdminChat.route, arguments = listOf(navArgument("userId") { type = NavType.StringType }, navArgument("userName") { type = NavType.StringType })) { backStackEntry -> ChatScreen(chatId = "admin_${backStackEntry.arguments?.getString("userId")}", title = backStackEntry.arguments?.getString("userName") ?: "دردشة", onBack = { navController.popBackStack() }) }

                composable(Screen.Meetings.route) { MeetingsScreen() }
                composable(Screen.Videos.route) { VideosScreen() }
                composable(Screen.Settings.route) { SettingsScreen(settingsViewModel, authViewModel, onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }) }
                composable(Screen.WeeklyQuestion.route) { WeeklyQuestionScreen() }
                
                composable(route = Screen.AdminAnswers.route, arguments = listOf(navArgument("questionId") { type = NavType.StringType })) { backStackEntry -> AdminAnswersScreen(questionId = backStackEntry.arguments?.getString("questionId") ?: "", onContactUser = { userId, userName -> navController.navigate(Screen.AdminChat.createRoute(userId, userName)) }) }
                composable(Screen.Admin.route) { AdminScreen(onNavigate = { route -> navController.navigate(route) }) }
                composable(Screen.AdminPrivateChats.route) { AdminPrivateChatsScreen(onChatClick = { userId, userName -> navController.navigate(Screen.AdminChat.createRoute(userId, userName)) }) }
            }
        }
    }
}
