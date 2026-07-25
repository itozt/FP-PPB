package com.example.moviecatalogue.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import com.example.moviecatalogue.ui.components.glassMorphism
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.ui.components.MovieCard
import com.example.moviecatalogue.ui.components.ShimmerBrush

/**
 * Search & Filter Screen
 *
 * HCI Principles:
 * - Flexibility and efficiency: debounced search, no manual submit required
 * - Recognition over recall: genre chips are always visible
 * - Feedback: loading indicator, empty state messages with clear guidance
 * - Error prevention: 2-char minimum threshold, clear button, genre undo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: MovieRepository,
    onMovieClick: (Int, String) -> Unit
) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor      = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Search Field ─────────────────────────────────────────────────
            SearchField(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onClear = {
                    viewModel.onQueryChange("")
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp) // <--- Top margin agar tidak menabrak status bar
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Genre Filter Chips ───────────────────────────────────────────
            AnimatedVisibility(visible = uiState.displayGenres.isNotEmpty()) {
                GenreFilterRow(
                    genres = uiState.displayGenres,
                    selectedGenreId = uiState.selectedGenreId,
                    onGenreSelected = viewModel::onGenreSelected
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Results Area ─────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> SearchLoadingGrid()

                    uiState.displayResults.isNotEmpty() -> {
                        val canShowMoreButton = uiState.canLoadMore || uiState.filteredResults.size > uiState.displayLimit
                        SearchResultsGrid(
                            movies = uiState.displayResults,
                            isLoadingMore = uiState.isLoadingMore,
                            canLoadMore = canShowMoreButton,
                            onLoadMore = viewModel::loadMore,
                            onMovieClick = { id, mediaType ->
                                keyboardController?.hide()
                                onMovieClick(id, mediaType)
                            }
                        )
                    }

                    uiState.hasSearched && uiState.query.isNotBlank() -> {
                        EmptySearchState(
                            query = uiState.query,
                            hasGenreFilter = uiState.selectedGenreId != null
                        )
                    }

                    else -> SearchHintState()
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search movies, titles...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = androidx.compose.foundation.shape.CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), // <--- Opacity Search Background
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) // <--- Opacity Search Background
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onQueryChange(query) }),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp), // <--- Ukuran teks pencarian
        modifier = modifier // Menghapus height(50.dp) agar teks kembali rata tengah
    )
}

@Composable
private fun GenreFilterRow(
    genres: List<Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(bottom = 16.dp), // <--- Margin bawah untuk area hitam
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .glassMorphism(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        backgroundColor = if (selectedGenreId == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f),
                        strokeColor = if (selectedGenreId == null) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .clickable { onGenreSelected(null) }
                    .padding(horizontal = 16.dp, vertical = 6.dp) // <--- Padding atas bawah (vertical) dan kiri kanan (horizontal)
            ) {
                Text(
                    text = "All",
                    fontSize = 12.sp,
                    color = if (selectedGenreId == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(items = genres, key = { it.id }) { genre ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .glassMorphism(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        backgroundColor = if (selectedGenreId == genre.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f),
                        strokeColor = if (selectedGenreId == genre.id) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .clickable { onGenreSelected(if (selectedGenreId == genre.id) null else genre.id) }
                    .padding(horizontal = 16.dp, vertical = 6.dp) // <--- Padding atas bawah (vertical) dan kiri kanan (horizontal)
            ) {
                Text(
                    text = genre.name,
                    fontSize = 12.sp,
                    color = if (selectedGenreId == genre.id) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchResultsGrid(
    movies: List<Movie>,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onMovieClick: (Int, String) -> Unit
) {
    val listState = rememberLazyGridState()

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 130.dp), // <--- Bottom margin diperbesar agar tidak menabrak nav bottom saat tombol "Tampilkan lebih banyak" hilang
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = movies, key = { "${it.id}_${it.mediaType.value}" }) { movie ->
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id, movie.mediaType.value) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (canLoadMore && movies.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onLoadMore) {
                        Text("Tampilkan lebih banyak...", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchLoadingGrid() {
    val shimmerBrush: Brush = ShimmerBrush()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(9) {
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(shimmerBrush)
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(10.dp)
                                .background(shimmerBrush)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(8.dp)
                                .background(shimmerBrush)
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState(query: String, hasGenreFilter: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Rounded.SentimentDissatisfied,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasGenreFilter) "No results in this genre for\n\"$query\""
            else "No results found for\n\"$query\"",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasGenreFilter) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try removing the genre filter",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SearchHintState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Rounded.Movie,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Find your next favourite film",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Type at least 2 characters to search",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
