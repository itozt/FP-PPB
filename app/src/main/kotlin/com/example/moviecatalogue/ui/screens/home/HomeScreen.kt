package com.example.moviecatalogue.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.ui.components.MovieCard
import com.example.moviecatalogue.ui.components.MovieSlider
import com.example.moviecatalogue.ui.components.ShimmerCategorySection
import com.example.moviecatalogue.ui.components.ShimmerSliderCard

/**
 * Home Screen — Netflix-style with auto-sliding hero banner + categorised rows.
 *
 * HCI Principles:
 * - Visibility of system status: shimmer skeletons during data fetch
 * - Recognition over recall: labelled category sections with emoji cues
 * - Aesthetic & minimalist: dark layout, no redundant chrome
 * - Error recovery: Snackbar with Retry action on network failure
 */
@Composable
fun HomeScreen(
    repository: MovieRepository,
    onMovieClick: (Int) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
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
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item { HomeTopBar() }

        // Hero slider (trending)
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

        if (uiState.nowPlayingMovies.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "🎬  Now Playing",
                    movies       = uiState.nowPlayingMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (uiState.popularMovies.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "🔥  Popular",
                    movies       = uiState.popularMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (uiState.topRatedMovies.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title        = "⭐  Top Rated",
                    movies       = uiState.topRatedMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = "MovFlix",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
                color      = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text  = "Movies",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MovieCategoryRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 20.dp)) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = movies, key = { it.id }) { movie ->
                MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
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
        item { HomeTopBar() }
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
