package com.example.moviecatalogue.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moviecatalogue.SplashScreen
import com.example.moviecatalogue.domain.AuthRepository
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.ui.screens.auth.LoginScreen
import com.example.moviecatalogue.ui.screens.auth.RegisterScreen
import com.example.moviecatalogue.ui.screens.detail.DetailScreen
import com.example.moviecatalogue.ui.screens.home.HomeScreen
import com.example.moviecatalogue.ui.screens.profile.ProfileScreen
import com.example.moviecatalogue.ui.screens.search.SearchScreen
import kotlinx.coroutines.launch

// ─── Screen Routes ────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object Splash   : Screen("splash")
    object Login    : Screen("login")
    object Register : Screen("register")
    object Main     : Screen("main")
    object Detail   : Screen("detail/{movieId}") {
        fun createRoute(movieId: Int) = "detail/$movieId"
    }
}

// ─── Bottom Nav Items ─────────────────────────────────────────────────────────

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home",    Icons.Filled.Home,   Icons.Outlined.Home),
    BottomNavItem("Search",  Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

// ─── Main screen: swipeable pager + synced bottom bar ──────────────────────────

@Composable
fun MainScreen(
    repository: MovieRepository,
    authRepository: AuthRepository,
    isGuest: Boolean,
    onMovieClick: (Int) -> Unit,
    onAccountAction: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val scope = rememberCoroutineScope()

    // System back on a non-first tab returns to Home before exiting the app.
    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    val isSelected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
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
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    repository = repository,
                    onMovieClick = onMovieClick,
                    isGuest = isGuest,
                    onAccountAction = onAccountAction
                )
                1 -> SearchScreen(repository = repository, onMovieClick = onMovieClick)
                2 -> ProfileScreen(
                    repository = repository,
                    authRepository = authRepository,
                    onMovieClick = onMovieClick,
                    onAccountAction = onAccountAction
                )
            }
        }
    }
}

// ─── Root Navigation Graph ────────────────────────────────────────────────────

@Composable
fun AppNavigation(
    repository: MovieRepository,
    authRepository: AuthRepository
) {
    val navController = rememberNavController()
    val session by authRepository.session.collectAsState()
    val isGuest = session?.isGuest == true

    // Exit to the login screen (used by both Logout and the guest "Masuk" action).
    val goToLogin: () -> Unit = {
        authRepository.logout()
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Main.route) { inclusive = true }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
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
            SplashScreen(
                onTimeout = {
                    val loggedIn = authRepository.session.value != null
                    val target = if (loggedIn) Screen.Main.route else Screen.Login.route
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = authRepository,
                onAuthenticated = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                authRepository = authRepository,
                onAuthenticated = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                repository     = repository,
                authRepository = authRepository,
                isGuest        = isGuest,
                onMovieClick   = { navController.navigate(Screen.Detail.createRoute(it)) },
                onAccountAction = goToLogin
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
                movieId        = movieId,
                repository     = repository,
                isGuest        = isGuest,
                onBackClick    = { navController.popBackStack() },
                onRequestLogin = goToLogin
            )
        }
    }
}
