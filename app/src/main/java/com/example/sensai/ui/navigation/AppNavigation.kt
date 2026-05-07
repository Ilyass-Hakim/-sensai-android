package com.example.sensai.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.Chat

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val token by tokenManager.accessToken.collectAsState(initial = null)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Only show BottomNav on top-level screens
    val topLevelRoutes = listOf("home", "search", "sensei", "chat")
    val showBottomNav = currentDestination?.route in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = currentDestination?.hierarchy?.any { it.route == "search" } == true,
                        onClick = {
                            navController.navigate("search") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Face, contentDescription = "Sensei") },
                        label = { Text("Sensei") },
                        selected = currentDestination?.hierarchy?.any { it.route == "sensei" } == true,
                        onClick = {
                            navController.navigate("sensei") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                        label = { Text("Chat") },
                        selected = currentDestination?.hierarchy?.any { it.route == "chat" } == true,
                        onClick = {
                            navController.navigate("chat") {
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
            startDestination = if (token == null) "auth" else "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile")
                    }
                )
            }

            composable("profile") {
                com.example.sensai.ui.screens.profile.ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate("auth") {
                            popUpTo(0) // Clear entire backstack
                        }
                    }
                )
            }
            
            composable("search") {
                SearchScreen(
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }

            composable("sensei") {
                SenseiChatScreen()
            }

            composable(
                route = "chat?animeId={animeId}&animeName={animeName}",
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
            
            composable(
                route = "detail/{animeId}",
                arguments = listOf(navArgument("animeId") { type = NavType.IntType })
            ) {
                AnimeDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onJoinDiscussion = { id, name ->
                        navController.navigate("chat?animeId=$id&animeName=$name")
                    }
                )
            }
        }
    }
}
