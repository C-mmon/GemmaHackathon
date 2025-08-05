package com.example.gemmahackathon.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gemmahackathon.ui.screens.*
import com.example.gemmahackathon.viewModel.DiaryViewModel
import com.example.gemmahackathon.viewModel.UserViewModel

// Navigation routes
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object TagCloud : Screen("tag_cloud", "Tags", Icons.Default.Face)
    object MoodTracker : Screen("mood_tracker", "Mood", Icons.Default.Info)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.TagCloud,
    Screen.MoodTracker,
    Screen.Profile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryNavigation(
    diaryViewModel: DiaryViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(diaryViewModel = diaryViewModel)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(diaryViewModel = diaryViewModel)
            }
            composable(Screen.TagCloud.route) {
                TagCloudScreen(diaryViewModel = diaryViewModel)
            }
            composable(Screen.MoodTracker.route) {
                MoodTrackerScreen(diaryViewModel = diaryViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    diaryViewModel = diaryViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}