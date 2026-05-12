package com.example.sensai.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.sensai.data.local.TokenManager
import com.example.sensai.ui.auth.AuthScreen
import com.example.sensai.ui.screens.detail.AnimeDetailScreen
import com.example.sensai.ui.screens.home.HomeScreen
import com.example.sensai.ui.screens.search.SearchScreen
import com.example.sensai.ui.chat.DiscussionScreen
import com.example.sensai.ui.sensei.SenseiChatScreen
import com.example.sensai.ui.screens.quiz.QuizScreen
import com.example.sensai.ui.screens.watchlist.WatchlistScreen
import com.example.sensai.ui.screens.quizhistory.QuizHistoryScreen

// All routes in one place — easy to maintain and extend
private object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val SEARCH = "search"
    const val SENSEI = "sensei"
    const val QUIZ = "quiz"
    const val WATCHLIST = "watchlist"
    const val PROFILE = "profile"
    const val QUIZ_HISTORY = "quiz_history"
    const val DETAIL = "detail/{animeId}"
    const val CHAT = "chat?animeId={animeId}&animeName={animeName}"
    const val USER_PROFILE = "user_profile/{userId}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val token by tokenManager.accessToken.collectAsState(initial = null)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val topLevelRoutes = listOf(Routes.HOME, Routes.SEARCH, Routes.SENSEI, Routes.QUIZ, Routes.WATCHLIST)
    val showBottomNav = currentDestination?.route in topLevelRoutes

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text("SensAI Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    icon = { Icon(Icons.Default.Face, contentDescription = "Profile") },
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.PROFILE)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Leaderboard") },
                    selected = false,
                    icon = { Icon(Icons.Default.Star, contentDescription = "Leaderboard") },
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("leaderboard")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.HOME } == true,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.SEARCH } == true,
                        onClick = {
                            navController.navigate(Routes.SEARCH) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Face, contentDescription = "Sensei") },
                        label = { Text("Sensei") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.SENSEI } == true,
                        onClick = {
                            navController.navigate(Routes.SENSEI) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = "Quiz") },
                        label = { Text("Quiz") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.QUIZ } == true,
                        onClick = {
                            navController.navigate(Routes.QUIZ) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Watchlist") },
                        label = { Text("My List") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.WATCHLIST } == true,
                        onClick = {
                            navController.navigate(Routes.WATCHLIST) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (token == null) Routes.AUTH else Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Auth ────────────────────────────────────────────────────────────
            composable(Routes.AUTH) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }

            // ── Home ────────────────────────────────────────────────────────────
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToDetail = { animeId -> navController.navigate("detail/$animeId") },
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }

            // ── Profile ─────────────────────────────────────────────────────────
            composable(Routes.PROFILE) {
                com.example.sensai.ui.screens.profile.ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) // Clear the entire back-stack on logout
                        }
                    }
                )
            }

            // ── Search ──────────────────────────────────────────────────────────
            composable(Routes.SEARCH) {
                SearchScreen(
                    onNavigateToDetail = { animeId -> navController.navigate("detail/$animeId") }
                )
            }

            // ── Sensei (AI Chat) ─────────────────────────────────────────────────
            composable(Routes.SENSEI) {
                SenseiChatScreen()
            }

            // ── Daily Quiz ───────────────────────────────────────────────────────
            composable(Routes.QUIZ) {
                QuizScreen(
                    onNavigateBack = {
                        navController.navigate(Routes.HOME) { popUpTo(0) }
                    },
                    onNavigateToHistory = {
                        navController.navigate(Routes.QUIZ_HISTORY)
                    }
                )
            }

            // ── Watchlist (History + Favorites) ──────────────────────────────────
            composable(Routes.WATCHLIST) {
                WatchlistScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { animeId -> navController.navigate("detail/$animeId") }
                )
            }

            // ── Quiz History ──────────────────────────────────────────────────────
            composable(Routes.QUIZ_HISTORY) {
                QuizHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Discussion (Chat Room) ────────────────────────────────────────────
            composable(
                route = Routes.CHAT,
                arguments = listOf(
                    navArgument("animeId") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument("animeName") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getInt("animeId") ?: -1
                val animeName = backStackEntry.arguments?.getString("animeName")
                DiscussionScreen(initialAnimeId = animeId, initialAnimeName = animeName)
            }

            // ── Anime Detail ─────────────────────────────────────────────────────
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("animeId") { type = NavType.IntType })
            ) {
                AnimeDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onJoinDiscussion = { id, name ->
                        navController.navigate("chat?animeId=$id&animeName=$name")
                    }
                )
            }
            // ── Leaderboard ──────────────────────────────────────────────────────
            composable("leaderboard") {
                com.example.sensai.ui.screens.leaderboard.LeaderboardScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onUserClick = { userId -> navController.navigate("user_profile/$userId") }
                )
            }

            // ── User Profile (View Other Users) ──────────────────────────────────
            composable(
                route = Routes.USER_PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) {
                com.example.sensai.ui.screens.userprofile.UserProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        } // End of NavHost
    } // End of Scaffold
} // End of ModalNavigationDrawer
} // End of AppNavigation
