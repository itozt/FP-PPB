package com.example.moviecatalogue.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moviecatalogue.SplashScreen
import com.example.moviecatalogue.domain.AuthRepository
import com.example.moviecatalogue.domain.MediaType
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.ui.components.glassMorphism
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
    object Detail   : Screen("detail/{movieId}/{mediaType}") {
        fun createRoute(movieId: Int, mediaType: String = "movie") = "detail/$movieId/$mediaType"
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
    onMovieClick: (Int, String) -> Unit,
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
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 28.dp) // Margin bawah untuk nav bar
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassMorphism(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            backgroundColor = Color(0xFF1A1A1A).copy(alpha = 0.90f),
                            strokeColor = Color.White.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp), // <--- Memberikan gap 16.dp murni antar tombol
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        val isSelected = pagerState.currentPage == index
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .glassMorphism(
                                            shape = androidx.compose.foundation.shape.CircleShape,
                                            backgroundColor = Color(0xFFE50914).copy(alpha = 0.25f),
                                            strokeColor = Color(0xFFE50914).copy(alpha = 0.5f)
                                        )
                                )
                            }
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
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
                onMovieClick   = { id, mediaType ->
                    navController.navigate(Screen.Detail.createRoute(id, mediaType))
                },
                onAccountAction = goToLogin
            )
        }
        composable(
            route     = Screen.Detail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType },
                navArgument("mediaType") { type = NavType.StringType; defaultValue = "movie" }
            ),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350))
            }
        ) { backStack ->
            val movieId = backStack.arguments?.getInt("movieId") ?: return@composable
            val mediaTypeStr = backStack.arguments?.getString("mediaType") ?: "movie"
            val mediaType = MediaType.fromString(mediaTypeStr)
            DetailScreen(
                movieId        = movieId,
                mediaType      = mediaType,
                repository     = repository,
                isGuest        = isGuest,
                onBackClick    = { navController.popBackStack() },
                onRequestLogin = goToLogin
            )
        }
    }
}
