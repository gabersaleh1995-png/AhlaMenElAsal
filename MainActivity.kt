package com.gaber.ahlamenelasal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()
            
            val fontSizeMultiplier by settingsViewModel.fontSizeMultiplier
            val isDarkMode by settingsViewModel.isDarkMode
            val useSystemTheme by settingsViewModel.useSystemTheme
            val primaryColorIndex by settingsViewModel.primaryColorIndex
            val selectedFont by settingsViewModel.selectedFont
            val customColor = settingsViewModel.availableColors[primaryColorIndex]

            // طلب إذن الإشعارات لأندرويد 13+
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
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
}

@Composable
fun MainScreen(authViewModel: AuthViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser
    
    val startDestination = if (currentUser == null) Screen.Login.route else Screen.Home.route

    val items = listOf(
        Screen.Home,
        Screen.Bible,
        Screen.Chat,
        Screen.Meetings,
        Screen.Videos,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            val showBottomBar = currentUser != null && items.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    when (screen) {
                                        Screen.Home -> Icons.Filled.Home
                                        Screen.Bible -> Icons.Filled.MenuBook
                                        Screen.Chat -> Icons.AutoMirrored.Filled.Chat
                                        Screen.Meetings -> Icons.Filled.DateRange
                                        Screen.Videos -> Icons.Filled.PlayArrow
                                        Screen.Settings -> Icons.Filled.Settings
                                        else -> Icons.Filled.Home
                                    },
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
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
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { 
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Home.route) { 
                HomeScreen(onNavigate = { screen -> navController.navigate(screen.route) }) 
            }
            composable(Screen.Bible.route) { 
                BibleScreen(onBookClick = { book ->
                    navController.navigate(Screen.BibleChapters.createRoute(book.name, book.chapters))
                }) 
            }
            composable(
                route = Screen.BibleChapters.route,
                arguments = listOf(
                    navArgument("bookName") { type = NavType.StringType },
                    navArgument("chapterCount") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val bookName = backStackEntry.arguments?.getString("bookName") ?: ""
                val chapterCount = backStackEntry.arguments?.getInt("chapterCount") ?: 0
                ChaptersScreen(bookName, chapterCount, onChapterClick = { chapterNumber ->
                    navController.navigate(Screen.BibleVerses.createRoute(bookName, chapterNumber))
                })
            }
            composable(
                route = Screen.BibleVerses.route,
                arguments = listOf(
                    navArgument("bookName") { type = NavType.StringType },
                    navArgument("chapterNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val bookName = backStackEntry.arguments?.getString("bookName") ?: ""
                val chapterNumber = backStackEntry.arguments?.getInt("chapterNumber") ?: 1
                VersesScreen(bookName, chapterNumber)
            }
            
            // شاشة المكتبة PDF
            composable(Screen.Library.route) {
                LibraryScreen()
            }

            // شاشة المكتبة الصوتية
            composable(Screen.AudioLibrary.route) {
                AudioLibraryScreen()
            }
            
            // الأجبية
            composable(Screen.Agbeya.route) {
                AgbeyaScreen(onPrayerClick = { prayerName ->
                    navController.navigate(Screen.AgbeyaContent.createRoute(prayerName))
                })
            }
            composable(
                route = Screen.AgbeyaContent.route,
                arguments = listOf(navArgument("prayerName") { type = NavType.StringType })
            ) { backStackEntry ->
                val prayerName = backStackEntry.arguments?.getString("prayerName") ?: ""
                AgbeyaContentScreen(prayerName)
            }

            // الدردشة
            composable(Screen.Chat.route) { 
                ChatSelectionScreen(
                    onNavigateToGroup = { navController.navigate(Screen.GroupChat.route) },
                    onNavigateToAdmin = { 
                        val uid = currentUser?.uid ?: "unknown"
                        val name = currentUser?.displayName ?: "أنا"
                        navController.navigate(Screen.AdminChat.createRoute(uid, name))
                    }
                )
            }
            composable(Screen.GroupChat.route) { 
                ChatScreen(chatId = "group_all", title = "دردشة الجماعة") 
            }
            composable(
                route = Screen.AdminChat.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("userName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val userName = backStackEntry.arguments?.getString("userName") ?: "دردشة"
                ChatScreen(chatId = "admin_$userId", title = userName)
            }

            composable(Screen.Meetings.route) { MeetingsScreen() }
            composable(Screen.Videos.route) { VideosScreen() }
            composable(Screen.Commentary.route) { CommentaryScreen() }
            
            // شاشة الإعدادات
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // سؤال الأسبوع
            composable(Screen.WeeklyQuestion.route) { WeeklyQuestionScreen() }
            
            // مراجعة إجابات الأعضاء (للأدمن)
            composable(
                route = Screen.AdminAnswers.route,
                arguments = listOf(navArgument("questionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
                AdminAnswersScreen(
                    questionId = questionId,
                    onContactUser = { userId, userName ->
                        navController.navigate(Screen.AdminChat.createRoute(userId, userName))
                    }
                )
            }

            // لوحة الأدمن
            composable(Screen.Admin.route) { 
                AdminScreen(onNavigate = { route -> navController.navigate(route) }) 
            }
            composable(Screen.AdminPrivateChats.route) { 
                AdminPrivateChatsScreen(onChatClick = { userId, userName ->
                    navController.navigate(Screen.AdminChat.createRoute(userId, userName))
                })
            }
        }
    }
}
