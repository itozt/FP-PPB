package com.example.moviecatalogue.ui.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.ui.components.ShimmerBrush
import com.example.moviecatalogue.ui.components.TrailerFullscreenPlayer

/**
 * Detail Screen — full movie info with trailer playback and watchlist toggle.
 *
 * HCI Principles:
 * - Chunking: info is grouped into logical sections (header, actions, genres, synopsis, details)
 * - Direct manipulation: bookmark icon updates instantly (optimistic UI feel)
 * - Visibility of system status: loading shimmer, error state with retry
 * - User control & freedom: back button always accessible at top-left
 * - Constraints: "Watch Trailer" button is disabled when no trailer exists
 */
@Composable
fun DetailScreen(
    movieId: Int,
    repository: MovieRepository,
    isGuest: Boolean,
    onBackClick: () -> Unit,
    onRequestLogin: () -> Unit
) {
    val viewModel: DetailViewModel = viewModel(
        factory = DetailViewModel.Factory(movieId, repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLoginDialog by remember { mutableStateOf(false) }

    // Guests can't save a watchlist — prompt them to log in instead of toggling.
    val onWatchlistToggle: () -> Unit = {
        if (isGuest) showLoginDialog = true else viewModel.toggleWatchlist()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Login diperlukan") },
            text = { Text("Masuk ke akunmu untuk menyimpan film ke watchlist.") },
            confirmButton = {
                TextButton(onClick = {
                    showLoginDialog = false
                    onRequestLogin()
                }) { Text("Login") }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        containerColor  = MaterialTheme.colorScheme.background,
        // Let the backdrop run edge-to-edge under the status bar; the back/
        // bookmark buttons already use statusBarsPadding() to stay reachable.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> DetailShimmer(onBackClick = onBackClick)
                uiState.movieDetail != null -> DetailContent(
                    movieDetail        = uiState.movieDetail!!,
                    isInWatchlist      = uiState.isInWatchlist,
                    isWatchlistLoading = uiState.isWatchlistLoading,
                    onBackClick        = onBackClick,
                    onWatchlistToggle  = onWatchlistToggle,
                    onWatchTrailer     = viewModel::playTrailer,
                    modifier           = Modifier.padding(padding)
                )
                else -> ErrorDetail(
                    message  = uiState.errorMessage ?: "Something went wrong.",
                    onBack   = onBackClick,
                    onRetry  = viewModel::retryLoad
                )
            }

            // Full-screen trailer overlay (landscape), shown on demand.
            if (uiState.isTrailerPlaying) {
                val keys = uiState.movieDetail?.trailerCandidates.orEmpty()
                if (keys.isNotEmpty()) {
                    TrailerFullscreenPlayer(videoKeys = keys, onClose = viewModel::closeTrailer)
                }
            }
        }
    }
}

// ─── Main Content ─────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(
    movieDetail: MovieDetail,
    isInWatchlist: Boolean,
    isWatchlistLoading: Boolean,
    onBackClick: () -> Unit,
    onWatchlistToggle: () -> Unit,
    onWatchTrailer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val movie = movieDetail.movie
    val hasTrailer = !movieDetail.trailerKey.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {

        // ── Backdrop banner with overlaid controls ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.backdropUrl.ifEmpty { movie.posterUrl })
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim: darken edges for control legibility and blend into the page.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.45f),
                                0.45f to Color.Black.copy(alpha = 0.10f),
                                1.0f to MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Back button — always reachable
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = Color.White
                )
            }

            // Bookmark (top-right quick toggle)
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
            ) {
                WatchlistIconButton(isInWatchlist, isWatchlistLoading, onWatchlistToggle)
            }

            // Center play button → opens the trailer in full-screen landscape.
            if (hasTrailer) {
                FilledIconButton(
                    onClick  = onWatchTrailer,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(66.dp),
                    colors   = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                        contentColor   = Color.White
                    )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play trailer",
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // ── Poster + title row ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-30).dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Card(
                shape     = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier  = Modifier.size(110.dp, 165.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text     = movie.title,
                    style    = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color    = MaterialTheme.colorScheme.onBackground,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (movie.releaseDate.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = movie.releaseDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.Star, null,
                        tint     = Color(0xFFF5C518),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text  = movie.formattedRating,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF5C518)
                    )
                    Text(
                        text  = "(${movie.voteCount.formatCount()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (movieDetail.formattedRuntime != "N/A") {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "⏱ ${movieDetail.formattedRuntime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height((-16).dp))

        // ── Action Buttons ────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick  = onWatchTrailer,
                enabled  = hasTrailer,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape    = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (hasTrailer) "Watch Trailer" else "No Trailer")
            }

            OutlinedButton(
                onClick  = onWatchlistToggle,
                enabled  = !isWatchlistLoading,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
                border   = BorderStroke(
                    1.5.dp,
                    if (isInWatchlist) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(
                    imageVector    = if (isInWatchlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint           = if (isInWatchlist) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurface,
                    modifier       = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = if (isInWatchlist) "Saved" else "Watchlist",
                    color = if (isInWatchlist) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ── Tagline ───────────────────────────────────────────────────────────
        if (movieDetail.tagline.isNotBlank()) {
            Text(
                text      = "\"${movieDetail.tagline}\"",
                style     = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }

        // ── Genres ────────────────────────────────────────────────────────────
        if (movie.genres.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionTitle("Genres")
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                movie.genres.forEach { genre -> GenreChip(genre) }
            }
        }

        // ── Synopsis ──────────────────────────────────────────────────────────
        if (movie.overview.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("Synopsis")
            Text(
                text     = movie.overview,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // ── Extra Details Grid ────────────────────────────────────────────────
        Spacer(Modifier.height(16.dp))
        SectionTitle("Details")
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem("Status",   movieDetail.status.ifBlank { "N/A" },              Modifier.weight(1f))
                InfoItem("Language", movie.originalLanguage.uppercase().ifBlank { "N/A" }, Modifier.weight(1f))
            }
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem("Popularity", "%.0f".format(movie.popularity),  Modifier.weight(1f))
                InfoItem("Votes",      movie.voteCount.formatCount(),     Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Section helpers ──────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color    = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun GenreChip(genre: Genre) {
    SuggestionChip(
        onClick = {},
        label   = { Text(genre.name, style = MaterialTheme.typography.labelMedium) },
        colors  = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor     = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border  = SuggestionChipDefaults.suggestionChipBorder(
            enabled     = true,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
    }
}

// ─── Watchlist Icon Button ──────────────────────────────────────────────────

/** Circular bookmark toggle, reused on the backdrop and above the trailer. */
@Composable
private fun WatchlistIconButton(
    isInWatchlist: Boolean,
    isWatchlistLoading: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick  = onToggle,
        enabled  = !isWatchlistLoading,
        modifier = Modifier.background(Color.Black.copy(alpha = 0.55f), CircleShape)
    ) {
        if (isWatchlistLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(20.dp),
                color       = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector        = if (isInWatchlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                tint               = if (isInWatchlist) MaterialTheme.colorScheme.primary else Color.White
            )
        }
    }
}

// ─── Shimmer Loading State ────────────────────────────────────────────────────

@Composable
private fun DetailShimmer(onBackClick: () -> Unit) {
    val shimmer = ShimmerBrush(targetValue = 1800f)
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(shimmer)
        ) {
            IconButton(
                onClick  = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
        }
        Row(
            modifier              = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-30).dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp, 165.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shimmer)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                Box(Modifier.fillMaxWidth(0.8f).height(22.dp).background(shimmer))
                Box(Modifier.fillMaxWidth(0.5f).height(14.dp).background(shimmer))
                Box(Modifier.fillMaxWidth(0.4f).height(14.dp).background(shimmer))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp)).background(shimmer))
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth(0.3f).height(16.dp).background(shimmer))
            repeat(5) { Box(Modifier.fillMaxWidth().height(14.dp).background(shimmer)) }
        }
    }
}

// ─── Error State ──────────────────────────────────────────────────────────────

@Composable
private fun ErrorDetail(message: String, onBack: () -> Unit, onRetry: () -> Unit) {
    Column(
        modifier              = Modifier.fillMaxSize(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text("⚠️", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Couldn't load movie",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack,  shape = RoundedCornerShape(10.dp)) { Text("Go Back") }
            Button(onClick = onRetry,         shape = RoundedCornerShape(10.dp)) { Text("Retry") }
        }
    }
}

// ─── Utilities ────────────────────────────────────────────────────────────────

private fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${"%.1f".format(this / 1_000_000.0)}M"
    this >= 1_000     -> "${"%.1f".format(this / 1_000.0)}K"
    else              -> toString()
}
