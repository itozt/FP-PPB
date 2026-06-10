package com.example.moviecatalogue.ui.screens.watchlist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.ui.components.ShimmerBrush
import com.example.moviecatalogue.ui.components.WatchlistMovieCard
import kotlinx.coroutines.launch

/**
 * Watchlist Screen — persisted offline-first grid of saved movies.
 *
 * HCI Principles:
 * - Undo: Snackbar with "Undo" action prevents accidental deletions
 * - Feedback: count badge, immediate card removal, offline-first
 * - Recognition: visual poster grid is faster to scan than a list
 * - Consistency: same card shape language as Home/Search
 */
@Composable
fun WatchlistScreen(
    repository: MovieRepository,
    onMovieClick: (Int) -> Unit
) {
    val viewModel: WatchlistViewModel = viewModel(factory = WatchlistViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Undo snackbar triggered whenever a movie is deleted
    LaunchedEffect(uiState.recentlyDeletedMovie) {
        uiState.recentlyDeletedMovie?.let { movie ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "\"${movie.title}\" removed",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                } else {
                    viewModel.clearRecentlyDeleted()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WatchlistHeader(movieCount = uiState.movies.size)

            when {
                uiState.isLoading -> WatchlistShimmer()
                uiState.movies.isEmpty() -> EmptyWatchlist()
                else -> WatchlistGrid(
                    movies = uiState.movies,
                    onMovieClick = onMovieClick,
                    onDelete = viewModel::removeMovie
                )
            }
        }
    }
}

@Composable
private fun WatchlistHeader(movieCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Watchlist",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        AnimatedVisibility(visible = movieCount > 0) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "$movieCount saved",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun WatchlistGrid(
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    onDelete: (Movie) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = movies, key = { it.id }) { movie ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(initialScale = 0.93f),
                exit = fadeOut() + scaleOut(targetScale = 0.93f)
            ) {
                WatchlistMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    onDelete = { onDelete(movie) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EmptyWatchlist() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📚", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your watchlist is empty",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the bookmark icon on any movie\nto save it — accessible even offline.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}

@Composable
private fun WatchlistShimmer() {
    val shimmerBrush: Brush = ShimmerBrush()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .background(shimmerBrush)
                )
            }
        }
    }
}
