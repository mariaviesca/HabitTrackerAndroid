package com.habittrackerapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habittrackerapp.ui.achievements.AchievementsScreen
import com.habittrackerapp.ui.habits.AddHabitScreen
import com.habittrackerapp.ui.habits.HabitDetailScreen
import com.habittrackerapp.ui.habits.MonthlyScreen
import com.habittrackerapp.ui.home.HomeScreen
import com.habittrackerapp.ui.report.ReportScreen
import com.habittrackerapp.ui.rewards.RewardsScreen
import com.habittrackerapp.ui.score.ScoreDetailScreen
import com.habittrackerapp.viewmodel.HabitStore

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Habits : Screen("habits", "Habits", Icons.Default.CheckCircle)
    data object Monthly : Screen("monthly", "Monthly", Icons.Default.DateRange)
    data object Rewards : Screen("rewards", "Rewards", Icons.Default.CardGiftcard)
    data object Achievements : Screen("achievements", "Achievements", Icons.Default.EmojiEvents)
}

val bottomTabs = listOf(Screen.Habits, Screen.Monthly, Screen.Rewards, Screen.Achievements)

@Composable
fun AppNavigation(store: HabitStore) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.Rewards && store.pendingGiftBoxes > 0) {
                                    BadgedBox(badge = {
                                        Badge { Text("${store.pendingGiftBoxes}") }
                                    }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = selected,
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
            navController = navController,
            startDestination = Screen.Habits.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Habits.route) {
                HomeScreen(
                    store = store,
                    onAddHabit = { navController.navigate("add_habit") },
                    onHabitClick = { navController.navigate("habit_detail/${it.id}") },
                    onScoreClick = { navController.navigate("score_detail") }
                )
            }
            composable(Screen.Monthly.route) {
                MonthlyScreen(
                    store = store,
                    onReportClick = { navController.navigate("report") }
                )
            }
            composable(Screen.Rewards.route) {
                RewardsScreen(store = store)
            }
            composable(Screen.Achievements.route) {
                AchievementsScreen(store = store)
            }
            composable("add_habit") {
                AddHabitScreen(store = store, onDone = { navController.popBackStack() })
            }
            composable(
                "edit_habit/{habitId}",
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                AddHabitScreen(
                    store = store,
                    editingHabitId = habitId,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(
                "habit_detail/{habitId}",
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                HabitDetailScreen(
                    store = store,
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("edit_habit/$habitId") }
                )
            }
            composable("score_detail") {
                ScoreDetailScreen(store = store, onDone = { navController.popBackStack() })
            }
            composable("report") {
                ReportScreen(store = store, onDone = { navController.popBackStack() })
            }
        }
    }
}
