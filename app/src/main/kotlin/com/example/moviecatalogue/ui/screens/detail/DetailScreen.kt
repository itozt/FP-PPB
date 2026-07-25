package com.example.moviecatalogue.ui.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.MediaType
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.WatchProgress
import com.example.moviecatalogue.ui.components.glassMorphism
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.domain.TvEpisode
import com.example.moviecatalogue.domain.TvSeason
import com.example.moviecatalogue.ui.components.ShimmerBrush
import com.example.moviecatalogue.ui.components.StreamingFullscreenPlayer
import com.example.moviecatalogue.ui.components.TrailerFullscreenPlayer

/**
 * Detail Screen — full movie/TV info with streaming playback, trailer, and watchlist toggle.
 *
 * HCI Principles:
 * - Chunking: info is grouped into logical sections (header, actions, genres, synopsis, episodes, details)
 * - Direct manipulation: bookmark icon updates instantly (optimistic UI feel)
 * - Visibility of system status: loading shimmer, error state with retry
 * - User control & freedom: back button always accessible at top-left
 * - Constraints: "Watch Trailer" button is disabled when no trailer exists
 */
@Composable
fun DetailScreen(
    movieId: Int,
    mediaType: MediaType,
    repository: MovieRepository,
    isGuest: Boolean,
    onBackClick: () -> Unit,
    onRequestLogin: () -> Unit
) {
    val viewModel: DetailViewModel = viewModel(
        factory = DetailViewModel.Factory(movieId, mediaType, repository)
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
        containerColor  = Color.Transparent,
        // Let the backdrop run edge-to-edge under the status bar; the back/
        // bookmark buttons already use statusBarsPadding() to stay reachable.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic blurred poster background
            uiState.movieDetail?.movie?.let { movie ->
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(movie.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(80.dp)
                )
                // Add a scrim to ensure text remains readable over the blur
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                )
            }
            
            when {
                uiState.isLoading -> DetailShimmer(onBackClick = onBackClick)
                uiState.movieDetail != null -> DetailContent(
                    movieDetail        = uiState.movieDetail!!,
                    mediaType          = uiState.mediaType,
                    isInWatchlist      = uiState.isInWatchlist,
                    isWatchlistLoading = uiState.isWatchlistLoading,
                    onBackClick        = onBackClick,
                    onWatchlistToggle  = onWatchlistToggle,
                    onWatchTrailer     = viewModel::playTrailer,
                    onWatchStream      = viewModel::playStream,
                    // TV
                    episodes           = uiState.episodes,
                    selectedSeason     = uiState.selectedSeason,
                    isLoadingEpisodes  = uiState.isLoadingEpisodes,
                    onSeasonSelected   = viewModel::selectSeason,
                    onEpisodePlay      = viewModel::playEpisode,
                    // Watch Progress
                    watchProgress      = uiState.watchProgress,
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

            // Full-screen streaming overlay
            if (uiState.isStreamingPlaying) {
                StreamingFullscreenPlayer(
                    tmdbId = movieId,
                    mediaType = uiState.mediaType,
                    season = if (uiState.mediaType == MediaType.TV) uiState.selectedSeason else null,
                    episode = if (uiState.mediaType == MediaType.TV) uiState.selectedEpisode else null,
                    startProgress = uiState.resumeTimestamp,
                    onClose = viewModel::closeStream,
                    onProgressUpdate = viewModel::onStreamProgress
                )
            }

            // Sticky Top Bar (Back & Bookmark)
            if (!uiState.isTrailerPlaying && !uiState.isStreamingPlaying) {
                // Back button — always reachable
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.55f), androidx.compose.foundation.shape.CircleShape)
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
                    WatchlistIconButton(uiState.isInWatchlist, uiState.isWatchlistLoading, onWatchlistToggle)
                }
            }
        }
    }
}

