package com.example.moviecatalogue.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moviecatalogue.SplashScreen
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.ui.screens.detail.DetailScreen
import com.example.moviecatalogue.ui.screens.home.HomeScreen
import com.example.moviecatalogue.ui.screens.search.SearchScreen
import com.example.moviecatalogue.ui.screens.watchlist.WatchlistScreen

// ─── Screen Routes ────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object Splash   : Screen("splash")
    object Home     : Screen("home")
    object Search   : Screen("search")
    object Watchlist: Screen("watchlist")
    object Detail   : Screen("detail/{movieId}") {
        fun createRoute(movieId: Int) = "detail/$movieId"
    }
}

// ─── Bottom Nav Items ─────────────────────────────────────────────────────────

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,      "Home",      Icons.Filled.Home,           Icons.Outlined.Home),
    BottomNavItem(Screen.Search,    "Search",    Icons.Filled.Search,         Icons.Outlined.Search),
    BottomNavItem(Screen.Watchlist, "Watchlist", Icons.Filled.Bookmark,       Icons.Outlined.BookmarkBorder)
)

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == item.screen.route
            } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label, style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ─── Root Navigation Graph ────────────────────────────────────────────────────

@Composable
fun AppNavigation(repository: MovieRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on the Splash, Detail screens
    val showBottomBar = !currentRoute.orEmpty().startsWith("detail/") && currentRoute != Screen.Splash.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) BottomNavigationBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController   = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(tween(300)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
                )
            },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = {
                fadeIn(tween(300)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(300)
                )
            },
            popExitTransition = {
                fadeOut(tween(200)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(300)
                )
            }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    repository   = repository,
                    onMovieClick = { navController.navigate(Screen.Detail.createRoute(it)) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    repository   = repository,
                    onMovieClick = { navController.navigate(Screen.Detail.createRoute(it)) }
                )
            }
            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    repository   = repository,
                    onMovieClick = { navController.navigate(Screen.Detail.createRoute(it)) }
                )
            }
            composable(
                route     = Screen.Detail.route,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350))
                },
                popExitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350))
                }
            ) { backStack ->
                val movieId = backStack.arguments?.getInt("movieId") ?: return@composable
                DetailScreen(
                    movieId     = movieId,
                    repository  = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
