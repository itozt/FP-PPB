package com.example.moviecatalogue.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moviecatalogue.R
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.domain.WatchProgress
import com.example.moviecatalogue.ui.components.ContinueWatchingCard
import com.example.moviecatalogue.ui.components.MovieCard
import com.example.moviecatalogue.ui.components.MovieSlider
import com.example.moviecatalogue.ui.components.ShimmerCategorySection
import com.example.moviecatalogue.ui.components.ShimmerSliderCard
import com.example.moviecatalogue.ui.components.glassMorphism

/**
 * Home Screen — Netflix-style with auto-sliding hero banner + categorised rows.
 *
 * HCI Principles:
 * - Visibility of system status: shimmer skeletons during data fetch
 * - Recognition over recall: labelled category sections with emoji cues
 * - Aesthetic & minimalist: dark layout, no redundant chrome
 * - Error recovery: Snackbar with Retry action on network failure
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: MovieRepository,
    onMovieClick: (Int, String) -> Unit,
    isGuest: Boolean = false,
    onAccountAction: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message     = msg,
                actionLabel = "Retry",
                duration    = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.loadHome()
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar         = { HomeTopBar(scrollBehavior, isGuest, onAccountAction) },
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        if (uiState.isLoading) {
            HomeShimmerContent(modifier = Modifier.padding(paddingValues))
        } else {
            HomeContent(
                uiState      = uiState,
                onMovieClick = onMovieClick,
                modifier     = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding      = PaddingValues(bottom = 100.dp)
    ) {
        // Hero slider (trending — now includes both movies and TV)
        if (uiState.trendingMovies.isNotEmpty()) {
            item {
                MovieSlider(
                    movies       = uiState.trendingMovies,
                    onMovieClick = onMovieClick,
                    modifier     = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        if (uiState.continueWatching.isNotEmpty()) {
            item {
                ContinueWatchingRow(
                    title = "Lanjutkan Tontonan",
                    icon = Icons.Rounded.PlayCircle,
                    progressList = uiState.continueWatching,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (uiState.nowPlayingMovies.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "Now Playing",
                    icon         = Icons.Rounded.Movie,
                    movies       = uiState.nowPlayingMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (uiState.popularMovies.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "Popular Movies",
                    icon         = Icons.Rounded.LocalFireDepartment,
                    movies       = uiState.popularMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (uiState.popularTvShows.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "Popular TV Series",
                    icon         = Icons.Rounded.Tv,
                    movies       = uiState.popularTvShows,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (uiState.topRatedMovies.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "Top Rated Movies",
                    icon         = Icons.Rounded.Star,
                    movies       = uiState.topRatedMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (uiState.topRatedTvShows.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "Top Rated TV Series",
                    icon         = Icons.Rounded.Star,
                    movies       = uiState.topRatedTvShows,
                    onMovieClick = onMovieClick
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    isGuest: Boolean,
    onAccountAction: () -> Unit
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Image(
                painter = painterResource(R.drawable.movflix_logo),
                contentDescription = "MovFlix",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(36.dp)
            )
        },
        actions = {
            if (isGuest) {
                TextButton(onClick = onAccountAction) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Masuk", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                IconButton(onClick = onAccountAction) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor         = Color.Transparent,
            scrolledContainerColor = Color.Black,
            titleContentColor      = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun MovieCategoryRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    movies: List<Movie>,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text     = title,
                style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color    = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = movies, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id, movie.mediaType.value) }
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    progressList: List<WatchProgress>,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text     = title,
                style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color    = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = progressList, key = { "${it.contentId}_${it.mediaType.value}" }) { progress ->
                ContinueWatchingCard(
                    progress = progress,
                    onClick = { onMovieClick(progress.contentId, progress.mediaType.value) }
                )
            }
        }
    }
}

@Composable
private fun HomeShimmerContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            ShimmerSliderCard(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
        }
        items(3) {
            ShimmerCategorySection()
            Spacer(Modifier.height(12.dp))
        }
    }
}