// ─── Main Content ─────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(
    movieDetail: MovieDetail,
    mediaType: MediaType,
    isInWatchlist: Boolean,
    isWatchlistLoading: Boolean,
    onBackClick: () -> Unit,
    onWatchlistToggle: () -> Unit,
    onWatchTrailer: () -> Unit,
    onWatchStream: () -> Unit,
    // TV
    episodes: List<TvEpisode>,
    selectedSeason: Int,
    isLoadingEpisodes: Boolean,
    onSeasonSelected: (Int) -> Unit,
    onEpisodePlay: (Int, Int) -> Unit,
    // Watch Progress
    watchProgress: com.example.moviecatalogue.domain.WatchProgress?,
    modifier: Modifier = Modifier
) {
    val movie = movieDetail.movie
    val hasTrailer = !movieDetail.trailerKey.isNullOrBlank()
    val isTv = movieDetail.isTvSeries

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


            // Center play button → opens the streaming player in full-screen landscape.
            FilledIconButton(
                onClick  = onWatchStream,
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
                    contentDescription = "Play",
                    modifier = Modifier.size(38.dp)
                )
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
                    contentDescription = movie.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                // Media type badge
                if (isTv) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "TV SERIES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text     = movie.displayTitle,
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
                if (!isTv && movieDetail.formattedRuntime != "N/A") {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = movieDetail.formattedRuntime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isTv && movieDetail.numberOfSeasons > 0) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = "${movieDetail.numberOfSeasons} Season${if (movieDetail.numberOfSeasons > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height((-16).dp))

        // ── Action Buttons ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primary: "Tonton" button (full width)
            Button(
                onClick  = onWatchStream,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassMorphism(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        strokeColor = Color.White.copy(alpha = 0.2f)
                    ),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape    = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                val hasProgress = watchProgress != null && watchProgress.progress > 0.0 && watchProgress.progress < 1.0
                Text(
                    text = if (hasProgress) "Lanjutkan" else if (isTv) "Tonton" else "Tonton Film",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Secondary row: Trailer + Watchlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onWatchTrailer,
                    enabled  = hasTrailer,
                    modifier = Modifier
                        .weight(1f)
                        .glassMorphism(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            backgroundColor = Color.White.copy(alpha = 0.05f),
                            strokeColor = if (hasTrailer) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                          else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    shape    = androidx.compose.foundation.shape.CircleShape,
                    border   = null,
                    colors   = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                ) {
                    Icon(
                        Icons.Outlined.PlayCircleOutline, null,
                        modifier = Modifier.size(18.dp),
                        tint = if (hasTrailer) MaterialTheme.colorScheme.onSurface
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (hasTrailer) "Trailer" else "No Trailer",
                        color = if (hasTrailer) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick  = onWatchlistToggle,
                    enabled  = !isWatchlistLoading,
                    modifier = Modifier
                        .weight(1f)
                        .glassMorphism(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            backgroundColor = if (isInWatchlist) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                            strokeColor = if (isInWatchlist) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                    shape    = androidx.compose.foundation.shape.CircleShape,
                    border   = null,
                    colors   = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
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

        // ── Episodes (TV Series Only) ─────────────────────────────────────────
        if (isTv && movieDetail.playableSeasons.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            EpisodeSection(
                seasons = movieDetail.playableSeasons,
                selectedSeason = selectedSeason,
                episodes = episodes,
                isLoadingEpisodes = isLoadingEpisodes,
                onSeasonSelected = onSeasonSelected,
                onEpisodePlay = onEpisodePlay
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

// ─── Episode Section ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeSection(
    seasons: List<TvSeason>,
    selectedSeason: Int,
    episodes: List<TvEpisode>,
    isLoadingEpisodes: Boolean,
    onSeasonSelected: (Int) -> Unit,
    onEpisodePlay: (Int, Int) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val currentSeason = seasons.firstOrNull { it.seasonNumber == selectedSeason }

    SectionTitle("Episodes")

    // Season Dropdown
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = currentSeason?.name ?: "Season $selectedSeason",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                seasons.forEach { season ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${season.name} (${season.episodeCount} ep)",
                                fontWeight = if (season.seasonNumber == selectedSeason) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSeasonSelected(season.seasonNumber)
                            dropdownExpanded = false
                        },
                        leadingIcon = if (season.seasonNumber == selectedSeason) {
                            { Text("▶", color = MaterialTheme.colorScheme.primary) }
                        } else null
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Episode List
    if (isLoadingEpisodes) {
        // Shimmer loading for episodes
        val shimmer = ShimmerBrush()
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(shimmer)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            episodes.forEach { episode ->
                EpisodeCard(
                    episode = episode,
                    onPlay = { onEpisodePlay(selectedSeason, episode.episodeNumber) }
                )
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: TvEpisode,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Episode thumbnail
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(75.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (episode.stillUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(episode.stillUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = episode.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Play overlay
                FilledIconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play episode",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Episode info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "${episode.episodeNumber}. ${episode.name}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (episode.formattedRuntime != "N/A") {
                        Text(
                            text = episode.formattedRuntime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (episode.voteAverage > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star, null,
                                tint = Color(0xFFF5C518),
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "%.1f".format(episode.voteAverage),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF5C518)
                            )
                        }
                    }
                    episode.airDate?.let { date ->
                        if (date.isNotBlank()) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (episode.overview.isNotBlank()) {
                    Text(
                        text = episode.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
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
            containerColor = Color.Transparent,
            labelColor     = MaterialTheme.colorScheme.onBackground
        ),
        border  = SuggestionChipDefaults.suggestionChipBorder(
            enabled     = true,
            borderColor = MaterialTheme.colorScheme.outline
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
        androidx.compose.material3.Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
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
